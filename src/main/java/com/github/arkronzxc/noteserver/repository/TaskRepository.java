package com.github.arkronzxc.noteserver.repository;

import com.github.arkronzxc.noteserver.repository.scheduler.Scheduler;
import org.bson.Document;
import spark.Request;
import spark.Response;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Timer;

public class TaskRepository extends CommonRepository {
    public TaskRepository() {
        super("tasks");
        Timer time = new Timer();
        Scheduler scheduler = new Scheduler(collection);
        time.schedule(scheduler, 0, 60000);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public String insert(Request req, Response res) {
        Document document = new Document();
        if (getDocumentByName(req.params(":name"), req.headers("Authorization")) != null) {
            res.status(400);
            return "You can't duplicate task's names. Such as: " + req.params(":name") + "!";
        }
        String time = req.queryParamOrDefault("time", "");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd-MM-yy");
        LocalDateTime dateTime;
        if (time.isEmpty()) {
            dateTime = LocalDateTime.now().plusMinutes(1L);
        } else {
            dateTime = LocalDateTime.parse(time, formatter);
        }

        document.put("user_id", req.headers("Authorization"));
        document.put("name", req.params(":name"));
        document.put("time", Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
        document.put("text", req.body());

        collection.insertOne(document);
        res.status(201);
        return "";
    }

    @Override
    public String get(Request req, Response res) {
        Document document = getDocumentByName(req.params(":name"), req.headers("Authorization"));
        if (document != null) {
            document.remove("_id");
            return document.toJson();
        } else {
            res.status(404);
            return "Task with name " + req.params(":name") + " not found!";
        }
    }
}
