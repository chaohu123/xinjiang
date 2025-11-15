<template>
  <div class="login-page">
    <div class="login-container">
      <div class="modern-form">
        <h2 class="form-title">欢迎登录</h2>

        <form @submit.prevent="handleLogin">
          <div class="input-group">
            <div class="input-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <input
                v-model="loginForm.username"
                type="text"
                class="form-input"
                placeholder="请输入用户名"
                required
                :class="{ 'error': usernameError }"
                @blur="validateUsername"
                @input="usernameError = ''"
              />
            </div>
          </div>

          <div class="input-group">
            <div class="input-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              <input
                v-model="loginForm.password"
                :type="showPassword ? 'text' : 'password'"
                class="form-input"
                placeholder="请输入密码"
                required
                minlength="6"
                :class="{ 'error': passwordError }"
                @blur="validatePassword"
                @input="passwordError = ''"
                @keyup.enter="handleLogin"
              />
              <button
                type="button"
                class="password-toggle"
                @click="showPassword = !showPassword"
              >
                <svg v-if="showPassword" class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                  <line x1="1" y1="1" x2="23" y2="23"></line>
                </svg>
                <svg v-else class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
              </button>
            </div>
          </div>

          <button type="submit" class="submit-button" :disabled="loading">
            <span v-if="!loading">登录</span>
            <span v-else>登录中...</span>
            <div class="button-glow"></div>
          </button>
        </form>

        <div class="form-footer">
          <a href="#" class="login-link" @click.prevent="$router.push('/register')">
            还没有账号？<span>立即注册</span>
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const rememberMe = ref(false)
const loading = ref(false)
const showPassword = ref(false)
const usernameError = ref('')
const passwordError = ref('')

const loginForm = reactive({
  username: '',
  password: '',
})

const validateUsername = () => {
  if (!loginForm.username.trim()) {
    usernameError.value = '请输入用户名或邮箱'
    return false
  }
  usernameError.value = ''
  return true
}

const validatePassword = () => {
  if (!loginForm.password.trim()) {
    passwordError.value = '请输入密码'
    return false
  }
  passwordError.value = ''
  return true
}

const handleLogin = async () => {
  if (!validateUsername() || !validatePassword()) {
    return
  }

  loading.value = true
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
    console.error('登录错误:', error)
    // 错误消息已经在 axios 拦截器中显示
    if (error.response?.status === 401) {
      passwordError.value = '用户名或密码错误'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin: 0;
  padding: 20px;
  font-family: system-ui, -apple-system, sans-serif;
}

.login-container {
  width: 100%;
  max-width: 300px;
}

.modern-form {
  --primary: #3b82f6;
  --primary-dark: #2563eb;
  --primary-light: rgba(59, 130, 246, 0.1);
  --success: #10b981;
  --text-main: #1e293b;
  --text-secondary: #64748b;
  --bg-input: #f8fafc;

  width: 300px;
  padding: 24px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.1),
    0 2px 4px -2px rgba(0, 0, 0, 0.05),
    inset 0 0 0 1px rgba(148, 163, 184, 0.1);
  font-family: system-ui, -apple-system, sans-serif;
}

.form-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-main);
  margin: 0 0 24px;
  text-align: center;
  letter-spacing: -0.01em;
}

.input-group {
  margin-bottom: 16px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.form-input {
  width: 100%;
  height: 40px;
  padding: 0 36px;
  font-size: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: var(--bg-input);
  color: var(--text-main);
  transition: all 0.2s ease;
  box-sizing: border-box;

  &::placeholder {
    color: var(--text-secondary);
  }

  &.error {
    border-color: #ef4444;
    animation: shake 0.2s ease-in-out;
  }
}

.input-icon {
  position: absolute;
  left: 12px;
  width: 16px;
  height: 16px;
  color: var(--text-secondary);
  pointer-events: none;
  z-index: 1;
}

.form-input.error ~ .input-icon {
  color: #ef4444;
}

.form-input:not(:placeholder-shown):valid:not(.error) {
  border-color: var(--success);
}

.form-input:not(:placeholder-shown):valid:not(.error) ~ .input-icon {
  color: var(--success);
}

.form-input:not(:placeholder-shown):invalid {
  border-color: #ef4444;
  animation: shake 0.2s ease-in-out;
}

.form-input:not(:placeholder-shown):invalid ~ .input-icon {
  color: #ef4444;
}

.password-toggle {
  position: absolute;
  right: 12px;
  display: flex;
  align-items: center;
  padding: 4px;
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 1;
}

.eye-icon {
  width: 16px;
  height: 16px;
}

.submit-button {
  position: relative;
  width: 100%;
  height: 40px;
  margin-top: 8px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.2s ease;

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

.button-glow {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transform: translateX(-100%);
  transition: transform 0.5s ease;
}

.submit-button:hover:not(:disabled) .button-glow {
  transform: translateX(100%);
}

.form-footer {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary);
}

.login-link {
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.2s ease;

  span {
    color: var(--primary);
    font-weight: 500;
  }

  &:hover {
    color: var(--text-main);

    span {
      color: var(--primary-dark);
    }
  }
}

.form-input:hover {
  border-color: #cbd5e1;
}

.form-input:focus {
  outline: none;
  border-color: var(--primary);
  background: white;
  box-shadow: 0 0 0 4px var(--primary-light);
}

.password-toggle:hover {
  color: var(--primary);
  transform: scale(1.1);
}

.submit-button:hover:not(:disabled) {
  background: var(--primary-dark);
  transform: translateY(-1px);
  box-shadow:
    0 4px 12px rgba(59, 130, 246, 0.25),
    0 2px 4px rgba(59, 130, 246, 0.15);
}

.login-link:hover {
  color: var(--text-main);

  span {
    color: var(--primary-dark);
  }
}

.submit-button:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: none;
}

.password-toggle:active {
  transform: scale(0.9);
}

.form-input:not(:placeholder-shown):valid {
  border-color: var(--success);
}

.form-input:not(:placeholder-shown):valid ~ .input-icon {
  color: var(--success);
}

@keyframes shake {
  0%,
  100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-4px);
  }
  75% {
    transform: translateX(4px);
  }
}

@media (max-width: 480px) {
  .modern-form {
    width: 100%;
    max-width: 300px;
  }
}
</style>
