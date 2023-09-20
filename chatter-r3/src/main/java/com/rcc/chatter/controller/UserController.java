package com.rcc.chatter.controller;

import com.lambdaworks.crypto.SCryptUtil;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.dalesbred.Database;
import org.json.JSONObject;

import spark.Filter;
import spark.Request;
import spark.Response;

import static spark.Spark.halt;

public class UserController {
  private static final String USERNAME_PATTERN = "[a-zA-Z][a-zA-Z0-9]{1,29}";

  private final Database database;

  public UserController(Database database) {
    this.database = database;
  }

  public JSONObject registerUser(Request request, Response response) throws Exception {
    var json = new JSONObject(request.body());
    var username = json.getString("username");
    var password = json.getString("password");

    // apply the sane username validation used in space controller
    if (!username.matches(USERNAME_PATTERN)) {
      throw new IllegalArgumentException("invalid username");
    }
    if (password.length() < 8) {
      throw new IllegalArgumentException("password must be at least 8 characters");
    }

    // Use the Scrypt library to hash the password. Use the recommended parameter for 2019
    var hash = SCryptUtil.scrypt(password, 32768, 8, 1);
    database.updateUnique("INSERT INTO users(user_id, pw_hash) VALUES(?, ?)",
        username, hash);

    response.status(201);
    response.header("Location", "/users/" + username);
    return new JSONObject().put("username", username);
  }

  public void authenticate(Request request, Response response) {
    // check to see if there is an HTTP Basic Authorization header
    var authHeader = request.headers("Authorization");
    if (authHeader == null || !authHeader.startsWith("Basic ")) {
      return;
    }

    var offset = "Basic ".length();
    // decode the credentials using Base64 and UTF-8
    var credentials =
        new String(Base64.getDecoder().decode(authHeader.substring(offset)),
            StandardCharsets.UTF_8);

    // split the credentials into username and password
    var components = credentials.split(":", 2);
    if (components.length != 2) {
      throw new IllegalArgumentException("invalid auth header");
    }

    var username = components[0];
    var password = components[1];

    if (!username.matches(USERNAME_PATTERN)) {
      throw new IllegalArgumentException("invalid username");
    }

    var hash = database.findOptional(String.class,
        "SELECT pw_hash FROM users WHERE user_id = ?", username);

    // if the user exists, then use the Scrypt library to check the password
    if (hash.isPresent() && SCryptUtil.check(password, hash.get())) {
      request.attribute("subject", username);
    }
  }

  // enforce authentication. This can be used as a Spark before filter.
  public void requireAuthentication(Request request, Response response) {
    if (request.attribute("subject") == null) {
      // send a WWW-Authenticate header to inform the client that the user
      // should authenticate with Basic authentication. A user-agent, e.g.,
      // the browser after receiving this header, would prompt the user for
      // their username and password.
      //
      // Disable this to use custom login page
      //response.header("WWW-Authenticate", "Basic realm=\"/\", charset=\"UTF-8\"");

      halt(401);
    }
  }

  public Filter requirePermission(String method, String permission) {
    return (request, response) -> {
      // ignore requests that don't match the request method
      if (!method.equalsIgnoreCase(request.requestMethod())) {
        return;
      }

      // first check if the user is authenticated
      requireAuthentication(request, response);

      var spaceId = Long.parseLong(request.params(":spaceId"));
      var username = (String) request.attribute("subject");

      // look up permissions for the current user in the given space
      // default to no permissions
      var perms = database.findOptional(String.class, 
          "SELECT perms FROM permissions WHERE space_id = ? AND user_id = ?", 
          spaceId, username).orElse("");
      
      // if the user does not have permission, then halt with a 403 
      // Forbidden status
      if (!perms.contains(permission)) {
        halt(403);
      }
    };
  }
}
