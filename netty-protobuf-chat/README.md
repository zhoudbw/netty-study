# Netty Protobuf Chat 使用说明

## 项目简介
基于Netty和Protobuf的聊天系统，支持多种频道类型（世界频道、公会频道、队伍频道、私聊）。

## 启动步骤

### 1. 启动服务器
```bash
mvn exec:java -Dexec.mainClass="com.kernelcode.chat.server.ChatServer"
```
或直接运行编译后的类：
```bash
java -cp target/classes com.kernelcode.chat.server.ChatServer
```

服务器将在 127.0.0.1:8080 监听连接。

### 2. 启动客户端
```bash
mvn exec:java -Dexec.mainClass="com.kernelcode.chat.client.ChatClient"
```
或直接运行编译后的类：
```bash
java -cp target/classes com.kernelcode.chat.client.ChatClient
```

## 使用流程

### 重要：必须先登录！
客户端启动后会自动建立TCP连接，但**必须先选择"1"进行登录**才能使用其他功能。

### 登录步骤
1. 启动客户端后，输入 `1` 选择"连接服务器"
2. 按提示输入：
   - 玩家ID（例如：player001）
   - 玩家名称（例如：张三）
   - 玩家等级（例如：10）
   - Token（任意字符串，例如：abc123）
3. 登录成功后会自动加入世界频道

### 功能菜单
登录成功后可以使用以下功能：

```
1. 连接服务器    - 登录（仅首次需要）
2. 发送消息      - 向当前频道发送消息
3. 切换频道      - 切换到世界/公会/队伍频道
4. 查看在线玩家  - 显示当前频道的在线玩家
5. 查看历史消息  - 获取历史消息（未实现）
6. 私聊          - 向指定玩家发送私聊消息
7. 退出          - 退出客户端
```

### 频道说明
- **世界频道**：所有在线玩家都能看到消息
- **公会频道**：同一公会的玩家能看到消息（需要输入公会名称）
- **队伍频道**：同一队伍的玩家能看到消息（需要输入队伍名称）
- **私聊**：点对点私聊消息

## 测试场景

### 场景1：世界频道聊天
1. 启动服务器
2. 启动客户端A，登录（ID: player001, 名称: 张三）
3. 启动客户端B，登录（ID: player002, 名称: 李四）
4. 客户端A选择"2"，输入消息"大家好！"
5. 客户端B应该能看到张三的消息

### 场景2：私聊
1. 客户端A选择"6"，输入对方ID: player002，消息: "你好李四"
2. 客户端B应该能收到私聊消息

### 场景3：切换频道
1. 客户端A选择"3"，然后选择"2"（公会频道）
2. 输入公会名称: "我的公会"
3. 客户端A现在在"我的公会"频道，消息只有同频道的玩家能看到

## 已知问题

1. **用户体验**：客户端启动后需要手动登录，建议改进为启动时自动弹出登录界面
2. **历史消息**：查看历史消息功能服务器端未实现
3. **黑名单功能**：添加/移除黑名单功能未实现UI

## 技术栈

- **Netty**: 4.2.12.Final - 网络通信框架
- **Protobuf**: 4.34.1 - 序列化协议
- **Java**: 1.8
- **Maven**: 项目构建工具

## 项目结构

```
src/main/java/com/kernelcode/chat/
├── client/              # 客户端代码
│   ├── ChatClient.java  # 客户端主类
│   ├── ClientHandler.java
│   ├── PacketEncoder.java
│   ├── PacketDecoder.java
│   └── PlayerInfo.java
├── server/              # 服务端代码
│   ├── ChatServer.java  # 服务端主类
│   ├── ServerHandler.java
│   ├── ChannelManager.java
│   ├── ChannelGroup.java
│   ├── PlayerSession.java
│   ├── PacketEncoder.java
│   ├── PacketDecoder.java
│   └── PlayerInfo.java
├── protocol/            # Protobuf生成的代码
│   └── ChatProtocol.java
└── proto/               # Protobuf定义文件
    └── chat.proto
```

## 编译和运行

### 编译项目
```bash
mvn clean compile
```

### 打包项目
```bash
mvn package
```

### 运行服务器
```bash
java -cp target/netty-protobuf-chat-1.0-SNAPSHOT.jar:target/classes com.kernelcode.chat.server.ChatServer
```

### 运行客户端
```bash
java -cp target/netty-protobuf-chat-1.0-SNAPSHOT.jar:target/classes com.kernelcode.chat.client.ChatClient
```

## 注意事项

1. 客户端启动后会自动连接到 127.0.0.1:8080
2. 必须先登录才能使用聊天功能
3. 心跳机制每10秒发送一次，保持连接活跃
4. 退出客户端时会自动离开当前频道
5. 可以启动多个客户端进行测试

## 错误修复记录

详细的错误修复记录请查看：`src/main/logs/2026-03-30-err.log`