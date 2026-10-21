<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Người dùng</title>

<c:if test="${success != null}"><div class="alert alert-success">${success}</div></c:if>
<c:if test="${error != null}"><div class="alert alert-danger">${error}</div></c:if>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5>👥 Quản lý người dùng</h5>
    <a href="${pageContext.request.contextPath}/admin/users/new" class="btn btn-warning">
        <i class="fas fa-user-plus"></i> Thêm người dùng
    </a>
</div>

<form class="row g-2 mb-3 bg-white p-3 rounded shadow-sm" method="get">
    <div class="col-md-3">
        <input type="text" name="kw" value="${kw}" class="form-control" placeholder="Tên/email/SĐT">
    </div>
    <div class="col-md-2">
        <select name="role" class="form-select">
            <option value="">-- Vai trò --</option>
            <c:forEach items="${roles}" var="r">
                <option value="${r.name}" <c:if test="${roleFilter == r.name}">selected</c:if>>
                    ${fn:replace(r.name, 'ROLE_', '')}
                </option>
            </c:forEach>
        </select>
    </div>
    <div class="col-md-3">
        <select name="branchId" class="form-select">
            <option value="">-- Chi nhánh --</option>
            <c:forEach items="${branches}" var="b">
                <option value="${b.id}" <c:if test="${branchFilter == b.id}">selected</c:if>>${b.name}</option>
            </c:forEach>
        </select>
    </div>
    <div class="col-md-2">
        <select name="enabled" class="form-select">
            <option value="">-- Trạng thái --</option>
            <option value="true"  <c:if test="${enabledFilter == 'true'}">selected</c:if>>Hoạt động</option>
            <option value="false" <c:if test="${enabledFilter == 'false'}">selected</c:if>>Đã khóa</option>
        </select>
    </div>
    <div class="col-md-2">
        <button class="btn btn-warning w-100">Lọc</button>
    </div>
</form>

<table class="table table-hover bg-white align-middle">
    <thead class="table-dark">
        <tr><th>STT</th><th>Email</th><th>Họ tên</th><th>Vai trò</th><th>Chi nhánh</th><th>Trạng thái</th><th>Thao tác</th></tr>
    </thead>
    <tbody>
        <c:forEach items="${userPage.content}" var="u" varStatus="st">
            <tr>
                <td>${userPage.number * userPage.size + st.index + 1}</td>
                <td>${u.email}
                    <c:if test="${u.id == currentUserId}">
                        <span class="badge bg-info ms-1">Bạn</span>
                    </c:if>
                </td>
                <td>${u.fullName}</td>
                <td>
                    <c:forEach items="${u.roles}" var="r">
                        <span class="badge bg-secondary">${fn:replace(r.name, 'ROLE_', '')}</span>
                    </c:forEach>
                </td>
                <td>${u.branch != null ? u.branch.name : '—'}</td>
                <td>
                    <span class="badge ${u.enabled ? 'bg-success' : 'bg-danger'}">
                        ${u.enabled ? 'Hoạt động' : 'Đã khóa'}
                    </span>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/users/edit/${u.id}"
                       class="btn btn-sm btn-primary">Sửa</a>
                    <c:if test="${u.id != currentUserId}">
                        <form action="${pageContext.request.contextPath}/admin/users/toggle-enabled"
                              method="post" style="display:inline">
                            <input type="hidden" name="id" value="${u.id}">
                            <button class="btn btn-sm ${u.enabled ? 'btn-warning' : 'btn-success'}">
                                ${u.enabled ? 'Khóa' : 'Mở khóa'}
                            </button>
                        </form>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<c:if test="${userPage.totalPages > 1}">
    <nav>
        <ul class="pagination justify-content-center">
            <c:forEach begin="0" end="${userPage.totalPages - 1}" var="i">
                <li class="page-item ${i == userPage.number ? 'active' : ''}">
                    <a class="page-link"
                       href="?page=${i}&kw=${kw}&role=${roleFilter}&branchId=${branchFilter}&enabled=${enabledFilter}">
                        ${i + 1}
                    </a>
                </li>
            </c:forEach>
        </ul>
    </nav>
</c:if>