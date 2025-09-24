import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { fileURLToPath, URL } from 'node:url'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    uni({
      exclude: [
        'node_modules'
      ]
    })
  ],
  server: {
      // 确保前端开发服务器与后端CORS和客户端配置匹配
      port: 5173,
    },
  define: {
    global: 'globalThis',
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  optimizeDeps: {
    include: [
      'pdf-lib',
      'qrcode',
      'jsqr',
      'js-yaml',
      'xml2js',
      'compressorjs'
    ]
  },
  build: {
    rollupOptions: {
      external: (id) => {
        // 小程序端排除某些库
        if (process.env.UNI_PLATFORM === 'mp-weixin') {
          return ['html2canvas'].includes(id)
        }
        return false
      }
    }
  }
})
