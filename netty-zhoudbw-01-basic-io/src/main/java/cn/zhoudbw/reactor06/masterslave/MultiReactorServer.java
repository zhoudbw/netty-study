package cn.zhoudbw.reactor06.masterslave;


import cn.zhoudbw.reactor06.Acceptor;

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
public class MultiReactorServer {

    /**
     * 使用Selector对应角色Reactor 反应器 、通知器、 监听器
     * 实现主从Reactor模型，将Selector拆分成mainSelector和slaveSelector分别对应主Reactor和从Reactor
     */
    private Selector mainSelector;
    private Selector slaveSelector;
    private ServerSocketChannel serverChannel;

    /**
     * 初始化主从Reactor模型
     */
    public MultiReactorServer() {
        try {
            // 获取主Reactor(Selector)和从Reactor(Selector)
            mainSelector = Selector.open();
            slaveSelector = Selector.open();

            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);

            SocketAddress address = new InetSocketAddress(8888);
            serverChannel.socket().bind(address);

            // 将接收事件注册到主Selector中，并创建Acceptor对象，将其和接收事件进行绑定
            SelectionKey key = serverChannel.register(mainSelector, SelectionKey.OP_ACCEPT);
            /**
             * 注意此时将从Selector作为参数传递，因为在Acceptor的run()方法中，是将读事件绑定在传递的Selector中的。
             * 从Selector用于处理读写事件，因此将从Selector作为参数传递到new Acceptor()中。
             */
            Acceptor acceptor = new Acceptor(serverChannel, slaveSelector);
            key.attach(acceptor);

            /**
             * 从Selector使用独立的线程监听事件，避免相互阻塞
             * 使用new HandlerLoop(从选择器).run()方法后，其实相当于，
             * 该方法一直在监听从Selector是否有事件发生。在Acceptor的run()方法中，注册的是读事件
             * 因此该方法实际上是一直在监听是否有读事件的发生，
             * 如果有，那么会处理读事件，并且将写事件也attache()到对应的事件上去。
             * 那么此时监听的就是读和写事件了。
             */
            new HandlerLoop(slaveSelector).run();

            /**
             * 主Selector使用while(true)不停地监听事件
             */
            while (true) {
                int num = mainSelector.select();
                if (num == 0) {
                    continue;
                }

                Set<SelectionKey> set = mainSelector.selectedKeys();
                Iterator<SelectionKey> iterator = set.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();
                    // 拿到之前存储的附加对象
                    //   如果事件是接收事件   分发给绑定的acceptor
                    //   如果事件是读写事件   分发给绑定的handler
                    Runnable runnable = (Runnable) selectionKey.attachment();
                    runnable.run();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}