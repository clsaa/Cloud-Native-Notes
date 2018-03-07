# 5. Docker的网络机制

## 5.1 Linux路由机制打通网络

![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/5-1-01.png)

* docker128上修改Docker0的网络地址，与docker130不冲突

```shell

vi /usr/lib/systemd/system/docker.service

[Unit]
Description=Docker Application Container Engine
Documentation=http://docs.docker.com
After=network.target rhel-push-plugin.socket registries.service
Wants=docker-storage-setup.service
Requires=docker-cleanup.timer

[Service]
Type=notify
NotifyAccess=all
EnvironmentFile=-/run/containers/registries.conf
EnvironmentFile=-/etc/sysconfig/docker
EnvironmentFile=-/etc/sysconfig/docker-storage
EnvironmentFile=-/etc/sysconfig/docker-network
Environment=GOTRACEBACK=crash
Environment=DOCKER_HTTP_HOST_COMPAT=1
Environment=PATH=/usr/libexec/docker:/usr/bin:/usr/sbin
ExecStart=/usr/bin/docker daemon --bip=172.18.42.1/16 \
          --add-runtime docker-runc=/usr/libexec/docker/docker-runc-current \
          --default-runtime=docker-runc \
          --exec-opt native.cgroupdriver=systemd \
          --userland-proxy-path=/usr/libexec/docker/docker-proxy-current \
          $OPTIONS \
          $DOCKER_STORAGE_OPTIONS \
          $DOCKER_NETWORK_OPTIONS \
          $ADD_REGISTRY \
          $BLOCK_REGISTRY \
          $INSECURE_REGISTRY\
      $REGISTRIES
ExecReload=/bin/kill -s HUP $MAINPID
LimitNOFILE=1048576
LimitNPROC=1048576
LimitCORE=infinity
TimeoutStartSec=0
Restart=on-abnormal
MountFlags=slave
KillMode=process

[Install]
WantedBy=multi-user.target

systemctl daemon-reload

service docker restart

```

* 若出现报错

```shell

Mar 07 10:30:42 VM_81_7_centos docker[28370]: time="2018-03-07T10:30:42.244006221+08:00" level=fatal msg="no sockets found via socket activation: make su
Mar 07 10:30:42 VM_81_7_centos systemd[1]: docker.service: main process exited, code=exited, status=1/FAILURE
Mar 07 10:30:42 VM_81_7_centos systemd[1]: Failed to start Docker Application Container Engine.

```

* 尝试删除之前的ip网段

```shell

ip addr
ip link del docker0
ip addr

```

* 类似的在另一个节点上做相同配置只不过ip段为```172.17.42.1/16```

* 目前我们两个节点对应dockerIp段为

```shell

VM_194_204_centos:172.17.42.1/16
VM_81_7_centos:172.18.42.1/16

```

* 设置路由

```shell

在VM_194_204_centos执行 route add -net 172.18.42.1/16 gw 58.87.68.220
在VM_81_7_centos执行 route add -net 172.17.42.1/16 gw 122.152.214.50

```

## 5.2 双网卡独立大二层交换(Linux Bridge)

## 5.3 Overlay网络

>这是当前比较主流的一种Docker网络方案

![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/5-3-01.png)

* GRE/VXLAN介绍<http://www.cnblogs.com/xingyun/p/4620727.html>

* 优势(来自封装)
  * 不影响当前的物理网络,不对当前物理网络做要求
  * 内部可以虚拟任何的IP段来用
* 劣势(来自封装)
  * 性能损耗,当前硬件还不支持拆包

![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/5-3-02.png)

* 逻辑上gre tunnel直接连通实际是通过eth0连通

![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/5-3-03.png)

* ovs官网: <https://www.openvswitch.org/>
* ovs介绍: <http://blog.csdn.net/sqx2011/article/details/39344869>(一个主机上的虚拟交换机,任何此主机上的虚机都可以接到此交换机上进行交换)

## 5.4 官方Libnetwork

### 5.4.1 起源

>开发者不想操作是否是 VLANs, VXLANs, Tunnels 或者是 TEPs. 对于架构人们最关心的是性能和可靠性。而SocketPlane 在 socket 层面提供了一个网络的抽象层，通过可管理的方式去解决各种网络问题。

* Socketplane被docker公司收购，成为其官方网络的起源

* 主要特性
  * Open vSwitch 集成
  * 用于 Docker 的零配置多主机网络
  * Docker/SocketPlane 集群的优雅增长
  * 支持多网络
  * 分布式 IP 地址管理 (IPAM)

![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/5-4-01.png)

### 5.4.2 Libnetwork

![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/5-4-02.png)

* 沙箱(Sandbox)类似一个交换机
* 端点(Endpoint)类似一个端口
* Network相当于一个物理网,一个sandbox可以接到多个物理网,只要在同一个物理网中他们就可以相互访问

1. 将libnetwork集成到Docker Engine
2. 在Docker CLI中使用新的network命令
3. 撰写『 -net』 参数的文档，以告知用户如何使用它来为容器指定网络
4. 在network和endpoint中支持添加『 label』
5. 研发新的『 bridge』 插件，以替换Docker目前的实现
6. 研发『 分布式bridge』 插件，以支持跨容器网络

>Currently libnetwork is nothing more than anattempt to modularize the Docker platform's networking subsystem by moving it into libnetwork as a library.

* 可能要等很久~

>目前看到有一些计划是打算将OVS项目关联到Docker上来，从Linux Kernel 3.3开始， OVS项目就是内核的一部分。当我听到这个的时候我觉得是不是脑袋让驴踢了。首先声明我并不是反对使用OVS，实际上，它是一个非常不错的网络工具套件。它的设置比较复杂，对于新手来说有一个陡峭的学习曲线，但是一旦学会， OVS就可以帮你事半功倍。关于这个话题我听到的一个讨论是：“如果OVS工作在Docker上，那么工作一切都变得很美好”。让我告诉你，亲们：如果让我花费大量时间学习它，最后的结果只能是：“还好，可以用”。我并不想说的那么愤世嫉俗，实际情况是在某些常用环境下OVS会崩溃。因此，使用OVS只是一种疯狂的想法罢了。

<http://containertutorials.com/network/ovs_docker.html>
<https://github.com/openvswitch/ovs/blob/master/utilities/ovs-docker>