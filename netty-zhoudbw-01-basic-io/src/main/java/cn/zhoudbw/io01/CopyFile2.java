package cn.zhoudbw.io01;

import java.io.*;

/**
 * @author zhoudbw
 * 最初使用的是InputStream和OutputStream进行拷贝
 * 它们是一个字节一个字节来处理数据的
 * 这样处理数据性能是非常缓慢的
 * 能不能变成一种批量的处理方式，增加缓冲。
 * 读入的时候读取一块，然后在将这一块写出。
 * IO中提供了Buffer：BufferedInputStream、BufferedOutputStream
 */
public class CopyFile2 {

    public static void copyByBuffer(String srcName, String destName) throws IOException {
        File src = new File(srcName);
        File dest = new File(destName);
        // 判断dest是否存在
        if (!dest.exists()) {
            dest.createNewFile();
        }
        InputStream is = new FileInputStream(src);
        BufferedInputStream bis = new BufferedInputStream(is);
        OutputStream os = new FileOutputStream(dest);
        BufferedOutputStream bos = new BufferedOutputStream(os);
        // 此时不需要使用字节数组，BufferedOutputStream、BufferedInputStream帮我们封装了一个字节数组
        int tmp = 0;
        while ((tmp = bis.read()) != -1) {
            System.out.println("read():" + tmp);
            // 此时写入也发生了变化，写入的是读取到的temp。
            // 这个temp就代表一个字节型数据，bos.write(tmp)就是将接收到的这一个一个字节型数据写出到文件中。
            bos.write(tmp);
        }
        bos.close();
        os.close();
        bis.close();
        is.close();
    }

    public static void main(String[] args) throws IOException {
        String basePath = "netty-zhoudbw-01-basic-io/src/";
        String srcName = basePath + "io.txt";
        String destName = basePath + "new_io2.txt";
        copyByBuffer(srcName, destName);
    }
}
