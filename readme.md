## waynboot-mall项目

waynboot-mall是一套全部开源的微商城项目，包含一个运营后台、h5商城和后台接口。
实现了一个商城所需的首页展示、商品分类、商品详情、sku详情、商品搜索、加入购物车、结算下单、订单状态流转、商品评论等一系列功能。
技术上基于Springboot2.0，整合了Redis、RabbitMQ、ElasticSearch等常用中间件，
贴近生产环境实际经验开发而来不断完善、优化、改进中。

[后台接口项目](https://github.com/wayn111/waynboot-mall)  
[运营后台项目](https://github.com/wayn111/waynboot-admin)  
[h5商城项目](https://github.com/wayn111/waynboot-mobile)

## waynboot-mall接口项目

1.  商城接口代码清晰、注释完善、模块拆分合理
2.  使用Spring-Security进行访问权限控制
3.  使用jwt进行接口授权验证 
4.  ORM层使用Mybatis Plus提升开发效率
5.  添加全局异常处理器，统一异常处理
6.  添加https配置代码，支持https访问
7.  集成七牛云存储配置，上传文件至七牛 
8.  集成常用邮箱配置，方便发送邮件
9.  集成druid连接池，进行sql监控
10. 集成swagger，管理接口文档 
11. 添加策略模式使用示例，优化首页金刚区跳转逻辑
12. 拆分出通用的数据访问模块，统一redis & elastic配置与访问
13. 使用elasticsearch-rest-high-level-client客户端对elasticsearch进行操作
14. 支持商品数据同步elasticsearch操作以及elasticsearch商品搜索
15. RabbitMQ生产者发送消息采用异步confirm模式，消费者消费消息时需手动确认
16. ...

## 商城难点整理
#### 1. 下单流程，库存扣减操作是在下单操作扣减还是在支付成功时扣减？（ps：扣减库存使用乐观锁机制 `where goods_num - num >= 0`）
1. 下单时扣减，这个方案属于实时扣减，当有大量下单请求时，由于订单数小于请求数，会发生下单失败，但是无法防止短时间大量恶意请求占用库存，
造成普通用户无法下单
2. 支付成功扣减，这个方案可以预防恶意请求占用库存，但是会存在多个请求同时下单后，在支付回调中扣减库存失败，导致订单还是下单失败并且还要退还订单金额（这种请求就是订单数超过了库存数，无法发货，影响用户体验）
3. 还是下单时扣减，但是对于未支付订单设置一个超时过期机制，比如下单时库存减一，生成订单后，对于未在15分钟内完成支付的订单，
自动取消超期未支付订单并将库存加一，该方案基本满足了大部分使用场景
4. 针对大流量下单场景，比如一分钟内五十万次下单请求，可以通过设置虚拟库存的方式减少下单接口对数据库的访问。具体来说就是把商品实际库存保存到redis中，
下单时配合lua脚本原子的get和decr商品库存数量（这一步就拦截了大部分请求），执行成功后在扣减实际库存

#### 2. 首页商品展示接口利用多线程技术进行查询优化，将多个sql语句的排队查询变成异步查询，接口时长只跟查询时长最大的sql查询挂钩
```java
# 1. 通过创建子线程继承Callable接口
Callable<List<Banner>> bannerCall = () -> iBannerService.list(new QueryWrapper<Banner>().eq("status", 0).orderByAsc("sort"));
# 2. 传入Callable的任务给FutureTask
FutureTask<List<Banner>> bannerTask = new FutureTask<>(bannerCall);
# 3. 放入线程池执行
threadPoolTaskExecutor.submit(bannerTask);
# 4. 最后可以在外部通过FutureTask的get方法异步获取执行结果 
List<Banner> list = bannerTask.get()
```
- todo

## 文件目录
```java
|-- waynboot-admin-api             // 运营后台api模块，提供后台项目api接口
|-- waynboot-common                // 通用模块，包含项目核心基础类
|-- waynboot-data                  // 数据模块，通用中间件数据访问
|   |-- waynboot-data-redis        // redis访问配置模块
|   |-- waynboot-data-elastic      // elastic访问配置模块
|-- waynboot-generator             // 代码生成模块
|-- waynboot-message-consumer      // 消费者模块，处理订单消息和邮件消息
|-- waynboot-message-core          // 消费者核心模块，队列、交换机配置
|-- waynboot-mobile-api            // h5商城api模块，提供h5商城api接口
|-- pom.xml                        // maven父项目依赖，定义子项目依赖版本
|-- ...
```
 
## 开发部署
```
# 1. 克隆项目
git clone git@github.com:wayn111/waynboot-mall.git

# 2. 导入项目依赖
将waynboot-mall目录用idea打开，导入maven依赖

# 3. 安装Mysql8.0+、Redis3.0+、RabbitMQ3.0+、ElasticSearch7.0+到本地

# 4. 导入sql文件
在项目根目录下，找到`wayn_shop_*.sql`文件，新建mysql数据库wayn_shop，导入其中

# 5. 修改Mysql、Redis、RabbitMQ、Elasticsearch连接配置
修改`application-dev.yml`以及`application.yml`文件中数据连接配置相关信息

# 6. 启动项目
后台api：
    进入waynboot-admin-api子项目，找到AdminApplication文件，右键`run AdminApplication`，启动后台项目
h5商城api:
    进入waynboot-mobile-api子项目，找到MobileApplication文件，右键`run MobileApplication`，启动h5商城项目
```

## 在线体验

- 注册一个账号
- 然后登陆

演示地址：http://www.wayn.ltd

## 演示图

<table>
    <tr>
        <td>商城登陆<img src="https://oscimg.oschina.net/oscnet/up-747e6a1f87aec9b4def2fcfea35c31be89e.png"/></td>
        <td>商城注册<img src="https://oscimg.oschina.net/oscnet/up-a7b38a69ee8c09664f418144e4fbd494b7a.png"/></td>
    </tr>
    <tr>
        <td>商城首页<img src="https://oscimg.oschina.net/oscnet/up-b2c9a914706af384815f686dfad9421cb45.png"/></td>
        <td>商城搜索<img src="https://oscimg.oschina.net/oscnet/up-f20a1e9e524b6edc117a10bd473179800a2.png"/></td>
    </tr>
    <tr>
        <td>搜索结果展示<img src="https://oscimg.oschina.net/oscnet/up-46798559cd1c1c835bbb33ea7cad43e303b.png"/></td>
        <td>金刚位跳转<img src="https://oscimg.oschina.net/oscnet/up-65739b9ba4fbf5b3e7f6995b6fd789e560a.png"/></td>
    </tr>
    <tr>
        <td>商品分类<img src="https://oscimg.oschina.net/oscnet/up-aa2db733e6bb8f69eca266ab80fbe5d47ed.png"/></td>
        <td>商品详情<img src="https://oscimg.oschina.net/oscnet/up-9543fd81e990293ff0dfb271d4fc8c649aa.png"/></td>
    </tr>
    <tr>
        <td>商品sku选择<img src="https://oscimg.oschina.net/oscnet/up-f8091881053964d500ae470454acd5d318b.png"/></td>
        <td>购物车查看<img src="https://oscimg.oschina.net/oscnet/up-96cfec4abf17eadda150babd7e1ad6ef656.png"/></td>
    </tr>
    <tr>
        <td>确认下单<img src="https://oscimg.oschina.net/oscnet/up-b9adf7939b993c9665e998e4dbc1e6780ce.png"/></td>
        <td>选择支付方式<img src="https://oscimg.oschina.net/oscnet/up-47d4dd70dd9a848e39e8578e9a2f5a09619.png"/></td>
    </tr>
    <tr>
        <td>商城我的页面<img src="https://oscimg.oschina.net/oscnet/up-6a9d0719f9263522e017240e08154a4d670.png"/></td>
        <td>我的订单列表<img src="https://oscimg.oschina.net/oscnet/up-989f6f1fb0a67abeb1f767516a4e6f55c66.png"/></td>
    </tr>
    <tr>
        <td>添加商品评论<img src="https://oscimg.oschina.net/oscnet/up-d4b26026685d15d1af529fdbdb176bf651f.png"/></td>
        <td>查看商品评论<img src="https://oscimg.oschina.net/oscnet/up-9460edfa3630e955643ff370d0979e7ab36.png"/></td>
    </tr>
    <tr>
        <td>后台登陆<img src="https://oscimg.oschina.net/oscnet/up-3cb800e60b6c99c5e0c2e2984b9ddffdcc0.png"/></td>
        <td>后台首页<img src="https://oscimg.oschina.net/oscnet/up-224e54dcb3eb4035736fcbea4626b85223d.png"/></td>
    </tr>
    <tr>
        <td>后台会员管理<img src="https://oscimg.oschina.net/oscnet/up-7fabe6227a9447653e06d2d8e4efb974195.png"/></td>
        <td>后台评论管理<img src="https://oscimg.oschina.net/oscnet/up-0192d4f41f9c061ed5329d9d6267ce3b8f3.png"/></td>
    </tr>
    <tr>
        <td>后台地址管理<img src="https://oscimg.oschina.net/oscnet/up-cec6bde100884182a9ad7aae10cb8c9d5b8.png"/></td>
        <td>后台添加商品<img src="https://oscimg.oschina.net/oscnet/up-535f8cab7365885095e3e511ec3762b0973.png"/></td>
    </tr>
    <tr>
        <td>后台商品管理<img src="https://oscimg.oschina.net/oscnet/up-b831bbf1f4fa00655d3b7e4368d4181a9b9.png"/></td>
        <td>后台banner管理<img src="https://oscimg.oschina.net/oscnet/up-e9edbb0bfcfd2c8687457573caf603e6275.png"/></td>
    </tr>
    <tr>
        <td>后台订单管理<img src="https://oscimg.oschina.net/oscnet/up-5447c1deaa5e1db63b81a9f90b5eed2fc07.png"/></td>
        <td>后台分类管理<img src="https://oscimg.oschina.net/oscnet/up-c769886a136ce0f25edcc4d647322d5d51f.png"/></td>
    </tr>
     <tr>
        <td>后台金刚区管理<img src="https://oscimg.oschina.net/oscnet/up-ef857d19b1f905afec239c673be24860a9a.png"/></td>
        <td>后台栏目管理<img src="https://oscimg.oschina.net/oscnet/up-e0effea68ad0073f30d5b5f6d9567a24b9e.png"/></td>
    </tr>
</table>


## waynboot-mall交流群

QQ群：<a target="_blank" href="https://qm.qq.com/cgi-bin/qm/qr?k=Mvf4HO4EhdXlfh0OLIq5I8wDIjRj6DlT&jump_from=webapi"><img border="0" src="https://pub.idqqimg.com/wpa/images/group.png" alt="waynboot-mall交流群" title="waynboot-mall交流群"></a>
有问题可以先提issue😁

## todo

- [ ] 支持多店铺
- [ ] 订单详情页面
- [ ] 秒杀专区
- [ ] 优惠卷使用
- [ ] 团购下单
- [ ] ...

## 感谢

- [panda-mall](https://github.com/Ewall1106/vue-h5-template)
- [litemall](https://github.com/linlinjava/litemall)
- [vant-ui](https://github.com/youzan/vant)
