package com.Net.Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by john on 2016/10/19.
 */
public class client {
    private static BufferedReader in;
    private static Socket clientsocket;

    public static void connect(String host) {
        try {
            clientsocket = new Socket(host, 80);
            System.out.println("Successfully connected to server,\nPlease input your request like " +
                    "\"METHOD_NAME(GET) SRC_URI(/video/music.html) PROTOCOL(HTTP/1.1)\"");

//            send("GET /video/music.html HTTP/1.1\r\n"+"Host: www.bilibili.com\r\n"+"\r\n");
//            send("Host: www.bilibili.com\r\n");
//            send("\r\n");
            Scanner console = new Scanner(System.in);
            String form_get;
            form_get = console.nextLine();
            SendGet(form_get);
            echo2File();


        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot connect to Server.java ");
        }
    }

    public static void echo2File() throws IOException {
        in = new BufferedReader(new InputStreamReader(clientsocket.getInputStream(), "utf-8"));
        String buffer;
        while (!(buffer = in.readLine()).equals("")) {
            System.out.println(buffer);
        }
        FileOutputStream fo = new FileOutputStream("response.txt");
        while ((buffer = in.readLine()) != null) {
            fo.write((buffer).getBytes("utf-8"));
        }
        fo.close();
        in.close();
    }

    public static void SendGet(String out) {
        try {
            String req = out + "\r\nHost: " + clientsocket.getInetAddress().getHostName() + "\r\n\r\n";
            clientsocket.getOutputStream().write((req).getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        if (args.length < 1){
            System.out.println("Please input like \"Java client HOSTNAME\"");
            return;
        }
        else
            connect(args[0]);
    }
}
