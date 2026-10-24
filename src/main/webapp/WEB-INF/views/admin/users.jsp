<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Người dùng</title>

<c:if test="${success != null}"><div class="alert alert-success">${fn:escapeXml(success)}</div></c:if>
<c:if test="${error != null}"><div class="alert alert-danger">${fn:escapeXml(error)}</div></c:if>

<div class="d-flex justify-content-between align-items-center mb-3">
    <div>
        <h5 class="mb-0">👥 Quản lý người dùng</h5>
        <c:if test="${!isAdmin && managerBranchName != null}">
            <small class="text-muted">
                Chi nhánh: <b>${fn:escapeXml(managerBranchName)}</b>
                — Chỉ hiển thị nhân viên (Staff) trong chi nhánh của bạn.
            </small>
        </c:if>
    </div>
    <a href="${pageContext.request.contextPath}/admin/users/new" class="btn btn-warning">
        <i class="fas fa-user-plus"></i> Thêm người dùng
    </a>
</div>

<form class="row g-2 mb-3 bg-white p-3 rounded shadow-sm" method="get">
    <div class="col-md-3">
        <input type="text" name="kw" value="${fn:escapeXml(kw)}" class="form-control" placeholder="Tên/email/SĐT">
    </div>

    <c:choose>
        <c:when test="${!isAdmin}">
            <div class="col-md-2">
                <input type="text" class="form-control" value="Staff" disabled>
            </div>
            <div class="col-md-3">
                <input type="text" class="form-control" value="${fn:escapeXml(managerBranchName)}" disabled>
            </div>
        </c:when>
        <c:otherwise>
            <div class="col-md-2">
                <select name="role" class="form-select">
                    <option value="">-- Vai trò --</option>
                    <c:forEach items="${roles}" var="r">
                        <option value="${fn:escapeXml(r.name)}" <c:if test="${roleFilter == r.name}">selected</c:if>>
                            ${fn:escapeXml(fn:replace(r.name, 'ROLE_', ''))}
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-3">
                <select name="branchId" class="form-select">
                    <option value="">-- Chi nhánh --</option>
                    <c:forEach items="${branches}" var="b">
                        <option value="${fn:escapeXml(b.id)}" <c:if test="${branchFilter == b.id}">selected</c:if>>${fn:escapeXml(b.name)}</option>
                    </c:forEach>
                </select>
            </div>
        </c:otherwise>
    </c:choose>

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
        <tr>
            <th>STT</th>
            <th>Email</th>
            <th>Họ tên</th>
            <th>Vai trò</th>
            <th>Chi nhánh</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${userPage.content}" var="u" varStatus="st">
            <tr>
                <td>${fn:escapeXml(userPage.number * userPage.size + st.index + 1)}</td>
                <td>${fn:escapeXml(u.email)}
                    <c:if test="${u.id == currentUserId}">
                        <span class="badge bg-info ms-1">Bạn</span>
                    </c:if>
                </td>
                <td>${fn:escapeXml(u.fullName)}</td>
                <td>
                    <c:forEach items="${u.roles}" var="r">
                        <span class="badge bg-secondary">${fn:escapeXml(fn:replace(r.name, 'ROLE_', ''))}</span>
                    </c:forEach>
                </td>
                <td>${fn:escapeXml(u.branch != null ? u.branch.name : '—')}</td>
                <td>
                    <span class="badge ${fn:escapeXml(u.enabled ? 'bg-success' : 'bg-danger')}">
                        ${fn:escapeXml(u.enabled ? 'Hoạt động' : 'Đã khóa')}
                    </span>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/users/edit/${fn:escapeXml(u.id)}"
                       class="btn btn-sm btn-primary">Sửa</a>
                    <c:if test="${u.id != currentUserId}">
                        <form action="${pageContext.request.contextPath}/admin/users/toggle-enabled"
                              method="post" style="display:inline">
                            <input type="hidden" name="id" value="${fn:escapeXml(u.id)}">
                            <button class="btn btn-sm ${fn:escapeXml(u.enabled ? 'btn-warning' : 'btn-success')}">
                                ${fn:escapeXml(u.enabled ? 'Khóa' : 'Mở khóa')}
                            </button>
                        </form>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty userPage.content}">
            <tr><td colspan="7" class="text-center text-muted">Không có người dùng nào</td></tr>
        </c:if>
    </tbody>
</table>

<c:if test="${userPage.totalPages > 1}">
    <nav>
        <ul class="pagination justify-content-center">
            <c:forEach begin="0" end="${userPage.totalPages - 1}" var="i">
                <li class="page-item ${fn:escapeXml(i == userPage.number ? 'active' : '')}">
                    <a class="page-link"
                       href="?page=${fn:escapeXml(i)}&kw=${fn:escapeXml(kw)}&role=${fn:escapeXml(roleFilter)}&branchId=${fn:escapeXml(branchFilter)}&enabled=${fn:escapeXml(enabledFilter)}">
                        ${fn:escapeXml(i + 1)}
                    </a>
                </li>
            </c:forEach>
        </ul>
    </nav>
</c:if>