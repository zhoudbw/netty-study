# （一）基础篇

## 1. I/O基础

I/O，本质上是处理人机交互、机器间交互的问题。I/O应用在两个大的领域：一是磁盘的读写、二是网络数据的传输。

I/O在设计的时候需要考虑很多：

- 我们需要各种各样的媒介打交道，如文件、控制台、网络等等
- 我们需要使用不同的传输方式，如顺序传递、随机传递、二进制形式传递、按照字符传递等等

所以I/O的设计是很复杂的，如何用简单的方式来实现这些复杂的设计呢？通过流模型。

```
流模型：将数据的传递模拟成自然界中水流的形式。
  * 按照方向的不同，划分为：输入流、输出流；
  * 按照大小的不同，划分为：字节流、字符流。

不论我们要处理的是什么样的节点，我们都希望使用同样的处理方式，这种设计模式就是装饰者模式。
	* 使用相同的方式处理不同的节点
```

**具体使用I/O的时候，我们都是用哪些具体的类，这些类的作用又是什么呢？**

| \\\        | 字节流       | 字符流 |
| ---------- | ------------ | ------ |
| **输入流** | InputStream  | Reader |
| **输出流** | OutputStream | Writer |

* 输入流和输出流：从方向上进行划分的。

* 字节流和字符流：按照大小进行划分的。

  * ```
    * 计算机中最小的二进制单位是比特bit，代表的就是0和1。
    * 计算机处理的最小单位是byte，1byte=8bit。
    * 我们人能够识别的最小单位char(字符)，1char=8byte。
    * 这也能够看出字节流和字符流的区别：
    	& 字节流通常处理音频视频图片这样的二进制数据。
    	& 字符流通常处理文本这样的字符数据。
    ```

  * 字节输入流、字节输出流

    ```java
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
    ```

  * 将指定文件拷贝到指定位置

    ```java
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
    ```

  * 字符输入流、字符输出流

    ```java
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
    ```

  * 缓冲流实现文件拷贝

    ```java
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
    ```

    