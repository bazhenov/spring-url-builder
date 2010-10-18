package com.farpost.spring;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

import static com.farpost.spring.UrlBuilder.build;

public class Functions {

	public static String buildUrl(HttpServletRequest request, Endpoint endpoint) {
		UrlBuilder builder = build(request, endpoint.getType(), endpoint.getMethodName());
		for (Map.Entry<String, String> row : endpoint.getParameters().entrySet()) {
			builder.parameter(row.getKey(), row.getValue());
		}
		return builder.asString();
	}
}
