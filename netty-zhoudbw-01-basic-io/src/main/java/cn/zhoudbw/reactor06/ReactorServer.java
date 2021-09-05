package cn.zhoudbw.reactor06;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author zhoudbw
 */
public class ReactorServer {

    /**
     * 在Reactor模型中：
     * 使用Selector对应角色Reactor 反应器 、通知器、 监听器
     */
    private Selector selector;
    /**
     * 作为服务端，需要有ServerSocketChannel
     */
    private ServerSocketChannel serverChannel;

    /**
     * 初始化Reactor
     */
    public ReactorServer() {
        try {
            // 获取选择器和服务端socket通道
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            // 设置通道为非阻塞的
            serverChannel.configureBlocking(false);
            // 为服务端通道绑定IP和端口。IP默认为本机的 端口8888
            SocketAddress address = new InetSocketAddress(8888);
            serverChannel.socket().bind(address);

            /**注册连接事件到selector的同时，声明一个Acceptor和事件绑定*/
            SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            // 创建一个Acceptor, 用于处理SelectionKey.OP_ACCEPT事件，传递ServerSocketChannel和Selector作为属性
            Acceptor acceptor = new Acceptor(serverChannel, selector);
            // 将Acceptor作为一个附加对象进行绑定到SelectionKey上，当SelectionKey.OP_ACCEPT事件触发时，拿到Acceptor进行处理
            key.attach(acceptor);

            while (true) {
                int num = selector.select();
                if (num == 0) {
                    continue;
                }

                Set<SelectionKey> set = selector.selectedKeys();
                Iterator<SelectionKey> iterator = set.iterator();

                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();

                    // 通过attachment()方法，可以拿到attach()方法绑定的附加对象
                    //   如果事件是接收事件   分发给绑定的acceptor
                    //   如果事件是读写事件   分发给绑定的handler
                    /**因为有一个连接就需要创建一个Acceptor，所以Acceptor实现了Runnable接口*/
                    // Acceptor中run()方法中，处理接收事件，并且创建Handler，将Handler和读事件绑定在一起
                    // 所以在执行时如果有读事件，那么attachment()方法接收到的就是Handler，如果是接收事件那么就是Acceptor。
                    /**
                     * Acceptor的处理逻辑详见 run()方法
                     * Handler的处理逻辑详见  run()方法
                     */
                    Runnable runnable = (Runnable) selectionKey.attachment();
                    runnable.run();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}