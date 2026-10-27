/* UTeTra - giao diện khách hàng */
(function () {
    'use strict';

    /* ----- Tự ẩn thông báo thành công sau 4 giây ----- */
    document.querySelectorAll('.alert.auto-hide').forEach(function (el) {
        setTimeout(function () {
            if (window.bootstrap) { bootstrap.Alert.getOrCreateInstance(el).close(); } else { el.remove(); }
        }, 4000);
    });

    /* ----- Popup "Chọn phương thức nhận hàng" ----- */
    var form = document.getElementById('receiveForm');
    if (form) {
        var methodInput = document.getElementById('receiveMethod');
        var addressBox = document.getElementById('recvAddressBox');
        var addressInput = document.getElementById('recvAddress');
        var branchLabel = document.getElementById('recvBranchLabel');
        var tabs = form.querySelectorAll('[data-recv]');
        var msg = document.getElementById('recvLocateMsg');

        function setMethod(m) {
            methodInput.value = m;
            tabs.forEach(function (t) { t.classList.toggle('active', t.getAttribute('data-recv') === m); });
            var delivery = (m === 'DELIVERY');
            addressBox.style.display = delivery ? '' : 'none';
            addressInput.required = delivery;
            branchLabel.textContent = delivery ? 'Cửa hàng phục vụ' : 'Chọn cửa hàng để đến lấy';
        }
        tabs.forEach(function (t) {
            t.addEventListener('click', function () { setMethod(t.getAttribute('data-recv')); });
        });
        setMethod(methodInput.value === 'PICKUP' ? 'PICKUP' : 'DELIVERY');

        // Sau khi chọn xong, quay lại đúng trang đang xem
        document.getElementById('receiveReturn').value = location.pathname + location.search;

        // "Vị trí hiện tại": chỉ chạy khi khách bấm; địa chỉ điền vào ô vẫn sửa được
        var locate = document.getElementById('recvLocate');
        if (locate) {
            locate.addEventListener('click', function () {
                if (!navigator.geolocation) { msg.textContent = 'Trình duyệt không hỗ trợ định vị.'; return; }
                msg.textContent = 'Đang lấy vị trí...';
                navigator.geolocation.getCurrentPosition(function (pos) {
                    var lat = pos.coords.latitude, lon = pos.coords.longitude;
                    document.getElementById('receiveLat').value = lat;
                    document.getElementById('receiveLng').value = lon;
                    // Gợi ý cửa hàng gần nhất (chỉ xét cửa hàng đã có toạ độ)
                    fetch(ctxPath() + '/receive/nearest?lat=' + encodeURIComponent(lat) + '&lng=' + encodeURIComponent(lon),
                          { credentials: 'same-origin' })
                        .then(function (r) { return r.json(); })
                        .then(function (n) {
                            if (!n || !n.found) return;
                            var radio = form.querySelector('input[name="branchId"][value="' + n.id + '"]');
                            if (radio) {
                                radio.checked = true;
                                msg.textContent = 'Cửa hàng gần bạn nhất: ' + n.name + ' (~' + n.km + ' km) đã được chọn sẵn.';
                            }
                        }).catch(function () { });
                    fetch('https://nominatim.openstreetmap.org/reverse?format=jsonv2&accept-language=vi&lat='
                        + encodeURIComponent(lat) + '&lon=' + encodeURIComponent(lon))
                        .then(function (r) { return r.json(); })
                        .then(function (d) {
                            if (d && d.display_name) {
                                addressInput.value = d.display_name.substring(0, 500);
                                msg.textContent = 'Đã điền địa chỉ theo vị trí của bạn, hãy kiểm tra lại nhé.';
                            } else { msg.textContent = 'Không xác định được địa chỉ, vui lòng nhập tay.'; }
                        })
                        .catch(function () { msg.textContent = 'Không lấy được địa chỉ, vui lòng nhập tay.'; });
                }, function () {
                    msg.textContent = 'Bạn chưa cho phép định vị, vui lòng nhập địa chỉ.';
                }, { timeout: 8000 });
            });
        }
    }

    /* ----- Đường dẫn gốc của ứng dụng (context path) ----- */
    function ctxPath() {
        var b = document.body.getAttribute('data-ctx');
        return b == null ? '' : b;
    }

    /* ----- Tìm kiếm sản phẩm: lịch sử, từ khóa phổ biến và gợi ý trực tiếp ----- */
    var sForm = document.querySelector('.header-search');
    var sInput = document.querySelector('.header-search input[name="kw"]');
    var sBox = document.getElementById('searchSuggest');
    var sClear = document.getElementById('searchInputClear');
    if (sForm && sInput && sBox) {
        var timer = null, seq = 0;
        var RECENT_KEY = 'utetra_search_recent_v1';
        var popularKeywords = [
            'Trà sữa trân châu UTe', 'Trà sữa matcha', 'Trà đào cam sả',
            'Trà sữa', 'Trà trái cây', 'Bánh ngọt', 'Matcha',
            'Trân châu đen', 'Pudding trứng', 'Kem phô mai'
        ];

        function getRecent() {
            try {
                var value = JSON.parse(window.localStorage.getItem(RECENT_KEY) || '[]');
                return Array.isArray(value) ? value.filter(function (x) {
                    return typeof x === 'string' && x.trim();
                }).slice(0, 6) : [];
            } catch (ignore) { return []; }
        }
        function saveRecent(term) {
            term = (term || '').trim().replace(/\s+/g, ' ');
            if (!term) return;
            try {
                var normalized = term.toLocaleLowerCase('vi');
                var items = getRecent().filter(function (x) {
                    return x.toLocaleLowerCase('vi') !== normalized;
                });
                items.unshift(term.slice(0, 100));
                window.localStorage.setItem(RECENT_KEY, JSON.stringify(items.slice(0, 6)));
            } catch (ignore) { /* Trình duyệt có thể chặn localStorage; tìm kiếm vẫn hoạt động. */ }
        }
        function setOpen(open) {
            sBox.hidden = !open;
            sInput.setAttribute('aria-expanded', open ? 'true' : 'false');
        }
        function updateClearButton() {
            if (sClear) sClear.hidden = !sInput.value.trim();
        }
        function hide() {
            seq++;
            clearTimeout(timer);
            setOpen(false);
            sBox.textContent = '';
        }
        function el(tag, className, text) {
            var node = document.createElement(tag);
            if (className) node.className = className;
            if (text !== undefined && text !== null) node.textContent = text;
            return node;
        }
        function submitTerm(term) {
            sInput.value = term;
            updateClearButton();
            saveRecent(term);
            if (typeof sForm.requestSubmit === 'function') sForm.requestSubmit();
            else sForm.submit();
        }
        function makeChip(term, kind) {
            var button = el('button', 'suggest-keyword' + (kind ? ' ' + kind : ''), term);
            button.type = 'button';
            button.addEventListener('click', function () { submitTerm(term); });
            return button;
        }
        function renderRecentPopular() {
            sBox.textContent = '';

            var recentSection = el('section', 'suggest-section');
            var recentHead = el('div', 'suggest-heading');
            recentHead.appendChild(el('strong', '', 'Tìm kiếm gần đây'));
            var clearButton = el('button', 'suggest-clear', 'Xóa tất cả');
            clearButton.type = 'button';
            clearButton.addEventListener('click', function () {
                try { window.localStorage.removeItem(RECENT_KEY); } catch (ignore) { }
                renderRecentPopular();
                sInput.focus();
            });
            recentHead.appendChild(clearButton);
            recentSection.appendChild(recentHead);
            var recentItems = el('div', 'suggest-keywords');
            var recent = getRecent();
            if (!recent.length) {
                recentItems.appendChild(el('span', 'suggest-empty', 'Các từ khóa bạn tìm sẽ xuất hiện ở đây.'));
            } else {
                recent.forEach(function (term) { recentItems.appendChild(makeChip(term, 'recent')); });
            }
            recentSection.appendChild(recentItems);
            sBox.appendChild(recentSection);

            sBox.appendChild(el('div', 'suggest-divider'));
            var popularSection = el('section', 'suggest-section suggest-section-popular');
            popularSection.appendChild(el('div', 'suggest-heading', 'Từ khóa phổ biến'));
            var popularItems = el('div', 'suggest-keywords');
            popularKeywords.forEach(function (term) { popularItems.appendChild(makeChip(term, 'popular')); });
            popularSection.appendChild(popularItems);
            sBox.appendChild(popularSection);
            setOpen(true);
        }
        function renderProducts(list, term) {
            sBox.textContent = '';
            var heading = el('div', 'suggest-heading suggest-results-heading', 'Sản phẩm phù hợp');
            sBox.appendChild(heading);
            if (!list || !list.length) {
                sBox.appendChild(el('div', 'suggest-no-results', 'Chưa tìm thấy món phù hợp với “' + term + '”.'));
                var all = el('a', 'suggest-all-results', 'Tìm tất cả sản phẩm');
                all.href = ctxPath() + '/products?kw=' + encodeURIComponent(term);
                sBox.appendChild(all);
                setOpen(true);
                return;
            }
            list.forEach(function (p) {
                var a = el('a', 'suggest-item');
                a.href = ctxPath() + '/product/' + encodeURIComponent(p.id);
                a.setAttribute('role', 'option');
                if (p.image) {
                    var img = el('img');
                    img.src = p.image; img.alt = ''; img.loading = 'lazy';
                    a.appendChild(img);
                } else {
                    a.appendChild(el('span', 'suggest-no-image', '☕'));
                }
                var name = el('span', 'suggest-name', p.name + (p.outOfStock ? ' (hết hàng)' : ''));
                var price = el('span', 'suggest-price', Math.round(Number(p.price) || 0).toLocaleString('vi-VN') + ' ₫');
                a.appendChild(name);
                a.appendChild(price);
                a.addEventListener('click', function () { saveRecent(term); });
                sBox.appendChild(a);
            });
            var allLink = el('a', 'suggest-all-results', 'Xem tất cả kết quả cho “' + term + '”');
            allLink.href = ctxPath() + '/products?kw=' + encodeURIComponent(term);
            sBox.appendChild(allLink);
            setOpen(true);
        }
        function requestSuggestions(term) {
            clearTimeout(timer);
            var mine = ++seq;
            if (term.length < 2) {
                renderRecentPopular();
                return;
            }
            timer = setTimeout(function () {
                fetch(ctxPath() + '/products/suggest?q=' + encodeURIComponent(term), { credentials: 'same-origin' })
                    .then(function (r) { return r.ok ? r.json() : []; })
                    .then(function (list) {
                        if (mine === seq && document.activeElement === sInput) renderProducts(list, term);
                    })
                    .catch(function () {
                        if (mine === seq && document.activeElement === sInput) renderRecentPopular();
                    });
            }, 220);
        }

        sInput.addEventListener('focus', function () {
            updateClearButton();
            renderRecentPopular();
        });
        sInput.addEventListener('click', function () {
            if (sBox.hidden) renderRecentPopular();
        });
        sInput.addEventListener('input', function () {
            updateClearButton();
            requestSuggestions(sInput.value.trim());
        });
        if (sClear) {
            sClear.addEventListener('click', function () {
                sInput.value = '';
                updateClearButton();
                requestSuggestions('');
                sInput.focus();
            });
        }
        updateClearButton();
        sForm.addEventListener('submit', function () { saveRecent(sInput.value); });
        document.addEventListener('click', function (event) {
            if (!sForm.contains(event.target)) hide();
        });
        sInput.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') hide();
        });
    }

    /* ----- Chống bấm 2 lần + hiện vòng xoay khi gửi form ----- */
    document.addEventListener('submit', function (e) {
        var f = e.target;
        if (!f || f.tagName !== 'FORM' || f.hasAttribute('data-no-lock') || e.defaultPrevented) return;
        // Chờ 1 nhịp để dữ liệu của nút (name/value) vẫn được gửi đi rồi mới khoá
        setTimeout(function () {
            var btns = Array.prototype.slice.call(f.querySelectorAll('button[type="submit"], button:not([type])'));
            if (f.id) {
                btns = btns.concat(Array.prototype.slice.call(document.querySelectorAll('button[form="' + f.id + '"]')));
            }
            btns.forEach(function (b) {
                if (b.hasAttribute('data-no-lock') || b.disabled) return;
                b.disabled = true;
                b.classList.add('is-loading');
                var sp = document.createElement('span');
                sp.className = 'spinner-border spinner-border-sm ms-2';
                sp.setAttribute('aria-hidden', 'true');
                b.appendChild(sp);
                b.setAttribute('data-locked', '1');
            });
        }, 0);
    });
    // Nếu sau 12 giây trang vẫn chưa chuyển đi (mạng lỗi, bị chặn chuyển hướng...), mở khoá để khách thử lại
    function unlockAll() {
        document.querySelectorAll('button[data-locked]').forEach(function (b) {
            b.disabled = false; b.classList.remove('is-loading'); b.removeAttribute('data-locked');
            var sp = b.querySelector('.spinner-border'); if (sp) sp.remove();
        });
    }
    document.addEventListener('submit', function (e) {
        var f = e.target;
        if (!f || f.tagName !== 'FORM' || f.hasAttribute('data-no-lock') || e.defaultPrevented) return;
        setTimeout(unlockAll, 12000);
    });
    // Quay lại bằng nút Back của trình duyệt: mở khoá lại các nút
    window.addEventListener('pageshow', function (ev) {
        if (!ev.persisted) return;
        document.querySelectorAll('button[data-locked]').forEach(function (b) {
            b.disabled = false; b.classList.remove('is-loading'); b.removeAttribute('data-locked');
            var sp = b.querySelector('.spinner-border'); if (sp) sp.remove();
        });
    });
})();


