package cn.zhoudbw.reactor06;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author zhoudbw
 */
public class Acceptor implements Runnable {

    private ServerSocketChannel serverChannel;
    private Selector selector;

    public Acceptor(ServerSocketChannel serverChannel, Selector selector) {
        this.serverChannel = serverChannel;
        this.selector = selector;
    }

    /**
     * ReactorServer端接收到accept()方法后，创建Acceptor线程处理相关事件。
     * Acceptor的处理接收事件的逻辑封装在run()方法中了。
     */
    @Override
    public void run() {
        try {
            // 通过serverChannel.accept()方法该连接事件的SocketChannel
            SocketChannel socketChannel = serverChannel.accept();
            // 设置该通道为非阻塞的
            socketChannel.configureBlocking(false);
            // 处理完接收事件之后，紧接着就会有后续的读写操作。这里将OP_READ事件注册到selector中。
            SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
            // 注册读写事件后，分发给Handler处理，Acceptor创建Handler，传递事件类型让Handler处理
            /**这里是只有一个Handler时候的处理方式*/
            Handler handler = new Handler(key);

            /**这里是使用多个Handler时候的处理方式*/
            /*MultiHandler handler = new MultiHandler(key);*/

            // Handler也是使用key.attach()方法和对应的SelectionKey绑定，这样就可以对应事件的类型
            // 通过attachment()方法，就可以拿到所需的对应的处理对象（要么是Acceptor，要么是Handler）
            key.attach(handler);

            // 注意：唤醒selector本身。因为原来的selector.select()方法是阻塞的。
            // 所以当我们这里处理完毕后，使用wakeup()方法唤醒阻塞。
            selector.wakeup();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
