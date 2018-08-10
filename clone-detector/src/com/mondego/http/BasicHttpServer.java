package com.mondego.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class BasicHttpServer {
    HttpServer server;
    
    
    public BasicHttpServer(String address, int port) throws IOException{
        this.server = HttpServer.create(new InetSocketAddress(address,port), 0);
        this.server.createContext("/getcandidates", new getCandidatesHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class getCandidatesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    public void stop(){
        this.server.stop(0);
    }
    
    
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost",8001), 0);
        server.createContext("/getcandidates", new getCandidatesHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }
}