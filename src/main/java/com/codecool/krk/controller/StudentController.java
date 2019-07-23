package com.codecool.krk.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.codecool.krk.model.Student;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentController implements HttpHandler {
    private List<Student> students;
    private int studentId;

    public StudentController(List<Student> students) {
        this.students = students;
        studentId = 3;
    }


    public void handle(HttpExchange httpExchange) throws IOException {


        String method = httpExchange.getRequestMethod();
        URI uri = httpExchange.getRequestURI();
        Map<String, String> parsedUri = parseURI(uri.getPath());
        String response = "";
        String action = "index";
        int data = 0;
        if(!parsedUri.isEmpty()) {
            action = parsedUri.keySet().iterator().next();
            if(parsedUri.size() > 0){
                data = Integer.parseInt(parsedUri.get(action));
            }
        }
        switch (action) {
            case "index":
                httpExchange.sendResponseHeaders(200, response.length());
                response = index();
                break;
            case "add":
                response = add(method, httpExchange);
                break;
            case "edit":
                response = edit(data, method, httpExchange);
                break;
            case "delete":
                delete(data, httpExchange);
                break;
            default:
                index();
                break;
        }
        sendResponse(httpExchange, response);
    }

    private String index() {

        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/index.twig");
        JtwigModel model = JtwigModel.newModel();
        model.with("students", students);
        return template.render(model);
    }

    private String add(String method, HttpExchange httpExchange) throws IOException {
        String response = "";
        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/form.twig");
        JtwigModel model = JtwigModel.newModel();
        if(method.equals("GET")) {
            httpExchange.sendResponseHeaders(200, response.length());
            response = template.render(model);
        }
        if(method.equals("POST")) {
            InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String formData = br.readLine();

            Map inputs = parseFormData(formData);
            String firstName = (String) inputs.get("firstName");
            String lastName = (String) inputs.get("lastName");
            int age = Integer.parseInt((String) inputs.get("age"));
            Student student = new Student(studentId++, firstName, lastName, age);
            students.add(student);

            String url = "/students";
            httpExchange.getResponseHeaders().set("Location", url);
            httpExchange.sendResponseHeaders(303, -1);

        }
        return response;
    }

    private String edit(int id, String method, HttpExchange httpExchange) throws IOException {
        String response = "";
        if(method.equals("GET")) {
            Student student = students.get(id - 1);
            String firstName = student.getFirstName();
            String lastName = student.getLastName();
            int age = student.getAge();

            JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/form.twig");
            JtwigModel model = JtwigModel.newModel();

            model.with("firstName", firstName);
            model.with("lastName", lastName);
            model.with("age", age);

            response = template.render(model);
            httpExchange.sendResponseHeaders(200, response.getBytes().length);
        }
        if(method.equals("POST")) {
            InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String formData = br.readLine();

            Map inputs = parseFormData(formData);
            String firstName = (String) inputs.get("firstName");
            String lastName = (String) inputs.get("lastName");
            int age = Integer.parseInt((String) inputs.get("age"));

            Student student = students.get(id - 1);
            student.setFirstName(firstName);
            student.setLastName(lastName);
            student.setAge(age);

            String url = "/students";
            httpExchange.getResponseHeaders().set("Location", url);
            httpExchange.sendResponseHeaders(303, -1);
        }
        return response;

    }

    private void delete(int id, HttpExchange httpExchange) throws IOException {
        students.remove(id -1);
        String url = "/students";
        httpExchange.getResponseHeaders().set("Location", url);
        httpExchange.sendResponseHeaders(303, -1);
    }

    private Map<String, String> parseURI (String uri) {
        Map<String, String> parsedURI = new HashMap<>();
        String[] uriParts = uri.split("/");
        String action;
        String data;
        if(uriParts.length > 3) {
            action = uriParts[2];
            data = uriParts[3];
            parsedURI.put(action, data);

        } else if(uriParts.length > 2) {
            action = uriParts[2];
            data = "0";
            parsedURI.put(action, data);
        }

        return parsedURI;
    }

    private static Map<String, String> parseFormData(String formData) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        String[] pairs = formData.split("&");
        for(String pair : pairs){
            String[] keyValue = pair.split("=");
            String value = new URLDecoder().decode(keyValue[1], "UTF-8");
            map.put(keyValue[0], value);
        }
        return map;
    }

    private void sendResponse(HttpExchange httpExchange, String response) throws IOException {
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
