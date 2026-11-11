package com.example.medical;

import com.example.medical.dto.DatabaseSnapshot;
import com.example.medical.mapper.DatabaseProbeMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MedicalSystemApplicationTests {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @MockitoBean
    private DatabaseProbeMapper mapper;

    @Test
    void environmentEndpointReturnsMapperQueryValues() throws Exception {
        when(mapper.inspect()).thenReturn(new DatabaseSnapshot("test-database", "2026-11-11 12:00:00", 1));
        HttpResponse<String> response = request("GET", "/api/environment");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"databaseStatus\":\"connected\""));
        assertTrue(response.body().contains("\"databaseVersion\":\"test-database\""));
        assertTrue(response.body().contains("\"probe\":1"));
    }

    @Test
    void databaseFailureReturnsUnavailableWithoutLeakingDriverDetails() throws Exception {
        when(mapper.inspect()).thenThrow(new DataAccessResourceFailureException("private-driver-details"));
        HttpResponse<String> response = request("GET", "/api/environment");
        assertEquals(503, response.statusCode());
        assertTrue(response.body().contains("\"databaseStatus\":\"unavailable\""));
        assertFalse(response.body().contains("private-driver-details"));
    }

    @Test
    void invalidProbeDoesNotReportConnected() throws Exception {
        when(mapper.inspect()).thenReturn(new DatabaseSnapshot("test-database", "2026-11-11 12:00:00", 0));
        assertEquals(503, request("GET", "/api/environment").statusCode());
    }

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
