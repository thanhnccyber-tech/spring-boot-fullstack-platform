<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<title>Quản lý sản phẩm</title>

<a href="${pageContext.request.contextPath}/admin/products/new" class="btn btn-warning mb-3">
    <i class="fas fa-plus"></i> Thêm sản phẩm
</a>

<c:if test="${success != null}"><div class="alert alert-success">${success}</div></c:if>

<table class="table table-bordered bg-white">
    <thead class="table-dark">
        <tr><th>ID</th><th>Ảnh</th><th>Tên</th><th>Danh mục</th><th>Giá</th><th>Active</th><th>Hành động</th></tr>
    </thead>
    <tbody>
        <c:forEach items="${products}" var="p">
            <tr>
                <td>${p.id}</td>
                <td><img src="${p.imageUrl}" width="60"></td>
                <td>${p.name}</td>
                <td>${p.category != null ? p.category.name : '-'}</td>
                <td><fmt:formatNumber value="${p.basePrice}" pattern="#,##0"/> ₫</td>
                <td>${p.active ? '✔' : '✘'}</td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/products/edit/${p.id}" class="btn btn-sm btn-primary">Sửa</a>
                    <a href="${pageContext.request.contextPath}/admin/products/delete/${p.id}" class="btn btn-sm btn-danger"
                       onclick="return confirm('Xóa?')">Xóa</a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>