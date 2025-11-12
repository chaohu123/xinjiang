<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <div>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户名或邮箱"
              style="width: 300px; margin-right: 12px"
              clearable
              @keyup.enter="loadUsers"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="loadUsers"> 搜索 </el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="users" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role?.toUpperCase() === 'ADMIN' ? 'danger' : 'primary'">
              {{ row.role?.toUpperCase() === 'ADMIN' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)"> 编辑 </el-button>
            <el-button
              link
              :type="row.enabled ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.enabled ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" title="编辑用户" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" />
          <span style="margin-left: 10px">{{ form.enabled ? '启用' : '禁用' }}</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false"> 取消 </el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit"> 确定 </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getUsers, updateUser, deleteUser, toggleUserStatus } from '@/api/admin'
import type { UserInfo } from '@/types/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { formatDate } from '@/utils'

const loading = ref(false)
const submitting = ref(false)
const users = ref<UserInfo[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const dialogVisible = ref(false)
const editingItem = ref<UserInfo | null>(null)

const form = ref<Partial<UserInfo>>({
  username: '',
  email: '',
  nickname: '',
  phone: '',
  role: 'USER',
  enabled: true,
})

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await getUsers({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
    })
    users.value = response.list || []
    total.value = response.total || 0
  } catch (error: any) {
    console.error('Failed to load users:', error)
    // 如果是404或500错误，说明后端API还未实现
    if (error?.response?.status === 404 || error?.response?.status === 500) {
      ElMessage.warning('管理员API功能尚未实现，请等待后端开发完成')
      users.value = []
      total.value = 0
    } else {
      ElMessage.error('加载用户列表失败：' + (error?.message || '未知错误'))
    }
  } finally {
    loading.value = false
  }
}

const handleEdit = (user: UserInfo) => {
  editingItem.value = user
  form.value = {
    username: user.username,
    email: user.email,
    nickname: user.nickname,
    phone: user.phone,
    role: (user.role?.toUpperCase() || 'USER') as 'USER' | 'ADMIN',
    enabled: user.enabled,
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!editingItem.value) return

  submitting.value = true
  try {
    await updateUser(editingItem.value.id, form.value)
    ElMessage.success('更新成功')
    dialogVisible.value = false
    loadUsers()
  } catch (error) {
    console.error('Failed to update user:', error)
    ElMessage.error('更新失败')
  } finally {
    submitting.value = false
  }
}

const handleToggleStatus = async (user: UserInfo) => {
  try {
    await toggleUserStatus(user.id, !user.enabled)
    ElMessage.success(user.enabled ? '已禁用' : '已启用')
    loadUsers()
  } catch (error) {
    console.error('Failed to toggle user status:', error)
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (user: UserInfo) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${user.username}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteUser(user.id)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete user:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style lang="scss" scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
