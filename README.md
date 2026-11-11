# 医疗系统实验一

Java Web homework — medical system lab 1.

实验一环境搭建与前后端联调：Vue 页面调用 Spring Boot 接口，由 MyBatis 执行真实的 MySQL 只读查询，展示数据库版本、服务器时间和验证值。开发工具采用 VS Code。

GitHub 仓库：https://github.com/sfhyrt/medical-system

## 环境与依赖

| 组件 | 本次使用版本 |
| --- | --- |
| Java / Maven | Oracle JDK 21.0.6 / Maven 3.10.0 |
| Spring Boot / MyBatis Starter | 4.1.2 / 4.0.1 |
| MySQL Server | 8.0.44 |
| Node.js / npm | 26.13.0 / 11.20.0 |
| Vue / Vite | 3.5.45 / 8.3.8 |
| Vite Vue 插件 | 6.0.10 |
| 开发工具 | VS Code，Java、Spring Boot 和 Vue Official 扩展 |

前端依赖使用精确版本及 package-lock.json 锁定，MySQL 驱动由 Spring Boot 管理版本。MyBatis Starter 4.0 系列支持 Spring Boot 4.0+。JDK、Node.js 和数据库密码不随源码提交。

## 本机数据库配置

在含 pom.xml 的项目根目录，将 application-local.properties.example 复制为 application-local.properties，在本机编辑用户名、密码。此文件已加入 Git 忽略，保持在项目根目录，不放入 src/main/resources。

也可使用环境变量 DB_URL、DB_USERNAME、DB_PASSWORD；本机配置文件中的值优先于默认占位符。默认连接 127.0.0.1:3306。

验证 SQL 仅执行 SELECT VERSION(), CURRENT_TIMESTAMP, 1，不需要创建数据库或业务表，不修改已有数据。关闭 SSL 和允许公钥获取仅用于本机实验；远程部署应另行配置 TLS。

## 构建并运行完整页面

在项目根目录的 PowerShell 中执行：

```powershell
node -v
npm.cmd -v
java -version
mvn.cmd -v
.\scripts\build-demo.ps1
.\scripts\start-demo.ps1
```

构建脚本依次执行 npm ci、npm run build、mvn clean verify，并确认 Vue 的 index.html 已进入 Jar。Maven 本身只复制已有的 frontend/dist，因此交付完整页面时应使用上述脚本。若本机脚本策略限制，可执行 powershell -ExecutionPolicy Bypass -File .\scripts\build-demo.ps1，不需要修改系统策略。

打开 http://localhost:8080/，点击“重新检测”，应看到 Vue、后端接口、MyBatis 和 MySQL 均通过，数据库版本显示本机实测值。运行期间保持终端开启。

也可以在项目根目录执行 java -jar target/medical-system-0.0.1-SNAPSHOT.jar。Jar 内已含 Vue 构建产物，运行时不需要 Node.js。若使用独立 Jar，请在它的当前工作目录放置本机的 application-local.properties，或设置数据库环境变量。

## 前后端分别开发

后端，在项目根目录执行：

```powershell
mvn.cmd spring-boot:run
```

前端，另开终端执行：

```powershell
cd frontend
npm.cmd ci
npm.cmd run dev
```

访问 http://127.0.0.1:5173/。Vite 将 /api 转发到 http://127.0.0.1:8080，页面使用相对接口路径，避免开发时跨域。5173 已被占用时会明确报错。

## 接口和验证

| 请求 | 行为 |
| --- | --- |
| GET /api/hello | HTTP 200，Hello World! Medical System |
| GET /api/environment | MyBatis 查询成功返回 HTTP 200 和实际数据库信息；连接或验证失败返回 HTTP 503 |
| GET /api/not-found | HTTP 404 |
| POST /api/hello | HTTP 405 |

mvn clean verify 中的 6 项自动化测试覆盖原有接口、查询值传递、数据库失败和异常结果，测试使用 Mapper 替身，不要求测试机保存数据库密码。真实 MySQL 连接、Vue 开发代理及打包页面另以浏览器联调验证；两类证据分别记录。

## 主要目录

```text
frontend/                     Vue 单文件组件、Vite 配置与依赖锁
src/main/java/com/example/medical/
  controller/                 Hello 与环境检测接口
  service/                    服务逻辑、数据库失败处理
  mapper/DatabaseProbeMapper  MyBatis 只读查询
  dto/                        查询结果和接口响应
src/main/resources/application.properties  公开配置及环境变量占位符
application-local.properties.example       本机凭据配置示例
scripts/                      构建、启动和 HTTP 验证脚本
docs/                         环境与提交说明
```

## 提交与可选项

develop 保存开发提交，验证完成后合并至 main，两分支同步到 GitHub。提交包包含报告、源码、可执行 Jar、Git bundle 和配置及运行截图。Docker 按新要求作为可选项，本次未加入。

官方依据：[MyBatis 兼容表](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)、[Vue 快速开始](https://vuejs.org/guide/quick-start.html)、[Node.js 下载](https://nodejs.org/en/download)。
