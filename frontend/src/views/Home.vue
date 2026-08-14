<template>
  <div class="page">
    <h1 class="page-title">飞机票售票系统 · DDD 教学原型</h1>

    <!-- 航班列表 -->
    <el-card class="section-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>航班列表</span>
          <el-button size="small" @click="loadFlights" :loading="loadingFlights">刷新</el-button>
        </div>
      </template>
      <el-table :data="flights" v-loading="loadingFlights" stripe border>
        <el-table-column prop="flightNo" label="航班号" min-width="110" />
        <el-table-column prop="fromCity" label="出发城市" min-width="100" />
        <el-table-column prop="toCity" label="到达城市" min-width="100" />
        <el-table-column prop="departTime" label="起飞时间" min-width="160" />
        <el-table-column prop="arriveTime" label="到达时间" min-width="160" />
        <el-table-column prop="remainingSeats" label="余票" width="80" />
        <el-table-column label="价格(¥)" width="100">
          <template #default="{ row }">
            <span class="price">{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              :disabled="!canBook(row)"
              @click="openBookDialog(row)"
            >
              预订
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 我的订单 -->
    <el-card class="section-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>我的订单</span>
          <el-button size="small" @click="loadOrders" :loading="loadingOrders">刷新</el-button>
        </div>
      </template>
      <el-table :data="orders" v-loading="loadingOrders" stripe border>
        <el-table-column prop="orderNo" label="订单号" min-width="150" />
        <el-table-column prop="passengerName" label="乘机人" min-width="100" />
        <el-table-column prop="passengerPhone" label="电话" min-width="130" />
        <el-table-column label="价格(¥)" width="100">
          <template #default="{ row }">
            <span class="price">{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="130" />
        <el-table-column label="下单时间" min-width="160">
          <template #default="{ row }">
            {{ row.createdAt || '' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 订票弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="预订机票"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="bookFormRef" :model="bookForm" :rules="bookRules" label-width="90px">
        <el-form-item label="乘机人" prop="passengerName">
          <el-input
            v-model="bookForm.passengerName"
            placeholder="请输入乘机人姓名"
            maxlength="20"
            clearable
          />
        </el-form-item>
        <el-form-item label="联系电话" prop="passengerPhone">
          <el-input
            v-model="bookForm.passengerPhone"
            placeholder="请输入 11 位手机号"
            maxlength="11"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitBook">确认预订</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const flights = ref([])
const loadingFlights = ref(false)

const orders = ref([])
const loadingOrders = ref(false)

const dialogVisible = ref(false)
const submitting = ref(false)
const bookFormRef = ref(null)
const currentFlight = ref(null)

const bookForm = reactive({
  passengerName: '',
  passengerPhone: ''
})

const bookRules = {
  passengerName: [
    { required: true, message: '请输入乘机人姓名', trigger: 'blur' }
  ],
  passengerPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^\d{11}$/, message: '请输入正确的 11 位联系电话', trigger: 'blur' }
  ]
}

// 仅当航班可售（status = 1）且有余票时可预订
function canBook(row) {
  return row.status === 1 && Number(row.remainingSeats) > 0
}

async function loadFlights() {
  loadingFlights.value = true
  try {
    const res = await request.get('/flights')
    if (res.code === 0) {
      flights.value = res.data || []
    } else {
      ElMessage.error(res.msg || '加载航班失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '加载航班失败')
  } finally {
    loadingFlights.value = false
  }
}

async function loadOrders() {
  loadingOrders.value = true
  try {
    const res = await request.get('/orders')
    if (res.code === 0) {
      orders.value = res.data || []
    } else {
      ElMessage.error(res.msg || '加载订单失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '加载订单失败')
  } finally {
    loadingOrders.value = false
  }
}

function openBookDialog(row) {
  currentFlight.value = row
  bookForm.passengerName = ''
  bookForm.passengerPhone = ''
  dialogVisible.value = true
}

async function submitBook() {
  if (!bookFormRef.value) return
  try {
    await bookFormRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const res = await request.post('/orders', {
      flightId: currentFlight.value.id,
      passengerName: bookForm.passengerName.trim(),
      passengerPhone: bookForm.passengerPhone.trim()
    })
    if (res.code === 0) {
      ElMessage.success('订票成功')
      dialogVisible.value = false
      await loadOrders()
      await loadFlights()
    } else {
      ElMessage.error(res.msg || '订票失败')
    }
  } catch (e) {
    // 后端失败时错误字段是 msg，直接展示后端返回的提示
    ElMessage.error(e.response?.data?.msg || '订票失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadFlights()
  loadOrders()
})
</script>

<style scoped>
.page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px 16px 40px;
}

.page-title {
  text-align: center;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 24px;
}

.section-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.price {
  font-weight: 600;
  color: #f56c6c;
}
</style>
