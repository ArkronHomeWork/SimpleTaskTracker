package com.github.arkronzxc.noteserver.repository;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NoteRepository {
    private final MongoClient mongoClient = new MongoClient();
    private final MongoDatabase database = mongoClient.getDatabase("notedb");
    private final MongoCollection<Document> collection = database.getCollection("notes");

    private final Gson gson = new Gson();

    public String insertNote(Request req, Response res) {
        if (getDocumentByName(req.params(":name")) != null) {
            updateByName(req, res);
        } else {
            Document document = new Document();
            document.put("name", req.params(":name"));
            document.put("text", req.body());
            collection.insertOne(document);
            res.status(201);
        }
        return "";
    }

    public String selectByName(Request req, Response res) {
        Document document = getDocumentByName(req.params(":name"));
        if (document == null) {
            res.status(404);
            return "Note " + req.params(":name") + " wasn't found";
        }
        document.remove("_id");
        return gson.toJson(document);
    }

    public String deleteByName(Request req, Response res) {
        collection.deleteOne(Filters.eq("name", req.params(":name")));
        res.status(201);
        return "";
    }

    private void updateByName(Request req, Response res) {
        Document setData = getDocumentByName(req.params(":name"));
        String currentText = setData.getString("text");
        currentText = currentText + "\n" + req.body();
        setData.put("name", req.params(":name"));
        setData.put("text",  currentText);
        Document update = new Document();
        update.append("$set", setData);
        collection.updateOne(Filters.eq("name", req.params(":name")), update);
        res.status(202);
    }

    @SuppressWarnings("DuplicatedCode")
    public String showAllNotes(Request req, Response res) {
        List<Document> documents = new ArrayList<>();
        Consumer<Document> foreach = document -> {
            document.remove("_id");
            documents.add(document);
        };
        collection.find().forEach(foreach);
        return gson.toJson(documents);
    }

    private Document getDocumentByName(String documentName) {
        return collection.find(Filters.eq("name", documentName)).first();
    }
}
