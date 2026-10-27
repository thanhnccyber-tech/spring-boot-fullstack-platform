<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><sitemesh:write property="title"/> - UTETra</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&family=Playfair+Display:ital,wght@0,400;0,500;0,600;0,700;1,400;1,500;1,600&display=swap&subset=vietnamese,latin,latin-ext" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/brand-theme.css">
    <sitemesh:write property="head"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/typography-loader.css">
</head>
<body data-ctx="${pageContext.request.contextPath}" class="${fn:endsWith(pageContext.request.requestURI, '/home') or fn:endsWith(pageContext.request.requestURI, '/') ? 'ute-home-body' : ''}">
<div id="uteSiteLoader" class="ute-site-loader" role="status" aria-live="polite" aria-label="Đang tải trang UTETra">
  <div class="ute-site-loader__inner">
    <img class="ute-site-loader__logo" src="${pageContext.request.contextPath}/static/images/utetra-logo.png" alt="UTETra" width="340" height="207" fetchpriority="high">
    <div class="ute-site-loader__percent"><span id="uteSiteLoaderPercent">0</span><span aria-hidden="true">%</span></div>
    <div class="ute-site-loader__track" role="progressbar" aria-label="Tiến trình tải trang" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0" id="uteSiteLoaderProgress"><span id="uteSiteLoaderBar"></span></div>
    <p class="ute-site-loader__caption">ĐANG CHUẨN BỊ TRÀ NGON</p>
  </div>
</div>


<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="cartCount" value="0"/>
<c:forEach items="${sessionScope.CART}" var="ci"><c:set var="cartCount" value="${cartCount + ci.quantity}"/></c:forEach>
<c:set var="uri" value="${not empty requestScope['jakarta.servlet.forward.request_uri'] ? requestScope['jakarta.servlet.forward.request_uri'] : pageContext.request.requestURI}"/>
<c:set var="isHomePage" value="${fn:endsWith(uri, '/home') or fn:endsWith(uri, '/')}"/>

