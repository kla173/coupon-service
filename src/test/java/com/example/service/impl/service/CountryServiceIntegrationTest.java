package com.example.service.impl.service;

import com.example.service.impl.CountryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class CountryServiceIntegrationTest {
    @Autowired
    private CountryServiceImpl countryService;

    @MockitoBean
    private RestTemplate restTemplate;

    private static final String BASE_URL = "http://fake-api.com/";

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(countryService, "ipApiUrl", BASE_URL);
    }

    @Test
    void shouldReturnPLForLocalhostIp() {
        // When
        Optional<String> result = countryService.getCountryFromIp("127.0.0.1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("PL");
    }

    @Test
    void shouldReturnCountryFromExternalApi() {
        // Given
        String ip = "8.8.8.8";
        String url = BASE_URL + ip;

        Map<String, Object> response = new HashMap<>();
        response.put("countryCode", "US");

        when(restTemplate.getForObject(eq(url), eq(Map.class))).thenReturn(response);

        // When
        Optional<String> result = countryService.getCountryFromIp(ip);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("US");
        verify(restTemplate).getForObject(eq(url), eq(Map.class));
    }

    @Test
    void shouldReturnEmptyIfApiFails() {
        // Given
        String ip = "9.9.9.9";
        String url = BASE_URL + ip;

        when(restTemplate.getForObject(eq(url), eq(Map.class)))
                .thenThrow(new RuntimeException("API error"));

        // When
        Optional<String> result = countryService.getCountryFromIp(ip);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyIfNoCountryInApiResponse() {
        // Given
        String ip = "6.6.6.6";
        String url = BASE_URL + ip;

        Map<String, Object> response = new HashMap<>();

        when(restTemplate.getForObject(eq(url), eq(Map.class))).thenReturn(response);

        // When
        Optional<String> result = countryService.getCountryFromIp(ip);

        // Then
        assertThat(result).isEmpty();
    }
}