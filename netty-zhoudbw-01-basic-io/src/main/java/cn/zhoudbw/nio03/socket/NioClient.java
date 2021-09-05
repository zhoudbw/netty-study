package cn.zhoudbw.nio03.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author zhoudbw
 * 基于BIO的Socket实现了客户端。
 * 现在，
 * 基于NIO的Socket实现客户端。
 */
public class NioClient {

    public static void main(String[] args) throws IOException {
        System.out.println("Client start ...");

        SocketChannel socketChannel = SocketChannel.open();
        SocketAddress address = new InetSocketAddress("127.0.0.1", 4321);
        // 注意：此时该处是连接服务端，使用connect()方法，传递地址
        socketChannel.connect(address);

        // 沿用NioServer,先写给客户端，再读客户端传过来的信息

        // 向自己的通道中写，从而让服务端读到数据。
        ByteBuffer writeBuffer = ByteBuffer.allocate(128);
        writeBuffer.put("Hello server, I am client".getBytes());
        writeBuffer.flip();
        // 写入自身的通道中
        socketChannel.write(writeBuffer);

        // 读服务端写入SocketChannel通道中传递过来的信息
        ByteBuffer readBuffer = ByteBuffer.allocate(128);
        socketChannel.read(readBuffer);
        readBuffer.flip();

        // 遍历数据
        StringBuffer stringBuffer = new StringBuffer();
        while (readBuffer.hasRemaining()) {
            stringBuffer.append((char)readBuffer.get());
        }
        System.out.println("server data: " + stringBuffer.toString());

        socketChannel.close();
    }
}
