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

## 5.5 Linux namespace详解

>namespace起到了隔离的作用,在用户层面用户只能看到自己的namespace下的资源,既每个namespace相当于一个独立的操作系统.容器就是利用了namespace,每个容器使用自己的namespace保持隔离.

* namespace示意图

![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/5-5-01.png)

* namespace的层级关系

![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/5-5-02.png)

* namespace之间的通信
  * 点对点型,veth pair是用于不同network namespace间进行通信的方式， veth pair将一个networknamespace数据发往另一个networknamespace的veth。 ![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/5-5-03.png)
  * 交换机型,Linux Bridge可以实现类似交换机的工作模式，将多个不同Namespace上的网卡连通 ![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/5-5-04.png)

## 5.6 OVS&Docker实战

### 5.6.1 OVS介绍

>OVS是一个功能强大的软件交换机

* OVS基本示意图

![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/5-6-01.png)

* OVS&Docker示意图(同一个子网,也可以不同子网)

![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/5-6-02.png)

* OVS&Docker示意图(不同子网,与Kubernetes类似)

>两个不同子网交互必须走路由,相同子网可以走mac

![image](http://clsaadockerimgbed-10042610.cossh.myqcloud.com/5-6-03.png)

### 5.6.2 OVS安装

* 参考<https://www.jianshu.com/p/658332deac99>

* 关闭SELINUX

```shell

#永久关闭SELINUX
编辑/etc/selinux/config文件，并设置SELINUX=disabled，然后重启生效
#临时关闭SELINUX
setenforce 0
#临时打开SELINUX
setenforce 1
#验证SELINUX状态（Permissive-关闭，Enforcing-打开）
getenforce

```

* 安装依赖包/下载openvswitch/预处理

```shell

#yum -y install make gcc openssl-devel autoconf automake rpm-build redhat-rpm-config

#yum -y install python-devel openssl-devel kernel-devel kernel-debug-devel libtool wget

#wget http://openvswitch.org/releases/openvswitch-2.5.2.tar.gz

#mkdir -p ~/rpmbuild/SOURCES
#cp openvswitch-2.5.2.tar.gz ~/rpmbuild/SOURCES/ 
#cd ~/rpmbuild/SOURCES
#tar xvfz openvswitch-2.5.2.tar.gz
#sed 's/openvswitch-kmod, //g' openvswitch-2.5.2/rhel/openvswitch.spec > openvswitch-2.5.2/rhel/openvswitch_no_kmod.spec
```

* 构建RPM包

```shell

#rpmbuild -bb --nocheck openvswitch-2.5.2/rhel/openvswitch_no_kmod.spec

```

* 安装

```shell

#yum localinstall ~/rpmbuild/RPMS/x86_64/openvswitch-2.5.2-1.x86_64.rpm

```

* 启动服务,检查状态,查看日志

```shell

#yum localinstall ~/rpmbuild/RPMS/x86_64/openvswitch-2.5.2-1.x86_64.rpm

#service openvswitch restart
Restarting openvswitch (via systemctl):                    [  OK  ]

#service openvswitch status
ovsdb-server is running with pid 4567
ovs-vswitchd is running with pid 4581

[root@VM_81_7_centos SOURCES]# tail /var/log/messages
Mar  7 15:01:43 localhost ovs-vsctl: ovs|00001|vsctl|INFO|Called as ovs-vsctl --no-wait -- init -- set Open_vSwitch . db-version=7.12.1
Mar  7 15:01:43 localhost ovs-vsctl: ovs|00001|vsctl|INFO|Called as ovs-vsctl --no-wait set Open_vSwitch . ovs-version=2.5.2 "external-ids:system-id=\"14486b4e-04a8-42f1-b837-bab05ab2072f\"" "system-type=\"unknown\"" "system-version=\"unknown\""
Mar  7 15:01:43 localhost openvswitch: Configuring Open vSwitch system IDs [  OK  ]
Mar  7 15:01:43 localhost kernel: openvswitch: Open vSwitch switching datapath
Mar  7 15:01:43 localhost openvswitch: Inserting openvswitch module [  OK  ]
Mar  7 15:01:43 localhost openvswitch: Starting ovs-vswitchd [  OK  ]
Mar  7 15:01:43 localhost openvswitch: Enabling remote OVSDB managers [  OK  ]
Mar  7 15:01:43 localhost systemd: Started LSB: Open vSwitch switch.
Mar  7 15:02:01 localhost systemd: Started Session 36822 of user root.
Mar  7 15:02:01 localhost systemd: Starting Session 36822 of user root.

```

### 5.6.3 OVS+Docker设置

```shell

ovs-vsctl add-br br0
ovs-vsctl add-port br0 gre1 -- set interface gre1 type=gre
option:remote_ip=192.168.18.128
#添加br0到本地docker0， 使得容器流量通过OVS流经tunnel
brctl addif docker0 br0
ip link set dev br0 up
ip link set dev docker0 up
iptables -t nat -F;iptables -F
ip route add 172.17.0.0/16 dev docker0

```