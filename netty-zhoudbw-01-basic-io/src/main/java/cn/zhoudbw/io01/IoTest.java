package cn.zhoudbw.io01;

import java.io.*;

/**
 * @author zhoudbw
 * 写出数据 OutputStream
 * 读入数据 InputStream
 * * 写出和读入方向的基准在于程序。
 * *** 从外部读入程序中：Input
 * *** 从程序写出到外部：Output
 */
public class IoTest {
    public static void main(String[] args) throws IOException {

        // 此时的相对路径是项目的根路径，也就是 /netty-study
        String basePath = "netty-zhoudbw-01-basic-io/src/";
        // 此时的路径是 /netty-study/netty-zhoudbw-01-basic-io/src/io.txt

        // 如果文件不存在，不创建该文件（映射关系，不是真正的对应）
        File file = new File(basePath + "io.txt");
        // 如果不存在那么创建该文件
        if (!file.exists()) {
            System.out.println("io.txt does not exists : create new file.");
            file.createNewFile();
        }

        System.out.println("absolute path of io.txt: " + file.getAbsolutePath());
        // 打印结果↑：absolute path of io.txt: /Users/zhoudw/Desktop/netty-study/netty-zhoudbw-01-basic-io/src/io.txt

        // 写入字节型数据：os.write(str.getBytes())
        String str = "hello io";
        OutputStream os = new FileOutputStream(file);
        os.write(str.getBytes());

        // 写操作是覆盖操作，会用byte[]内的数据覆盖已有的数据。
        // 这个覆盖是指，两次启动程序。最终结果会以后一个写入为准。
        // 同一次启动程序，多次写入byte[]数据，是追加
        String str2 = " hello io again";
        os.write(str2.getBytes());
        // io.txt内的结果：hello io hello io again

        // 关闭流模型
        os.close();

        // 、、、、、、、、、、、、、、、、、、、、、、、、、、、、

        // 读出字节数据
        InputStream is = new FileInputStream(file);
        // 写是通过byte[]，读同样是读到byte[]中
        byte[] readContainer = new byte[(int) file.length()];
        int size = is.read(readContainer);
        System.out.println("data size: " + size + "    data content: " + new String(readContainer));
        // 关闭流模型
        is.close();
    }
}
