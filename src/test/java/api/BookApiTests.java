package api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.liudmylamalomuzh.entity.Book;
import org.liudmylamalomuzh.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;
import static org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.json;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class BookApiTests {
    @Autowired
    private MockMvc mockMvc;
    private BookRepository bookRepository;
    @Value("${admin.password}")
    private String adminPwd;

    public static String generateBasicAuthHeader(String username, String password) {
        // Concatenate username and password with a colon
        String credentials = username + ":" + password;

        // Encode the credentials in Base64
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        // Combine "Basic " with the encoded credentials
        return "Basic " + encodedCredentials;
    }
    private final String serviceUrl = "http://localhost:8080";
    @Test
    void contextLoads() throws Exception {
        assertNotNull(mockMvc);
    }

    @Test
    void loginToAdmin() throws Exception {
        Map<String, String> credentials = Map.of(
                "email", "admin@test.com",
                "password", adminPwd
        );

        String requestBody = new ObjectMapper().writeValueAsString(credentials);

        mockMvc.perform(post(serviceUrl + "/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    public void testAddingBooks() throws Exception {
        var values = new HashMap<String, String>(){{
            put("author", "George Orwell");
            put("title", "Animal farm");
        }};
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(values);
        this.mockMvc.perform(post(requestBody, URI.create(serviceUrl+"/api/books")))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().string("{\"id\": 3}"));
    }
    @Test
    public void testAddBook() throws IOException, InterruptedException {
        var values = new HashMap<String, String>(){{
            put("author", "George Orwell");
            put("title", "Animal farm");
        }};
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(values);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serviceUrl))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(response.statusCode(), 201);
        assertEquals(response.body(), "{\"id\": 1}");
    }
    // GET all books -> assert that there is one book
    @Test
    public void testGetBooks() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serviceUrl))
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(response.statusCode(), 200);
        assertEquals((long) bookRepository.findAll().size(), 1);
    }
    // DELETE book
    @Test
    public void testDeleteBook() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serviceUrl + "/1"))
                .DELETE()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(response.statusCode(), 204);
    }
    // GET all books -> assert that there is no books
    @Test
    public void testDeleteBookInRepo() throws IOException, InterruptedException {
        List<Book> books =  bookRepository.findAll();
        assertEquals(books.size(), 0);
    }
}
