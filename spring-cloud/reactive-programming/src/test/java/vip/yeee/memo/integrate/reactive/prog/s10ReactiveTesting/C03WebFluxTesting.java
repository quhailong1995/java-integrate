package vip.yeee.memo.integrate.reactive.prog.s10ReactiveTesting;

import java.math.BigDecimal;

import lombok.var;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Base64Utils;
import vip.yeee.memo.integrate.reactive.prog.ReactiveSpringTutorialApplication;
import vip.yeee.memo.integrate.reactive.prog.domain.Book;
import vip.yeee.memo.integrate.reactive.prog.s03WebfluxAnnotationOrRouter.C03RouterBased;
import vip.yeee.memo.integrate.reactive.prog.s03WebfluxAnnotationOrRouter.C05GlobalErrorWebExceptionHandler;
import vip.yeee.memo.integrate.reactive.prog.s06ReactiveWebSecurity.C01SecurityConfigurer;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
		classes = {
				ReactiveSpringTutorialApplication.class,
				C03RouterBased.class,
				C05GlobalErrorWebExceptionHandler.class,
				C01SecurityConfigurer.class
		}
)
@WebFluxTest
public class C03WebFluxTesting {
	@Autowired
	private WebTestClient webTestClient;
	
	@Test
	public void testWebFlux() {
		var book = Book.builder()
				.category("title category")
				.isbn("1234567")
				.price(BigDecimal.valueOf(19.99))
				.title("test title")
				.build();
		webTestClient.post()
			.uri("/routed/book")
			.bodyValue(book)
			.header("Authorization", "Basic " +
					Base64Utils.encodeToString("admin:secret".getBytes())
			).exchange()
			.expectStatus()
			.isCreated();
	}
}
