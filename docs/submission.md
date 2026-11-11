# 实验一提交说明

## 本地材料

源码包含 Vue 页面、Vite 和 Maven 配置、MyBatis Mapper、Hello World 与数据库检测接口、6 项自动化测试、构建启动脚本及说明。新报告和证据附件保存在 D:\Java web\实验一_新要求，原提交包及修改前 Git bundle 已备份。

## 远程仓库

本实验 GitHub 仓库：https://github.com/sfhyrt/medical-system 。本地已关联该仓库，后续提交可在项目根目录执行：

```powershell
git push -u origin main
git push -u origin develop
```

以远程页面可见源码、main/develop 两个分支为核验标准，报告使用上述真实仓库地址。Git bundle 保留完整历史，可在需要时恢复本地仓库。

## 复现

下载源码后进入含 pom.xml 的目录，准备 JDK 21、Node.js 和 MySQL，参照根目录示例创建本机数据库配置。执行 `.\scripts\build-demo.ps1` 构建，再执行 `.\scripts\start-demo.ps1`，访问 http://localhost:8080/。

分别开发时，后端运行 `mvn spring-boot:run`，前端在 frontend 目录运行 `npm.cmd run dev`，访问 http://127.0.0.1:5173/。完整说明见 README。Docker 是可选项，本次没有配置。
