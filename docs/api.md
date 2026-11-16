# HTTP 接口

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

## 诊疗流程接口

所有集合GET同样返回PageResult；page为1至1000000，pageSize为1至100。排班按日期倒序，预约和处方按ID倒序，库存按ID升序。浏览器选择器最多读取前2000条匹配记录，患者支持按姓名缩小范围。

| 方法与路径 | 参数或请求体 | 成功状态 |
| --- | --- | --- |
| GET /api/schedules | visitDate、doctorId可选 | 200 |
| POST /api/schedules | doctorId、visitDate（不早于今天）、session（AM/PM）、capacity（1至1000） | 201 |
| PUT /api/schedules/{id} | capacity、enabled（0/1），保留已占用数 | 200 |
| GET /api/registrations | status、patientId可选 | 200 |
| POST /api/registrations | patientId、scheduleId | 201 |
| POST /api/registrations/{id}/cancel | 无请求体，重复取消幂等 | 200 |
| GET /api/prescriptions | 分页 | 200 |
| GET /api/prescriptions/{id} | 含药品明细items | 200 |
| POST /api/prescriptions | 见下例 | 201 |
| GET /api/medicines | 分页返回药品与stock | 200 |

```json
{
  "registrationId": 123,
  "diagnosis": "模拟诊断摘要",
  "items": [{"medicineId": 1,"dosage": "模拟用量","frequency": "模拟频次","days": 1,"quantity": 2.00}]
}
```

上述编号需替换成当前数据库真实编号。items为1至20项，药品不得重复；quantity为正数，最多两位小数。库存以quantity按药品unit扣减，不由用量文字或天数推算。库存页面当前只读，未提供采购或库存调整流程。

| HTTP状态 | 业务错误码 | 用户提示含义 |
| --- | --- | --- |
| 409 | DUPLICATE_BOOKING | 患者已有该排班有效预约 |
| 409 | SCHEDULE_FULL / SCHEDULE_DISABLED / SCHEDULE_EXPIRED | 已满、暂停或过期 |
| 409 | DOCTOR_UNAVAILABLE / SCHEDULE_EXISTS / CAPACITY_TOO_SMALL | 停诊、重复排班或容量小于占用数 |
| 409 | REGISTRATION_COMPLETED / REGISTRATION_NOT_BOOKED | 已完成不可取消；取消或完成不可开方 |
| 409 | DUPLICATE_MEDICINE / INSUFFICIENT_STOCK | 药品重复或库存不足，整张处方不保存 |
| 500 | INTERNAL_ERROR | 未预期运行异常，向用户隐藏内部信息 |

参数错误400，记录不存在404，数据库不可用503。业务失败与运行异常会触发事务回滚，前端保留输入和错误提示，支持修正后重试。服务只绑定本机，尚未实现身份认证和角色权限。
