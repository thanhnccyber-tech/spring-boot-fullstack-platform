<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<title>Đặt hàng</title>

<h3>📦 Thông tin đặt hàng</h3>
<form action="${pageContext.request.contextPath}/order/place" method="post">
    <div class="mb-3">
        <label class="form-label">Chọn chi nhánh</label>
        <select name="branchId" class="form-select" required>
            <c:forEach items="${branches}" var="b">
                <option value="${b.id}">${b.name} - ${b.address}</option>
            </c:forEach>
        </select>
    </div>
    <div class="mb-3">
        <label class="form-label">Địa chỉ nhận hàng</label>
        <textarea name="shippingAddress" rows="2" class="form-control" required></textarea>
    </div>
    <div class="mb-3">
        <label class="form-label">Phương thức thanh toán</label>
        <select name="paymentMethod" class="form-select">
            <option value="CASH">Tiền mặt tại quầy</option>
            <option value="BANK">Chuyển khoản</option>
            <option value="COD">COD - Thanh toán khi nhận hàng</option>
        </select>
    </div>
    <div class="mb-3">
        <label class="form-label">Ghi chú</label>
        <textarea name="note" rows="2" class="form-control"></textarea>
    </div>
    <button class="btn btn-warning btn-lg">Xác nhận đặt hàng</button>
</form>