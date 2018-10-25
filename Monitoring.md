# 1.微服务监控

## 1.1.CAT

>CAT（Central Application Tracking）是一个实时和接近全量的监控系统，它侧重于对Java应用的监控，基本接入了美团上海侧所有核心应用。目前在中间件（MVC、RPC、数据库、缓存等）框架中得到广泛应用，为美团各业务线提供系统的性能指标、健康状况、监控告警等。自2014年开源以来，除了美团之外，CAT还在携程、陆金所、猎聘网、找钢网等多家互联网公司生产环境应用

### 1.1.1.相关链接

* github: <http://github.com/dianping/cat>
* 官方博客: <https://tech.meituan.com/CAT_in_Depth_Java_Application_Monitoring.html>

### 1.1.2.主要优势与特点

* 生产环境长时间考验
* 多语言客户端支持
* 实时性强, 只有两秒左右的延迟
* 异步数据传输
* 监控数据是全量统计
* 可以报警

* 手动埋点，需要改造才能减少埋点的侵入性
* 日志不落地本地磁盘，较少IO，但很消耗网络资源
* 复杂的消息存储和消息ID查询看起来麻烦，需要建立查询索引

### 1.1.3.监控内容

>CAT支持监控消息类型如下

* Transaction 适合记录跨越系统边界的程序访问行为,比如远程调用，数据库调用，也适合执行时间较长的业务逻辑监控，Transaction用来记录一段代码的执行时间和次数
* Event 用来记录一件事发生的次数，比如记录系统异常，它和transaction相比缺少了时间的统计，开销比transaction要小。
* Heartbeat 表示程序内定期产生的统计信息, 如CPU%, MEM%, 连接池状态, 系统负载等。
* Metric 用于记录业务指标、指标可能包含对一个指标记录次数、记录平均值、记录总和，业务指标最低统计粒度为1分钟。
* Trace 用于记录基本的trace信息，类似于log4j的info信息，这些信息仅用于查看一些相关信息

### 1.1.4.设计

* 整体

![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-25-155948.png)

* 客户端

![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-25-160020.png)

* 服务端

![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-25-160051.png)

* 领域模型

![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-25-160131.png)


## 1.2.ZipKin


>由Twitter公司开源，开放源代码分布式的跟踪系统，用于收集服务的定时数据，以解决微服务架构中的延迟问题，包括数据的收集、存储、查找和展现。

### 1.2.1.相关链接

官网: <https://zipkin.io/>

### 1.2.2.主要优势与特点

* 与SpringCloud可无缝集成,可快速完成部署
* 支持多种数据收集方式(HTTP/MQ),数据存储方式(内存/ES/RDB)
* 经历了生成环境考验(阿里早期和京东都使用定制化开发之后的ZipKin)
* 严格按照Google Dapper论文实现
* 多语言支持

* 功能单一, 监控维度, 监控信息不够丰富, 没有告警功能
* 需要手动埋点(有些组件(如SpringBoot)已经实现了ZipKin的埋点可以不用埋,如果需要监控未被埋点的接口则需要手动埋点)

### 1.2.3.监控内容

* 服务依赖树
* 调用链(详细程度取决于埋点质量),包括方法执行时间,实际主机ip 

### 1.2.4.设计

![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-25-161625.png)

* collector：一旦跟踪数据到达Zipkin collector守护进程，它将被验证，存储和索引，以供Zipkin收集器查找；
* storage：Zipkin最初数据存储在Cassandra上，因为Cassandra是可扩展的，具有灵活的模式，并在Twitter中大量使用；但是这个组件可插入，除了Cassandra之外，还支持ElasticSearch和MySQL；
* search：一旦数据被存储和索引，我们需要一种方法来提取它。查询守护进程提供了一个简单的JSON API来查找和检索跟踪，主要给Web UI使用；
* web UI：创建了一个GUI，为查看痕迹提供了一个很好的界面；Web UI提供了一种基于服务，时间和注释查看跟踪的方法。


## 1.3.Pinpoint

>Pinpoint 是 Naver 开源的一款深度支持 Java 语言的服务追踪系统

### 1.3.1.相关链接

github: <http://naver.github.io/pinpoint/>

### 1.3.2.主要优势与特点


* Pinpoint提供的功能比较丰富
* 无侵入性,无需埋点
* 监控粒度更细,深入到服务中调用的每一个方法

* 定制化开发难度极大
* 使用字节码注入方式, Only Java
* 使用了专用的通信框架(性能提高/可扩展性降低)


### 1.3.3.监控内容

* 服务间关系, 服务与中间件间关系
* 调用链, 包括服务内部方法的调用
* JVM/HOST相关指标


![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-25-163126.png)
![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-21-122514.png)
![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-25-163358.png)

### 1.3.4.设计

![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-25-162829.png)

## 1.4.skywalking


>国人吴晟基于OpenTracking实现的开源项目skywalking>针对分布式系统的APM（应用性能监控）系统，特别针对微服务、cloud native和容器化(Docker, Kubernetes, Mesos)架构， 其核心是个分布式追踪系统。

### 1.4.1.相关链接

github: <https://github.com/apache/incubator-skywalking>

### 1.4.2.主要优势与特点

* 多语言支持
* 多类型探针(支持Pinpoint中的字节码增强也支持传统的埋点)
* 监控内容丰富,详细
* 性能最优
* 经历了生产环境考验
* 易于扩展
* 支持告警
* 支持中间件监控

