spring中的设计模式
------------

## 观察者模式

### 事件机制
   * 事件：ApplicationEvent，封装某种事件的信息
   * 事件监听：ApplicationListener，即观察者，其中只有一个方法onApplicationEvent，当监听的事件发生后该方法会被执行
   * 事件源：ApplicationContext，Spring的核心容器，事件的发布者
   * 事件管理：ApplicationEventMulticaster，用于事件监听器的注册和事件的广播

## 策略模式

### Resource
Resource接口是具体资源访问策略的抽象，也是所有资源访问类所实现的接口
Resource接口本身没有提供访问任何底层资源的实现逻辑，针对不同的底层资源，Spring将会提供不同的Resource实现类，不同的实现类负责不同的资源访问逻辑。

   * UrlResource： 访问网络资源的实现类。
   * ClassPathResource： 访问类加载路径里资源的实现类。
   * FileSystemResource： 访问文件系统里资源的实现类。
   * ServletContextResource： 访问相对于 ServletContext 路径里的资源的实现类.
   * InputStreamResource： 访问输入流资源的实现类。
   * ByteArrayResource： 访问字节数组资源的实现类。

这些Resource实现类，针对不同的的底层资源，提供了相应的资源访问逻辑，并提供便捷的包装，以利于客户端程序的资源访问。

## 工厂模式

### 简单工厂（静态工厂方法）

### 工厂方法
























