# SSL example



## 生成证书

> 来源：https://www.cnblogs.com/developer-ios/p/11417730.html

1. 生成Netty服务端私钥和证书仓库

   ```shell
   keytool -genkey -alias securechat -keysize 2048 -validity 365 -keyalg RSA -dname "CN=localhost" -keypass sNetty -storepass sNetty -keystore sChat.jks
   ```

   * -keysize 2048 密钥长度2048位（这个长度的密钥目前可认为无法被暴力破解）
   * -validity 365 证书有效期365天
   * -keyalg RSA 使用RSA非对称加密算法
   * -dname "CN=localhost" 设置Common Name为localhost
   * -keypass sNetty密钥的访问密码为sNetty
   * -storepass sNetty密钥库的访问密码为sNetty（其实这两个密码也可以设置一样，通常都设置一样，方便记）
   * -keystore sChat.jks 指定生成的密钥库文件为sChata.jks

2. 生成Netty服务端自签名证书

   ```shell
   keytool -export -alias securechat -keystore sChat.jks -storepass sNetty -file sChat.cer
   ```

3. 生成客户端的密钥对和证书仓库，用于将服务端的证书保存到客户端的授信证书仓库

   ```shell
   keytool -genkey -alias smcc -keysize 2048 -validity 365  -keyalg RSA -dname "CN=localhost" -keypass sNetty  -storepass sNetty -keystore cChat.jks
   ```

4. 将Netty服务端证书导入到客户端的证书仓库

   ```shell
   keytool -import -trustcacerts -alias securechat -file sChat.cer -storepass sNetty -keystore cChat.jks
   ```

   > 如果你只做单向认证，则到此就可以结束了，如果是双向认证，则还需继续往下走

5. 生成客户端自签名证书

   ```shell
   keytool -export -alias smcc -keystore cChat.jks -storepass sNetty -file cChat.cer
   ```

6. 将客户端的自签名证书导入到服务端的信任证书仓库

   ```shell
   keytool -import -trustcacerts -alias smcc -file cChat.cer -storepass sNetty -keystore sChat.jks
   ```

   到这里，证书就生成完毕了，我们就可以得到两个jks文件，一个是服务端的sChat.jks  ，一个是客户端的cChat.jks 



## 启动

按上以上流程来生成服务端和客户端的整数，如果不需要双向加密的话，则不需要生成客户端证书。将生成的证书拷贝到 resources 目录下，在通过 RpcServerBuilder 构建 RpcServer 时，在 enableSsl 方法中传入 jks 的 File 对象、密码，第三个参数是否开启双向认证。此时服务端的就创建好了。

> **只适合 storepass  与 keypass 密码都相同的场景**

```java
File file = new ClassPathResource("sChat.jks").getFile();
RpcServer rpcServer = new RpcServerBuilder()
        .bind(8989)
        .registerComponent(RpcServerComponent.class)
        .enableSSL(file, "passworld", true)
        .build();
rpcServer.start();
```

客户端与服务端流程类似，在 enableSsl 方法中传入证书和密码即可。

```java
InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8989);

File file = new ClassPathResource("cChat.jks").getFile();
RpcClientComponent clientComponent = new RpcClientProxy(address)
    .enableSsl(file, "passworld")
    .createProxy(RpcClientComponent.class);
```

