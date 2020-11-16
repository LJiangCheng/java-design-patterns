/*
 * The MIT License
 * Copyright © 2014-2019 Ilkka Seppälä
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.iluwatar.reactor.framework;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 这个类在Reactor模式中作为
 * This class acts as Synchronous Event De-multiplexer and Initiation Dispatcher of Reactor pattern.
 * Multiple handles i.e. {@link AbstractNioChannel}s can be registered to the reactor and it blocks
 * for events from all these handles. Whenever an event occurs on any of the registered handles, it
 * synchronously de-multiplexes the event which can be any of read, write or accept, and dispatches
 * the event to the appropriate {@link ChannelHandler} using the {@link Dispatcher}.
 *
 * <p>Implementation: A NIO reactor runs in its own thread when it is started using {@link
 * #start()} method. {@link NioReactor} uses {@link Selector} for realizing Synchronous Event
 * De-multiplexing.
 *
 * <p>NOTE: This is one of the ways to implement NIO reactor and it does not take care of all
 * possible edge cases which are required in a real application. This implementation is meant to
 * demonstrate the fundamental concepts that lie behind Reactor pattern.
 */
public class NioReactor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NioReactor.class);

    private final Selector selector;
    private final Dispatcher dispatcher;
    /**
     * SelectionKey和Selector操作的所有变更工作都在reactor主event loop的环境中完成
     * 所以当任何channel需要更改其可读/可写性时，一个新的指令将会被添加到指令队列，然后等待event loop在下一次循环时取出并执行
     * All the work of altering the SelectionKey operations and Selector operations are performed in
     * the context of main event loop of reactor. So when any channel needs to change its readability
     * or writability, a new command is added in the command queue and then the event loop picks up
     * the command and executes it in next iteration.
     */
    private final Queue<Runnable> pendingCommands = new ConcurrentLinkedQueue<>();
    private final ExecutorService reactorMain = Executors.newSingleThreadExecutor();

    /**
     * Creates a reactor which will use provided {@code dispatcher} to dispatch events. The
     * application can provide various implementations of dispatcher which suits its needs.
     *
     * @param dispatcher a non-null dispatcher used to dispatch events on registered channels.
     * @throws IOException if any I/O error occurs.
     */
    public NioReactor(Dispatcher dispatcher) throws IOException {
        this.dispatcher = dispatcher;
        this.selector = Selector.open();
    }

    /**
     * 封装为任务提交到线程池，在一个新线程中启动reactor事件循环
     * Starts the reactor event loop in a new thread.
     */
    public void start() {
        reactorMain.execute(() -> {
            try {
                LOGGER.info("Reactor started, waiting for events...");
                eventLoop();
            } catch (IOException e) {
                LOGGER.error("exception in event loop", e);
            }
        });
    }

    /**
     * Stops the reactor and related resources such as dispatcher.
     *
     * @throws InterruptedException if interrupted while stopping the reactor.
     * @throws IOException          if any I/O error occurs.
     */
    public void stop() throws InterruptedException, IOException {
        reactorMain.shutdownNow();
        selector.wakeup();
        reactorMain.awaitTermination(4, TimeUnit.SECONDS);
        selector.close();
        LOGGER.info("Reactor stopped");
    }

    /**
     * 注册一个新通道到本reactor上
     * reactor将会开始等待这个通道上的事件和事件的任何通知
     * 注册channel的时候reactor通过AbstractNioChannel#getInterestedOps()获知通道感兴趣的操作类型
     * Registers a new channel (handle) with this reactor. Reactor will start waiting for events on
     * this channel and notify of any events. While registering the channel the reactor uses {@link
     * AbstractNioChannel#getInterestedOps()} to know about the interested operation of this channel.
     *
     * @param channel a new channel on which reactor will wait for events. The channel must be bound
     *                prior to being registered.
     * @return this
     * @throws IOException if any I/O error occurs.
     */
    public NioReactor registerChannel(AbstractNioChannel channel) throws IOException {
        //返回一个key，表示该通道在特定的选择器中注册
        SelectionKey key = channel.getJavaChannel().register(selector, channel.getInterestedOps());
        //将一个对象附加到key(用于绑定key需要的某些数据) 每次绑定会丢弃之前绑定的对象，可以传null代表丢弃当前绑定的对象。返回值：之前绑定的对象
        key.attach(channel);
        //channel持有reactor对象（自定义）
        channel.setReactor(this);
        return this;
    }

    /**
     * 正式：启动reactor事件循环
     */
    private void eventLoop() throws IOException {
        // honor interrupt request 响应中断
        while (!Thread.interrupted()) {
            // honor any pending commands first 首先执行预操作集中新增的指令（封装为Runnable）
            processPendingCommands();
            /*
             * 同步事件多路复用发生在这里，这是一个阻塞调用，只有当任何注册到这里的通道上有非阻塞操作产生时才会返回
             * Synchronous event de-multiplexing happens here, this is blocking call which returns when it
             * is possible to initiate non-blocking operation on any of the registered channels.
             */
            selector.select();

            /* 代表发生在已注册的处理器上的事件集合
             * Represents the events that have occurred on registered handles.
             */
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            //遍历并处理就绪的事件
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (!key.isValid()) {
                    iterator.remove();
                    continue;
                }
                processKey(key);
            }
            keys.clear();
        }
    }

    private void processPendingCommands() {
        Iterator<Runnable> iterator = pendingCommands.iterator();
        while (iterator.hasNext()) {
            Runnable command = iterator.next();
            command.run();
            iterator.remove();
        }
    }

    /*
     * Initiation dispatcher logic, it checks the type of event and notifier application specific
     * event handler to handle the event.
     */
    private void processKey(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            onChannelAcceptable(key);
        } else if (key.isReadable()) {
            onChannelReadable(key);
        } else if (key.isWritable()) {
            onChannelWritable(key);
        }
    }

    private static void onChannelWritable(SelectionKey key) throws IOException {
        AbstractNioChannel channel = (AbstractNioChannel) key.attachment();
        channel.flush(key);
    }

    private void onChannelReadable(SelectionKey key) {
        try {
            // reads the incoming data in context of reactor main loop. Can this be improved?
            Object readObject = ((AbstractNioChannel) key.attachment()).read(key);
            dispatchReadEvent(key, readObject);
        } catch (IOException e) {
            try {
                key.channel().close();
            } catch (IOException e1) {
                LOGGER.error("error closing channel", e1);
            }
        }
    }

    /* 使用程序提供的分发器分发事件到处理器上
     * Uses the application provided dispatcher to dispatch events to application handler.
     */
    private void dispatchReadEvent(SelectionKey key, Object readObject) {
        dispatcher.onChannelReadEvent((AbstractNioChannel) key.attachment(), readObject, key);
    }

    private void onChannelAcceptable(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        SelectionKey readKey = socketChannel.register(selector, SelectionKey.OP_READ);
        readKey.attach(key.attachment());
    }

    /**
     * Queues the change of operations request of a channel, which will change the interested
     * operations of the channel sometime in future.
     *
     * <p>This is a non-blocking method and does not guarantee that the operations have changed when
     * this method returns.
     *
     * @param key           the key for which operations have to be changed.
     * @param interestedOps the new interest operations.
     */
    public void changeOps(SelectionKey key, int interestedOps) {
        //添加预操作
        pendingCommands.add(new ChangeKeyOpsCommand(key, interestedOps));
        //唤醒selector以执行预操作
        selector.wakeup();
    }

    /**
     * A command that changes the interested operations of the key provided.
     */
    class ChangeKeyOpsCommand implements Runnable {
        private final SelectionKey key;
        private final int interestedOps;

        public ChangeKeyOpsCommand(SelectionKey key, int interestedOps) {
            this.key = key;
            this.interestedOps = interestedOps;
        }

        public void run() {
            key.interestOps(interestedOps);
        }

        @Override
        public String toString() {
            return "Change of ops to: " + interestedOps;
        }
    }
}
