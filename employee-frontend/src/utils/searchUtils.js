// 搜索工具类
// 注意：需要安装 pinyin-pro 依赖
// npm install pinyin-pro

/**
 * 搜索引擎类
 */
export class SearchEngine {
  constructor(searchData = []) {
    this.searchData = searchData
    this.searchIndex = this.buildSearchIndex(searchData)
    this.searchCache = new Map() // 搜索结果缓存
    this.maxCacheSize = 100 // 最大缓存数量
  }

  /**
   * 构建搜索索引
   */
  buildSearchIndex(data) {
    const index = new Map()
    
    data.forEach((item, itemIndex) => {
      // 为每个搜索项创建索引
      const indexTerms = new Set()
      
      // 添加标题相关的索引
      if (item.title) {
        indexTerms.add(item.title.toLowerCase())
        // 添加拼音索引（简化版，不依赖外部库）
        indexTerms.add(this.getSimplePinyin(item.title).toLowerCase())
      }
      
      // 添加关键词索引
      if (item.keywords && Array.isArray(item.keywords)) {
        item.keywords.forEach(keyword => {
          indexTerms.add(keyword.toLowerCase())
          // 为中文关键词添加简化拼音索引
          if (/[\u4e00-\u9fff]/.test(keyword)) {
            indexTerms.add(this.getSimplePinyin(keyword).toLowerCase())
          }
        })
      }
      
      // 添加描述索引
      if (item.description) {
        const descWords = item.description.split(/\s+/)
        descWords.forEach(word => {
          if (word.length > 1) {
            indexTerms.add(word.toLowerCase())
          }
        })
      }
      
      // 添加路径索引
      if (item.path) {
        const pathParts = item.path.split('/').filter(part => part)
        pathParts.forEach(part => {
          indexTerms.add(part.toLowerCase())
        })
      }
      
      // 将索引项映射到数据项
      indexTerms.forEach(term => {
        if (!index.has(term)) {
          index.set(term, [])
        }
        index.get(term).push({
          item,
          itemIndex,
          term
        })
      })
    })
    
    return index
  }

  /**
   * 简化的拼音转换（不依赖外部库）
   */
  getSimplePinyin(text) {
    // 简化版拼音映射，只处理常用字
    const pinyinMap = {
      '工': 'gong', '作': 'zuo', '台': 'tai', '个': 'ge', '人': 'ren',
      '考': 'kao', '勤': 'qin', '记': 'ji', '录': 'lu', '打': 'da', '卡': 'ka',
      '请': 'qing', '假': 'jia', '申': 'shen', '请': 'qing', '历': 'li', '史': 'shi',
      '协': 'xie', '同': 'tong', '编': 'bian', '辑': 'ji', '文': 'wen', '档': 'dang',
      '任': 'ren', '务': 'wu', '管': 'guan', '理': 'li', '创': 'chuang', '建': 'jian',
      '日': 'ri', '程': 'cheng', '安': 'an', '排': 'pai', '计': 'ji', '划': 'hua',
      '公': 'gong', '告': 'gao', '通': 'tong', '知': 'zhi', '消': 'xiao', '息': 'xi',
      '信': 'xin', '息': 'xi', '设': 'she', '置': 'zhi', '资': 'zi', '料': 'liao'
    }
    
    return text.split('').map(char => pinyinMap[char] || char).join('')
  }

  /**
   * 执行搜索
   */
  search(query, options = {}) {
    const {
      limit = 10,
      category = null,
      fuzzy = true,
      minScore = 0.1
    } = options

    if (!query || query.trim().length === 0) {
      return []
    }

    const searchQuery = query.toLowerCase().trim()
    const cacheKey = `${searchQuery}:${JSON.stringify(options)}`

    // 检查缓存
    if (this.searchCache.has(cacheKey)) {
      return this.searchCache.get(cacheKey)
    }

    const results = new Map()

    // 精确匹配
    this.exactMatch(searchQuery, results)
    
    // 前缀匹配
    this.prefixMatch(searchQuery, results)
    
    // 模糊匹配
    if (fuzzy) {
      this.fuzzyMatch(searchQuery, results)
    }

    // 转换为数组并排序
    let sortedResults = Array.from(results.values())
      .filter(result => result.score >= minScore)
      .sort((a, b) => b.score - a.score)

    // 按分类过滤
    if (category) {
      sortedResults = sortedResults.filter(result => result.item.category === category)
    }

    // 限制结果数量
    const finalResults = sortedResults.slice(0, limit)

    // 缓存结果
    this.cacheSearchResult(cacheKey, finalResults)

    return finalResults
  }

