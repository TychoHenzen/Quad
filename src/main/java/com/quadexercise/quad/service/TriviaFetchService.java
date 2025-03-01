package com.quadexercise.quad.service;

import com.quadexercise.quad.utils.ApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Service responsible for fetching trivia data from the API.
 * Handles constructing API URLs and making HTTP requests.
 */
@Service
public class TriviaFetchService {

    private final RestTemplate _restTemplate;

    @Autowired
    public TriviaFetchService(RestTemplate restTemplate) {
        _restTemplate = restTemplate;
    }

    /**
     * Fetches trivia data from the API.
     *
     * @param amount The number of trivia questions to retrieve
     * @return JSON response as a string
     */
    public String fetchTrivia(int amount) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .newInstance()
                .scheme("https")
                .host(ApiConstants.TRIVIA_API_HOST)
                .path(ApiConstants.TRIVIA_API_PATH)
                .queryParam(ApiConstants.PARAM_AMOUNT, amount);

        return _restTemplate.getForObject(
                builder.toUriString(),
                String.class
        );
    }
}
