package com.example.service.impl;

import com.example.service.CountryService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CountryServiceImpl implements CountryService {
    private final RestTemplate restTemplate;

    @Value("${app.ip-api-url}")
    private String ipApiUrl;

    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String LOCAL_COUNTRY = "PL";
    private static final String IP_TO_COUNTRY_CACHE = "ipToCountry";

    @Override
    @CircuitBreaker(name = "countryService", fallbackMethod = "getCountryFallback")
    @Cacheable(value = IP_TO_COUNTRY_CACHE, key = "#ipAddress")
    public Optional<String> getCountryFromIp(String ipAddress) {
        if (LOCALHOST_IP.equals(ipAddress)) {
            log.info("Localhost IP detected, returning default country: {}", LOCAL_COUNTRY);
            return Optional.of(LOCAL_COUNTRY);
        }

        String url = ipApiUrl + ipAddress;
        log.info("Fetching country for IP: {}", ipAddress);
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            String country = (String) response.get("countryCode");
            log.info("Country for IP {}: {}", ipAddress, country);
            return Optional.ofNullable(country);
        } catch (Exception e) {
            log.error("Failed to fetch country for IP: {}", ipAddress, e);
            return Optional.empty();
        }
    }

    public Optional<String> getCountryFallback(String ipAddress, Throwable t) {
        log.error("Country service fallback triggered for IP: {}", ipAddress, t);
        return Optional.empty();
    }
}