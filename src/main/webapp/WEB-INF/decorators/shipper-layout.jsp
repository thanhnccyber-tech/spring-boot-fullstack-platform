<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="uri" value="${pageContext.request.requestURI}" />
<c:set var="cartCount" value="0" />
<c:forEach items="${sessionScope.CART}" var="ci"><c:set var="cartCount" value="${cartCount + ci.quantity}" /></c:forEach>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="theme-color" content="#ffffff">
<title><sitemesh:write property="title" /> - UTETra Shipper</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Playfair+Display:ital,wght@0,400;0,500;0,600;0,700;1,400;1,500;1,600&display=swap&subset=vietnamese,latin,latin-ext" rel="stylesheet">
<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
<link rel="stylesheet" href="${ctx}/static/css/shipper.css">
<sitemesh:write property="head" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/typography-loader.css">
</head>
<body class="sh-body">
<div id="uteSiteLoader" class="ute-site-loader" role="status" aria-live="polite" aria-label="Đang tải trang UTETra">
  <div class="ute-site-loader__inner">
    <img class="ute-site-loader__logo" src="${ctx}/static/images/utetra-logo.png" alt="UTETra" width="340" height="207" fetchpriority="high">
    <div class="ute-site-loader__percent"><span id="uteSiteLoaderPercent">0</span><span aria-hidden="true">%</span></div>
    <div class="ute-site-loader__track" role="progressbar" aria-label="Tiến trình tải trang" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0" id="uteSiteLoaderProgress"><span id="uteSiteLoaderBar"></span></div>
    <p class="ute-site-loader__caption">ĐANG CHUẨN BỊ TRÀ NGON</p>
  </div>
