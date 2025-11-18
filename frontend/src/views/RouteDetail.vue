<template>
  <div class="route-detail-page">
    <!-- 头部横幅区域 -->
    <div v-if="routeDetail" class="route-hero">
      <div class="hero-background">
        <div class="hero-gradient"></div>
        <div class="hero-pattern"></div>
      </div>
      <div class="container">
        <div class="hero-content">
          <div class="route-badge">
            <span class="badge-icon">🏔️</span>
            <span>精品路线</span>
          </div>
          <h1 class="route-title">{{ routeDetail.title }}</h1>
          <p class="route-subtitle">{{ routeDetail.description }}</p>
          <div class="route-info-cards">
            <div class="info-card">
              <div class="info-icon blue">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="info-content">
                <div class="info-label">行程天数</div>
                <div class="info-value">{{ routeDetail.duration }} 天</div>
              </div>
            </div>
            <div class="info-card">
              <div class="info-icon gold">
                <el-icon><MapLocation /></el-icon>
              </div>
              <div class="info-content">
                <div class="info-label">总里程</div>
                <div class="info-value">{{ routeDetail.distance }}km</div>
              </div>
            </div>
            <div class="info-card">
              <div class="info-icon red">
                <el-icon><Location /></el-icon>
              </div>
              <div class="info-content">
                <div class="info-label">路线</div>
                <div class="info-value">{{ routeDetail.startLocation }} → {{ routeDetail.endLocation }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 行程安排区域 -->
    <div class="container">
      <div v-loading="loading" class="route-detail-content">
        <div v-if="routeDetail" class="route-itinerary">
          <div class="itinerary-header">
            <div class="header-decoration">
              <div class="decoration-line left"></div>
              <div class="decoration-icon">✨</div>
              <h2>行程安排</h2>
              <div class="decoration-icon">✨</div>
              <div class="decoration-line right"></div>
            </div>
            <p class="itinerary-subtitle">探索新疆的每一处精彩</p>
          </div>

          <div v-if="!routeDetail.itinerary || routeDetail.itinerary.length === 0" class="empty-itinerary">
            <el-empty description="暂无行程安排" :image-size="120">
              <template #description>
                <p style="color: #909399; margin-top: 16px;">该路线暂未添加详细行程，请稍后再来查看</p>
              </template>
            </el-empty>
          </div>
          <div v-else class="custom-timeline">
            <div
              v-for="(item, index) in routeDetail.itinerary"
              :key="item.day"
              class="timeline-item"
              :class="{ 'last-item': index === routeDetail.itinerary.length - 1 }"
            >
              <div class="timeline-marker">
                <div class="marker-circle">
                  <span class="day-number">{{ item.day }}</span>
                </div>
                <div v-if="index !== routeDetail.itinerary.length - 1" class="marker-line"></div>
              </div>
              <div class="timeline-content">
                <div class="day-card">
                  <div class="day-header">
                    <span class="day-label">第 {{ item.day }} 天</span>
                    <div class="day-divider"></div>
                  </div>
                  <h3 class="day-title">{{ item.title }}</h3>
                  <p class="day-description" style="white-space: pre-line;">{{ item.description }}</p>

                  <!-- 时间轴展示 -->
                  <div v-if="item.timeSchedule" class="time-schedule">
                    <div class="schedule-header">
                      <span class="schedule-icon">⏰</span>
                      <span class="schedule-title">时间安排</span>
                    </div>
                    <div class="schedule-content" style="white-space: pre-line;">{{ item.timeSchedule }}</div>
                  </div>

                  <div v-if="item.locations.length > 0" class="day-locations">
                    <div
                      v-for="(loc, locIndex) in item.locations"
                      :key="loc.name"
                      class="location-item"
                      :style="{ animationDelay: `${locIndex * 0.1}s` }"
                    >
                      <div class="location-header">
                        <span class="tag-icon">📍</span>
                        <span class="tag-text">{{ loc.name }}</span>
                      </div>
                      <p v-if="loc.description" class="location-description" style="white-space: pre-line;">{{ loc.description }}</p>
                      <div v-if="loc.lat && loc.lng" class="location-actions">
                        <el-button
                          text
                          type="primary"
                          size="small"
                          @click="openGoogleMaps(loc.lat, loc.lng, loc.name)"
                        >
                          <el-icon><MapLocation /></el-icon>
                          查看地图
                        </el-button>
                      </div>
                    </div>
                  </div>

                  <!-- 每日预算汇总 -->
                  <div v-if="item.dailyBudget" class="daily-budget">
                    <div class="budget-header">
                      <span class="budget-icon">💰</span>
                      <span class="budget-title">每日预算</span>
                    </div>
                    <div class="budget-content" style="white-space: pre-line;">{{ item.dailyBudget }}</div>
                  </div>

                  <div v-if="item.accommodation" class="day-extra">
                    <span class="extra-label">住宿：</span>
                    <span class="extra-value">{{ item.accommodation }}</span>
                  </div>
                  <div v-if="item.meals && item.meals.length > 0" class="day-extra">
                    <span class="extra-label">餐饮：</span>
                    <div class="meals-list">
                      <div v-for="(meal, mealIndex) in item.meals" :key="mealIndex" class="meal-item">
                        {{ meal }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 注意事项区域 -->
          <div v-if="routeDetail.tips && routeDetail.tips.length > 0" class="route-tips-section">
            <div class="tips-header">
              <div class="header-decoration">
                <div class="decoration-line left"></div>
                <div class="decoration-icon">💡</div>
                <h2>注意事项</h2>
                <div class="decoration-icon">💡</div>
                <div class="decoration-line right"></div>
              </div>
              <p class="tips-subtitle">出行前必读，让旅程更顺利</p>
            </div>
            <div class="tips-list">
              <div
                v-for="(tip, index) in routeDetail.tips"
                :key="index"
                class="tip-item"
                :style="{ animationDelay: `${index * 0.1}s` }"
              >
                <div class="tip-icon">📌</div>
                <div class="tip-content">{{ tip }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getRouteDetail } from '@/api/route'
import type { RouteDetail } from '@/types/route'
import { Clock, MapLocation, Location } from '@element-plus/icons-vue'

const openGoogleMaps = (lat: number, lng: number, name: string) => {
  const url = `https://maps.google.com/?q=${lat},${lng}`
  window.open(url, '_blank')
}

const route = useRoute()
const routeDetail = ref<RouteDetail | null>(null)
const loading = ref(false)

const loadDetail = async () => {
  const id = parseInt(route.params.id as string)
  loading.value = true
  try {
    const data = await getRouteDetail(id)
    routeDetail.value = data
  } catch (error) {
    console.error('Failed to load route detail:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.route-detail-page {
  min-height: calc(100vh - 70px);
  background: linear-gradient(to bottom, #f8f9fa 0%, #ffffff 100%);
}

// 头部横幅区域
.route-hero {
  position: relative;
  padding: 80px 0 60px;
  overflow: hidden;
  margin-bottom: 40px;

  .hero-background {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 0;
  }

  .hero-gradient {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(
      135deg,
      rgba(0, 115, 230, 0.08) 0%,
      rgba(230, 176, 0, 0.08) 50%,
      rgba(230, 0, 0, 0.08) 100%
    );
  }

  .hero-pattern {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url('data:image/svg+xml,%3Csvg width="100" height="100" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg"%3E%3Cpath d="M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z" fill="%23e6b000" fill-opacity="0.05" fill-rule="evenodd"/%3E%3C/svg%3E');
    background-size: 200px 200px;
    opacity: 0.4;
  }

  .hero-content {
    position: relative;
    z-index: 1;
    text-align: center;
  }

  .route-badge {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 8px 20px;
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);
    border-radius: 50px;
    font-size: 14px;
    font-weight: 600;
    color: #e6b000;
    margin-bottom: 24px;
    box-shadow: 0 4px 12px rgba(230, 176, 0, 0.15);
    animation: fadeInDown 0.6s ease-out;

    .badge-icon {
      font-size: 18px;
    }
  }

  .route-title {
    font-size: 48px;
    font-weight: 700;
    margin-bottom: 20px;
    background: linear-gradient(135deg, #0073e6 0%, #e6b000 50%, #e60000 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    line-height: 1.2;
    animation: fadeInUp 0.6s ease-out 0.2s both;
  }

  .route-subtitle {
    font-size: 18px;
    color: #606266;
    line-height: 1.8;
    max-width: 800px;
    margin: 0 auto 40px;
    animation: fadeInUp 0.6s ease-out 0.4s both;
  }

  .route-info-cards {
    display: flex;
    justify-content: center;
    gap: 24px;
    flex-wrap: wrap;
    animation: fadeInUp 0.6s ease-out 0.6s both;
  }

  .info-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px 28px;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
    min-width: 200px;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
    }

    .info-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      color: white;
      flex-shrink: 0;

      &.blue {
        background: linear-gradient(135deg, #0073e6 0%, #005bb3 100%);
      }

      &.gold {
        background: linear-gradient(135deg, #e6b000 0%, #b38f00 100%);
      }

      &.red {
        background: linear-gradient(135deg, #e60000 0%, #b30000 100%);
      }
    }

    .info-content {
      .info-label {
        font-size: 12px;
        color: #909399;
        margin-bottom: 4px;
      }

      .info-value {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
    }
  }
}

// 行程安排区域
.route-detail-content {
  background: white;
  border-radius: 20px;
  padding: 50px 40px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  margin-bottom: 40px;
}

.route-itinerary {
  .itinerary-header {
    text-align: center;
    margin-bottom: 50px;

    .header-decoration {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 16px;
      margin-bottom: 12px;

      .decoration-line {
        flex: 1;
        height: 2px;
        max-width: 150px;
        background: linear-gradient(
          to right,
          transparent,
          #e6b000,
          transparent
        );

        &.left {
          background: linear-gradient(to right, transparent, #e6b000);
        }

        &.right {
          background: linear-gradient(to left, transparent, #e6b000);
        }
      }

      .decoration-icon {
        font-size: 20px;
        animation: twinkle 2s ease-in-out infinite;
      }

      h2 {
        font-size: 36px;
        font-weight: 700;
        background: linear-gradient(135deg, #0073e6 0%, #e6b000 50%, #e60000 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        margin: 0;
      }
    }

    .itinerary-subtitle {
      font-size: 16px;
      color: #909399;
      font-style: italic;
    }
  }

  .custom-timeline {
    position: relative;
    padding-left: 60px;

    .timeline-item {
      position: relative;
      margin-bottom: 40px;

      &.last-item {
        margin-bottom: 0;
      }

      .timeline-marker {
        position: absolute;
        left: -60px;
        top: 0;
        display: flex;
        flex-direction: column;
        align-items: center;

        .marker-circle {
          width: 50px;
          height: 50px;
          border-radius: 50%;
          background: linear-gradient(135deg, #0073e6 0%, #e6b000 50%, #e60000 100%);
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 4px 16px rgba(0, 115, 230, 0.3);
          position: relative;
          z-index: 2;
          transition: all 0.3s ease;

          &::before {
            content: '';
            position: absolute;
            inset: -4px;
            border-radius: 50%;
            background: linear-gradient(135deg, #0073e6, #e6b000, #e60000);
            opacity: 0.3;
            z-index: -1;
            animation: pulse 2s ease-in-out infinite;
          }

          .day-number {
            color: white;
            font-size: 20px;
            font-weight: 700;
          }
        }

        .marker-line {
          width: 3px;
          height: calc(100% + 40px);
          background: linear-gradient(
            to bottom,
            #0073e6,
            #e6b000,
            #e60000
          );
          margin-top: 8px;
          border-radius: 2px;
          opacity: 0.3;
        }
      }

      .timeline-content {
        .day-card {
          background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
          border-radius: 20px;
          padding: 32px;
          box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
          border: 1px solid rgba(230, 176, 0, 0.1);
          transition: all 0.3s ease;
          position: relative;
          overflow: hidden;

          &::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, #0073e6, #e6b000, #e60000);
          }

          &:hover {
            transform: translateX(8px);
            box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
            border-color: rgba(230, 176, 0, 0.3);
          }

          .day-header {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 16px;

            .day-label {
              font-size: 14px;
              font-weight: 600;
              color: #e6b000;
              text-transform: uppercase;
              letter-spacing: 1px;
            }

            .day-divider {
              flex: 1;
              height: 1px;
              background: linear-gradient(
                to right,
                #e6b000,
                transparent
              );
            }
          }

          .day-title {
            font-size: 24px;
            font-weight: 700;
            color: #303133;
            margin-bottom: 12px;
            line-height: 1.4;
          }

          .day-description {
            font-size: 16px;
            color: #606266;
            line-height: 1.8;
            margin-bottom: 20px;
          }

          // 时间轴展示
          .time-schedule {
            margin: 20px 0;
            padding: 16px;
            background: linear-gradient(135deg, rgba(0, 115, 230, 0.08), rgba(230, 176, 0, 0.08));
            border: 1px solid rgba(0, 115, 230, 0.2);
            border-radius: 12px;

            .schedule-header {
              display: flex;
              align-items: center;
              gap: 8px;
              margin-bottom: 12px;

              .schedule-icon {
                font-size: 20px;
              }

              .schedule-title {
                font-weight: 600;
                font-size: 16px;
                color: #303133;
              }
            }

            .schedule-content {
              font-size: 14px;
              color: #606266;
              line-height: 1.8;
              padding-left: 28px;
            }
          }

          // 每日预算
          .daily-budget {
            margin: 20px 0;
            padding: 16px;
            background: linear-gradient(135deg, rgba(103, 194, 58, 0.08), rgba(230, 176, 0, 0.08));
            border: 1px solid rgba(103, 194, 58, 0.2);
            border-radius: 12px;

            .budget-header {
              display: flex;
              align-items: center;
              gap: 8px;
              margin-bottom: 12px;

              .budget-icon {
                font-size: 20px;
              }

              .budget-title {
                font-weight: 600;
                font-size: 16px;
                color: #303133;
              }
            }

            .budget-content {
              font-size: 14px;
              color: #606266;
              line-height: 1.8;
              padding-left: 28px;
            }
          }

          .day-locations {
            display: flex;
            flex-direction: column;
            gap: 16px;
            margin-bottom: 16px;

            .location-item {
              padding: 16px;
              background: linear-gradient(135deg, rgba(0, 115, 230, 0.05), rgba(230, 176, 0, 0.05));
              border: 1px solid rgba(230, 176, 0, 0.2);
              border-radius: 12px;
              transition: all 0.3s ease;
              animation: fadeInScale 0.5s ease-out both;

              &:hover {
                background: linear-gradient(135deg, rgba(0, 115, 230, 0.1), rgba(230, 176, 0, 0.1));
                border-color: rgba(230, 176, 0, 0.4);
                transform: translateX(4px);
                box-shadow: 0 4px 12px rgba(230, 176, 0, 0.15);
              }

              .location-header {
                display: flex;
                align-items: center;
                gap: 8px;
                margin-bottom: 8px;

                .tag-icon {
                  font-size: 18px;
                }

                .tag-text {
                  font-weight: 600;
                  font-size: 16px;
                  color: #303133;
                }
              }

              .location-description {
                font-size: 14px;
                color: #606266;
                line-height: 1.6;
                margin: 0 0 12px 0;
                white-space: pre-line;
              }

              .location-actions {
                margin-top: 8px;
                display: flex;
                gap: 8px;
              }
            }
          }

          .day-extra {
            padding: 16px;
            background: rgba(0, 115, 230, 0.05);
            border-radius: 12px;
            font-size: 14px;
            margin-top: 12px;

            .extra-label {
              color: #909399;
              margin-right: 8px;
              font-weight: 600;
            }

            .extra-value {
              color: #303133;
              font-weight: 500;
              line-height: 1.6;
            }

            .meals-list {
              display: flex;
              flex-direction: column;
              gap: 8px;
              margin-top: 8px;

              .meal-item {
                padding: 8px 12px;
                background: rgba(255, 255, 255, 0.6);
                border-radius: 8px;
                color: #303133;
                line-height: 1.5;
              }
            }
          }
        }
      }
    }
  }

  // 注意事项区域
  .route-tips-section {
    margin-top: 60px;
    padding-top: 40px;
    border-top: 2px solid rgba(230, 176, 0, 0.1);

    .tips-header {
      text-align: center;
      margin-bottom: 40px;

      .header-decoration {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 16px;
        margin-bottom: 12px;

        .decoration-line {
          flex: 1;
          height: 2px;
          max-width: 150px;
          background: linear-gradient(
            to right,
            transparent,
            #e6b000,
            transparent
          );

          &.left {
            background: linear-gradient(to right, transparent, #e6b000);
          }

          &.right {
            background: linear-gradient(to left, transparent, #e6b000);
          }
        }

        .decoration-icon {
          font-size: 20px;
          animation: twinkle 2s ease-in-out infinite;
        }

        h2 {
          font-size: 32px;
          font-weight: 700;
          background: linear-gradient(135deg, #0073e6 0%, #e6b000 50%, #e60000 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
          margin: 0;
        }
      }

      .tips-subtitle {
        font-size: 16px;
        color: #909399;
        font-style: italic;
      }
    }

    .tips-list {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 20px;

      .tip-item {
        display: flex;
        align-items: flex-start;
        gap: 16px;
        padding: 20px;
        background: linear-gradient(135deg, rgba(255, 193, 7, 0.08), rgba(255, 152, 0, 0.08));
        border: 1px solid rgba(255, 193, 7, 0.2);
        border-radius: 16px;
        transition: all 0.3s ease;
        animation: fadeInScale 0.5s ease-out both;

        &:hover {
          background: linear-gradient(135deg, rgba(255, 193, 7, 0.12), rgba(255, 152, 0, 0.12));
          border-color: rgba(255, 193, 7, 0.4);
          transform: translateY(-4px);
          box-shadow: 0 8px 24px rgba(255, 193, 7, 0.2);
        }

        .tip-icon {
          font-size: 24px;
          flex-shrink: 0;
          margin-top: 2px;
        }

        .tip-content {
          flex: 1;
          font-size: 15px;
          color: #303133;
          line-height: 1.8;
          font-weight: 500;
        }
      }
    }
  }
}

// 动画
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInScale {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes twinkle {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(0.9);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.3;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.5;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .route-hero {
    padding: 60px 0 40px;

    .route-title {
      font-size: 32px;
    }

    .route-subtitle {
      font-size: 16px;
      padding: 0 20px;
    }

    .route-info-cards {
      flex-direction: column;
      align-items: center;
    }

    .info-card {
      width: 100%;
      max-width: 300px;
    }
  }

  .route-detail-content {
    padding: 30px 20px;
    border-radius: 16px;
  }

  .custom-timeline {
    padding-left: 40px;

    .timeline-item {
      .timeline-marker {
        left: -40px;

        .marker-circle {
          width: 40px;
          height: 40px;

          .day-number {
            font-size: 16px;
          }
        }
      }

      .timeline-content {
        .day-card {
          padding: 24px;

          .day-title {
            font-size: 20px;
          }
        }
      }
    }
  }

  .itinerary-header {
    .header-decoration {
      h2 {
        font-size: 28px;
      }

      .decoration-line {
        max-width: 80px;
      }
    }

    .route-tips-section {
      .tips-list {
        grid-template-columns: 1fr;
      }

      .tips-header {
        .header-decoration {
          h2 {
            font-size: 24px;
          }

          .decoration-line {
            max-width: 60px;
          }
        }
      }
    }
  }
}
</style>
