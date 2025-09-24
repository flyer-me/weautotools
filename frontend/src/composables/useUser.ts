import { ref, onMounted, shallowRef } from "vue";
import userManager from "@/services/authService";
import type { User } from "oidc-client-ts";

export interface AuthState {
  user: User | null;
  isLoading: boolean;
}

export interface AuthActions {
  login: (returnUrl?: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
}

/**
 * A composable function that provides access to the reactive user state
 * and authentication-related actions like login and logout.
 */
export function useUser() {
  const state = ref<AuthState>({
    user: null,
    isLoading: true,
  });
  const login = async () => {
    // Store the current path to redirect back after login.
    await userManager.signinRedirect({ state: window.location.pathname });
  };

  const logout = async () => {
    await userManager.signoutRedirect();
  };

  const refreshUser = async () => {
    try {
      const user = await userManager.getUser();
      state.value.user = user && !user.expired ? user : null;
    } catch (err) {
      console.error("Failed to refresh user:", err);
      state.value.user = null;
    } finally {
      state.value.isLoading = false;
    }
  };

  // 监听用户事件
  onMounted(() => {
    // 初始化：加载用户
    refreshUser();

    userManager.events.addUserLoaded((user: User) => {
      state.value.user = user;
    });

    userManager.events.addUserUnloaded(() => {
      state.value.user = null;
    });

    userManager.events.addSilentRenewError((err) => {
      console.error("Silent renew error:", err);
    });
  });

  return {
    user: shallowRef(state.value.user),
    isLoading: shallowRef(state.value.isLoading),
    login,
    logout,
    refreshUser,
  };
}
