package com.rcc.chatter;

import java.util.Set;

import spark.Filter;
import spark.Request;
import spark.Response;

import static spark.Spark.halt;

public class CorsFilter implements Filter {
    private final Set<String> allowedOrigins;

    CorsFilter(Set<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void handle(Request request, Response response) {
        var origin = request.headers("Origin");
        if (origin != null && allowedOrigins.contains(origin)) {
            // if the origin is allowed, then add the 
            // basic CORS headers to the response
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Allow-Credentials", "true");
            response.header("Vary", "Origin");
        }

        if (isPreflightRequest(request)) {
            // if the origin is not allowed, then reject the
            // preflight request
            if (origin == null || !allowedOrigins.contains(origin)) {
                halt(403);
            }
            response.header("Access-Control-Allow-Headers", 
                            "Content-Type, Authorization, X-CSRF-Token");
            response.header("Access-Control-Allow-Methods", 
                            "GET, POST, DELETE");
            // for permitted preflight request, return a 204 No Content
            halt(204);
        }
    }

    private boolean isPreflightRequest(Request request) {
        // preflight requests use the HTTP OPTIONS method 
        // and include the Access-Control-Request-Method header
        return "OPTIONS".equals(request.requestMethod()) &&
               request.headers().contains("Access-Control-Request-Method");
    }
}
