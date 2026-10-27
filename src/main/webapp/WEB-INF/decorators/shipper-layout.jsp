<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="uri" value="${pageContext.request.requestURI}" />
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="theme-color" content="#0f172a">
<title><sitemesh:write property="title" /> - UTETra Shipper</title>
<link
	href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
	rel="stylesheet">
<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
<link rel="stylesheet" href="${ctx}/static/css/shipper.css">
<sitemesh:write property="head" />
</head>
<body class="sh-body">

	<!-- ===== Top Bar ===== -->
	<header class="sh-topbar">
		<div class="sh-topbar-inner">
			<a href="${ctx}/shipper/dashboard" class="sh-brand">
				<div class="sh-brand-icon">
					<i class="fas fa-motorcycle"></i>
				</div>
				<div class="sh-brand-label">
					UTETra <small>SHIPPER</small>
				</div>
			</a>

			<nav class="sh-topnav">
				<a href="${ctx}/shipper/dashboard"
					class="${fn:contains(uri, '/shipper/dashboard') ? 'active' : ''}">
					<i class="fas fa-box-open"></i> Đơn hàng
				</a> <a href="${ctx}/shipper/history"
					class="${fn:contains(uri, '/shipper/history') ? 'active' : ''}">
					<i class="fas fa-clock-rotate-left"></i> Lịch sử
				</a> <a href="${ctx}/shipper/stats"
					class="${fn:contains(uri, '/shipper/stats') ? 'active' : ''}">
					<i class="fas fa-chart-column"></i> Thống kê
				</a>
			</nav>

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
								<i class="fas fa-motorcycle"></i> Shipper
							</div>
						</div>
					</li>
					<li><hr class="dropdown-divider"></li>
					<li><a class="dropdown-item" href="${ctx}/shipper/profile"> <i
							class="fas fa-user-circle"></i> Thông tin cá nhân
					</a></li>
					<li><a class="dropdown-item" href="${ctx}/shipper/history">
							<i class="fas fa-clock-rotate-left"></i> Lịch sử giao hàng
					</a></li>
					<li><hr class="dropdown-divider"></li>
					<li><a class="dropdown-item text-danger" href="${ctx}/logout">
							<i class="fas fa-right-from-bracket"></i> Đăng xuất
					</a></li>
				</ul>
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

	<!-- ===== Bottom Nav (mobile) ===== -->
	<nav class="sh-bottom-nav">
		<a href="${ctx}/shipper/dashboard"
			class="sh-nav-item ${fn:contains(uri, '/shipper/dashboard') ? 'active' : ''}">
			<i class="fas fa-box-open"></i> <span>Đơn hàng</span>
		</a> <a href="${ctx}/shipper/history"
			class="sh-nav-item ${fn:contains(uri, '/shipper/history') ? 'active' : ''}">
			<i class="fas fa-clock-rotate-left"></i> <span>Lịch sử</span>
		</a> <a href="${ctx}/shipper/stats"
			class="sh-nav-item ${fn:contains(uri, '/shipper/stats') ? 'active' : ''}">
			<i class="fas fa-chart-column"></i> <span>Thống kê</span>
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
</body>
</html>