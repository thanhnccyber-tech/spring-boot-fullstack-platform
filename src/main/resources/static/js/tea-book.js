(function () {
  'use strict';

  const stage = document.getElementById('teaBookStage');
  const frame = document.getElementById('teaBookFrame');
  const pagesSurface = document.getElementById('teaBookPages');
  const leftPage = document.getElementById('teaSpreadLeft');
  const rightPage = document.getElementById('teaSpreadRight');
  const curlHost = document.getElementById('teaBookCurlHost');
  const source = document.getElementById('teaBookData');
  const prevButton = document.getElementById('teaBookPrev');
  const nextButton = document.getElementById('teaBookNext');
  const magnifier = document.getElementById('teaMagnifier');
  const magnifierCopy = document.getElementById('teaMagnifierCopy');
  const zoomInButton = document.getElementById('teaZoomIn');
  const zoomOutButton = document.getElementById('teaZoomOut');
  const zoomReadout = document.getElementById('teaZoomReadout');
  const loupeToggle = document.getElementById('teaLoupeToggle');

  if (!stage || !frame || !pagesSurface || !leftPage || !rightPage || !curlHost || !source) return;

  const records = Array.from(source.querySelectorAll('.tea-book-record'));
  const products = records.map(function (record) {
    const imageNode = record.querySelector('[data-image]');
    return {
      title: record.dataset.title || 'Trà sữa UTETra',
      description: record.dataset.description || 'Pha chế tươi mới mỗi ngày tại UTETra.',
      price: Number(record.dataset.price || 0),
      url: record.dataset.url || '/products',
      image: imageNode ? imageNode.dataset.image : '',
      outOfStock: record.dataset.outOfStock === 'true'
    };
  });

  const coverLeftHTML = leftPage.innerHTML;
  const coverRightHTML = rightPage.innerHTML;
  const reduceMotion = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  const coarsePointer = window.matchMedia && window.matchMedia('(pointer: coarse)').matches;
  const formatter = new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 0 });
  const STRIPS = 18;
  const SPAN = 0.449;
  const BETA = 0.60;
  let spreadIndex = -1; // -1 is the opening spread; 0..N-1 are product spreads.
  let activeTurn = null;
  let pointerDrag = null;
  let zoom = 1;
  let loupeEnabled = true;
  let loupeX = 0;
  let loupeY = 0;
  let loupePointer = null;
  let magnifierCopyBook = null;
  let resizeFrame = 0;

  function el(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
  }

  function escapeIds(root) {
    root.querySelectorAll('[id]').forEach(function (node) { node.removeAttribute('id'); });
  }

  function cupDoodle() {
    const holder = el('div', 'tea-cup-doodle');
    holder.setAttribute('aria-hidden', 'true');
    holder.innerHTML = '<svg viewBox="0 0 64 82" fill="none"><path d="M39 5 49 1M39 5 32 17M15 23h35l-4 46a5 5 0 0 1-5 4H25a5 5 0 0 1-5-4l-5-46Z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/><path d="M13 23c2-6 7-9 18-9h10c7 0 11 3 13 9" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/><circle cx="26" cy="49" r="3" stroke="currentColor" stroke-width="1.5"/><circle cx="36" cy="55" r="3" stroke="currentColor" stroke-width="1.5"/><circle cx="29" cy="62" r="3" stroke="currentColor" stroke-width="1.5"/><circle cx="40" cy="44" r="3" stroke="currentColor" stroke-width="1.5"/></svg>';
    return holder;
  }

  function pageNumber(text) {
    return el('div', 'tea-page-number', '— ' + text + ' —');
  }

  function renderLeftProduct(productIndex) {
    const product = products[productIndex];
    leftPage.replaceChildren();
    const page = el('div', 'tea-product-polaroid-page');
    const topLine = el('div', 'tea-page-topline');
    topLine.appendChild(el('span', '', 'UTETra / JOURNAL'));
    topLine.appendChild(el('span', '', 'VOL. 01'));
    page.appendChild(topLine);

    const polaroid = el('a', 'tea-polaroid');
    polaroid.href = product.url + '?quantity=1';
    polaroid.setAttribute('aria-label', 'Xem món ' + product.title);
    const tape = el('span', 'tea-polaroid-tape');
    tape.setAttribute('aria-hidden', 'true');
    const image = el('img');
    image.src = product.image;
    image.alt = product.title;
    image.loading = 'eager';
    image.decoding = 'async';
    const caption = el('span', 'tea-polaroid-caption', 'Món số ' + String(productIndex + 1).padStart(2, '0'));
    polaroid.append(tape, image, caption);
    page.appendChild(polaroid);

    const detail = el('div', 'tea-polaroid-detail');
    detail.appendChild(el('span', '', formatter.format(product.price) + ' ₫'));
    const link = el('a', '', 'Xem món ↗');
    link.href = product.url + '?quantity=1';
    detail.appendChild(link);
    page.appendChild(detail);
    page.appendChild(pageNumber(String(productIndex + 3).padStart(2, '0')));
    leftPage.appendChild(page);
  }

  function renderRightProduct(productIndex) {
    const product = products[productIndex];
    rightPage.replaceChildren();
    const page = el('div', 'tea-product-page');
    page.appendChild(cupDoodle());
    page.appendChild(el('p', 'tea-product-kicker', 'MÓN SỐ ' + String(productIndex + 1).padStart(2, '0')));
    page.appendChild(el('h2', '', product.title));
    page.appendChild(el('p', 'tea-product-description', product.description));

    const priceLine = el('div', 'tea-price-line');
    priceLine.appendChild(el('span', 'tea-price-label', 'GIÁ TỪ'));
    priceLine.appendChild(el('span', 'tea-price-dots'));
    priceLine.appendChild(el('strong', 'tea-product-price', formatter.format(product.price) + ' ₫'));
    page.appendChild(priceLine);

    const actions = el('div', 'tea-product-actions');
    let quantity = 1;
    const stepper = el('div', 'tea-qty-stepper');
    stepper.setAttribute('aria-label', 'Chọn số lượng');
    const minus = el('button', '', '−');
    minus.type = 'button';
    minus.setAttribute('aria-label', 'Giảm số lượng');
    const count = el('span', 'tea-qty-value', '1');
    count.setAttribute('aria-live', 'polite');
    const plus = el('button', '', '+');
    plus.type = 'button';
    plus.setAttribute('aria-label', 'Tăng số lượng');
    stepper.append(minus, count, plus);
    actions.appendChild(stepper);

    let orderLink;
    if (product.outOfStock) {
      orderLink = el('span', 'tea-order-link is-sold-out', 'Hết hàng');
      orderLink.setAttribute('aria-disabled', 'true');
    } else {
      orderLink = el('a', 'tea-order-link');
      orderLink.href = product.url + '?quantity=1';
      orderLink.append(document.createTextNode('Đặt ngay '));
      const arrow = el('i', 'fas fa-arrow-right');
      arrow.setAttribute('aria-hidden', 'true');
      orderLink.appendChild(arrow);
      orderLink.setAttribute('aria-label', 'Đặt ngay ' + product.title);
    }
    actions.appendChild(orderLink);

    function updateQuantity() {
      count.textContent = String(quantity);
      minus.disabled = quantity <= 1;
      plus.disabled = quantity >= 99;
      if (orderLink.tagName === 'A') orderLink.href = product.url + '?quantity=' + quantity;
    }
    minus.addEventListener('click', function (event) { event.stopPropagation(); quantity = Math.max(1, quantity - 1); updateQuantity(); });
    plus.addEventListener('click', function (event) { event.stopPropagation(); quantity = Math.min(99, quantity + 1); updateQuantity(); });
    if (orderLink.tagName === 'A') orderLink.addEventListener('click', function (event) { event.stopPropagation(); });
    updateQuantity();
    page.appendChild(actions);

    const options = el('a', 'tea-product-options', 'Tùy chọn size · đường · đá →');
    options.href = product.url + '?quantity=1';
    options.addEventListener('click', function (event) { event.stopPropagation(); });
    page.appendChild(options);
    page.appendChild(pageNumber(String(productIndex + 4).padStart(2, '0')));
    rightPage.appendChild(page);
  }

  function renderSpread(state) {
    spreadIndex = state;
    if (state < 0 || !products.length) {
      leftPage.innerHTML = coverLeftHTML;
      rightPage.innerHTML = coverRightHTML;
    } else {
      const leftProductIndex = Math.max(0, state - 1);
      renderLeftProduct(leftProductIndex);
      renderRightProduct(state);
    }
    updateControls();
    refreshMagnifierCopy();
  }

  function updateControls() {
    const disabled = products.length === 0 || Boolean(activeTurn) || Boolean(pointerDrag);
    prevButton.disabled = disabled;
    nextButton.disabled = disabled;
    prevButton.setAttribute('aria-label', 'Lật về trang trước');
    nextButton.setAttribute('aria-label', 'Lật sang trang tiếp theo');
    pagesSurface.setAttribute('aria-label', spreadIndex < 0 ? 'Sổ thực đơn UTETra - trang giới thiệu' : 'Sổ thực đơn UTETra - món số ' + (spreadIndex + 1));
    zoomReadout.textContent = Math.round(zoom * 100) + '%';
    zoomOutButton.disabled = zoom <= 0.9 + 0.001;
    zoomInButton.disabled = zoom >= 1.22 - 0.001;
  }

  function nextIndex(from, direction) {
    if (!products.length) return -1;
    if (direction === 'next') {
      if (from < 0) return 0;
      return from >= products.length - 1 ? -1 : from + 1;
    }
    if (from < 0) return products.length - 1;
    return from === 0 ? -1 : from - 1;
  }

  function createPayload(page, offset) {
    const payload = el('div', 'tea-curl-content');
    payload.style.left = (-offset) + 'px';
    payload.style.width = (pagesSurface.clientWidth / 2) + 'px';
    payload.innerHTML = page.innerHTML;
    escapeIds(payload);
    payload.querySelectorAll('a,button,input,select,textarea').forEach(function (node) {
      node.setAttribute('tabindex', '-1');
      node.setAttribute('aria-hidden', 'true');
    });
    return payload;
  }

  function buildCurl(direction, fromPage, toPage) {
    curlHost.replaceChildren();
    const bookWidth = pagesSurface.clientWidth;
    const pageWidth = bookWidth / 2;
    const stripWidth = bookWidth * SPAN / STRIPS;
    const curl = el('div', 'tea-book-curl ' + direction);
    curl.style.setProperty('--bw', bookWidth + 'px');
    curl.style.setProperty('--span', SPAN);
    curl.style.setProperty('--n', STRIPS);
    const stripNodes = [];
    let host = curl;

    for (let i = 0; i < STRIPS; i += 1) {
      const strip = el('div', 'tea-book-strip');
      const front = el('div', 'tea-curl-face front');
      const back = el('div', 'tea-curl-face back');
      // A leaf's back face is mirrored at the spine: reverse the source slice order on that side.
      const offsetFrom = direction === 'next' ? i * stripWidth : Math.max(0, pageWidth - stripWidth - i * stripWidth);
      const offsetTo = direction === 'next' ? Math.max(0, pageWidth - stripWidth - i * stripWidth) : i * stripWidth;
      front.appendChild(createPayload(fromPage, offsetFrom));
      back.appendChild(createPayload(toPage, offsetTo));
      front.appendChild(el('div', 'tea-curl-shade'));
      back.appendChild(el('div', 'tea-curl-shade'));
      front.appendChild(el('div', 'tea-curl-gloss'));
      back.appendChild(el('div', 'tea-curl-gloss'));
      strip.append(front, back);
      host.appendChild(strip);
      host = strip;
      stripNodes.push(strip);
    }
    curlHost.appendChild(curl);
    return { curl: curl, strips: stripNodes, direction: direction, stripWidth: stripWidth };
  }

  function applyCurl(turn, t) {
    if (!turn) return;
    const th = Math.PI * t;
    const beta = BETA * Math.sin(Math.PI * t);
    const degrees = 180 / Math.PI;
    const total = th + beta;
    const turnAngle = (total * degrees).toFixed(2) + 'deg';
    const stripAngle = ((2 * beta / STRIPS) * degrees).toFixed(3) + 'deg';
    turn.curl.style.setProperty('--tt', turn.direction === 'next' ? turnAngle : turnAngle);
    turn.curl.style.setProperty('--td', stripAngle);
    turn.curl.style.setProperty('--shade', Math.sin(Math.PI * t).toFixed(3));
    turn.strips.forEach(function (strip, i) {
      const lightA = Math.abs(Math.cos(total - i * (2 * beta / STRIPS)));
      const lightB = Math.abs(Math.cos(total - (i + 1) * (2 * beta / STRIPS)));
      strip.style.setProperty('--lit', lightA.toFixed(3));
      strip.style.setProperty('--a1', ((1 - lightA) * .62).toFixed(3));
      strip.style.setProperty('--a2', ((1 - lightB) * .62).toFixed(3));
    });
  }

  function prepareTurn(direction) {
    if (!products.length) return null;
    const targetState = nextIndex(spreadIndex, direction);
    if (targetState === spreadIndex) return null;

    const sourcePage = (direction === 'next' ? rightPage : leftPage).cloneNode(true);
    const originalState = spreadIndex;
    renderSpread(targetState);
    const destinationPage = (direction === 'next' ? leftPage : rightPage).cloneNode(true);
    const curlData = buildCurl(direction, sourcePage, destinationPage);
    const turn = {
      direction: direction,
      fromState: originalState,
      targetState: targetState,
      curl: curlData.curl,
      strips: curlData.strips,
      t: 0
    };
    pagesSurface.classList.add('is-turning');
    activeTurn = turn;
    updateControls();
    applyCurl(turn, 0);
    refreshMagnifierCopy();
    return turn;
  }

  function finishTurn(commit) {
    if (!activeTurn) return;
    const completed = activeTurn;
    activeTurn = null;
    pointerDrag = null;
    curlHost.replaceChildren();
    pagesSurface.classList.remove('is-turning');
    renderSpread(commit ? completed.targetState : completed.fromState);
  }

  function animateTo(target, duration) {
    if (!activeTurn) return;
    const turn = activeTurn;
    if (reduceMotion || duration <= 1) {
      applyCurl(turn, target);
      finishTurn(target >= 0.999);
      return;
    }
    const startValue = turn.t;
    const startTime = performance.now();
    function frame(now) {
      if (activeTurn !== turn) return;
      const progress = Math.min(1, (now - startTime) / duration);
      const eased = progress * progress * (3 - 2 * progress);
      turn.t = startValue + (target - startValue) * eased;
      applyCurl(turn, turn.t);
      if (progress >= 1) finishTurn(target >= 0.999);
      else requestAnimationFrame(frame);
    }
    requestAnimationFrame(frame);
  }

  function turnByButton(direction) {
    if (activeTurn || pointerDrag || !products.length) return;
    const turn = prepareTurn(direction);
    if (!turn) return;
    if (reduceMotion) finishTurn(true);
    else animateTo(1, 860);
  }

  prevButton.addEventListener('click', function () { turnByButton('prev'); });
  nextButton.addEventListener('click', function () { turnByButton('next'); });

  pagesSurface.addEventListener('pointerdown', function (event) {
    if (event.button !== 0 || activeTurn || !products.length) return;
    if (event.target.closest('a,button,input,select,textarea,[role="button"]')) return;
    const rect = pagesSurface.getBoundingClientRect();
    const direction = event.clientX >= rect.left + rect.width / 2 ? 'next' : 'prev';
    const turn = prepareTurn(direction);
    if (!turn) return;
    event.preventDefault();
    pointerDrag = { pointerId: event.pointerId, direction: direction, startX: event.clientX, startY: event.clientY, lastX: event.clientX, lastTime: performance.now(), velocity: 0, moved: 0 };
    pagesSurface.setPointerCapture(event.pointerId);
    updateControls();
  });

  pagesSurface.addEventListener('pointermove', function (event) {
    if (!pointerDrag || !activeTurn || event.pointerId !== pointerDrag.pointerId) return;
    const drag = pointerDrag;
    const dx = event.clientX - drag.startX;
    const pageWidth = Math.max(1, pagesSurface.clientWidth / 2);
    const signed = drag.direction === 'next' ? -dx : dx;
    const t = Math.max(0, Math.min(1, signed / (pageWidth * .92)));
    const now = performance.now();
    drag.velocity = (event.clientX - drag.lastX) / Math.max(1, now - drag.lastTime);
    drag.lastX = event.clientX;
    drag.lastTime = now;
    drag.moved = Math.max(drag.moved, Math.abs(dx));
    activeTurn.t = t;
    applyCurl(activeTurn, t);
  });

  function endPointerDrag(event) {
    if (!pointerDrag || !activeTurn) return;
    if (event && event.pointerId !== undefined && event.pointerId !== pointerDrag.pointerId) return;
    const drag = pointerDrag;
    pointerDrag = null;
    updateControls();
    if (drag.moved < 7) {
      animateTo(1, 640);
      return;
    }
    const positiveVelocity = drag.direction === 'next' ? drag.velocity < -.35 : drag.velocity > .35;
    const commit = activeTurn.t > .38 || positiveVelocity;
    animateTo(commit ? 1 : 0, commit ? 470 : 390);
  }
  pagesSurface.addEventListener('pointerup', endPointerDrag);
  pagesSurface.addEventListener('pointercancel', endPointerDrag);
  pagesSurface.addEventListener('lostpointercapture', function () {
    if (pointerDrag && activeTurn) endPointerDrag({ pointerId: pointerDrag.pointerId });
  });
  pagesSurface.addEventListener('dragstart', function (event) { event.preventDefault(); });

  pagesSurface.addEventListener('keydown', function (event) {
    if (event.key === 'ArrowRight') { event.preventDefault(); turnByButton('next'); }
    if (event.key === 'ArrowLeft') { event.preventDefault(); turnByButton('prev'); }
  });

  stage.addEventListener('pointermove', function (event) {
    if (coarsePointer || pointerDrag || activeTurn || event.pointerType === 'touch') return;
    const rect = pagesSurface.getBoundingClientRect();
    if (!rect.width || !rect.height) return;
    const nx = Math.max(-1, Math.min(1, (event.clientX - rect.left - rect.width / 2) / (rect.width * .55)));
    const ny = Math.max(-1, Math.min(1, (event.clientY - rect.top - rect.height / 2) / (rect.height * .8)));
    frame.style.setProperty('--tb-rx', (-ny * 2.2).toFixed(2) + 'deg');
    frame.style.setProperty('--tb-ry', (nx * 3.2).toFixed(2) + 'deg');
  }, { passive: true });
  stage.addEventListener('pointerleave', function () {
    frame.style.setProperty('--tb-rx', '0deg');
    frame.style.setProperty('--tb-ry', '0deg');
  });

  function refreshMagnifierCopy() {
    if (!magnifierCopy) return;
    magnifierCopy.replaceChildren();
    const cloneBook = el('div', 'tea-magnifier-copy-book');
    const leftClone = leftPage.cloneNode(true);
    const rightClone = rightPage.cloneNode(true);
    escapeIds(leftClone);
    escapeIds(rightClone);
    leftClone.style.width = '100%';
    rightClone.style.width = '100%';
    leftClone.style.height = '100%';
    rightClone.style.height = '100%';
    cloneBook.style.width = pagesSurface.clientWidth + 'px';
    cloneBook.style.height = pagesSurface.clientHeight + 'px';
    cloneBook.style.gridTemplateColumns = 'minmax(0,1fr) minmax(0,1fr)';
    cloneBook.append(leftClone, rightClone);
    magnifierCopy.appendChild(cloneBook);
    magnifierCopyBook = cloneBook;
    positionMagnifierZoom();
  }

  function positionMagnifierZoom() {
    if (!magnifierCopyBook || !magnifier || !loupeEnabled) return;
    const stageRect = stage.getBoundingClientRect();
    const pagesRect = pagesSurface.getBoundingClientRect();
    const lensSize = magnifier.querySelector('.tea-magnifier-glass').clientWidth;
    const centerX = stageRect.left + loupeX + magnifier.clientWidth / 2;
    const centerY = stageRect.top + loupeY + magnifier.clientHeight / 2;
    const surfaceScale = zoom || 1;
    const xInPages = pagesSurface.clientWidth / 2 + (centerX - (pagesRect.left + pagesRect.width / 2)) / surfaceScale;
    const yInPages = pagesSurface.clientHeight / 2 + (centerY - (pagesRect.top + pagesRect.height / 2)) / surfaceScale;
    const scale = 1.9;
    magnifierCopyBook.style.transform = 'translate(' + (lensSize / 2 - xInPages * scale).toFixed(1) + 'px,' + (lensSize / 2 - yInPages * scale).toFixed(1) + 'px) scale(' + scale + ')';
  }

  function resetMagnifierPosition() {
    if (!magnifier) return;
    const size = magnifier.offsetWidth || 160;
    loupeX = Math.max(0, stage.clientWidth - size * .83);
    loupeY = Math.max(0, stage.clientHeight * .80);
    magnifier.style.left = loupeX + 'px';
    magnifier.style.top = loupeY + 'px';
    positionMagnifierZoom();
  }

  if (magnifier && magnifierCopy) {
    magnifier.addEventListener('pointerdown', function (event) {
      if (!loupeEnabled || event.button !== 0 || coarsePointer) return;
      event.preventDefault();
      event.stopPropagation();
      loupePointer = { id: event.pointerId, x: event.clientX, y: event.clientY, left: loupeX, top: loupeY };
      magnifier.setPointerCapture(event.pointerId);
      magnifier.style.cursor = 'grabbing';
    });
    magnifier.addEventListener('pointermove', function (event) {
      if (!loupePointer || event.pointerId !== loupePointer.id) return;
      const size = magnifier.offsetWidth;
      loupeX = Math.max(-size * .2, Math.min(stage.clientWidth - size * .45, loupePointer.left + event.clientX - loupePointer.x));
      loupeY = Math.max(-size * .2, Math.min(stage.clientHeight - size * .35, loupePointer.top + event.clientY - loupePointer.y));
      magnifier.style.left = loupeX + 'px';
      magnifier.style.top = loupeY + 'px';
      positionMagnifierZoom();
    });
    function releaseMagnifier() { loupePointer = null; magnifier.style.cursor = 'grab'; }
    magnifier.addEventListener('pointerup', releaseMagnifier);
    magnifier.addEventListener('pointercancel', releaseMagnifier);
    magnifier.addEventListener('lostpointercapture', releaseMagnifier);
    magnifier.addEventListener('keydown', function (event) {
      const step = event.shiftKey ? 28 : 12;
      if (event.key === 'ArrowLeft') loupeX -= step;
      else if (event.key === 'ArrowRight') loupeX += step;
      else if (event.key === 'ArrowUp') loupeY -= step;
      else if (event.key === 'ArrowDown') loupeY += step;
      else return;
      event.preventDefault();
      loupeX = Math.max(-20, Math.min(stage.clientWidth - magnifier.offsetWidth * .45, loupeX));
      loupeY = Math.max(-20, Math.min(stage.clientHeight - magnifier.offsetHeight * .35, loupeY));
      magnifier.style.left = loupeX + 'px';
      magnifier.style.top = loupeY + 'px';
      positionMagnifierZoom();
    });
  }

  loupeToggle.addEventListener('click', function () {
    loupeEnabled = !loupeEnabled;
    magnifier.hidden = !loupeEnabled;
    loupeToggle.setAttribute('aria-pressed', String(loupeEnabled));
    if (loupeEnabled) positionMagnifierZoom();
  });

  function setZoom(nextZoom) {
    zoom = Math.max(.9, Math.min(1.22, nextZoom));
    pagesSurface.style.setProperty('--tb-zoom', zoom.toFixed(3));
    updateControls();
    positionMagnifierZoom();
  }
  zoomInButton.addEventListener('click', function () { setZoom(zoom * 1.12); });
  zoomOutButton.addEventListener('click', function () { setZoom(zoom / 1.12); });

  window.addEventListener('resize', function () {
    window.cancelAnimationFrame(resizeFrame);
    resizeFrame = window.requestAnimationFrame(function () {
      if (activeTurn && !pointerDrag) finishTurn(false);
      resetMagnifierPosition();
      refreshMagnifierCopy();
    });
  });

  // Preload the local tea images so the first page curl does not wait for image decoding.
  products.forEach(function (product) {
    if (!product.image) return;
    const image = new Image();
    image.src = product.image;
  });
  renderSpread(-1);
  if (coarsePointer) magnifier.hidden = true;
  resetMagnifierPosition();
  refreshMagnifierCopy();
})();
