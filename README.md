> # Netty集成ProtoBuf开发

## Netty简介

Netty是业界最流行的NIO框架之，它的健壮性、功能、性能、可定制性和可扩展性在同类框架中都是首屈-指的，它已经得到成百上千的商用项目验证，例如Hadoop的RPC框架Avro就使用了Netty作为底层通信框架，其他还有业界主流的RPC框架，也使
用Netty来构建商性能的异步通信能力。通过对Netty的分析，我们将它的优点总结如下:
	1、API 使用简单，开发门槛低，功能强大，预置了多种编解码功能，支持多种主流协议
	2、定制能力强， 可以通过ChannelHandler对通信框架进行灵活地扩展
	3、性能高，通过与其他业界主流的NIO框架对比，Netty的综合性能最优
	4、成熟、稳定，Netty修复了已经发现的所有JDK NIO BUG, 业务开发人员不需要
		再为NIO的BUG而烦恼:
	5、社区活跃，版本迭代周期短，发现的BUG可以被及时修复，同时，更多的新功
		能会加入;
	6、经历 了大规模的商业应用考验，质量得到验证。Netty 在互联网、大数据、网络
		游戏、企业应用、电信软件等众多行业已经得到了成功商用，证明它已经完全能
		够满足不同行业的商业应用了.
正是因为这些优点，Netty 逐渐成为了Java NIO编程的首选框架。

## ProtoBuf简介

protocolbuffer(以下简称PB)是google 的一种数据交换的格式，它独立于语言，独立于平台。google 提供了多种语言的实现：java、c#、c++、go 和python，每一种实现都包含了相应语言的编译器以及库文件。由于它是一种二进制的格式，比使用 xml进行数据交换快许多。可以把它用于分布式应用之间的数据通信或者异构环境下的数据交换。作为一种效率和兼容性都很优秀的二进制数据传输格式，可以用于诸如网络传输、配置文件、数据存储等诸多领域。

官方地址：https://github.com/google/protobuf

## 开发步骤

#### 使用protobuf生成序列化文件

1、下载protobuf执行文件，文件地址：https://github.com/protocolbuffers/protobuf/releases/download/v4.0.0-rc2/protoc-4.0.0-rc-2-win64.zip

2、在D盘新建文件夹：protobuf

3、进入下载文件bin文件夹拷贝protoc.exe到D盘的protobuf

4、新建文件User.proto

```
syntax = "proto3";
//生成的包名，此处根据实际来修改
option java_package = "com.fy.protobuf";
//类名
option java_outer_classname="UserInfo";

message UserMsg{
  int32 id=1;
  string name=2;
  int32 age=3;
  int32 state=4;
}
```

:one:**proto**文件和生成的**Java**文件名称不能一致!

:two:文件编码为ANSI

5、cmd窗口执行命令：protoc.exe --java_out=D:\protobuf User.proto

6、将生成的文件拷贝到项目中

#### 代码示例

```
// 按照定义的数据结构，创建一个对象
UserInfo.UserMsg.Builder userInfo = UserInfo.UserMsg.newBuilder();
userInfo.setId(1);
userInfo.setName("fangyan");
userInfo.setAge(18);
UserInfo.UserMsg userMsg = userInfo.build();
// 将数据写到输出流
ByteArrayOutputStream output = new ByteArrayOutputStream();
userMsg.writeTo(output);
// 将数据序列化后发送
byte[] byteArray = output.toByteArray();
// 接收到流并读取
ByteArrayInputStream input = new ByteArrayInputStream(byteArray);
// 反序列化
UserInfo.UserMsg userInfo2 = UserInfo.UserMsg.parseFrom(input);
System.out.println("id:" + userInfo2.getId());
System.out.println("name:" + userInfo2.getName());
System.out.println("age:" + userInfo2.getAge());

输出：
id:1
name:fangyan
age:18
```

### 开始Coding~

1、新建maven项目，引入依赖

```
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.1.51.Final</version>
</dependency>
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>3.11.0</version>
</dependency>
```

2、创建服务端启动代码

