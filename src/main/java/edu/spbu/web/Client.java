package edu.spbu.web;

import java.net.*;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws Exception {
         //String adress = "example.com";
         String adress = "localhost";
         int port = 80;
         Socket socket = new Socket(InetAddress.getByName(adress),port);

        String request = "GET /example.html \r\n";
        socket.getOutputStream().write(request.getBytes());
        socket.getOutputStream().flush();


        Scanner scanner = new Scanner(socket.getInputStream());
        while (scanner.hasNextLine()){
            System.out.println(scanner.nextLine());
        }

    }



}