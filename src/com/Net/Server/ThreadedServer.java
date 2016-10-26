package com.Net.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Created by john on 2016/10/24.
 */
class ThreadedServer implements Runnable {
    private final ServerSocket serverSocket;
    private final ExecutorService pool;
    private final String ROOT;
    public ThreadedServer(String root,int port,int poolsize)
            throws IOException {
        ROOT = root;
        serverSocket = new ServerSocket(port);
        pool = Executors.newFixedThreadPool(poolsize);
    }

    public void run() { // run the service
        try {
            for (;;) {
                pool.execute(new HandlerX(serverSocket.accept(),ROOT));
            }
        } catch (IOException ex) {
            pool.shutdown();
        }
    }
    public static void main(String[] args){
        try {
            if(args.length<1)
                System.out.println("Please input your server_root_directory");
            else {
            new Thread(new ThreadedServer(args[0],8000,1000)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class HandlerX implements Runnable {
    private final Socket socket;
    private StringBuilder path = new StringBuilder();
    private int contentLength;
    HandlerX(Socket socket,String root) {
        this.socket = socket;
        path.append(root);
    }
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String headLine[] = reader.readLine().split(" ");
//            for(int i =0 ; i<headLine.length;i++)
//                System.out.print(headLine[i]+" ");
            String method = headLine[0];
            path.append(headLine[1]);
            if(method.equalsIgnoreCase("get")){
                doget(reader,writer);
            }else if(method.equalsIgnoreCase("put"))
                doput(reader,writer);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doget(BufferedReader reader, BufferedWriter out) {
        String URL = path.toString();
        try {
            if (new File(URL).exists()) {
                FileInputStream fi = new FileInputStream(URL);
                byte[] buf = new byte[fi.available()];
                fi.read(buf);
                out.write("HTTP/1.1 200 OK\n\r\n");
                out.write(new String(buf,"utf-8"));
                out.close();
                fi.close();
                reader.close();
            }else
                out.write("file does not exist");
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            // TODO: handle exception
        }
    }
    private void doput(BufferedReader reader,BufferedWriter out)throws IOException
    {
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
        String body = "<html><head><title>test server</title></head><body><p>put ok:</p>" + data + "</body></html>";
        System.out.println(body);
        out.write(response);
        out.write(body);
        out.flush();
        reader.close();
        out.close();
        System.out.println("request complete.");
    }
}
