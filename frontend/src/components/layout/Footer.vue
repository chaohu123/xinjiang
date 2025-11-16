<template>
  <footer class="footer">
    <div class="container">
      <div class="footer-content">
        <div class="footer-section">
          <h3>{{ $t('footer.about') }}</h3>
          <p>{{ $t('footer.description') }}</p>
          <div class="social-links">
            <el-icon><Share /></el-icon>
            <el-icon><Message /></el-icon>
            <el-icon><Link /></el-icon>
          </div>
        </div>

        <div class="footer-section">
          <h3>{{ $t('footer.quickLinks') }}</h3>
          <ul>
            <li>
              <router-link to="/home">
                {{ $t('nav.home') }}
              </router-link>
            </li>
            <li>
              <router-link to="/map">
                {{ $t('nav.map') }}
              </router-link>
            </li>
            <li>
              <router-link to="/routes">
                {{ $t('nav.routes') }}
              </router-link>
            </li>
            <li>
              <router-link to="/events">
                {{ $t('nav.events') }}
              </router-link>
            </li>
          </ul>
        </div>

        <div class="footer-section">
          <h3>{{ $t('footer.contact') }}</h3>
          <ul>
            <li>
              <el-icon><Message /></el-icon>
              <span>{{ email }}</span>
            </li>
            <li>
              <el-icon><Phone /></el-icon>
              <span>{{ phone }}</span>
            </li>
            <li>
              <el-icon><Location /></el-icon>
              <span>{{ address }}</span>
            </li>
          </ul>
        </div>
      </div>

      <div class="footer-bottom">
        <p>&copy; {{ currentYear }} {{ $t('app.name') }}. {{ $t('footer.rights') }}</p>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Share, Message, Link, Phone, Location } from '@element-plus/icons-vue'

const { t } = useI18n()
const currentYear = computed(() => new Date().getFullYear())

// 使用计算属性获取联系信息
// 将邮箱分成两部分存储，避免 vue-i18n 将 @ 解析为链接引用
const email = computed(() => {
  const user = t('footer.emailUser')
  const domain = t('footer.emailDomain')
  return `${user}@${domain}`
})
const phone = computed(() => t('footer.phone'))
const address = computed(() => t('footer.address'))
</script>

<style lang="scss" scoped>
.footer {
  background: linear-gradient(135deg, #002a4d 0%, #00111a 100%);
  color: #ecf0f1;
  padding: 40px 0 20px;
  margin-top: 60px;
  position: relative;
  overflow: hidden;

  // 添加传统花纹装饰
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url('data:image/svg+xml,%3Csvg width="100" height="100" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg"%3E%3Cpath d="M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z" fill="%23e6b000" fill-opacity="0.1" fill-rule="evenodd"/%3E%3C/svg%3E');
    background-size: 200px 200px;
    opacity: 0.3;
    pointer-events: none;
  }

  > * {
    position: relative;
    z-index: 1;
  }
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
  margin-bottom: 30px;
}

.footer-section {
  h3 {
    margin-bottom: 20px;
    font-size: 18px;
    color: #fff;
  }

  p {
    line-height: 1.8;
    color: #bdc3c7;
    margin-bottom: 15px;
  }

  ul {
    list-style: none;

    li {
      margin-bottom: 12px;
      color: #bdc3c7;
      display: flex;
      align-items: center;
      gap: 8px;

      a {
        color: #bdc3c7;
        text-decoration: none;
        transition: all 0.3s;

        &:hover {
          color: #e6b000;
          transform: translateX(4px);
        }
      }
    }
  }
}

.social-links {
  display: flex;
  gap: 15px;
  margin-top: 15px;

  .el-icon {
    font-size: 24px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      color: #e6b000;
      transform: scale(1.2) rotate(5deg);
    }
  }
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid rgba(230, 176, 0, 0.2);
  color: #95a5a6;
  position: relative;
}
</style>
