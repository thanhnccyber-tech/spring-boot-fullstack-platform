/* UTETra loader: chỉ hiện khi người dùng điều hướng (click link / submit form).
   Lần đầu truy cập, F5, back/forward → ẩn ngay, không có màn hình load. */
(() => {
  'use strict';
  const overlay = document.getElementById('uteSiteLoader');
  if (!overlay) return;

  const NAV_KEY = 'utetra:navLoading';
  const number   = document.getElementById('uteSiteLoaderPercent');
  const bar      = document.getElementById('uteSiteLoaderBar');
  const progress = document.getElementById('uteSiteLoaderProgress');

  // Đọc cờ điều hướng do trang trước để lại
  let userInitiated = false;
  try {
    userInitiated = sessionStorage.getItem(NAV_KEY) === '1';
    sessionStorage.removeItem(NAV_KEY);
  } catch (e) { /* chế độ riêng tư chặn sessionStorage → coi như load đầu */ }

  attachNavListener();

  if (!userInitiated) {
    // Load lần đầu / F5 / back-forward: không hiện loader
    return;
  }

  overlay.classList.add('is-loading');

  const startedAt        = performance.now();
  const minimumVisibleMs = 480;
  let   value            = 0;
  let   pageLoaded       = document.readyState === 'complete';
  let   removed          = false;

  const watchdog = window.setTimeout(() => { pageLoaded = true; }, 8000);
  window.addEventListener('load', () => { pageLoaded = true; }, { once: true });
  window.addEventListener('pageshow', (event) => {
    if (event.persisted && !removed) {
      removed = true;
      window.clearTimeout(watchdog);
      overlay.classList.remove('is-loading', 'is-hidden');
    }
  }, { once: true });

  function render() {
    const rounded = Math.max(0, Math.min(100, Math.round(value)));
    if (number)   number.textContent = String(rounded);
    if (bar)      bar.style.width    = rounded + '%';
    if (progress) progress.setAttribute('aria-valuenow', String(rounded));
  }

  function frame(now) {
    if (removed) return;
    const elapsed = now - startedAt;
    if (!pageLoaded) {
      value = Math.max(value, Math.min(92, 92 * (1 - Math.exp(-elapsed / 460))));
    } else if (elapsed < minimumVisibleMs) {
      value = Math.max(value, Math.min(92, (elapsed / minimumVisibleMs) * 92));
    } else {
      value += Math.max(1.7, (100 - value) * .22);
    }
    if (value >= 99.6) value = 100;
    render();
    if (value >= 100) {
      removed = true;
      window.clearTimeout(watchdog);
      overlay.classList.add('is-hidden');
      window.setTimeout(() => overlay.classList.remove('is-loading', 'is-hidden'), 520);
      return;
    }
    window.requestAnimationFrame(frame);
  }

  render();
  window.requestAnimationFrame(frame);

  /* ===================================================================
   * Đánh dấu điều hướng: click link nội bộ / submit form
   * → set sessionStorage flag cho trang mới + hiện overlay trên trang hiện tại
   * =================================================================== */
  function attachNavListener() {
    document.addEventListener('click', function (event) {
      if (event.defaultPrevented) return;
      if (event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
      const link = event.target.closest('a[href]');
      if (!link) return;
      if (link.target && link.target !== '_self') return;
      if (link.hasAttribute('download')) return;
      const href = link.getAttribute('href');
      if (!href || href.startsWith('#') || href.startsWith('javascript:')
          || href.startsWith('mailto:') || href.startsWith('tel:') || href.startsWith('data:')) return;
      try {
        const url = new URL(href, location.href);
        if (url.origin !== location.origin) return;
        // Chỉ khác hash → không thực sự điều hướng
        if (url.pathname === location.pathname
            && url.search === location.search
            && url.hash) return;
      } catch (err) { return; }
      markNavigation();
    }, true);

    document.addEventListener('submit', function (event) {
      if (event.defaultPrevented) return;
      const form = event.target;
      if (!form || form.tagName !== 'FORM') return;
      if (form.target && form.target !== '_self') return;
      markNavigation();
    }, true);
  }

  function markNavigation() {
    try { sessionStorage.setItem(NAV_KEY, '1'); } catch (e) { return; }

    // Che trang hiện tại bằng overlay trắng trong lúc chờ trang mới
    if (overlay && overlay.isConnected) {
      overlay.classList.remove('is-hidden');
      overlay.classList.add('is-loading');
      if (bar)      bar.style.width = '0%';
      if (number)   number.textContent = '0';
      if (progress) progress.setAttribute('aria-valuenow', '0');
    }

    // Nếu không điều hướng thực sự (lỗi mạng, chặn) → tự hủy cờ sau 5s
    window.setTimeout(function () {
      try {
        if (sessionStorage.getItem(NAV_KEY) === '1') sessionStorage.removeItem(NAV_KEY);
      } catch (e) {}
    }, 5000);
  }
})();