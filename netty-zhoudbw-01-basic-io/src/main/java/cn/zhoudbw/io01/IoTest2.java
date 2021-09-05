package cn.zhoudbw.io01;

import java.io.*;

/**
 * @author zhoudbw
 * io.txt本质上还是一个文本文件
 * 所以我们可以使用字符流来处理
 * Reader、Writer
 */
public class IoTest2 {
    public static void main(String[] args) throws IOException{
        String basePath = "netty-zhoudbw-01-basic-io/src/";
        File file = new File(basePath + "io.txt");
        if (!file.exists()) {
            System.out.println("io.txt does not exists : create new file.");
            file.createNewFile();
        }
        System.out.println("absolute path of io.txt: " + file.getAbsolutePath());

        // 写入字符型数据
        String str = "hello io again again";
        Writer writer = new FileWriter(file);
        // 不同于字节流，需要将字符转换为字节才能写入
        // 两者的相同点在于，都借助了一个数组来读写
        // OutputStream -> byte[]
        // Writer -> char[] (String底层就是一个char[]，所以看似没有使用数组)
        // 写入char数组内的字符
        writer.write(str);
        // 关闭流模型
        writer.close();

        // 、、、、、、、、、、、、、、、、、、、、、、、、、、、、

        // 读出字符数据
        Reader reader = new FileReader(file);
        // 读操作，InputStream通过byte[]；写操作，OutputStream同样写到byte[]中
        // 读操作，Reader通过char[]；写操作，Writer同样写到char[]中
        char[] readContainer = new char[(int) file.length()];
        int size = reader.read(readContainer);
        System.out.println("data size: " + size + "    data content: " + new String(readContainer));
        // 关闭流模型
        reader.close();
    }
}
