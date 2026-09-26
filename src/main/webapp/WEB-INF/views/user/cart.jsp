<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<title>Giỏ hàng</title>

<h3>🛒 Giỏ hàng của bạn</h3>

<c:if test="${empty cart}">
    <div class="alert alert-info">Giỏ hàng đang trống. <a href="${pageContext.request.contextPath}/products">Mua sắm ngay</a></div>
</c:if>

<c:if test="${not empty cart}">
<table class="table align-middle">
    <thead>
        <tr>
            <th>#</th><th>Sản phẩm</th><th>Size</th><th>Đường</th><th>Đá</th>
            <th>Topping</th><th>SL</th><th>Đơn giá</th><th>Thành tiền</th><th></th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${cart}" var="item" varStatus="st">
            <tr>
                <td>${st.index + 1}</td>
                <td>${item.productName}</td>
                <td>${item.size}</td>
                <td>${item.sugarLevel}%</td>
                <td>${item.iceLevel}%</td>
                <td><c:forEach items="${item.toppingNames}" var="tn">${tn}<br/></c:forEach></td>
                <td>
                    <form action="${pageContext.request.contextPath}/cart/update" method="post" class="d-flex">
                        <input type="hidden" name="index" value="${st.index}">
                        <input type="number" name="quantity" value="${item.quantity}" min="1" class="form-control form-control-sm" style="width:70px">
                        <button class="btn btn-sm btn-outline-secondary ms-1">✓</button>
                    </form>
                </td>
                <td><fmt:formatNumber value="${item.unitPrice}" pattern="#,##0"/> ₫</td>
                <td class="text-danger fw-bold"><fmt:formatNumber value="${item.subtotal}" pattern="#,##0"/> ₫</td>
                <td>
                    <a href="${pageContext.request.contextPath}/cart/remove/${st.index}" class="btn btn-sm btn-danger">✕</a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
    <tfoot>
        <tr>
            <th colspan="8" class="text-end">Tổng cộng:</th>
            <th colspan="2" class="text-danger fs-5"><fmt:formatNumber value="${total}" pattern="#,##0"/> ₫</th>
        </tr>
    </tfoot>
</table>

<a href="${pageContext.request.contextPath}/order/checkout" class="btn btn-warning btn-lg">Tiến hành đặt hàng →</a>
</c:if>