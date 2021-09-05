package cn.zhoudbw.nio03;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author zhoudbw
 *
 */
public class ChannelTest {
    public static void main(String[] args) throws IOException {
        String basePath = "netty-zhoudbw-01-basic-io/src/";
        File file = new File(basePath + "nio.txt");
        if (!file.exists()) {
            System.out.println("nio.txt does not exists : create new file.");
            file.createNewFile();
        }
        OutputStream os = new FileOutputStream(file);
        // 通过FileOutputStream获取FileChannel (通过Stream获取Channel)
        FileChannel fileChannel = ((FileOutputStream)os).getChannel();
        // 使用Buffer来处理数据
        // 通过ByteBuffer这个具体的实现类，分配一个1024大小的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // 设置要写入nio.txt的内容
        String str = "hello nio";
        // 将字节数据放入到缓冲区
        buffer.put(str.getBytes());

        // 放入完成，需要调用flip()方法刷新
        buffer.flip();
        // 将缓冲区的内容，通过通道写入文件中
        fileChannel.write(buffer);

        // 关闭
        fileChannel.close();
        os.close();
    }
}
