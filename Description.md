# Description

## 中文版

Aurora-Mall（极光商城）是 Spring Cloud Alibaba 全栈电商，后端 13 微服务一域一库，前端 Vue3+PWA，配 Spring AI 客服。亮点：Seata AT 跨库强一致加补偿兜底三层容错、Outbox 加死信队列超时关单、事务后 ack 保 DB 与 MQ 一致、订单状态机加 Redis 生命周期锁支付关单并发只成功一次、迟到支付原路退款不复活、退款 out_request_no 幂等闸、定时任务锁 SET NX 加 Lua 释放、秒杀券 Redis Lua 预占挡热路径。基于 Java17、Spring Boot3.5.4、Spring AI、MyBatis、MySQL、Redis 构建。学习微服务与高并发交易一致性样本。

## English

Aurora-Mall is a full-stack e-commerce system: a Spring Cloud Alibaba backend of 13 database-per-service microservices, a Vue 3 PWA frontend, and a Spring AI customer-service agent. Every concurrency hazard has a named, source-traceable countermeasure: Seata AT global transactions with a compensation-recorder fallback for three-tier fault tolerance, an Outbox plus RabbitMQ dead-letter queue for eventual consistency on payment-timeout cancellations, a transaction-after-commit ack binding that keeps DB and MQ consistent without XA, an order state machine with a Redis lifecycle lock so payment-success and auto-cancellation never both win, late-payment refunds that return money without resurrecting closed orders, an out_request_no idempotency gate stops double refunds, a SET-NX-plus-Lua distributed scheduler lock keeps multi-instance polling honest, and a Redis Lua pre-reserve front-runs seckill coupons off the DB. Built with Java 17, Spring Boot 3.5.4, Spring AI, MyBatis, MySQL and Redis. A solid study reference for microservice architecture and high-concurrency transactional consistency.