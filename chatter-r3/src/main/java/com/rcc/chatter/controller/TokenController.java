package com.rcc.chatter.controller;

import java.time.temporal.ChronoUnit;

import org.json.JSONObject;

import com.rcc.chatter.token.TokenStore;

import spark.Request;
import spark.Response;

import static java.time.Instant.now;

public class TokenController {
    private final TokenStore tokenStore;

    public TokenController(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    public JSONObject login(Request request, Response response) {
        String subject = request.attribute("subject");
        var expiry = now().plus(10, ChronoUnit.MINUTES);

        var token = new TokenStore.Token(expiry, subject);
        // tokenId has the same value as session ID
        var tokenId = tokenStore.create(request, token);

        response.status(201);
        return new JSONObject().put("token", tokenId);
    }

    public void validateToken(Request request, Response response) {
        var tokenId = request.headers("X-CSRF-Token");
        if (tokenId == null) return;

        // check if a token is present and not expired
        tokenStore.read(request, tokenId).ifPresent(token -> {
            if (now().isBefore(token.expiry)) {
                // populate the request subject attribute and any 
                // attributes associated with the token
                request.attribute("subject", token.username);
                token.attributes.forEach(request::attribute);
            }
        });
    }
}
