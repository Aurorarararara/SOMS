import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'path'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia']
    }),
    Components({
      resolvers: [ElementPlusResolver()]
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3001,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      // 添加所有WebSocket端点的代理配置
      '/ws-collaboration': {
        target: 'ws://localhost:8081',
        changeOrigin: true,
        ws: true
      },
      '/ws-richtext': {
        target: 'ws://localhost:8081',
        changeOrigin: true,
        ws: true
      },
      '/ws-markdown': {
        target: 'ws://localhost:8081',
        changeOrigin: true,
        ws: true
      },
      '/ws-code': {
        target: 'ws://localhost:8081',
        changeOrigin: true,
        ws: true
      },
      '/ws-table': {
        target: 'ws://localhost:8081',
        changeOrigin: true,
        ws: true
      }
    }
  },
  // 添加define配置来解决global is not defined的问题
  define: {
    global: 'globalThis'
  }
})