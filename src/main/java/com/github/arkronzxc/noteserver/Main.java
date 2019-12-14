package com.github.arkronzxc.noteserver;

import com.github.arkronzxc.noteserver.repository.DocRepository;
import com.github.arkronzxc.noteserver.repository.NoteRepository;
import com.github.arkronzxc.noteserver.repository.TaskRepository;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        port(4567);

        TaskRepository taskRepository = new TaskRepository();
        DocRepository docRepository = new DocRepository();
        NoteRepository noteRepository = new NoteRepository();

        path("/note", () -> {
            get("/", noteRepository::showAll);
            get("/:name", noteRepository::get);
            put("/:name", noteRepository::insert);
            delete("/:name", noteRepository::delete);
        });

        path("/doc", () -> {
            get("/", docRepository::showAll);
            post("/", docRepository::insert);
            get("/:name", docRepository::get);
            delete("/:name", docRepository::delete);
        });

        path("/task", () -> {
            get("/", taskRepository::showAll);
            get("/:name", taskRepository::get);
            put("/:name", taskRepository::insert);
            delete("/:name", taskRepository::delete);
        });
    }
}
