-- 数据库状态检查，仅执行只读查询。
SELECT VERSION() AS mysql_version, @@port AS mysql_port;
SHOW DATABASES;
