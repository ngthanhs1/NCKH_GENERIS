(() => {
	const STORAGE_KEY = 'dacs_theme';
	const root = document.documentElement;

	function normalizeTheme(v) {
		return (v === 'light' || v === 'dark') ? v : null;
	}

	function safeGetStoredTheme() {
		try {
			return normalizeTheme(localStorage.getItem(STORAGE_KEY));
		} catch {
			return null;
		}
	}

	function safeSetStoredTheme(theme) {
		try {
			localStorage.setItem(STORAGE_KEY, theme);
		} catch {
			// ignore
		}
	}

	function systemTheme() {
		try {
			return window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
		} catch {
			return 'dark';
		}
	}

	function getInitialTheme() {
		return safeGetStoredTheme() || systemTheme();
	}

	function updateButton(theme) {
		const btn = document.getElementById('btnTheme');
		if (!btn) return;

		// Label thể hiện theme sẽ chuyển sang khi click
		const next = theme === 'light' ? 'dark' : 'light';
		btn.textContent = next === 'light' ? 'Sáng' : 'Tối';
		btn.title = next === 'light' ? 'Chuyển sang giao diện sáng' : 'Chuyển sang giao diện tối';
		btn.setAttribute('aria-pressed', theme === 'dark' ? 'true' : 'false');
	}

	function applyTheme(theme) {
		root.dataset.theme = theme;
		updateButton(theme);
		try {
			window.dispatchEvent(new CustomEvent('themechange', { detail: { theme } }));
		} catch {
			// ignore
		}
	}

	// Apply sớm nhất có thể để giảm “flash”
	applyTheme(getInitialTheme());

	function init() {
		const btn = document.getElementById('btnTheme');
		if (btn) {
			btn.addEventListener('click', () => {
				const current = normalizeTheme(root.dataset.theme) || 'dark';
				const next = current === 'light' ? 'dark' : 'light';
				safeSetStoredTheme(next);
				applyTheme(next);
			});
			updateButton(normalizeTheme(root.dataset.theme) || 'dark');
		}

		// Nếu user chưa chọn thủ công thì auto theo hệ điều hành
		const mq = window.matchMedia ? window.matchMedia('(prefers-color-scheme: light)') : null;
		if (mq && typeof mq.addEventListener === 'function') {
			mq.addEventListener('change', () => {
				if (safeGetStoredTheme()) return;
				applyTheme(systemTheme());
			});
		}
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', init);
	} else {
		init();
	}
})();
