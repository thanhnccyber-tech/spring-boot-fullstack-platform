<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><sitemesh:write property="title"/> - UTeTra</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
    <sitemesh:write property="head"/>
</head>
<body>

<nav class="navbar navbar-expand-lg navbar-dark bg-dark sticky-top">
    <div class="container">
        <a class="navbar-brand fw-bold text-warning" href="${pageContext.request.contextPath}/">
            <i class="fas fa-mug-hot"></i> UTeTra
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navMenu">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navMenu">
            <ul class="navbar-nav me-auto">
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/">
                    <i class="fas fa-home"></i> Trang chủ</a></li>
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/products">
                    <i class="fas fa-coffee"></i> Sản phẩm</a></li>
            </ul>
            <ul class="navbar-nav ms-auto">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/cart">
                        <i class="fas fa-shopping-cart"></i> Giỏ hàng
                    </a>
                </li>
                <c:choose>
                    <c:when test="${not empty sessionScope.userId}">
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/notifications">
                                <i class="fas fa-bell"></i>
                            </a>
                        </li>
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">
                                <i class="fas fa-user-circle"></i> ${fn:escapeXml(sessionScope.fullName)}
                            </a>
                            <ul class="dropdown-menu dropdown-menu-end">
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/profile">
                                    <i class="fas fa-user"></i> Thông tin cá nhân</a></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/order/history">
                                    <i class="fas fa-receipt"></i> Đơn hàng của tôi</a></li>
                                <c:if test="${sessionScope.roles.contains('ROLE_ADMIN')
                                            || sessionScope.roles.contains('ROLE_MANAGER')
                                            || sessionScope.roles.contains('ROLE_STAFF')}">
                                    <li><hr class="dropdown-divider"></li>
                                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/dashboard">
                                        <i class="fas fa-tachometer-alt"></i> Trang quản trị</a></li>
                                </c:if>
                                <c:if test="${sessionScope.roles.contains('ROLE_SHIPPER')}">
                                    <li><hr class="dropdown-divider"></li>
                                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/shipper/dashboard">
                                        <i class="fas fa-truck"></i> Giao hàng</a></li>
                                </c:if>
                                <li><hr class="dropdown-divider"></li>
                                <li><a class="dropdown-item text-danger" href="${pageContext.request.contextPath}/logout">
                                    <i class="fas fa-sign-out-alt"></i> Đăng xuất</a></li>
                            </ul>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/login">
                            <i class="fas fa-sign-in-alt"></i> Đăng nhập</a></li>
                        <li class="nav-item">
                            <a class="btn btn-warning btn-sm ms-2 mt-1"
                               href="${pageContext.request.contextPath}/register">Đăng ký</a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </div>
</nav>

<main class="container my-4">
    <sitemesh:write property="body"/>
</main>

<footer class="bg-dark text-white text-center py-3 mt-5">
    <p class="mb-0">&copy; 2025 UTeTra - Chuỗi trà sữa UTe. Hotline: 1900 1234</p>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
<script src="${pageContext.request.contextPath}/static/js/main.js"></script>

<c:if test="${not empty sessionScope.userId}">
<script>
(function() {
    const userId = ${sessionScope.userId};
    const socket = new SockJS('${pageContext.request.contextPath}/ws');
    const stomp  = Stomp.over(socket);
    stomp.debug = null;
    stomp.connect({}, function () {
        stomp.subscribe('/topic/order-status/' + userId, function (msg) {
            const data = JSON.parse(msg.body);
            const banner = document.createElement('div');
            banner.className = 'alert alert-info alert-dismissible fade show position-fixed top-0 end-0 m-3';
            banner.style.zIndex = '9999';
            banner.innerHTML = '🔔 Đơn <b>' + data.orderCode +
                '</b> cập nhật: <b>' + data.status + '</b>' +
                '<button type="button" class="btn-close" data-bs-dismiss="alert"></button>';
            document.body.appendChild(banner);
            setTimeout(() => banner.remove(), 8000);
        });
    });
})();
</script>
</c:if>

<sitemesh:write property="bodyScript"/>
</body>
</html>