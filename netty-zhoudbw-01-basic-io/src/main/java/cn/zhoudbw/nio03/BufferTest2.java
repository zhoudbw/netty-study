package cn.zhoudbw.nio03;

import java.nio.CharBuffer;

/**
 * @author zhoudbw
 * Buffer的mark()和reset()方法。
 * mark()标记position的位置
 * reset()恢复到mark的position位置
 */
public class BufferTest2 {
    public static void main(String[] args) {

        CharBuffer charBuffer = CharBuffer.allocate(8);
        charBuffer.put('t');
        charBuffer.put('i');
        charBuffer.put('a');
        charBuffer.put('n');
        charBuffer.flip();


        System.out.println("flip() over -> position：" + charBuffer.position());

        /**
         * mark() and reset()
         */
        System.out.println("、、、、、、、读取数据，0号位置，因为此时的position==0、、、、、、、");
        System.out.println(charBuffer.get());
        System.out.println("capacity：" + charBuffer.capacity());
        System.out.println("limit：" + charBuffer.limit());
        System.out.println("position：" + charBuffer.position());

        // 标记此位置position，也就是标记了position==0的位置（存储当前位置）
        charBuffer.mark();

        System.out.println("、、、、、、、读取数据，1号位置， 因为此时position==1、、、、、、、");
        System.out.println(charBuffer.get());
        System.out.println("capacity：" + charBuffer.capacity());
        System.out.println("limit：" + charBuffer.limit());
        System.out.println("position：" + charBuffer.position());

        System.out.println("、、、、、、、调用reset()、、、、、、、");
        // 回退（重置）
        charBuffer.reset();
        System.out.println("capacity：" + charBuffer.capacity());
        System.out.println("limit：" + charBuffer.limit());
        System.out.println("position：" + charBuffer.position());
        /**
         * 此时 position的值和mark()的值一致，相当于中间读取1号位置的代码没有作用一样
         * 没有改变position的值。
         */
    }
}
