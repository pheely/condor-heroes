package com.rcc.chatter;

import com.google.common.util.concurrent.RateLimiter;
import com.rcc.chatter.controller.AuditController;
import com.rcc.chatter.controller.ModeratorController;
import com.rcc.chatter.controller.SpaceController;
import com.rcc.chatter.controller.TokenController;
import com.rcc.chatter.controller.UserController;
import com.rcc.chatter.token.CookieTokenStore;
import com.rcc.chatter.token.TokenStore;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.dalesbred.Database;
import org.dalesbred.result.EmptyResultException;
import org.h2.jdbcx.JdbcConnectionPool;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Spark;

import static spark.Spark.after;
import static spark.Spark.afterAfter;
import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.internalServerError;
import static spark.Spark.notFound;
import static spark.Spark.post;
import static spark.Spark.secure;

public class Main {

    public static void main(String[] args) throws Exception{
        Spark.staticFiles.location("/public");
	    // Enable HTTPS
	    secure("localhost.p12", "changeit", null, null);

        // natter is a privileged user
        var datasource = JdbcConnectionPool.create(
            "jdbc:h2:mem:natter", "natter", "password");
        var database = Database.forDataSource(datasource);
        createTables(database);

        // switch to less privileged natter_api_user
        datasource = JdbcConnectionPool.create(
            "jdbc:h2:mem:natter", "natter_api_user", "password");
        database = Database.forDataSource(datasource);

        var spaceController = new SpaceController(database);
        var userController = new UserController(database);
        var auditController = new AuditController(database);
        // rate limiter should be at the first gate
        // create a shared rate limiter object and allow just 2 API request/second
        var rateLimiter = RateLimiter.create(2.0d);
        before((request, response) -> {
            // check if the rate has been exceeded
            if (!rateLimiter.tryAcquire()) {
                // if so, add a Retry-After header indicating when the client
                // should retry
                response.header("Retry-After", "2");
                // return a 429 - Too Many Requests - status
                halt(429);
            }
        });

        before((request, response) -> {
            if (request.requestMethod().equals("POST") &&
                    !"application/json".equals(request.contentType())) {
                halt(415, new JSONObject().put(
                    "error", "Only application/json supported").toString());
            }
        });

        afterAfter((request, response) -> {
            response.type("application/json;charset=utf-8");
            response.header("X-Content-Type-Options", "nosniff");
            response.header("X-Frame-Options", "DENY");
            response.header("X-XSS-Protection", "0");
            response.header("Cache-Control", "no-store");
            response.header("Content-Security-Policy",
                "default-src 'none'; frame-ancestors 'none'; sandbox");
            response.header("Server", "");
        });

        TokenStore tokenStore = new CookieTokenStore();
        var tokenController = new TokenController(tokenStore);

        // first, try to authenticate the user
        // the first time login requires username and password 
        // subsequently with a valid token
        before(userController::authenticate);
        before(tokenController::validateToken);

        // then perform audit logging
        before(auditController::auditRequestStart);
        afterAfter(auditController::auditRequestEnd);

        // finally add the check if authentication was successful
        before("/sessions", userController::requireAuthentication);
        post("/sessions", tokenController::login);

        before("/spaces", userController::requireAuthentication);
        post("/spaces", spaceController::createSpace);

        // for each operation, you add a before() filter that ensures the user
        // has correct permissions
        before("/spaces/:spaceId/messages", 
            userController.requirePermission("POST", "w"));
        post("/spaces/:spaceId/messages", spaceController::postMessage);

        before("/spaces/:spaceId/messages/*", 
            userController.requirePermission("GET", "r"));
        get("/spaces/:spaceId/messages/:msgId", spaceController::readMessage);

        before("/spaces/:spaceId/messages", 
            userController.requirePermission("GET", "r"));
        post("/spaces/:spaceId/messages", spaceController::findMessages);

        var moderatorController = new ModeratorController(database);

        before("/spaces/:spaceId/messages/*", 
            userController.requirePermission("DELETE", "d"));
        post("/spaces/:spaceId/messages/:messageId", moderatorController::deletePost);

        before("/spaces/:spaceId/members",
            userController.requirePermission("POST", "rwd"));
        post("/spaces/:spaceId/members", spaceController::addMember);

        // register endpoint for reading logs
        get("/logs", auditController::readAuditLog);

        post("/users", userController::registerUser);

        after((request, response) -> {
            response.type("application/json");
        });

        internalServerError(new JSONObject()
            .put("error", "internal server error").toString());
        notFound(new JSONObject()
            .put("error", "not found").toString());

        exception(IllegalArgumentException.class, Main::badRequest);
        exception(JSONException.class, Main::badRequest);
        exception(EmptyResultException.class,
            (e, request, response) -> response.status(404));
    }

    private static void createTables(Database database) throws Exception {
        var path = Paths.get(Main.class.getResource("/schema.sql").toURI());
        database.update(Files.readString(path));
    }

    private static void badRequest(Exception ex, Request request, Response response) {
        response.status(400);
        response.body(new JSONObject()
            .put("error", ex.getMessage()).toString());
    }
}
