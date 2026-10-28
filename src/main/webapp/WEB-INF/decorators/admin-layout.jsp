<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title><sitemesh:write property="title" /> - UTETra Admin</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link
	href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Playfair+Display:ital,wght@0,400;0,500;0,600;0,700;1,400;1,500;1,600&display=swap&subset=vietnamese,latin,latin-ext"
	rel="stylesheet">
<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/static/css/admin.css?v=20261028-logo-transparent">
<sitemesh:write property="head" />
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/static/css/typography-loader.css">
</head>
<body class="admin-theme">
	<div id="uteSiteLoader" class="ute-site-loader" role="status"
		aria-live="polite" aria-label="Đang tải trang UTETra">
		<div class="ute-site-loader__inner">
			<img class="ute-site-loader__logo"
				src="${pageContext.request.contextPath}/static/images/utetra-logo.png"
				alt="UTETra" width="340" height="207" fetchpriority="high">
			<div class="ute-site-loader__percent">
				<span id="uteSiteLoaderPercent">0</span><span aria-hidden="true">%</span>
			</div>
			<div class="ute-site-loader__track" role="progressbar"
				aria-label="Tiến trình tải trang" aria-valuemin="0"
				aria-valuemax="100" aria-valuenow="0" id="uteSiteLoaderProgress">
				<span id="uteSiteLoaderBar"></span>
			</div>
			<p class="ute-site-loader__caption">ĐANG CHUẨN BỊ TRÀ NGON</p>
		</div>
	</div>


	<div class="d-flex" style="min-height: 100vh;">
		<!-- Sidebar: Cố định chiều rộng 260px, luôn sticky, tự cuộn nếu menu dài -->
		<aside class="ute-admin-sidebar text-white p-3">
			<div class="ute-admin-brand mb-4 mt-1">
				<a class="ute-admin-brand__link"
					href="${pageContext.request.contextPath}/admin/dashboard"
					aria-label="UTETra - Trang quản trị"> <img
					class="ute-admin-brand__logo"
					src="${pageContext.request.contextPath}/static/images/utetra-logo.png"
					alt="UTETra - Tea for you" width="166" height="101">
				</a>
				<div class="ute-admin-brand__caption">QUẢN TRỊ HỆ THỐNG</div>
			</div>
			<hr class="ute-admin-divider" />
			<ul class="nav flex-column">
				<%-- ============ Nhóm ADMIN + MANAGER ============ --%>
				<c:if
					test="${sessionScope.roles.contains('ROLE_ADMIN')
                || sessionScope.roles.contains('ROLE_MANAGER')}">
					<li><a class="nav-link text-white"
						href="${pageContext.request.contextPath}/admin/dashboard"><i
							class="fas fa-tachometer-alt"></i> Dashboard</a></li>
				</c:if>
				<c:if
					test="${sessionScope.roles.contains('ROLE_ADMIN')
                || sessionScope.roles.contains('ROLE_MANAGER')}">
					<li><a class="nav-link text-white"
						href="${pageContext.request.contextPath}/admin/products"><i
							class="fas fa-coffee"></i> Sản phẩm</a></li>
				</c:if>

				<%-- ============ Chỉ ADMIN: Danh mục ============ --%>
				<c:if test="${sessionScope.roles.contains('ROLE_ADMIN')}">
					<li><a class="nav-link text-white"
						href="${pageContext.request.contextPath}/admin/categories"><i
							class="fas fa-list"></i> Danh mục</a></li>
				</c:if>

				<%-- ============ Nhóm ADMIN + MANAGER: Topping ============ --%>
				<c:if
					test="${sessionScope.roles.contains('ROLE_ADMIN')
                || sessionScope.roles.contains('ROLE_MANAGER')}">
					<li><a class="nav-link text-white"
						href="${pageContext.request.contextPath}/admin/toppings"><i
							class="fas fa-ice-cream"></i> Topping</a></li>
				</c:if>

				<%-- ============ Chỉ ADMIN: Thuộc tính (Size/Sugar/Ice) ============ --%>
				<c:if test="${sessionScope.roles.contains('ROLE_ADMIN')}">
					<li><a
						class="nav-link text-white d-flex justify-content-between align-items-center"
						data-bs-toggle="collapse" href="#attributeMenu" role="button"
						aria-expanded="${fn:contains(pageContext.request.requestURI, '/admin/sizes') || fn:contains(pageContext.request.requestURI, '/admin/sugars') || fn:contains(pageContext.request.requestURI, '/admin/ices') ? 'true' : 'false'}"
						aria-controls="attributeMenu"> <span><i
								class="fas fa-sliders-h"></i> Thuộc tính</span> <i
							class="fas fa-chevron-down d-inline-block"
							style="font-size: 0.7rem; transition: transform 0.2s;"></i>
					</a>
						<div
							class="collapse ${fn:contains(pageContext.request.requestURI, '/admin/sizes') || fn:contains(pageContext.request.requestURI, '/admin/sugars') || fn:contains(pageContext.request.requestURI, '/admin/ices') ? 'show' : ''}"
							id="attributeMenu">
							<ul class="nav flex-column ms-3 mt-1 mb-2"
								style="border-left: 1px solid rgba(255, 255, 255, 0.1);">
								<li><a class="nav-link text-white py-1"
									style="font-size: 0.85rem;"
									href="${pageContext.request.contextPath}/admin/sizes"><i
										class="fas fa-ruler" style="font-size: 0.75rem;"></i> Kích cỡ
										(Size)</a></li>
								<li><a class="nav-link text-white py-1"
									style="font-size: 0.85rem;"
									href="${pageContext.request.contextPath}/admin/sugars"><i
										class="fas fa-cubes-stacked" style="font-size: 0.75rem;"></i>
										Mức đường</a></li>
								<li><a class="nav-link text-white py-1"
									style="font-size: 0.85rem;"
									href="${pageContext.request.contextPath}/admin/ices"><i
										class="fas fa-snowflake" style="font-size: 0.75rem;"></i> Mức
										đá</a></li>
							</ul>
						</div></li>
				</c:if>

				<%-- ============ Chỉ ADMIN: Chi nhánh ============ --%>
				<c:if test="${sessionScope.roles.contains('ROLE_ADMIN')}">
					<li><a class="nav-link text-white"
						href="${pageContext.request.contextPath}/admin/branches"><i
							class="fas fa-store"></i> Chi nhánh</a></li>
				</c:if>

				<%-- ============ Nhóm ADMIN + MANAGER: Voucher, Khuyến mãi ============ --%>
				<c:if
					test="${sessionScope.roles.contains('ROLE_ADMIN')
                || sessionScope.roles.contains('ROLE_MANAGER')}">
					<li><a class="nav-link text-white"
						href="${pageContext.request.contextPath}/admin/vouchers"><i
							class="fas fa-ticket"></i> Mã giảm giá</a></li>
					<li><a class="nav-link text-white"
						href="${pageContext.request.contextPath}/admin/promotions"><i
							class="fas fa-bullhorn"></i> Khuyến mãi</a></li>
				</c:if>

				<%-- ============ Đơn hàng: ADMIN + MANAGER + STAFF ============ --%>
				<c:if
					test="${sessionScope.roles.contains('ROLE_ADMIN')
            || sessionScope.roles.contains('ROLE_MANAGER')
            || sessionScope.roles.contains('ROLE_STAFF')}">
					<li><a class="nav-link text-white"
						href="${pageContext.request.contextPath}/admin/orders"><i
							class="fas fa-receipt"></i> Đơn hàng</a></li>
				</c:if>

				<%-- ============ Người dùng: ADMIN + MANAGER ============ --%>
				<c:if
					test="${sessionScope.roles.contains('ROLE_ADMIN')
            || sessionScope.roles.contains('ROLE_MANAGER')}">
					<li><a class="nav-link text-white"
						href="${pageContext.request.contextPath}/admin/users"><i
							class="fas fa-users"></i> Người dùng</a></li>
				</c:if>

				<li><a class="nav-link text-white" target="_blank"
					rel="noopener" href="${pageContext.request.contextPath}/"><i
						class="fas fa-globe"></i> Xem trang web</a></li>
				<li><a class="nav-link text-white"
					href="${pageContext.request.contextPath}/logout"><i
						class="fas fa-sign-out-alt"></i> Đăng xuất</a></li>
			</ul>
		</aside>

		<!-- Nội dung chính: Cho phép co giãn lấp đầy khoảng trống còn lại -->
		<div class="ute-admin-main flex-grow-1"
			style="min-width: 0; overflow-x: hidden;">
			<header
				class="ute-admin-topbar p-3 d-flex justify-content-between align-items-center sticky-top">
				<h5 class="ute-admin-page-title mb-0">
					<sitemesh:write property="title" />
				</h5>
				<div class="d-flex align-items-center">
					<div class="dropdown me-3">
						<button type="button" id="noti-bell"
							class="btn btn-link text-dark p-0 position-relative"
							data-bs-toggle="dropdown" data-bs-auto-close="outside"
							aria-expanded="false" title="Đơn hàng mới">
							<i class="fas fa-bell fa-lg"></i> <span id="noti-count"
								class="badge bg-danger position-absolute top-0 start-100 translate-middle d-none">0</span>
						</button>
						<div class="dropdown-menu dropdown-menu-end p-0 shadow"
							style="width: 360px; max-width: 92vw;">
							<div
								class="d-flex justify-content-between align-items-center px-3 py-2 border-bottom">
								<b>Đơn hàng mới</b> <a class="small"
									href="${pageContext.request.contextPath}/admin/orders">Xem
									tất cả</a>
							</div>
							<div id="noti-list" style="max-height: 380px; overflow-y: auto;">
								<div class="text-center text-muted small py-4">Đang tải...</div>
							</div>
						</div>
					</div>
					<span class="text-muted small">Xin chào, <b>${fn:escapeXml(sessionScope.fullName)}</b></span>
				</div>
			</header>
			<div class="ute-admin-content p-4">
				<sitemesh:write property="body" />
			</div>
		</div>
	</div>

	<div class="toast-container position-fixed top-0 end-0 p-3"
		id="noti-toasts" style="z-index: 2000;"></div>
	<%-- SỬA (Bước 2): Gắn src trỏ tới file chuông --%>
	<audio id="noti-sound" preload="auto"
		src="${pageContext.request.contextPath}/static/sounds/chuon.mp3"></audio>
	<script
		src="https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js"></script>
	<script
		src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
	<script
		src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
	<script
		src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
	<script>
    (function() {
        var ctx = '${pageContext.request.contextPath}';
        var KEY = 'adminLastSeenOrderId';
        var items = [];

        // SỬA (Bước 3a + 4): Quản lý âm thanh chuông thông báo
        var notiAudio = document.getElementById('noti-sound');
        if (notiAudio) notiAudio.volume = 0.6;   // Bước 4: 0.0 – 1.0
        var audioUnlocked = false;

        function primeAudio() {
            if (audioUnlocked || !notiAudio) return;
            notiAudio.muted = true;
            var p = notiAudio.play();
            if (p && p.then) {
                p.then(function () {
                    notiAudio.pause();
                    notiAudio.currentTime = 0;
                    notiAudio.muted = false;
                    audioUnlocked = true;
                    removeUnlockListeners();
                }).catch(function () {
                    notiAudio.muted = false;
                });
            } else {
                notiAudio.muted = false;
            }
        }
        function removeUnlockListeners() {
            ['click', 'keydown', 'touchstart', 'pointerdown'].forEach(function (ev) {
                document.removeEventListener(ev, primeAudio, true);
            });
        }
        ['click', 'keydown', 'touchstart', 'pointerdown'].forEach(function (ev) {
            document.addEventListener(ev, primeAudio, true);
        });

        function playNotiSound() {
            if (!notiAudio) return;
            notiAudio.currentTime = 0;
            var p = notiAudio.play();
            if (p && p.catch) p.catch(function () { primeAudio(); });
        }

        function getSeen() { try { return parseInt(localStorage.getItem(KEY)) || 0; } catch (e) { return 0; } }
        function setSeen(v) { try { localStorage.setItem(KEY, String(v)); } catch (e) { } }
        function money(v) { return Number(v || 0).toLocaleString('vi-VN') + ' ₫'; }
        function ago(iso) {
            if (!iso) return '';
            var s = Math.max(0, Math.floor((Date.now() - new Date(iso).getTime()) / 1000));
            if (s < 60) return 'vừa xong';
            if (s < 3600) return Math.floor(s / 60) + ' phút trước';
            if (s < 86400) return Math.floor(s / 3600) + ' giờ trước';
            return Math.floor(s / 86400) + ' ngày trước';
        }
        function unreadCount() {
            var seen = getSeen();
            return items.filter(function (o) { return o.id > seen; }).length;
        }
        function renderBadge() {
            var n = unreadCount();
            var b = document.getElementById('noti-count');
            b.innerText = n > 9 ? '9+' : String(n);
            b.classList.toggle('d-none', n === 0);
        }
        function renderList() {
            var box = document.getElementById('noti-list');
            var seen = getSeen();
            box.textContent = '';
            if (!items.length) {
                var empty = document.createElement('div');
                empty.className = 'text-center text-muted small py-4';
                empty.textContent = 'Chưa có đơn hàng nào';
                box.appendChild(empty);
                return;
            }
            items.forEach(function (o) {
                var a = document.createElement('a');
                a.href = ctx + '/admin/orders/detail/' + encodeURIComponent(o.id);
                a.className = 'd-block px-3 py-2 border-bottom text-decoration-none text-dark' + (o.id > seen ? ' bg-warning-subtle' : '');
                var l1 = document.createElement('div');
                l1.className = 'd-flex justify-content-between';
                var who = document.createElement('span');
                var strong = document.createElement('b');
                strong.textContent = o.customer;
                who.appendChild(strong);
                who.appendChild(document.createTextNode(' đã đặt hàng'));
                var amt = document.createElement('span');
                amt.className = 'text-success fw-bold';
                amt.textContent = money(o.total);
                l1.appendChild(who); l1.appendChild(amt);
                var l2 = document.createElement('div');
                l2.className = 'small text-muted';
                var pay = o.payment ? ' · ' + o.payment + (o.paid ? ' (đã trả)' : '') : '';
                l2.textContent = o.code + pay + ' · ' + o.statusLabel + ' · ' + ago(o.createdAt);
                a.appendChild(l1); a.appendChild(l2);
                box.appendChild(a);
            });
        }
        function load() {
            return fetch(ctx + '/admin/orders/recent', { credentials: 'same-origin' })
                .then(function (r) { return r.ok ? r.json() : []; })
                .then(function (list) { items = list || []; renderBadge(); renderList(); })
                .catch(function () { });
        }
        function toast(data) {
            try {
                var wrap = document.getElementById('noti-toasts');
                var el = document.createElement('div');
                el.className = 'toast';
                el.setAttribute('role', 'alert');
                var head = document.createElement('div');
                head.className = 'toast-header';
                var t = document.createElement('strong');
                t.className = 'me-auto';
                t.textContent = '🔔 Đơn hàng mới';
                var x = document.createElement('button');
                x.type = 'button'; x.className = 'btn-close'; x.setAttribute('data-bs-dismiss', 'toast');
                head.appendChild(t); head.appendChild(x);
                var body = document.createElement('div');
                body.className = 'toast-body';
                body.textContent = (data.customer || 'Khách') + ' đã đặt ' + data.orderCode + ' · ' + money(data.total);
                el.appendChild(head); el.appendChild(body);
                wrap.appendChild(el);
                el.addEventListener('hidden.bs.toast', function () { el.remove(); });
                new bootstrap.Toast(el, { delay: 6000 }).show();
            } catch (e) { }
        }

        var bell = document.getElementById('noti-bell');
        bell.addEventListener('show.bs.dropdown', function () {
            load().then(function () {
                var max = items.reduce(function (m, o) { return Math.max(m, o.id); }, 0);
                setTimeout(function () { setSeen(max); renderBadge(); }, 1500);
            });
        });
        load();

        var socket = new SockJS(ctx + '/ws');
        var stomp = Stomp.over(socket);
        stomp.debug = null;
        stomp.connect({}, function() {
            stomp.subscribe('/topic/orders', function(message) {
                var data = JSON.parse(message.body);
                // SỬA (Bước 3b): gọi hàm phát chuông an toàn thay vì try/catch
                playNotiSound();
                toast(data);
                load();
                var p = window.location.pathname;
                if (p.indexOf('/admin/orders') !== -1 || /\/admin(\/dashboard)?\/?$/.test(p)) {
                    setTimeout(function () { window.location.reload(); }, 2500);
                }
            });
        });
    })();
</script>
	<sitemesh:write property="bodyScript" />
	<script
		src="${pageContext.request.contextPath}/static/js/site-loader.js"
		defer></script>
</body>
</html>