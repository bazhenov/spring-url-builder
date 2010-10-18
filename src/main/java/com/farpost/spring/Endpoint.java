package com.farpost.spring;

import java.util.HashMap;
import java.util.Map;

public class Endpoint {

	private final Class<?> type;
	private final String methodName;
	private final Map<String, String> parameters;

	public Endpoint(Class<?> type, String methodName, Map<String, String> parameters) {
		this.type = type;
		this.methodName = methodName;
		this.parameters = parameters;
	}

	public Class<?> getType() {
		return type;
	}

	public String getMethodName() {
		return methodName;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}
}
