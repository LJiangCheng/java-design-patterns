---
layout: pattern
title: Delegation
folder: delegation
permalink: /patterns/delegation/
categories: Structural
tags:
 - Decoupling
---

## Also known as
Proxy Pattern

## Intent

这是一种对象对外表达某种特定功能的技术，但实际上这个功能的实现被委托给内部的关联对象（组合方式）
It is a technique where an object expresses certain behavior to the outside but in 
reality delegates responsibility for implementing that behaviour to an associated object. 

## Class diagram
![alt text](./etc/delegation.png "Delegate")

## Applicability
Use the Delegate pattern in order to achieve the following

* Reduce the coupling of methods to their class
* Components that behave identically, but realize that this situation can change in the future.

## Credits

* [Delegate Pattern: Wikipedia ](https://en.wikipedia.org/wiki/Delegation_pattern)
* [Proxy Pattern: Wikipedia ](https://en.wikipedia.org/wiki/Proxy_pattern)
