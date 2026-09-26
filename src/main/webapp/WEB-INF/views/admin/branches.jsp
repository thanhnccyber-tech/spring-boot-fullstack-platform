<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<title>Chi nhánh</title>
<a href="${pageContext.request.contextPath}/admin/branches/new" class="btn btn-warning mb-3">+ Thêm chi nhánh</a>
<table class="table bg-white">
    <thead class="table-dark"><tr><th>ID</th><th>Tên</th><th>Địa chỉ</th><th>Điện thoại</th><th></th></tr></thead>
    <tbody>
        <c:forEach items="${branches}" var="b">
            <tr>
                <td>${b.id}</td><td>${b.name}</td><td>${b.address}</td><td>${b.phone}</td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/branches/edit/${b.id}" class="btn btn-sm btn-primary">Sửa</a>
                    <a href="${pageContext.request.contextPath}/admin/branches/delete/${b.id}" class="btn btn-sm btn-danger">Xóa</a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>