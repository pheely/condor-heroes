package com.rcc.chatter.token;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import spark.Request;

public class CookieTokenStore implements TokenStore {
    @Override
    public String create(Request request, Token token) {
        // check if there is an existing session and invalidate it
        // to prevent the session fixation attacks
        var session = request.session(false);
        if (session != null) {
            session.invalidate();
        }

        // pass true to create a new session cookie
        session = request.session(true);

        session.attribute("username", token.username);
        session.attribute("expiry", token.expiry);
        session.attribute("attrs", token.attributes);

        // return the SHA256 hash of the session cookie, Base64url encoded
        return Base64url.encode(sha256(session.id()));
    }

    @Override
    public Optional<Token> read(Request request, String tokenId) {
        // pass false to check if a valid session is present
        var session = request.session(false);
        if (session == null) {
            return Optional.empty();
        }

        // tokenId is the value of the X-CSRF-Token
        var provided = Base64url.decode(tokenId);
        var computed = sha256(session.id());

        if (!MessageDigest.isEqual(computed, provided)) {
            return Optional.empty();
        }

        var token = new Token(session.attribute("expiry"), 
            session.attribute("username"));
        token.attributes.putAll(session.attribute("attrs"));

        return Optional.of(token);
    }

    @Override
    public void revoke(Request request, String tokenId) {
        var session = request.session(false);
        if (session == null) return;
        var provided = Base64url.decode(tokenId);
        var computed = sha256(session.id());

        if (!MessageDigest.isEqual(computed, provided)) return;
        
        session.invalidate();
    }

    static byte[] sha256(String tokenId) {
        try {
            var sha256 = MessageDigest.getInstance("SHA-256");
            return sha256.digest(tokenId.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
