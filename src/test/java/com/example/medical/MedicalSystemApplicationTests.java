package com.example.medical;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MedicalSystemApplicationTests {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void helloEndpointReturnsExpectedText() throws Exception {
        HttpResponse<String> response = request("GET", "/api/hello");
        assertEquals(200, response.statusCode());
        assertEquals("Hello World! Medical System", response.body());
        assertTrue(response.headers().firstValue("Content-Type")
                .orElse("").startsWith("text/plain"));
    }

    @Test
    void unknownEndpointReturnsNotFound() throws Exception {
        assertEquals(404, request("GET", "/api/not-found").statusCode());
    }

    @Test
    void helloEndpointRejectsUnsupportedMethod() throws Exception {
        assertEquals(405, request("POST", "/api/hello").statusCode());
    }

    private HttpResponse<String> request(String method, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + path))
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

}
