# 6. Docker图形界面管理与监控

## 6.1 Docker管理工具

### 6.1.1 Docker Machine

>**从前:**你需要登录主机，按照主机及操作系统特有的安装以及配置步骤安装Docker， 使其能运行Docker容器.**现在:**Docker Machine的产生简化了这一过程，让你可以使用一条命令在你的计算机，公有云
平台以及私有数据中心创建及管理Docker主机。

* Create Docker Machine主要包括三个Create过程。
  * 首先是Provider Create（ libmachine/provider.go）， 此函数主要是在当前运行docker-machine命令主机上创建以machine name命名的文件夹，并将根证书，服务器证书以及用户证书拷贝到此文件夹。
  * 其次是Driver create（ 例如drivers/virtualbox/virtualbox.go） 用来创建主机，
  * 最后是运行Host create（ libmachine/host.go） 通过SSH安装并配置Docker。 目前在本地环境中使用的是boot2docker镜像，云端环境使用的是Ubuntu镜像。

>其实真相是这样的：自动创建一个虚机并且安装好设置好Docker Engine

* 优势
  * Docker Machine简化了部署的复杂度，无论是在本机的虚拟机上还是在公有云平台，只需要一条命令便可搭建好Docker主机
  * Docker Machine提供了多平台多Docker主机的集中管理
  * Docker Machine 使应用由本地迁移到云端变得简单，只需要修改一下环境变量即可和任意Docker主机通信部署应用。
* 为什么会有Docker Compose
  * 与容器技术同样受到关注的微服务架构也在潜移默化的改变着应用的部署方式，其提倡将应用分割成一系列细小的服务，每个服务专注于单一业务功能，服务之间采用轻量级通信机制相互沟通

### 6.1.2 Docker Swarm

>在很长的一段时间内， Docker只能在单host上运行，其跨host的部署、运行与管理能力颇受外界诟病。跨host能力的薄弱，直接导致Docker容器与host的紧耦合，这种情况下， Docker容器的灵活性很难令人满意，容器的迁移、分组等都成为很难实现的功能点。 Swarm发布于2014年12月，以管理Docker集群，并将其抽象为一个虚拟整体暴露给用户，其架构以及命令比较简单。

* Swarm作为一个管理Docker集群的工具，可以单独部署于一个节点。
* Swarm的具体工作流程： Docker Client发送请求给Swarm； Swarm处理请求并发送至相应的Docker Node； Docker Node执行相应的操作并返回响应。
  1. 运行一个命令去创建一个集群.
  2. 运行另一个命令去启动Swarm.
  3. 在运行有Docker Engine的每个主机上，运行一个命令与上面的集群相连
* 在某些点, Swarm将可以在主机故障时重调度容器.
* Swarm可以很好地与第三方容器编配产品和运供应商提供的编配服务整合，如Mesos

>DockerSwarm就是一个代理,在DockerSwarm上执行一个命令,DockerSwarm找到一个合适的Node执行,很像一个kubernetes

* swarm则将一组docker enginge作为一个集群进行管理，并提供过了lablel， schedule， filter的能力。其中调度部分，允许用户定制自己的调度策略。
  1. docker run -e "constraint： operationsystem=fedora"
  2. docker run -e "constraint： storagedriver=aufs"

### 6.1.3 Docker Compose

* Docker Compose将所管理的容器分为三层，工程（ project），服务（ service）以及容器（ contaienr）。一个工程当中可包含多个服务，每个服务中定义了容器运行的镜像，参数，依赖。一个服务当中可包括多个容器实例， Docker Compose并没有解决负载均衡的问题，因此需要借助其他工具实现服务发现及负载均衡。
* Docker Compose中定义构建的镜像只存在在一台Docker Swarm主机上， 无法做到多主机共享

>真正系统中Docker Compose非常难以维护,Kubernetes不使用这种方式,而是由应用程序自己解决

## 6.2 Shipyard入门

>Shipyard 是一个基于 Web 的 Docker 管理工具，支持多 host，可以把多个 Docker host 上的 containers 统一管理；可以查看 images，甚至 build images；并提供 RESTful API 等等。 Shipyard 要管理和控制 Dockerhost 的话需要先修改 Docker host 上的默认配置使其支持远程管理。

* 所有需要纳管的Docker主机，需要让Docker在TCP上监听，以便被纳管```OPTIONS=-H=unix:///var/run/docker.sock -H=tcp://0.0.0.0:2375```
* 安装shipyard```docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \ shipyard/deploy start```
* 运行,浏览器访问物理机的8080端口： <http://192.168.131.134:8080/> 默认用户名密码```:admin/shipyard```，点击Engines标签页，添加一个Docker主机（ Engine）
* 填写Docker 主机的名字、 CPU核心数、内存数量（单位MB）、以及Labels，比如部署Web的标签为web，以后调度容器的时候，会优先调度到相应的标签。
* 创建成功后，列表页面显示Docker主机的版本信息
* 如果添加失败，则需要排除是否端口不可访问，可以用wget/telnet/curl等方式来排查问题，是在另外主机上看是否能连接
* Containers标签页显示了所有的Docker容器，可以按照Engine（主机）排序，目前还没有分页和查询功能
* 点击Container标签页的Deploy按钮，可以启动新的容器
* 容器的镜像，名称，环境变量，启动参数，是否有Volume存储、是否有端口映射等都可以在界面上定义。启动成功以后，可以看到列表中新容器的状态为RUNNING
* 点击容器的链接，可以看到容器的细节信息，如端口、 CPU占用、内存占用、环境变量、重启策略等，还可以重启、停止、销毁容器或者查看容器日志
* 如果容器启动失败，或者排查问题，则可以查看容器的日志信息
* 该容器会有如下的自我修复能力
  * 该容器的进程被意外Kill而停止时， shipyard能监测到这个变化，并且自动重启此容器。
  * 如果该容器是正常stop的，如登录到主机上执行dockerstop命令，则此容器不会自动重启。
* 在Web的页面上，我们能观察到这些事件

## 6.3 cAdvisor入门

>cAdvisor的监控图默认1秒刷新一次，显示最近一分钟的实时数据，不显示汇聚的和历史数据，也没有阀值告警功能，此外它也无法同时监控多个Docker主机，不过由于其简单方便，并且具备很好的实时性能监控能力，所以适合特殊情况下的性能监控和问题排查。

* google的cAdvisor，免费开源，实施简单，每个Docker主机上启动一个容器即可通过Web端口监

```shell
docker run --volume=/:/rootfs:ro --volume=/var/run:/var/run:rw --volume=/sys:/sys:ro --volume=/var/lib/docker/:/var/lib/docker:ro --publish=8082:8082 --detach=true --name=cadvisor google/cadvisor:latest --port=8082
```

* 上述部分参数可能与主机操作系统有关，需要修改，可参照官方文档：<https://github.com/google/cadvisor>
* 由于shipyard是在本机8080端口运行，因此上面把cAdvisor改为了8082端口，运行起来后，访问本机8082端口，可看到监控界面