# RabbitMQJsonRPCWorker

This project implements two rabbitmq approaches in an abstract manner to use it later on as an library:

* "Work queue" for being able distribute time-consuming tasks among multiple workers [see here for more..](http://www.rabbitmq.com/tutorials/tutorial-two-java.html)

  ![RabbitMQ-Work queue](/images/rabbitmq-worker.png)
* "RPC" for allowing client-server communication over the queue by using answer queues per client and an unique correlation id [see here for more..](http://www.rabbitmq.com/tutorials/tutorial-six-java.html)

  ![RabbitMQ-RPC](/images/rabbitmq-rpc.png)

