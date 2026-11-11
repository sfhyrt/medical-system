# 实验一环境核验与排错记录

首次核验：2026-11-04；按课堂新要求补充：2026-11-11。系统为 Windows 10 64 位，内存约 32 GB。

## 已验证的环境

| 项目 | 本机版本或位置 | 核验方法 |
|---|---|---|
| JDK | 21.0.6；`D:\develob\java` | `java -version`、`javac -version`、Maven 启动日志 |
| Maven | 3.10.0；`D:\develob\apache-maven-3.10.0-bin\apache-maven-3.10.0` | `mvn -v`、实际依赖解析与构建 |
| MySQL | 8.0.44；MySQL80 服务；3306 | 本机登录、版本查询和数据库列表截图 |
| Git | 2.55.0.windows.5 | `git --version`、分支与提交历史 |
| VS Code | 1.144.1 | 安装清单及 Java、Spring 扩展清单 |
| Spring Boot | 4.1.2 | Maven 配置及服务启动日志 |

## 实际发生并解决的问题

1. **Java 版本冲突。** 初始 `java` 为 Java 8，`javac` 为 Java 17。将 `JAVA_HOME` 设为 `D:\develob\java`，把对应 bin 提到 Path 前面，重新打开终端后统一到 JDK 21。
2. **mvn 命令找不到。** 解压包额外包含一层 `apache-maven-3.10.0-bin`，最初 Path 与实际目录不符。将 Path 指向实际 Maven bin 目录，版本检查与依赖下载通过。
3. **找不到 spring-boot 插件前缀。** 曾在 `src/main/java/.../controller` 中运行命令。切换至含 `pom.xml` 的项目根目录后正常启动。
4. **VS Code 无法解析 java.lang.Object。** Maven 已能编译运行，但编辑器仍报告基础类缺失。明确映射 JDK 21，保留旧缓存备份并重新导入后，Java 语言服务报告 HelloController 为 0 个问题。
5. **MySQL 登录失败。** 首次密码输入未通过，重新输入正确密码后成功登录并查询出 8.0.44、3306 和数据库列表。未修改现有数据库和账号。

## 未发生故障时的定位方法

- Maven 仓库或镜像失效：先查看失败仓库 URL、HTTP 状态、代理和 TLS 报错，再检查用户级 `settings.xml`。本次 Maven Central 实际可用，使用默认仓库完成依赖下载，未人为切换镜像。
- MySQL 端口冲突：查看 `Get-NetTCPConnection -LocalPort 3306 -State Listen` 的进程归属，再检查 MySQL 配置和客户端端口。本机 3306 由 MySQL 自身正常监听，未发生端口冲突。
- 编码乱码：Java 源文件、Maven 工程与 HTTP 响应使用 UTF-8。Windows 终端显示编码问题与源文件编码分别排查；报告如实采用 VS Code，未声称在 IDEA 中复现故障。

## 按新要求补充的环境

项目保留 Java 21、Spring Boot 4.1.2，引入 MyBatis Starter 4.0.1 和 MySQL Connector/J。MyBatis 官方兼容表说明 4.0 系列支持 Spring Boot 4.0+。本机只读 SQL 查询数据库版本、当前时间和常量 1，验证真实 Mapper 调用，不修改已有业务数据。

Node.js 26.13.0 LTS 安装在 D:\develob\node-v26.13.0-win-x64，npm 11.20.0；压缩包经 Node.js 官方 SHASUMS256.txt 验证。用户 Path 已补充该目录。已经打开的终端可能保留旧环境，可新开 VS Code 终端；项目本机终端配置也包含 Node.js 路径。

Vue 3.5.45、Vite 8.3.8、Vue 插件 6.0.10 的版本固定在 frontend/package.json，并提交 package-lock.json。开发页面监听 5173，/api 代理至后端 8080。生产构建复制至 Jar 内 static，交付后可只启动 Java 服务。

VS Code 增加 Vue Official 扩展。Node.js 下载发生中断后使用断点续传，再校验完整文件的 SHA256；没有跳过完整性验证。

数据库凭据保存在根目录的 application-local.properties，Git 忽略该文件，Jar 和源码包不包含它。后端查询失败返回 503，前端显示错误提示；重新检测会清除上次状态。
