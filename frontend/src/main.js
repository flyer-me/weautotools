import {
	createSSRApp
} from "vue";
import App from "./App.vue";
import { Icon } from 'tdesign-icons-vue-next';

export function createApp() {
	const app = createSSRApp(App);
	app.component('t-icon', Icon);
	return {
		app,
	};
}
