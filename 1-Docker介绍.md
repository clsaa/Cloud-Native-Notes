# 1. Docker介绍

## 1.1 Docker的历史与现状分析

>新一代的PAAS平台

* 新一代的云应用平台技术则实现全方位的应用生命周期管理，关注开放性、应用的可移植性和云间相互操作性，其代表者包括Cloud Foundry, OpenShift, Docker, Heroku, MoPaaS等PaaS技术或服务， 除了在第一代PaaS技术对用户在实现应用交付的加速所提供的功能外，具备以下大多数特征
  * 多语言和框架:支持多语言和框架以及语言框架的扩展机制
  * 多服务： 开放的核心服务以及服务的扩展机制
  * 多云和多IaaS技术： 支持多种IaaS技术和多云的部署，包括公有云和私有云

## 1.2 Docker的技术原理介绍

* Docker就是虚拟化的一种轻量级替代技术。 Docker的容器技术不依赖任何语言、框架或系统，可以将App变成一种标准化的、可移植的、自管理的组件，并脱离服务器硬件在任何主流系统中开发、调试和运行
* 简单的说就是，在 Linux 系统上迅速创建一个容器（类似虚拟机）并在容器上部署和运行应用程序，并通过配置文件可以轻松实现应用程序的自动化安装、部署和升级，非常方便。因为使用了容器，所以可以很方便的把生产环境和开发环境分开，互不影响，这是 docker 最普遍的一个玩法。

### 1.2.1 cgroups

* Linux系统中经常有个需求就是希望能限制某个或者某些进程的分配资源。 于是就出现了cgroups的概念，cgroup就是controller group ，在这个group中，有分配好的特定比例的cpu时间， IO时间，可用内存大小等。cgroups是将任意进程进行分组化管理的Linux内核功能。最初由google的工程师提出，后来被整合进Linux内核中。
* cgroups中的 重要概念是“子系统”，也就是资源控制器，每种子系统就是一个资源的分配器，比如cpu子系统是控制cpu时间分配的。 首先挂载子系统，然后才有control group的。比如先挂载memory子系统，然后在memory子系统中创建一个cgroup节点，在这个节点中，将需要控制的进程id写入，并且将控制的属性写入，这就完成了内存的资源限制。
* cgroups 被Linux内核支持， 有得天独厚的性能优势，发展势头迅猛。在很多领域可以取代虚拟化技术分割资源。cgroup默认有诸多资源组，可以限制几乎所有服务器上的资源： cpu mem iops,iobandwide,net,device acess等

### 1.2.2 LXC

* LXC是Linux containers的简称，是一种基于容器的操作系统层级的虚拟化技术。 借助于namespace的隔离机制和cgroup限额功能， LXC提供了一套统一的API和工具来建立和管理container。 LXC跟其他操作系统层次的虚拟化技术相比，最大的优势在于LXC被整合进内核，不用单独为内核打补丁
* LXC 旨在提供一个共享kernel的 OS 级虚拟化方法，在执行时不用重复加载Kernel, 且container的kernel与host共享，因此可以大大加快container的 启动过程，并显著减少内存消耗， 容器在提供隔离的同时，还通过共享这些资源节省开销，这意味着容器比真正的虚拟化的开销要小得多。 在实际测试中，基于LXC的虚拟化方法的IO和
* CPU性能几乎接近 baremetal 的性能。虽然容器所使用的这种类型的隔离总的来说非常强大，然而是不是像运行在hypervisor上的虚拟机那么强壮仍具有争议性。如果内核停止，那么所有的容器就会停止运行。

* 性能方面： LXC>>KVM>>XEN
* 内存利用率： LXC>>KVM>>XEN
* 隔离程度： XEN>>KVM>>LXC

### 1.2.3 AUFS

* 什么是AUFS? AuFS是一个能透明覆盖一或多个现有文件系统的层状文件系统。 支持将不同目录挂载到同一个虚拟文件系统下，可以把不同的目录联合在一起，组成一个单一的目录。这种是一种虚拟的文件系统，文件系统不用格式化，直接挂载即可。
* Docker一直在用AuFS作为容器的文件系统。当一个进程需要修改一个文件时， AuFS创建该文件的一个副本。AuFS可以把多层合并成文件系统的单层表示。这个过程称为写入复制（copy on write ） 。
* AuFS允许Docker把某些镜像作为容器的基础。例如，你可能有一个可以作为很多不同容器的基础的CentOS系统镜像。多亏AuFS，只要一个CentOS镜像的副本就够了，这样既节省了存储和内存，也保证更快速的容器部署。
* 使用AuFS的另一个好处是Docker的版本容器镜像能力。每个新版本都是一个与之前版本的简单差异改动，有效地保持镜像文件最小化。但，这也意味着你总是要有一个记录该容器从一个版本到另一个版本改动的审计跟踪。

### 1.2.4 APP打包

* LXC的基础上, Docker额外提供的Feature包括：标准统一的打包部署运行方案
* 为了最大化重用Image，加快运行速度，减少内存和磁盘footprint, Docker container运行时所构造的运行环境，实际上是由具有依赖关系的多个Layer组成的。例如一个apache的运行环境可能是在基础的rootfs image的基础上，叠加了包含例如Emacs等各种工具的image，再叠加包含apache及其相关依赖library的image，这些image由AUFS文件系统加载合并到统一路径中，以只读的方式存在，最后再叠加加载一层可写的空白的Layer用作记录对当前运行环境所作的修改。
* 有了层级化的Image做基础，理想中，不同的APP就可以既可能的共用底层文件系统，相关依赖工具等，同一个APP的不同实例也可以实现共用绝大多数数据，进而以copy on write的形式维护自己的那一份修改过的数据等

## 1.3 Docker的基本概念

### 1.3.1 Docker Container

* Docker Container是Image的实例，共享内核
* Docker Container里可以运行不同Os的Image，比如Ubuntu的或者Centos
* Docker Container不建议内部开启一个SSHD服务， 1.3版本后新增了docker exec命令进入容器排查问题。
* Docker Container没有IP地址，通常不会有服务端口暴露，是一个封闭的“盒子/沙箱”

### 1.3.2 Docker Daemon

Docker Daemon是创建和运行Container的Linux守护进程，也是Docker最主要的核心组件
Docker Daemon可以理解为Docker Container的Container
Docker Daemon可以绑定本地端口并提供Rest API服务，用来远程访问和控制
