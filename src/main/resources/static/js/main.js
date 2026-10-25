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
})();
