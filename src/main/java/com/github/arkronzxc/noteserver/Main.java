package com.github.arkronzxc.noteserver;

import com.github.arkronzxc.noteserver.repository.DocRepository;
import com.github.arkronzxc.noteserver.repository.NoteRepository;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        port(4567);

        DocRepository docRepository = new DocRepository();
        NoteRepository noteRepository = new NoteRepository();

        path("/note", () -> {

            get("/", noteRepository::showAllNotes);
            get("/:name", noteRepository::selectByName);
            put("/:name", noteRepository::insertNote);
            delete("/:name", noteRepository::deleteByName);

        });

        path("/doc", () -> {
            get("/", docRepository::showAllDocs);

            post("/", docRepository::saveDoc);

            get("/:name", docRepository::getDoc);

            delete("/:name", docRepository::deleteDoc);
        });
    }
}
