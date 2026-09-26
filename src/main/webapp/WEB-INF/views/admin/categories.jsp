<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<title>Danh mục</title>
<a href="${pageContext.request.contextPath}/admin/categories/new" class="btn btn-warning mb-3">+ Thêm</a>
<table class="table bg-white">
    <thead class="table-dark"><tr><th>ID</th><th>Ảnh</th><th>Tên</th><th>Mô tả</th><th>Active</th><th></th></tr></thead>
    <tbody>
        <c:forEach items="${categories}" var="c">
            <tr>
                <td>${c.id}</td>
                <td><img src="${c.imageUrl}" width="60"></td>
                <td>${c.name}</td>
                <td>${c.description}</td>
                <td>${c.active ? '✔' : '✘'}</td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/categories/edit/${c.id}" class="btn btn-sm btn-primary">Sửa</a>
                    <a href="${pageContext.request.contextPath}/admin/categories/delete/${c.id}" class="btn btn-sm btn-danger">Xóa</a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>