```
public class EchoServer {
    public void bind(int port) {
        //我们创建了两个NioEventLoopGroup实例。NioEventLoopGroup是个线程组，
        //它包含了一组NIO线程，专门用于网络事件的处理，实际上它们就是Reactor线程组。
        //这里创建两个的原因是一个用于服务端接受客户端的连接,另一个用于进行SocketChannel的网络读写。
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //创建ServerBootstrap对象，它是Netty用于启动NIO服务端的辅助启动类，目的是降低服务端的开发复杂度。
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap
                    //调用ServerBootstrap的group方法，将两个NIO线程组当作入参传递到ServerBootstrap中。
                    .group(bossGroup, workerGroup)
                    //接着设置创建的Channel为NioServerSocketChannel,它的功能对应于JDK NIO类库中的ServerSocketChannel类。
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    //最后绑定I/O事件的处理类ChannelInitializer，它的作用类似于Reactor模式中的Handler类，
                    //主要用于处理网络I/O事件，例如记录日志、对消息进行编解码等。
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    //创建消息解码器，获取消息长度
                                    .addLast(new ProtobufVarint32FrameDecoder())
                                    //指定消息类型
                                    .addLast(new ProtobufDecoder(UserInfo.UserMsg.getDefaultInstance()))
                                    //消息头置长度
                                    .addLast(new ProtobufVarint32LengthFieldPrepender())
                                    //创建消息编码器
                                    .addLast(new ProtobufEncoder())
                                    //消息的处理
                                    .addLast(new EchoServerHandler());
                        }
                    });
            //服务端启动辅助类配置完成之后，调用它的bind 方法绑定监听端口，
            //随后，调用它的同步阻塞方法sync等待绑定操作完成。
            //完成之后Netty会返回一个ChannelFuture, 它的功能类似于JDK的java.util.concurrent.Future，主要用于异步操作的通知回调。
            //使用f.channel.closeFuture().syncO方法进行阻塞，等待服务端链路关闭之后main函数才退出。
            ChannelFuture sync = bootstrap.bind(port).sync();

            sync.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //调用NIO线程组的shutdownGracefully 进行优雅退出，它会释放跟shutdownGracefully相关联的资源。
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
```

tips:rotating_light:TCP协议位于传输层，传输数据为流，需要处理消息的粘包与拆包，粘包与拆包有四种解决方案：

(1)定长：消息长度固定，累计读取到长度总和为定长LEN的报文后，就认为读取到了一个完整的消息:将计数器置位，重新开始读取下一个数据报

(2)消息头定长：通过在消息头中定义长度字段来标识消息的总长度。

(3)回车符：将回车换行符作为消息结束符，例如FTP协议，这种方式在文本协议中应用比较广泛:

(4)分隔符：将特殊的分隔符作为消息的结束标志，回车换行符就是一种特殊的结束分隔符

我们这里采用的方案为消息头定长。

3、创建服务端消息处理代码

```
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    int count;

	//当通道激活时
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接的客户端地址:" + ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

	//当通道读到数据时
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
        	//拿到解码后的消息
            UserInfo.UserMsg userMsg = (UserInfo.UserMsg) msg;
            System.out.println("Receive:[" + userMsg.toString() + "],count:[" + (++count) + "]");
            UserInfo.UserMsg.Builder builder = userMsg.toBuilder().setState(1);
            ctx.writeAndFlush(builder);
        } finally {
            ReferenceCountUtil.release(msg);
        }


    }

	//当通道异常时
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```

4、创建客户端启动代码

```
public class Echoclient {
    public void bind(int port) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new ProtobufVarint32FrameDecoder())
                                    .addLast(new ProtobufDecoder(UserInfo.UserMsg.getDefaultInstance()))
                                    .addLast(new ProtobufVarint32LengthFieldPrepender())
                                    .addLast(new ProtobufEncoder())
                                    .addLast(new EchoClientHandler());
                        }
                    });
            ChannelFuture f = b.connect("127.0.0.1", port).sync();

            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
```

客户端启动代码与服务端相似

5、创建客户端消息处理

```
public class EchoClientHandler extends ChannelInboundHandlerAdapter {
    int count;
    String echo_req = "Hi,fangyan,welcome to netty";
    UserInfo.UserMsg.Builder builder = UserInfo.UserMsg.newBuilder();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (int i = 1; i <= 10; i++) {
            UserInfo.UserMsg fangyan = builder.setId(1).setAge(i).setName("fangyan" + i).build();
            ctx.writeAndFlush(fangyan);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            UserInfo.UserMsg userMsg = (UserInfo.UserMsg) msg;
            System.out.println("Server:[" + userMsg.toString() + "]count:[" + (++count) + "]");
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```

这里的逻辑是连接成功后发送五条消息，服务端修改五条消息状态并返回。

6、启动服务端

7、启动客户端

服务端控制台打印：

```
连接的客户端地址:/127.0.0.1:50411
Receive:[id: 1
name: "fangyan1"
age: 1
],count:[1]
Receive:[id: 1
name: "fangyan2"
age: 2
],count:[2]
Receive:[id: 1
name: "fangyan3"
age: 3
],count:[3]
Receive:[id: 1
name: "fangyan4"
age: 4
],count:[4]
Receive:[id: 1
name: "fangyan5"
age: 5
],count:[5]
```

客户端控制台打印：

```
Server:[id: 1
name: "fangyan1"
age: 1
state: 1
]count:[1]
Server:[id: 1
name: "fangyan2"
age: 2
state: 1
]count:[2]
Server:[id: 1
name: "fangyan3"
age: 3
state: 1
]count:[3]
Server:[id: 1
name: "fangyan4"
age: 4
state: 1
]count:[4]
Server:[id: 1
name: "fangyan5"
age: 5
state: 1
]count:[5]
```

可以看到服务端收到消息后修改状态字段并返回成功啦。
