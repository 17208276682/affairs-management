<template>
  <div class="task-list">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-left">
        <el-input
          v-model="keyword"
          placeholder="搜索事务描述"
          prefix-icon="Search"
          clearable
          class="filter-select"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="statusFilter"
          placeholder="状态筛选"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="待处理" value="pending" />
          <el-option label="待审核" value="submitted" />
          <el-option label="已逾期" value="overdue" />
          <el-option label="已完成" value="completed" />
          <el-option label="不通过" value="rejected" />
          <el-option label="已作废" value="cancelled" />
        </el-select>
        <el-select
          v-model="levelFilter"
          placeholder="级别筛选"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="重要紧急" value="A" />
          <el-option label="重要不紧急" value="C" />
          <el-option label="不重要紧急" value="B" />
          <el-option label="不重要不紧急" value="D" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      <div class="filter-right">
        <el-button v-if="canCreateTask" class="create-btn" type="primary" @click="showCreatePanel">
          <el-icon><Plus /></el-icon>新建事务
        </el-button>
      </div>
    </div>

    <!-- 左右分栏布局 -->
    <div class="split-layout">
      <!-- 左侧：表格 -->
      <el-card v-loading="taskStore.listLoading" class="list-card">
        <div class="table-scroll-area">
          <el-table
            :data="sortedTaskList"
            stripe
            style="width: 100%"
            highlight-current-row
            @row-click="handleRowClick"
            @sort-change="handleSortChange"
          >
            <el-table-column label="事务级别" width="170" prop="level" :sortable="enableColumnSort ? 'custom' : false" header-class-name="sortable-right-header">
              <template #default="{ row }">
                <div class="level-cell">
                  <div class="level-badge-sm" :style="{ background: TASK_LEVEL_MAP[row.level as TaskLevel]?.color }">
                    {{ row.level }}
                  </div>
                  <span class="level-text">{{ TASK_LEVEL_MAP[row.level as TaskLevel]?.label }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="事务描述" min-width="160" show-overflow-tooltip prop="description" :sortable="enableColumnSort ? 'custom' : false" header-class-name="sortable-right-header">
              <template #default="{ row }">
                {{ row.description?.substring(0, 20) || row.title?.substring(0, 20) || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="下达人" width="90" prop="assignerName" :sortable="enableColumnSort ? 'custom' : false" header-class-name="sortable-right-header">
              <template #default="{ row }">{{ row.assignerName }}</template>
            </el-table-column>
            <el-table-column label="执行人" width="90" prop="executorName" :sortable="enableColumnSort ? 'custom' : false" header-class-name="sortable-right-header">
              <template #default="{ row }">{{ row.executorName }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center" prop="status" :sortable="enableColumnSort ? 'custom' : false" header-class-name="sortable-right-header">
              <template #default="{ row }">
                <el-tag
                  :color="getDisplayStatus(row.status).bgColor"
                  :style="{ color: getDisplayStatus(row.status).color, border: 'none' }"
                  size="small"
                  round
                >
                  {{ getDisplayStatus(row.status).label }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="截止时间" width="150" prop="completionDeadline" :sortable="enableColumnSort ? 'custom' : false" header-class-name="sortable-right-header">
              <template #default="{ row }">
                <span :class="{ 'text-danger': getTimeRemaining(row.completionDeadline).isOverdue }">
                  {{ formatDateTime(row.completionDeadline) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="taskStore.listParams.page"
            v-model:page-size="taskStore.listParams.pageSize"
            :total="taskStore.listTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            small
            @size-change="handleSearch"
            @current-change="handleSearch"
          />
        </div>
      </el-card>

      <!-- 右侧：详情/新建面板 -->
      <div class="detail-panel">
        <!-- 新建事务表单 -->
        <template v-if="panelMode === 'create'">
          <el-card class="detail-panel-card create-panel-card">
            <div class="panel-header">
              <h4 class="create-title">新建事务</h4>
              <el-button text circle @click="panelMode = null">
                <el-icon :size="18"><Close /></el-icon>
              </el-button>
            </div>
            <el-form
              ref="createFormRef"
              :model="createForm"
              :rules="taskFormRules"
              label-position="top"
              class="create-form"
            >
              <el-form-item label="事务描述" prop="description">
                <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请详细描述事务内容" maxlength="2000" show-word-limit />
              </el-form-item>
              <el-form-item label="事务紧急性" prop="urgencyType">
                <el-radio-group v-model="createForm.urgencyType" class="urgency-group">
                  <el-radio-button v-for="opt in urgencyOptions" :key="opt.value" :value="opt.value" :class="['urgency-btn', `urgency-${opt.value}`]">{{ opt.label }}</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="截止时间" prop="completionDeadline">
                <div class="deadline-input">
                  <div class="deadline-presets">
                    <el-button size="small" @click="setCreatePresetDeadline(30, 'minute')">半个小时</el-button>
                    <el-button size="small" @click="setCreatePresetDeadline(1, 'hour')">一个小时</el-button>
                    <el-button size="small" @click="setCreatePresetDeadline(1, 'day')">一天</el-button>
                    <el-button size="small" @click="setCreatePresetDeadline(1, 'week')">一周</el-button>
                  </div>
                  <el-date-picker
                    v-model="createForm.completionDeadline"
                    type="datetime"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                    placeholder="请选择截止时间"
                    style="width: 100%"
                    :shortcuts="deadlineShortcuts"
                  />
                </div>
              </el-form-item>
              <el-form-item label="执行人" prop="executorId">
                <div class="executor-selector">
                  <div v-if="createSelectedExecutor" class="selected-executor" @click="showPersonPicker = true">
                    <el-avatar :size="28">{{ createSelectedExecutor.name.charAt(0) }}</el-avatar>
                    <div>
                      <div class="executor-name">{{ createSelectedExecutor.name }}</div>
                      <div class="executor-dept">{{ getMemberDeptText(createSelectedExecutor) }}</div>
                    </div>
                    <el-icon class="change-icon"><Edit /></el-icon>
                  </div>
                  <el-button v-else size="small" @click="showPersonPicker = true">
                    <el-icon><Plus /></el-icon>选择执行人
                  </el-button>
                </div>
              </el-form-item>
              <el-form-item label="附件">
                <el-upload v-model:file-list="createFileList" action="#" :auto-upload="false" :limit="10" multiple :on-preview="handleUploadPreview">
                  <el-button type="primary" plain size="small"><el-icon><UploadFilled /></el-icon>选择文件</el-button>
                  <template #file="{ file }">
                    <div class="upload-file-row">
                      <span class="upload-file-name">{{ file.name }}</span>
                      <div class="upload-file-actions">
                        <el-button link type="primary" size="small" @click="handleUploadPreview(file)">预览</el-button>
                        <el-button link type="primary" size="small" @click="handleUploadDownload(file)">下载</el-button>
                        <el-button link type="danger" size="small" @click="removeCreateUploadFile(file)">取消</el-button>
                      </div>
                    </div>
                  </template>
                  <template #tip><div class="el-upload__tip">最多10个文件</div></template>
                </el-upload>
              </el-form-item>
            </el-form>
            <div class="create-actions">
              <el-button @click="panelMode = null">取消</el-button>
              <el-button type="primary" :loading="createSubmitting" @click="handleCreateSubmit">
                <el-icon><Promotion /></el-icon>下达事务
              </el-button>
            </div>
          </el-card>
        </template>
        <!-- 事务详情 -->
        <template v-else-if="selectedTask">
          <el-card class="detail-panel-card" v-loading="detailLoading">
            <!-- 面板头部 -->
            <div class="panel-header">
              <div v-if="showSimpleHeader" class="panel-header-left">
                <span class="panel-header-title">事务详情</span>
              </div>
              <div v-else class="panel-header-left">
                <div class="level-badge-md" :style="{ background: TASK_LEVEL_MAP[selectedTask.level as TaskLevel]?.color }">
                  {{ selectedTask.level }}
                </div>
                <div>
                  <div class="panel-task-id">{{ selectedTask.id }}</div>
                  <el-tag
                    :color="getDisplayStatus(selectedTask.status).bgColor"
                    :style="{ color: getDisplayStatus(selectedTask.status).color, border: 'none' }"
                    size="small" round
                  >
                    {{ getDisplayStatus(selectedTask.status).label }}
                  </el-tag>
                </div>
              </div>
              <el-button text circle @click="selectedTask = null">
                <el-icon :size="18"><Close /></el-icon>
              </el-button>
            </div>

            <!-- 1. 事务描述 -->
            <div class="panel-section">
              <div class="panel-label">事务描述</div>
              <p class="panel-desc">{{ selectedTask.description || selectedTask.title }}</p>
            </div>

            <!-- 1.2 附件 -->
            <div class="panel-section">
              <div class="panel-label">附件</div>
              <template v-if="selectedTask.attachments && selectedTask.attachments.length">
                <div class="attachment-scroll">
                  <div v-for="file in selectedTask.attachments" :key="file.id" class="panel-file">
                    <el-icon :size="16" color="#4F6EF7"><Document /></el-icon>
                    <span class="file-name">{{ file.name }}</span>
                    <el-button link type="primary" size="small" @click="openPreview(file.id, file.name, file.type)">预览</el-button>
                    <el-button link type="primary" size="small" @click="openDownload(file.id)">下载</el-button>
                  </div>
                </div>
              </template>
              <p v-else class="empty-hint">暂无附件</p>
            </div>

            <!-- 1.3 流程动态（所有角色统一显示） -->
            <div class="panel-section">
              <div class="panel-label">流程动态</div>
              <template v-if="flowNodes.length">
                <div class="flow-timeline">
                  <div
                    v-for="(node, idx) in flowNodes"
                    :key="idx"
                    class="flow-node"
                  >
                    <div class="flow-dot" :style="{ background: node.color }" />
                    <div v-if="idx < flowNodes.length - 1" class="flow-line" />
                    <div class="flow-content">
                      <div class="flow-text">
                        <span class="flow-name">{{ node.operator }}</span>
                        <span class="flow-action">{{ node.action }}</span>
                        <span v-if="node.target" class="flow-name">{{ node.target }}</span>
                      </div>
                      <div class="flow-time">{{ node.time }}</div>
                    </div>
                  </div>
                </div>
              </template>
              <p v-else class="empty-hint">暂无流程动态</p>
            </div>

            <!-- 1.5 任务信息（我的代办中隐藏） -->
            <div v-if="!showSimpleHeader && !isMyTodo" class="panel-section">
              <div class="panel-label">任务信息</div>
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-key">下达人</span>
                  <span class="info-val">{{ selectedTask.assignerName }}</span>
                </div>
                <div class="info-item">
                  <span class="info-key">下达部门</span>
                  <span class="info-val">{{ selectedTask.assignerDept }}</span>
                </div>
                <div class="info-item">
                  <span class="info-key">执行人</span>
                  <span class="info-val">{{ selectedTask.executorName }}</span>
                </div>
                <div class="info-item">
                  <span class="info-key">截止时间</span>
                  <span class="info-val" :class="{ 'text-danger': getTimeRemaining(selectedTask.completionDeadline).isOverdue }">
                    {{ formatDateTime(selectedTask.completionDeadline) }}
                  </span>
                </div>
              </div>
            </div>

            <!-- —— 经理代办专属布局 —— -->
            <template v-if="isManagerReceived">
              <!-- 已提交/已终结：保留已提交内容只读 + 状态横幅 -->
              <template v-if="isReceiverDone">
                <!-- 已提交的内容（只读） -->
                <!-- 已转交：显示下级提交的实际工作内容 -->
                <div v-if="hasDelegated && effectiveChildSubmissions.length" class="panel-section">
                  <div class="panel-label">下级提交内容</div>
                  <div v-for="sub in effectiveChildSubmissions" :key="sub.id" class="submission-block">
                    <div class="submission-author">
                      <el-avatar :size="24">{{ sub.operatorName.charAt(0) }}</el-avatar>
                      <span>{{ sub.operatorName }}</span>
                      <span class="submission-time">{{ formatDateTime(sub.createdAt) }}</span>
                    </div>
                    <p class="submission-desc">{{ sub.content }}</p>
                    <div v-if="sub.attachments.length" class="submission-files">
                      <div v-for="file in sub.attachments" :key="file.id" class="panel-file">
                        <el-icon :size="16" color="#4F6EF7"><Document /></el-icon>
                        <span class="file-name">{{ file.name }}</span>
                        <el-button link type="primary" size="small" @click="openPreview(file.id, file.name, file.type)">预览</el-button>
                        <el-button link type="primary" size="small" @click="openDownload(file.id)">下载</el-button>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 未转交：显示自己提交的内容 -->
                <div v-else-if="directSubmissions.length" class="panel-section">
                  <div class="panel-label">已提交内容</div>
                  <div v-for="sub in directSubmissions" :key="sub.id" class="submission-block">
                    <div class="submission-author">
                      <el-avatar :size="24">{{ sub.operatorName.charAt(0) }}</el-avatar>
                      <span>{{ sub.operatorName }}</span>
                      <span class="submission-time">{{ formatDateTime(sub.createdAt) }}</span>
                    </div>
                    <p class="submission-desc">{{ sub.content }}</p>
                    <div v-if="sub.attachments.length" class="submission-files">
                      <div v-for="file in sub.attachments" :key="file.id" class="panel-file">
                        <el-icon :size="16" color="#4F6EF7"><Document /></el-icon>
                        <span class="file-name">{{ file.name }}</span>
                        <el-button link type="primary" size="small" @click="openPreview(file.id, file.name, file.type)">预览</el-button>
                        <el-button link type="primary" size="small" @click="openDownload(file.id)">下载</el-button>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="panel-section">
                  <div class="action-result" :class="taskResultClass">
                    <el-icon :size="18"><CircleCheck v-if="!['rejected', 'cancelled'].includes(selectedTask.status)" /><CircleClose v-else /></el-icon>
                    {{ taskResultText }}
                  </div>
                </div>
                <div v-if="selectedTask.status === 'rejected' && rejectReasonText" class="panel-section">
                  <div class="panel-label">不通过理由</div>
                  <p class="reject-reason-text">{{ rejectReasonText }}</p>
                </div>
                <div v-if="selectedTask.status === 'cancelled' && cancelReasonText" class="panel-section">
                  <div class="panel-label">作废原因</div>
                  <p class="reject-reason-text">{{ cancelReasonText }}</p>
                </div>
              </template>

              <!-- 已转交、等待下级提交 -->
              <template v-else-if="hasDelegated && !childHasSubmitted">
                <div class="panel-section">
                  <div class="action-result result-pending">
                    <el-icon :size="18"><Clock /></el-icon>
                    已转交，等待下级提交
                  </div>
                </div>
              </template>

              <!-- 下级已提交、可提交给上级 -->
              <template v-else-if="hasDelegated && childHasSubmitted">
                <!-- 下级提交内容 -->
                <div class="panel-section">
                  <div class="panel-label">下级提交内容</div>
                  <div v-for="sub in effectiveChildSubmissions" :key="sub.id" class="submission-block">
                    <div class="submission-author">
                      <el-avatar :size="24">{{ sub.operatorName.charAt(0) }}</el-avatar>
                      <span>{{ sub.operatorName }}</span>
                      <span class="submission-time">{{ formatDateTime(sub.createdAt) }}</span>
                    </div>
                    <p class="submission-desc">{{ sub.content }}</p>
                    <div v-if="sub.attachments.length" class="submission-files">
                      <div v-for="file in sub.attachments" :key="file.id" class="panel-file">
                        <el-icon :size="16" color="#4F6EF7"><Document /></el-icon>
                        <span class="file-name">{{ file.name }}</span>
                        <el-button link type="primary" size="small" @click="openPreview(file.id, file.name, file.type)">预览</el-button>
                        <el-button link type="primary" size="small" @click="openDownload(file.id)">下载</el-button>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 提交按钮 -->
                <div class="panel-section">
                  <el-button type="primary" style="width: 100%" :loading="submittingInline" @click="handleDelegatedSubmit">
                    <el-icon><Upload /></el-icon>提交给上级
                  </el-button>
                </div>
              </template>

              <!-- 未转交：可直接提交 或 转交给下级 -->
              <template v-else>
                <div class="panel-section">
                  <div class="action-tab-bar">
                    <span :class="['action-tab', { active: managerActionMode === 'submit' }]" @click="managerActionMode = 'submit'">提交内容</span>
                    <span :class="['action-tab', { active: managerActionMode === 'delegate' }]" @click="managerActionMode = 'delegate'">转交任务</span>
                  </div>
                </div>
                <!-- 提交表单 -->
                <div v-if="managerActionMode === 'submit'" class="panel-section">
                  <el-form label-position="top" class="inline-submit-form">
                    <el-form-item label="附件">
                      <el-upload v-model:file-list="submitFileList" action="#" :auto-upload="false" :limit="5" multiple :on-preview="handleUploadPreview">
                        <el-button type="primary" plain size="small"><el-icon><UploadFilled /></el-icon>选择文件</el-button>
                        <template #file="{ file }">
                          <div class="upload-file-row">
                            <span class="upload-file-name">{{ file.name }}</span>
                            <div class="upload-file-actions">
                              <el-button link type="primary" size="small" @click="handleUploadPreview(file)">预览</el-button>
                              <el-button link type="primary" size="small" @click="handleUploadDownload(file)">下载</el-button>
                              <el-button link type="danger" size="small" @click="removeSubmitUploadFile(file)">取消</el-button>
                            </div>
                          </div>
                        </template>
                        <template #tip><div class="el-upload__tip">最多5个文件</div></template>
                      </el-upload>
                    </el-form-item>
                    <el-form-item label="事务反馈">
                      <el-input v-model="submitContent" type="textarea" :rows="2" placeholder="请输入事务反馈" />
                    </el-form-item>
                  </el-form>
                  <el-button type="primary" style="width: 100%; margin-top: 4px" :disabled="!submitContent.trim()" :loading="submittingInline" @click="handleInlineSubmit">
                    <el-icon><Upload /></el-icon>提交
                  </el-button>
                </div>
                <!-- 转交表单 -->
                <div v-if="managerActionMode === 'delegate'" class="panel-section">
                  <el-form label-position="top" class="inline-delegate-form">
                    <el-form-item label="执行人">
                      <div class="executor-selector">
                        <div v-if="delegateSelectedExecutor" class="selected-executor" @click="showDelegatePicker = true">
                          <el-avatar :size="28">{{ delegateSelectedExecutor.name.charAt(0) }}</el-avatar>
                          <div>
                            <div class="executor-name">{{ delegateSelectedExecutor.name }}</div>
                            <div class="executor-dept">{{ delegateSelectedExecutor.deptName }}</div>
                          </div>
                          <el-icon class="change-icon"><Edit /></el-icon>
                        </div>
                        <el-button v-else size="small" @click="showDelegatePicker = true">
                          <el-icon><Plus /></el-icon>选择执行人
                        </el-button>
                      </div>
                    </el-form-item>
                  </el-form>
                  <el-button type="warning" style="width: 100%; margin-top: 4px" :disabled="!delegateExecutorId" :loading="delegatingInline" @click="handleInlineDelegate">
                    <el-icon><Promotion /></el-icon>转交
                  </el-button>
                </div>
              </template>
            </template>

            <!-- —— 员工代办专属布局 —— -->
            <template v-else-if="isStaffReceived">
              <!-- 区域2：事务反馈 + 附件 + 提交 -->
              <div v-if="!isReceiverDone" class="panel-section">
                <div class="panel-label">提交内容</div>
                <el-form label-position="top" class="inline-submit-form">
                  <el-form-item label="事务反馈">
                    <el-input v-model="submitContent" type="textarea" :rows="2" placeholder="请输入事务反馈" />
                  </el-form-item>
                  <el-form-item label="附件">
                    <el-upload v-model:file-list="submitFileList" action="#" :auto-upload="false" :limit="10" multiple :on-preview="handleUploadPreview">
                      <el-button type="primary" plain size="small"><el-icon><UploadFilled /></el-icon>选择文件</el-button>
                      <template #file="{ file }">
                        <div class="upload-file-row">
                          <span class="upload-file-name">{{ file.name }}</span>
                          <div class="upload-file-actions">
                            <el-button link type="primary" size="small" @click="handleUploadPreview(file)">预览</el-button>
                            <el-button link type="primary" size="small" @click="handleUploadDownload(file)">下载</el-button>
                            <el-button link type="danger" size="small" @click="removeSubmitUploadFile(file)">取消</el-button>
                          </div>
                        </div>
                      </template>
                      <template #tip><div class="el-upload__tip">最多10个文件</div></template>
                    </el-upload>
                  </el-form-item>
                </el-form>
                <el-button type="primary" style="width: 100%; margin-top: 4px" :disabled="!submitContent.trim()" :loading="submittingInline" @click="handleInlineSubmit">
                  <el-icon><Upload /></el-icon>提交
                </el-button>
              </div>

              <!-- 状态提示 + 已提交内容只读 -->
              <template v-if="isReceiverDone">
                <div v-if="directSubmissions.length" class="panel-section">
                  <div class="panel-label">已提交内容</div>
                  <div v-for="sub in directSubmissions" :key="sub.id" class="submission-block">
                    <div class="submission-author">
                      <el-avatar :size="24">{{ sub.operatorName.charAt(0) }}</el-avatar>
                      <span>{{ sub.operatorName }}</span>
                      <span class="submission-time">{{ formatDateTime(sub.createdAt) }}</span>
                    </div>
                    <p class="submission-desc">{{ sub.content }}</p>
                    <div v-if="sub.attachments.length" class="submission-files">
                      <div v-for="file in sub.attachments" :key="file.id" class="panel-file">
                        <el-icon :size="16" color="#4F6EF7"><Document /></el-icon>
                        <span class="file-name">{{ file.name }}</span>
                        <el-button link type="primary" size="small" @click="openPreview(file.id, file.name, file.type)">预览</el-button>
                        <el-button link type="primary" size="small" @click="openDownload(file.id)">下载</el-button>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="panel-section">
                  <div class="action-result" :class="taskResultClass">
                      <el-icon :size="18"><CircleCheck v-if="!['rejected', 'cancelled'].includes(selectedTask.status)" /><CircleClose v-else /></el-icon>
                    {{ taskResultText }}
                  </div>
                </div>
                <div v-if="selectedTask.status === 'rejected' && rejectReasonText" class="panel-section">
                  <div class="panel-label">不通过理由</div>
                  <p class="reject-reason-text">{{ rejectReasonText }}</p>
                </div>
                <div v-if="selectedTask.status === 'cancelled' && cancelReasonText" class="panel-section">
                  <div class="panel-label">作废原因</div>
                  <p class="reject-reason-text">{{ cancelReasonText }}</p>
                </div>
              </template>
            </template>

            <!-- —— 其他角色的通用布局 —— -->
            <template v-else>
              <!-- 3. 事务反馈（包含所有提交记录） -->
              <div class="panel-section">
                <div class="panel-label">事务反馈</div>
                <template v-if="effectiveAllSubmissions.length">
                  <div v-for="sub in effectiveAllSubmissions" :key="sub.id" class="submission-block">
                    <div class="submission-author">
                      <el-avatar :size="24">{{ sub.operatorName.charAt(0) }}</el-avatar>
                      <span>{{ sub.operatorName }}</span>
                      <span class="submission-time">{{ formatDateTime(sub.createdAt) }}</span>
                    </div>
                    <p class="submission-desc">{{ sub.content }}</p>
                    <div v-if="sub.attachments.length" class="submission-files">
                      <div v-for="file in sub.attachments" :key="file.id" class="panel-file">
                        <el-icon :size="16" color="#4F6EF7"><Document /></el-icon>
                        <span class="file-name">{{ file.name }}</span>
                        <el-button link type="primary" size="small" @click="openPreview(file.id, file.name, file.type)">预览</el-button>
                        <el-button link type="primary" size="small" @click="openDownload(file.id)">下载</el-button>
                      </div>
                    </div>
                  </div>
                </template>
                <p v-else class="empty-hint">暂无下级人员反馈事务</p>
              </div>

              <!-- 状态横幅（已完成/不通过/已作废） -->
              <div v-if="isTaskResolved" class="panel-section">
                <div class="action-result" :class="taskResultClass">
                  <el-icon :size="18"><CircleCheck v-if="!['rejected', 'cancelled'].includes(selectedTask.status)" /><CircleClose v-else /></el-icon>
                  {{ taskResultText }}
                </div>
              </div>
              <!-- 不通过理由 -->
              <div v-if="selectedTask.status === 'rejected' && rejectReasonText" class="panel-section">
                <div class="panel-label">不通过理由</div>
                <p class="reject-reason-text">{{ rejectReasonText }}</p>
              </div>
              <!-- 作废原因 -->
              <div v-if="selectedTask.status === 'cancelled' && cancelReasonText" class="panel-section">
                <div class="panel-label">作废原因</div>
                <p class="reject-reason-text">{{ cancelReasonText }}</p>
              </div>

            </template>

            <!-- 5. 操作按钮（代办审批角色） -->
            <div v-if="canReviewTodoTask" class="panel-section panel-actions">
              <template v-if="isTaskResolved && !isManagerReceived && !isStaffReceived">
                <div class="action-result" :class="selectedTask.status === 'rejected' ? 'result-reject' : selectedTask.status === 'cancelled' ? 'result-cancel' : 'result-approve'">
                  <el-icon :size="18"><CircleCheck v-if="!['rejected', 'cancelled'].includes(selectedTask.status)" /><CircleClose v-else /></el-icon>
                  {{ selectedTask.status === 'rejected' ? '已标记为不通过' : selectedTask.status === 'cancelled' ? '已标记为作废' : '已标记为完成' }}
                </div>
              </template>
              <template v-else-if="isTaskResolved">
                <!-- 已由经理/员工代办区显示状态，此处不重复 -->
              </template>
              <template v-else-if="selectedTask.status === 'submitted'">
                <el-button type="success" style="flex: 1" :loading="approving" @click="handleApprove">
                  <el-icon><CircleCheck /></el-icon>通过
                </el-button>
                <el-button type="danger" style="flex: 1" :loading="rejecting" @click="rejectDialogVisible = true">
                  <el-icon><CircleClose /></el-icon>不通过
                </el-button>
                <el-button type="info" style="flex: 1" @click="handleCancel">
                  <el-icon><Close /></el-icon>作废
                </el-button>
              </template>
              <template v-else>
                <div class="action-result result-pending">
                  <el-icon :size="18"><Clock /></el-icon>
                  等待提交
                </div>
              </template>
            </div>
            <!-- 事务列表（中级管理者/manager）：提交给上级（已移入中级管理者列表专属布局） -->
            <!-- 事务代办（普通员工）：提交（已移入员工代办专属布局） -->
          </el-card>
        </template>
        <template v-else-if="panelMode !== 'create'">
          <el-card class="detail-panel-card empty-panel">
            <el-empty description="点击任意事务行查看详情" :image-size="80" />
          </el-card>
        </template>
      </div>
    </div>

    <!-- 不通过弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="不通过理由" width="420px" append-to-body>
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入不通过理由" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :disabled="!rejectReason.trim()" @click="handleReject">确认</el-button>
      </template>
    </el-dialog>

    <!-- 提交弹窗 -->
    <el-dialog v-model="submitDialogVisible" title="提交" width="500px" append-to-body>
      <el-form label-position="top">
        <el-form-item label="说明">
          <el-input v-model="submitContent" type="textarea" :rows="4" placeholder="请输入提交说明" />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            v-model:file-list="submitFileList"
            action="#"
            :auto-upload="false"
            :limit="5"
            multiple
            :on-preview="handleUploadPreview"
          >
            <el-button type="primary" plain>
              <el-icon><UploadFilled /></el-icon>选择文件
            </el-button>
            <template #file="{ file }">
              <div class="upload-file-row">
                <span class="upload-file-name">{{ file.name }}</span>
                <div class="upload-file-actions">
                  <el-button link type="primary" size="small" @click="handleUploadPreview(file)">预览</el-button>
                  <el-button link type="primary" size="small" @click="handleUploadDownload(file)">下载</el-button>
                  <el-button link type="danger" size="small" @click="removeSubmitUploadFile(file)">取消</el-button>
                </div>
              </div>
            </template>
            <template #tip>
              <div class="el-upload__tip">最多上传5个文件</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!submitContent.trim()" @click="handleSubmitTask">确认提交</el-button>
      </template>
    </el-dialog>

    <!-- 转交弹窗 -->
    <el-dialog v-model="delegateDialogVisible" title="转交任务" width="420px" append-to-body>
      <el-form label-position="top">
        <el-form-item label="选择执行人">
          <el-select v-model="delegateExecutorId" placeholder="请选择执行人" style="width: 100%">
            <el-option
              v-for="u in subordinates"
              :key="u.id"
              :label="`${u.name} — ${u.deptName}`"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="delegateDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!delegateExecutorId" @click="handleDelegate">确认转交</el-button>
      </template>
    </el-dialog>

    <!-- 执行人选择弹窗 -->
    <el-dialog v-model="showPersonPicker" title="选择执行人" width="420px" destroy-on-close append-to-body>
      <div class="person-list">
        <div
          v-for="member in orgStore.selectableMembers"
          :key="member.id"
          class="person-item"
          :class="{ active: createForm.executorId === member.id }"
          @click="selectExecutor(member)"
        >
          <el-avatar :size="32">{{ member.name.charAt(0) }}</el-avatar>
          <div class="person-info">
            <div class="person-name">{{ member.name }}</div>
            <div class="person-dept">{{ getMemberDeptText(member) }}</div>
          </div>
          <el-icon v-if="createForm.executorId === member.id" color="#4F6EF7"><CircleCheck /></el-icon>
        </div>
        <el-empty v-if="orgStore.selectableMembers.length === 0" description="无可选下级人员" />
      </div>
    </el-dialog>

    <!-- 转交执行人选择弹窗 -->
    <el-dialog v-model="showDelegatePicker" title="选择执行人" width="420px" destroy-on-close append-to-body>
      <div class="person-list">
        <div
          v-for="member in subordinates"
          :key="member.id"
          class="person-item"
          :class="{ active: delegateExecutorId === member.id }"
          @click="selectDelegateExecutor(member)"
        >
          <el-avatar :size="32">{{ member.name.charAt(0) }}</el-avatar>
          <div class="person-info">
            <div class="person-name">{{ member.name }}</div>
            <div class="person-dept">{{ member.deptName }} · {{ member.position }}</div>
          </div>
          <el-icon v-if="delegateExecutorId === member.id" color="#4F6EF7"><CircleCheck /></el-icon>
        </div>
        <el-empty v-if="subordinates.length === 0" description="无可选下级人员" />
      </div>
    </el-dialog>

    <AttachmentPreviewDialog
      :visible="previewVisible"
      :url="previewUrl"
      :title="previewTitle"
      :mime-type="previewMimeType"
      @update:visible="handlePreviewVisibleChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import AttachmentPreviewDialog from '@/components/AttachmentPreviewDialog.vue'
import { useUserStore, useTaskStore, useOrganizationStore } from '@/stores'
import { TASK_LEVEL_MAP, PROCESS_ACTION_MAP } from '@/utils/constants'
import { formatDateTime, getTimeRemaining } from '@/utils/format'
import { getSubordinatesApi } from '@/api/organization'
import { buildPreviewUrl, buildDownloadUrl } from '@/api/file'
import { taskFormRules } from '@/utils/validate'
import { uploadAttachments, getUploadFilePreview, downloadUploadFile } from '@/utils/attachment'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Task, TaskLevel, TaskProcessRecord, User, TaskUrgencyType } from '@/types'
import type { FormInstance, UploadUserFile, UploadFile } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const taskStore = useTaskStore()
const orgStore = useOrganizationStore()

const normalizeListType = (raw?: string) => {
  if (raw === 'received') return 'todo'
  if (raw === 'all') return 'scope'
  if (raw === 'scope' || raw === 'assigned' || raw === 'todo') return raw
  if (userStore.currentRole === 'staff') return 'todo'
  return 'scope'
}

const currentType = ref<string>(normalizeListType(route.params.type as string | undefined))
const panelMode = ref<'detail' | 'create' | null>(null)
const keyword = ref('')
const statusFilter = ref('')
const levelFilter = ref('')
const selectedTask = ref<Task | null>(null)
const detailLoading = ref(false)
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const approving = ref(false)
const rejecting = ref(false)
const submitDialogVisible = ref(false)
const submitContent = ref('')
const submitFileList = ref<UploadUserFile[]>([])
const isSubmitToSuperior = ref(false)
const delegateDialogVisible = ref(false)
const delegateExecutorId = ref('')
const subordinates = ref<User[]>([])
const submittingInline = ref(false)
const delegatingInline = ref(false)
const showDelegatePicker = ref(false)
const delegateSelectedExecutor = ref<User | null>(null)
const managerActionMode = ref<'submit' | 'delegate'>('submit')

// ---- 新建事务表单 ----
const createFormRef = ref<FormInstance>()
const createSubmitting = ref(false)
const showPersonPicker = ref(false)
const createFileList = ref<UploadUserFile[]>([])
const createSelectedExecutor = ref<User | null>(null)
const previewVisible = ref(false)
const previewUrl = ref('')
const previewTitle = ref('')
const previewMimeType = ref('')
const previewAttachmentId = ref('')
const previewUploadFile = ref<UploadUserFile | null>(null)
const previewRevoke = ref<null | (() => void)>(null)
const createForm = ref({
  description: '',
  urgencyType: '' as TaskUrgencyType | '',
  completionDeadline: '',
  executorId: '',
})
const urgencyOptions: { value: TaskUrgencyType; label: string }[] = [
  { value: 'important_urgent', label: '重要紧急' },
  { value: 'important_not_urgent', label: '重要不紧急' },
  { value: 'not_important_urgent', label: '不重要紧急' },
  { value: 'not_important_not_urgent', label: '不重要不紧急' },
]
const deadlineShortcuts = [
  { text: '半个小时', value: () => dayjs().add(30, 'minute').toDate() },
  { text: '一个小时', value: () => dayjs().add(1, 'hour').toDate() },
  { text: '一天', value: () => dayjs().add(1, 'day').toDate() },
  { text: '一周', value: () => dayjs().add(1, 'week').toDate() },
]

const deptNameMap = computed<Record<string, string>>(() => {
  return orgStore.deptList.reduce((acc, dept) => {
    acc[dept.id] = dept.name
    return acc
  }, {} as Record<string, string>)
})

function getMemberDeptText(member: User) {
  const names: string[] = []
  if (member.deptName) {
    names.push(member.deptName)
  }
  if (member.managedDeptIds?.length) {
    for (const deptId of member.managedDeptIds) {
      const deptName = deptNameMap.value[deptId]
      if (deptName) {
        names.push(deptName)
      }
    }
  }
  const uniqueNames = [...new Set(names)]
  return uniqueNames.join(' / ')
}

function setCreatePresetDeadline(value: number, unit: 'minute' | 'hour' | 'day' | 'week') {
  createForm.value.completionDeadline = dayjs().add(value, unit).format('YYYY-MM-DDTHH:mm:ss')
}

const isMyAssigned = computed(() => currentType.value === 'assigned')
const isMyTodo = computed(() => currentType.value === 'todo')
const isScopeMode = computed(() => currentType.value === 'scope')
const enableColumnSort = computed(() => isScopeMode.value || isMyAssigned.value || isMyTodo.value)
const sortState = ref<{ prop: '' | 'level' | 'status' | 'completionDeadline' | 'description' | 'assignerName' | 'executorName'; order: '' | 'ascending' | 'descending' }>({
  prop: '',
  order: '',
})

const sortedTaskList = computed(() => {
  const list = [...taskStore.taskList]
  if (!enableColumnSort.value || !sortState.value.prop || !sortState.value.order) {
    return list
  }

  const direction = sortState.value.order === 'ascending' ? 1 : -1
  return list.sort((a, b) => {
    if (sortState.value.prop === 'level') {
      const rank: Record<TaskLevel, number> = { A: 1, B: 2, C: 3, D: 4 }
      return (rank[a.level] - rank[b.level]) * direction
    }

    if (sortState.value.prop === 'status') {
      return (statusSortValue(a.status) - statusSortValue(b.status)) * direction
    }

    if (sortState.value.prop === 'description') {
      return (a.description || '').localeCompare(b.description || '', 'zh') * direction
    }

    if (sortState.value.prop === 'assignerName') {
      return (a.assignerName || '').localeCompare(b.assignerName || '', 'zh') * direction
    }

    if (sortState.value.prop === 'executorName') {
      return (a.executorName || '').localeCompare(b.executorName || '', 'zh') * direction
    }

    const ta = new Date(a.completionDeadline).getTime()
    const tb = new Date(b.completionDeadline).getTime()
    return (ta - tb) * direction
  })
})

const pageTitle = computed(() => {
  if (isMyAssigned.value) return '我的下达'
  if (isMyTodo.value) return '我的代办'
  return '事务列表'
})

const canCreateTask = computed(() => {
  if (isMyTodo.value) return false
  if (userStore.isAdmin) return false
  if (userStore.currentRole === 'staff') return false
  return true
})

/** 将内部状态映射为显示状态 */
function getDisplayStatus(status: string): { label: string; color: string; bgColor: string } {
  if (['pending', 'accepted', 'in_progress'].includes(status)) {
    return { label: '待处理', color: '#F59E0B', bgColor: '#FFFBEB' }
  }
  if (status === 'submitted') {
    return { label: '待审核', color: '#3B82F6', bgColor: '#EFF6FF' }
  }
  if (['completed', 'approved'].includes(status)) {
    return { label: '已完成', color: '#22C55E', bgColor: '#ECFDF5' }
  }
  if (status === 'rejected') {
    return { label: '不通过', color: '#DC2626', bgColor: '#FEF2F2' }
  }
  if (status === 'overdue') {
    return { label: '已逾期', color: '#8B5CF6', bgColor: '#F5F3FF' }
  }
  if (status === 'cancelled') {
    return { label: '已作废', color: '#9CA3AF', bgColor: '#F3F4F6' }
  }
  return { label: '待处理', color: '#F59E0B', bgColor: '#FFFBEB' }
}

/** 将处理记录转为流程节点 */
const flowNodes = computed(() => {
  if (!selectedTask.value) return []
  const task = selectedTask.value!
  const records = taskStore.currentRecords
  const parseFlowTarget = (content?: string) => {
    if (!content) return ''
    const match = content.match(/(?:分派给|转交给|提交给|下发给|给)\s*([^，。,：:\s]+)/)
    return match ? match[1] : ''
  }
  return records.map(r => {
    const actionInfo = PROCESS_ACTION_MAP[r.action] || { label: r.action, color: '#6B7280' }
    let action = actionInfo.label
    let target = ''
    if (r.action === 'create' || r.action === 'assign') {
      action = '下发'
      target = parseFlowTarget(r.content) || task.executorName
    } else if (r.action === 'reassign') {
      action = '转交'
      target = parseFlowTarget(r.content) || '下级'
    } else if (r.action === 'submit') {
      action = '提交'
      // 优先从内容解析目标人
      target = parseFlowTarget(r.content)
      if (!target) {
        // 查找分配给此操作人的转交/下发记录，其操作人即为提交目标
        const assignRecord = records.find(rec =>
          (rec.action === 'reassign' || rec.action === 'create') &&
          rec.content?.includes(r.operatorName)
        )
        target = assignRecord ? assignRecord.operatorName : task.assignerName
      }
    } else if (r.action === 'accept') {
      action = '接收'
    } else if (r.action === 'approve') {
      action = '通过'
    } else if (r.action === 'reject') {
      action = '不通过'
    }
    return {
      operator: r.operatorName,
      action,
      target,
      color: actionInfo.color,
      time: formatDateTime(r.createdAt),
    }
  })
})

/** 仅当前任务直接记录的流程节点（不含子任务记录），用于高级管理者视图 */
const directFlowNodes = computed(() => {
  if (!selectedTask.value) return []
  const taskId = selectedTask.value.id
  return flowNodes.value.filter((_, idx) => {
    const r = taskStore.currentRecords[idx]
    return r.taskId === taskId
  })
})

/** 所有提交记录（包含子任务的提交，即普通员工→中级管理者→高级管理者链路） */
const allSubmissions = computed((): TaskProcessRecord[] => {
  const records = taskStore.currentRecords
  return records.filter(r => r.action === 'submit')
})

/** 子任务的提交记录 */
const childSubmissions = computed((): TaskProcessRecord[] => {
  if (!selectedTask.value) return []
  const records = taskStore.currentRecords
  return records.filter(r => r.action === 'submit' && r.taskId !== selectedTask.value!.id)
})

/** 合并后的下级提交内容：显示直接下级的名字 + 实际工作者的内容/附件 */
const effectiveChildSubmissions = computed((): TaskProcessRecord[] => {
  if (!selectedTask.value) return []
  const task = selectedTask.value
  const records = taskStore.currentRecords
  const childIds = task.childTaskIds || []

  // 所有子任务的提交记录
  const childSubs = records.filter(r =>
    r.action === 'submit' && r.taskId !== task.id
  )
  if (!childSubs.length) return []

  // 直接子任务的提交记录（获取直接下级名字）
  const directChildSubmit = childSubs.find(r => childIds.includes(r.taskId))
  const directName = directChildSubmit?.operatorName

  // 含有实际工作内容的提交记录（非纯转发 "提交给 xxx"）
  const realWorkSubs = childSubs.filter(r =>
    r.attachments.length > 0 || (r.content && !/^提交给\s\S+$/.test(r.content))
  )

  if (realWorkSubs.length > 0 && directName) {
    return realWorkSubs.map(sub => ({
      ...sub,
      operatorName: directName
    }))
  }

  return directChildSubmit ? [directChildSubmit] : childSubs
})

/** 仅当前任务的直接提交记录（不含子任务） */
const directSubmissions = computed((): TaskProcessRecord[] => {
  if (!selectedTask.value) return []
  const records = taskStore.currentRecords
  return records.filter(r => r.action === 'submit' && r.taskId === selectedTask.value!.id)
})

/** 提交记录中带附件的条目（用于独立展示附件区域） */
const submissionAttachments = computed(() => {
  return allSubmissions.value
    .filter(s => s.attachments.length > 0)
    .map(s => ({ sub: s, files: s.attachments }))
})

/** 合并后的所有提交记录：过滤纯转发，显示直接下级名字 + 实际工作内容 */
const effectiveAllSubmissions = computed((): TaskProcessRecord[] => {
  if (!selectedTask.value) return []
  const task = selectedTask.value
  const subs = allSubmissions.value
  if (!subs.length) return []

  // 找到有实际工作内容的提交记录（非纯转发）
  const realWorkSubs = subs.filter(r =>
    r.attachments.length > 0 || (r.content && !/^提交给\s\S+$/.test(r.content))
  )

  if (realWorkSubs.length > 0) {
    return realWorkSubs
  }

  return subs
})

/** 合并后的附件列表 */
const effectiveSubmissionAttachments = computed(() => {
  return effectiveAllSubmissions.value
    .filter(s => s.attachments.length > 0)
    .map(s => ({ sub: s, files: s.attachments }))
})

/** 仅当前任务的直接提交附件（不含子任务） */
const directSubmissionAttachments = computed(() => {
  return directSubmissions.value
    .filter(s => s.attachments.length > 0)
    .map(s => ({ sub: s, files: s.attachments }))
})

/** 任务是否已终结（通过/不通过/已完成/已取消），用于高级管理者审批按钮 */
const isTaskResolved = computed(() => {
  if (!selectedTask.value) return true
  return ['completed', 'approved', 'rejected', 'cancelled'].includes(selectedTask.value.status)
})

/** 代办方是否已完成（含已提交），用于经理/员工代办视图禁用按钮 */
const isReceiverDone = computed(() => {
  if (!selectedTask.value) return true
  return ['submitted', 'completed', 'approved', 'rejected', 'cancelled'].includes(selectedTask.value.status)
})

/** 是否已下发给下级 */
const hasDelegated = computed(() => {
  return (selectedTask.value?.childTaskIds?.length ?? 0) > 0
})

/** 下级是否已提交（子任务中有 submit 记录） */
const childHasSubmitted = computed(() => {
  if (!hasDelegated.value || !selectedTask.value) return false
  const childIds = selectedTask.value.childTaskIds
  return allSubmissions.value.some(s => childIds.includes(s.taskId))
})

/** 驳回原因文本（从流程记录中提取，只保留纯理由） */
const rejectReasonText = computed(() => {
  const records = taskStore.currentRecords
  const rejectRecord = [...records].reverse().find(r => r.action === 'reject')
  const content = rejectRecord?.content || ''
  // 后端格式: "驳回，原因：xxx，评语：xxx" — 提取原因部分
  const m = content.match(/原因[:：]\s*([^，,]+)/)
  return m ? m[1].trim() : content.replace(/^驳回[，,]?\s*/, '').trim()
})

/** 作废原因文本（从流程记录中提取，只保留纯理由） */
const cancelReasonText = computed(() => {
  const records = taskStore.currentRecords
  const cancelRecord = [...records].reverse().find(r => r.action === 'cancel')
  const content = cancelRecord?.content || ''
  // 后端格式: "取消事务，原因：xxx" — 提取原因部分
  const m = content.match(/原因[:：]\s*(.+)$/)
  return m ? m[1].trim() : content.replace(/^取消事务[，,]?\s*/, '').trim()
})

/** 任务终结状态文本 */
const taskResultText = computed(() => {
  if (!selectedTask.value) return ''
  const status = selectedTask.value.status
  if (status === 'submitted') return '待审核'
  if (status === 'completed' || status === 'approved') return '已完成'
  if (status === 'rejected') return '不通过'
  if (status === 'cancelled') return '已作废'
  return ''
})

/** 任务终结状态样式 */
const taskResultClass = computed(() => {
  if (!selectedTask.value) return ''
  const status = selectedTask.value.status
  if (status === 'rejected') return 'result-reject'
  if (status === 'cancelled') return 'result-cancel'
  if (status === 'submitted') return 'result-pending'
  return 'result-approve'
})

/** 是否可在“我的代办”中执行通过/不通过 */
const canReviewTodoTask = computed(() => {
  if (!selectedTask.value || !isMyTodo.value) return false
  const uid = userStore.userInfo?.id
  if (!uid) return false
  if (userStore.isDirector) return selectedTask.value.assignerId === uid && !selectedTask.value.parentTaskId
  if (userStore.currentRole === 'manager') return selectedTask.value.assignerId === uid && !selectedTask.value.parentTaskId
  return false
})

/** 是否为非员工代办视图（经理/主管/CEO 作为执行人） */
const isManagerReceived = computed(() => {
  return isMyTodo.value && userStore.currentRole !== 'staff'
})

/** 是否为经理事务列表视图 */
const isManagerAssigned = computed(() => {
  return currentType.value === 'assigned' && userStore.currentRole === 'manager'
})

/** 是否为员工代办视图 */
const isStaffReceived = computed(() => {
  return isMyTodo.value && userStore.currentRole === 'staff'
})

/** 是否使用简洁头部（无级别/编号/状态） */
const showSimpleHeader = computed(() => {
  return isScopeMode.value || (isMyAssigned.value && userStore.isDirector) || isManagerReceived.value || isManagerAssigned.value || isStaffReceived.value
})

/** 显示新建事务面板 */
function showCreatePanel() {
  selectedTask.value = null
  panelMode.value = 'create'
  createForm.value = { description: '', urgencyType: '', completionDeadline: '', executorId: '' }
  createSelectedExecutor.value = null
  createFileList.value = []
  orgStore.fetchSelectableMembers(undefined, 'subordinates')
}

function selectExecutor(member: User) {
  createForm.value.executorId = member.id
  createSelectedExecutor.value = member
  showPersonPicker.value = false
}

async function handleCreateSubmit() {
  if (!createFormRef.value) return
  await createFormRef.value.validate()
  createSubmitting.value = true
  try {
    const uploadedAttachments = await uploadAttachments(createFileList.value)
    await taskStore.createTask({
      description: createForm.value.description,
      urgencyType: createForm.value.urgencyType as TaskUrgencyType,
      completionDeadline: createForm.value.completionDeadline,
      executorId: createForm.value.executorId,
      attachments: uploadedAttachments,
    })
    ElMessage.success('事务下达成功')
    panelMode.value = null
    handleSearch()
  } finally {
    createSubmitting.value = false
  }
}

/** 点击详情按钮 */
async function showDetail(row: Task) {
  panelMode.value = 'detail'
  detailLoading.value = true
  // 重置内联表单状态
  submitContent.value = ''
  submitFileList.value = []
  delegateExecutorId.value = ''
  delegateSelectedExecutor.value = null
  try {
    await taskStore.fetchTaskDetail(row.id)
    selectedTask.value = taskStore.currentTask
    // 非员工代办视图时预加载下属列表
    if (isMyTodo.value && userStore.currentRole !== 'staff') {
      try {
        const res = await getSubordinatesApi()
        subordinates.value = res.data
      } catch {
        subordinates.value = []
      }
    }
  } finally {
    detailLoading.value = false
  }
}

function handleRowClick(row: Task) {
  showDetail(row)
}

async function handleApprove() {
  if (!selectedTask.value) return
  approving.value = true
  try {
    await taskStore.approveTask(selectedTask.value.id, '审核通过')
    ElMessage.success('已通过')
    // 重新加载详情以刷新流程动态和状态
    await taskStore.fetchTaskDetail(selectedTask.value.id)
    selectedTask.value = { ...taskStore.currentTask! }
    handleSearch()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    approving.value = false
  }
}

async function handleReject() {
  if (!selectedTask.value || !rejectReason.value.trim()) return
  rejecting.value = true
  try {
    await taskStore.rejectTask(selectedTask.value.id, rejectReason.value, rejectReason.value)
    ElMessage.success('已不通过')
    rejectDialogVisible.value = false
    rejectReason.value = ''
    // 重新加载详情以刷新流程动态和状态
    await taskStore.fetchTaskDetail(selectedTask.value.id)
    selectedTask.value = { ...taskStore.currentTask! }
    handleSearch()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    rejecting.value = false
  }
}

async function handleCancel() {
  if (!selectedTask.value) return
  try {
    const { value } = await ElMessageBox.prompt('请输入作废原因', '作废事务', {
      inputPlaceholder: '请输入作废原因',
      confirmButtonText: '确认作废',
      cancelButtonText: '取消',
      inputValidator: (val) => !!val?.trim() || '作废原因不能为空',
    })
    await taskStore.cancelTask(selectedTask.value.id, value)
    ElMessage.success('已作废')
    await taskStore.fetchTaskDetail(selectedTask.value.id)
    selectedTask.value = { ...taskStore.currentTask! }
    handleSearch()
  } catch {
    // 用户取消或请求失败
  }
}

/** 打开普通提交弹窗 */
function openNormalSubmit() {
  isSubmitToSuperior.value = false
  submitContent.value = ''
  submitFileList.value = []
  submitDialogVisible.value = true
}

/** 打开提交给上级弹窗（中级管理者在事务列表中使用） */
function openSubmitToSuperior() {
  isSubmitToSuperior.value = true
  submitContent.value = ''
  submitFileList.value = []
  submitDialogVisible.value = true
}

async function handleSubmitTask() {
  if (!selectedTask.value || !submitContent.value.trim()) return
  // 如果是提交给上级，则提交到父任务
  const taskId = isSubmitToSuperior.value && selectedTask.value.parentTaskId
    ? selectedTask.value.parentTaskId
    : selectedTask.value.id
  try {
    const uploadedAttachments = await uploadAttachments(submitFileList.value)
    await taskStore.submitResult(taskId, {
      content: submitContent.value,
      attachments: uploadedAttachments,
    })
    ElMessage.success('提交成功')
    submitDialogVisible.value = false
    submitContent.value = ''
    submitFileList.value = []
    isSubmitToSuperior.value = false
    await taskStore.fetchTaskDetail(selectedTask.value.id)
    selectedTask.value = taskStore.currentTask
    handleSearch()
  } catch {
    ElMessage.error('提交失败')
  }
}

async function openDelegateDialog() {
  delegateExecutorId.value = ''
  try {
    const res = await getSubordinatesApi()
    subordinates.value = res.data
  } catch {
    subordinates.value = []
  }
  delegateDialogVisible.value = true
}

async function handleDelegate() {
  if (!selectedTask.value || !delegateExecutorId.value) return
  try {
    await taskStore.reassignTask(selectedTask.value.id, delegateExecutorId.value)
    ElMessage.success('转交成功')
    delegateDialogVisible.value = false
    delegateExecutorId.value = ''
    delegateSelectedExecutor.value = null
    await taskStore.fetchTaskDetail(selectedTask.value.id)
    selectedTask.value = taskStore.currentTask
    handleSearch()
  } catch {
    ElMessage.error('转交失败')
  }
}

function selectDelegateExecutor(member: User) {
  delegateExecutorId.value = member.id
  delegateSelectedExecutor.value = member
  showDelegatePicker.value = false
}

/** 内联提交（经理代办面板内） */
async function handleInlineSubmit() {
  if (!selectedTask.value || !submitContent.value.trim()) return
  submittingInline.value = true
  try {
    const uploadedAttachments = await uploadAttachments(submitFileList.value)
    await taskStore.submitResult(selectedTask.value.id, {
      content: submitContent.value,
      attachments: uploadedAttachments,
    })
    ElMessage.success('提交成功')
    submitContent.value = ''
    submitFileList.value = []
    await taskStore.fetchTaskDetail(selectedTask.value.id)
    selectedTask.value = { ...taskStore.currentTask! }
    handleSearch()
  } catch {
    ElMessage.error('提交失败')
  } finally {
    submittingInline.value = false
  }
}

/** 转交任务后直接提交给上级（不需要自己填写反馈和附件） */
async function handleDelegatedSubmit() {
  if (!selectedTask.value) return
  submittingInline.value = true
  try {
    await taskStore.submitResult(selectedTask.value.id, {
      content: '',
      attachments: [],
    })
    ElMessage.success('提交成功')
    await taskStore.fetchTaskDetail(selectedTask.value.id)
    selectedTask.value = { ...taskStore.currentTask! }
    handleSearch()
  } catch {
    ElMessage.error('提交失败')
  } finally {
    submittingInline.value = false
  }
}

/** 内联下发（经理代办面板内） */
async function handleInlineDelegate() {
  if (!selectedTask.value || !delegateExecutorId.value) return
  delegatingInline.value = true
  try {
    await taskStore.reassignTask(selectedTask.value.id, delegateExecutorId.value)
    ElMessage.success('转交成功')
    delegateExecutorId.value = ''
    delegateSelectedExecutor.value = null
    await taskStore.fetchTaskDetail(selectedTask.value.id)
    selectedTask.value = { ...taskStore.currentTask! }
    handleSearch()
  } catch {
    ElMessage.error('转交失败')
  } finally {
    delegatingInline.value = false
  }
}

function handleSearch() {
  taskStore.fetchTaskList({
    type: currentType.value as any,
    keyword: keyword.value,
    status: statusFilter.value as any,
    level: levelFilter.value as any,
    page: taskStore.listParams.page,
    pageSize: taskStore.listParams.pageSize,
  })
}

function handleReset() {
  keyword.value = ''
  statusFilter.value = ''
  levelFilter.value = ''
  taskStore.listParams.page = 1
  handleSearch()
}

function handleSortChange({ prop, order }: { prop: string; order: 'ascending' | 'descending' | null }) {
  if (!enableColumnSort.value || !prop || !order) {
    sortState.value = { prop: '', order: '' }
    return
  }

  const allowedProps = ['level', 'status', 'completionDeadline', 'description', 'assignerName', 'executorName']
  if (!allowedProps.includes(prop)) {
    sortState.value = { prop: '', order: '' }
    return
  }

  sortState.value = { prop: prop as any, order }
}

function statusSortValue(status: Task['status']) {
  if (['completed', 'approved'].includes(status)) return 3
  if (status === 'overdue') return 4
  if (status === 'cancelled') return 5
  if (status === 'submitted') return 2
  return 1
}

function isPreviewable(name?: string, mimeType?: string) {
  const ext = (name || '').split('.').pop()?.toLowerCase() || ''
  const mime = (mimeType || '').toLowerCase()

  if (mime.startsWith('image/') || ['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp', 'svg'].includes(ext)) return true
  if (mime === 'application/pdf' || ext === 'pdf') return true
  if (
    mime.startsWith('text/')
    || mime === 'application/json'
    || mime === 'application/xml'
    || mime === 'application/javascript'
    || mime.endsWith('+json')
    || mime.endsWith('+xml')
    || mime === 'text/html'
    || ['txt', 'md', 'json', 'xml', 'csv', 'log', 'html', 'htm'].includes(ext)
  ) return true
  if (mime.startsWith('video/') || ['mp4', 'webm', 'ogg', 'mov'].includes(ext)) return true
  if (mime.startsWith('audio/') || ['mp3', 'wav', 'ogg', 'm4a', 'aac'].includes(ext)) return true
  return false
}

function openPreview(fileId: string, name?: string, mimeType?: string) {
  clearPreviewObjectUrl()
  previewAttachmentId.value = fileId
  previewUploadFile.value = null
  previewUrl.value = isPreviewable(name, mimeType) ? buildPreviewUrl(fileId) : ''
  previewTitle.value = name || '附件预览'
  previewMimeType.value = mimeType || ''
  previewVisible.value = true
}

function openDownload(fileId: string) {
  window.open(buildDownloadUrl(fileId), '_blank')
}

function handleUploadPreview(file: UploadFile) {
  const userFile = file as UploadUserFile
  const source = getUploadFilePreview(userFile)
  if (!source) return
  clearPreviewObjectUrl()
  previewAttachmentId.value = ''
  previewUploadFile.value = userFile
  previewUrl.value = source.url
  previewTitle.value = source.name
  previewMimeType.value = source.type
  previewRevoke.value = source.revoke
  previewVisible.value = true
}

function handleUploadDownload(file: UploadFile) {
  downloadUploadFile(file as UploadUserFile)
}

function removeCreateUploadFile(file: UploadFile) {
  const uploadFile = file as UploadUserFile
  createFileList.value = createFileList.value.filter(item => {
    if (uploadFile.uid != null && item.uid != null) {
      return item.uid !== uploadFile.uid
    }
    return !(item.name === uploadFile.name && item.size === uploadFile.size)
  })
}

function removeSubmitUploadFile(file: UploadFile) {
  const uploadFile = file as UploadUserFile
  submitFileList.value = submitFileList.value.filter(item => {
    if (uploadFile.uid != null && item.uid != null) {
      return item.uid !== uploadFile.uid
    }
    return !(item.name === uploadFile.name && item.size === uploadFile.size)
  })
}

function handlePreviewVisibleChange(visible: boolean) {
  previewVisible.value = visible
  if (!visible) {
    clearPreviewObjectUrl()
  }
}

function clearPreviewObjectUrl() {
  if (previewRevoke.value) {
    previewRevoke.value()
    previewRevoke.value = null
  }
}

watch(() => route.params.type, (newType) => {
  currentType.value = normalizeListType(newType as string | undefined)
  selectedTask.value = null
  handleSearch()
})

onMounted(async () => {
  orgStore.fetchDeptList()
  handleSearch()
  // 从通知跳转来时，自动展示对应事务详情
  const taskId = route.query.taskId as string
  if (taskId) {
    try {
      await taskStore.fetchTaskDetail(taskId)
      if (taskStore.currentTask) {
        selectedTask.value = taskStore.currentTask
        panelMode.value = 'detail'
      }
    } catch {
      // 事务不存在或无权限，忽略
    }
  }
})
</script>

<style lang="scss" scoped>
.task-list {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.filter-bar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  align-items: end;
  margin-bottom: $spacing-md;
  gap: $spacing-md;

  .filter-left {
    display: flex;
    gap: $spacing-sm;
    align-items: flex-end;

    .filter-select {
      flex: 1;
      min-width: 0;
    }
  }

  .filter-right {
    display: flex;
    align-items: flex-end;
    justify-content: flex-end;
    gap: $spacing-sm;

    .create-btn {
      height: 32px;
    }
  }
}

/* 左右分栏 */
.split-layout {
  display: flex;
  gap: $spacing-md;
  flex: 1;
  min-height: 0;
}

.list-card {
  flex: 1;
  min-width: 0;
  width: 0;
  height: 100%;
  overflow: hidden;

  :deep(.el-card__body) {
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow: hidden;
  }

  .el-table {
    flex: 1;
    overflow: auto;
  }

  .table-scroll-area {
    flex: 1;
    min-height: 0;
    overflow-x: auto;
    overflow-y: scroll;

    &::-webkit-scrollbar {
      width: 6px;
      height: 6px;
    }
    &::-webkit-scrollbar-thumb {
      background: $border-light;
      border-radius: 6px;
    }
  }

  .level-badge-sm {
    width: 26px;
    height: 26px;
    border-radius: $radius-xs;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: $font-size-xs;
    font-weight: 700;
  }

  .level-cell {
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  .level-text {
    font-size: $font-size-xs;
    color: $text-regular;
    white-space: nowrap;
  }

  .text-danger {
    color: $danger;
    font-weight: 500;
  }

  :deep(.sortable-right-header .cell) {
    /* 由非 scoped 全局样式块覆盖，此处无需重复定义 */
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: $spacing-sm;
  flex-shrink: 0;
  border-top: 1px solid $border-lighter;
}

/* 右侧详情面板 */
.detail-panel {
  flex: 1;
  min-width: 0;
  width: 0;
  height: 100%;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: $border-light;
    border-radius: 4px;
  }
}

.detail-panel-card {
  height: 100%;

  :deep(.el-card__body) {
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow-y: scroll;
    padding: 16px;

    &::-webkit-scrollbar {
      width: 6px;
    }
    &::-webkit-scrollbar-thumb {
      background: $border-light;
      border-radius: 6px;
    }
  }
}

.empty-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

/* 面板头部 */
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-sm;
  padding-bottom: $spacing-sm;
  border-bottom: 1px solid $border-lighter;
  flex-shrink: 0;
}

.panel-header-left {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.panel-header-title {
  font-size: $font-size-md;
  font-weight: 600;
  color: $text-primary;
}

.level-badge-md {
  width: 36px;
  height: 36px;
  border-radius: $radius-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: $font-size-md;
  font-weight: 700;
}

.panel-task-id {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: 4px;
}

/* 面板分区 */
.panel-section {
  margin-bottom: $spacing-sm;
  padding-bottom: $spacing-sm;
  border-bottom: 1px solid $border-lighter;

  &:last-child {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
  }
}

.panel-label {
  font-size: $font-size-sm;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 4px;
}

.panel-desc {
  font-size: $font-size-base;
  color: $text-regular;
  line-height: 1.7;
  white-space: pre-wrap;
  margin: 0;
}

.empty-hint {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin: 0;
}

/* 任务信息 */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-sm $spacing-md;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;

  .info-key {
    font-size: $font-size-xs;
    color: $text-secondary;
  }

  .info-val {
    font-size: $font-size-sm;
    color: $text-primary;
    font-weight: 500;
  }
}

/* 流程线 */
.flow-timeline {
  position: relative;
}

.flow-node {
  display: flex;
  position: relative;
  padding-left: 20px;
  padding-bottom: 16px;

  &:last-child {
    padding-bottom: 0;
    .flow-line { display: none; }
  }
}

.flow-dot {
  position: absolute;
  left: 0;
  top: 4px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
  z-index: 1;
}

.flow-line {
  position: absolute;
  left: 4px;
  top: 16px;
  bottom: 0;
  width: 2px;
  background: $border-light;
}

.flow-content {
  flex: 1;
  min-width: 0;
}

.flow-text {
  font-size: $font-size-sm;
  color: $text-regular;
  line-height: 1.5;
}

.flow-name {
  font-weight: 600;
  color: $text-primary;
}

.flow-action {
  margin: 0 4px;
  color: $text-secondary;
}

.flow-time {
  font-size: $font-size-xs;
  color: $text-secondary;
  margin-top: 2px;
}

/* 提交内容 */
.submission-block {
  background: $bg-page;
  border-radius: $radius-sm;
  padding: $spacing-sm $spacing-md;

  & + & {
    margin-top: $spacing-sm;
  }
}

.submission-author {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: $spacing-sm;
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-primary;

  .submission-time {
    font-weight: 400;
    color: $text-secondary;
    font-size: $font-size-xs;
    margin-left: auto;
  }
}

.submission-desc {
  font-size: $font-size-sm;
  color: $text-regular;
  line-height: 1.7;
  white-space: pre-wrap;
  margin: 0 0 $spacing-sm;
}

.submission-files {
  border-top: 1px solid $border-lighter;
  padding-top: $spacing-sm;
  max-height: 180px;
  overflow-y: auto;
  padding-right: 4px;

  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: $border-light;
    border-radius: 6px;
  }
}

.panel-file {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 3px 0;

  .file-name {
    font-size: $font-size-sm;
    color: $text-regular;
  }
}

.attachment-scroll {
  max-height: 180px;
  overflow-y: auto;
  padding-right: 4px;

  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: $border-light;
    border-radius: 6px;
  }
}

/* 操作按钮 */
.panel-actions {
  display: flex;
  gap: $spacing-sm;
  border-bottom: none !important;
}

.action-result {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  padding: $spacing-sm $spacing-md;
  border-radius: $radius-sm;
  font-size: $font-size-sm;
  font-weight: 600;

  &.result-approve {
    background: #ECFDF5;
    color: #22C55E;
  }
  &.result-reject {
    background: #FEF2F2;
    color: #DC2626;
  }
  &.result-cancel {
    background: #F3F4F6;
    color: #6B7280;
  }
  &.result-pending {
    background: #FFF7ED;
    color: #F59E0B;
  }
}

/* 内联表单 */
.inline-submit-form,
.inline-delegate-form {
  :deep(.el-form-item) {
    margin-bottom: $spacing-sm;
  }
  :deep(.el-form-item__label) {
    font-size: $font-size-sm;
    padding-bottom: 4px;
  }
  :deep(.el-upload-list) {
    max-height: 180px;
    overflow-y: auto;
    padding-right: 4px;
  }
  :deep(.el-upload-list::-webkit-scrollbar) {
    width: 6px;
  }
  :deep(.el-upload-list::-webkit-scrollbar-thumb) {
    background: $border-light;
    border-radius: 6px;
  }
}

/* 不通过理由 */
.reject-reason-text {
  font-size: $font-size-sm;
  color: $danger;
  background: #FEF2F2;
  border-radius: $radius-sm;
  padding: $spacing-sm $spacing-md;
  margin: 0;
  line-height: 1.5;
}

/* 提交/转交切换标签 */
.action-tab-bar {
  display: flex;
  gap: 0;
  border-bottom: 1px solid #E5E7EB;
  margin-bottom: 4px;
}
.action-tab {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  font-size: 13px;
  color: #6B7280;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: color 0.2s, border-color 0.2s;
  &.active {
    color: #4F6EF7;
    border-bottom-color: #4F6EF7;
    font-weight: 500;
  }
  &:hover:not(.active) {
    color: #374151;
  }
}

/* ====== 新建事务面板 ====== */
.create-panel-card {
  :deep(.el-card__body) {
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow: hidden;
  }
}

.create-title {
  font-size: $font-size-md;
  font-weight: 600;
  margin: 0;
}

.create-form {
  flex: 1;
  overflow-y: scroll;
  padding-right: 4px;

  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: $border-light;
    border-radius: 6px;
  }

  :deep(.el-form-item__label) {
    font-size: $font-size-sm;
    padding-bottom: 4px;
  }
}

.create-actions {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
  padding-top: $spacing-sm;
  border-top: 1px solid $border-lighter;
  flex-shrink: 0;
}

.urgency-group {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

.urgency-btn {
  &.is-active.urgency-important_urgent :deep(.el-radio-button__inner) {
    background-color: #EF4444; border-color: #EF4444; color: white;
  }
  &.is-active.urgency-important_not_urgent :deep(.el-radio-button__inner) {
    background-color: #F59E0B; border-color: #F59E0B; color: white;
  }
  &.is-active.urgency-not_important_urgent :deep(.el-radio-button__inner) {
    background-color: #3B82F6; border-color: #3B82F6; color: white;
  }
  &.is-active.urgency-not_important_not_urgent :deep(.el-radio-button__inner) {
    background-color: #22C55E; border-color: #22C55E; color: white;
  }
}

.deadline-input {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.deadline-presets {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.executor-selector {
  .selected-executor {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-xs $spacing-sm;
    border: 1px solid $border-light;
    border-radius: $radius-sm;
    cursor: pointer;
    transition: border-color $transition-fast;
    &:hover { border-color: $primary; }
    .executor-name { font-weight: 500; font-size: $font-size-sm; }
    .executor-dept { font-size: $font-size-xs; color: $text-secondary; }
    .change-icon { margin-left: auto; color: $text-secondary; }
  }
}

.upload-file-row {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.upload-file-name {
  flex: 1;
  color: $text-regular;
  font-size: $font-size-sm;
  white-space: normal;
  word-break: break-all;
}

.upload-file-actions {
  display: flex;
  gap: 4px;
  margin-left: auto;
  flex-shrink: 0;
}

/* 人员选择弹窗 */
.person-list {
  max-height: 360px;
  overflow-y: auto;
}

.person-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  border-radius: $radius-sm;
  cursor: pointer;
  transition: background $transition-fast;
  border-bottom: 1px solid $border-lighter;
  &:hover { background: $bg-hover; }
  &.active { background: $primary-light; }
  .person-name { font-weight: 500; font-size: $font-size-sm; }
  .person-dept { font-size: $font-size-xs; color: $text-secondary; }
}
</style>

<!-- 全局样式（不加 scoped），覆盖 Element Plus 2.x 排序列头 flex-direction: column 的默认行为 -->
<style lang="scss">
.task-list .sortable-right-header .cell {
  display: flex !important;
  flex-direction: row !important;
  align-items: center !important;
  justify-content: center;
  flex-wrap: nowrap !important;
  overflow: visible !important;
  white-space: nowrap !important;
  padding-inline: 6px !important;
  gap: 2px;
  line-height: normal;
}

.task-list .sortable-right-header .caret-wrapper {
  height: auto !important;
  flex-shrink: 0;
  display: inline-flex !important;
  flex-direction: column !important;
  align-items: center;
  position: relative;
}

.task-list .sortable-right-header .sort-caret.ascending {
  top: -4px;
  position: relative;
}

.task-list .sortable-right-header .sort-caret.descending {
  bottom: -3px;
  position: relative;
}
</style>
