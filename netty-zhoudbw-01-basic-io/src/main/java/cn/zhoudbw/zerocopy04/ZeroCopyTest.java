package cn.zhoudbw.zerocopy04;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author zhoudbw
 * 不仅仅是在操作系统层面分析出零拷贝的原理。
 * 还要通过Java代码来演示零拷贝的实现方式。
 * <p>
 * 通过零拷贝实现文件复制操作。
 */
public class ZeroCopyTest {

    public static void main(String[] args) throws IOException {
        String basePath = "netty-zhoudbw-01-basic-io/src/";

        /**
         * 通过mmap的方式拷贝文件
         */
//        copyByMmap((basePath + "nio.txt"), (basePath + "nio_mmap.txt"));
        copyBySendFile((basePath + "nio.txt"), (basePath + "nio_sendfile.txt"));
    }

    /**
     * 通过mmap内存映射的方式实现文件拷贝
     *
     * @param sourceName 源文件
     * @param destName   目标文件
     * @throws IOException
     */
    public static void copyByMmap(String sourceName, String destName) throws IOException {
        File source = new File(sourceName);
        File dest = new File(destName);
        if (!dest.exists()) {
            dest.createNewFile();
        }

        FileInputStream fis = new FileInputStream(source);
        FileChannel inChannel = fis.getChannel();

        FileOutputStream fos = new FileOutputStream(dest);
        FileChannel outChannel = fos.getChannel();

        // 为了实现inChannel和outChannel这两个通道的连通，使用MappedByteBuffer,是ByteBuffer的子类
        /**
         * 对应于mmap内存映射的拷贝方式
         */
        MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, source.length());
        outChannel.write(buffer);
        buffer.clear();

        inChannel.close();
        fis.close();
        outChannel.close();
        fos.close();
    }

    /**
     * 比mmap性能更高的拷贝方式 —— sendfile
     */
    public static void copyBySendFile(String sourceName, String destName) throws IOException {
        File source = new File(sourceName);
        File dest = new File(destName);
        if (!dest.exists()) {
            dest.createNewFile();
        }

        FileInputStream fis = new FileInputStream(source);
        FileChannel inChannel = fis.getChannel();

        FileOutputStream fos = new FileOutputStream(dest);
        FileChannel outChannel = fos.getChannel();

        // 将数据从inChannel搬迁到outChannel
        /**
         * 通过transferTo()方法可以直接将A通道数据搬运到B通道，都不需要Buffer。
         * transferTo方法声明：
         *     public abstract long transferTo(long position, long count, WritableByteChannel target)
         * @Param position 数据起始位置
         * @Param count 数据长度
         * @Param target 搬运位置
         *
         */
        inChannel.transferTo(0, inChannel.size(), outChannel);

        inChannel.close();
        fis.close();
        outChannel.close();
        fos.close();
    }
}
