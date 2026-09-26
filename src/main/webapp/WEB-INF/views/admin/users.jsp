<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<title>Người dùng</title>
<table class="table bg-white">
    <thead class="table-dark"><tr><th>ID</th><th>Email</th><th>Họ tên</th><th>Vai trò</th><th>Chi nhánh</th></tr></thead>
    <tbody>
        <c:forEach items="${users}" var="u">
            <tr>
                <td>${u.id}</td>
                <td>${u.email}</td>
                <td>${u.fullName}</td>
                <td>
                    <c:forEach items="${u.roles}" var="r">
                        <span class="badge bg-secondary">${r.name}</span>
                    </c:forEach>
                </td>
                <td>${u.branch != null ? u.branch.name : '-'}</td>
            </tr>
        </c:forEach>
    </tbody>
</table>