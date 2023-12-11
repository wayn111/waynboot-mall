# waynboot-mall

| 分支名称                                                                           | Spring Boot 版本 | JDK 版本 |
|--------------------------------------------------------------------------------|----------------|--------|
| [master](https://github.com/wayn111/waynboot-mall)                             | 3.1.4          | 17     |
| [springboot-2.7](https://github.com/wayn111/waynboot-mall/tree/springboot-2.7) | 2.7            | 1.8    | 

 
---

- [简介](#简介)
- [功能设计](#功能设计)
- [系统设计](#系统设计)
- [技术选型](#技术选型)
- [文件目录](#文件目录)
- [todo](#todo)
- [本地开发](#本地开发)
- [在线体验](#在线体验)
- [演示截图](#演示截图)
- [文件目录](#文件目录)
- [感谢](#感谢)

---

# 简介

🔥waynboot-mall 是一套全部开源的微商城项目，包含三个项目：**运营后台、H5 商城和后端接口**。实现了一套完整的商城业务，有首页展示、商品分类、商品详情、sku
详情、商品搜索、加入购物车、结算下单、支付宝/微信支付、订单列表、商品评论等一系列功能🔥。

商城前后台项目源码全部开源，绝无套路。技术上基于最新得 Spring Boot3.1、Mybatis Plus、Spring Security，整合了 Mysql、Redis、RabbitMQ、ElasticSearch 等常用中间件，根据博主多年线上项目实战经验总结开发而来不断优化、完善。

对于初学者而言 waynboot-mall 项目是非常易于部署的，根据 readme 中的本地开发指南就能成功启动项目。

并且提供了 docker-compose 一键部署脚本，只需要十多分钟就能启动商城前后台所有服务。

- 后端接口项目 https://github.com/wayn111/waynboot-mall
- 前端H5商城项目 https://github.com/wayn111/waynboot-mobile
- 前端运管后台项目 https://github.com/wayn111/waynboot-admin

> 如果有任何使用问题，欢迎提交Issue或加wx告知，方便互相交流反馈～ 💘。最后，喜欢的话麻烦给我个star

# 功能设计

![功能设计](images/功能设计.png)

# 系统设计

![系统设计](images/系统设计.png)

关注我的公众号：waynblog，每周更新技术文章、项目实战。回复关键字：

- **加群**：加群交流，探讨技术问题。
- **演示账号**：获得 waynboot-mall 商城后台演示账号。
- **开源项目**：获取我写的三个开源项目，包含PC、H5商城、后台权限管理系统等。
- **加微信**：联系我。

<img src="images/wx-mp-code.png" width = "100" />

---

# 技术选型

|    | 系统组件              | 采用技术                        | 官网                                                                                         |
|----|-------------------|-----------------------------|--------------------------------------------------------------------------------------------|
| 1  | 基础框架              | Spring Boot                 | https://spring.io/projects/spring-boot                                                     |
| 2  | ORM 框架            | MyBatis-Plus                | https://baomidou.com                                                                       |
| 3  | 工具类库              | hutool                      | https://hutool.cn                                                                          |
| 4  | 流量网关              | Nginx                       | http://nginx.org/en/index.html                                                             |
| 5  | 访问控制              | Spring Security             | https://spring.io/projects/spring-security                                                 |
| 6  | 日志记录              | Nginx                       | https://logback.qos.ch/                                                                    |
| 7  | 验证码               | easy-captcha                | https://github.com/ele-admin/EasyCaptcha                                                   |
| 8  | 数据库连接池            | HikariCP                    | https://github.com/brettwooldridge/HikariCP                                                |
| 9  | Redis 客户端         | Lettuce                     | https://lettuce.io                                                                         |
| 10 | Elasticsearch 客户端 | Java High Level REST Client | https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html |
| 11 | 消息队列              | RabbitMQ                    | https://www.rabbitmq.com                                                                   |
| 12 | 定时任务              | xxl-job                     | https://www.xuxueli.com/xxl-job                                                            |
| 13 | 服务监控              | spring-boot-admin           | https://docs.spring-boot-admin.com/current/getting-started.html                            |

---

# todo

- [x] 订单详情页面
- [x] 完善支付功能
- [ ] 商城资讯流
- [ ] 联系客服

---

# 本地开发

由于本项目图片压缩包超过 100m 不能在 github 上传，
提供百度网盘地址链接：链接：https://pan.baidu.com/s/1hxaRKqBjv4BRRmR2Ck1inQ ， 提取码：hoep

```
# 1. 克隆项目
git clone git@github.com:wayn111/waynboot-mall.git

# 2. 导入项目依赖
将 waynboot-mall 目录用 idea 打开，导入 maven 依赖

# 3. 安装 Mysql8.0+、Redis3.0+、RabbitMQ3.0+（含延迟消息插件）、ElasticSearch7.0+（含分词、拼英插件）到本地

# 4. 导入 sql 文件
在项目根目录下，找到 `wayn_shop_*.sql` 文件，新建 mysql 数据库 wayn_shop，导入其中

# 5. 项目图片部署
使用百度网盘，下载商城图片压缩包，将 zip 中所有图片解压缩部署到 D:/waynshop/webp 目录下

# 6. 修改Mysql、Redis、RabbitMQ、Elasticsearch连接配置
修改`application-dev.yml`以及`application.yml`文件中数据连接配置相关信息

# 7. 启动项目
后台api：
    进入waynboot-admin-api子项目，找到AdminApplication文件，右键`run AdminApplication`，启动后台项目
h5商城api:
    进入waynboot-mobile-api子项目，找到MobileApplication文件，右键`run MobileApplication`，启动h5商城项目
消费者api：
    进入waynboot-message-consumer子项目，找到MessageApplication文件，右键`run MessageApplication`，启动消费者项目
    
# 8. 启动商城H5项目
请查看商城H5前端项目https://github.com/wayn111/waynboot-mobile，readme文档，进行本地启动

# 9. 启动商城后管项目
请查看商城后管前端项目https://github.com/wayn111/waynboot-admin，readme文档，进行本地启动
```

---

# 在线体验

前台

- 使用邮箱 + 手机号注册商城用户
- 使用手机号 + 密码登陆

演示地址以及账号：关注我的公众号【waynblog】，发送 演示账号

---

# 服务器部署

对于想要自己部署这个项目的同学又没有开发资源，可以关注公众号【waynblog】 发送 **加微信**，提供有偿帮助。

# 咨询指南

商城咨询时请考虑我的时间成本，虽然我是乐于帮助新手解决问题。

但是某些人不仅白嫖咨询大量问题，消耗我的时间成本。而且咨询态度就像是我的客户一样，咨询完了一句谢谢也不会说。

所以如有咨询大量问题，请先付出金钱成本😜。

# 演示截图

<table>
    <tr>
        <td>商城首页<img src="https://files.mdnice.com/user/40549/914d6ec3-7456-49fc-bda5-24256bdec9ee.png"/></td>
        <td>更多商品<img src="https://files.mdnice.com/user/40549/dc5160be-77ed-4082-99d0-94dc529f4093.png"/></td>
    </tr>
    <tr>
        <td>商城搜索<img src="https://files.mdnice.com/user/40549/f9a80d94-3e99-477d-a7e5-deb7269092ec.png"/></td>
        <td>金刚位跳转<img src="https://files.mdnice.com/user/40549/a7f61d46-24e2-4197-8684-f741cb584bf9.png"/></td>
    </tr>
    <tr>
        <td>商品详情<img src="https://files.mdnice.com/user/40549/d6da447b-cc18-4175-a879-482a14ba7892.png"/></td>
        <td>商品分类<img src="https://files.mdnice.com/user/40549/b0b57fee-87c8-49e1-b870-afcf98408bde.png"/></td>
    </tr>
    <tr>
        <td>商品sku选择<img src="https://files.mdnice.com/user/40549/2ea549b3-842c-4949-ba97-56c954206b0e.png"/></td>
        <td>购物车查看<img src="https://files.mdnice.com/user/40549/0e5e0317-5caf-41d9-9171-831bc13decda.png"/></td>
    </tr>
    <tr>
        <td>确认下单<img src="https://files.mdnice.com/user/40549/aa089cc8-301f-4b39-9caa-9eff9d5bb407.png"/></td>
        <td>选择支付方式<img src="https://files.mdnice.com/user/40549/38aed1a1-7bfc-422c-a080-a919af91c306.png"/></td>
    </tr>
    <tr>
        <td>下单成功<img src="https://files.mdnice.com/user/40549/f1e6bd71-8f70-442d-852d-803937b9d703.png"/></td>
        <td>订单列表<img src="https://files.mdnice.com/user/40549/6c171780-2c94-48cf-bfe8-b4369745a567.png"/></td>
    </tr>
    <tr>
        <td>商品评论<img src="https://files.mdnice.com/user/40549/590615c0-1167-44e9-9b8f-0fc7e11db654.png"/></td>
        <td>我的页面<img src="https://files.mdnice.com/user/40549/fc26c2d0-6e71-4e76-aeff-8886f15be092.png"/></td>
    </tr>
    <tr>
        <td>登录<img src="https://files.mdnice.com/user/40549/57bb81a0-92af-4db4-93c8-e83d87cc1a73.png"/></td>
        <td>注册<img src="https://files.mdnice.com/user/40549/09c7c705-5839-4364-b908-e4231d31a8cc.png"/></td>
    </tr>
</table>

# 文件目录

```
|-- db-init                        // 数据库初始化脚本
|-- waynboot-monitor               // 监控模块
|-- waynboot-admin-api             // 运营后台api模块，提供后台项目api接口
|   |-- controller                 // 后台接口
|   |-- framework                  // 后台配置相关
|-- waynboot-common                // 通用模块，包含项目核心基础类
|   |-- annotation                
|   |-- base      
|   |-- config
|   |-- constant
|   |-- core
|   |-- enums
|   |-- exception
|   |-- task
|   |-- util
|-- waynboot-data                  // 数据模块，通用中间件数据访问
|   |-- waynboot-data-redis        // redis访问配置模块
|   |-- waynboot-data-elastic      // elastic访问配置模块
|-- waynboot-message-consumer      // 消费者模块，处理订单消息和邮件消息
|-- waynboot-message-core          // 消费者核心模块，队列、交换机配置
|-- waynboot-mobile-api            // H5商城api模块，提供H5商城api接口
|   |-- controller                 // 前台接口
|   |-- framework                  // 前台配置相关
|-- pom.xml                        // maven父项目依赖，定义子项目依赖版本
|-- ...
```

# 感谢

- [panda-mall](https://github.com/Ewall1106/vue-h5-template)
- [litemall](https://github.com/linlinjava/litemall)
- [vant-ui](https://github.com/youzan/vant)

# 捐助

<img src="./images/捐助.jpg" width="260" alt="如果这个项目对你有所帮助，不如请作者喝杯咖啡吧">
