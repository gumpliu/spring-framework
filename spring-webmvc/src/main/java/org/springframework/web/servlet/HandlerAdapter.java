/*
 * Copyright 2002-2018 the original author or authors.
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

/**
 * MVC framework SPI, allowing parameterization of the core MVC workflow.
 * MVC框架SPI，允许参数化核心MVC工作流。
 *
 * <p>Interface that must be implemented for each handler type to handle a request.
 * This interface is used to allow the {@link DispatcherServlet} to be indefinitely
 * extensible. The {@code DispatcherServlet} accesses all installed handlers through
 * this interface, meaning that it does not contain code specific to any handler type.
 *
 * 接口，必须为每个处理程序类型实现该接口才能处理请求。这个接口允许{@link DispatcherServlet}无限扩展。
 * {@code DispatcherServlet}通过这个接口访问所有安装的处理程序，这意味着它不包含特定于任何处理程序类型的代码。
 *
 * <p>Note that a handler can be of type {@code Object}. This is to enable
 * handlers from other frameworks to be integrated with this framework without
 * custom coding, as well as to allow for annotation-driven handler objects that
 * do not obey any specific Java interface.
 *
 * 请注意，处理程序可以是{@code Object}类型的。这是为了使来自其他框架的处理程序能够集成到这个框架中，
 * 而不需要定制代码，并且允许不遵循任何特定Java接口的注释驱动的处理程序对象。
 *
 * <p>This interface is not intended for application developers. It is available
 * to handlers who want to develop their own web workflow.
 *
 * 此接口不适合应用程序开发人员使用。对于希望开发自己的web工作流的处理程序来说，它是可用的。
 *
 * <p>Note: {@code HandlerAdapter} implementors may implement the {@link
 * org.springframework.core.Ordered} interface to be able to specify a sorting
 * order (and thus a priority) for getting applied by the {@code DispatcherServlet}.
 * Non-Ordered instances get treated as lowest priority.
 *
 * 注意:{@code HandlerAdapter}实现者可以实现{@link org.springframework.core.Order}接口
 * 可以为{@code DispatcherServlet}的应用指定排序顺序(以及优先级)。非有序实例的优先级最低。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter
 * @see org.springframework.web.servlet.handler.SimpleServletHandlerAdapter
 */
public interface HandlerAdapter {

	/**
	 * Given a handler instance, return whether or not this {@code HandlerAdapter}
	 * can support it. Typical HandlerAdapters will base the decision on the handler
	 * type. HandlerAdapters will usually only support one handler type each.
	 *
	 * 给定处理程序实例，请返回此{@code HandlerAdapter}是否可以支持它。
	 * 典型的HandlerAdapters将根据处理程序类型做出决定。 HandlerAdapters通常通常仅支持一种处理程序类型。
	 *
	 * <p>A typical implementation:
	 * <p>{@code
	 * return (handler instanceof MyHandler);
	 * }
	 * @param handler the handler object to check
	 * @return whether or not this object can use the given handler
	 */
	boolean supports(Object handler);

	/**
	 * Use the given handler to handle this request.
	 * The workflow that is required may vary widely.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler the handler to use. This object must have previously been passed
	 * to the {@code supports} method of this interface, which must have
	 * returned {@code true}.
	 * @throws Exception in case of errors
	 * @return a ModelAndView object with the name of the view and the required
	 * model data, or {@code null} if the request has been handled directly
	 */
	@Nullable
	ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

	/**
	 * Same contract as for HttpServlet's {@code getLastModified} method.
	 * Can simply return -1 if there's no support in the handler class.
	 *
	 * 与HttpServlet的{@code getLastModified}方法相同。如果处理程序类中没有支持，可以简单地返回-1。
	 *
	 * @param request current HTTP request
	 * @param handler the handler to use
	 * @return the lastModified value for the given handler
	 * @see javax.servlet.http.HttpServlet#getLastModified
	 * @see org.springframework.web.servlet.mvc.LastModified#getLastModified
	 */
	long getLastModified(HttpServletRequest request, Object handler);

}
