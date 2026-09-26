<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<title>Đơn hàng</title>
<table class="table bg-white">
    <thead class="table-dark"><tr><th>Mã</th><th>Khách</th><th>Chi nhánh</th><th>Tổng</th><th>Trạng thái</th><th>Ngày</th><th></th></tr></thead>
    <tbody>
        <c:forEach items="${orders}" var="o">
            <tr>
                <td>${o.orderCode}</td>
                <td>${o.user.fullName}</td>
                <td>${o.branch.name}</td>
                <td><fmt:formatNumber value="${o.totalAmount}" pattern="#,##0"/> ₫</td>
                <td><span class="badge bg-info">${o.status}</span></td>
                <td><fmt:formatDate value="${o.createdAt}" pattern="dd/MM HH:mm"/></td>
                <td><a href="${pageContext.request.contextPath}/admin/orders/detail/${o.id}" class="btn btn-sm btn-primary">Xem</a></td>
            </tr>
        </c:forEach>
    </tbody>
</table>