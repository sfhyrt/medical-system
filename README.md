# 医疗系统实验一

Java Web 开发实验一：开发环境搭建与医疗系统 Hello World。

## 环境

本机验证组合：JDK 21.0.6、Maven 3.10.0、MySQL 8.0.44、Git 2.55.0.windows.5、VS Code 1.144.1、Spring Boot 4.1.2。Java 运行器、编译器和 Maven 使用同一套 JDK 21。

VS Code 推荐扩展见 `.vscode/extensions.json`。本机 JDK 路径存放在忽略提交的 `.vscode/settings.json` 中，其他电脑按实际安装位置配置。

## 启动与访问

在包含 `pom.xml` 的项目根目录打开终端：

```powershell
java -version
javac -version
mvn -v
mvn spring-boot:run
```

项目也提供 Maven Wrapper：`./mvnw.cmd spring-boot:run`。首次执行需要联网下载依赖。

浏览器访问 `http://localhost:8080/api/hello`，应收到 HTTP 200 和正文 `Hello World! Medical System`。服务运行期间保持终端开启，按 Ctrl+C 停止。

## 分层结构

```text
src/main/java/com/example/medical/
  MedicalSystemApplication.java    启动入口与组件扫描根包
  controller/HelloController.java  接收 GET /api/hello
  service/HelloService.java        示例服务，通过构造方法注入 Controller
  entity/                         医疗业务实体预留包
  mapper/                         数据访问层预留包
  dto/                            接口数据对象预留包
  config/                         配置预留包
  exception/                      异常处理预留包
src/main/resources/application.properties
src/test/java/com/example/medical/MedicalSystemApplicationTests.java
sql/environment-check.sql
```

`Controller → Service` 调用链已实现。预留包通过 `package-info.java` 说明职责；业务表、持久化代码和 CRUD 在实验二实现。Hello World 不依赖数据库连接，MySQL 安装与登录单独核验。

## 测试和打包

```powershell
mvn clean verify
java -jar target/medical-system-0.0.1-SNAPSHOT.jar
```

自动化测试启动随机端口的真实 Web 服务，核验 GET 正常返回 200、未知路径返回 404、错误请求方法返回 405。生成的可执行 Jar 位于 `target/`，构建产物不提交到 Git。

在服务启动后，可用 `./scripts/verify-http.ps1` 再次核验本机接口。

## Git 分支和提交规范

- `main`：通过测试、可供验收的版本。
- `develop`：开发分支，完成后经验证合并到 `main`。
- 提交使用 `chore:`（初始化）、`feat:`（功能）、`test:`（测试）、`docs:`（文档）前缀，描述实际变更。

远程仓库设置方法见 `docs/submission.md`。数据库密码、令牌、机器专用 JDK 路径及构建目录不提交。

## 环境排错

实际遇到的问题和处理记录见 `docs/environment.md`。本项目使用 VS Code 完成实验，报告中如实记录开发工具。
