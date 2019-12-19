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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.logging.Filter;

public class Scheduler extends TimerTask {
    private final ZoneId zid = ZoneId.of("Europe/Moscow");
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

        collection.find(Filters.and(
                Filters.gte("time",
                        Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())),
                Filters.lt("time",
                        Date.from(LocalDateTime.now().plusMinutes(1L).atZone(ZoneId.systemDefault()).toInstant()))))
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
