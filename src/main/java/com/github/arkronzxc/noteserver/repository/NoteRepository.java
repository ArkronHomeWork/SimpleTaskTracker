package com.github.arkronzxc.noteserver.repository;

import com.mongodb.client.model.Filters;
import org.bson.Document;
import spark.Request;
import spark.Response;

public class NoteRepository extends CommonRepository {

    public NoteRepository() {
        super("notes");
    }

    @Override
    public String insert(Request req, Response res) {
        if (getDocumentByName(req.params(":name"), req.headers("Authorization")) != null) {
            updateByName(req, res);
        } else {
            Document document = new Document();
            document.put("user_id", req.headers("Authorization"));
            document.put("name", req.params(":name"));
            document.put("text", req.body());
            collection.insertOne(document);
            res.status(201);
        }
        return "";
    }

    @Override
    public String get(Request req, Response res) {
        Document document = getDocumentByName(req.params(":name"), req.headers("Authorization"));
        if (document == null) {
            res.status(404);
            return "Note " + req.params(":name") + " wasn't found";
        }
        document.remove("_id");
        return gson.toJson(document);
    }

    private void updateByName(Request req, Response res) {
        Document setData = getDocumentByName(req.params(":name"), req.headers("Authorization"));
        String currentText = setData.getString("text");
        currentText = currentText + "\n" + req.body();
        setData.put("name", req.params(":name"));
        setData.put("text", currentText);
        Document update = new Document();
        update.append("$set", setData);
        collection.updateOne(Filters.eq("name", req.params(":name")), update);
        res.status(202);
    }
}
