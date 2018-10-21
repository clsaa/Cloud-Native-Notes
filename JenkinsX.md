# 概述

CI/CD solution for modern cloud applications on kubernetes

Kubernetes原生的CI/CD解决方案,用于云生应用的开发部署

JenkinsX≠Jenkins+Jenkins插件+Kubernetes

JenkinsX=JenkinsCore+Kubernetes

# 核心特性

* 自动化CI/CD
  * Jenkins流水线,OOTB特性
  * 选择项目类型自动生成Jenkinsfile定义流水线
  * 自动生成Dockerfile并打包容器镜像
  * 自动创建Helm Chart并运行在Kubernetes集群
  * 自动关联代码库和流水线，作为代码变更自动触发（基于Webhook实现）
  * 自动版本号自动归档
* 基于GitOps部署:将环境配置纳入git(版本控制)之中,开发人员review配置管理的版本改变,进行批准. ![image](http://cloud-computing-notes-img-bed-1252032169.cossh.myqcloud.com/2018-10-17-143708.png)

# 与Jenkins对比

* 易用性:JenkinsX功能更强大更适合云原生应用, JenkinsX官网文档仅使用一条命令便能搭建好整个JenkinsX环境, 只需输入几条指令无需编写脚本就能进行一个构建一个CI/CD流水线.(实际并非如此, 有很多与云平台/操作系统/kubernetes组件不兼容的地方)
* 侵入性:JenkinsX需改变代码目录结构有侵入性(和其GitOps的理念有关)<https://github.com/clsaa/demo>, Jenkins无侵入性. 
* 可用性:由于发布时间较短JenkinsX当前bug数远远高于JenkinsX, 每天release版本数为3-6个, 不稳定; 另外, Jenkins依赖的很多kubernetes中的服务也是非稳定版本, 加剧了其不稳定性. Jenkins已经在企业有了长时间的考验.<https://github.com/jenkinsci/jep/tree/master/jep/400> 中表明了JenkinsX仍然在draft status
* 兼容性:JenkinsX目前支持的第三方设施为Jenkins的子集<https://jenkins-x.io/contribute/roadmdmdmap/>, 比较致命的在于还不支持python/gitlab/istio

综上, Jenkins建议生产使用, JenkinsX建议探索学习使用.