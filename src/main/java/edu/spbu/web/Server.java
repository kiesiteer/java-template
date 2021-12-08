
package edu.spbu.web;

        import java.net.*;
        import java.io.*;
        import java.nio.file.Files;
        import java.nio.file.Paths;
        import java.util.Scanner;

public class Server {
    public static void main(String[] args) {
        try {
            int port = 80;
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Waiting for connection...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connection accepted.");

                String fileName = recieveRequest(socket);
                sendResponse(fileName,socket);


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String recieveRequest(Socket socket){

        String fileName = null;
        try {
            InputStream inputStream = socket.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            fileName = scanner.nextLine().split(" ")[1].split("/")[1]; // "GET /index.html" -> "index.html"

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(fileName == null){fileName="noSuchFile";}
            return fileName;
        }
    }

    private static void sendResponse(String fileName , Socket socket){
        try {
            File file = new File(fileName);
            if (file.isFile()) {
                String content = new String(Files.readAllBytes(Paths.get(fileName)));
                String response = "HTTP/1.1 200 OK\n";
                socket.getOutputStream().write((response + content).getBytes());

            } else {
                socket.getOutputStream().write("HTTP/1.1 404\n".getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