  /**
   * 缓存搜索结果
   */
  cacheSearchResult(key, results) {
    // 如果缓存已满，删除最旧的条目
    if (this.searchCache.size >= this.maxCacheSize) {
      const firstKey = this.searchCache.keys().next().value
      this.searchCache.delete(firstKey)
    }

    this.searchCache.set(key, results)
  }

  /**
   * 精确匹配
   */
  exactMatch(query, results) {
    const matches = this.searchIndex.get(query) || []
    matches.forEach(match => {
      const key = match.item.id
      if (!results.has(key)) {
        results.set(key, {
          ...match,
          score: 1.0,
          matchType: 'exact'
        })
      }
    })
  }

  /**
   * 前缀匹配
   */
  prefixMatch(query, results) {
    this.searchIndex.forEach((matches, term) => {
      if (term.startsWith(query)) {
        matches.forEach(match => {
          const key = match.item.id
          if (!results.has(key)) {
            const score = query.length / term.length * 0.8
            results.set(key, {
              ...match,
              score,
              matchType: 'prefix'
            })
          }
        })
      }
    })
  }

  /**
   * 模糊匹配
   */
  fuzzyMatch(query, results) {
    this.searchIndex.forEach((matches, term) => {
      if (term.includes(query)) {
        matches.forEach(match => {
          const key = match.item.id
          if (!results.has(key)) {
            const score = this.calculateFuzzyScore(query, term)
            if (score > 0.1) {
              results.set(key, {
                ...match,
                score,
                matchType: 'fuzzy'
              })
            }
          }
        })
      }
    })
  }

  /**
   * 计算模糊匹配分数
   */
  calculateFuzzyScore(query, term) {
    const queryLen = query.length
    const termLen = term.length
    
    // 基础分数：查询长度与词条长度的比值
    let score = queryLen / termLen * 0.6
    
    // 位置加权：越靠前的匹配分数越高
    const index = term.indexOf(query)
    if (index === 0) {
      score += 0.2 // 开头匹配
    } else if (index > 0) {
      score += 0.1 * (1 - index / termLen) // 位置权重
    }
    
    return Math.min(score, 0.9) // 模糊匹配最高0.9分
  }

  /**
   * 获取搜索建议
   */
  getSuggestions(query, limit = 5) {
    if (!query || query.trim().length === 0) {
      return []
    }

    const suggestions = new Set()
    const searchQuery = query.toLowerCase().trim()

    // 从索引中查找匹配的词条
    this.searchIndex.forEach((matches, term) => {
      if (term.startsWith(searchQuery) && term !== searchQuery) {
        suggestions.add(term)
      }
    })

    return Array.from(suggestions).slice(0, limit)
  }

  /**
   * 更新搜索数据
   */
  updateSearchData(newData) {
    this.searchData = newData
    this.searchIndex = this.buildSearchIndex(newData)
    this.clearCache() // 清除缓存
  }

  /**
   * 清除搜索缓存
   */
  clearCache() {
    this.searchCache.clear()
  }
}

/**
 * 防抖函数
 */
export function debounce(func, wait) {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

/**
 * 高亮匹配文本
 */
export function highlightMatch(text, query) {
  if (!query || !text) return text
  
  const regex = new RegExp(`(${escapeRegExp(query)})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

/**
 * 转义正则表达式特殊字符
 */
function escapeRegExp(string) {
  return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

/**
 * 格式化搜索结果
 */
export function formatSearchResult(result, query, t) {
  return {
    id: result.item.id,
    title: t ? t(result.item.titleKey) : result.item.title,
    description: t ? t(result.item.descriptionKey) : result.item.description,
    path: result.item.path,
    icon: result.item.icon,
    category: result.item.category,
    categoryLabel: t ? t(result.item.categoryKey) : result.item.category,
    score: result.score,
    matchType: result.matchType,
    highlightedTitle: highlightMatch(
      t ? t(result.item.titleKey) : result.item.title, 
      query
    )
  }
}