<!-- ============ Hàng 1: logo | tìm kiếm | nhận hàng | tài khoản ============ -->
<header class="site-header sticky-top">
    <div class="container py-2 d-flex align-items-center gap-2 gap-md-3">
        <a class="brand text-decoration-none" href="${ctx}/" aria-label="UTETra - Trang chủ">
            <img class="ute-brand-logo" src="${ctx}/static/images/utetra-logo.png" alt="UTETra" width="94" height="58" fetchpriority="high">
        </a>

        <form class="header-search" action="${ctx}/products" method="get" role="search">
            <i class="fas fa-search"></i>
            <input type="text" name="kw" maxlength="100" value="${fn:escapeXml(param.kw)}"
                   placeholder="Bạn muốn uống gì hôm nay..." aria-label="Tìm sản phẩm" aria-autocomplete="list"
                   aria-controls="searchSuggest" aria-expanded="false" autocomplete="off">
            <button type="button" class="header-search-clear" id="searchInputClear" aria-label="Xóa từ khóa tìm kiếm" hidden>×</button>
            <div class="search-suggest" id="searchSuggest" hidden role="listbox" aria-label="Gợi ý tìm kiếm"></div>
        </form>

        <span class="ms-auto"></span>
        <button type="button" class="btn receive-btn d-none d-md-inline-flex" data-bs-toggle="modal" data-bs-target="#receiveModal">
            <i class="fas fa-motorcycle"></i>
            <span class="text-start">
                <c:choose>
                    <c:when test="${not empty sessionScope.RECV_METHOD}">
                        <small class="d-block" style="color:#64748b;font-size:.72rem">${fn:escapeXml(sessionScope.RECV_METHOD == 'PICKUP' ? 'Đến lấy tại' : 'Giao hàng từ')}</small>
                        <b>${fn:escapeXml(sessionScope.RECV_BRANCH_NAME)}</b>
                    </c:when>
                    <c:otherwise><b>Chọn cách nhận hàng</b></c:otherwise>
                </c:choose>
            </span>
        </button>

        <c:choose>
            <c:when test="${not empty sessionScope.userId}">
                <button type="button" class="icon-btn notification-trigger" id="notificationBell" title="Thông báo" aria-label="Mở thông báo" aria-controls="notificationDrawer" aria-expanded="false">
                    <i class="fas fa-bell"></i>
                    <c:if test="${notificationDrawer.unread > 0}"><span class="ute-notification-badge" id="notificationUnreadBadge">${fn:escapeXml(notificationDrawer.unread > 99 ? '99+' : notificationDrawer.unread)}</span></c:if>
                </button>
                <div class="dropdown">
                    <a class="icon-btn" href="#" data-bs-toggle="dropdown" title="Tài khoản"><i class="fas fa-user-circle"></i></a>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li class="dropdown-header">${fn:escapeXml(sessionScope.fullName)}</li>
                        <li><a class="dropdown-item" href="${ctx}/profile"><i class="fas fa-id-badge"></i> Thông tin cá nhân</a></li>
                        <li><a class="dropdown-item" href="${ctx}/account/member"><i class="fas fa-id-card"></i> Khách hàng thành viên</a></li>
                        <li><a class="dropdown-item" href="${ctx}/account/vouchers"><i class="fas fa-ticket"></i> Ưu đãi của tôi</a></li>
                        <li><a class="dropdown-item" href="${ctx}/account/addresses"><i class="fas fa-location-dot"></i> Số địa chỉ</a></li>
                        <li><a class="dropdown-item" href="${ctx}/order/history"><i class="fas fa-cart-shopping"></i> Đơn hàng</a></li>
                        <li><a class="dropdown-item" href="${ctx}/notifications"><i class="fas fa-bell"></i> Thông báo</a></li>
                        <c:if test="${sessionScope.roles.contains('ROLE_ADMIN')
                                    || sessionScope.roles.contains('ROLE_MANAGER')
                                    || sessionScope.roles.contains('ROLE_STAFF')}">
                            <li><hr class="dropdown-divider"></li>
                            <li><a class="dropdown-item" href="${ctx}/admin/dashboard"><i class="fas fa-tachometer-alt"></i> Trang quản trị</a></li>
                        </c:if>
                        <c:if test="${sessionScope.roles.contains('ROLE_SHIPPER')}">
                            <li><hr class="dropdown-divider"></li>
                            <li><a class="dropdown-item fw-semibold" href="${ctx}/shipper/dashboard"><i class="fas fa-motorcycle"></i> Chế độ Shipper · Nhận đơn giao</a></li>
                        </c:if>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item text-danger" href="${ctx}/logout"><i class="fas fa-sign-out-alt"></i> Đăng xuất</a></li>
                    </ul>
                </div>
            </c:when>
            <c:otherwise>
                <a class="btn btn-outline-dark btn-sm" href="${ctx}/login">Đăng nhập</a>
                <a class="btn btn-warning btn-sm d-none d-sm-inline-block" href="${ctx}/register">Đăng ký</a>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- ============ Hàng 2: nav chính + mega-menu ============ -->
    <nav class="main-nav border-top">
        <div class="container">
            <button class="btn btn-sm d-md-none my-1" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav" aria-label="Mở menu">
                <i class="fas fa-bars"></i> Menu
            </button>
            <ul class="nav justify-content-center collapse d-md-flex" id="mainNav">
                <li class="nav-item"><a class="nav-link" href="${ctx}/">Trang chủ</a></li>
                <li class="nav-item mega">
                    <a class="nav-link" href="${ctx}/products">Menu</a>
                    <div class="mega-panel">
                        <div class="mega-col">
                            <h6>Thức uống &amp; bánh</h6>
                            <a href="${ctx}/products">Tất cả sản phẩm</a>
                            <c:forEach items="${menuCategories}" var="mc">
                                <a href="${ctx}/products?categoryId=${fn:escapeXml(mc.id)}">${fn:escapeXml(mc.name)}</a>
                            </c:forEach>
                        </div>
                        <div class="mega-col">
                            <h6>Gợi ý nhanh</h6>
                            <a href="${ctx}/products?sort=priceAsc">Giá tốt nhất</a>
                            <a href="${ctx}/products?sort=newest">Món mới ra mắt</a>
                        </div>
                    </div>
                </li>
                <li class="nav-item"><a class="nav-link" href="${ctx}/khuyen-mai">Khuyến mãi</a></li>
                <li class="nav-item"><a class="nav-link" href="${ctx}/hoi-vien">Hội viên</a></li>
                <li class="nav-item"><a class="nav-link" href="${ctx}/cua-hang">Cửa hàng</a></li>
                <li class="nav-item d-md-none"><a class="nav-link" href="#" data-bs-toggle="modal" data-bs-target="#receiveModal">Chọn cách nhận hàng</a></li>
            </ul>
        </div>
    </nav>