/* ----- Bảng thông báo dạng drawer: Tin tức / Đơn hàng ----- */
(function () {
    'use strict';
    var bell = document.getElementById('notificationBell');
    var drawer = document.getElementById('notificationDrawer');
    var backdrop = document.getElementById('notificationBackdrop');
    var close = document.getElementById('notificationClose');
    if (!bell || !drawer || !backdrop) return;
    var ctx = document.body.getAttribute('data-ctx') || '';
    if (window.location.pathname.replace(/\/+$/, '').endsWith('/notifications')) {
        drawer.querySelectorAll('.ute-notification-item.unread').forEach(function (item) { item.classList.remove('unread'); });
        var existingBadge = document.getElementById('notificationUnreadBadge');
        if (existingBadge) existingBadge.remove();
    }
    var opened = false;
    var readMarked = false;

    function setTab(name) {
        drawer.querySelectorAll('[data-notification-tab]').forEach(function (tab) {
            var active = tab.getAttribute('data-notification-tab') === name;
            tab.classList.toggle('active', active);
            tab.setAttribute('aria-selected', active ? 'true' : 'false');
        });
        drawer.querySelectorAll('[data-notification-panel]').forEach(function (panel) {
            panel.hidden = panel.getAttribute('data-notification-panel') !== name;
        });
    }
    function markRead() {
        if (readMarked) return;
        readMarked = true;
        drawer.querySelectorAll('.ute-notification-item.unread').forEach(function (item) { item.classList.remove('unread'); });
        var badge = document.getElementById('notificationUnreadBadge');
        if (badge) badge.remove();
        fetch(ctx + '/notifications/read-all', { method: 'POST', credentials: 'same-origin', headers: { 'X-Requested-With': 'XMLHttpRequest' } })
            .catch(function () { readMarked = false; });
    }
    function openDrawer() {
        opened = true;
        drawer.hidden = false;
        backdrop.hidden = false;
        drawer.setAttribute('aria-hidden', 'false');
        bell.setAttribute('aria-expanded', 'true');
        document.body.classList.add('ute-notification-open');
        markRead();
    }
    function closeDrawer() {
        opened = false;
        drawer.hidden = true;
        backdrop.hidden = true;
        drawer.setAttribute('aria-hidden', 'true');
        bell.setAttribute('aria-expanded', 'false');
        document.body.classList.remove('ute-notification-open');
        bell.focus({ preventScroll: true });
    }
    bell.addEventListener('click', function () { opened ? closeDrawer() : openDrawer(); });
    if (close) close.addEventListener('click', closeDrawer);
    backdrop.addEventListener('click', closeDrawer);
    drawer.querySelectorAll('[data-notification-tab]').forEach(function (tab) {
        tab.addEventListener('click', function () { setTab(tab.getAttribute('data-notification-tab')); });
    });
    document.addEventListener('keydown', function (event) { if (opened && event.key === 'Escape') closeDrawer(); });
    drawer.querySelectorAll('a').forEach(function (link) { link.addEventListener('click', function () { document.body.classList.remove('ute-notification-open'); }); });
})();
