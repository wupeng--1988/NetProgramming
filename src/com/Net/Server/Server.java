package com.Net.Server;
import java.io.*;
import java.net.*;

/**
 * Created by john on 2016/10/20.
 */

public class Server {
    public static String ROOT ;
    private int port;
    static int contentLength;
    String path = null;

    public Server(String root, int port) {
        ROOT = root;
        this.port = port;
    }

    public void doGet(BufferedReader reader, OutputStream out) {
        String URL = ROOT + path;
        try {
            if (new File(URL).exists()) {
                FileInputStream fi = new FileInputStream(URL);
                byte[] buf = new byte[fi.available()];
                fi.read(buf);
                out.write(("HTTP/1.1 200 OK\n\r\n").getBytes("utf-8"));
                out.write(buf);
                out.close();
                fi.close();
                reader.close();
            }else
                out.write(("file does not exist").getBytes("UTF-8"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            // TODO: handle exception
        }
    }

    public void doPut(final BufferedReader reader, OutputStream out)
            throws IOException {
        String line;
        while ((line=reader.readLine()) != null) {
            System.out.println(line);

            if ("".equals(line)) {
                break;
            } else if (line.indexOf("Content-Length") != -1) {
                this.contentLength = Integer.parseInt(line.substring(line.indexOf("Content-Length") + 16));
            }

        }
        String data = null;
        byte[] buf = {};
        int size = 0;
        if (this.contentLength != 0) {
            buf = new byte[this.contentLength];
            while (size < this.contentLength) {

                int c = reader.read();
                buf[size++] = (byte) c;

            }
            data = new String(buf);
            System.out.println("The data user put: "
                    + data);
        }
        String response = "";
        response += "HTTP/1.1 200 OK\n";
        response += "Server: CentOs 6.5\n";
        response += "Content-Type: text/html\n";
        response += "Last-Modified: Mon, 09 May 2016 18:23:33 GMT\n";
        response += "Accept-ranges: bytes";
        response += "\r\n";
        String body = "<html><head><title>test server</title></head><body><p>post ok:</p>" + data + "</body></html>";
        System.out.println(body);
        out.write(response.getBytes());
        out.write(body.getBytes());
        out.flush();
        reader.close();
        out.close();
        System.out.println("request complete.");
    }
    public void parse() throws IOException {
        ServerSocket sskt = new ServerSocket(this.port);
        while (true) {
            Socket httpskt = sskt.accept();
            System.out.println("receive connection");
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    httpskt.getInputStream()));

            String RequestLine[] = bf.readLine().split(" ");
//            for(int i =0 ; i<RequestLine.length;i++)
//                System.out.print(RequestLine[i]+" ");
            String method = RequestLine[0];
            path = RequestLine[1];
            OutputStream out = httpskt.getOutputStream();
            if (method.equalsIgnoreCase("GET")) {
                doGet(bf, out);
            } else if (method.equalsIgnoreCase("PUT")) {
                doPut(bf, out);
            }
            httpskt.close();
        }
    }

    public static void main(String[] args) {
        try {
            if(args.length<1)
                System.out.println("Please input your server_root_directory");
            else {
                Server httpServer = new Server(args[0], 8000);
                httpServer.parse();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
