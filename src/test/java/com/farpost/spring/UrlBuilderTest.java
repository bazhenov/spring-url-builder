package com.farpost.spring;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

import static com.farpost.spring.UrlBuilder.build;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

public class UrlBuilderTest {

	private final HttpServletRequest request = new RequestStub("/");

	@Test
	public void shouldBeAbleToBuildUrlForSimpleMappings() {
		class Controller {

			@RequestMapping("/hello")
			public void handle() {
			}
		}

		String url = build(request, Controller.class, "#handle").asString();
		assertThat(url, equalTo("/hello"));
	}

	@Test
	public void shouldBeAbleToBuildUrlWithParameters() {
		class Controller {

			@RequestMapping("/hello")
			public void handle() {
			}
		}

		String url = build(request, Controller.class, "#handle").
			parameter("name", "John").
			asString();
		assertThat(url, equalTo("/hello?name=John"));
	}

	@Test
	public void shouldBeAbleToPassParametersInPattern() {
		class Controller {

			@RequestMapping("/blog/{date}")
			public void handle() {
			}
		}

		String url = build(request, Controller.class, "#handle").
			parameter("date", "2010-01-21").
			asString();
		assertThat(url, equalTo("/blog/2010-01-21"));
	}

	@Test
	public void shouldBeAbleToProcessClassLevelAnnotations() {

		@RequestMapping("/module/*")
		class Controller {

			@RequestMapping("foo")
			public void handle() {
			}
		}

		String url = build(request, Controller.class, "#handle").asString();
		assertThat(url, equalTo("/module/foo"));
	}

	@Test
	public void shouldBeAbleToProcessContextUrls() {

		class Controller {

			@RequestMapping("/foo")
			public void handle() {
			}
		}

		String url = build(new RequestStub("/bar"), Controller.class, "#handle").asString();
		assertThat(url, equalTo("/bar/foo"));
	}

	@Test
	public void shouldBeAbleToBuildUrlsWithMissingParameters() {
		@RequestMapping("/booking/details.htm")
		class DetailsController {

			@RequestMapping(method = GET)
			public void showForm(@RequestParam String source, @RequestParam String target) {}
		}

		String url = build(request, DetailsController.class, "#showForm").asString();
		assertThat(url, equalTo("/booking/details.htm"));
	}

	@Test
	public void shouldBeAbleToWithWithMultipleBindings() {
		class PurchaseController {

			@RequestMapping(value={"/source-{srcAirport}-target-{tgtAirport}.htm", "/full.html"})
			public void serve() {}
		}
		String url = build(request, PurchaseController.class, "#serve").asString();
		assertThat(url, equalTo("/full.html"));

		url = build(request, PurchaseController.class, "#serve").
			parameter("srcAirport", "VVO").
			parameter("tgtAirport", "BJNG").
			asString();
		assertThat(url, equalTo("/source-VVO-target-BJNG.htm"));
	}
}
