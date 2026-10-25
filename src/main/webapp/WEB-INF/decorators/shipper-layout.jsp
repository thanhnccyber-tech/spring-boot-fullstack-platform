<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title><sitemesh:write property="title" /> - UTeTra Giao hàng</title>
<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
<style>
body { background: #f6f3ec; }
.ship-nav { background: #2b2118; }
.ship-nav .navbar-brand { color: #ffc107; font-weight: 700; }
.ship-nav .nav-link { color: #f1e9dc; }
.ship-nav .nav-link:hover { color: #ffc107; }
.stat-card { border: 0; border-radius: 14px; }
.order-card { border: 0; border-radius: 14px; }
</style>
<sitemesh:write property="head" />
</head>
<body>

	<nav class="navbar navbar-expand-md ship-nav sticky-top">
		<div class="container">
			<a class="navbar-brand" href="${ctx}/shipper/dashboard"> <i
				class="fas fa-motorcycle"></i> UTeTra Giao hàng
			</a>
			<button class="navbar-toggler border-0" type="button"
				data-bs-toggle="collapse" data-bs-target="#shipNav"
				aria-label="Menu">
				<i class="fas fa-bars text-warning"></i>
			</button>
			<div class="collapse navbar-collapse" id="shipNav">
				<ul class="navbar-nav me-auto">
					<li class="nav-item"><a class="nav-link"
						href="${ctx}/shipper/dashboard"><i class="fas fa-box"></i> Đơn
							cần giao</a></li>
				</ul>
				<ul class="navbar-nav align-items-md-center">
					<li class="nav-item"><span class="nav-link"> <i
							class="fas fa-user-circle"></i> ${fn:escapeXml(sessionScope.fullName)}
					</span></li>
					<li class="nav-item"><a class="nav-link" href="${ctx}/logout"><i
							class="fas fa-sign-out-alt"></i> Đăng xuất</a></li>
				</ul>
			</div>
		</div>
	</nav>

	<main class="container py-3">
		<%-- Thông báo sau thao tác (flash) hiển thị chung ở layout, các trang shipper không cần tự hiển thị --%>
		<c:if test="${not empty success}">
			<div class="alert alert-success alert-dismissible fade show"
				role="alert">
				${fn:escapeXml(success)}
				<button type="button" class="btn-close" data-bs-dismiss="alert"></button>
			</div>
		</c:if>
		<c:if test="${not empty error}">
			<div class="alert alert-danger alert-dismissible fade show"
				role="alert">
				${fn:escapeXml(error)}
				<button type="button" class="btn-close" data-bs-dismiss="alert"></button>
			</div>
		</c:if>

		<sitemesh:write property="body" />
	</main>

	<script
		src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
	<sitemesh:write property="bodyScript" />
</body>
</html>