</div>


	<!-- ===== Top Bar ===== -->
	<header class="sh-topbar">
		<div class="sh-topbar-inner">
			<a href="${ctx}/shipper/dashboard" class="sh-brand" aria-label="UTETra Shipper - Đơn hàng">
				<img class="sh-brand-logo" src="${ctx}/static/images/utetra-logo.png" alt="UTETra" width="76" height="46">
				<span class="sh-brand-tag"><i class="fas fa-motorcycle"></i> Shipper</span>
			</a>

			<nav class="sh-topnav">
				<a href="${ctx}/shipper/dashboard"
					class="${fn:contains(uri, '/shipper/dashboard') or fn:contains(uri, '/shipper/orders') ? 'active' : ''}">
					<i class="fas fa-box-open"></i> Đơn hàng
				</a> <a href="${ctx}/shipper/history"
					class="${fn:contains(uri, '/shipper/history') ? 'active' : ''}">
					<i class="fas fa-clock-rotate-left"></i> Lịch sử
				</a> <a href="${ctx}/shipper/stats"
					class="${fn:contains(uri, '/shipper/stats') ? 'active' : ''}">
					<i class="fas fa-chart-column"></i> Thống kê
				</a>
			</nav>

			<div class="sh-topbar-right">
				<%-- Shipper cũng là khách hàng: chuyển nhanh giữa hai chế độ --%>
				<div class="sh-mode" role="group" aria-label="Chuyển chế độ">
					<a href="${ctx}/shipper/dashboard" class="on" aria-current="page"><i class="fas fa-motorcycle"></i> <span>Giao hàng</span></a>
					<a href="${ctx}/" title="Mua sắm như một khách hàng"><i class="fas fa-mug-hot"></i> <span>Mua sắm</span></a>
				</div>

				<a class="sh-icon-btn d-none d-sm-inline-flex" href="${ctx}/cart" title="Giỏ hàng" aria-label="Giỏ hàng">
					<i class="fas fa-shopping-cart"></i>
					<c:if test="${cartCount > 0}"><span class="sh-icon-badge">${fn:escapeXml(cartCount)}</span></c:if>
				</a>

				<div class="dropdown">
					<button class="sh-user-btn" type="button" data-bs-toggle="dropdown"
						aria-expanded="false" aria-label="Menu tài khoản">
						<span class="sh-user-name">${fn:escapeXml(sessionScope.fullName)}</span>
						<span class="sh-user-avatar">${fn:escapeXml(fn:substring(sessionScope.fullName, 0, 1))}</span>
						<i class="fas fa-chevron-down sh-user-caret"></i>
					</button>
					<ul class="dropdown-menu dropdown-menu-end sh-user-menu">
						<li class="sh-user-menu-head">
							<div class="sh-user-menu-avatar">${fn:escapeXml(fn:substring(sessionScope.fullName, 0, 1))}</div>
							<div class="sh-user-menu-info">
								<div class="sh-user-menu-name">${fn:escapeXml(sessionScope.fullName)}</div>
								<div class="sh-user-menu-role">
									<i class="fas fa-motorcycle"></i> Shipper · <i class="fas fa-id-card"></i> Khách hàng
								</div>
							</div>
						</li>
						<li><hr class="dropdown-divider"></li>
						<li><h6 class="dropdown-header">Giao hàng</h6></li>
						<li><a class="dropdown-item" href="${ctx}/shipper/profile"> <i
								class="fas fa-user-circle"></i> Hồ sơ shipper
						</a></li>
						<li><a class="dropdown-item" href="${ctx}/shipper/history">
								<i class="fas fa-clock-rotate-left"></i> Lịch sử giao hàng
						</a></li>
						<li><hr class="dropdown-divider"></li>
						<li><h6 class="dropdown-header">Mua sắm</h6></li>
						<li><a class="dropdown-item sh-menu-switch" href="${ctx}/products">
								<i class="fas fa-mug-hot"></i> Đặt đồ uống
						</a></li>
						<li><a class="dropdown-item" href="${ctx}/order/history">
								<i class="fas fa-receipt"></i> Đơn hàng của tôi
						</a></li>
						<li><a class="dropdown-item" href="${ctx}/account/vouchers">
								<i class="fas fa-ticket"></i> Ưu đãi của tôi
						</a></li>
						<li><a class="dropdown-item" href="${ctx}/profile">
								<i class="fas fa-id-badge"></i> Tài khoản khách hàng
						</a></li>
						<li><a class="dropdown-item" href="${ctx}/notifications">
								<i class="fas fa-bell"></i> Thông báo
						</a></li>
						<li><hr class="dropdown-divider"></li>
						<li><a class="dropdown-item text-danger" href="${ctx}/logout">
								<i class="fas fa-right-from-bracket"></i> Đăng xuất
						</a></li>
					</ul>
				</div>
			</div>
		</div>
	</header>

	<!-- ===== Main ===== -->
	<main class="sh-main">
		<c:if test="${not empty success}">
			<div class="sh-alert success">
				<i class="fas fa-circle-check"></i> <span>${fn:escapeXml(success)}</span>
			</div>
		</c:if>
		<c:if test="${not empty error}">
			<div class="sh-alert error">
				<i class="fas fa-circle-exclamation"></i> <span>${fn:escapeXml(error)}</span>
			</div>
		</c:if>
		<sitemesh:write property="body" />
	</main>

    <footer class="sh-footer">
        <span><i class="fas fa-mug-hot"></i> UTETra</span>
        <span>Đồng hành cùng bạn trên mỗi chuyến giao hàng</span>
    </footer>

	<!-- ===== Bottom Nav (mobile) ===== -->
	<nav class="sh-bottom-nav">
		<a href="${ctx}/shipper/dashboard"
			class="sh-nav-item ${fn:contains(uri, '/shipper/dashboard') or fn:contains(uri, '/shipper/orders') ? 'active' : ''}">
			<i class="fas fa-box-open"></i> <span>Đơn hàng</span>
		</a> <a href="${ctx}/shipper/history"
			class="sh-nav-item ${fn:contains(uri, '/shipper/history') ? 'active' : ''}">
			<i class="fas fa-clock-rotate-left"></i> <span>Lịch sử</span>
		</a> <a href="${ctx}/shipper/stats"
			class="sh-nav-item ${fn:contains(uri, '/shipper/stats') ? 'active' : ''}">
			<i class="fas fa-chart-column"></i> <span>Thống kê</span>
		</a> <a href="${ctx}/" class="sh-nav-item">
			<i class="fas fa-mug-hot"></i> <span>Mua sắm</span>
		</a> <a href="${ctx}/shipper/profile"
			class="sh-nav-item ${fn:contains(uri, '/shipper/profile') ? 'active' : ''}">
			<i class="fas fa-user-circle"></i> <span>Tài khoản</span>
		</a>
	</nav>

	<script
		src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
	<script>
		// Tự ẩn flash message sau 5s
		document
				.querySelectorAll('.sh-alert')
				.forEach(
						function(el) {
							setTimeout(
									function() {
										el.style.transition = 'opacity .35s, transform .35s, margin .35s, max-height .35s';
										el.style.opacity = '0';
										el.style.transform = 'translateY(-6px)';
										el.style.maxHeight = '0';
										el.style.marginBottom = '0';
										el.style.paddingTop = '0';
										el.style.paddingBottom = '0';
										setTimeout(function() {
											el.remove();
										}, 400);
									}, 5000);
						});

		// Chống double-submit + loading state cho mọi form
		document.addEventListener('submit', function(e) {
			var f = e.target;
			if (!f || f.tagName !== 'FORM' || f.dataset.noLock !== undefined)
				return;
			setTimeout(function() {
				f.querySelectorAll('button[type="submit"], button:not([type])')
						.forEach(function(b) {
							if (b.disabled)
								return;
							b.disabled = true;
							b.classList.add('is-loading');
						});
			}, 0);
		});
		window.addEventListener('pageshow', function(ev) {
			if (!ev.persisted)
				return;
			document.querySelectorAll('.sh-btn.is-loading').forEach(
					function(b) {
						b.disabled = false;
						b.classList.remove('is-loading');
					});
		});
	</script>
	<sitemesh:write property="bodyScript" />
<script src="${ctx}/static/js/site-loader.js" defer></script>
</body>
</html>