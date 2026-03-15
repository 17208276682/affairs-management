-- 事务管理系统 - 后端数据表定义（MySQL 8.x）
-- 说明：当前文件仅包含“表与字段”，用于后端初始化第一步。

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS departments (
  id                VARCHAR(32)  NOT NULL COMMENT '部门ID，如D001',
  name              VARCHAR(100) NOT NULL COMMENT '部门名称',
  parent_id         VARCHAR(32)  NULL COMMENT '上级部门ID',
  leader_id         VARCHAR(32)  NULL COMMENT '部门负责人用户ID',
  sort_order        INT          NOT NULL DEFAULT 0 COMMENT '排序',
  level             INT          NOT NULL DEFAULT 0 COMMENT '层级',
  member_count      INT          NOT NULL DEFAULT 0 COMMENT '成员数量（可冗余）',
  status            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0=停用 1=启用',
  created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_departments_parent_id (parent_id),
  KEY idx_departments_leader_id (leader_id),
  CONSTRAINT fk_departments_parent
    FOREIGN KEY (parent_id) REFERENCES departments(id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT chk_departments_status
    CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

CREATE TABLE IF NOT EXISTS users (
  id                VARCHAR(32)   NOT NULL COMMENT '用户ID，如U001',
  username          VARCHAR(50)   NOT NULL COMMENT '登录账号',
  password_hash     VARCHAR(255)  NOT NULL COMMENT '密码哈希',
  name              VARCHAR(50)   NOT NULL COMMENT '姓名',
  avatar            VARCHAR(500)  NULL COMMENT '头像URL',
  phone             VARCHAR(20)   NOT NULL COMMENT '手机号',
  email             VARCHAR(100)  NULL COMMENT '邮箱',
  dept_id           VARCHAR(32)   NULL COMMENT '所属部门ID',
  position          VARCHAR(100)  NULL COMMENT '岗位',
  role              VARCHAR(20)   NOT NULL COMMENT '角色：admin/director/manager/staff',
  status            TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：0=离职 1=在职',
  created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  UNIQUE KEY uk_users_phone (phone),
  KEY idx_users_dept_id (dept_id),
  KEY idx_users_role (role),
  KEY idx_users_status (status),
  CONSTRAINT fk_users_dept
    FOREIGN KEY (dept_id) REFERENCES departments(id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT chk_users_role
    CHECK (role IN ('admin', 'director', 'manager', 'staff')),
  CONSTRAINT chk_users_status
    CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

ALTER TABLE departments
  ADD CONSTRAINT fk_departments_leader
  FOREIGN KEY (leader_id) REFERENCES users(id)
  ON UPDATE CASCADE ON DELETE SET NULL;

CREATE TABLE IF NOT EXISTS user_managed_departments (
  id                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id           VARCHAR(32)   NOT NULL COMMENT '用户ID',
  dept_id           VARCHAR(32)   NOT NULL COMMENT '可管理部门ID',
  created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_managed_dept (user_id, dept_id),
  KEY idx_user_managed_depts_user_id (user_id),
  KEY idx_user_managed_depts_dept_id (dept_id),
  CONSTRAINT fk_umd_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_umd_dept
    FOREIGN KEY (dept_id) REFERENCES departments(id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户可管理部门关系表';

CREATE TABLE IF NOT EXISTS tasks (
  id                   VARCHAR(32)   NOT NULL COMMENT '事务ID，如T20260201001',
  title                VARCHAR(200)  NOT NULL COMMENT '事务标题',
  description          TEXT          NULL COMMENT '事务描述',
  assigner_id          VARCHAR(32)   NOT NULL COMMENT '下达人ID',
  executor_id          VARCHAR(32)   NOT NULL COMMENT '执行人ID',
  importance           TINYINT       NOT NULL COMMENT '重要性：1-3',
  urgency              TINYINT       NOT NULL COMMENT '紧急度：1-3',
  level                CHAR(1)       NOT NULL COMMENT '级别：A/B/C/D',
  response_deadline    DATETIME      NOT NULL COMMENT '响应截止时间',
  completion_deadline  DATETIME      NOT NULL COMMENT '完成截止时间',
  response_time        DATETIME      NULL COMMENT '实际响应时间',
  completion_time      DATETIME      NULL COMMENT '实际完成时间',
  status               VARCHAR(20)   NOT NULL COMMENT '状态',
  parent_task_id       VARCHAR(32)   NULL COMMENT '父任务ID（向下分派）',
  created_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_tasks_assigner_id (assigner_id),
  KEY idx_tasks_executor_id (executor_id),
  KEY idx_tasks_parent_task_id (parent_task_id),
  KEY idx_tasks_status (status),
  KEY idx_tasks_level (level),
  KEY idx_tasks_completion_deadline (completion_deadline),
  CONSTRAINT fk_tasks_assigner
    FOREIGN KEY (assigner_id) REFERENCES users(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_tasks_executor
    FOREIGN KEY (executor_id) REFERENCES users(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_tasks_parent
    FOREIGN KEY (parent_task_id) REFERENCES tasks(id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT chk_tasks_importance
    CHECK (importance IN (1, 2, 3)),
  CONSTRAINT chk_tasks_urgency
    CHECK (urgency IN (1, 2, 3)),
  CONSTRAINT chk_tasks_level
    CHECK (level IN ('A', 'B', 'C', 'D')),
  CONSTRAINT chk_tasks_status
    CHECK (status IN (
      'pending', 'accepted', 'in_progress', 'submitted',
      'approved', 'rejected', 'completed', 'overdue', 'cancelled'
    ))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事务表';

CREATE TABLE IF NOT EXISTS task_process_records (
  id                VARCHAR(32)   NOT NULL COMMENT '流程记录ID，如R001',
  task_id           VARCHAR(32)   NOT NULL COMMENT '关联任务ID',
  operator_id       VARCHAR(32)   NOT NULL COMMENT '操作人ID',
  action            VARCHAR(20)   NOT NULL COMMENT '操作类型',
  content           TEXT          NULL COMMENT '操作内容',
  created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_records_task_id (task_id),
  KEY idx_records_operator_id (operator_id),
  KEY idx_records_action (action),
  KEY idx_records_created_at (created_at),
  CONSTRAINT fk_records_task
    FOREIGN KEY (task_id) REFERENCES tasks(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_records_operator
    FOREIGN KEY (operator_id) REFERENCES users(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_records_action
    CHECK (action IN (
      'create', 'assign', 'accept', 'process', 'submit',
      'approve', 'reject', 'reassign', 'cancel', 'comment'
    ))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务流程记录表';

CREATE TABLE IF NOT EXISTS attachments (
  id                VARCHAR(32)   NOT NULL COMMENT '附件ID，如F001',
  file_name         VARCHAR(255)  NOT NULL COMMENT '文件名',
  file_url          VARCHAR(500)  NOT NULL COMMENT '文件访问地址',
  file_size         BIGINT        NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  mime_type         VARCHAR(120)  NULL COMMENT 'MIME类型',
  uploaded_by       VARCHAR(32)   NULL COMMENT '上传人ID',
  uploaded_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_attachments_uploaded_by (uploaded_by),
  CONSTRAINT fk_attachments_uploaded_by
    FOREIGN KEY (uploaded_by) REFERENCES users(id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件元数据表';

CREATE TABLE IF NOT EXISTS task_attachments (
  id                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  task_id           VARCHAR(32)   NOT NULL COMMENT '任务ID',
  attachment_id     VARCHAR(32)   NOT NULL COMMENT '附件ID',
  created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_task_attachment (task_id, attachment_id),
  KEY idx_task_attachments_task_id (task_id),
  KEY idx_task_attachments_attachment_id (attachment_id),
  CONSTRAINT fk_task_attachments_task
    FOREIGN KEY (task_id) REFERENCES tasks(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_task_attachments_attachment
    FOREIGN KEY (attachment_id) REFERENCES attachments(id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务-附件关系表';

CREATE TABLE IF NOT EXISTS record_attachments (
  id                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  record_id         VARCHAR(32)   NOT NULL COMMENT '流程记录ID',
  attachment_id     VARCHAR(32)   NOT NULL COMMENT '附件ID',
  created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_record_attachment (record_id, attachment_id),
  KEY idx_record_attachments_record_id (record_id),
  KEY idx_record_attachments_attachment_id (attachment_id),
  CONSTRAINT fk_record_attachments_record
    FOREIGN KEY (record_id) REFERENCES task_process_records(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_record_attachments_attachment
    FOREIGN KEY (attachment_id) REFERENCES attachments(id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程记录-附件关系表';

CREATE TABLE IF NOT EXISTS notifications (
  id                VARCHAR(32)   NOT NULL COMMENT '通知ID，如N001',
  user_id           VARCHAR(32)   NOT NULL COMMENT '接收用户ID',
  title             VARCHAR(200)  NOT NULL COMMENT '通知标题',
  content           TEXT          NOT NULL COMMENT '通知内容',
  type              VARCHAR(30)   NOT NULL COMMENT '通知类型',
  related_task_id   VARCHAR(32)   NULL COMMENT '关联任务ID',
  is_read           TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否已读：0否 1是',
  created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_notifications_user_id (user_id),
  KEY idx_notifications_related_task_id (related_task_id),
  KEY idx_notifications_is_read (is_read),
  KEY idx_notifications_created_at (created_at),
  CONSTRAINT fk_notifications_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_notifications_task
    FOREIGN KEY (related_task_id) REFERENCES tasks(id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT chk_notifications_type
    CHECK (type IN (
      'task_assigned', 'task_accepted', 'task_responded', 'task_completed',
      'task_submitted', 'task_approved', 'task_rejected', 'system'
    )),
  CONSTRAINT chk_notifications_is_read
    CHECK (is_read IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';
