package com.hanger.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author hanger
 * 2019-09-06 23:04
 */
@ServerEndpoint("/websocket")
@Component
public class WebSocketServer {
    private Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    //与客户端的会话
    private Session session;
    //每个客户端对应的WebSocket对象（concurrent包下的线程安全的集合）
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();


    //建立连接
    @OnOpen
    public void onOpen(Session session) {
        this.session=session;
        webSocketSet.add(this);
        logger.info("[WS消息服务器]：欢迎@[" + session.getId() + "]加入群聊，当前群聊总人数：{}",webSocketSet.size());
    }



    //关闭连接
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        logger.info("[WS消息服务器]：@[" + session.getId() + "]退出群聊，当前群聊总人数：{}",webSocketSet.size());
    }



    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.warn("[WS消息服务器]：@[" + session.getId() + "]的连接出现错误=>" + throwable.getMessage());
    }



    //接收客户端消息
    @OnMessage
    public void onMessage(String message){
        logger.info("[" +  session.getId() + "]：@[WS消息服务器]{}",message);
    }



    //发送消息
    public void sendMessage(String message){
        logger.info("[WS消息服务器]：@[全体成员]{}",message);
        for (WebSocketServer webSocket : webSocketSet){
            try {
                webSocket.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}



