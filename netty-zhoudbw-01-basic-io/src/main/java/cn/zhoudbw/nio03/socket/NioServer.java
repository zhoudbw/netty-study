package cn.zhoudbw.nio03.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author zhoudbw
 * 基于BIO的Socket实现了服务端。
 * 现在，
 * 基于NIO的Socket实现服务端。
 */
public class NioServer {

    public static void main(String[] args) throws IOException {
        System.out.println("Server start ...");

        // BIO中使用ServerSocket，NIO中使用ServerSocketChannel
        // 创建一个服务端的通道，调用open()方法获取
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        // 为ServerSocketChannel绑定IP Port
        SocketAddress address = new InetSocketAddress("127.0.0.1", 4321);
        serverSocketChannel.socket().bind(address);

        // 接收客户端连接
        // 在BIO中使用Socket，NIO中使用SocketChannel
        SocketChannel socketChannel = serverSocketChannel.accept();

        // BIO中数据处理传输都是用输入输出流，接下来要进行数据处理
        // NIO中，数据处理都要通过Buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);

        // 存入数据并刷新
        byteBuffer.put("Hello client, I am server.".getBytes());
        byteBuffer.flip();

        // 将buffer写入通道中(向客户端的通道写入数据)
        // 客户端得读取Buffer中的数据才可以接收到。
        socketChannel.write(byteBuffer);

        // 读取客户端给我们发送的数据
        ByteBuffer readBuffer = ByteBuffer.allocate(128);
        // 将数据读入到readBuffer中，并刷新参数
        socketChannel.read(readBuffer);
        readBuffer.flip();

        // 遍历数据
        StringBuffer stringBuffer = new StringBuffer();
        while (readBuffer.hasRemaining()) {
            stringBuffer.append((char)readBuffer.get());
        }
        System.out.println("clint data: " + stringBuffer.toString());

        // 关闭通道
        socketChannel.close();
        serverSocketChannel.close();
    }
}
