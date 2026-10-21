<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<title>Quản lý sản phẩm</title>

<c:if test="${success != null}"><div class="alert alert-success">${success}</div></c:if>
<c:if test="${error != null}"><div class="alert alert-danger">${error}</div></c:if>

<a href="${pageContext.request.contextPath}/admin/products/new" class="btn btn-warning mb-3">
    <i class="fas fa-plus"></i> Thêm sản phẩm
</a>

<table class="table table-bordered bg-white align-middle">
    <thead class="table-dark">
        <tr><th>ID</th><th>Ảnh</th><th>Tên</th><th>Danh mục</th><th>Giá</th><th>Active</th><th>Hành động</th></tr>
    </thead>
    <tbody>
        <c:forEach items="${products}" var="p">
            <tr>
                <td>${p.id}</td>
                <td><img src="${p.imageUrl}" width="60" style="object-fit:cover;height:60px"></td>
                <td>${p.name}</td>
                <td>${p.category != null ? p.category.name : '-'}</td>
                <td><fmt:formatNumber value="${p.basePrice}" pattern="#,##0"/> ₫</td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/products/toggle/${p.id}"
                       class="badge ${p.active ? 'bg-success' : 'bg-secondary'} text-decoration-none">
                        ${p.active ? 'Đang bán' : 'Đã ẩn'}
                    </a>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/products/edit/${p.id}"
                       class="btn btn-sm btn-primary">Sửa</a>
                    <a href="${pageContext.request.contextPath}/admin/products/delete/${p.id}"
                       class="btn btn-sm btn-danger"
                       onclick="return confirm('Xóa sản phẩm này?')">Xóa</a>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty products}">
            <tr><td colspan="7" class="text-center text-muted">Chưa có sản phẩm</td></tr>
        </c:if>
    </tbody>
</table>