# 开发环境与故障排查

## 环境要求

| 组件 | 当前使用版本 | 检查命令 |
| --- | --- | --- |
| JDK | 21.0.6 | java -version、javac -version |
| Maven | 3.10.0 | mvn -v |
| Node.js / npm | 26.13.0 / 11.20.0 | node -v、npm.cmd -v |
| MySQL | 8.0.44 | 连接数据库后查询版本 |
| Spring Boot | 4.1.2 | pom.xml |
| MyBatis Starter | 4.0.1 | pom.xml |
| Vue / Vite | 3.5.45 / 8.3.8 | frontend/package.json |

VS Code 可使用 Java、Spring Boot 和 Vue Official 扩展。编辑器推荐清单位于 .vscode/extensions.json。

## 连接与端口

- 后端默认监听8080，Vite开发服务监听5173。
- Vite 将 /api 代理到 http://127.0.0.1:8080。
- MySQL 默认使用127.0.0.1:3306。
- MyBatis 只读查询数据库版本、服务器时间及常量1，不修改业务数据。
- 数据库凭据保存在项目根目录的 application-local.properties，或通过环境变量提供。

## Java 版本不一致

同时检查 java、javac、Maven 输出和编辑器项目运行时。JAVA_HOME 应指向JDK根目录，Path应包含对应bin目录。更改环境变量后新开终端。

若Maven能编译而编辑器无法解析java.lang.Object，先明确映射项目JDK，再重新导入项目或重建Java语言服务缓存。

## Maven 启动或依赖下载失败

在包含pom.xml的根目录执行命令。检查实际Maven bin目录，留意压缩包解压后的嵌套路径。

下载失败时查看仓库地址、HTTP状态和TLS错误，再检查网络、代理与settings.xml。可先重新执行原命令，无需立即更换所有依赖版本。

## Node.js 与前端构建

确认node和npm来自同一安装目录。已经打开的终端可能保留旧Path，更新配置后新开终端。

使用npm.cmd ci安装锁定依赖。分发完整JAR之前先构建Vue页面；scripts/build-demo.ps1会检查打包内容。

## 数据库连接失败

确认MySQL服务、端口与本机凭据正确，检查当前工作目录能否读取application-local.properties。页面显示503时，不应按正常连接处理。

公开错误响应不包含驱动内部连接信息。修复连接后点击重新检测，确认数据库版本和时间来自新的实际查询。

## 端口与编码

通过监听进程确认端口归属，或使用--server.port指定其他后端端口。后端端口改变时同步修改Vite代理地址。

源文件和HTTP响应使用UTF-8。终端显示乱码与文件编码需要分别排查，避免仅修改源文件编码而未检查终端设置。
