# <font style="color:rgb(51, 51, 51);">JavaC-OJ 在线判题系统</font>
<!-- 这是一张图片，ocr 内容为： -->
![](https://img.shields.io/badge/Spring%20Boot-2.7.2-brightgreen.svg)<font style="color:rgb(51, 51, 51);"> </font><!-- 这是一张图片，ocr 内容为： -->
![](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2021.0.5.0-blue.svg)<font style="color:rgb(51, 51, 51);"> </font><!-- 这是一张图片，ocr 内容为： -->
![](https://img.shields.io/badge/Java-8-orange.svg)<font style="color:rgb(51, 51, 51);"> </font><!-- 这是一张图片，ocr 内容为： -->
![](https://img.shields.io/badge/MySQL-8.0-blue.svg)<font style="color:rgb(51, 51, 51);"> </font><!-- 这是一张图片，ocr 内容为： -->
![](https://img.shields.io/badge/Redis-%E7%BC%93%E5%AD%98-red.svg)<font style="color:rgb(51, 51, 51);">  
</font><!-- 这是一张图片，ocr 内容为： -->
![](https://img.shields.io/badge/Docker-%E5%AE%B9%E5%99%A8%E5%8C%96-blue.svg)<font style="color:rgb(51, 51, 51);"> </font><!-- 这是一张图片，ocr 内容为： -->
![](https://img.shields.io/badge/RabbitMQ-%E6%B6%88%E6%81%AF%E9%98%9F%E5%88%97-green.svg)<font style="color:rgb(51, 51, 51);"> </font><!-- 这是一张图片，ocr 内容为： -->
![](https://img.shields.io/badge/Nacos-%E6%9C%8D%E5%8A%A1%E6%B3%A8%E5%86%8C%E5%8F%91%E7%8E%B0-purple.svg)<font style="color:rgb(51, 51, 51);"> </font><!-- 这是一张图片，ocr 内容为： -->
![](https://img.shields.io/badge/MyBatis--Plus-3.5.2-yellow.svg)

## 项目介绍
**<font style="color:rgb(51, 51, 51);">JavaC-OJ</font>**<font style="color:rgb(51, 51, 51);"> 是一个基于 Spring Cloud 微服务架构 + RabbitMQ + Docker 的在线 Java 编程题目评测系统。系统能够根据管理员预设的题目用例对用户提交的代码进行安全执行和自动评测，同时系统内置的</font>**<font style="color:rgb(51, 51, 51);">代码沙箱</font>**<font style="color:rgb(51, 51, 51);">可作为</font>**<font style="color:rgb(51, 51, 51);">独立服务</font>**<font style="color:rgb(51, 51, 51);">供其他开发者调用。</font>

<font style="color:rgb(51, 51, 51);">项目地址：</font>[<font style="color:rgb(65, 131, 196);">微服务架构源码</font>](https://github.com/qiuping01/ping-oj-backend/tree/main/oj-backend-microservice)<font style="color:rgb(51, 51, 51);"> | </font>[<font style="color:rgb(65, 131, 196);">代码沙箱源码</font>](https://github.com/qiuping01/ping-oj-backend/tree/main/oj-code-sandbox)<font style="color:rgb(51, 51, 51);"> | </font>[<font style="color:rgb(65, 131, 196);">在线体验</font>](https://oj.qiutang.icu/)

## <font style="color:rgb(51, 51, 51);">目录</font>
+ [<font style="color:rgb(65, 131, 196);">JavaC-OJ 在线判题系统</font>](#javac-oj-%E5%9C%A8%E7%BA%BF%E5%88%A4%E9%A2%98%E7%B3%BB%E7%BB%9F)
    - [<font style="color:rgb(65, 131, 196);">目录</font>](#%E7%9B%AE%E5%BD%95)
    - [<font style="color:rgb(65, 131, 196);">核心功能</font>](#%E6%A0%B8%E5%BF%83%E5%8A%9F%E8%83%BD)
    - [<font style="color:rgb(65, 131, 196);">核心业务流程</font>](#%E6%A0%B8%E5%BF%83%E4%B8%9A%E5%8A%A1%E6%B5%81%E7%A8%8B)
    - [<font style="color:rgb(65, 131, 196);">技术栈</font>](#%E6%8A%80%E6%9C%AF%E6%A0%88)
        * [<font style="color:rgb(65, 131, 196);">核心框架</font>](#%E6%A0%B8%E5%BF%83%E6%A1%86%E6%9E%B6)
        * [<font style="color:rgb(65, 131, 196);">数据存储</font>](#%E6%95%B0%E6%8D%AE%E5%AD%98%E5%82%A8)
        * [<font style="color:rgb(65, 131, 196);">中间件</font>](#%E4%B8%AD%E9%97%B4%E4%BB%B6)
        * [<font style="color:rgb(65, 131, 196);">开发工具</font>](#%E5%BC%80%E5%8F%91%E5%B7%A5%E5%85%B7)
    - [<font style="color:rgb(65, 131, 196);">项目亮点</font>](#%E9%A1%B9%E7%9B%AE%E4%BA%AE%E7%82%B9)
    - [项目演示](#RQKzL)
    - [<font style="color:rgb(65, 131, 196);">核心模块</font>](#%E6%A0%B8%E5%BF%83%E6%A8%A1%E5%9D%97)
        * [<font style="color:rgb(65, 131, 196);">代码沙箱模块</font>](#%E4%BB%A3%E7%A0%81%E6%B2%99%E7%AE%B1%E6%A8%A1%E5%9D%97)
        * [<font style="color:rgb(65, 131, 196);">判题服务模块</font>](#%E5%88%A4%E9%A2%98%E6%9C%8D%E5%8A%A1%E6%A8%A1%E5%9D%97)
        * [<font style="color:rgb(65, 131, 196);">题目服务模块</font>](#%E9%A2%98%E7%9B%AE%E6%9C%8D%E5%8A%A1%E6%A8%A1%E5%9D%97)
        * [<font style="color:rgb(65, 131, 196);">用户服务模块</font>](#%E7%94%A8%E6%88%B7%E6%9C%8D%E5%8A%A1%E6%A8%A1%E5%9D%97)
    - [<font style="color:rgb(65, 131, 196);">安全机制</font>](#%E5%AE%89%E5%85%A8%E6%9C%BA%E5%88%B6)
    - [<font style="color:rgb(65, 131, 196);">部署架构</font>](#%E9%83%A8%E7%BD%B2%E6%9E%B6%E6%9E%84)

## <font style="color:rgb(51, 51, 51);">核心功能</font>
+ <font style="color:rgb(51, 51, 51);">代码提交与判题：支持 Java 语言代码提交和自动判题，限制用户提交频率</font>
+ <font style="color:rgb(51, 51, 51);">代码沙箱：安全执行用户代码，防止恶意代码攻击，返回详细错误类型</font>
+ <font style="color:rgb(51, 51, 51);">用户管理：完整的用户注册、登录、登出功能、点击题号查看提交的代码</font>
+ <font style="color:rgb(51, 51, 51);">题目管理：题目的创建、查询、编辑和管理</font>
+ <font style="color:rgb(51, 51, 51);">异步判题：基于 RabbitMQ 的异步判题机制，提升系统响应速度</font>
+ <font style="color:rgb(51, 51, 51);">微服务架构：高可用、可扩展的微服务设计</font>

### <font style="color:rgb(51, 51, 51);">用户提交判题流程</font>
<!-- 这是一张图片，ocr 内容为： -->
![](https://cdn.nlark.com/yuque/0/2026/jpeg/57681281/1773476055744-cfd3832e-1abd-40e6-993f-7de9cac4e141.jpeg)

### <font style="color:rgb(51, 51, 51);">系统架构流程</font>
<!-- 这是一张图片，ocr 内容为： -->
![](https://cdn.nlark.com/yuque/0/2026/jpeg/57681281/1773478738759-3efbb80e-ee9c-4149-9283-8209ce8633a9.jpeg)

## <font style="color:rgb(51, 51, 51);">技术栈</font>
### <font style="color:rgb(51, 51, 51);">核心框架</font>
+ <font style="color:rgb(51, 51, 51);">后端框架：Spring Boot 2.7.2</font>
+ <font style="color:rgb(51, 51, 51);">微服务框架：Spring Cloud Alibaba 2021.0.5.0</font>
+ <font style="color:rgb(51, 51, 51);">服务注册与发现：Nacos</font>
+ <font style="color:rgb(51, 51, 51);">服务熔断：Sentinel</font>
+ <font style="color:rgb(51, 51, 51);">远程调用：OpenFeign</font>
+ <font style="color:rgb(51, 51, 51);">负载均衡：Spring Cloud LoadBalancer</font>
+ <font style="color:rgb(51, 51, 51);">API 文档：Knife4j（基于 Swagger）</font>

### <font style="color:rgb(51, 51, 51);">数据存储</font>
+ <font style="color:rgb(51, 51, 51);">关系型数据库：MySQL 8.0</font>
+ <font style="color:rgb(51, 51, 51);">ORM 框架：MyBatis-Plus 3.5.2</font>
+ <font style="color:rgb(51, 51, 51);">缓存：Redis</font>

### <font style="color:rgb(51, 51, 51);">中间件</font>
+ <font style="color:rgb(51, 51, 51);">消息队列：RabbitMQ</font>
+ <font style="color:rgb(51, 51, 51);">容器化：Docker</font>

### <font style="color:rgb(51, 51, 51);">开发工具</font>
+ <font style="color:rgb(51, 51, 51);">构建工具：Maven</font>
+ <font style="color:rgb(51, 51, 51);">代码生成：MyBatis-Plus Generator</font>
+ <font style="color:rgb(51, 51, 51);">工具库：Hutool、Commons Lang3</font>
+ <font style="color:rgb(51, 51, 51);">开发工具：IntelliJ IDEA</font>

## <font style="color:rgb(51, 51, 51);">项目亮点</font>
### <font style="color:rgb(51, 51, 51);">架构设计</font>
1. <font style="color:rgb(51, 51, 51);">灵活沙箱架构：自主设计判题机模块，定义代码沙箱抽象接口，支持多种实现（本地/远程/第三方沙箱）</font>
2. <font style="color:rgb(51, 51, 51);">静态工厂模式：通过静态工厂模式 + Spring 配置化实现多种代码沙箱的灵活调用</font>
3. <font style="color:rgb(51, 51, 51);">代理模式：对代码沙箱接口进行能力增强，统一实现调用前后的日志记录</font>
4. <font style="color:rgb(51, 51, 51);">策略模式：封装不同语言的判题算法，避免 if-else 逻辑，提高可维护性</font>

### <font style="color:rgb(51, 51, 51);">安全机制</font>
1. <font style="color:rgb(51, 51, 51);">Docker 容器隔离：使用 Docker 容器执行用户代码，与主机系统完全隔离</font>
2. <font style="color:rgb(51, 51, 51);">资源限制：限制 CPU、内存、磁盘使用，防止资源耗尽</font>
3. <font style="color:rgb(51, 51, 51);">网络隔离：禁止容器访问网络，防止恶意请求</font>
4. <font style="color:rgb(51, 51, 51);">文件系统保护：只读文件系统，防止系统文件被修改</font>
5. <font style="color:rgb(51, 51, 51);">执行时间限制：设置执行超时时间，防止无限循环</font>
6. <font style="color:rgb(51, 51, 51);">API 签名认证：防止恶意请求代码沙箱服务</font>

### <font style="color:rgb(51, 51, 51);">性能优化</font>
1. <font style="color:rgb(51, 51, 51);">异步判题：使用 RabbitMQ 实现异步判题，提升用户体验</font>
2. <font style="color:rgb(51, 51, 51);">微服务架构：服务拆分，独立部署，提高系统可用性</font>
3. <font style="color:rgb(51, 51, 51);">分布式 Session：使用 Redis 存储用户登录信息</font>
4. <font style="color:rgb(51, 51, 51);">网关聚合：统一接口管理和路由</font>

### <font style="color:rgb(51, 51, 51);">代码沙箱实现</font>
1. <font style="color:rgb(51, 51, 51);">Java 原生沙箱：使用 Java Runtime exec 方法实现代码编译执行</font>
2. <font style="color:rgb(51, 51, 51);">Docker 沙箱：使用 Docker Java 库创建容器隔离执行</font>
3. <font style="color:rgb(51, 51, 51);">安全管理器：自定义 Security Manager 进行权限控制</font>
4. <font style="color:rgb(51, 51, 51);">黑名单机制：使用字典树实现敏感操作限制</font>
5. <font style="color:rgb(51, 51, 51);">超时控制：守护线程 + Thread.sleep 实现进程超时中断</font>

### <font style="color:rgb(51, 51, 51);">开发实践</font>
1. <font style="color:rgb(51, 51, 51);">远程开发：使用 VMware + Ubuntu + Docker 搭建开发环境</font>
2. <font style="color:rgb(51, 51, 51);">依赖管理：Maven 父子模块配置，保证版本一致性</font>
3. <font style="color:rgb(51, 51, 51);">接口聚合：Knife4j Gateway 统一聚合各服务 Swagger 文档</font>
4. <font style="color:rgb(51, 51, 51);">跨域处理：自定义 CorsWebFilter 全局解决跨域问题</font>
5. <font style="color:rgb(51, 51, 51);">内部接口保护：统一 inner 前缀 + GlobalFilter 权限校验</font>

## 项目核心演示图
首页

<!-- 这是一张图片，ocr 内容为： -->
![](https://cdn.nlark.com/yuque/0/2026/png/57681281/1773645017296-8572b55d-afb4-400a-b316-36ad63259203.png)

题目提交页

<!-- 这是一张图片，ocr 内容为： -->
![](https://cdn.nlark.com/yuque/0/2026/png/57681281/1773645135819-29ed7b66-9a20-4567-881c-4bb813b6da8b.png)

做题页

<!-- 这是一张图片，ocr 内容为： -->
![](https://cdn.nlark.com/yuque/0/2026/png/57681281/1773645186074-9c9ff4d0-ba93-4cd4-a745-b8ee1751a327.png)

查看提交代码

<!-- 这是一张图片，ocr 内容为： -->
![](https://cdn.nlark.com/yuque/0/2026/png/57681281/1773645215576-00a5b83d-5826-436c-a32e-c52d22852bbb.png)

创建题目页

<!-- 这是一张图片，ocr 内容为： -->
![](https://cdn.nlark.com/yuque/0/2026/png/57681281/1773645277483-94fa333c-4abe-41c7-bc58-4279505ab3ad.png)



## <font style="color:rgb(51, 51, 51);">核心模块</font>
### <font style="color:rgb(51, 51, 51);">代码沙箱模块</font>
<font style="color:rgb(51, 51, 51);">负责安全执行用户提交的代码，主要实现：</font>

+ <font style="color:rgb(51, 51, 51);">JavaDockerCodeSandbox：基于 Docker 容器执行 Java 代码</font>
+ <font style="color:rgb(51, 51, 51);">JavaNativeCodeSandbox：本地执行 Java 代码（开发环境）</font>

<font style="color:rgb(51, 51, 51);">执行流程：</font>

1. <font style="color:rgb(51, 51, 51);">接收执行代码请求</font>
2. <font style="color:rgb(51, 51, 51);">创建临时目录和文件</font>
3. <font style="color:rgb(51, 51, 51);">编译代码</font>
4. <font style="color:rgb(51, 51, 51);">执行代码（带资源限制）</font>
5. <font style="color:rgb(51, 51, 51);">收集执行结果</font>
6. <font style="color:rgb(51, 51, 51);">清理临时文件和容器</font>

### <font style="color:rgb(51, 51, 51);">判题服务模块</font>
<font style="color:rgb(51, 51, 51);">负责对用户提交的代码进行评估：</font>

+ <font style="color:rgb(51, 51, 51);">JudgeServiceImpl：判题服务实现</font>
+ <font style="color:rgb(51, 51, 51);">JudgeManager：判题管理器，根据语言选择策略</font>
+ <font style="color:rgb(51, 51, 51);">JudgeStrategy：判题策略接口</font>

<font style="color:rgb(51, 51, 51);">判题流程：</font>

1. <font style="color:rgb(51, 51, 51);">获取题目提交信息</font>
2. <font style="color:rgb(51, 51, 51);">调用代码沙箱执行代码</font>
3. <font style="color:rgb(51, 51, 51);">根据执行结果和题目要求进行判定</font>
4. <font style="color:rgb(51, 51, 51);">更新提交状态和判题结果</font>

### <font style="color:rgb(51, 51, 51);">题目服务模块</font>
<font style="color:rgb(51, 51, 51);">负责题目管理和提交管理：</font>

+ <font style="color:rgb(51, 51, 51);">QuestionService：题目管理服务</font>
+ <font style="color:rgb(51, 51, 51);">QuestionSubmitService：提交管理服务</font>

<font style="color:rgb(51, 51, 51);">功能：</font>

+ <font style="color:rgb(51, 51, 51);">题目增删改查</font>
+ <font style="color:rgb(51, 51, 51);">代码提交</font>
+ <font style="color:rgb(51, 51, 51);">提交历史查询</font>
+ <font style="color:rgb(51, 51, 51);">提交结果统计</font>

### <font style="color:rgb(51, 51, 51);">用户服务模块</font>
<font style="color:rgb(51, 51, 51);">负责用户管理和权限控制：</font>

+ <font style="color:rgb(51, 51, 51);">UserService：用户管理服务</font>

<font style="color:rgb(51, 51, 51);">功能：</font>

+ <font style="color:rgb(51, 51, 51);">用户注册登录</font>
+ <font style="color:rgb(51, 51, 51);">个人信息管理</font>
+ <font style="color:rgb(51, 51, 51);">权限控制</font>

## <font style="color:rgb(51, 51, 51);">安全机制</font>
### <font style="color:rgb(51, 51, 51);">代码沙箱安全</font>
1. <font style="color:rgb(51, 51, 51);">Docker 容器隔离：与主机系统隔离执行</font>
2. <font style="color:rgb(51, 51, 51);">资源限制：CPU、内存、磁盘使用限制</font>
3. <font style="color:rgb(51, 51, 51);">网络隔离：禁止容器访问外部网络</font>
4. <font style="color:rgb(51, 51, 51);">文件系统限制：只读文件系统</font>
5. <font style="color:rgb(51, 51, 51);">执行时间限制：防止无限循环和死锁</font>

### <font style="color:rgb(51, 51, 51);">系统安全</font>
1. <font style="color:rgb(51, 51, 51);">网关认证：统一认证过滤</font>
2. <font style="color:rgb(51, 51, 51);">API 签名：防止恶意调用</font>
3. <font style="color:rgb(51, 51, 51);">权限控制：基于角色的访问控制</font>
4. <font style="color:rgb(51, 51, 51);">输入验证：防止注入攻击</font>

