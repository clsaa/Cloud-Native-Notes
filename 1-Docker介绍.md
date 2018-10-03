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

## 1.4.Docker基本原理

这也是容器相比于虚拟机的主要缺陷之一：毕竟后者不仅有模拟出来的硬件机器充当沙盒，而且每个沙盒里还运行着一个完整的 Guest OS 给应用随便折腾。

正是由于 rootfs 的存在，容器才有了一个被反复宣传至今的重要特性：一致性。

### 1.4.1.镜像与容器的一致性

由于云端与本地服务器环境不同，应用的打包过程，一直是使用 PaaS 时最痛苦的一个步骤。但有了容器之后，更准确地说，有了容器镜像（即 rootfs）之后，这个问题被非常优雅地解决了。由于 rootfs 里打包的不只是应用，而是整个操作系统的文件和目录，也就意味着，应用以及它运行所需要的所有依赖，都被封装在了一起。事实上，对于大多数开发者而言，他们对应用依赖的理解，一直局限在编程语言层面。比如 Golang 的 Godeps. Json。但实际上，一个一直以来很容易被忽视的事实是，对一个应用来说，操作系统本身才是它运行所需要的最完整的“依赖库”. 有了容器镜像“打包操作系统’ 的能力，这个最基础的依赖环境也终于变成了应用沙盒的一部分。这就赋予了容器所谓的一致性：无论在本地、云端，还是在一台任何地方的机器上，用户只需要解压打包好的容器镜像，那么这个应用运行所需要的完整的执行环境就被重现出来了。这种深入到操作系统级别的运行环境-致性，打通了应用在本地开发和远端执行环境之间难以逾越的鸿沟。

不过，这时你可能已经发现了另一个非常棘手的问题：难道我每开发一个应用，或者升级一下现有的应用，都要重复制作-次 rootfs 吗？比如，我现在用 Ubuntu 操作系统的 ISO 做了一个 rootfs，然后又在里面安装了 Java 环境，用来部署我的 Java 应用。那么，我的另一个同事在发布他的 Java 应用时，显然希望能够直接使用我安装过 Java 环境的 rootfs，而不是重复这个流程。

一种比较直观的解决办法是，我在制作 rootfs 的时候，每做一步“有意义”的操作，就保存一个 rootfs 出来，这样其他同事就可以按需求去用他需要的 rootfs 了。但是，这个解决办法并不具备推广性。原因在于，一旦你的同事们修改了这个 rootfs，新旧两个 rootfs 之间就没有任何关系了。这样做的结果就是极度的碎片化。那么，既然这些修改都基于一个旧的 rootfs，我们能不能以增量的方式去做这些修改呢？这样做的好处是，所有人都只需要维护相对于 base rootfs 修改的增量内容，而不是每次修改都制造一个“fork“。

答案当然是肯定的。这也正是为何，Docker 公司在实现 Docker 镜像时并没有沿用以前制作 rootfs 的标准流程，而是做了一个小小的创新：Docker 在镜像的设计中，引入了层（layer）的概念。也就是说，用户制作镜像的每一步操作，都会生成一个层，也就是一个增量 rootfs。当然，这个想法不是凭空臆造出来的，而是用到了一种叫作联合文件系统（Union File System）的能力。

Union File System 也叫 UnionFS，最主要的功能是将多个不同位置的目录联合挂载（union mount）到同一个目录下。比如，我现在有两个目录 A 和 B，它们分别有两个文件：

```shell

$ tree
.
├── A
│  ├── a
│  └── x
└── B
  ├── b
  └── x
```

使用联合挂载的方式，将这两个目录挂载到一个公共的目录 C 上：

```shell

$ mkdir C
$ mount -t aufs -o dirs=./A:./B none ./C

```

再查看目录 C 的内容，就能看到目录 A 和 B 下的文件被合并到了一起：


```shell

$ tree ./C
./C
├── a
├── b
└── x

```

可以看到，在这个合并后的目录 C 里，有 a、b、x 三个文件，并且 x 文件只有一份。这，就是“合并”的含义。此外，如果你在目录 C 里对 a、b、x 文件做修改，这些修改也会在对应的目录 A、B 中生效。

假设我们使用一个运行一个ubuntu镜像. 这个所谓的“镜像”，实际。上就是一个 Ubuntu 操作系统的 rootfs，它的内容是 Ubuntu 操作系统的所有文件和目录。不过，与之前我们讲述的 rootfs 稍微不同的是，Docker 镜像使用的 rootfs，往往由多个“层”组成：

