import { createSSRApp } from "vue";
import App from "./App.vue";
import { initDevModeFromStorage } from "@/config";
export function createApp() {
  const app = createSSRApp(App);

  try {
    initDevModeFromStorage();
  } catch (error) {
    console.warn("开发模式初始化失败，但不影响应用运行:", error);
  }
  return {
    app,
  };
}
