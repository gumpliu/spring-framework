/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 * Servlet 3.0 {@link ServletContainerInitializer} designed to support code-based
 * configuration of the servlet container using Spring's {@link WebApplicationInitializer}
 * SPI as opposed to (or possibly in combination with) the traditional
 * {@code web.xml}-based approach.
 *
 * Servlet 3.0 {@link ServletContainerInitializer}设计为使用Spring的{@link WebApplicationInitializer}
 * SPI支持基于代码的servlet容器配置，这与传统的基于{@code web.xml}的方法相反（或可能与之结合）。
 *
 * <h2>Mechanism of Operation</h2>
 * This class will be loaded and instantiated and have its {@link #onStartup}
 * method invoked by any Servlet 3.0-compliant container during container startup assuming
 * that the {@code spring-web} module JAR is present on the classpath. This occurs through
 * the JAR Services API {@link ServiceLoader#load(Class)} method detecting the
 * {@code spring-web} module's {@code META-INF/services/javax.servlet.ServletContainerInitializer}
 * service provider configuration file. See the
 * <a href="https://download.oracle.com/javase/6/docs/technotes/guides/jar/jar.html#Service%20Provider">
 * JAR Services API documentation</a> as well as section <em>8.2.4</em> of the Servlet 3.0
 * Final Draft specification for complete details.
 *
 * 假设在类路径中存在{@code spring-web}模块JAR，则将在容器启动期间由任何与Servlet 3.0
 * 兼容的容器调用该类并将其实例化，并使其{@link #onStartup}方法被调用。
 * 这是通过JAR Services API {@link ServiceLoader＃load（Class）}方法检测到{@code spring-web}
 * 模块的{@code META-INF / services / javax.servlet.ServletContainerInitializer}服务提供程序配置文件而发生的。
 * 请参见<a href="https://download.oracle.com/javase/6/docs/technotes/guides/jar/jar.html#Service%20Provider">
 *     JAR Services API文档</a>以及本节 有关完整详细信息，请参见Servlet 3.0最终草案规范的<em> 8.2.4 </ em>。
 *
 *
 * <h3>In combination with {@code web.xml}</h3>
 * A web application can choose to limit the amount of classpath scanning the Servlet
 * container does at startup either through the {@code metadata-complete} attribute in
 * {@code web.xml}, which controls scanning for Servlet annotations or through an
 * {@code <absolute-ordering>} element also in {@code web.xml}, which controls which
 * web fragments (i.e. jars) are allowed to perform a {@code ServletContainerInitializer}
 * scan. When using this feature, the {@link SpringServletContainerInitializer}
 * can be enabled by adding "spring_web" to the list of named web fragments in
 * {@code web.xml} as follows:
 *
 * 一个Web应用程序可以选择通过限制{@code web.xml}中的{@code meta-complete}属性来限制Servlet容器在启动时扫描类路径的数量，
 * 该属性控制Servlet注释的扫描或通过{@code {@code web.xml}中的<absolute-ordering>}元素，
 * 它控制允许哪些Web片段（即jar）执行{@code ServletContainerInitializer}扫描。
 * 使用此功能时，可以通过将“ spring_web”添加到{@code web.xml}中的命名Web片段列表中来启用
 * {@link SpringServletContainerInitializer}，如下所示：
 *
 * <pre class="code">
 * &lt;absolute-ordering&gt;
 *   &lt;name>some_web_fragment&lt;/name&gt;
 *   &lt;name>spring_web&lt;/name&gt;
 * &lt;/absolute-ordering&gt;
 * </pre>
 *
 * <h2>Relationship to Spring's {@code WebApplicationInitializer}</h2>
 * Spring's {@code WebApplicationInitializer} SPI consists of just one method:
 * {@link WebApplicationInitializer#onStartup(ServletContext)}. The signature is intentionally
 * quite similar to {@link ServletContainerInitializer#onStartup(Set, ServletContext)}:
 * simply put, {@code SpringServletContainerInitializer} is responsible for instantiating
 * and delegating the {@code ServletContext} to any user-defined
 * {@code WebApplicationInitializer} implementations. It is then the responsibility of
 * each {@code WebApplicationInitializer} to do the actual work of initializing the
 * {@code ServletContext}. The exact process of delegation is described in detail in the
 * {@link #onStartup onStartup} documentation below.
 *
 * Spring的{@code WebApplicationInitializer} SPI仅包含一种方法：
 * {@link WebApplicationInitializer＃onStartup（ServletContext）}。
 * 签名故意与{@link ServletContainerInitializer＃onStartup（Set，ServletContext）}非常相似：
 * 简而言之，{@ code SpringServletContainerInitializer}负责将{@code ServletContext}
 * 实例化并委派给任何用户定义的{@code WebApplicationInitializer} 实现。
 * 然后，每个{@code WebApplicationInitializer}都有责任进行初始化{@code ServletContext}的实际工作。
 * 下面的{@link #onStartup onStartup}文档中详细描述了委派的确切过程。
 *
 *
 * <h2>General Notes</h2>
 * In general, this class should be viewed as <em>supporting infrastructure</em> for
 * the more important and user-facing {@code WebApplicationInitializer} SPI. Taking
 * advantage of this container initializer is also completely <em>optional</em>: while
 * it is true that this initializer will be loaded and invoked under all Servlet 3.0+
 * runtimes, it remains the user's choice whether to make any
 * {@code WebApplicationInitializer} implementations available on the classpath. If no
 * {@code WebApplicationInitializer} types are detected, this container initializer will
 * have no effect.
 *
 * 通常，对于更重要且面向用户的{@code WebApplicationInitializer} SPI，
 * 此类应被视为<em>支持基础结构</ em>。 利用此容器初始化程序也是完全<em> optional </ em>：
 * 尽管确实会在所有Servlet 3.0+运行时下加载并调用该初始化程序，
 * 但用户仍可以选择是否进行任何{@code WebApplicationInitializer }在类路径上可用的实现。
 * 如果未检测到{@code WebApplicationInitializer}类型，则此容器初始化程序将无效。
 *
 * <p>Note that use of this container initializer and of {@code WebApplicationInitializer}
 * is not in any way "tied" to Spring MVC other than the fact that the types are shipped
 * in the {@code spring-web} module JAR. Rather, they can be considered general-purpose
 * in their ability to facilitate convenient code-based configuration of the
 * {@code ServletContext}. In other words, any servlet, listener, or filter may be
 * registered within a {@code WebApplicationInitializer}, not just Spring MVC-specific
 * components.
 *
 * 请注意，使用容器初始化器和{@code WebApplicationInitializer}不会以任何方式“绑定”到Spring MVC上，
 * 除非这些类型是在{@code spring-web}模块JAR中提供的。 相反，它们可以被认为具有通用性，
 * 因为它们能够促进{@code ServletContext}的基于代码的便捷配置。
 * 换句话说，任何Servlet，侦听器或过滤器都可以在{@code WebApplicationInitializer}中注册，
 * 而不仅仅是Spring MVC特定的组件。
 *
 *
 * <p>This class is neither designed for extension nor intended to be extended.
 *  * It should be considered an internal type, with {@code WebApplicationInitializer}
 *  * being the public-facing SPI.
 *
 *  此类既不是为扩展而设计的，也不是旨在扩展的。 应该将其视为内部类型，其中{@code WebApplicationInitializer}是面向公众的SPI。
 *
 * <h2>See Also</h2>
 * See {@link WebApplicationInitializer} Javadoc for examples and detailed usage
 * recommendations.<p>
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 3.1
 * @see #onStartup(Set, ServletContext)
 * @see WebApplicationInitializer
 */
@HandlesTypes(WebApplicationInitializer.class)
public class SpringServletContainerInitializer implements ServletContainerInitializer {

	/**
	 * Delegate the {@code ServletContext} to any {@link WebApplicationInitializer}
	 * implementations present on the application classpath.
	 *
	 * 将{@code ServletContext}委托给应用程序类路径上存在的任何{@link WebApplicationInitializer}实现。
	 *
	 * <p>Because this class declares @{@code HandlesTypes(WebApplicationInitializer.class)},
	 * Servlet 3.0+ containers will automatically scan the classpath for implementations
	 * of Spring's {@code WebApplicationInitializer} interface and provide the set of all
	 * such types to the {@code webAppInitializerClasses} parameter of this method.
	 *
	 * 由于此类声明了@ {@code HandlesTypes（WebApplicationInitializer.class）}，
	 * 因此Servlet 3.0+容器将自动扫描类路径以查找Spring的{@code WebApplicationInitializer}接口的实现，
	 * 并将所有此类类型的集合提供给{@code webAppInitializerClasses} 此方法的参数。
	 *
	 * <p>If no {@code WebApplicationInitializer} implementations are found on the classpath,
	 * this method is effectively a no-op. An INFO-level log message will be issued notifying
	 * the user that the {@code ServletContainerInitializer} has indeed been invoked but that
	 * no {@code WebApplicationInitializer} implementations were found.
	 *
	 * 如果在类路径上没有找到{@code WebApplicationInitializer}实现，则此方法实际上是无操作的。
	 * 将发出INFO级别的日志消息，通知用户确实已调用{@code ServletContainerInitializer}，
	 * 但是未找到{@code WebApplicationInitializer}实现。
	 *
	 * <p>Assuming that one or more {@code WebApplicationInitializer} types are detected,
	 * they will be instantiated (and <em>sorted</em> if the @{@link
	 * org.springframework.core.annotation.Order @Order} annotation is present or
	 * the {@link org.springframework.core.Ordered Ordered} interface has been
	 * implemented). Then the {@link WebApplicationInitializer#onStartup(ServletContext)}
	 * method will be invoked on each instance, delegating the {@code ServletContext} such
	 * that each instance may register and configure servlets such as Spring's
	 * {@code DispatcherServlet}, listeners such as Spring's {@code ContextLoaderListener},
	 * or any other Servlet API componentry such as filters.
	 *
	 * 假设检测到一种或多种{@code WebApplicationInitializer}类型，
	 * 则将实例化它们（并进行<em> sorted </ em>，
	 * 如果存在@ {@link org.springframework.core.annotation.Order @Order}注释或
	 * {@link org.springframework.core.Ordered Ordered}接口已实现）。
	 * 然后，将在每个实例上调用{@link WebApplicationInitializer＃onStartup（ServletContext）}方法，
	 * 委派{@code ServletContext}，以便每个实例可以注册和配置Servlet（例如Spring的{@code DispatcherServlet}）
	 * 以及侦听器（例如Spring的{ @code ContextLoaderListener}或任何其他Servlet API组件，例如过滤器。
	 *
	 * @param webAppInitializerClasses all implementations of
	 * {@link WebApplicationInitializer} found on the application classpath
	 * @param servletContext the servlet context to be initialized
	 * @see WebApplicationInitializer#onStartup(ServletContext)
	 * @see AnnotationAwareOrderComparator
	 */
	@Override
	public void onStartup(@Nullable Set<Class<?>> webAppInitializerClasses, ServletContext servletContext)
			throws ServletException {

		List<WebApplicationInitializer> initializers = Collections.emptyList();

		if (webAppInitializerClasses != null) {
			initializers = new ArrayList<>(webAppInitializerClasses.size());
			for (Class<?> waiClass : webAppInitializerClasses) {
				// Be defensive: Some servlet containers provide us with invalid classes,
				// no matter what @HandlesTypes says...
				if (!waiClass.isInterface() && !Modifier.isAbstract(waiClass.getModifiers()) &&
						WebApplicationInitializer.class.isAssignableFrom(waiClass)) {
					try {
						initializers.add((WebApplicationInitializer)
								ReflectionUtils.accessibleConstructor(waiClass).newInstance());
					}
					catch (Throwable ex) {
						throw new ServletException("Failed to instantiate WebApplicationInitializer class", ex);
					}
				}
			}
		}

		if (initializers.isEmpty()) {
			servletContext.log("No Spring WebApplicationInitializer types detected on classpath");
			return;
		}

		servletContext.log(initializers.size() + " Spring WebApplicationInitializers detected on classpath");
		AnnotationAwareOrderComparator.sort(initializers);
		for (WebApplicationInitializer initializer : initializers) {
			initializer.onStartup(servletContext);
		}
	}

}
