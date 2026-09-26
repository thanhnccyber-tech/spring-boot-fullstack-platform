<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<title>Chi tiết đơn hàng</title>

<h3>Đơn hàng ${order.orderCode}</h3>
<p>Trạng thái: <span class="badge bg-info">${order.status}</span></p>
<p>Địa chỉ: ${order.shippingAddress}</p>
<p>Thanh toán: ${order.paymentMethod}</p>
<p>Tổng: <span class="text-danger fw-bold"><fmt:formatNumber value="${order.totalAmount}" pattern="#,##0"/> ₫</span></p>

<table class="table">
    <thead><tr><th>Sản phẩm</th><th>Size</th><th>Đường</th><th>Đá</th><th>SL</th><th>Đơn giá</th><th>Thành tiền</th></tr></thead>
    <tbody>
        <c:forEach items="${order.details}" var="d">
            <tr>
                <td>${d.product.name}</td>
                <td>${d.size}</td>
                <td>${d.sugarLevel}%</td>
                <td>${d.iceLevel}%</td>
                <td>${d.quantity}</td>
                <td><fmt:formatNumber value="${d.unitPrice}" pattern="#,##0"/> ₫</td>
                <td><fmt:formatNumber value="${d.subtotal}" pattern="#,##0"/> ₫</td>
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
                if (data.orderId == ${order.id}) {
                    alert('Đơn hàng cập nhật: ' + data.status);
                    location.reload();
                }
            });
        });
    }
</script>