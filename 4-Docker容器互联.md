# 4. 容器互联

## 4.1 基于Volume的互联

* /var/lib/docker/graph 存放本地Image里的分层信息(分层信息存储了两份,一份在graphDB中一份在graph文件目录中)
* /var/lib/docker/devicemapper/devicemapper/data 存储了Image与Container的二进制数据文件
* /var/lib/docker/devicemapper/devicemapper/metadata 存储了相关元数据

![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/4-1-01.png)

>graphdriver用来表明如何存储一个镜像或一个容器,其有四种不同的实现方式,类似JDBC有mysqlDriver/oracleDriver等

* Aufs driver是Docker最早支持的driver，但是aufs只是Linux内核的一个补丁集
* Device mapper是Linux 2.6内核中提供的一种从逻辑设备到物理设备的映射框架机制，是LVM2的核心，支持块级别的copy on write特性目前，除少数版本如Ubuntu， Docker基本运行在Devicemapper基础上
* VFS虚拟文件系统的最大缺陷是不支持copy on write特性，每层都是一个单独的目录，如果新增一个child层，则需要将父级层镜像文件一并复制到新目录
* btrfs 非常快，采用btrfs的文件系统级的快照能力来实现layer分层功能，缺点是仍然在进化中，还不够成熟，特别是大量写操作的压力下

![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/4-1-02.png)

>可以看到这种方式不适合高频写文件操作,所以使用Volume的方式

* Volume就是通过宿主机上的一个文件夹与Docker镜像中的某个文件夹互联(挂在),与本机写文件没什么区别
  * 比如大量日志
  * 数据库记录

```shell

docker run --rm=true -it -v /leader java /bin/bash

```

![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/4-1-03.png)

> 可以看到容器中```/leader```目录被挂在到了```/var/lib/docker/volumes/xxxxx..../_data```目录下,**容器删除目录不存在**

* 我们在/var/lib/docker/volumes/xxxxx..../_data下创建一个mybook文件夹,可以在docker中看到mybook目录

![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/4-1-04.png)

>```/storage```目录被挂在到了```/leader java /bin/bash```目录,**容器删除目录存在**

* 基于Volume的互联,可以解决跨主机的共享问题
  * iscsi
  * nfs
  * csph
  * 分布式文件系统

* **基于数据容器的单主机互联**

>数据容器只提供数据其他事情什么都不做(只是一个概念,并没有与其他容器本质性的区别)



```shell

docker run -it -v /leader java /bin/bash
docker run --rm=true --privileged=true --volumes-from=3d1ada481637 -it java /bin/bash

```

>上述将本机目录挂在到docker不利于docker的一致,使镜像依赖于本机目录,在buildFile文件中也是不允许这种方式的.

![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/4-1-05.png)

## 4.2 基于Link的互联

```shell

docker run --rm=true --name=mysqlserver -e MYSQL_ROOT_PASSWORD=123456 mysql

```

* docker默认是允许container互通，通过-icc=false关闭互通。一旦关闭了互通，只能通过-link name:alias命令连接指定container
* -- link redis:db的别名，会在/etc/hosts中生成对应的ip映射

```shell
--link=myjaveserver:serverM1
目标容器（需要连接的容器）: 给一个主机名（ DNS名称）用来代替IP地址进行访问

/usr/bin/docker daemon --icc=false --iptables=true

docker run --rm=true --link=mysqlserver:myserver -it java /bin/bash
/usr/bin/docker daemon --icc=false --iptables=true
iptables-save
只开放container的某个端口
```

* link不支持跨主机
  * 有公司提出了在每个主机上设置一个sidecar(proxy)

![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/4-1-06.png)

>现在不怎么使用

## 4.3 基于网络的互联

> 探索中