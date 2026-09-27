<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><sitemesh:write property="title"/> - UTeTra Admin</title>

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin.css">

    <sitemesh:write property="head"/>
</head>
<body>

<div class="d-flex">
    <!-- Sidebar -->
    <aside class="bg-dark text-white p-3 vh-100" style="width: 250px; position: sticky; top: 0;">
        <h4 class="text-warning"><i class="fas fa-mug-hot"></i> UTeTra Admin</h4>
        <hr/>
        <ul class="nav flex-column">

            <%-- Dashboard: chỉ ADMIN --%>
            <c:if test="${sessionScope.roles.contains('ROLE_ADMIN')}">
                <li class="nav-item">
                    <a class="nav-link text-white" href="${pageContext.request.contextPath}/admin/dashboard">
                        <i class="fas fa-tachometer-alt"></i> Dashboard
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link text-white" href="${pageContext.request.contextPath}/admin/products">
                        <i class="fas fa-coffee"></i> Sản phẩm
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link text-white" href="${pageContext.request.contextPath}/admin/categories">
                        <i class="fas fa-list"></i> Danh mục
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link text-white" href="${pageContext.request.contextPath}/admin/toppings">
                        <i class="fas fa-ice-cream"></i> Topping
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link text-white" href="${pageContext.request.contextPath}/admin/branches">
                        <i class="fas fa-store"></i> Chi nhánh
                    </a>
                </li>
            </c:if>

            <%-- Đơn hàng: ADMIN + MANAGER + STAFF --%>
            <c:if test="${sessionScope.roles.contains('ROLE_ADMIN')
                      || sessionScope.roles.contains('ROLE_MANAGER')
                      || sessionScope.roles.contains('ROLE_STAFF')}">
                <li class="nav-item">
                    <a class="nav-link text-white" href="${pageContext.request.contextPath}/admin/orders">
                        <i class="fas fa-receipt"></i> Đơn hàng
                    </a>
                </li>
            </c:if>

            <%-- Người dùng: ADMIN + MANAGER --%>
            <c:if test="${sessionScope.roles.contains('ROLE_ADMIN')
                      || sessionScope.roles.contains('ROLE_MANAGER')}">
                <li class="nav-item">
                    <a class="nav-link text-white" href="${pageContext.request.contextPath}/admin/users">
                        <i class="fas fa-users"></i> Người dùng
                    </a>
                </li>
            </c:if>

            <li class="nav-item">
                <a class="nav-link text-white" href="${pageContext.request.contextPath}/logout">
                    <i class="fas fa-sign-out-alt"></i> Đăng xuất
                </a>
            </li>
        </ul>
    </aside>

    <!-- Content Area -->
    <div class="flex-grow-1">
        <!-- Topbar -->
        <header class="bg-white shadow-sm p-3 d-flex justify-content-between align-items-center sticky-top">
            <h5 class="mb-0"><sitemesh:write property="title"/></h5>
            <div class="d-flex align-items-center">
                <span id="noti-bell" class="position-relative me-3">
                    <i class="fas fa-bell fa-lg"></i>
                    <span id="noti-count" class="badge bg-danger position-absolute top-0 start-100 translate-middle d-none">0</span>
                </span>
                <span class="text-muted small">
                    <c:if test="${not empty sessionScope.fullName}">
                        Xin chào, <b>${sessionScope.fullName}</b>
                    </c:if>
                </span>
            </div>
        </header>

        <!-- Main Content -->
        <div class="p-4">
            <sitemesh:write property="body"/>
        </div>
    </div>
</div>

<audio id="noti-sound" src="${pageContext.request.contextPath}/static/sound/noti.mp3" preload="auto"></audio>

<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    (function() {
        // Lắng nghe đơn hàng mới cho admin/manager/staff
        const socket = new SockJS('${pageContext.request.contextPath}/ws');
        const stomp = Stomp.over(socket);
        stomp.debug = null;
        stomp.connect({}, function () {
            stomp.subscribe('/topic/orders', function (message) {
                const data = JSON.parse(message.body);
                const count = document.getElementById('noti-count');
                count.classList.remove('d-none');
                count.innerText = (parseInt(count.innerText) || 0) + 1;

                const audio = document.getElementById('noti-sound');
                if (audio) audio.play().catch(()=>{});

                alert('🔔 ĐƠN HÀNG MỚI: ' + data.orderCode +
                      '\nKhách: ' + data.customer +
                      '\nTổng: ' + data.total + '₫');

                if (window.location.pathname.includes('/admin/orders')) {
                    window.location.reload();
                }
            });
        });
    })();
</script>

<sitemesh:write property="bodyScript"/>
</body>
</html>