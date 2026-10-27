/* UTETra entry loading screen: logo focuses from blurred/faded to crisp, with a percent indicator. */
(() => {
  'use strict';
  const overlay = document.getElementById('uteSiteLoader');
  if (!overlay) return;

  const number = document.getElementById('uteSiteLoaderPercent');
  const bar = document.getElementById('uteSiteLoaderBar');
  const progress = document.getElementById('uteSiteLoaderProgress');
  const startedAt = performance.now();
  const minimumVisibleMs = 920;
  let value = 0;
  let pageLoaded = document.readyState === 'complete';
  let removed = false;

  const watchdog = window.setTimeout(() => { pageLoaded = true; }, 12000);
  window.addEventListener('load', () => { pageLoaded = true; }, { once: true });
  window.addEventListener('pageshow', (event) => {
    if (event.persisted && !removed) {
      removed = true;
      window.clearTimeout(watchdog);
      overlay.remove();
    }
  }, { once: true });

  function render() {
    const rounded = Math.max(0, Math.min(100, Math.round(value)));
    if (number) number.textContent = String(rounded);
    if (bar) bar.style.width = rounded + '%';
    if (progress) progress.setAttribute('aria-valuenow', String(rounded));
  }

  function frame(now) {
    if (removed) return;
    const elapsed = now - startedAt;

    if (!pageLoaded) {
      // Ease toward 92% while the browser is still loading resources.
      const estimated = Math.min(92, 92 * (1 - Math.exp(-elapsed / 680)));
      value = Math.max(value, estimated);
    } else if (elapsed < minimumVisibleMs) {
      // Keep the motion visible briefly even on a warm cache.
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
      window.setTimeout(() => overlay.remove(), 520);
      return;
    }
    window.requestAnimationFrame(frame);
  }

  render();
  window.requestAnimationFrame(frame);
})();
