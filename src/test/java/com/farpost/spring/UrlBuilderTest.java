package com.farpost.spring;

import org.springframework.web.bind.annotation.RequestMapping;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

import static com.farpost.spring.UrlBuilder.build;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UrlBuilderTest {

	@Test
	public void shouldBeAbleToBuildUrlForSimpleMappings() {
		class SimpleController {
			@RequestMapping("/hello")
			public void handle() {}
		}

		HttpServletRequest request = new RequestStub("/");
		String url = build(request, SimpleController.class, "#handle").asString();
		assertThat(url, equalTo("/hello"));
	}
}
