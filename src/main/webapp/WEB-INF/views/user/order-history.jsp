<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Lịch sử đơn hàng</title>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="small mb-3"><a href="${ctx}/">Trang chủ</a> / <a href="${ctx}/profile">Tài khoản</a> / Đơn hàng</nav>
<div class="row g-4">
<div class="col-lg-4 col-xl-3">
    <jsp:include page="/WEB-INF/views/user/_account-menu.jsp"><jsp:param name="active" value="orders"/></jsp:include>
</div>
<div class="col-lg-8 col-xl-9"><div class="checkout-box">
<h4 class="mb-3" style="color:#8a6d00;font-weight:700">Đơn hàng của tôi</h4>

<c:if test="${empty orders}">
    <div class="empty-state">
        <i class="fas fa-receipt"></i>
        <h5>Bạn chưa có đơn hàng nào</h5>
        <p class="text-muted mb-3">Đặt ly trà sữa đầu tiên và theo dõi tiến trình ngay tại đây.</p>
        <a href="${ctx}/products" class="btn btn-warning">Xem menu</a>
    </div>
</c:if>

<c:if test="${not empty orders}">
<div class="table-responsive">
<table class="table table-hover align-middle">
    <thead class="table-dark">
        <tr>
            <th>Mã</th><th>Chi nhánh</th><th>Ngày</th>
            <th>Tổng tiền</th><th>Trạng thái</th><th></th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${orders}" var="o">
            <tr>
                <td>${fn:escapeXml(o.orderCode)}</td>
                <td>${fn:escapeXml(o.branch.name)}</td>
                <td>${fn:escapeXml(o.createdAtFormatted)}</td>
                <td class="text-danger">
                    <fmt:formatNumber value="${o.totalAmount}" pattern="#,##0"/> ₫
                </td>
                <td><span class="badge status-${fn:escapeXml(fn:toLowerCase(o.status))}">${fn:escapeXml(o.statusLabel)}</span></td>
                <td class="text-nowrap">
                    <a href="${ctx}/order/detail/${fn:escapeXml(o.id)}" class="btn btn-sm btn-outline-primary">Xem</a>
                    <form action="${ctx}/order/reorder/${fn:escapeXml(o.id)}" method="post" class="d-inline">
                        <button class="btn btn-sm btn-outline-warning" title="Thêm lại các món này vào giỏ"><i class="fas fa-rotate-right"></i> Đặt lại</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
</div>
</c:if>
</div></div>
</div>

<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
<script>
    const userId = ${sessionScope.userId != null ? sessionScope.userId : 0};

    if (userId) {
        const socket = new SockJS('${pageContext.request.contextPath}/ws');
        const stomp = Stomp.over(socket);
        stomp.debug = null;

        stomp.connect({}, function () {
            stomp.subscribe('/topic/order-status/' + userId, function (msg) {
                const data = JSON.parse(msg.body);
                console.log('Order update:', data);

                const banner = document.createElement('div');
                banner.className = 'alert alert-info alert-dismissible fade show position-fixed top-0 end-0 m-3';
                banner.style.zIndex = '9999';
                banner.innerHTML = '🔔 Đơn <b>' + data.orderCode +
                    '</b> cập nhật: <b>' + data.status + '</b>' +
                    '<button type="button" class="btn-close" data-bs-dismiss="alert"></button>';
                document.body.appendChild(banner);

                setTimeout(() => location.reload(), 2000);
            });
        });
    }
</script>