```json

"RootFS": {
"Type": "layers",
"Layers": [
  "sha256:f49017d4d5ce9c0f544c...",
  "sha256:8f2b771487e9d6354080...",
  "sha256:ccd4d61916aaa2159429...",
  "sha256:c01d74f99de40e097c73...",
  "sha256:268a067217b5fe78e000..."
]
}

```

可以看到，这个 Ubuntu 镜像，实际上由五个层组成。这五个层就是五个增量 rootfs，每一层都是 Ubuntu 操作系统文件与目录的一部分；而在使用镜像时，Docker 会把这些增量联合挂载在一个统 一的挂载点上(等价于前面例子里的“/C”目录）。

这个挂载点就是/var/ib/docker/aufs/mnt/,比如:

```
/var/lib/docker/aufs/mnt/6e3be5d2ecccae7cc0fcfa2a2f5c89dc21ee30e166be823ceaeba15dce645b3e
```

不出意外的，这个目录里面正是一个完整的 Ubuntu 操作系统：

```
$ ls /var/lib/docker/aufs/mnt/6e3be5d2ecccae7cc0fcfa2a2f5c89dc21ee30e166be823ceaeba15dce645b3e
bin boot dev etc home lib lib64 media mnt opt proc root run sbin srv sys tmp usr var
```

那么，前面提到的五个镜像层，又是如何被联合挂载成这样一个完整的 Ubuntu 文件系统的呢？这个信息记录在AuFS的系统目录/sys/fs/aufs'下面。

首先，通过查看 AuFS 的挂载信息，我们可以找到这个目录对应的 AuFS 的内部 ID（也叫：si):\

```
$ cat /proc/mounts| grep aufs
none /var/lib/docker/aufs/mnt/6e3be5d2ecccae7cc0fc... aufs rw,relatime,si=972c6d361e6b32ba,dio,dirperm1 0 0
```

即，si=972c6d361e6b32ba。

然后使用这个ID,你就可以在/sys/fs/aufs'下查看被联合挂载在-起的各个层的信息：

从这些信息里，我们可以看到，镜像的层都放置在/var/ib/docker/aufs/diff目录下，然后被联合挂载在/var/ib/docker/aufs/mnt里面。

而且，从这个结构可以看出来，这个容器的 rootfs 由如下图所示的三部分组成：

