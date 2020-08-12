package com.fy;

import com.fy.CustomServer.CustomServer;
import com.fy.echoserver.EchoServer;
import com.fy.fileserver.FileServer;
import com.fy.protobuf.UserInfo;
import com.fy.websocketserver.WebSocketServer;
import com.google.protobuf.ByteString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * <p>
 *
 * </p >
 *
 * @author fangyan
 * @since 2020/8/9 15:20
 */
public class Server {
    public static void main(String[] args) throws Exception {
//        new WebSocketServer().bind(8080);
//        new FileServer().bind(8080);
//        new CustomServer().bind(8080);
        new EchoServer().bind(8080);
    }
}