</header>

<c:if test="${not empty sessionScope.userId}">
<div class="ute-notification-backdrop" id="notificationBackdrop" hidden></div>
<aside class="ute-notification-drawer" id="notificationDrawer" aria-label="Thông báo UTETra" aria-hidden="true" hidden>
    <div class="ute-notification-head">
        <div>
            <h5><i class="fas fa-bell"></i> Thông báo</h5>
            <p>Tin mới nhất về ưu đãi và đơn hàng của bạn</p>
        </div>
        <button type="button" class="ute-notification-close" id="notificationClose" aria-label="Đóng thông báo"><i class="fas fa-xmark"></i></button>
    </div>
    <div class="ute-notification-tabs" role="tablist" aria-label="Nhóm thông báo">
        <button type="button" class="active" role="tab" aria-selected="true" data-notification-tab="news"><i class="fas fa-bullhorn"></i> TIN TỨC <span>${fn:length(notificationDrawer.news)}</span></button>
        <button type="button" role="tab" aria-selected="false" data-notification-tab="orders"><i class="fas fa-receipt"></i> ĐƠN HÀNG <span>${fn:length(notificationDrawer.orders)}</span></button>
    </div>
    <div class="ute-notification-list" data-notification-panel="news" role="tabpanel">
        <c:forEach items="${notificationDrawer.news}" var="n">
            <article class="ute-notification-item ${n.isRead ? '' : 'unread'}">
                <div class="ute-notification-icon ${n.type == 'VOUCHER' ? 'voucher' : 'news'}"><i class="fas ${n.type == 'VOUCHER' ? 'fa-ticket' : 'fa-bell'}"></i></div>
                <div class="ute-notification-copy">
                    <div class="ute-notification-item-title">${fn:escapeXml(n.title)}</div>
                    <p>${fn:escapeXml(n.content)}</p>
                    <time>${fn:escapeXml(n.createdAtFormatted)}</time>
                    <c:if test="${n.type == 'VOUCHER'}"><a href="${ctx}/account/vouchers" class="ute-notification-action">Xem ưu đãi <i class="fas fa-arrow-right"></i></a></c:if>
                </div>
            </article>
        </c:forEach>
        <c:if test="${empty notificationDrawer.news}"><div class="ute-notification-empty"><i class="fas fa-inbox"></i><b>Chưa có tin tức</b><span>Thông báo voucher và ưu đãi sẽ xuất hiện tại đây.</span></div></c:if>
    </div>
    <div class="ute-notification-list" data-notification-panel="orders" role="tabpanel" hidden>
        <c:forEach items="${notificationDrawer.orders}" var="n">
            <article class="ute-notification-item ${n.isRead ? '' : 'unread'}">
                <div class="ute-notification-icon order"><i class="fas fa-motorcycle"></i></div>
                <div class="ute-notification-copy">
                    <div class="ute-notification-item-title">${fn:escapeXml(n.title)}</div>
                    <p>${fn:escapeXml(n.content)}</p>
                    <time>${fn:escapeXml(n.createdAtFormatted)}</time>
                    <a href="${ctx}/order/history" class="ute-notification-action">Xem đơn hàng <i class="fas fa-arrow-right"></i></a>
                </div>
            </article>
        </c:forEach>
        <c:if test="${empty notificationDrawer.orders}"><div class="ute-notification-empty"><i class="fas fa-receipt"></i><b>Chưa có thông báo đơn hàng</b><span>Cập nhật về đơn hàng sẽ xuất hiện tại đây.</span></div></c:if>
    </div>
    <div class="ute-notification-footer"><a href="${ctx}/notifications">Xem tất cả thông báo <i class="fas fa-arrow-right"></i></a></div>
</aside>
</c:if>

