<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Chi tiết đơn hàng</title>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<h3>Đơn hàng ${fn:escapeXml(order.orderCode)}</h3>

<%-- Thanh tiến trình: tự cập nhật khi cửa hàng đổi trạng thái (WebSocket bên dưới) --%>
<c:choose>
    <c:when test="${order.progressStep > 0}">
        <ol class="order-progress" aria-label="Tiến trình đơn hàng">
            <li class="${order.progressStep >= 1 ? 'done' : ''} ${order.progressStep == 1 ? 'now' : ''}"><i class="fas fa-receipt"></i><span>Đã tiếp nhận</span></li>
            <li class="${order.progressStep >= 2 ? 'done' : ''} ${order.progressStep == 2 ? 'now' : ''}"><i class="fas fa-blender"></i><span>Đang pha chế</span></li>
            <li class="${order.progressStep >= 3 ? 'done' : ''} ${order.progressStep == 3 ? 'now' : ''}"><i class="${order.pickup ? 'fas fa-store' : 'fas fa-motorcycle'}"></i><span>${order.pickup ? 'Sẵn sàng nhận' : 'Đang giao'}</span></li>
            <li class="${order.progressStep >= 4 ? 'done' : ''} ${order.progressStep == 4 ? 'now' : ''}"><i class="fas fa-circle-check"></i><span>Hoàn thành</span></li>
        </ol>
    </c:when>
    <c:otherwise>
        <div class="alert alert-secondary">Đơn này: <b>${fn:escapeXml(order.statusLabel)}</b>.
            <c:if test="${not empty order.failedReason}"> Lý do: ${fn:escapeXml(order.failedReason)}</c:if></div>
    </c:otherwise>
</c:choose>

<div class="d-flex flex-wrap gap-2 mb-3">
    <c:if test="${order.cancellable}">
        <form action="${ctx}/order/cancel/${fn:escapeXml(order.id)}" method="post" onsubmit="return confirm('Hủy đơn hàng này? Mã giảm giá và điểm đã dùng sẽ được hoàn lại.')">
            <button class="btn btn-outline-danger"><i class="fas fa-ban"></i> Hủy đơn</button>
        </form>
    </c:if>
    <c:if test="${order.awaitingOnlinePayment}">
        <c:choose>
            <c:when test="${order.paymentMethod == 'MOMO'}">
                <a href="${ctx}/payment/momo/${fn:escapeXml(order.id)}" class="btn btn-warning" data-no-lock="true"><i class="fas fa-wallet"></i> Thanh toán MoMo</a>
            </c:when>
            <c:otherwise>
                <a href="${ctx}/payment/vnpay/${fn:escapeXml(order.id)}" class="btn btn-warning" data-no-lock="true"><i class="fas fa-credit-card"></i> Thanh toán VNPay</a>
            </c:otherwise>
        </c:choose>
    </c:if>
    <form action="${ctx}/order/reorder/${fn:escapeXml(order.id)}" method="post">
        <button class="btn btn-outline-dark"><i class="fas fa-rotate-right"></i> Đặt lại đơn này</button>
    </form>
</div>
<p>Trạng thái: <span class="badge status-${fn:escapeXml(fn:toLowerCase(order.status))}">${fn:escapeXml(order.statusLabel)}</span></p>
<p>Nhận hàng: <b>${fn:escapeXml(order.pickup ? 'Đến lấy tại cửa hàng' : 'Giao tận nơi')}</b>
   <c:if test="${order.branch != null}"> · ${fn:escapeXml(order.branch.name)}</c:if></p>
<p>Địa chỉ: ${fn:escapeXml(order.shippingAddress)}</p>
<c:if test="${not empty order.phone}"><p>Số điện thoại: ${fn:escapeXml(order.phone)}</p></c:if>
<c:if test="${not empty order.deliveryTime}"><p>Thời gian nhận: ${fn:escapeXml(order.deliveryTime == 'ASAP' ? 'Càng sớm càng tốt' : order.deliveryTime)}</p></c:if>
<p>Thanh toán: ${fn:escapeXml(order.paymentMethod)}
    <c:if test="${order.paymentMethod == 'VNPAY' or order.paymentMethod == 'MOMO'}"> · <b>${fn:escapeXml(order.paymentStatus == 'PAID' ? 'Đã thanh toán' : 'Chưa thanh toán')}</b></c:if></p>
<c:if test="${not empty order.vatInfo}"><p>Hoá đơn VAT: ${fn:escapeXml(order.vatInfo)}</p></c:if>
<div class="checkout-box mb-3" style="max-width:420px">
    <div class="sum-row"><span>Tiền hàng</span><span><fmt:formatNumber value="${order.subtotalAmount}" pattern="#,##0"/> ₫</span></div>
    <div class="sum-row"><span>Phí vận chuyển</span><span><fmt:formatNumber value="${order.shippingFee != null ? order.shippingFee : 0}" pattern="#,##0"/> ₫</span></div>
    <div class="sum-row"><span>Giảm giá<c:if test="${not empty order.voucherCode}"> (${fn:escapeXml(order.voucherCode)})</c:if></span><span>− <fmt:formatNumber value="${order.discountAmount != null ? order.discountAmount : 0}" pattern="#,##0"/> ₫</span></div>
    <c:if test="${order.pointsUsed != null && order.pointsUsed > 0}">
        <div class="sum-row"><span>Dùng ${fn:escapeXml(order.pointsUsed)} điểm</span><span>− <fmt:formatNumber value="${order.pointsDiscount}" pattern="#,##0"/> ₫</span></div>
    </c:if>
    <div class="sum-row border-0"><b>Tổng thanh toán</b><span class="sum-total"><fmt:formatNumber value="${order.totalAmount}" pattern="#,##0"/> ₫</span></div>
    <c:if test="${order.pointsEarned != null && order.pointsEarned > 0}"><div class="small text-success mt-1">+${fn:escapeXml(order.pointsEarned)} điểm hội viên</div></c:if>
</div>

<table class="table">
    <thead><tr><th>Sản phẩm</th><th>Size</th><th>Đường</th><th>Đá</th><th>SL</th><th>Đơn giá</th><th>Thành tiền</th></tr></thead>
    <tbody>
        <c:forEach items="${order.details}" var="d">
            <tr>
                <td>${fn:escapeXml(d.product.name)}</td>
                <td>${fn:escapeXml(d.size != null ? d.size : '—')}</td>
                <td>${fn:escapeXml(d.sugarLevel != null ? d.sugarLevel : '—')}</td>
                <td>${fn:escapeXml(d.iceLevel != null ? d.iceLevel : '—')}</td>
                <td>${fn:escapeXml(d.quantity)}</td>
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
                    // Tải lại để thanh tiến trình và các nút phản ánh trạng thái mới
                    location.reload();
                }
            });
        });
    }
</script>