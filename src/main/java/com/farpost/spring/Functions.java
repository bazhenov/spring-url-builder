package com.farpost.spring;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

import static com.farpost.spring.PublishRequestFilter.getCurrentRequest;
import static com.farpost.spring.UrlBuilder.build;

public class Functions {

	public static String buildUrl(HttpServletRequest request, Action action) {
		UrlBuilder builder = build(request, action.getType(), action.getMethodName());
		for (Map.Entry<String, String> row : action.getParameters().entrySet()) {
			builder.parameter(row.getKey(), row.getValue());
		}
		return builder.asString();
	}

	public static String buildUrl(Action action) {
		return buildUrl(getCurrentRequest(), action);
	}
}
