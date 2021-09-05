package cn.zhoudbw.nio03;

import java.nio.CharBuffer;

/**
 * @author zhoudbw
 * NIO的Buffer内存模型值的读写校验
 */
public class BufferTest {

    public static void main(String[] args) {

        System.out.println("、、、、、、、初始化Buffer、、、、、、、");
        CharBuffer charBuffer = CharBuffer.allocate(8);
        System.out.println("capacity：" + charBuffer.capacity());
        System.out.println("limit：" + charBuffer.limit());
        System.out.println("position：" + charBuffer.position());

        System.out.println("、、、、、、、存入t & i & a & n 这四个字母、、、、、、、");
        charBuffer.put('t');
        charBuffer.put('i');
        charBuffer.put('a');
        charBuffer.put('n');
        System.out.println("capacity：" + charBuffer.capacity());
        System.out.println("limit：" + charBuffer.limit());
        System.out.println("position：" + charBuffer.position());

        System.out.println("、、、、、、、调用flip()、、、、、、、");
        charBuffer.flip();
        System.out.println("capacity：" + charBuffer.capacity());
        System.out.println("limit：" + charBuffer.limit());
        System.out.println("position：" + charBuffer.position());

        System.out.println("、、、、、、、读取数据，0号位置、、、、、、、");
        // get() || get(int index) 不传参代表获取第一个，传参代表指定索引位置
        // 调用get()时，即不传参时，position的值变化。
        // 调用get(int index)时，即传参，position的值不变化。
        System.out.println(charBuffer.get());
        System.out.println("capacity：" + charBuffer.capacity());
        System.out.println("limit：" + charBuffer.limit());
        System.out.println("position：" + charBuffer.position());

        System.out.println("、、、、、、、读取数据，2号位置、、、、、、、");
        System.out.println(charBuffer.get(2));
        System.out.println("capacity：" + charBuffer.capacity());
        System.out.println("limit：" + charBuffer.limit());
        System.out.println("position：" + charBuffer.position());

        System.out.println("、、、、、、、遍历charBuffer、、、、、、、");
        // buffer.hasRemaining()方法，可以判断Buffer中是否还有值
        // 如果遍历发生在clear之后，会打印出空的char，因为此时limit=capacity。position=0。
        while (charBuffer.hasRemaining()) {
            // get()不传参的第一个位置，position所在位置的值。
            System.out.println(charBuffer.get());
        }

        System.out.println("、、、、、、、调用clear()方法、、、、、、、");
        // clear()方法重置的是索引位置，对象依然存在，想要获取数据依旧可以。
        charBuffer.clear();
        System.out.println("capacity：" + charBuffer.capacity());
        System.out.println("limit：" + charBuffer.limit());
        System.out.println("position：" + charBuffer.position());

    }
}
