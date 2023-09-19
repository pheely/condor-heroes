package com.rcc.chatter.token;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import spark.Request;

public interface TokenStore {
    String create(Request request, Token token);
    Optional<Token> read(Request request, String tokenId);

    class Token {
        public final Instant expiry;
        public final String username;
        public final Map<String, String> attributes;

        public Token(Instant expiry, String username) {
            this.expiry = expiry;
            this.username = username;
            // use a concurrent map if the token will be accessed from multiple threads
            this.attributes = new ConcurrentHashMap<>();
        }
    }
}
