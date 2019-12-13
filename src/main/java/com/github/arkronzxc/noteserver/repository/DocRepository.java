package com.github.arkronzxc.noteserver.repository;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import spark.Request;
import spark.Response;
import spark.utils.IOUtils;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DocRepository {

    private final MongoClient mongoClient = new MongoClient();
    private final MongoDatabase database = mongoClient.getDatabase("notedb");
    private final MongoCollection<Document> collection = database.getCollection("docs");

    private final Gson gson = new Gson();
    private final File folder = new File("C:/tmp");


    public DocRepository() {
        folder.mkdir();
    }

    public String saveDoc(Request request, Response res) throws IOException, ServletException {
        request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("C:/tmp"));
        Collection<Part> filePart = request.raw().getParts();

        //Если файл сохранился успешно, он преобразуется в null (54 строка)
        // , если файл сохранился с ошибкой преобразуем в Exception (49, 52 строки)
        // :: = ссылка на метод
        // 56 строка: фильтр отбрасывает все объекты равные null(фильтрует их по условию)
        List<Exception> exceptionList = filePart.parallelStream().map(part -> {
            OutputStream outputStream;
            if (collection.find(Filters.eq("name", part.getSubmittedFileName())).first() != null) {
                return new RuntimeException("File with the current name is already exists");
            }
            try (InputStream inputStream = part.getInputStream()) {
                outputStream = new FileOutputStream("C:/tmp/" + part.getSubmittedFileName());
                IOUtils.copy(inputStream, outputStream);
                Document document = new Document();
                document.put("name", part.getSubmittedFileName());
                collection.insertOne(document);
                outputStream.close();
            } catch (Exception e) {
                return e;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        if (exceptionList.isEmpty()) {
            res.status(201);
            return "";
        } else {
            res.status(500);
            return "Cannot upload " + exceptionList.size() + " file!";
        }
    }

    public String getDoc(Request req, Response res) {
        String filename = req.params(":name");

        try {
            FileInputStream inputStream = new FileInputStream("C:/tmp/" + filename);  //read the file
            HttpServletResponse response = res.raw();
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            try {
                int c;
                while ((c = inputStream.read()) != -1) {
                    response.getWriter().write(c);
                }
            } finally {
                inputStream.close();
                response.getWriter().close();
            }
        } catch (IOException e) {
            res.status(404);
            return "File with name " + filename + " wasn't found!";
        }
        res.status(200);
        return "";
    }

    public String deleteDoc(Request req, Response res) {
        String fileName = req.params(":name");
        collection.deleteOne(Filters.eq("name", fileName));
        File file = new File("C:/tmp/" + fileName);
        if (file.delete()) {
            res.status(202);
            return "";
        } else {
            res.status(404);
            return "File with name " + fileName + " wasn't deleted!";
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public String showAllDocs(Request req, Response res) {
        List<Document> documents = new ArrayList<>();
        Consumer<Document> foreach = document -> {
            document.remove("_id");
            documents.add(document);
        };
        collection.find().forEach(foreach);
        return gson.toJson(documents);
    }
}
