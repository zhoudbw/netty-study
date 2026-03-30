#!/bin/bash

# Netty Protobuf Chat 启动脚本

echo "========================================"
echo "  Netty Protobuf Chat 启动脚本"
echo "========================================"
echo ""

# 检查是否已编译
if [ ! -d "target/classes" ]; then
    echo "项目未编译，正在编译..."
    mvn clean compile
    if [ $? -ne 0 ]; then
        echo "编译失败！"
        exit 1
    fi
    echo "编译成功！"
    echo ""
fi

# 获取用户选择
echo "请选择要启动的组件："
echo "1. 启动服务器"
echo "2. 启动客户端"
echo "3. 同时启动服务器和客户端"
echo ""
read -p "请输入选项 (1/2/3): " choice

case $choice in
    1)
        echo ""
        echo "正在启动服务器..."
        echo "服务器将在 127.0.0.1:8080 监听"
        echo "按 Ctrl+C 停止服务器"
        echo ""
        java -cp target/classes com.kernelcode.chat.server.ChatServer
        ;;
    2)
        echo ""
        echo "正在启动客户端..."
        echo "注意：启动后请先选择 '1' 进行登录！"
        echo ""
        java -cp target/classes com.kernelcode.chat.client.ChatClient
        ;;
    3)
        echo ""
        echo "正在启动服务器和客户端..."
        echo ""
        # 启动服务器（后台）
        java -cp target/classes com.kernelcode.chat.server.ChatServer &
        SERVER_PID=$!
        echo "服务器已启动 (PID: $SERVER_PID)"
        echo ""

        # 等待服务器启动
        sleep 2

        # 启动客户端
        echo "正在启动客户端..."
        echo "注意：启动后请先选择 '1' 进行登录！"
        echo ""
        java -cp target/classes com.kernelcode.chat.client.ChatClient

        # 客户端退出后，停止服务器
        echo ""
        echo "正在停止服务器..."
        kill $SERVER_PID
        echo "服务器已停止"
        ;;
    *)
        echo "无效的选项！"
        exit 1
        ;;
esac