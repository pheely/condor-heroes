package com.rcc.chatter.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.dalesbred.Database;
import org.json.JSONArray;
import org.json.JSONObject;

import spark.Request;
import spark.Response;

public class AuditController {
    private final Database database;

    public AuditController(Database database) {
        this.database = database;
    }
    
    public void auditRequestStart(Request request, Response response) {
        database.withVoidTransaction(tx -> {
            // generate a new audit id before the request is processed
            // and save it as an attribute on the request
            var auditId = database.findUniqueLong("SELECT NEXT VALUE FOR audit_id_seq");
            request.attribute("audit_id", auditId);
            database.updateUnique(
                "INSERT INTO audit_log(audit_id, method, path, user_id, audit_time) " +
                "VALUES (?, ?, ?, ?, current_timestamp)",
                auditId,
                request.requestMethod(),
                request.pathInfo(),
                request.attribute("subject"));
        });
    }

    public void auditRequestEnd(Request request, Response response) {
        database.updateUnique(
            "INSERT INTO audit_log(audit_id, method, path, status, user_id, audit_time) " +
            "VALUES (?, ?, ?, ?, ?, current_timestamp)", 
            // look up the audit id from the request attributes
            request.attribute("audit_id"),
            request.requestMethod(),
            request.pathInfo(),
            response.status(),
            request.attribute("subject"));
    }

    public JSONArray readAuditLog(Request request, Response response) {
        // read log entries for the last hour
        var since = Instant.now().minus(1, ChronoUnit.HOURS);
        var logs = database.findAll(AuditController::recordToJson, 
            "SELECT * FROM audit_log WHERE audit_time > ? LIMIT 20", since);
        // convert each entry into a JSON object and collect as a JSON array
        return new JSONArray(logs);
    }

    private static JSONObject recordToJson(ResultSet row) throws SQLException{
        // helper method to convert the record into a JSON object
        return new JSONObject()
            .put("id", row.getLong("audit_id"))
            .put("method", row.getString("method"))
            .put("path", row.getString("path"))
            .put("status", row.getInt("status"))
            .put("user", row.getString("user_id"))
            .put("time", row.getTimestamp("audit_time").toInstant());
    }
}
