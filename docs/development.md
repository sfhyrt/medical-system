# 开发指南

## 项目结构

前端采用 Vue 单文件组件，后端按 Controller、Service、Mapper 和 DTO 分层。资料管理使用DirectoryService及MyBatis-Plus BaseMapper，统一校验和异常处理位于validation、exception包。数据库状态查询由DatabaseProbeMapper保留提供。

## 分支与提交

main 保存通过构建与验证的版本，develop 用于功能迭代。功能验证完成后合并至 main，再同步两个远程分支。

提交说明采用 feat、fix、docs、test、chore 等前缀，写明具体能力或修复结果。

## 本地开发

1. 在项目根目录根据 application-local.properties.example 创建本机连接配置。
2. 在项目根目录运行 mvn.cmd spring-boot:run。
3. 在 frontend 目录运行 npm.cmd ci、npm.cmd run dev。
4. 访问 http://127.0.0.1:5173/。Vite 将 /api 转发到8080。

完整构建使用 scripts/build-demo.ps1；启动打包服务使用 scripts/start-demo.ps1。详细命令见根目录 README。

## 验证与故障处理

后端自动化测试覆盖接口成功响应、404、405、数据库故障与无效查询结果。集成验证时，确认浏览器显示的版本和时间来自实际数据库，再检查错误提示和重新检测是否正常。

数据库密码通过环境变量或本机私有配置提供。配置示例、源码和公开文档不包含实际凭据。排错方法见 environment.md。


首次运行先初始化sql/schema.sql，详见README。代码生成器位于tools/codegen。所有开发数据均须为合成数据。

## 诊疗业务迭代

旧数据库先执行scripts/migrate-database.ps1，再构建应用。新增工作流不替换基础资料CRUD，接口见api.md，事务与时序见workflows.md。库存与号源由服务事务管理，禁止在页面直接修改计数。
