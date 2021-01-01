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

package org.springframework.context;

/**
 * Callback interface for initializing a Spring {@link ConfigurableApplicationContext}
 * prior to being {@linkplain ConfigurableApplicationContext#refresh() refreshed}.
 *
 * <p>Typically used within web applications that require some programmatic initialization
 * of the application context. For example, registering property sources or activating
 * profiles against the {@linkplain ConfigurableApplicationContext#getEnvironment()
 * context's environment}. See {@code ContextLoader} and {@code FrameworkServlet} support
 * for declaring a "contextInitializerClasses" context-param and init-param, respectively.
 *
 * <p>{@code ApplicationContextInitializer} processors are encouraged to detect
 * whether Spring's {@link org.springframework.core.Ordered Ordered} interface has been
 * implemented or if the {@link org.springframework.core.annotation.Order @Order}
 * annotation is present and to sort instances accordingly if so prior to invocation.
 *
 * 回调接口，用于在刷新{@linkplain ConfigurableApplicationContext#refresh()}
 *  之前初始化一个Spring {@link ConfigurableApplicationContext}。
 *
 * <p>通常用于需要对应用程序上下文进行一些编程初始化的web应用程序中。
 * 例如，根据{@linkplain ConfigurableApplicationContext#getEnvironment()上下文环境}注册属性源或激活配置文件。
 * 请参阅{@code ContextLoader}和{@code FrameworkServlet}对声明“contextInitializerClasses”上下文参数和init-param的支持。
 *
 * <p>{@code ApplicationContextInitializer}鼓励处理器检测Spring的{@link org.springframework.core。已经实现了Ordered}接口，
 * 或者如果@{@link org.springframework.core.annotation。Order Order}注释存在，如果在调用之前存在，则对实例进行相应的排序。
 *
 * @author Chris Beams
 * @since 3.1
 * @param <C> the application context type
 * @see org.springframework.web.context.ContextLoader#customizeContext
 * @see org.springframework.web.context.ContextLoader#CONTEXT_INITIALIZER_CLASSES_PARAM
 * @see org.springframework.web.servlet.FrameworkServlet#setContextInitializerClasses
 * @see org.springframework.web.servlet.FrameworkServlet#applyInitializers
 */
@FunctionalInterface
public interface ApplicationContextInitializer<C extends ConfigurableApplicationContext> {

	/**
	 * Initialize the given application context.
	 * @param applicationContext the application to configure
	 */
	void initialize(C applicationContext);

}
