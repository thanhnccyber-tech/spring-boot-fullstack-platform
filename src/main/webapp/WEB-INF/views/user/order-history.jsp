<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<title>Lịch sử đơn hàng</title>

<h3>📋 Lịch sử đơn hàng</h3>

<table class="table table-hover">
    <thead class="table-dark">
        <tr>
            <th>Mã</th><th>Chi nhánh</th><th>Ngày</th>
            <th>Tổng tiền</th><th>Trạng thái</th><th></th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${orders}" var="o">
            <tr>
                <td>${o.orderCode}</td>
                <td>${o.branch.name}</td>
                <td>${o.createdAtFormatted}</td>
                <td class="text-danger">
                    <fmt:formatNumber value="${o.totalAmount}" pattern="#,##0"/> ₫
                </td>
                <td><span class="badge bg-info">${o.status}</span></td>
                <td>
                    <a href="${pageContext.request.contextPath}/order/detail/${o.id}"
                       class="btn btn-sm btn-outline-primary">Xem</a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

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