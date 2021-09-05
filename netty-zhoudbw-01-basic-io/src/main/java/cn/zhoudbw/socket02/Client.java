package cn.zhoudbw.socket02;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author zhoudbw
 * 客户端的逻辑
 */
public class Client {

    public static void main(String[] args) throws IOException {
        System.out.println("socket client start ...");
        // 需要IP和Port，这样才能确定找的是哪个应用程序
        Socket socket = new Socket("127.0.0.1", 1234);
        // 传输数据，相当于写操作
        OutputStream os = socket.getOutputStream();

        String message = "hello socket !!!";
        os.write(message.getBytes());

        os.close();
        socket.close();
    }
}
