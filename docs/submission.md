# 实验一提交说明

## 本地材料

源码包含 Maven 配置、标准分层包、Hello World 接口、自动化测试、环境核验 SQL、运行及排错说明。实验报告和证据附件单独放在课程文件夹的“实验一”目录。

## 远程仓库

本实验 GitHub 仓库：https://github.com/sfhyrt/medical-system 。本地已关联该仓库，后续提交可在项目根目录执行：

```powershell
git push -u origin main
git push -u origin develop
```

以远程页面可见源码、main/develop 两个分支为核验标准，报告使用上述真实仓库地址。Git bundle 保留完整历史，可在需要时恢复本地仓库。

## 复现

下载源码后进入含 pom.xml 的目录，安装 JDK 21，运行 `mvn clean verify` 和 `mvn spring-boot:run`。打开 `/api/hello` 检查正文，再根据环境说明核对 MySQL 服务。
