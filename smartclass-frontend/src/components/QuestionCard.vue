<template>
  <div class="question-card" :class="{ 'is-wrong': showResult && !result.correct }">
    <div class="question-head">
      <el-tag size="small" :type="typeTag(question.type)">{{ typeLabel(question.type) }}</el-tag>
      <el-tag v-if="question.knowledgeName" size="small" type="info" effect="plain">
        {{ question.knowledgeName }}
      </el-tag>
      <el-rate :model-value="question.difficulty" disabled size="small" class="difficulty" />
      <span v-if="scoreText" class="score-text">{{ scoreText }}</span>
    </div>

    <div class="question-title">{{ index }}. {{ question.title }}</div>

    <!-- 单选 / 判断 -->
    <template v-if="question.type === 'SINGLE' || question.type === 'JUDGE'">
      <el-radio-group v-model="inner" :disabled="disabled" class="options">
        <el-radio v-for="(option, i) in optionList" :key="i" :value="optionValue(i)">
          {{ optionLabel(i) }}. {{ option }}
        </el-radio>
      </el-radio-group>
    </template>

    <!-- 多选 -->
    <template v-else-if="question.type === 'MULTI'">
      <el-checkbox-group v-model="multiValue" :disabled="disabled" class="options">
        <el-checkbox v-for="(option, i) in optionList" :key="i" :value="optionValue(i)">
          {{ optionLabel(i) }}. {{ option }}
        </el-checkbox>
      </el-checkbox-group>
    </template>

    <!-- 填空 -->
    <template v-else>
      <el-input
        v-model="inner"
        :disabled="disabled"
        placeholder="请输入答案"
        style="max-width: 420px"
      />
    </template>

    <!-- 判分结果 -->
    <el-alert
      v-if="showResult"
      :type="result.correct ? 'success' : 'error'"
      :closable="false"
      class="result"
    >
      <template #title>
        <span>{{ result.correct ? '回答正确' : '回答错误' }}</span>
        <span class="right-answer">正确答案:{{ result.rightAnswer }}</span>
      </template>
      <div v-if="result.analysis" class="analysis">解析:{{ result.analysis }}</div>
    </el-alert>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  question: { type: Object, required: true },
  modelValue: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  result: { type: Object, default: null },
  index: { type: Number, default: 1 },
  scoreText: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const optionList = computed(() => props.question.options || [])

function optionLabel(i) {
  return String.fromCharCode(65 + i)
}

// 判断题固定渲染「对/错」两个选项,其余题型取题目自带选项
function optionValue(i) {
  if (props.question.type === 'JUDGE') {
    return optionList.value[i]
  }
  return optionLabel(i)
}

const inner = computed({
  get: () => props.modelValue || '',
  set: val => emit('update:modelValue', val)
})

const multiValue = computed({
  get: () => (props.modelValue || '').split('').filter(Boolean),
  set: val => emit('update:modelValue', [...val].sort().join(''))
})

const showResult = computed(() => !!props.result)

function typeLabel(type) {
  return { SINGLE: '单选题', MULTI: '多选题', JUDGE: '判断题', FILL: '填空题' }[type] || type
}

function typeTag(type) {
  return { SINGLE: 'primary', MULTI: 'warning', JUDGE: 'success', FILL: 'info' }[type] || 'info'
}
</script>

<style scoped>
.question-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
}

.question-card.is-wrong {
  border-color: #f89898;
  background: #fef0f0;
}

.question-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.difficulty {
  height: 18px;
}

.score-text {
  margin-left: auto;
  color: #909399;
  font-size: 12px;
}

.question-title {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  margin-bottom: 14px;
  white-space: pre-wrap;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-start;
}

.result {
  margin-top: 14px;
}

.right-answer {
  margin-left: 16px;
  font-weight: 600;
}

.analysis {
  margin-top: 6px;
  line-height: 1.7;
  font-size: 13px;
}
</style>
