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

public abstract class CommonRepository {
    final Gson gson = new Gson();
    final MongoClient mongoClient = new MongoClient();
    final MongoDatabase database = mongoClient.getDatabase("notedb");
    final MongoCollection<Document> collection;

    public CommonRepository(String collectionName) {
        collection = database.getCollection(collectionName);
    }

    public String showAll(Request req, Response res) {
        List<Document> documents = new ArrayList<>();
        Consumer<Document> foreach = document -> {
            document.remove("_id");
            documents.add(document);
        };
        collection.find().forEach(foreach);
        return gson.toJson(documents);
    }

    public abstract String insert(Request req, Response res);

    public abstract String get(Request req, Response res);

    public String delete(Request req, Response res) {
        collection.deleteOne(Filters.eq("name", req.params(":name")));
        res.status(202);
        return "";
    }

    Document getDocumentByName(String documentName) {
        return collection.find(Filters.eq("name", documentName)).first();
    }
}
