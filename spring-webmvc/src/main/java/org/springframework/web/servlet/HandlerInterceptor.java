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

package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;

/**
 * Workflow interface that allows for customized handler execution chains.
 * Applications can register any number of existing or custom interceptors
 * for certain groups of handlers, to add common preprocessing behavior
 * without needing to modify each handler implementation.
 *
 * 允许自定义处理程序执行链的工作流接口。应用程序可以为某些处理程序组注册任意数量的现有或自定义拦截器，
 * 以添加通用的预处理行为，而不需要修改每个处理程序实现。
 *
 * <p>A HandlerInterceptor gets called before the appropriate HandlerAdapter
 * triggers the execution of the handler itself. This mechanism can be used
 * for a large field of preprocessing aspects, e.g. for authorization checks,
 * or common handler behavior like locale or theme changes. Its main purpose
 * is to allow for factoring out repetitive handler code.
 *
 * 在适当的HandlerAdapter触发处理程序本身的执行之前，HandlerInterceptor会被调用。
 * 这种机制可用于预处理方面的大量领域，例如授权检查，或常见的处理程序行为，如区域设置或主题更改。
 * 它的主要目的是允许分解出重复的处理程序代码。
 *
 * <p>In an asynchronous processing scenario, the handler may be executed in a
 * separate thread while the main thread exits without rendering or invoking the
 * {@code postHandle} and {@code afterCompletion} callbacks. When concurrent
 * handler execution completes, the request is dispatched back in order to
 * proceed with rendering the model and all methods of this contract are invoked
 * again. For further options and details see
 * {@code org.springframework.web.servlet.AsyncHandlerInterceptor}
 *
 * 在异步处理场景中，处理程序可以在独立的线程中执行，
 * 而主线程退出时不呈现或调用{@code postHandle}和{@code afterCompletion}回调函数。
 * 当并发处理程序执行完成时，请求被分派回去，以便继续呈现模型，并再次调用该契约的所有方法。
 * 更多选项和细节请参见{@code org.springframework.web.servlet.AsyncHandlerInterceptor}
 *
 * <p>Typically an interceptor chain is defined per HandlerMapping bean,
 * sharing its granularity. To be able to apply a certain interceptor chain
 * to a group of handlers, one needs to map the desired handlers via one
 * HandlerMapping bean. The interceptors themselves are defined as beans
 * in the application context, referenced by the mapping bean definition
 * via its "interceptors" property (in XML: a &lt;list&gt; of &lt;ref&gt;).
 *
 * 通常，每个HandlerMapping bean定义一个拦截器链，共享其粒度。
 * 为了能够将某个拦截器链应用于一组处理程序，需要通过一个HandlerMapping bean映射所需的处理程序。
 * 拦截器本身在应用程序上下文中被定义为bean，由映射bean定义通过其“拦截器”属性(在XML中:a &lt;list&gt;& lt; ref&gt;)。
 *
 * <p>HandlerInterceptor is basically similar to a Servlet Filter, but in
 * contrast to the latter it just allows custom pre-processing with the option
 * of prohibiting the execution of the handler itself, and custom post-processing.
 * Filters are more powerful, for example they allow for exchanging the request
 * and response objects that are handed down the chain. Note that a filter
 * gets configured in web.xml, a HandlerInterceptor in the application context.
 *
 * HandlerInterceptor基本上类似于Servlet过滤器，
 * 但是与后者相比，它只允许自定义的预处理(可以选择禁止处理程序本身的执行)和自定义的后处理。
 * 过滤器更强大，例如，它们允许交换传递到链上的请求和响应对象。注意，过滤器是在web中配置的xml，
 * 应用程序上下文中的HandlerInterceptor。
 *
 * <p>As a basic guideline, fine-grained handler-related preprocessing tasks are
 * candidates for HandlerInterceptor implementations, especially factored-out
 * common handler code and authorization checks. On the other hand, a Filter
 * is well-suited for request content and view content handling, like multipart
 * forms and GZIP compression. This typically shows when one needs to map the
 * filter to certain content types (e.g. images), or to all requests.
 *
 * 作为一个基本准则，与细粒度处理程序相关的预处理任务是HandlerInterceptor实现的候选对象，
 * 特别是分解的通用处理程序代码和授权检查。
 * 另一方面，过滤器非常适合处理请求内容和视图内容，如多部分表单和GZIP压缩。
 * 当需要将过滤器映射到特定的内容类型(例如图像)，或者映射到所有请求时，这通常会显示出来。
 *
 * @author Juergen Hoeller
 * @since 20.06.2003
 * @see HandlerExecutionChain#getInterceptors
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping#setInterceptors
 * @see org.springframework.web.servlet.handler.UserRoleAuthorizationInterceptor
 * @see org.springframework.web.servlet.i18n.LocaleChangeInterceptor
 * @see org.springframework.web.servlet.theme.ThemeChangeInterceptor
 * @see javax.servlet.Filter
 */