<!-- ============ Modal chọn cách nhận hàng ============ -->
<div class="modal fade" id="receiveModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content border-0 shadow-lg" style="border-radius: 16px; overflow: hidden;">
      <form action="${ctx}/receive/set" method="post" id="receiveForm">
        <input type="hidden" name="returnUrl" id="receiveReturn" value="/products">
        <input type="hidden" name="lat" id="receiveLat" value="${fn:escapeXml(sessionScope.RECV_LAT)}">
        <input type="hidden" name="lng" id="receiveLng" value="${fn:escapeXml(sessionScope.RECV_LNG)}">
        <input type="hidden" name="method" id="receiveMethod" value="${fn:escapeXml(sessionScope.RECV_METHOD == 'PICKUP' ? 'PICKUP' : 'DELIVERY')}">
        <div class="modal-header border-0 pb-0">
          <ul class="nav nav-pills w-100 recv-tabs" role="tablist">
            <li class="nav-item flex-fill text-center"><button type="button" class="nav-link w-100 ${fn:escapeXml(sessionScope.RECV_METHOD == 'PICKUP' ? '' : 'active')}" data-recv="DELIVERY"><i class="fas fa-truck-fast"></i> GIAO HÀNG</button></li>
            <li class="nav-item flex-fill text-center"><button type="button" class="nav-link w-100 ${fn:escapeXml(sessionScope.RECV_METHOD == 'PICKUP' ? 'active' : '')}" data-recv="PICKUP"><i class="fas fa-store"></i> ĐẾN LẤY</button></li>
          </ul>
          <button type="button" class="btn-close position-absolute top-0 end-0 m-2" data-bs-dismiss="modal" aria-label="Đóng"></button>
        </div>
        <div class="modal-body">
          <div id="recvAddressBox" class="mb-3">
            <label class="form-label small text-muted mb-1">Địa chỉ nhận hàng</label>
            <div class="input-group">
              <span class="input-group-text bg-white"><i class="fas fa-location-dot"></i></span>
              <input type="text" class="form-control" name="address" id="recvAddress" maxlength="500"
                     placeholder="Vui lòng nhập địa chỉ" value="${fn:escapeXml(sessionScope.RECV_ADDR)}">
            </div>
            <button type="button" class="btn btn-link px-0 mt-1" id="recvLocate" style="text-decoration:none;font-size:.85rem;color:var(--ute-blue-600)"><i class="fas fa-crosshairs"></i> Vị trí hiện tại của tôi</button>
            <div class="small text-muted" id="recvLocateMsg"></div>
          </div>
          <label class="form-label small text-muted mb-1" id="recvBranchLabel">Cửa hàng phục vụ</label>
          <div class="list-group branch-list">
            <c:forEach items="${receiveBranches}" var="rb" varStatus="st">
              <label class="list-group-item d-flex gap-2 align-items-start">
                <input class="form-check-input mt-1" type="radio" name="branchId" value="${fn:escapeXml(rb.id)}" required
                       <c:if test="${(not empty sessionScope.RECV_BRANCH and sessionScope.RECV_BRANCH == rb.id) or (empty sessionScope.RECV_BRANCH and st.first)}">checked</c:if>>
                <span><b>${fn:escapeXml(rb.name)}</b><br><small class="text-muted">${fn:escapeXml(rb.address)} · ${fn:escapeXml(rb.phone)}</small></span>
              </label>
            </c:forEach>
            <c:if test="${empty receiveBranches}"><div class="list-group-item text-muted">Hiện chưa có cửa hàng nào hoạt động.</div></c:if>
          </div>
        </div>
        <div class="modal-footer border-0 pt-0">
          <button class="btn btn-warning w-100 fw-bold">XÁC NHẬN</button>
        </div>
      </form>
    </div>
  </div>
</div>

<c:if test="${not empty welcomeNew}">
<div class="modal fade" id="welcomeModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered"><div class="modal-content text-center p-4 border-0 shadow-lg" style="border-radius:16px">
    <div style="font-size:3rem">🎉</div>
    <h4 class="mb-2">Chào mừng hội viên mới!</h4>
    <p class="mb-3">Chúc mừng <b>${fn:escapeXml(welcomeNew)}</b>, bạn đã trở thành hội viên UTETra.<br>
       Mỗi 10.000đ mua hàng = 1 điểm. Dùng mã <b>WELCOME10</b> để giảm 10% cho đơn đầu tiên.</p>
    <div class="d-flex gap-2 justify-content-center">
      <a class="btn btn-warning" href="${ctx}/products">Đặt mua ngay</a>
      <a class="btn btn-outline-dark" href="${ctx}/account/member">Xem thẻ hội viên</a>
    </div>
  </div></div>
</div>
<script>
  window.addEventListener('load', function () {
    if (window.bootstrap) new bootstrap.Modal(document.getElementById('welcomeModal')).show();
  });
