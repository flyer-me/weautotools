import {
	createSSRApp
} from "vue";
import App from "./App.vue";
import { Icon } from 'tdesign-icons-vue-next';
import { useGlobalBadge } from '@/composables/useBadge';

export function createApp() {
	const app = createSSRApp(App);
	app.component('t-icon', Icon);

	// 初始化全局徽章数据
	const globalBadge = useGlobalBadge();
	globalBadge.initBadges();

	return {
		app,
	};
}