### 1.4.3.监控内容

* 拓扑图
* 调用链监控, 支持服务内本地方法监控
* 支持多维度的调用链分析/慢请求监控
* HOST/JVM监控
* 服务监控(吞吐量/响应时间/SLA)

![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-25-164507.png)

![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-25-165416.png)

![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-25-164529.png)

![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-25-165446.png)

![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-25-165508.png)

### 1.4.4.设计

![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-25-164914.png)


## 1.5.Prometheus

>Prometheus 是一套开源的系统监控报警框架。它启发于 Google 的 borgmon 监控系统，由工作在 SoundCloud 的 google 前员工在 2012 年创建，作为社区开源项目进行开发，并于 2015 年正式发布。2016 年，Prometheus 正式加入 Cloud Native Computing Foundation，成为受欢迎度仅次于 Kubernetes 的项目。

### 1.5.1.相关链接

* 官网: <https://prometheus.io/>
* IBM Prometheus 入门与实践: <https://www.ibm.com/developerworks/cn/cloud/library/cl-lo-prometheus-getting-started-and-practice/index.html>
* 知乎专栏: <https://zhuanlan.zhihu.com/p/24811652>

### 1.5.2.主要优势与特点

* 强大的多维度数据模型
  * 时间序列数据通过 metric 名和键值对来区分。
  * 所有的 metrics 都可以设置任意的多维标签。
  * 数据模型更随意，不需要刻意设置为以点分隔的字符串。
  * 可以对数据模型进行聚合，切割和切片操作。
  * 支持双精度浮点类型，标签可以设为全 unicode。
* 高效：平均每个采样点仅占 3.5 bytes，且一个 Prometheus server 可以处理数百万的 metrics
* 使用 pull 模式采集时间序列数据
* 与K8S集成好

* 无分布式方案
* 没看到调用链监控相关实例

### 1.5.3.监控内容

* Counter:一种累加的 metric，典型的应用如：请求的个数，结束的任务数， 出现的错误数等等
* Gauge:一种常规的 metric，典型的应用如：温度，运行的 goroutines 的个数
* Histogram:可以理解为柱状图，典型的应用如：请求持续时间，响应大小
* Summary:类似于 Histogram, 典型的应用如：请求持续时间，响应大小。提供观测值的 count 和 sum 功能。提供百分位的功能，即可以按百分比划分跟踪结果。

![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-25-170911.png)

为了保证 Prometheus 确实从 node exporter 中收集数据，可以在 Graph 页面中搜索 metric 名字，如 node_cpu 并点击 Execute，可以在 console 中看到 metric 如下。

* 可以与grafana集成

![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-25-171146.png)

### 1.5.4.设计

![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-25-165854.png)

* Prometheus server 定期从配置好的 jobs 或者 exporters 中拉 metrics，或者接收来自 Pushgateway 发过来的 metrics，或者从其他的 Prometheus server 中拉 metrics。
* Prometheus server 在本地存储收集到的 metrics，并运行已定义好的 alert.rules，记录新的时间序列或者向 Alertmanager 推送警报。
* Alertmanager 根据配置文件，对接收到的警报进行处理，发出告警
* 在图形界面中，可视化采集数据,可以对观察结果采样，分组及统计

## 比较

* 探针的性能消耗: Pinpoint>ZipKin>Skywalking 参考<https://juejin.im/post/5a7a9e0af265da4e914b46f1>
* 代码的侵入性: ZipKin≈CAT>>Pinpoint≈Skywalking
* 水平扩展能力: ZipKin(http通信会对正常的访问造成影响，所以还是推荐基于mq异步方式通信)> pinpoint(agent通过thrift通信框架，发送链路信息到collector)   ≈ Skywalking(单机和集群模式,collector与agent之间的通信使用了gRPC)> Prometheus
* 平台支持:ZipKin≈CAT≈Skywalking>>Pinpoint?Prometheus
* 调用链精度:Pinpoint≈Skywalking>>ZipKin>>Prometheus=0

![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-21-122429.png)
![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-21-122514.png)
![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-21-125502.png)


![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-21-113934.png)

## 其他

### 字节码vsAPI调用

Pinpoint 实现了基于字节码注入的 Java Agent 探针，而 Zipkin 的 Brave 框架仅仅提供了应用层面的 API，但是细想问题远不那么简单。字节码注入是一种简单粗暴的解决方案，理论上来说无论任何方法调用，都可以通过注入代码的方式实现拦截，也就是说没有实现不了的，只有不会实现的。但 Brave 则不同，其提供的应用层面的 API 还需要框架底层驱动的支持，才能实现拦截。比如，MySQL 的 JDBC 驱动，就提供有注入 interceptor 的方法，因此只需要实现 StatementInterceptor 接口，并在 Connection String 中进行配置，就可以很简单的实现相关拦截；而与此相对的，低版本的 MongoDB 的驱动或者是 Spring Data MongoDB 的实现就没有如此接口，想要实现拦截查询语句的功能，就比较困难。

因此在这一点上，Brave 是硬伤，无论使用字节码注入多么困难，但至少也是可以实现的，但是 Brave 却有无从下手的可能，而且是否可以注入，能够多大程度上注入，更多的取决于框架的 API 而不是自身的能力。
