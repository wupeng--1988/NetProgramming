package com.Net.Server;

import java.nio.*;
import java.io.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.net.*;

import org.apache.log4j.Logger;

/**
 * Created by john on 2016/10/20.
 */

public class ConcurrentServer {
    private int port = 8000;
    private ServerSocketChannel ssktc = null;
    private Selector selector = null;
    private ExecutorService executorService;// 线程池
    private final int POOL_SIZE = 2;// 单个CPU线程池大小
    private final int BSIZE = 1024;//ByteBuffer大小

    public ConcurrentServer() throws IOException {
        selector = selector.open();
        ssktc = ServerSocketChannel.open();
        ssktc.socket().bind(new InetSocketAddress(port));
        ssktc.socket().setReuseAddress(true);
        ssktc.configureBlocking(false);
        int cpuNums = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(cpuNums * POOL_SIZE);
        System.out.println("Server Started");
    }

    public void service() throws IOException {
        ssktc.register(selector, SelectionKey.OP_ACCEPT);
        while (selector.select() > 0) {//select()阻塞?
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = readyKeys.iterator();
            while (it.hasNext()) {
                SelectionKey key = null;
                try {
                    key = it.next();
                    it.remove();
                    if (key.isAcceptable()) {
                        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                        SocketChannel sc = ssc.accept();
                        System.out.println("Got Connection from: "
                                + sc.socket().getInetAddress() + ":"
                                + sc.socket().getPort());
                        sc.configureBlocking(false);
                        ByteBuffer buf = ByteBuffer.allocate(BSIZE);
                        sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, buf);
                    }
                    if (key.isReadable()) {
                        receive(key);
                    }
                    if (key.isWritable()) {
                        send(key);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        if (key != null) {
                            key.cancel();
                            key.channel().close();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public void send(SelectionKey key) throws IOException {
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        SocketChannel sc = (SocketChannel) key.channel();
        buffer.flip();
        String data = decode(buffer);
        if (data.indexOf(";") == -1)
            return;
        String output = "ok";
        ByteBuffer outBuf = encode("echo:" + output);
        while (outBuf.hasRemaining())
            sc.write(outBuf);
        ByteBuffer tmp = encode(output);
        buffer.position(tmp.limit());
        buffer.compact();
        key.cancel();
        sc.close();
        System.out.println("Connection shutdown");
    }

    public void receive(SelectionKey key) throws IOException {
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer readBuff = ByteBuffer.allocate(BSIZE);
        sc.read(readBuff);
        String rev = new String(readBuff.array());
        if (rev.length() > 0) {
            executorService.execute(new Handler(readBuff.array()));
        }
        readBuff.flip();
        buffer.limit(buffer.capacity());
        buffer.put(readBuff);
    }

    public String decode(ByteBuffer buffer) {
        CharBuffer charBuffer = Charset.defaultCharset().decode(buffer);
        return charBuffer.toString();
    }

    public ByteBuffer encode(String str) {
        return Charset.defaultCharset().encode(str);
    }

    public static void main(String[] args) throws IOException {
        ConcurrentServer server = new ConcurrentServer();
        server.service();
    }


}

class Handler implements Runnable {
    private byte[] revbytes;
    public static Logger log = Logger.getLogger(HandlerX.class);

    public Handler(byte[] revbytes) {
        this.revbytes = revbytes;
    }

    public void run() {
// 解析发送过来的数据
        try {

            System.out.println(System.currentTimeMillis());
        } catch (Exception e) {
            log.error(e);
        } finally {

        }
    }
}
