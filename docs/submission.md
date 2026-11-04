# 实验一提交说明

## 本地材料

源码包含 Maven 配置、标准分层包、Hello World 接口、自动化测试、环境核验 SQL、运行及排错说明。实验报告和证据附件单独放在课程文件夹的“实验一”目录。

## 远程仓库

在 GitHub 创建一个空仓库后，在本项目根目录执行，替换下列占位地址：

```powershell
git remote add origin https://github.com/YOUR_ACCOUNT/medical-system.git
git push -u origin main
git push -u origin develop
```

使用本人账号完成 GitHub 登录。以远程页面可见源码、main/develop 两个分支为核验标准，并将真实地址填入实验报告。未推送前，本地仓库路径不能代替 GitHub 仓库地址。

## 复现

下载源码后进入含 pom.xml 的目录，安装 JDK 21，运行 `mvn clean verify` 和 `mvn spring-boot:run`。打开 `/api/hello` 检查正文，再根据环境说明核对 MySQL 服务。
