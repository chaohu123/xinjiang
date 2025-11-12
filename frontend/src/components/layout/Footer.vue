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
  background: #2c3e50;
  color: #ecf0f1;
  padding: 40px 0 20px;
  margin-top: 60px;
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
        transition: color 0.3s;

        &:hover {
          color: #409eff;
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
    transition: color 0.3s;

    &:hover {
      color: #409eff;
    }
  }
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #34495e;
  color: #95a5a6;
}
</style>
