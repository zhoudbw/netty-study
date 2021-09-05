package cn.zhoudbw.io01;

import java.io.*;

/**
 * @author zhoudbw
 * 拷贝文件到指定位置。
 */
public class CopyFile {

    /**
     * 拷贝文件内容，将srcName中的数据拷贝到destName中
     *
     * @param srcName  源文件路径
     * @param destName 目标文件路径
     */
    public static void copy(String srcName, String destName) throws IOException {
        File src = new File(srcName);
        File dest = new File(destName);
        // 判断dest是否存在
        if (!dest.exists()) {
            dest.createNewFile();
        }
        InputStream is = new FileInputStream(src);
        OutputStream os = new FileOutputStream(dest);
        // byte[] 存储中间的数据
        byte[] bytes = new byte[1024];
        int size = 0;
        // read()返回的是byte[]中的数据的长度
        // 如果读取长度为1025的话，那么分别返回size为 1024 1 -1
        // 读文件尾的时候byte[]无长度 此时返回 -1
        while ((size = is.read(bytes)) != -1) {
            System.out.println("read():" + size);
            // 将bytes数据内的数据写入到目目标文件中，写入范围[0, size]
            os.write(bytes, 0, size);
        }
        os.flush();
        os.close();
        is.close();
    }

    public static void main(String[] args) throws IOException {
        String basePath = "netty-zhoudbw-01-basic-io/src/";
        String srcName = basePath + "io.txt";
        String destName = basePath + "new_io.txt";
        copy(srcName, destName);
    }
}
