(() => {
  const STORAGE_KEY = 'generics_theme';
  const root = document.documentElement;

  function normalizeTheme(value) {
    return value === 'light' || value === 'dark' ? value : null;
  }

  function readStoredTheme() {
    try {
      return normalizeTheme(localStorage.getItem(STORAGE_KEY));
    } catch {
      return null;
    }
  }

  function writeStoredTheme(theme) {
    try {
      localStorage.setItem(STORAGE_KEY, theme);
    } catch {
      // ignore storage failure
    }
  }

  function systemTheme() {
    try {
      return window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
    } catch {
      return 'dark';
    }
  }

  function updateButton(theme) {
    const btn = document.getElementById('btnTheme');
    if (!btn) return;

    const nextTheme = theme === 'light' ? 'dark' : 'light';
    btn.textContent = nextTheme === 'light' ? 'Sáng' : 'Tối';
    btn.title = nextTheme === 'light' ? 'Chuyển sang giao diện sáng' : 'Chuyển sang giao diện tối';
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

  applyTheme(readStoredTheme() || systemTheme());

  function init() {
    const btn = document.getElementById('btnTheme');
    if (btn) {
      btn.addEventListener('click', () => {
        const current = normalizeTheme(root.dataset.theme) || 'dark';
        const next = current === 'light' ? 'dark' : 'light';
        writeStoredTheme(next);
        applyTheme(next);
      });
      updateButton(normalizeTheme(root.dataset.theme) || 'dark');
    }

    const media = window.matchMedia ? window.matchMedia('(prefers-color-scheme: light)') : null;
    if (media && typeof media.addEventListener === 'function') {
      media.addEventListener('change', () => {
        if (readStoredTheme()) return;
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