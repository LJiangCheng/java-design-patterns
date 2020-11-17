Reactor With NIO
----------------

## NIO

1. 概述：同步非阻塞的I/O模型，I/O多路复用的基础
   * 同步：线程发起一次调用后直到获取到结果才返回
     - IO操作中的同步：API调用返回时调用者就知道结果如何了（实际读取/写入了多少字节）
   * 异步：线程发起一次调用后无论有没有结果，立即返回
     - IO操作中的异步：API调用返回时操作者不知道操作的结果，后面可以通过回调通知结果，也可以不关心结果
   * 阻塞：调用发起后在等待结果的过程中线程挂起
     - IO操作中的阻塞：当无数据可读或不能写入所有数据时，挂起线程
       > 同步阻塞：等待响应的过程中线程挂起   --传统BIO
       > 异步阻塞：等待通知的过程中线程挂起   --这种情形基本没有
   * 非阻塞：调用发起后在等待结果的过程中线程继续执行其他任务
     - IO操作中的非阻塞：读取时，可以读多少数据就读多少然后返回；写出时，可以写多少就写多少然后返回
       > 同步非阻塞：等待响应的过程中线程继续执行其他任务，但时不时查询任务状态，如nio的底层机制epoll  --NIO
       > 异步非阻塞：线程在调用返回后继续执行其他任务 PS:如果有之前调用的通知到达则处理通知，也可能不需要通知或者由其它线程处理通知  --AIO

2. 传统BIO的问题
   * 系统资源的浪费
     > 主要在于大量线程对内存的消耗及线程切换的开销（程序计数器、寄存器的值）：线程是一种昂贵的资源
   * 简单非阻塞IO的问题：任务完成的通知机制问题 --NIO中通过Selector解决
     > 非阻塞模式下，read()方法没读取到数据就会直接返回，但调用方并不知道数据何时会到达，只能不停地调用read()方法进行重试，  
     > 这会造成CPU资源的严重浪费。Selector组件正是为此而生

3. NIO核心组件
   * Channel
     * 描述
       > JDK1.8文档对Channel的描述：
       > A channel represents an open connection to an entity such as a hardware device, a file, a network socket,  
       > or a program component that is capable of performing one or more distinct I/O operations, for example reading or writing.
       > 一个通道代表关联到某个实体的打开的连接，如硬盘、文件、网络套接字，或者能够执行一个或多个IO操作（如读或写）的程序组件。

     * 通道使用起来和BIO的Stream有点像，可以从通道读取数据到buffer中，也可以把buffer中的数据写入通道。区别主要体现在两点：
       > 同一个通道既可以读又可以写，而Stream是单向的只能读或写（InputStream/OutputStream）
       > 通道有非阻塞IO模式

     * 通道是一种很基本的抽象描述，和不同的IO服务交互，执行不同的IO操作，实现不一样
       > FileChannel 读写文件
       > DatagramChannel UDP协议网络通信
       > SocketChannel TCP协议网络通信
       > ServerSocketChannel 服务端监听TCP连接
   * Buffer
     * capacity
     * position
     * limit
   * Selector
     > Selector是一个特殊的组件，用于采集各个通道的状态（即事件）。先将通道注册到selector，设置好关心的事件集，然后就可以通过#select()方法静待事件发生
     > 通道的事件类型：
     > > Accept：有可以接受的连接
     > > Connect：连接成功
     > > Read：读就绪
     > > Write：写就绪
     > Selector解决了前面提到的非阻塞IO不断重试耗费CPU的问题，非阻塞模式下通过Selector，系统线程只为已就绪的通道工作，不用盲目重试了。
     > 当所有通道都没有事件就绪时，eventLoop会在select()处挂起，从而让出CPU资源

4. 简单NIO编程

## Reactor

1. 概念



2. 示例
3. 主从分离

## Netty





















