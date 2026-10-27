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

    /* ----- Gợi ý tìm kiếm khi gõ ----- */
    var sInput = document.querySelector('.header-search input[name="kw"]');
    var sBox = document.getElementById('searchSuggest');
    if (sInput && sBox) {
        var timer = null, seq = 0;
        function hide() { sBox.hidden = true; sBox.textContent = ''; }
        function render(list) {
            sBox.textContent = '';
            if (!list.length) { hide(); return; }
            list.forEach(function (p) {
                var a = document.createElement('a');
                a.href = ctxPath() + '/product/' + encodeURIComponent(p.id);
                a.className = 'suggest-item';
                a.setAttribute('role', 'option');
                if (p.image) {
                    var img = document.createElement('img');
                    img.src = p.image; img.alt = ''; img.loading = 'lazy';
                    a.appendChild(img);
                }
                var name = document.createElement('span');
                name.className = 'suggest-name';
                name.textContent = p.name + (p.outOfStock ? ' (hết hàng)' : '');
                var price = document.createElement('span');
                price.className = 'suggest-price';
                price.textContent = Math.round(p.price).toLocaleString('vi-VN') + ' ₫';
                a.appendChild(name); a.appendChild(price);
                sBox.appendChild(a);
            });
            sBox.hidden = false;
        }
        sInput.addEventListener('input', function () {
            var q = sInput.value.trim();
            clearTimeout(timer);
            if (q.length < 2) { hide(); return; }
            var mine = ++seq;
            timer = setTimeout(function () {
                fetch(ctxPath() + '/products/suggest?q=' + encodeURIComponent(q), { credentials: 'same-origin' })
                    .then(function (r) { return r.ok ? r.json() : []; })
                    .then(function (list) { if (mine === seq) render(list); })
                    .catch(hide);
            }, 250);
        });
        document.addEventListener('click', function (e) { if (!sBox.contains(e.target) && e.target !== sInput) hide(); });
        sInput.addEventListener('keydown', function (e) { if (e.key === 'Escape') hide(); });
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