![image](https://static001.geekbang.org/resource/image/8a/5f/8a7b5cfabaab2d877a1d4566961edd5f.png)

第一部分，只读层

它是这个容器的 rootfs 最下面的五层，对应的正是 ubuntu: latest 镜像的五层。可以看到，它们的挂载方式都是只读的（ro+wh，即 readonly+whiteout，至于什么是 whiteout，我下面马。上会讲到）

```
$ ls /var/lib/docker/aufs/diff/72b0744e06247c7d0...
etc sbin usr var
$ ls /var/lib/docker/aufs/diff/32e8e20064858c0f2...
run
$ ls /var/lib/docker/aufs/diff/a524a729adadedb900...
bin boot dev etc home lib lib64 media mnt opt proc root run sbin srv sys tmp usr var

```

第二部分，可读写层

它是这个容器的 rotfs 最上面的一层（6e3be5d2ecccae7cc），它的挂载方式为：rw，即 read write。在没有写入文件之前，这个目录是空的。而一旦在容器里做了写操作，你修改产生的内容就会以增量的方式出现在这个层中。

可是，你有没有想到这样一个问题：如果我现在要做的，是删除只读层里的一个文件呢？

为了实现这样的删除操作，AuFS 会在可读写层创建一个 whiteout 文件，把只读层里的文件“遮挡”起来。

比如，你要删除只读层里一个名叫 foo 的文件，那么这个删除操作实际上是在可读写层创建了一个名叫。Wh. Foo 的文件。这样，当这两个层被联合挂载之后，foo 文件就会被 wh. Foo 文件“遮挡”起来，“消失”了。这个功能，就是“ro+wh”的挂载方式，即只读+whiteout 的含义。我喜欢把 whiteout 形象地翻译为：“白障”。

所以，最上面这个可读写层的作用，就是专】用来存放你修改 rootfs 后产生的增量，无论是增、删、改，都发生在这里。而当我们使用完了这个被修改过的容器之后，还可以使用 docker commit 和 push 指令，保存这个被修改过的可读写层，并上传到 Docker Hub 上，供其他人使用；而与此同时，原先的只读层里的内容则不会有任何变化。这，就是增量 rootfs 的好处。

第三部分，Init 层

它是一个以“-init”结尾的层，夹在只读层和读写层之间。Init 层是 Docker 项目单独生成的一个内 部层，专门用来存放/etc/hosts、/etc/resolv.conf 等信息。

需要这样一层的原因是，这些文件本来属于只读的 Ubuntu 镜像的一部分，但是用户往往需要在启动容器时写入一些指定的值比如 hostname，所以就需要在可读写层对它们进行修改。

可是，这些修改往往只对当前的容器有效，我们并不希望执行 docker commit 时，把这些信息连同可读写层一起提交掉。

所以，Docker 做法是，在修改了这些文件之后，以一个单独的层挂载了出来。而用户执行 docker commit 只会提交可读写层，所以是不包含这些内容的。

最终，这7个层都被联合挂载到/var/ib/docker/aufs/mnt目录下，表现为一个完整的Ubuntu 操作系统供容器使用。

需要注意的是，Dockerfle 里的原语并不都是指对容器内部的操作。就比如 ADD，它指的是把当前目录（即 Dockerfle 所在的目录）里的文件，复制到指定容器内的目录当中。

使用docker exec 命令进入到了容器当中。在了解了 Linux Namespace 的隔离机制后，你应该会很自然地想到一个问题：docker exec 是怎么做到进入容器里的呢？实际上，Linux Namespace 创建的隔离空间虽然看不见摸不着，但一个进程的 Namespace 信息在宿主机_上是确确实实存在的，并且是以一个文件的方式存在。

Docker commit，实际上就是在容器运行起来后，把最上层的“可读写层”，加_上原先容器镜像的只读层，打包组成了一个新的镜像。当然，下面这些只读层在宿主机上是共享的，不会占用额外的空间。而由于使用了联合文件系统，你在容器里对镜像 rootfs 所做的任何修改，都会被操作系统先复制到这个可读写层，然后再修改。这就是所谓的：Copy-on-Write。而正如前所说，Init 层的存在，就是为了避免你执行 docker commit 时，把 Docker 自己对 /etc/hosts等文件做的修改，也-起提交掉。

### 1.4.2.Volume原理

Docker是如何做到把一个宿主机上的目录或者文件，挂载到容器里面去呢？是 Mount Namespace 的黑科技吗？前面已经介绍过，当容器进程被创建之后，尽管开启了 Mount Namespace，但是在它执行 chroot（或者 pivot_ root）之前，容器进程一直可以看到宿主机_上的整个文件系统。

而宿主机上的文件系统，也自然包括了我们要使用的容器镜像。这个镜像的各个层，保存在 /var/lib/docker/aufs/diff目录下，在容器进程启动后，它们会被联合挂载在 /var/ib/docker/aufs/mnt/目录中，这样容器所需的rootfs就准备好了。

所以，我们只需要在 rotfs 准备好之后，在执行 chroot 之前，把 Volume 指定的宿主机目录 (比 如/home目录)，挂载到指定的容器目录(比如/test目录)在宿主机上对应的目录(即 /var/ib/docker/aufs/mnt/[可读写层ID]/test)上，这个Volume的挂载工作就完成了。

更重要的是，由于执行这个挂载操作时，“容器进程”已经创建了，也就意味着此时 MountNamespace 已经开启了。所以，这个挂载事件只在这个容器里可见。你在宿主机上，是看不见容器内部的这个挂载点的。这就保证了容器的隔离性不会被 Volume 打破。

这个原因其实我们前面已经提到过。容器的镜像操作，比如 docker commit，都是发生在宿主机空间的。而由于 Mount Namespace 的隔离作用，宿主机并不知道这个绑定挂载的存在。所以，在宿 主机看来，容器中可读写层的/test目录(/var/lib/docker/aufs/mnt/[可读写层 ID]/test)，始终是空的。

不过，由于Docker一开始还是要创建/test这个目录作为挂载点，所以执行了docker commit 之后，你会发现新产生的镜像里，会多出来一个空的/test目录。毕竟，新建目录操作，又不是挂载操作，Mount Namespace 对它可起不到“障眼法'的作用。

### 1.4.3.原理总览

在前面的四篇文章中，我以 Docker 项目为例，一步步剖析了 Linux 容器的具体实现方式。通过这些讲解你应该能够明白：一个“容器”，实际上是一个由 Linux Namespace、Linux Cgroups 和 rootfs 三种技术构建出来的进程的隔离环境。

从这个结构中我们不难看出，一个正在运行的 Linux 容器，其实可以被“-分为二”地看待:

1. 一组联合挂载在/var/lib/docker/aufs/mnt。上的 rootfs，这-部分我们称为“容器镜像”（Container Image），是容器的静态视图;
2. 一个由 Namespace+Cgroups 构成的隔离环境，这一部分我们称为“容器运行时”（ContainerRuntime），是容器的动态视图。