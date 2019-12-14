package com.github.arkronzxc.noteserver.repository;

import com.mongodb.client.model.Filters;
import org.bson.Document;
import spark.Request;
import spark.Response;

public class TaskRepository extends CommonRepository {
    public TaskRepository() {
        super("tasks");
    }

    @Override
    public String insert(Request req, Response res) {
        Document document = new Document();
        if(getTaskByName(req.params(":name")) != null){
            res.status(400);
            return "You can't duplicate task's names. Such as: " + req.params(":name") + "!";
        }
        document.put("name", req.params(":name"));
        document.put("time", req.queryParamOrDefault("time", ""));
        document.put("text", req.body());
        collection.insertOne(document);
        res.status(201);
        return "";
    }

    @Override
    public String get(Request req, Response res) {
        Document document = collection.find(Filters.eq("name", req.params(":name"))).first();
        if (document != null) {
            document.remove("_id");
            return document.toJson();
        } else {
            res.status(404);
            return "Task with name " + req.params(":name") + " not found!";
        }
    }

    @Override
    public String delete(Request req, Response res) {
        collection.deleteOne(Filters.eq("name", req.params(":name")));
        res.status(202);
        return "";
    }

    private Document getTaskByName(String documentName) {
        return collection.find(Filters.eq("name", documentName)).first();
    }
}
