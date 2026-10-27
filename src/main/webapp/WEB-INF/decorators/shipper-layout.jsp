<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title><sitemesh:write property="title" /> - UTETra Giao hàng</title>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
<style>
    body { background: #f8fafc; font-family: 'Inter', -apple-system, sans-serif; color: #334155; }
    .ship-nav { background: linear-gradient(135deg, #0f172a, #1e3a8a); }
    .ship-nav .navbar-brand { color: #fff; font-weight: 800; letter-spacing: -.02em; }
    .ship-nav .navbar-brand i { color: #60a5fa; }
    .ship-nav .nav-link { color: #cbd5e1; font-weight: 500; font-size: .9rem; }
    .ship-nav .nav-link:hover { color: #60a5fa; }
    .ship-nav .nav-link i { margin-right: 4px; }
    .stat-card { border: 1px solid #e2e8f0; border-radius: 14px; background: #fff; box-shadow: 0 2px 8px rgba(15,23,42,.04); }
    .order-card { border: 1px solid #e2e8f0; border-radius: 14px; background: #fff; box-shadow: 0 2px 8px rgba(15,23,42,.04); }
    .btn-warning { background: linear-gradient(135deg, #2563eb, #1d4ed8) !important; border: 0 !important; color: #fff !important; font-weight: 600; }
    .btn-warning:hover { background: linear-gradient(135deg, #1d4ed8, #1e40af) !important; }
</style>
<sitemesh:write property="head" />
</head>
<body>

<nav class="navbar navbar-expand-md ship-nav sticky-top">
    <div class="container">
        <a class="navbar-brand" href="${ctx}/shipper/dashboard"><i class="fas fa-motorcycle"></i> UTETra Giao hàng</a>
        <button class="navbar-toggler border-0" type="button" data-bs-toggle="collapse" data-bs-target="#shipNav" aria-label="Menu">
            <i class="fas fa-bars" style="color:#60a5fa"></i>
        </button>
        <div class="collapse navbar-collapse" id="shipNav">
            <ul class="navbar-nav me-auto">
                <li class="nav-item"><a class="nav-link" href="${ctx}/shipper/dashboard"><i class="fas fa-box"></i> Đơn cần giao</a></li>
                <li class="nav-item"><a class="nav-link" href="${ctx}/shipper/history"><i class="fas fa-clock-rotate-left"></i> Lịch sử</a></li>
                <li class="nav-item"><a class="nav-link" href="${ctx}/shipper/stats"><i class="fas fa-chart-column"></i> Thống kê</a></li>
            </ul>
            <ul class="navbar-nav align-items-md-center">
                <li class="nav-item"><span class="nav-link"><i class="fas fa-user-circle"></i> ${fn:escapeXml(sessionScope.fullName)}</span></li>
                <li class="nav-item"><a class="nav-link" href="${ctx}/logout"><i class="fas fa-sign-out-alt"></i> Đăng xuất</a></li>
            </ul>
        </div>
    </div>
</nav>

<main class="container py-3">
    <c:if test="${not empty success}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            ${fn:escapeXml(success)}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ${fn:escapeXml(error)}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <sitemesh:write property="body" />
</main>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<sitemesh:write property="bodyScript" />
</body>
</html>