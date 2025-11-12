<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-box">
        <h1 class="login-title">
          {{ $t('common.login') }}
        </h1>
        <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-width="0">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              :placeholder="'用户名或邮箱'"
              size="large"
              prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              :placeholder="'密码'"
              size="large"
              prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <div class="login-options">
              <el-checkbox v-model="rememberMe"> 记住我 </el-checkbox>
              <el-link type="primary" underline="never"> 忘记密码？ </el-link>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" style="width: 100%" @click="handleLogin">
              {{ $t('common.login') }}
            </el-button>
          </el-form-item>
          <el-form-item>
            <div class="login-footer">
              <span>还没有账号？</span>
              <el-link type="primary" @click="$router.push('/register')">
                {{ $t('common.register') }}
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
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名或邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async valid => {
    if (valid) {
      try {
        await userStore.loginUser(loginForm.username, loginForm.password)
        ElMessage.success('登录成功')

        // 判断如果是管理员，跳转到管理员界面
        // 后端返回的 role 是大写 'ADMIN' 或 'USER'
        const role = userStore.userInfo?.role?.toUpperCase()
        if (role === 'ADMIN') {
          router.push('/admin')
        } else {
          const redirect = (route.query.redirect as string) || '/home'
          router.push(redirect)
        }
      } catch (error: any) {
        // 错误消息已经在 axios 拦截器中显示，这里不需要重复显示
        // 只在 axios 拦截器没有处理的情况下才显示
        // 400 错误已经在拦截器中处理，不需要重复显示
        console.error('登录错误:', error)
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-container {
  width: 100%;
  max-width: 400px;
  padding: 20px;
}

.login-box {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.login-options {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.login-footer {
  text-align: center;
  color: #606266;
  font-size: 14px;
}
</style>
