# 预约与处方流程

Controller仅处理输入验证和HTTP响应，ScheduleService管理排班容量，RegistrationService协调挂号与取消，PrescriptionService编排处方，InventoryService封装库存条件扣减。Mapper负责持久化，WorkflowQueryService负责绑定参数的分页关联查询。

## 挂号调用时序

![挂号调用时序](booking-sequence.svg)

```mermaid
sequenceDiagram
    actor Patient as 患者
    participant UI as Vue预约页
    participant C as WorkflowController
    participant S as RegistrationService
    participant M as Schedule/RegistrationMapper
    participant DB as MySQL InnoDB
    Patient->>UI: 选择患者和排班
    UI->>C: POST /api/registrations
    C->>C: Bean Validation
    C->>S: book(request)
    Note over S,DB: 事务开始 rollbackFor=Exception.class
    S->>M: lock(scheduleId)
    M->>DB: SELECT ... FOR UPDATE
    DB-->>S: 当前排班和余号
    S->>M: 校验患者 医生 有效重复预约
    alt 重复 暂停 过期或号源已满
        S-->>C: BusinessException
        Note over S,DB: 回滚 不产生挂号 不扣号源
        C-->>UI: 409 code/message
        UI-->>Patient: 保留表单 显示可读原因
    else 可预约
        S->>M: reserve(id)
        M->>DB: UPDATE remaining=remaining-1 WHERE remaining>0
        S->>M: insert(registration)
        M->>DB: INSERT 有效预约唯一约束
        alt 途中发生运行异常
            Note over S,DB: 回滚挂号和号源更新
            C-->>UI: 500 通用提示
        else 全部成功
            Note over S,DB: COMMIT
            S-->>C: 挂号记录
            C-->>UI: 201
            UI-->>Patient: 刷新预约和余号
        end
    end
```

## 处方事务

```mermaid
sequenceDiagram
    actor Staff as 操作人员
    participant C as WorkflowController
    participant P as PrescriptionService
    participant I as InventoryService
    participant DB as MySQL InnoDB
    Staff->>C: 提交诊断与药品明细
    C->>P: issue(request)
    Note over P,DB: @Transactional 开始
    P->>DB: 挂号 SELECT FOR UPDATE
    P->>P: 校验BOOKED及不重复药品
    P->>DB: INSERT prescription
    loop 按medicineId升序
        P->>I: consume(id, quantity)
        Note over I,DB: MANDATORY 加入外层事务
        I->>DB: UPDATE stock=stock-quantity WHERE stock>=quantity
        P->>DB: INSERT prescription_item
    end
    alt 任一项缺货或抛出异常
        Note over P,DB: ROLLBACK 处方 明细 全部库存
        C-->>Staff: 409或通用错误 输入保留
    else 所有项通过
        P->>DB: UPDATE registration status=COMPLETED
        Note over P,DB: COMMIT
        C-->>Staff: 201 返回处方
    end
```

挂号事务与开方事务分别提交。开方失败保留之前成功的预约，不退回其号源；可以修正药品数量后重试。取消预约按“排班→挂号”的顺序加锁，重复取消不再次归还。开方只锁挂号与药品，不反向锁排班；多药品按ID顺序扣库存。取消与开方竞争由挂号行锁决定先后，最终只能是已取消且无处方，或已完成且库存已扣。

## 真实数据库验证

运行mvn.cmd test -Dtransaction.check=true，启用WorkflowTransactionTests。测试不使用内存库，也不对测试方法添加@Transactional；每个服务的提交或回滚独立可见。

- 正常预约、取消、重复取消和取消后重新预约。
- 正常开方、再次开方与已完成后取消拦截。
- 第二个药品不足，验证第一个药品库存、处方头和已有明细同时回滚。
- 使用Mockito Spy在真实号源更新后和真实库存更新后抛出异常；只在测试中注入，不向HTTP接口暴露失败开关。
- 最后一个号源并发、同一患者重复并发、同一库存并发、取消与开方并发。
- 容量低于占用数、停诊、暂停、过期及重复药品。

样例仅使用合成记录；默认测试运行14项无需真实库的测试，显式启用后共26项。测试清理仅针对本次创建的记录。真实接口回归scripts/workflow-regression.py需要已启动服务和迁移完成的数据库。

设计依据：[Spring事务回滚规则](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/rolling-back.html)、[MySQL锁定读](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking-reads.html)。使用事务代理的公开方法和显式rollbackFor，避免自调用绕开事务；库存服务要求已存在事务。
