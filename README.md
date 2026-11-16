# Medical System

基于 Vue 3、Spring Boot、MyBatis 和 MySQL 的医疗系统基础工程，提供科室、医生、患者信息维护，以及服务状态与数据库运行信息查询。

## 当前功能

- 科室、医生、患者信息的新增、编辑、删除和详情查询。
- 多条件组合检索与分页，医生列表关联展示科室名称。
- 手机号格式、身份证出生日期及校验码验证，重复信息和关联删除保护。
- 患者列表中的证件号和联系方式脱敏展示，编辑页按需读取完整资料。

- 服务状态面板：查看后端接口、数据库连接和 Java 运行环境。
- 数据库状态查询：通过 MyBatis 获取 MySQL 版本、服务器时间和查询验证结果。
- 手动刷新：重新检测服务状态，失败时清除过期结果并显示错误提示。
- 响应式页面：支持桌面与移动端查看。
- 一体化运行：Vue 构建产物包含在可执行 JAR 中，也支持前后端分别开发。

当前版本覆盖基础资料维护；挂号及处方已有关系表结构，业务流程、登录及授权模块尚未实现。服务默认仅监听127.0.0.1，供本地开发和验证使用。

## 技术栈

| 组件 | 版本 |
| --- | --- |
| Java / Maven | JDK 21 / Maven 3.10.0 |
| Spring Boot | 4.1.2 |
| MyBatis-Plus | 3.5.18，使用 Spring Boot 4 Starter 与独立分页组件 |
| MySQL | 8.0.44 |
| Node.js / npm | 26.13.0 / 11.20.0 |
| Vue / Vite | 3.5.45 / 8.3.8 |

前端依赖通过 package-lock.json 锁定；MySQL 驱动由 Spring Boot 管理版本。

## 快速启动

准备 JDK 21、Maven、Node.js 和 MySQL。在包含 pom.xml 的项目根目录创建本机连接配置：

```powershell
Copy-Item application-local.properties.example application-local.properties
```

在新文件中填写本机数据库账号、密码。默认数据库为medical_system，首次运行先初始化关系表：

```powershell
.\scripts\generate-persistence.ps1 -Initialize
```

该命令执行sql/schema.sql，包含CREATE DATABASE IF NOT EXISTS与CREATE TABLE IF NOT EXISTS。它保留已有数据，也不会替代结构迁移工具升级旧表。

按需在MySQL客户端执行sql/sample-data.sql加载合成开发数据；脚本中的姓名、证件、联系方式及诊疗内容均为合成内容。然后构建并启动：

```powershell
.\scripts\build-demo.ps1
.\scripts\start-demo.ps1
```

打开 http://localhost:8080/。构建脚本会安装前端依赖、构建 Vue 页面、执行后端测试并生成可执行 JAR。

已存在本机配置时直接使用原文件。数据库凭据不提交到仓库，也不打入 JAR。默认连接127.0.0.1:3306/medical_system。

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

## 持久层代码生成

```powershell
.\scripts\generate-persistence.ps1
.\scripts\generate-persistence.ps1 -Verify
```

生成器根据七张表创建实体、BaseMapper接口和XML映射。默认不覆盖已有源码；-Verify将全套21个文件生成到target/generated-persistence，便于复核生成结果。生成器为独立Maven工具模块，不进入应用JAR。

## 接口

科室、医生、患者的接口分别为/api/departments、/api/doctors、/api/patients。GET集合支持page、pageSize和组合筛选；POST新增；GET /{id}读取详情；PUT /{id}编辑；DELETE /{id}删除。成功状态分别为200、201或204，校验失败400、记录不存在404、唯一性或关联冲突409。

| 请求 | 响应 |
| --- | --- |
| GET /api/hello | 200，返回服务连通信息 |
| GET /api/environment | 200，返回运行环境和真实数据库查询结果；连接或验证失败时返回503 |

## 测试

构建脚本执行 npm ci、npm run build、mvn clean verify，并检查 JAR 内是否包含 Vue 页面。仅执行后端测试时使用 mvn test。

现有14项JUnit测试覆盖接口响应、异常分支、手机号与身份证边界、MyBatis参数绑定。运行真实数据库HTTP回归：

```powershell
python scripts/http-regression.py
```

脚本通过40项检查验证CRUD、组合分页、唯一约束、关联删除和注入载荷处理；仅清理本次创建的合成记录。首次运行前应加载sample-data.sql，其中的DOC001关联记录用于验证删除保护。

患者列表返回脱敏字段，编辑详情接口返回完整资料；尚未接入访问授权，因此只应用于本地合成数据环境。

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

详细说明：[开发指南](docs/development.md)、[数据库模型](docs/database.md)、[接口契约](docs/api.md)、[开发环境与故障排查](docs/environment.md)。
