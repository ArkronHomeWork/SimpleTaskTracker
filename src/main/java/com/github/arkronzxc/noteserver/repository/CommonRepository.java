package com.github.arkronzxc.noteserver.repository;

import spark.Request;
import spark.Response;

public interface CommonRepository {
    String showAll(Request req, Response res);

    String insert(Request req, Response res);

    String get(Request req, Response res);

    String delete(Request req, Response res);
}
