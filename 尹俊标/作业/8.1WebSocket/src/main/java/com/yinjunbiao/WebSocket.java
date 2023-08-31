package com.yinjunbiao;

import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@ServerEndpoint("/websocket/{userId}")
@Component
public class WebSocket {
    private Session session;

    private String userId;
    private static CopyOnWriteArrayList<WebSocket> webSockets = new CopyOnWriteArrayList<>();

    private static ConcurrentHashMap<String ,Session> sessionPool = new ConcurrentHashMap<>();
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId = userId;
        //加入map
       sessionPool.put(this.userId,this.session);
        webSockets.add(this);
        System.out.println("已连接");
    }
    @OnClose
    public void onClose(){
        webSockets.remove(this);
        sessionPool.remove(this.userId);
        System.out.println("duanai");
    }

    @OnMessage
    public void onMessage(String message){
        System.out.println(message);
    }
}
