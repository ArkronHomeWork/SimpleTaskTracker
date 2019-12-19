package com.github.arkronzxc.noteserver.repository;

import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.utils.IOUtils;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class DocRepository extends CommonRepository {
    private final Logger log = LoggerFactory.getLogger(DocRepository.class);

    public DocRepository() {
        super("docs");
        File folder = new File("C:/tmp");
        if (!folder.mkdir()) {
            log.info("Directory wasn't created");
        }
    }

    @Override
    public String insert(Request req, Response res) {
        req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("C:/tmp"));
        Collection<Part> filePart;
        try {
            filePart = req.raw().getParts();
        } catch (IOException | ServletException e) {
            res.status(400);
            log.info("Couldn't get file from request: {}", req);
            return "Uploading file is incorrect ";
        }

        //Если файл сохранился успешно, он преобразуется в null (54 строка)
        // , если файл сохранился с ошибкой преобразуем в Exception (49, 52 строки)
        // 56 строка: фильтр отбрасывает все объекты равные null(фильтрует их по условию)
        List<Exception> exceptionList = filePart.parallelStream().map(part -> {
            OutputStream outputStream;
            if (collection.find(Filters.eq("name", part.getSubmittedFileName())).first() != null) {
                return new RuntimeException("File with the current name is already exists");
            }
            try (InputStream inputStream = part.getInputStream()) {
                final String uid = UUID.randomUUID().toString();
                outputStream = new FileOutputStream("C:/tmp/" + uid);
                IOUtils.copy(inputStream, outputStream);
                Document document = new Document();
                document.put("user_id", req.headers("Authorization"));
                document.put("name", part.getSubmittedFileName());
                document.put("uid", uid);
                collection.insertOne(document);
                outputStream.close();
            } catch (Exception e) {
                log.warn("Can't save file {}", part);
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

    @Override
    public String get(Request req, Response res) {
        String fileName = req.params(":name");

        try {
            Document fileDoc = getDocumentByName(fileName, req.headers("Authorization"));
            FileInputStream inputStream = new FileInputStream("C:/tmp/" +
                    fileDoc.getString("uid"));  //read the file
            HttpServletResponse response = res.raw();
            response.setHeader("Content-Disposition", "attachment; filename=" +
                    fileDoc.getString("name"));
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
            log.info("IOException while getting file: ", e);
            res.status(404);
            return "File with name " + fileName + " wasn't found!";
        }
        res.status(200);
        return "";
    }

    @Override
    public String delete(Request req, Response res) {
        String fileName = req.params(":name");
        Document fileDoc = getDocumentByName(fileName, req.headers("Authorization"));
        collection.deleteOne(fileDoc);
        File file = new File("C:/tmp/" + fileDoc.getString("uid"));
        if (file.delete()) {
            res.status(202);
            return "";
        } else {
            log.info("File with name: {} wasn't deleted", fileName);
            res.status(404);
            return "File with name " + fileName + " wasn't deleted!";
        }
    }
}
