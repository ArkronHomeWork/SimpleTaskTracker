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
    final MongoCollection<Document> collection;

    public CommonRepository(String collectionName) {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("notedb");
        collection = database.getCollection(collectionName);
    }

    public String showAll(Request req, Response res) {
        List<Document> documents = new ArrayList<>();
        Consumer<Document> foreach = document -> {
            document.remove("_id");
            documents.add(document);
        };
        collection.find(Filters.eq("user_id", req.headers("Authorization"))).forEach(foreach);
        return gson.toJson(documents);
    }

    public abstract String insert(Request req, Response res);

    public abstract String get(Request req, Response res);

    public String delete(Request req, Response res) {
        collection.deleteOne(Filters.and(Filters.eq("name", req.params(":name")),
                Filters.eq("user_id", req.headers("Authorization"))));
        res.status(202);
        return "";
    }

    Document getDocumentByName(String documentName, String userId) {
        return collection.find(Filters.and(Filters.eq("name", documentName),
                Filters.eq("user_id", userId))).first();
    }
}
