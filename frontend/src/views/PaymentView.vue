<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi, payApi } from '../api/order'
import BaseCard from '../components/BaseCard.vue'
import BaseButton from '../components/BaseButton.vue'
import Skeleton from '../components/Skeleton.vue'
import { toast } from '../composables/useToast'
import { yuan } from '../utils/format'

const route = useRoute()
const router = useRouter()
const orderId = route.params.id

const order = ref(null)
const paymentNo = ref('')
const amount = ref(null)
const loading = ref(true)
const processing = ref(false)
const result = ref('')       // '' | 'success' | 'failed'
const failed = ref(false)

onMounted(async () => {
  try {
    order.value = await orderApi.detail(orderId)
    if (order.value.payStatus === 'PAID') {
      router.replace(`/orders/${orderId}`)
      return
    }
    if (order.value.payStatus === 'UNPAID') {
      // 发起支付，拿到模拟渠道支付单号与金额
      const pay = await orderApi.pay(orderId)
      paymentNo.value = pay.paymentNo
      amount.value = pay.amount
      order.value = pay.order
    } else if (order.value.payStatus === 'PROCESSING') {
      // 再次进入：查询当前支付单
      const st = await payApi.status(order.value.code ? paymentNo.value : '').catch(() => null)
      paymentNo.value = paymentNo.value || ''
      amount.value = order.value.totalPrice
    }
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
})

async function confirm(success) {
  processing.value = true
  try {
    const updated = await orderApi.confirmPay(orderId, success)
    order.value = updated
    if (updated.payStatus === 'PAID') {
      result.value = 'success'
      toast.success('支付成功，已出票')
      setTimeout(() => router.push(`/orders/${orderId}`), 1200)
    } else {
      result.value = 'failed'
      failed.value = true
      toast.error('支付失败，余票已回补')
    }
  } catch (e) {
    toast.error(e.message)
    failed.value = true
  } finally {
    processing.value = false
  }
}

function repay() {
  failed.value = false
  result.value = ''
  location.reload()
}
</script>

<template>
  <div v-if="loading"><Skeleton card :rows="4" /></div>
  <div v-else class="pay-wrap">
    <BaseCard class="pay-card">
      <div v-if="result === 'success'" class="result">
        <div class="result-icon success">✓</div>
        <h2>支付成功</h2>
        <p class="muted">订单已出票，正在跳转订单详情…</p>
      </div>

      <div v-else-if="failed" class="result">
        <div class="result-icon fail">✕</div>
        <h2>支付未完成</h2>
        <p class="muted">支付失败，余票已释放，可重新发起支付</p>
        <BaseButton class="mt" @click="repay">重新支付</BaseButton>
      </div>

      <div v-else>
        <h2 class="pay-title">模拟支付</h2>
        <div class="pay-rows">
          <div class="row-between"><span class="muted">支付单号</span><span>{{ paymentNo || '--' }}</span></div>
          <div class="row-between"><span class="muted">待付金额</span><span class="amount">{{ yuan(amount ?? order.totalPrice) }}</span></div>
          <div class="row-between"><span class="muted">订单号</span><span>{{ order.code }}</span></div>
        </div>
        <p class="muted note">此为模拟第三方渠道，点击"确认支付"即视为渠道回告成功</p>
        <div class="row pay-actions">
          <BaseButton block :loading="processing" @click="confirm(true)">确认支付</BaseButton>
          <BaseButton variant="ghost" @click="confirm(false)">模拟支付失败</BaseButton>
        </div>
      </div>
    </BaseCard>
  </div>
</template>

<style scoped>
.pay-wrap { max-width: 440px; margin: 0 auto; }
.pay-card { text-align: center; }
.pay-title { margin-bottom: var(--space-5); }
.pay-rows { display: flex; flex-direction: column; gap: var(--space-3); text-align: left; margin-bottom: var(--space-4); }
.amount { font-size: var(--text-xl); font-weight: 700; color: var(--color-accent); }
.note { margin-bottom: var(--space-5); }
.pay-actions { gap: var(--space-2); }
.result { padding: var(--space-4) 0; display: flex; flex-direction: column; align-items: center; gap: var(--space-3); }
.result-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}
.result-icon.success { background: var(--color-success); }
.result-icon.fail { background: var(--color-danger); }
.mt { margin-top: var(--space-2); }
</style>