</script>
</c:if>

<main class="${isHomePage ? 'ute-home-main' : 'container my-4'}">
    <c:set var="ownMsgPage" value="${fn:contains(uri, '/login') or fn:contains(uri, '/register') or fn:contains(uri, '/otp')
                        or fn:contains(uri, '/forgot-password') or fn:contains(uri, '/reset-password') or fn:contains(uri, '/profile')}"/>
    <c:if test="${not ownMsgPage}">
        <c:if test="${not empty success}">
            <div class="alert alert-success alert-dismissible fade show auto-hide" role="alert">${fn:escapeXml(success)}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>
        </c:if>
        <c:if test="${not empty error and not fn:contains(uri, '/product/')}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">${fn:escapeXml(error)}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>
        </c:if>
    </c:if>
    <sitemesh:write property="body"/>
</main>

<!-- Nút nổi -->
<div class="float-actions">
    <a class="float-btn float-cart" href="${ctx}/cart" title="Giỏ hàng" aria-label="Giỏ hàng">
        <i class="fas fa-shopping-cart"></i>
        <span class="float-badge" id="cartBadge">${fn:escapeXml(cartCount)}</span>
    </a>
    <a class="float-btn float-call" href="tel:0968523156" title="Gọi hotline 0968 523 156" aria-label="Gọi hotline"><i class="fas fa-phone"></i></a>
</div>

<!-- ============ Footer ============ -->
<footer class="ute-footer">
    <div class="container">
        <div class="row g-4">
            <div class="col-lg-4 col-md-6">
                <div class="brand-footer">
                    <img class="ute-footer-logo" src="${ctx}/static/images/utetra-logo.png" alt="UTETra" width="104" height="64" loading="lazy">
                </div>
                <p style="max-width:320px;font-size:.9rem;line-height:1.6">
                    Chuỗi trà sữa tươi mát, pha chế từ nguyên liệu chọn lọc. Giao nhanh trong 30 phút tại TP.HCM.
                </p>
                <div class="d-flex gap-3 mt-3">
                    <a href="#" aria-label="Facebook"><i class="fab fa-facebook fa-lg"></i></a>
                    <a href="#" aria-label="Instagram"><i class="fab fa-instagram fa-lg"></i></a>
                    <a href="#" aria-label="TikTok"><i class="fab fa-tiktok fa-lg"></i></a>
                    <a href="https://zalo.me" aria-label="Zalo"><i class="fas fa-comment-dots fa-lg"></i></a>
                </div>
            </div>
            <div class="col-lg-2 col-md-6">
                <h6>Khám phá</h6>
                <ul class="footer-list">
                    <li><a href="${ctx}/">Trang chủ</a></li>
                    <li><a href="${ctx}/products">Thực đơn</a></li>
                    <li><a href="${ctx}/khuyen-mai">Khuyến mãi</a></li>
                    <li><a href="${ctx}/hoi-vien">Hội viên</a></li>
                    <li><a href="${ctx}/cua-hang">Cửa hàng</a></li>
                </ul>
            </div>
            <div class="col-lg-3 col-md-6">
                <h6>Liên hệ</h6>
                <ul class="footer-list">
                    <li><i class="fas fa-location-dot"></i> Số 1 Võ Văn Ngân, Thủ Đức, TP.HCM</li>
                    <li><i class="fas fa-phone"></i> <a href="tel:0968523156">0968 523 156</a></li>
                    <li><i class="fas fa-envelope"></i> <a href="mailto:24162115@hcmute.edu.vn">24162115@hcmute.edu.vn</a></li>
                </ul>
            </div>
            <div class="col-lg-3 col-md-6">
                <h6>Giờ mở cửa</h6>
                <ul class="footer-list">
                    <li><i class="fas fa-clock"></i> Thứ 2 – Thứ 6: 08:00 – 22:00</li>
                    <li><i class="fas fa-clock"></i> Thứ 7 – CN: 08:00 – 23:00</li>
                    <li><i class="fas fa-headset"></i> Hotline hỗ trợ 24/7</li>
                </ul>
            </div>
        </div>
        <div class="footer-bottom text-center">
            © 2025 UTETra · Chuỗi trà sữa UTETra · Bảo lưu mọi quyền.
        </div>
    </div>
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
<script src="${pageContext.request.contextPath}/static/js/site-loader.js" defer></script>
</body>
</html>