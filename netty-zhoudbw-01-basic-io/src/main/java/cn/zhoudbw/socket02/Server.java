package cn.zhoudbw.socket02;


import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zhoudbw
 * 现有服务端，才会有客户端去连接
 */
public class Server {
    public static void main(String[] args) throws IOException {
        System.out.println("socket server start ...");

        // 使用线程池来处理不同客户端的线程
        // 用线程池创建线程
        // 红线警告：手动创建线程池会更好。
        // 这里直接使用四大线程池之一的可缓冲的线程池：如果当前线程数量超过需要使用的线程数，回收空闲的线程。
        // 当需要使用的时候，判断是否有空闲的线程，如果有直接复用，如果没有自动创建。
        // 此线程池可以无限大。
        ExecutorService threadPool = Executors.newCachedThreadPool();
        // 常见ServerSocket，设定端口号。当启动程序的时候，IP就是我们本机的IP，已经确定了。
        // 设置端口，就决定了我们当前的应用程序使用的端口号，这样就可以直接找到这个应用程序了。
        ServerSocket serverSocket = new ServerSocket(1234);
        // 不断的轮询，看是否有客户端过来连接
        while (true) {
            // 发现有客户端的连接，我们需要处理
            // 等待客户端连接，是阻塞的接收方式
            // 接收到客户端的连接后，我们创建Socket(此时通过accept()方法得到的Socket，映射的就是客户端的Socket)
            /* 增加final修饰符，代表不可变，确保线程安全*/
            final Socket socket = serverSocket.accept();
            // 通过线程池创建处理线程，来处理接收到的客户端的连接
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    // 处理Socket
                    handler(socket);
                }

                // 封装Socket的处理方法(和客户端通信的处理逻辑)
                private void handler(Socket socket) {
                    System.out.println("currentThread ID: " + Thread.currentThread().getId());
                    System.out.println("currentThread Name: " + Thread.currentThread().getName());
                    // 接收客户端传递的数据
                    // 因为此时还是IO操作，所以我们使用字节数据
                    byte[] bytes = new byte[1024];
                    // 通过socket拿到输入流(通过socket获取到的输入流，实际上就是客户端的输入流)
                    InputStream is = null;
                    try {
                        is = socket.getInputStream();
                        // 读取客户端传递过来的数据
                        for (; ; ) {
                            int read = is.read(bytes);
                            if (read == -1) {
                                // 读完成
                                break;
                            }
                            //未读完，打印内容 (new String(byte[], offset, length))
                            System.out.println("accept() content: " + new String(bytes, 0, read));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        // 不论是否抛出异常，都要关闭socket
                        try {
                            assert is != null;
                            is.close();
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }
}
