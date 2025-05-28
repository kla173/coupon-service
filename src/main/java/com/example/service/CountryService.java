package com.example.service;

import java.util.Optional;

public interface CountryService {
    Optional<String> getCountryFromIp(String ipAddress);
}