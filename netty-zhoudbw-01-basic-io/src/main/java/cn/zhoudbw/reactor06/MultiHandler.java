package cn.zhoudbw.reactor06;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

/**
 * @author zhoudbw
 */
public class MultiHandler implements Runnable {

    private SelectionKey key;
    private State state;
    /**
     * 增加线程池来实现Handler分发多个子线程处理事件
     */
    private ExecutorService pool;

    private enum State {
        /**
         * READ  代表读的状态
         * Write 代表写的状态
         */
        READ, WRITE
    }

    public MultiHandler(SelectionKey key) {
        this.key = key;
        this.state = State.READ;
    }

    @Override
    public void run() {
        switch (state) {
            case READ:
                /**
                 * 读操作是非常耗时的，
                 * 使用线程池直接创建一个线程，去执行读的操作
                 * 相当于是将读的操作异步化了。
                 */
                pool.execute(() -> {
                    read();
                });
                break;

            case WRITE:
                write();
                break;
            default:
                System.out.println("no status");
        }
    }

    /**
     * 读事件，相对于Handler.java来说，处理逻辑没有发生变化
     * 只是调用的方式发生了变化，不再是同步的，而是异步的。
     */
    private void read() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            int num = channel.read(buffer);
            String msg = new String(buffer.array());

            key.interestOps(SelectionKey.OP_WRITE);
            this.state = State.WRITE;

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 写事件
     */
    private void write() {
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
