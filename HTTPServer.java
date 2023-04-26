import java.io.*;
import java.net.*;

public class HTTPServer {

    static String rootPath = System.getProperty("user.dir");
    
    public static void handleRequest(Socket socket) {

        try(BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String requestLine;
                String[] requestLineVals;
                String method;
                String path;

                if((requestLine = in.readLine()) != null) {
                    System.out.println("Request Line: " + requestLine); 
                    requestLineVals = requestLine.split(" ");
                    method = requestLineVals[0];
                    path = requestLineVals[1].substring(1);
    
                    System.out.println("Method: " + method); 
                    System.out.println("Path: " + path); 

                    if(method.equals("GET")) {
                        GET(out, path);
                    } else if(method.equals("POST")) {
                        POST(in, out, path);
                    } else if(method.equals("PUT")) {
                        PUT(in, out, path);
                    } else if(method.equals("DELETE")) {
                        DELETE(out, path);
                    } else if(method.equals("HEAD")) {
                        HEAD(out, path);
                    }
                }
        } catch(Exception err) {
            err.printStackTrace();
        }
    }

    public static void GET(PrintWriter out, String path) {
        try {
            File file = new File(rootPath + File.separator + path);
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/plain");
                out.println();
                while ((line = reader.readLine()) != null) {
                    out.println(line);
                }
                reader.close();
            } else {
                out.println("HTTP/1.1 404 Not Found");
                out.println("Content-Type: text/html");
                out.println();
                String cat = "<html><body><h1>404 - Not Found</h1><img src='https://http.cat/404'></body></html>";
                out.println(cat);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void POST(BufferedReader in, PrintWriter out, String path) {
       
        try {
            File file = new File(rootPath + File.separator + path);
            int contentLength = 0;
            String line;

            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.substring("Content-Length:".length()).trim());
                }
            }

            StringBuilder requestBody = new StringBuilder();
            for (int i = 0; i < contentLength; i++) {
                requestBody.append((char)in.read());
            }
            
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write(requestBody.toString());
            fileWriter.close();

            out.println("HTTP/1.1 200 OK");
            out.println();
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void PUT(BufferedReader in, PrintWriter out, String path) {
        try {
            File file = new File(rootPath + File.separator + path);
            int contentLength = 0;
            String line;

            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.substring("Content-Length:".length()).trim());
                }
            }
            
            StringBuilder requestBody = new StringBuilder();
            for (int i = 0; i < contentLength; i++) {
                requestBody.append((char)in.read());
            }
            
            PrintWriter fileWriter = new PrintWriter(file);
            fileWriter.write(requestBody.toString());
            fileWriter.close();

            out.println("HTTP/1.1 200 OK");
            out.println();
        } catch (Exception e) {
            e.printStackTrace();
            out.println("HTTP/1.1 500 Internal Server Error");
            out.println("Content-Type: text/html");
            out.println();
            String cat = "<html><body><h1>500 - Internal Server Error</h1><img src='https://http.cat/500'></body></html>";
            out.println(cat);
        }
    }
    
    public static void DELETE(PrintWriter out, String path) {
        try {
            File file = new File(rootPath + File.separator + path);
            if (file.exists()) {
                if (file.delete()) {
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: text/plain");
                    out.println();
                    out.println("File deleted successfully");
                } else {
                    out.println("HTTP/1.1 500 Internal Server Error");
                    out.println("Content-Type: text/html");
                    out.println();
                    String cat = "<html><body><h1>500 - Internal Server Error</h1><img src='https://http.cat/500'></body></html>";
                    out.println(cat);
                }
            } else {
                out.println("HTTP/1.1 404 Not Found");
                out.println("Content-Type: text/html");
                out.println();
                String cat = "<html><body><h1>404 - Not Found</h1><img src='https://http.cat/404'></body></html>";
                out.println(cat);
            }
        } catch (Exception e) {
            out.println("HTTP/1.1 500 Internal Server Error");
            out.println("Content-Type: text/html");
            out.println();
            String cat = "<html><body><h1>500 - Internal Server Error</h1><img src='https://http.cat/500'></body></html>";
            out.println(cat);
        }
    }    

    public static void HEAD(PrintWriter out, String path) {
        try {
            File file = new File(rootPath + File.separator + path);
            if (file.exists()) {
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/plain");
                out.println("Content-Length: " + file.length());
                out.println();
            } else {
                out.println("HTTP/1.1 404 Not Found");
                out.println("Content-Type: text/html");
                out.println();
                String cat = "<html><body><h1>404 - Not Found</h1><img src='https://http.cat/404'></body></html>";
                out.println(cat);
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("HTTP/1.1 500 Internal Server Error");
            out.println("Content-Type: text/html");
            out.println();
            String cat = "<html><body><h1>500 - Internal Server Error</h1><img src='https://http.cat/500'></body></html>";
            out.println(cat);
        }
    }    
    public static void main(String... args) {
        try(ServerSocket server = new ServerSocket(8080)) {
            Socket socket = null;
            while ((socket = server.accept()) != null) {
                System.out.println("Accepted request");
                final Socket threadSocket = socket;
                new Thread( () -> handleRequest(threadSocket) ).start();
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}
