# 资料管理接口

接口前缀为/api，JSON编码为UTF-8。当前服务仅面向本机开发，未提供用户身份与角色授权。

| 资源 | 集合路径 | 组合查询参数 |
| --- | --- | --- |
| 科室 | /api/departments | code、name、location |
| 医生 | /api/doctors | code、name、departmentId、title、status |
| 患者 | /api/patients | name、phone、idCard |

GET集合统一接收page（从1开始）和pageSize（1至100）。响应包含records、total、page、pageSize、pages。筛选条件同时生效；患者phone、idCard为精确匹配，其余文本筛选为包含匹配。列表按id升序稳定分页。

| 操作 | 方法与路径 | 成功状态 |
| --- | --- | --- |
| 分页查询 | GET集合路径 | 200 |
| 新增 | POST集合路径 | 201，返回新记录 |
| 详情 | GET集合路径/{id} | 200 |
| 编辑 | PUT集合路径/{id} | 200，完整替换可编辑字段 |
| 删除 | DELETE集合路径/{id} | 204 |

GET /api/departments/options返回科室选项。患者列表中的phone、idCard脱敏；详情接口用于编辑，返回完整字段，需在后续访问授权模块中加以保护。

## 请求字段

- 科室：code、name、location、phone必填；description可选。code为2至32位大写字母、数字、下划线或短横线。
- 医生：code、name、departmentId、title、phone、status必填。status为0（停诊）或1（在职），departmentId必须存在。
- 患者：name、idCard、phone必填，address可选。身份证须满足日期与校验码验证，手机号须满足11位格式。

## 错误响应

错误统一返回code与message，不暴露SQL、驱动内部信息或数据库凭据。

| HTTP状态 | 错误码 | 场景 |
| --- | --- | --- |
| 400 | VALIDATION_ERROR / INVALID_REFERENCE | 字段、分页、类型、身份证格式错误或所选科室不存在 |
| 404 | NOT_FOUND | 记录不存在或已被删除 |
| 409 | DATA_CONFLICT | 唯一字段重复或记录被其他数据引用 |
| 503 | DATABASE_UNAVAILABLE | 数据服务不可用 |
