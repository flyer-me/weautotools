import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [uni()],
  define: {
    global: 'globalThis',
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
