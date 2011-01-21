package com.farpost.spring;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class PublishRequestFilter implements Filter {

	private static ThreadLocal<Deque<HttpServletRequest>> stack = new ThreadLocal<Deque<HttpServletRequest>>() {
		@Override
		protected Deque<HttpServletRequest> initialValue() {
			return new ArrayDeque<HttpServletRequest>();
		}
	};

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		Deque<HttpServletRequest> st = stack.get();
		try {
			st.addLast(httpRequest);
			chain.doFilter(httpRequest, response);
		} finally {
			st.pollLast();
		}
	}

	@Override
	public void destroy() {}

	/**
	 * Returns current request or throw {@link IllegalAccessException} if current thread out of request processing
	 * context.
	 *
	 * @return current request object
	 */
	static HttpServletRequest getCurrentRequest() {
		HttpServletRequest request = stack.get().peekLast();
		if (request == null) {
			throw new IllegalStateException("Call to a method out of request context");
		}
		return request;
	}
}
