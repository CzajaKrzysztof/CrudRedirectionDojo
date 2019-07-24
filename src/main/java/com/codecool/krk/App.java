package com.codecool.krk;

import com.sun.net.httpserver.HttpServer;
import com.codecool.krk.controller.StudentController;
import com.codecool.krk.model.Student;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class App 
{
    public static void main( String[] args ) throws IOException {
        List<Student> students = new ArrayList<>();
        students.add(new Student(1, "Jan", "Kowalski", 35));
        students.add(new Student(2, "Adam", "Smith", 19));

        // create a server on port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // set routes
        server.createContext("/static", new Static());
        server.createContext("/students", new StudentController(students));
        server.createContext("/students/add", new StudentController(students));
        server.createContext("/students/edit/5", new StudentController(students));
        server.createContext("/students/delete/3", new StudentController(students));
        server.setExecutor(null); // creates a default executor

        // start listening
        server.start();
    }
}
