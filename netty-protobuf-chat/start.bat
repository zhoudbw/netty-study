@echo off
REM Netty Protobuf Chat 启动脚本 (Windows)

echo ========================================
echo   Netty Protobuf Chat 启动脚本
echo ========================================
echo.

REM 检查是否已编译
if not exist "target\classes" (
    echo 项目未编译，正在编译...
    call mvn clean compile
    if errorlevel 1 (
        echo 编译失败！
        exit /b 1
    )
    echo 编译成功！
    echo.
)

REM 获取用户选择
echo 请选择要启动的组件：
echo 1. 启动服务器
echo 2. 启动客户端
echo 3. 同时启动服务器和客户端
echo.
set /p choice="请输入选项 (1/2/3): "

if "%choice%"=="1" goto start_server
if "%choice%"=="2" goto start_client
if "%choice%"=="3" goto start_both

echo 无效的选项！
exit /b 1

:start_server
echo.
echo 正在启动服务器...
echo 服务器将在 127.0.0.1:8080 监听
echo 按 Ctrl+C 停止服务器
echo.
java -cp target\classes com.kernelcode.chat.server.ChatServer
goto end

:start_client
echo.
echo 正在启动客户端...
echo 注意：启动后请先选择 '1' 进行登录！
echo.
java -cp target\classes com.kernelcode.chat.client.ChatClient
goto end

:start_both
echo.
echo 正在启动服务器和客户端...
echo.
REM 启动服务器（后台）
start "ChatServer" java -cp target\classes com.kernelcode.chat.server.ChatServer
echo 服务器已启动
echo.

REM 等待服务器启动
timeout /t 2 /nobreak >nul

REM 启动客户端
echo 正在启动客户端...
echo 注意：启动后请先选择 '1' 进行登录！
echo.
java -cp target\classes com.kernelcode.chat.client.ChatClient

echo.
echo 客户端已退出
taskkill /FI "WINDOWTITLE eq ChatServer*" /F >nul 2>&1
echo 服务器已停止
goto end

:end