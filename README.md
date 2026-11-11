# Medical System

基于 Vue 3、Spring Boot、MyBatis 和 MySQL 的医疗系统基础工程，当前提供服务状态面板、接口连通检查和数据库运行信息查询。

## 当前功能

- 服务状态面板：查看后端接口、数据库连接和 Java 运行环境。
- 数据库状态查询：通过 MyBatis 获取 MySQL 版本、服务器时间和查询验证结果。
- 手动刷新：重新检测服务状态，失败时清除过期结果并显示错误提示。
- 响应式页面：支持桌面与移动端查看。
- 一体化运行：Vue 构建产物包含在可执行 JAR 中，也支持前后端分别开发。

当前版本聚焦基础设施和服务状态查询。医疗业务模块可在现有分层上继续扩展。

## 技术栈

| 组件 | 版本 |
| --- | --- |
| Java / Maven | JDK 21 / Maven 3.10.0 |
| Spring Boot | 4.1.2 |
| MyBatis Starter / MyBatis | 4.0.1 / 3.5.19 |
| MySQL | 8.0.44 |
| Node.js / npm | 26.13.0 / 11.20.0 |
| Vue / Vite | 3.5.45 / 8.3.8 |

前端依赖通过 package-lock.json 锁定；MySQL 驱动由 Spring Boot 管理版本。

## 快速启动

准备 JDK 21、Maven、Node.js 和 MySQL。在包含 pom.xml 的项目根目录创建本机连接配置：

```powershell
Copy-Item application-local.properties.example application-local.properties
```

在新文件中填写本机数据库账号、密码，再构建并启动：

```powershell
.\scripts\build-demo.ps1
.\scripts\start-demo.ps1
```

打开 http://localhost:8080/。构建脚本会安装前端依赖、构建 Vue 页面、执行后端测试并生成可执行 JAR。

已存在本机配置时直接使用原文件。数据库凭据不提交到仓库，也不打入 JAR。默认连接 127.0.0.1:3306，查询无需创建业务表。

## 分别开发前后端

在项目根目录启动后端：

```powershell
mvn.cmd spring-boot:run
```

另开终端启动前端：

```powershell
cd frontend
npm.cmd ci
npm.cmd run dev
```

访问 http://127.0.0.1:5173/。Vite 将 /api 请求代理到8080。

## 配置与部署

可通过本机 application-local.properties 或环境变量 DB_URL、DB_USERNAME、DB_PASSWORD 提供数据库连接配置。本机文件中的值优先于默认占位符。

默认 JDBC 参数适用于本地开发。连接远程数据库时，使用符合部署环境的 TLS 配置。

完整构建后，在项目根目录运行：

```powershell
java -jar target/medical-system-0.0.1-SNAPSHOT.jar
```

JAR 运行时不需要 Node.js。单独分发 JAR 时，在启动目录提供本机连接配置或设置环境变量。端口可以通过 --server.port=8082 覆盖。

## 接口

| 请求 | 响应 |
| --- | --- |
| GET /api/hello | 200，返回服务连通信息 |
| GET /api/environment | 200，返回运行环境和真实数据库查询结果；连接或验证失败时返回503 |

## 测试

构建脚本执行 npm ci、npm run build、mvn clean verify，并检查 JAR 内是否包含 Vue 页面。仅执行后端测试时使用 mvn test。

现有6项自动化测试覆盖接口响应、404、405、查询值传递和数据库失败处理。测试使用 Mapper 替身；真实数据库连接通过页面或 /api/environment 单独验证。

## 目录

```text
frontend/                    Vue 页面、Vite 配置与依赖锁
src/main/java/com/example/medical/
  controller/                HTTP 接口
  service/                   服务逻辑与连接异常处理
  mapper/                    MyBatis 数据访问
  dto/                       查询结果与接口响应
src/main/resources/          公开配置
scripts/                     构建与启动脚本
docs/                        开发与环境文档
```

详细说明：[开发指南](docs/development.md)、[开发环境与故障排查](docs/environment.md)。
