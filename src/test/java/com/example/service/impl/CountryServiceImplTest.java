package com.example.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
class CountryServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CountryServiceImpl countryService;

    @BeforeEach
    void setUp() throws Exception {
        Field field = CountryServiceImpl.class.getDeclaredField("ipApiUrl");
        field.setAccessible(true);
        field.set(countryService, "http://fake-api.com/");
    }

    @Test
    void shouldReturnLocalCountryForLocalhostIp() {
        // When
        Optional<String> result = countryService.getCountryFromIp("127.0.0.1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("PL");
    }

    @Test
    void shouldReturnCountryFromApiResponse() {
        // Given
        String ip = "8.8.8.8";
        Map<String, Object> response = new HashMap<>();
        response.put("countryCode", "US");

        when(restTemplate.getForObject("http://fake-api.com/" + ip, Map.class)).thenReturn(response);

        // When
        Optional<String> result = countryService.getCountryFromIp(ip);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("US");
    }

    @Test
    void shouldReturnEmptyWhenApiFails() {
        // Given
        String ip = "1.2.3.4";
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenThrow(new RuntimeException("API error"));

        // When
        Optional<String> result = countryService.getCountryFromIp(ip);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void fallbackShouldReturnEmptyOptional() {
        // When
        Optional<String> result = countryService.getCountryFallback("5.5.5.5", new RuntimeException("boom"));

        // Then
        assertThat(result).isEmpty();
    }
}