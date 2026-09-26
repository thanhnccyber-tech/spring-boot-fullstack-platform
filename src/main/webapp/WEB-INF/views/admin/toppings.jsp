<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<title>Topping</title>
<a href="${pageContext.request.contextPath}/admin/toppings/new" class="btn btn-warning mb-3">+ Thêm Topping</a>
<table class="table bg-white">
    <thead class="table-dark"><tr><th>ID</th><th>Tên</th><th>Giá</th><th>Active</th><th></th></tr></thead>
    <tbody>
        <c:forEach items="${toppings}" var="t">
            <tr>
                <td>${t.id}</td><td>${t.name}</td>
                <td><fmt:formatNumber value="${t.price}" pattern="#,##0"/> ₫</td>
                <td>${t.active ? '✔' : '✘'}</td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/toppings/edit/${t.id}" class="btn btn-sm btn-primary">Sửa</a>
                    <a href="${pageContext.request.contextPath}/admin/toppings/delete/${t.id}" class="btn btn-sm btn-danger">Xóa</a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>