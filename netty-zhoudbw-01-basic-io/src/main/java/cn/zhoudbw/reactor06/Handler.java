package cn.zhoudbw.reactor06;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


/**
 * @author zhoudbw
 */
public class Handler implements Runnable {

    private SelectionKey key;
    private State state;


    @Override
    public void run() {

        /**
         * Handler是用来处理读写事件的。
         * 相应的Handler有两种状态。
         * 要么Handler对应读事件；要么Handler对应写事件。
         * 因此，Handler要分情况处理。
         */
        switch (state) {
            case READ:
                read();
                break;
            case WRITE:
                write();
                break;
            default:
                System.out.println("no status");
        }
    }

    /**
     * 通过内部的枚举类，来确定到底是读状态还是写状态。
     */
    private enum State {
        /**
         * READ  代表读的状态
         * Write 代表写的状态
         */
        READ, WRITE
    }

    /**
     * 初始化构造方法
     *
     * @param key 需要处理的事件所映射的SelectionKey
     *            初始化Handler的处理状态为读状态。
     */
    public Handler(SelectionKey key) {
        this.key = key;
        this.state = State.READ;
    }

    /**
     * 处理读事件
     */
    private void read() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            int num = channel.read(buffer);
            String msg = new String(buffer.array());

            /**
             * 因为读写是可以循环的，
             * 而且在读事件发生之后，更加关注写事件，所以注册写事件
             * 读事件时也是同理。
             * 这样也就是实现了读写循环。
             */
            key.interestOps(SelectionKey.OP_WRITE);
            this.state = State.WRITE;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理写事件
     */
    private void write() {
        // 将Hello直接写入ByteBuffer中
        ByteBuffer buffer = ByteBuffer.wrap("hello".getBytes());
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            channel.write(buffer);
            key.interestOps(SelectionKey.OP_READ);
            this.state = State.READ;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
