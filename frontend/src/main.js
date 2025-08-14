import {
	createSSRApp
} from "vue";
import App from "./App.vue";
import { Icon } from 'tdesign-icons-vue-next';
import { useGlobalBadge } from '@/composables/useBadge';
import { initDevModeFromStorage } from '@/config/features';

export function createApp() {
	const app = createSSRApp(App);
	app.component('t-icon', Icon);

	// 初始化全局徽章数据
	const globalBadge = useGlobalBadge();
	globalBadge.initBadges();

	// 初始化开发模式状态
	initDevModeFromStorage();

	return {
		app,
	};
}
