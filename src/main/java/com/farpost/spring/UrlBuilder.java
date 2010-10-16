package com.farpost.spring;

import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * Simple url build API for Spring Annotation based controllers.
 * <p/>
 * This class allows you to build urls for controllers annotated with {@link RequestMapping @RequestMapping}
 * annotation.
 * <p/>
 * Suppose you have following controller:
 * <pre>
 * class MyController {
 *   <code>@RequestMapping("/dashboard")</code>
 *   public void handleDashboard() {
 *		}
 * }
 * </pre>
 * then following code build url for this controller:
 * <pre>
 * String url = UrlBuilder.
 *   build(request, MyController.class, "#handleDashboard").
 *   asString();
 * // url = "/dashboard"
 * </pre>
 * <code>request</code> variable is type of {@link HttpServletRequest} and needed for correct contaxt path url
 * resolving.
 *
 * @author Denis Bazhenov <dotsid@gmail.com>
 */
public class UrlBuilder {

	private final HttpServletRequest request;
	private final Class<?> type;
	private final String urlPattern;

	private UrlBuilder(HttpServletRequest request, Class<?> type, String methodName) {
		this.request = request;
		this.type = type;
		urlPattern = getUrlPattern(type, methodName);
	}

	/**
	 * This method resolves url pattern from controller type and method name.
	 *
	 * @param type			 controller class object
	 * @param methodName method name which url pattern is needed
	 * @return url pattern
	 */
	private static String getUrlPattern(Class<?> type, String methodName) {
		Method handlerMethod = null;

		for (Method m : type.getMethods()) {
			if (m.getName().equals(methodName)) {
				if (handlerMethod != null) {
					throw new IllegalArgumentException("More than one method with name: " + methodName + "() found " +
						"on class: " + type.getName());
				}
				handlerMethod = m;
			}
		}

		if (handlerMethod == null) {
			throw new IllegalArgumentException("Method " + methodName + "() not found on type: " + type.getName());
		}

		RequestMapping mapping = handlerMethod.getAnnotation(RequestMapping.class);
		if (mapping == null) {
			throw new IllegalArgumentException("Method " + methodName + "() on type: " + type.getName() +
				" doesn't have @RequestMapping annotation");
		}
		// At the moment we'll take only first pattern
		return mapping.value()[0];
	}

	/**
	 * Static factory for {@link UrlBuilder}.
	 * <p/>
	 * Method name should have leading '#' symbol. This is intended constraint aimed at more simple rename refactoring
	 * of controller handler methods.
	 *
	 * @param request		http request object
	 * @param type			 controller class object
	 * @param methodName method name with leading # symbol
	 * @return url builder object
	 */
	public static UrlBuilder build(HttpServletRequest request, Class<?> type, String methodName) {
		if (methodName == null || request == null || type == null) {
			throw new NullPointerException();
		}
		if (methodName.length() <= 1) {
			throw new IllegalArgumentException("Method name should be non empty string");
		}
		if (methodName.charAt(0) != '#') {
			throw new IllegalArgumentException("Method name should have leading '#' symbol. For example: '#handle'");
		}
		return new UrlBuilder(request, type, methodName.substring(1));
	}

	public String asString() {
		return urlPattern;
	}
}
