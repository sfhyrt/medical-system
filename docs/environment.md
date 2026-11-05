# 实验一环境核验与排错记录

核验日期：2026-11-04。系统为 Windows 10 64 位，内存约 32 GB。

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

## 版本与后续实验

项目固定 Java 21、Spring Boot 4.1.2。后续引入 MyBatis-Plus 时应使用适配 Spring Boot 4 的 starter。数据库设计、CRUD、安全认证和前端均属于后续实验，本次只完成初始化与连通性验证。
