package com.farpost.spring;

import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.net.URLEncoder.encode;

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
 *				}
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

	private static final String ENCODING = "UTF8";
	private final HttpServletRequest request;
	private final Class<?> type;
	private final String methodName;
	private final Map<String, String> parameters = new HashMap<String, String>();

	private static final Pattern placeholderPattern;

	static {
		placeholderPattern = Pattern.compile("\\{([a-zA-Z]+)\\}");
	}

	private UrlBuilder(HttpServletRequest request, Class<?> type, String methodName) {
		this.request = request;
		this.type = type;
		this.methodName = methodName;
	}

	/**
	 * Calculate and return all parameter names defined in url string.
	 * <p/>
	 * For example if input string is <code>/news/{date}/{id}</code> this method returns set
	 * <code>["date", "id"]</code>.
	 *
	 * @param pattern url pattern
	 * @return set of parameter names
	 */
	private static Set<String> getPlaceholderParameterNames(String pattern) {
		Set<String> set = new HashSet<String>();
		Matcher matches = placeholderPattern.matcher(pattern);
		while (matches.find()) {
			set.add(matches.group(1));
		}
		return set;
	}

	/**
	 * This method resolves url pattern from controller type and method name.
	 *
	 * @param type			 controller class object
	 * @param methodName method name which url pattern is needed
	 * @param parameters the set of given request parameters
	 * @return url pattern
	 */
	private static String getUrlPattern(Class<?> type, String methodName, Set<String> parameters) {
		Method handlerMethod = getHandlerMethod(type, methodName);
		String methodPattern = getMethodUrlPattern(handlerMethod, parameters);

		String classPattern = getClassUrlPattern(type);
		return classPattern != null
			? classPattern.replace("*", methodPattern)
			: methodPattern;

	}

	private static String getMethodUrlPattern(Method handlerMethod, Set<String> parameters) {
		RequestMapping mapping = handlerMethod.getAnnotation(RequestMapping.class);
		if (mapping == null) {
			throw new IllegalArgumentException("Method " + handlerMethod.getName() + "() on type: " +
				handlerMethod.getDeclaringClass().getName() + " doesn't have @RequestMapping annotation");
		}
		String[] urlPatterns = mapping.value();
		for (String pattern : urlPatterns) {
			if (parameters.containsAll(getPlaceholderParameterNames(pattern))) {
				return pattern;
			}
		}
		return "";
	}

	private static Method getHandlerMethod(Class<?> type, String methodName) {
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

		return handlerMethod;
	}

	private static String getClassUrlPattern(Class<?> type) {
		RequestMapping mapping = type.getAnnotation(RequestMapping.class);
		if (mapping == null) {
			return null;
		}
		String[] value = mapping.value();
		if (value.length <= 0) {
			return null;
		} else if (value.length > 1) {
			throw new UnsupportedOperationException("Multiple url pattern on class level are not supported");
		} else {
			return value[0];
		}
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
		if (methodName.charAt(0) != '#') {
			throw new IllegalArgumentException("Method name should have leading '#' symbol. For example: '#handle'");
		}
		methodName = methodName.substring(1);
		if (methodName.isEmpty()) {
			throw new IllegalArgumentException("Method name should be non empty string");
		}
		return new UrlBuilder(request, type, methodName);
	}

	public UrlBuilder parameter(String name, String value) {
		if (name == null) {
			throw new NullPointerException();
		}
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Paramter name should not be empty");
		}
		parameters.put(name, value);
		return this;
	}

	/**
	 * Returns string representation of url
	 *
	 * @return url
	 */
	public String asString() {
		String urlPattern = getUrlPattern(type, methodName, parameters.keySet());
		Set<String> placeholderParameterNames = getPlaceholderParameterNames(urlPattern);
		String url = urlPattern;

		for (String name : placeholderParameterNames) {
			url = url.replace("{" + name + "}", parameters.get(name));
			parameters.remove(name);
		}

		if (!parameters.isEmpty()) {
			url = url + '?' + buildQueryString(parameters);
		}
		String contextPath = request.getContextPath();
		if (!contextPath.equals("/")) {
			url = contextPath + url;
		}
		return url;
	}

	private static String buildQueryString(Map<String, String> params) {
		StringBuilder queryString = new StringBuilder();
		try {
			Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();

			while (iterator.hasNext()) {
				Map.Entry<String, String> row = iterator.next();
				queryString.append(encode(row.getKey(), ENCODING));
				queryString.append('=');
				queryString.append(encode(row.getValue(), ENCODING));
				if (iterator.hasNext()) {
					queryString.append('&');
				}
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return queryString.toString();
	}
}
