<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<title>Đơn ${order.orderCode}</title>
<div class="card">
    <div class="card-body">
        <p>Khách: <b>${order.user.fullName}</b> (${order.user.email})</p>
        <p>Địa chỉ: ${order.shippingAddress}</p>
        <p>Thanh toán: ${order.paymentMethod}</p>
        <p>Tổng: <b class="text-danger"><fmt:formatNumber value="${order.totalAmount}" pattern="#,##0"/> ₫</b></p>

        <form action="${pageContext.request.contextPath}/admin/orders/update-status" method="post" class="row g-2">
            <input type="hidden" name="id" value="${order.id}">
            <div class="col-md-4">
                <select name="status" class="form-select">
                    <option value="PENDING" <c:if test="${order.status=='PENDING'}">selected</c:if>>Chờ xác nhận</option>
                    <option value="PREPARING" <c:if test="${order.status=='PREPARING'}">selected</c:if>>Đang pha chế</option>
                    <option value="DELIVERING" <c:if test="${order.status=='DELIVERING'}">selected</c:if>>Đang giao</option>
                    <option value="COMPLETED" <c:if test="${order.status=='COMPLETED'}">selected</c:if>>Đã hoàn thành</option>
                    <option value="CANCELLED" <c:if test="${order.status=='CANCELLED'}">selected</c:if>>Đã hủy</option>
                </select>
            </div>
            <div class="col-md-3"><button class="btn btn-warning w-100">Cập nhật</button></div>
        </form>
    </div>
</div>

<table class="table mt-3">
    <thead><tr><th>Sản phẩm</th><th>Size</th><th>SL</th><th>Đơn giá</th><th>Subtotal</th></tr></thead>
    <tbody>
        <c:forEach items="${order.details}" var="d">
            <tr>
                <td>${d.product.name}</td>
                <td>${d.size}</td>
                <td>${d.quantity}</td>
                <td><fmt:formatNumber value="${d.unitPrice}" pattern="#,##0"/> ₫</td>
                <td><fmt:formatNumber value="${d.subtotal}" pattern="#,##0"/> ₫</td>
            </tr>
        </c:forEach>
    </tbody>
</table>