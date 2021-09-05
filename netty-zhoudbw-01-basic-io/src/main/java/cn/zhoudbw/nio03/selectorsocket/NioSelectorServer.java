package cn.zhoudbw.nio03.selectorsocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author zhoudbw
 * 使用NIO的精华所在Selector实现Server
 */
public class NioSelectorServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Server start ...");

        // BIO中使用ServerSocket，NIO中使用ServerSocketChannel
        // 创建一个服务端的通道，调用open()方法获取
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        // 为ServerSocketChannel绑定IP Port
        SocketAddress address = new InetSocketAddress("127.0.0.1", 4321);
        serverSocketChannel.socket().bind(address);

        // 使用selector一定要让通道是非阻塞的，因为NIO是同步非阻塞的。
        // 将此Channel设置为非阻塞的
        serverSocketChannel.configureBlocking(false);

        // 打开一个选择器（获取一个选择器）
        Selector selector = Selector.open();
        // 将通道注册进选择器，参数(选择器, 监听事件)
        // 初始化监听的是连接事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 通过选择器管理通道
        // 需要感知这些管道是否有真正需要执行的操作
        //   通过select()方法，判断是否有需要执行的操作
        //     select()方法的返回值代表要处理的操作个数 > 0 表示有要处理的事件
        while (true) {
            Thread.sleep(2000);

            int ready = selector.select();
            if (ready == 0) {
                continue;
            }
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            // 不为0表示当前是有操作需要执行的
            // 获取具体要执行的操作集合
            Set<SelectionKey> set = selector.selectedKeys();
            Iterator<SelectionKey> iterator = set.iterator();
            while (iterator.hasNext()) {
                /** SelectionKey对应IO通道实际要处理的操作*/
                SelectionKey key = iterator.next();
                // 迭代到该SelectionKey后，移除该key，防止出现重复处理
                iterator.remove();

                /* ----------> 处理接收事件 <---------- */
                // 判断当前SelectionKey对应的操作是否是我们注册的事件（SelectionKey.OP_ACCEPT）
                // 通过isAcceptable()方法
                if (key.isAcceptable()) {
                    System.out.println("---> Acceptable <---");

                    // 处理连接的情况，通过SeverSocket接收客户端的连接
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    /**
                     * 然后需要现在这个通道进行后续的监听，以进行后续的操作。
                     * -> 设置通道是非阻塞的（First）
                     * -> 将通道注册到Selector中，并且监听写事件
                     * -> 这样就将一系列事情串联起来了，而且具有先后关系
                     * -> 如果SelectionKey.OP_ACCEPT被触发，那么之后执行SelectionKey.OP_WRITE事件
                     */
                    // 设置通道是非阻塞的
                    socketChannel.configureBlocking(false);
                    // 注册通道，并监听后续的写事件
                    socketChannel.register(selector, SelectionKey.OP_WRITE);

                /* ----------> 处理写事件 <---------- */
                } else if (key.isWritable()) {
                    System.out.println("---> Writable <---");

                    /**
                     * 从if(key.isAcceptable())下来，添加了写事件的监听。
                     * 那么，此时select()是可以被 isWritable()触发的。
                     * 所以，此处添加一个else if (key.isWritable()) 的监听
                     */
                    // 通过SelectionKey的channel()方法，可以找到接收到的SocketChannel
                    // 也就是channel()方法获取到事件对应的通道（该通道实际上就是处理SelectionKey.OP_ACCEPT时拿到的通道)
                    SocketChannel socketChannel = (SocketChannel)key.channel();

                    // 使用Buffer处理写操作
                    ByteBuffer writeBuffer = ByteBuffer.allocate(128);
                    writeBuffer.put("hello from 4321".getBytes());
                    writeBuffer.flip();
                    socketChannel.write(writeBuffer);

                    /**
                     * 再把读事件注册进来，这样select()判断时，可能被读事件触发，相应的添加读事件的处理
                     */
                    // 通过SelectionKey注册读事件
                    key.interestOps(SelectionKey.OP_READ);

                /* ----------> 处理读事件 <---------- */
                } else if (key.isReadable()) {
                    System.out.println("---> Readable <---");

                    // 处理SelectionKey.OP_READ对应的事件
                    SocketChannel socketChannel = (SocketChannel)key.channel();
                    ByteBuffer readBuffer = ByteBuffer.allocate(128);
                    socketChannel.read(readBuffer);
                    readBuffer.flip();

                    // 遍历
                    StringBuffer stringBuffer = new StringBuffer();
                    while (readBuffer.hasRemaining()) {
                        stringBuffer.append((char)readBuffer.get());
                    }
                    System.out.println("clint data: " + stringBuffer.toString());

                /* ----------> 处理连接事件 <---------- */
                } else if (key.isConnectable()) {
                    System.out.println("---> Connectable <---");

                    /**
                     * 除了 接收事件、读事件、写事件。
                     * 还有 连接事件。
                     * 经常处理的就是前三种事件。
                     */
                }
            }
        }
    }
}
