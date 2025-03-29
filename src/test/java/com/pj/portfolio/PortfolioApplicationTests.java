package com.pj.portfolio;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class MyWebAppTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private DataSource dataSource;

	@Test
	void testHomePage() {
		ResponseEntity<String> response = restTemplate.getForEntity("/", String.class);

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).contains("Pieter-Jan");
	}

	@Test
	void testContactButton() {
		ResponseEntity<String> response = restTemplate.getForEntity("/#contact", String.class);

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).contains("Contact Me");
	}

	@Test
	void testResume() throws Exception {
		mockMvc.perform(get("/PieterJanSterkens_cv.pdf"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/pdf"));
	}

	@Test
	void testRateLimiterForSaveMessage() throws Exception {
		// First valid request
		mockMvc.perform(post("/save-message")
						.param("name", "test1")
						.param("email", "test1@gmail.com")
						.param("message", "Message 1"))
				.andExpect(status().isFound()); // Should pass
		cleanDatabase("test1", "test1@gmail.com", "Message 1");

		// Second valid request
		mockMvc.perform(post("/save-message")
						.param("name", "test2")
						.param("email", "test2@gmail.com")
						.param("message", "Message 2"))
				.andExpect(status().isFound()); // Should pass
		cleanDatabase("test2", "test2@gmail.com", "Message 2");

		// Third request should exceed the rate limit
		mockMvc.perform(post("/save-message")
						.param("name", "test3")
						.param("email", "test3@gmail.com")
						.param("message", "Message 3"))
				.andExpect(status().isFound()) // Redirect or error due to rate limiting
				.andExpect(result -> {
					String redirectUrl = result.getResponse().getRedirectedUrl();
                    assert redirectUrl.contains("rateLimitError=Message+sending+limit+reached");
				});
	}

	// Clean the added database entries
	 void cleanDatabase(String name, String email, String message) throws SQLException {
		 try (Connection connection = dataSource.getConnection()) {
			 String deleteQuery = "DELETE FROM portfolio_db.messages WHERE name = ? AND email = ? AND msg_body = ?";
			 PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
			 deleteStatement.setString(1, name);
			 deleteStatement.setString(2, email);
			 deleteStatement.setString(3, message);
			 deleteStatement.executeUpdate();
		 }
	 }
}