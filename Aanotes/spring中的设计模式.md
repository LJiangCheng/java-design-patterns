spring中的设计模式
------------

## 核心部分
### 适配器模式
1. AOP中的适配器模式
   * Spring AOP 的实现是基于代理模式，但是 Spring AOP 的增强或通知(Advice)使用到了适配器模式
   > 与之相关的接口是AdvisorAdapter。  
   > Advice常用的类型有：BeforeAdvice（目标方法调用前，前置通知）、AfterAdvice（目标方法调用后，后置通知）、AfterReturningAdvice(目标方法执行结束后，return之前)等等。  
   > 每个类型Advice（通知）都有对应的拦截器:MethodBeforeAdviceInterceptor、AfterReturningAdviceAdapter、AfterReturningAdviceInterceptor。  
   > Spring预定义的通知要通过对应的适配器，适配成 MethodInterceptor接口(方法拦截器)类型的对象（如：MethodBeforeAdviceInterceptor负责适配MethodBeforeAdvice）。

2. SpringMVC中的适配器HandlerAdapter
   * 实现原理：
     * HandlerAdapter根据Handler规则执行不同的Handler
   * 实现过程：
     * DispatcherServlet根据HandlerMapping返回的handler，向HandlerAdapter发起请求，处理Handler。
       HandlerAdapter根据规则找到对应的Handler并让其执行，执行完毕后Handler会向HandlerAdapter返回一个ModelAndView，
       最后由HandlerAdapter向DispatchServelet返回一个ModelAndView。
   * 实现意义：
     * HandlerAdatper使得Handler的扩展变得容易，只需要增加一个新的Handler和一个对应的HandlerAdapter即可。
       因此Spring定义了一个适配接口，使得每一种Controller有一种对应的适配器实现类，让适配器代替controller执行相应的方法。
       这样在扩展Controller时，只需要增加一个适配器类就完成了SpringMVC的扩展了。

### 观察者模式
1. 事件机制
   * 事件：ApplicationEvent，封装某种事件的信息
   * 事件监听：ApplicationListener，即观察者，其中只有一个方法onApplicationEvent，当监听的事件发生后该方法会被执行
   * 事件源：ApplicationContext，Spring的核心容器，事件的发布者
   * 事件管理：ApplicationEventMulticaster，用于事件监听器的注册和事件的广播

### 策略模式
1. Resource
Resource接口是具体资源访问策略的抽象，也是所有资源访问类所实现的接口
Resource接口本身没有提供访问任何底层资源的实现逻辑，针对不同的底层资源，Spring将会提供不同的Resource实现类，不同的实现类负责不同的资源访问逻辑。

   * UrlResource： 访问网络资源的实现类。
   * ClassPathResource： 访问类加载路径里资源的实现类。
   * FileSystemResource： 访问文件系统里资源的实现类。
   * ServletContextResource： 访问相对于 ServletContext 路径里的资源的实现类.
   * InputStreamResource： 访问输入流资源的实现类。
   * ByteArrayResource： 访问字节数组资源的实现类。

这些Resource实现类，针对不同的的底层资源，提供了相应的资源访问逻辑，并提供便捷的包装，以利于客户端程序的资源访问。

### 工厂模式
1. 工厂方法
BeanFactory及其一系列接口和实现类，覆盖了Bean初始化的方方面面，并提供了很多扩展点
另外也可以自定义工厂方法（指定FactoryBean）用于实例化Bean，只需要在配置中指明类和方法即可

### 单例模式
1. AbstractBeanFactory#getBean ==> getSingleton中进行的bean的初始化
Spring中使用的还是双重检查锁的方式实现单例模式，因为它要初始化的并非某个特定的实例，难以使用延迟初始化占位类的方式。
但是Spring采取了其他方式解决DCL方式的安全隐患：正式初始化之前将BeanName放入一个Set中，其他线程使用这个Set判断Bean是否在初始化的过程中

### 装饰器模式
1. Spring中用到的装饰器模式在类名上有两种表现：一种是类名中含有Wrapper，另一种是类名中含有Decorator
   * 实质：
     动态地给一个对象添加一些额外的职责。
     就增加功能来说，Decorator模式相比生成子类更为灵活。

### 代理模式
1. AOP中的代理模式
> AOP能够将那些与业务无关，却为业务模块所共同调用的逻辑或责任（例如事务处理、日志管理、权限控制等）封装起来（抽取公共模块），
  便于减少系统的重复代码，降低模块间的耦合度，并有利于未来的可拓展性和可维护性。
> Spring AOP 就是基于动态代理的，如果要代理的对象，实现了某个接口，那么Spring AOP会使用JDK Proxy，去创建代理对象，  
  而对于没有实现接口的对象，就无法使用 JDK Proxy 去进行代理了，这时候Spring AOP会使用Cglib ，这时候Spring AOP会使用 Cglib 生成一个被代理对象的子类来作为代理

### 模板方法
1. SpringJDBC JdbcTemplate：模板方法和回调模式的结合  见JdbcTemplate.md及模板方法+回调.md

## 其它部分
### 责任链/职责链模式
1. SpringMVC Interceptors：拦截器链，可定制的责任链


















