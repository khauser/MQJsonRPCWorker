# MQJsonRPCWorker

This project implements two rabbitmq approaches in an abstract manner to use it later on as a library:

* "Work queue" for being able to distribute time-consuming tasks among multiple workers [see here for more..](http://www.rabbitmq.com/tutorials/tutorial-two-java.html)

  ![RabbitMQ-Work queue](/images/rabbitmq-worker.png)
* "RPC" for allowing client-server communication by an unique correlation id and one answer queue per client [see here for more..](http://www.rabbitmq.com/tutorials/tutorial-six-java.html)

  ![RabbitMQ-RPC](/images/rabbitmq-rpc.png)

## Main frameworks
* [Spring Boot] (http://projects.spring.io/spring-boot/)
* [RabbitMQ] (http://www.rabbitmq.com/)
* [JSONRPC 2.0] (http://software.dzhuvinov.com/json-rpc-2.0-base.html)

## Installation
* Install the [Java Development Kit] (http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* Install [RabbitMQ 3.5.4] (http://www.rabbitmq.com/download.html)
* Run `gradlew clean build test` to just build and run the test implementation
* the code under src/test/java is a sample implementation of this library and is used by the test

## Upcoming
* provide a task queue to query whether task is finished or not and also to get a result
* setup a maven repository to upload the library