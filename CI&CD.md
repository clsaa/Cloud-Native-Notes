# 20.CI&CD

## 20.1.JenkinsX

### 20.1.1.相关资料

<http://dockone.io/article/5109>
<https://www.oschina.net/p/jenkins-x>
<https://yq.aliyun.com/articles/610117>
<https://cloud.tencent.com/developer/article/1100256>
<http://www.caict.ac.cn/pphd/zb/2018kxy/15pm/5/201808/t20180813_181702.htm>
<https://jenkins-x.io/zh/>

官方文档：http://jenkins-x.io/documentation/

开发路线图：http://jenkins-x.io/contribute/roadmap/

JEP-400: https://github.com/jenkinsci/jep/tree/master/jep/400

博客

https://jenkins.io/blog/2018/03/19/introducing-jenkins-x/

https://thenewstack.io/jenkins-x-brings-automated-pipelines.to-kubernetes/ https://www.cloudbees.com/blog/all-in-on-kubernetes-for-developers 
https://www.weave.works/blog/gitops-high-velocity-cicd-for-kubernetes

### 20.1.1.JenkinsX是什么

>CI/CD solution for modern cloud applications on kubernetes(Kubernetes原生的CI/CD解决方案,用于云生应用的开发部署)

* 解决的问题(DCCM体系)
  * D(DevOps)
  * C(Continue Delivery)
  * C(Container)
  * M(Microservice)

* 设计理念:重新思考云原生应用时代研发工程师和CI/CD的交互方式
  * 自动化
  * 松耦合架构
  * 自服务系统
  * 基础设施即代码
  * 环境自动创建
  * 持续集成持续交付
  * 将一切纳入版本控制

* 核心特性
  * 自动化CI/CD
    * 代码仓库
    * Jenkins流水线(下一代流水线:开箱即用)
    * 应用包管理
    * 构建容器
    * 部署环境
    * 系统间集成
  * 基于GitOps部署:将环境配置纳入git(版本控制)之中,开发人员review配置管理的版本改变,进行批准. ![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-17-143708.png)
  * 环境的灵活管理. ![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-17-144116.png)
  * 预览和信息反馈(提供一个review的环境).

### 20.1.2.核心组件

* 命令行工具
  * 定方提供的命令行工具，作为集群的统一入口，同各个组件进行交互调试
  * 项目地址： https://github.com/jenkins -x/jx 
  * 参考文档：http: //jienkins x.io/commands/jxL
* 包管理工具
  * Helm
    * 用于管理 Kubernetes 资源对象的工具，类似 APT, YUM 和 HOMEBREW
    * 他通过将 Kubernetes 的资源对象打包成 Chart 的形式，完成复杂应用的部署和版本控制
    * 项目地址：https://github.com/kubernetes/helm
  * Chartmuseum
    * 开源 Helm Chart 仓库，用于存放 Chart 文件
    * 项目地址: https://github.com/kubernetes-helm/chartmuseum
  * Monocular
    * 包管理工具的界面
  * Draft
    * Draft是自动化应用构建和运行在Kubernetes上面的工具
    * 具有语言识别能力, 能够自动生成构建脚本, 依赖 环境并打包成docker镜像并部署
  * Nexus
    * 业界流行的开源制品管理工具
    * 用于应用构建包和依赖管理
  * Docker Registry/Harbor
    * 容器镜像管理制品仓库, 用于存放构建, 依赖和部署镜像

### 20.1.3.JenkinsX的准备

* 事先准备工作
  * 可运行Minikube的电脑(内存8G以上)
  * 私有镜像仓库（建议 Harbor）并准备本地镜像（列表参考部署文档）
  * 可用的 GitHub 账号和 Token
  * 一些工具（当然可以使用 jx create cluster minikube 以简化部分操作）

* 坑
  * 确认 minikube 的系统时间跟真实时间一致
  * 内部 docker registry 的地址要添加到 minikube 的 insecure registry中 
  * minikube启动hang掉可删除/private/var/db/dhcpd. Leases 和 ~/.minikube/machines/minikube/hyperkit.pid
  * minikube. Expose pod 运行失败, 请参考: https://github.com/jenkins -x/jx#cannot- access-services -on-minikube.
  * 容器 slave 启动资源不足可调整 Jenkins 系统配置中的资源限制（request 和 limit）缺少 expose 权限，kubectl create clusterrolebinding expose-cluster-rule -- clusterrole=cluster- admin --serviceaccount=jx: expose
  * 持久化 mongodb 注意调整路径权限 chown. -R 1001:1001 mongodb 
  * git push 报错，fatal: could not read Username for 'https: //github. Com' No such device or address，更新 ienkins -git-credentials 配置为个人 Github 账号密码