public interface HandlerInterceptor {

	/**
	 * Intercept the execution of a handler. Called after HandlerMapping determined
	 * an appropriate handler object, but before HandlerAdapter invokes the handler.
	 *
	 * 拦截处理程序的执行。在HandlerMapping确定适当的处理程序对象之后调用，但在HandlerAdapter调用处理程序之前调用。
	 *
	 * <p>DispatcherServlet processes a handler in an execution chain, consisting
	 * of any number of interceptors, with the handler itself at the end.
	 * With this method, each interceptor can decide to abort the execution chain,
	 * typically sending an HTTP error or writing a custom response.
	 *
	 * DispatcherServlet在执行链中处理一个处理程序，这个执行链由任意数量的拦截器组成，处理程序本身位于最后。
	 * 使用这个方法，每个拦截器都可以决定中止执行链，通常是发送一个HTTP错误或编写一个自定义响应.
	 *
	 * <p><strong>Note:</strong> special considerations apply for asynchronous
	 * request processing. For more details see
	 *
	 * 异步请求处理有特殊考虑。更多详情请参见
	 *
	 * {@link org.springframework.web.servlet.AsyncHandlerInterceptor}.
	 * <p>The default implementation returns {@code true}.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler chosen handler to execute, for type and/or instance evaluation
	 * @return {@code true} if the execution chain should proceed with the
	 * next interceptor or the handler itself. Else, DispatcherServlet assumes
	 * that this interceptor has already dealt with the response itself.
	 * @throws Exception in case of errors
	 */
	default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		return true;
	}

	/**
	 * Intercept the execution of a handler. Called after HandlerAdapter actually
	 * invoked the handler, but before the DispatcherServlet renders the view.
	 * Can expose additional model objects to the view via the given ModelAndView.
	 *
	 * 拦截处理程序的执行。在HandlerAdapter实际调用处理程序之后调用，
	 * 但在DispatcherServlet呈现视图之前调用。可以通过给定的ModelAndView向视图公开额外的模型对象。
	 *
	 * <p>DispatcherServlet processes a handler in an execution chain, consisting
	 * of any number of interceptors, with the handler itself at the end.
	 * With this method, each interceptor can post-process an execution,
	 * getting applied in inverse order of the execution chain.
	 *
	 * DispatcherServlet在执行链中处理一个处理程序，这个执行链由任意数量的拦截器组成，
	 * 处理程序本身位于最后。使用这种方法，每个拦截器都可以对一次执行进行后处理，以执行链的逆顺序应用。
	 *
	 * <p><strong>Note:</strong> special considerations apply for asynchronous
	 * request processing. For more details see
	 * {@link org.springframework.web.servlet.AsyncHandlerInterceptor}.
	 * <p>The default implementation is empty.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler the handler (or {@link HandlerMethod}) that started asynchronous
	 * execution, for type and/or instance examination
	 * @param modelAndView the {@code ModelAndView} that the handler returned
	 * (can also be {@code null})
	 * @throws Exception in case of errors
	 */
	default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable ModelAndView modelAndView) throws Exception {
	}

	/**
	 * Callback after completion of request processing, that is, after rendering
	 * the view. Will be called on any outcome of handler execution, thus allows
	 * for proper resource cleanup.
	 *
	 * 请求处理完成后的回调，即呈现视图之后。将在处理程序执行的任何结果上被调用，因此允许适当的资源清理。
	 *
	 * <p>Note: Will only be called if this interceptor's {@code preHandle}
	 * method has successfully completed and returned {@code true}!
	 * <p>As with the {@code postHandle} method, the method will be invoked on each
	 * interceptor in the chain in reverse order, so the first interceptor will be
	 * the last to be invoked.
	 *
	 * 注意:只有当拦截器的{@code preHandle}方法成功完成并返回{@code true}时才会被调用!
	 * 与{@code postHandle}方法一样，该方法将在链中的每个拦截器上以相反的顺序调用，因此第一个拦截器将是最后一个被调用的。
	 *
	 * <p><strong>Note:</strong> special considerations apply for asynchronous
	 * request processing. For more details see
	 * {@link org.springframework.web.servlet.AsyncHandlerInterceptor}.
	 * <p>The default implementation is empty.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler the handler (or {@link HandlerMethod}) that started asynchronous
	 * execution, for type and/or instance examination
	 * @param ex any exception thrown on handler execution, if any; this does not
	 * include exceptions that have been handled through an exception resolver
	 * @throws Exception in case of errors
	 */
	default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable Exception ex) throws Exception {
	}

}
