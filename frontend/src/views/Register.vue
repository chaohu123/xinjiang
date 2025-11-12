<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-box">
        <h1 class="register-title">
          {{ $t('common.register') }}
        </h1>
        <el-form ref="registerFormRef" :model="registerForm" :rules="rules" label-width="0">
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="用户名"
              size="large"
              prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="邮箱"
              size="large"
              prefix-icon="Message"
            />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="手机号（可选）"
              size="large"
              prefix-icon="Phone"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" style="width: 100%" @click="handleRegister">
              {{ $t('common.register') }}
            </el-button>
          </el-form-item>
          <el-form-item>
            <div class="register-footer">
              <span>已有账号？</span>
              <el-link type="primary" @click="$router.push('/login')">
                {{ $t('common.login') }}
              </el-link>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const registerFormRef = ref<FormInstance>()

const registerForm = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== registerForm.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async valid => {
    if (valid) {
      try {
        await userStore.registerUser({
          username: registerForm.username,
          email: registerForm.email,
          password: registerForm.password,
          phone: registerForm.phone || undefined,
        })
        ElMessage.success('注册成功')
        router.push('/home')
      } catch (error: any) {
        ElMessage.error(error.message || '注册失败')
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-container {
  width: 100%;
  max-width: 400px;
  padding: 20px;
}

.register-box {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.register-title {
  font-size: 28px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.register-footer {
  text-align: center;
  color: #606266;
  font-size: 14px;
}
</style>
