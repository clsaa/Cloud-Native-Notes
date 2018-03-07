# 7. Kubernetes介绍

## 7.1 Docker与CoreOS的恩怨情仇

* 2013年2月， Docker建立了一个网站发布它的首个演示版本， 3月， 美国加州Alex Polvi正在自己的车库开始他的第二次创业
* 有了第一桶金的Alex这次准备干一票大的，他计划开发一个足以颠覆传统的服务器系统的Linux发行版。为了提供能够从任意操作系统版本稳定无缝地升级到最新版系统的能力， Alex急需解决应用程序与操作系统之间的耦合问题。因此，当时还名不见经传的Docker容器引起了他的注意，凭着敏锐直觉， Alex预见了这个项目的价值，当仁不让地将Docker做为了这个系统支持的第一套应用程序隔离方案。不久以后，他们成立了以自己的系统发行版命名的组织： CoreOS。事实证明，采用Docker这个决定，后来很大程度上成就了CoreOS的生态系统。
* Docker一开始的时候是作为一个组件来构建平台，一个构建块，可以将它分层置入系统来利用容器…这是支撑
* Docker的原始价值，是一个帮助构建东西的简单工具，我认为这是目前它如此成功的原因。
* 但是，Polvi明显觉得，Docker忽略了自己的核心，期望拥有更多功能——成为一个平台。
* Docker 刚问世就红透半边天，不仅拿了融资，还得到了Google 等巨头的支持。 CoreOS此前一直忙于为
* 他们一直认为 Docker 应该成为一个简单的基础单元，但不幸的是事情并如他们期望的那样， Docker正在构建一些工具用于发布云服务器、集群系统以及构建、运行、上传和下载映像等服务，甚至包括底层网络的功能等，以打造自己的Docker平台或生态圈。
* Docker 刚问世就红透半边天，不仅拿了融资，还得到了Google 等巨头的支持。 CoreOS此前一直忙于为Docker 提供技术支持服务
* Alex Polvi 认为，由于 Docker 貌似已经从原本做"业界标准容器"的初心转变成打造一款以容器为中心的企业服务平台， CoreOS 才决定开始推出自己的标准化产品
* Polvi 表示， Docker 在安全性和可组合性方面是有根本上的缺陷的，而 Rocket 的设计原型就是为了弥补这些缺陷。
* CoreOS 的联合创始人兼 CTO Brandon Philips 是 Dcoker 管理委员会的成员， CoreOS 打算继续支持 Docker项目，但是当 Rocket 逐渐成熟之后，他们将重新评估是否继续参与贡献。
* 2015年5月4日举行的CoreOS Fest大会上， CoreOS宣布了它的新合作伙伴： Red Hat、 Google、 Vmware及Apcera。 Docker对容器的标准控制比较独断，而且目前看上去没有引入其他人一起进行设计的可能。 CoreOS的Rocket自定义程度相对更高，对于Google等公司来说，也是可以制衡Docker的手段。所以Google等公司支持Rocket
* 当开始做 CoreOS 的时候，我们便着手构建并传递给大家 Google 的基础架构。如今，伴随着 Tectonic 的诞生，这个目标也实现了。企业可以像 Google 运行其基础设施一样，在世界各地安全运行基于容器的分布式应用。

>cgroup最初由google的工程师提出，后来被整合进Linux内核中而后的Android操作系统也就凭借着这个技术，为每个应用程序分配不同的cgroup，将每个程序进行隔离，达到了一个应用程序不会影响其他应用程序环境的目的。Linux容器正是业界一直关注的Google基础设施Borg和Omega的基础之一，基于之前Google开源的cgroup项目。

```yaml
Borg
这是一个生产环境的经验下的论文,自于Google
高层次的
•任何东西都运行在Borg之中，包含存储系统如CFS和BigTable
•中等类型的集群大小有10k左右的节点，尽管有的要大的多
•节点可以是十分的异构
•使用了Linux的进程隔离（本质上来说是容器），因为Borg出现在现在的虚拟机基础设施之前。效率和启动时间
当时十分重要。
•所有的作业都是静态的链接的可执行文件。
•有非常复杂，十分丰富的资源定义语言可用。
•可以滚动升级运行的作业，这意味着配置和执行文件。这有时需要任务重启，因而容错是很重要的。
```

* 2013年10月3日 - Google发布了自己所用Linux容器系统的开源版本lmctfy
* Docker的负责人之一Solomon Hykes初步探索了代码后认为， lmctfy的一些代码功能非常底层，可以作为独立的库，有可能与Docker等容器项目配合使用。由于代码很干净，占用小，构建体验优秀，他正在考虑将其作为Docker的后端。
* 2015 Docker con大会上，当Polvi和Hykes在台上握手并宣布，启动开放容器基金会，这被认为原有的分裂得到了修复。 Hykes介绍了OCP项目，旨在为容器提供一种通用runtime
* Open Container Project，目标是实现容器镜像格式与运行时的标准化。这很有可能实现， 项目成员包括Docker、CoreOS、 Redhat、 IBM、 Google 、 微软、 IBM、 Google、英特尔、 Amazon、 HP、华为、思科、 EMC 等。
* 我们开发容器APP的经验将为我们合作OCP（开放容器项目）规范起到关键作用。我们期望大多数容器App能直接集成到OCP规范中，稍微做点调整就可以和现存的Docker生态圈完美兼容。最终目标是致力于形成一份统一的容器标准格式规范，而且OCP的成功将意味着容器App的大体目标是令人满意的。 ——Polvi

## 7.2 Kubernetes王者归来

* 2014年6月：谷歌宣布kubernetes 开源。
* 2014年7月： Mircrosoft、 Red Hat、 IBM、 Docker、 CoreOS、 Mesosphere 和Saltstack 加入kubernetes。
* 2014年8月： 2014年8月 ： Mesosphere宣布将kubernetes作为frame整合到mesosphere生态系统中，用于Docker容器集群的调度、部署和管理
* 2014年8月： VMware加入kubernetes社区， Google产品经理 Craig Mcluckie公开表示， VMware将会帮助kubernetes实现利用虚拟化来保证物理主机安全的功能模式。
* 2014年11月 ： HP加入kubernetes 社区。
* 2014年11月： Google容器引擎Alpha启动，谷歌宣布GCE中支持容器及服务，并以kubernetes为构架。
* 2015年1月： Google和Mirantis及伙伴将kubernetes引入OpenStack， 开发者可以在openstack上部署运行kubernetes 应用。
* 2015年4月： Google和CoreOs联合发布Tectonic， 它将kubernetes和CoreOS软件栈整合在了一起。
* 2015年5月： Intel加入kubernetes社区，宣布将合作加速Tectonic软件栈的发展进度。
* 2015年6月： Google容器引擎进入beta版。
* 2015年7月： Google正式加入OpenStack基金会， Kubernetes的产品经理Craig McLuckie宣布Google将成为OpenStack基金会的发起人之一， Google将把它容器计算的专家技术带入OpenStack,成一体提高公有云和私有云的互用性。
* 2015年7月： Kuberentes v1.0正式发布。
* 2015年7月22日Google正式对外发布 Kubernetes v 1.0，与此同时，谷歌联合linux基金会及其他合作伙伴共同成立了CNCF基金会( Cloud Native Computing Foundation)， 并将kuberentes 作为首个编入CNCF管理体系的开源项目，助力容器技术生态的发展进步。
* Kubernete在github开源社区，经过400多位贡献者一年的努力，多达14000次提交，最终达到了在之前会议中确定的发布版本v1的功能要求