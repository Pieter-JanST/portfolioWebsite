package com.pj.portfolio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@AutoConfigureMockMvc
public class PortfolioDatabaseTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    // TODO: Should probably not use the actual DB for testing
    @Test
    void testSaveMessageAndCheckDatabase() throws Exception {

        mockMvc.perform(post("/save-message")
                        .param("name", "test")
                        .param("email", "test@gmail.com")
                        .param("message", "test Database Post Message"))
                .andExpect(status().isFound());

        // Query the database to verify the new entry
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM  portfolio_db.messages WHERE name = ? AND email = ? AND msg_body = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, "test");
            preparedStatement.setString(2, "test@gmail.com");
            preparedStatement.setString(3, "test Database Post Message");
            ResultSet resultSet = preparedStatement.executeQuery();

            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getString("name")).isEqualTo("test");
            assertThat(resultSet.getString("email")).isEqualTo("test@gmail.com");
            assertThat(resultSet.getString("msg_body")).isEqualTo("test Database Post Message");

            // Check deletion too
            String deleteQuery = "DELETE FROM portfolio_db.messages WHERE name = ? AND email = ? AND msg_body = ?";
            PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
            deleteStatement.setString(1, "test");
            deleteStatement.setString(2, "test@gmail.com");
            deleteStatement.setString(3, "test Database Post Message");
            int rowsDeleted = deleteStatement.executeUpdate();

            assertThat(rowsDeleted).isEqualTo(1);
        }
    }
}