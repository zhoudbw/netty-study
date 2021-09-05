package cn.zhoudbw.chat05;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @author zhoudbw
 * 群聊系统客户端
 */
public class ChatClient {
    private SocketChannel socketChannel;
    private Selector selector;

    /**
     * 初始化客户端
     */
    public ChatClient() {
        try {
            selector = Selector.open();

            SocketAddress address = new InetSocketAddress("127.0.0.1",1234);
            // 传递地址，直接将客户端绑定到IP和端口上
            socketChannel = SocketChannel.open(address);
            // 设置通道是非阻塞的
            socketChannel.configureBlocking(false);
            // 注册到选择器，客户端关心的是客户端发送过来的是什么，所以注册Read事件
            socketChannel.register(selector, SelectionKey.OP_READ);
            System.out.println("User" + socketChannel.getLocalAddress() + " online(上线) ...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 服务端是读数据
     * 客户端是写数据
     */
    public void sendData(String msg) {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
        try {
            socketChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 其他人说话了，服务端需要推送给客户端
     * 客户端需要读服务端传递过来的数据
     */
    public void readData() {
        // 监听到通道中有读事件发生
        try {
            int num = selector.select();
            if (num > 0) {
                Set<SelectionKey> set = selector.selectedKeys();
                Iterator<SelectionKey> iterator = set.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isReadable()) {
                        SocketChannel needReadChannel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        // 将数据写入到buffer中
                        int num1 = needReadChannel.read(buffer);
                        String msg0 = new String(buffer.array());
                        StringBuilder stringBuffer = new StringBuilder();
                        for (int i = 0; i < num1; i++) {
                            stringBuffer.append(msg0.charAt(i));
                        }
                        System.out.println(stringBuffer.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接客户端
     */
    public static void main(String[] args) {
        final ChatClient chatClient = new ChatClient();
        // 启动一个线程，有时间间隔的去通过readData()方法读取服务端传递的消息
        // 不支持显示创建线程，应当使用线程池，这里就不规范的写了
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    chatClient.readData();
                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        /**
         * 写数据的时候，希望可以接收键盘的输入
         */
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String str = scanner.nextLine();
            chatClient.sendData(str);
        }
    }
}