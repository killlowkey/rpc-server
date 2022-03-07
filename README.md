# rpc-server

rpc-server 是一个基于 netty rpc 框架



## 特性

* 方法调用
  * **ASM：通过 ASM 生成字节码，避免了方法调用时反射开销**
  * Reflect
  * Method Handle
* 客户端接口动态代理
* 通讯加密：SSL
* 注解配置扫描
* 负载均衡
  * 轮训
  * 随机
* 接口限流
* 服务别名：一个服务采用多个名称



## 如何使用

1. 定义接口，并使用 @RpcClient 标记服务所处的 group

   ```java
   @RpcClient("service/")
   public interface PersonService {
       String hello();
       String say(String name);
       int age();
   }
   ```

2. 实现接口

   ```java
   @RpcService("service/")
   public class PersonServiceImpl implements PersonService {
       
       public String hello() {
           return "hello world";
       }
   
       public String say(String name) {
           return "hello " + name;
       }
   
       public int age() {
           return 10;
       }
   
   }
   ```

3. 启动服务端

   ```java
   RpcServer rpcServer = new RpcServerBuilder()
       .invokeType(InvokeType.ASM)
       // 注册服务，还提供扫描包方式注册服务
       .registerComponent(PersonServiceImpl.class)
       .bind(8989)
       .build();
   
   // 启动服务
   rpcServer.start();
   ```

4. 启动客户端，并进行调用

   ```java
   InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8989);
   RpcClientProxy rpcClientProxy = new RpcClientProxy(address);
   // 对 PersonService 接口动态代理
   PersonService clientComponent = rpcClientProxy.createProxy(PersonService.class);
   
   // 方法调用
   assertEquals("hello world", clientComponent.hello());
   assertEquals("hello tom", clientComponent.say("tom"));
   assertEquals(10, clientComponent.age());
   ```

   
