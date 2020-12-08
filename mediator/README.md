---
layout: pattern
title: Mediator
folder: mediator
permalink: /patterns/mediator/
categories: Behavioral
tags:
 - Gang Of Four
 - Decoupling
---

## Intent

定义一个对象封装一组对象的交互关系
中介者通过防止对象之间显式地互相引用以促进松耦合，并允许你独立地改变它们的交互关系
Define an object that encapsulates how a set of objects interact.
Mediator promotes loose coupling by keeping objects from referring to each
other explicitly, and it lets you vary their interaction independently.

## Class diagram
![alt text](./etc/mediator_1.png "Mediator")

## Applicability
Use the Mediator pattern when

* a set of objects communicate in well-defined but complex ways. The resulting interdependencies are unstructured and difficult to understand
  * 一组对象以定义良好但复杂的方式进行交互。由此产生的依赖关系是非结构化且难以理解的
* reusing an object is difficult because it refers to and communicates with many other objects
  * 对象的复用很困难，因为它依赖了很多其他的对象
* a behavior that's distributed between several classes should be customizable without a lot of subclassing
  * 分布在几个类之间的行为应该是可定制的，而不需要大量的子类化

## Real world examples

* All scheduleXXX() methods of [java.util.Timer](http://docs.oracle.com/javase/8/docs/api/java/util/Timer.html)
* [java.util.concurrent.Executor#execute()](http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executor.html#execute-java.lang.Runnable-)
* submit() and invokeXXX() methods of [java.util.concurrent.ExecutorService](http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html)
* scheduleXXX() methods of [java.util.concurrent.ScheduledExecutorService](http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ScheduledExecutorService.html)
* [java.lang.reflect.Method#invoke()](http://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Method.html#invoke-java.lang.Object-java.lang.Object...-)

## Credits

* [Design Patterns: Elements of Reusable Object-Oriented Software](https://www.amazon.com/gp/product/0201633612/ref=as_li_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=0201633612&linkCode=as2&tag=javadesignpat-20&linkId=675d49790ce11db99d90bde47f1aeb59)
* [Head First Design Patterns: A Brain-Friendly Guide](https://www.amazon.com/gp/product/0596007124/ref=as_li_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=0596007124&linkCode=as2&tag=javadesignpat-20&linkId=6b8b6eea86021af6c8e3cd3fc382cb5b)
