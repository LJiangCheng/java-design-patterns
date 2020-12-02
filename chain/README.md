---
layout: pattern
title: Chain of responsibility
folder: chain
permalink: /patterns/chain/
categories: Behavioral
tags:
 - Gang of Four
---

## Intent
通过授予多个对象处理请求的机会以避免请求发出方和接收方的耦合。  
将多个接收方对象组合成链，然后在这条链上传递请求直到某一个对象对它进行处理
Avoid coupling the sender of a request to its receiver by giving more than one object a chance to 
handle the request. Chain the receiving objects and pass the request along the chain until an object 
handles it.

## Explanation

Real world example

> The Orc King gives loud orders to his army. The closest one to react is the commander, then 
> officer and then soldier. The commander, officer and soldier here form a chain of responsibility.

In plain words

有助于建立一条对象链。请求从一端进入，沿着对象链向下传递直到找到合适的处理器。
> It helps to build a chain of objects. A request enters from one end and keeps going from an object 
> to another until it finds a suitable handler.

Wikipedia says

在面向对象的设计中，责任链模式是由一个命令对象和一组处理对象的源组成的设计模式
每一个处理对象定义了它可以处理的命令对象类型及处理逻辑，剩下不能处理的类型会被传递到责任链上的下一个处理对象
> In object-oriented design, the chain-of-responsibility pattern is a design pattern consisting of 
> a source of command objects and a series of processing objects. Each processing object contains 
> logic that defines the types of command objects that it can handle; the rest are passed to the 
> next processing object in the chain.

**Programmatic Example**

Translating our example with the orcs from above. First we have the `Request` class:

```java
/**
* 命令对象
* 封装请求原始信息
*/
public class Request {

  private final RequestType requestType;
  private final String requestDescription;
  private boolean handled;

  public Request(final RequestType requestType, final String requestDescription) {
    this.requestType = Objects.requireNonNull(requestType);
    this.requestDescription = Objects.requireNonNull(requestDescription);
  }

  public String getRequestDescription() { return requestDescription; }

  public RequestType getRequestType() { return requestType; }

  public void markHandled() { this.handled = true; }

  public boolean isHandled() { return this.handled; }

  @Override
  public String toString() { return getRequestDescription(); }
}

/**
* 命令类型
*/
public enum RequestType {
  DEFEND_CASTLE, TORTURE_PRISONER, COLLECT_TAX
}
```

Then the request handler hierarchy

```java
/**
* 责任链对象基类
* 对象链和请求处理基本功能定义
*/
public abstract class RequestHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);
  private final RequestHandler next;

  public RequestHandler(RequestHandler next) {
    this.next = next;
  }

  public void handleRequest(Request req) {
    if (next != null) {
      next.handleRequest(req);
    }
  }

  protected void printHandling(Request req) {
    LOGGER.info("{} handling request \"{}\"", this, req);
  }

  @Override
  public abstract String toString();
}

/**
* 具体的责任对象实现
*/
public class OrcCommander extends RequestHandler {
  public OrcCommander(RequestHandler handler) {
    super(handler);
  }

  @Override
  public void handleRequest(Request req) {
    if (req.getRequestType().equals(RequestType.DEFEND_CASTLE)) {
      printHandling(req);
      req.markHandled();
    } else {
      super.handleRequest(req);
    }
  }

  @Override
  public String toString() {
    return "Orc commander";
  }
}

// OrcOfficer and OrcSoldier are defined similarly as OrcCommander

```

Then we have the Orc King who gives the orders and forms the chain

```java
public class OrcKing {
  RequestHandler chain;

  public OrcKing() {
    /**
    * 初始化构建处理器链
    * 在Spring/JavaWeb中分别对应拦截器链/过滤器链，其初始化过程随着容器的启动而进行，并做成了可配置化
    */
    buildChain();
  }

  private void buildChain() {
    chain = new OrcCommander(new OrcOfficer(new OrcSoldier(null)));
  }

  public void makeRequest(Request req) {
    chain.handleRequest(req);
  }
}
```

Then it is used as follows

```java
var king = new OrcKing();
king.makeRequest(new Request(RequestType.DEFEND_CASTLE, "defend castle")); // Orc commander handling request "defend castle"
king.makeRequest(new Request(RequestType.TORTURE_PRISONER, "torture prisoner")); // Orc officer handling request "torture prisoner"
king.makeRequest(new Request(RequestType.COLLECT_TAX, "collect tax")); // Orc soldier handling request "collect tax"
```

## Class diagram

![alt text](./etc/chain.urm.png "Chain of Responsibility class diagram")

## Applicability

使用场景
Use Chain of Responsibility when

* More than one object may handle a request, and the handler isn't known a priori. The handler should be ascertained automatically.
  * 超过一个对象可能处理请求，且处理器无法提前知晓。处理器应该由程序自动确定
* You want to issue a request to one of several objects without specifying the receiver explicitly.
  * 你希望向多个对象之一发出请求但不显式指定接收方
* The set of objects that can handle a request should be specified dynamically.
  * 请求的处理器集合需要动态指定

## Real world examples

* Logger：通过父子关系关联的层级责任链对象
  * [java.util.logging.Logger#log()](http://docs.oracle.com/javase/8/docs/api/java/util/logging/Logger.html#log%28java.util.logging.Level,%20java.lang.String%29)
* Servlet：JavaWeb的过滤器链
  * [javax.servlet.Filter#doFilter()](http://docs.oracle.com/javaee/7/api/javax/servlet/Filter.html#doFilter-javax.servlet.ServletRequest-javax.servlet.ServletResponse-javax.servlet.FilterChain-)
* [Apache Commons Chain](https://commons.apache.org/proper/commons-chain/index.html)

## Credits

* [Design Patterns: Elements of Reusable Object-Oriented Software](https://www.amazon.com/gp/product/0201633612/ref=as_li_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=0201633612&linkCode=as2&tag=javadesignpat-20&linkId=675d49790ce11db99d90bde47f1aeb59)
* [Head First Design Patterns: A Brain-Friendly Guide](https://www.amazon.com/gp/product/0596007124/ref=as_li_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=0596007124&linkCode=as2&tag=javadesignpat-20&linkId=6b8b6eea86021af6c8e3cd3fc382cb5b)
