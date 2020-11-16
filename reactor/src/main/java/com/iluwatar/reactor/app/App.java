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

package com.iluwatar.reactor.app;

import com.iluwatar.reactor.framework.AbstractNioChannel;
import com.iluwatar.reactor.framework.ChannelHandler;
import com.iluwatar.reactor.framework.Dispatcher;
import com.iluwatar.reactor.framework.NioDatagramChannel;
import com.iluwatar.reactor.framework.NioReactor;
import com.iluwatar.reactor.framework.NioServerSocketChannel;
import com.iluwatar.reactor.framework.ThreadPoolDispatcher;
import java.io.IOException;
import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.List;

/**
 * This application demonstrates Reactor pattern. The example demonstrated is a Distributed Logging
 * Service where it listens on multiple TCP or UDP sockets for incoming log requests.
 *
 * <p><i>INTENT</i> <br>
 * The Reactor design pattern handles service requests that are delivered concurrently to an
 * application by one or more clients. The application can register specific handlers for processing
 * which are called by reactor on specific events.
 *
 * <p><i>PROBLEM</i> <br>
 * Server applications in a distributed system must handle multiple clients that send them service
 * requests. Following forces need to be resolved:
 * <ul>
 * <li>Availability</li>
 * <li>Efficiency</li>
 * <li>Programming Simplicity</li>
 * <li>Adaptability</li>
 * </ul>
 *
 * <p><i>PARTICIPANTS</i> <br>
 * <ul>
 * <li>Synchronous Event De-multiplexer 同步事件多路复用器
 * <p>
 *     {@link NioReactor} plays the role of synchronous event de-multiplexer.
 * It waits for events on multiple channels registered to it in an event loop.
 * 它在event loop中等待注册到它上面的多个通道的事件
 * </p>
 * </li>
 * <li>Initiation Dispatcher
 * <p>
 *     {@link NioReactor} plays this role as the application specific {@link ChannelHandler}s
 * are registered to the reactor.
 * </p>
 * </li>
 * <li>Handle
 * <p>
 *     {@link AbstractNioChannel} acts as a handle that is registered to the reactor.
 * When any events occur on a handle, reactor calls the appropriate handler.
 * </p>
 * </li>
 * <li>Event Handler
 * <p>
 *      {@link ChannelHandler} acts as an event handler, which is bound to a
 * channel and is called back when any event occurs on any of its associated handles. Application
 * logic resides in event handlers.
 * </p>
 * </li>
 * </ul>
 * 应用程序利用单线程侦听所有端口上的请求。它不会为每一个客户端创建一个线程，这为高负载提供了更好的扩展性。
 * The application utilizes single thread to listen for requests on all ports. It does not create a
 * separate thread for each client, which provides better scalability under load (number of clients
 * increase).
 * The example uses Java NIO framework to implement the Reactor.
 */
public class App {

  private NioReactor reactor;
  private final List<AbstractNioChannel> channels = new ArrayList<>();
  private final Dispatcher dispatcher;

  /**
   * Creates an instance of App which will use provided dispatcher for dispatching events on
   * reactor.
   * 创建一个App实例，使用提供的分发器分发事件
   * @param dispatcher the dispatcher that will be used to dispatch events.
   */
  public App(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  /**
   * App entry.
   */
  public static void main(String[] args) throws IOException {
    //创建自定义ThreadPoolDispatcher用于分发事件
    new App(new ThreadPoolDispatcher(2)).start();
  }

  /**
   * Starts the NIO reactor.
   *
   * @throws IOException if any channel fails to bind.
   */
  public void start() throws IOException {
    /* 创建reactor对象：
     *  设置事件分发器
     *  打开一个选择器(selector)用于后续的通道注册和事件监听
     * dispatcher：
     *  用于在selector接收到事件后将事件派发到指定的handler(handler可以在channel初始化的时候绑定)
     *  可以自定义对不同类型事件的处理方式
     *
     * 应用程序可以自定义事件分发机制
     * The application can customize its event dispatching mechanism.
     */
    reactor = new NioReactor(dispatcher);

    /* 代表分发器在对应的事件发生时可调用的应用程序特定业务逻辑。
     * 在这个例子里，这个事件是读事件
     * This represents application specific business logic that dispatcher will call on appropriate
     * events. These events are read events in our example.
     */
    LoggingHandler loggingHandler = new LoggingHandler();

    /* 将应用程序绑定到多个通道上，使用同一个日志处理器处理到来的日志请求
     * Our application binds to multiple channels and uses same logging handler to handle incoming
     * log requests.
     */
    reactor
        //创建一个channel（同时绑定port和handler）并注册到selector上
        .registerChannel(tcpChannel(6666, loggingHandler))
        .registerChannel(tcpChannel(6667, loggingHandler))
        .registerChannel(udpChannel(6668, loggingHandler))
        //启动reactor主程序，交由线程池处理，主线程到此结束
        .start();
  }

  /**
   * Stops the NIO reactor. This is a blocking call.
   *
   * @throws InterruptedException if interrupted while stopping the reactor.
   * @throws IOException          if any I/O error occurs
   */
  public void stop() throws InterruptedException, IOException {
    reactor.stop();
    dispatcher.stop();
    for (AbstractNioChannel channel : channels) {
      channel.getJavaChannel().close();
    }
  }

  private AbstractNioChannel tcpChannel(int port, ChannelHandler handler) throws IOException {
    NioServerSocketChannel channel = new NioServerSocketChannel(port, handler);
    channel.bind();
    channels.add(channel);
    return channel;
  }

  private AbstractNioChannel udpChannel(int port, ChannelHandler handler) throws IOException {
    NioDatagramChannel channel = new NioDatagramChannel(port, handler);
    channel.bind();
    channels.add(channel);
    return channel;
  }
}
