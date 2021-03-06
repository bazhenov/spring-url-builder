package com.farpost.spring;

import java.util.HashMap;
import java.util.Map;

public class Action {

	private final Class<?> type;
	private final String methodName;
	private final Map<String, String> parameters;

	public Action(Class<?> type, String methodName, Map<String, String> parameters) {
		this.type = type;
		this.methodName = methodName;
		this.parameters = parameters;
	}

	public Action(Class<?> type, String methodName) {
		this(type, methodName, new HashMap<String, String>());
	}

	public Action parameter(String name, String value) {
		parameters.put(name, value);
		return this;
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
