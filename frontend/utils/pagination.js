import { computed, ref, watch } from 'vue'

export function usePagination(source, size = 10) {
  const currentPage = ref(1)
  const pageSize = size

  const totalPages = computed(() => Math.max(1, Math.ceil(source.value.length / pageSize)))
  const pageStartIndex = computed(() => (currentPage.value - 1) * pageSize)
  const pagedItems = computed(() => source.value.slice(pageStartIndex.value, pageStartIndex.value + pageSize))

  watch(totalPages, (pages) => {
    if (currentPage.value > pages) {
      currentPage.value = pages
    }
  })

  const goToPage = (page) => {
    currentPage.value = Math.min(Math.max(page, 1), totalPages.value)
  }

  const resetPage = () => {
    currentPage.value = 1
  }

  return {
    currentPage,
    pageSize,
    totalPages,
    pageStartIndex,
    pagedItems,
    goToPage,
    resetPage
  }
}
