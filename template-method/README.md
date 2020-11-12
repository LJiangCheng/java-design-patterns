---
layout: pattern
title: Template method
folder: template-method
permalink: /patterns/template-method/
categories: Behavioral
tags:
 - Gang of Four
---

## Intent

Define the skeleton of an algorithm in an operation, deferring some steps to subclasses. Template 
Method lets subclasses redefine certain steps of an algorithm without changing the algorithm's 
structure.

## Explanation

Real world example

> The general steps in stealing an item are the same. First you pick the target, next you confuse 
> him somehow and finally you steal the item. However there are many ways to implement these steps.   

In plain words

> 模板方法在父类中给出一般性方法的提纲，然后由具体的子类来定义实现细节
> Template Method pattern outlines the general steps in the parent class and lets the concrete child 
> implementations define the details. 

Wikipedia says

> 模板方法描述：父类（通常是一个抽象类）中的一个方法，通过一系列高级步骤定义了某一功能的结构。  
> 这一系列步骤由定义在同一个类中的其他方法实现，这些方法就是模板方法
> In object-oriented programming, the template method is one of the behavioral design patterns 
> identified by Gamma et al. in the book Design Patterns. The template method is a method in a 
> superclass, usually an abstract superclass, and defines the skeleton of an operation in terms of 
> a number of high-level steps. These steps are themselves implemented by additional helper methods 
> in the same class as the template method.

**Programmatic Example**

Let's first introduce the template method class along with its concrete implementations.

```java
public abstract class StealingMethod {

  private static final Logger LOGGER = LoggerFactory.getLogger(StealingMethod.class);

  protected abstract String pickTarget();

  protected abstract void confuseTarget(String target);

  protected abstract void stealTheItem(String target);

  public void steal() {
    var target = pickTarget();
    LOGGER.info("The target has been chosen as {}.", target);
    confuseTarget(target);
    stealTheItem(target);
  }
}

public class SubtleMethod extends StealingMethod {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubtleMethod.class);

  @Override
  protected String pickTarget() {
    return "shop keeper";
  }

  @Override
  protected void confuseTarget(String target) {
    LOGGER.info("Approach the {} with tears running and hug him!", target);
  }

  @Override
  protected void stealTheItem(String target) {
    LOGGER.info("While in close contact grab the {}'s wallet.", target);
  }
}

public class HitAndRunMethod extends StealingMethod {

  private static final Logger LOGGER = LoggerFactory.getLogger(HitAndRunMethod.class);

  @Override
  protected String pickTarget() {
    return "old goblin woman";
  }

  @Override
  protected void confuseTarget(String target) {
    LOGGER.info("Approach the {} from behind.", target);
  }

  @Override
  protected void stealTheItem(String target) {
    LOGGER.info("Grab the handbag and run away fast!");
  }
}
```

Here's the halfling thief class containing the template method.

```java
public class HalflingThief {

  private StealingMethod method;

  public HalflingThief(StealingMethod method) {
    this.method = method;
  }

  public void steal() {
    method.steal();
  }

  public void changeMethod(StealingMethod method) {
    this.method = method;
  }
}
```

And finally we show how the halfling thief utilizes the different stealing methods.

```java
    var thief = new HalflingThief(new HitAndRunMethod());
    thief.steal();
    thief.changeMethod(new SubtleMethod());
    thief.steal();
```

## Class diagram

![alt text](./etc/template_method_urm.png "Template Method")

## Applicability

The Template Method pattern should be used

* To implement the invariant parts of an algorithm once and leave it up to subclasses to implement the behavior that can vary
* When common behavior among subclasses should be factored and localized in a common class to avoid code duplication. This is good example of "refactoring to generalize" as described by Opdyke and Johnson. You first identify the differences in the existing code and then separate the differences into new operations. Finally, you replace the differing code with a template method that calls one of these new operations
* To control subclasses extensions. You can define a template method that calls "hook" operations at specific points, thereby permitting extensions only at those points

## Tutorial

* [Template-method Pattern Tutorial](https://www.journaldev.com/1763/template-method-design-pattern-in-java)

## Real world examples

* [javax.servlet.GenericServlet.init](https://jakarta.ee/specifications/servlet/4.0/apidocs/javax/servlet/GenericServlet.html#init--): 
Method `GenericServlet.init(ServletConfig config)` calls the parameterless method `GenericServlet.init()` which is intended to be overridden in subclasses.
Method `GenericServlet.init(ServletConfig config)` is the template method in this example.

## Credits

* [Design Patterns: Elements of Reusable Object-Oriented Software](https://www.amazon.com/gp/product/0201633612/ref=as_li_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=0201633612&linkCode=as2&tag=javadesignpat-20&linkId=675d49790ce11db99d90bde47f1aeb59)
* [Head First Design Patterns: A Brain-Friendly Guide](https://www.amazon.com/gp/product/0596007124/ref=as_li_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=0596007124&linkCode=as2&tag=javadesignpat-20&linkId=6b8b6eea86021af6c8e3cd3fc382cb5b)
* [Refactoring to Patterns](https://www.amazon.com/gp/product/0321213351/ref=as_li_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=0321213351&linkCode=as2&tag=javadesignpat-20&linkId=2a76fcb387234bc71b1c61150b3cc3a7)
