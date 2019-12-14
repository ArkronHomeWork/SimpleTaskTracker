package com.github.arkronzxc.noteserver.repository.scheduler;

import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class Scheduler extends TimerTask {
    private final Gson gson = new Gson();
    private final MongoCollection<Document> collection;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    Logger log = LoggerFactory.getLogger(Scheduler.class);

    public Scheduler(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    @Override
    public void run() {
        List<Document> list = new ArrayList<>();
        Consumer<Document> consumer = document -> {
            document.remove("_id");
            list.add(document);
        };

        collection.find(Filters.in("time", LocalDateTime.now(), LocalDateTime.now().plusMinutes(1L)))
                .forEach(consumer);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4568/hello"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(list)))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    log.info("Response status code: " + response.statusCode());
                    log.info("Response headers: " + response.headers());
                    log.info("Response body: " + response.body());
                });
    }
}
