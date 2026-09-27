<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Người dùng</title>

<c:if test="${success != null}">
    <div class="alert alert-success alert-dismissible fade show">
        ${success}
        <button class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>
<c:if test="${error != null}">
    <div class="alert alert-danger alert-dismissible fade show">
        ${error}
        <button class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<%-- ===== Header + Nút thêm ===== --%>
<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
    <h5 class="mb-0">👥 Quản lý người dùng</h5>
    <div>
        <c:if test="${isAdmin}">
            <a href="${pageContext.request.contextPath}/admin/users/new?presetRole=ROLE_MANAGER"
               class="btn btn-outline-primary">
                <i class="fas fa-user-tie"></i> Thêm quản lý
            </a>
            <a href="${pageContext.request.contextPath}/admin/users/new?presetRole=ROLE_ADMIN"
               class="btn btn-outline-danger">
                <i class="fas fa-user-shield"></i> Thêm admin
            </a>
        </c:if>
        <a href="${pageContext.request.contextPath}/admin/users/new?presetRole=ROLE_STAFF"
           class="btn btn-warning">
            <i class="fas fa-user-plus"></i> Thêm nhân viên
        </a>
    </div>
</div>

<%-- ===== Filters ===== --%>
<form class="row g-2 mb-3 bg-white p-3 rounded shadow-sm" method="get">
    <div class="col-md-3">
        <input type="text" name="kw" value="${kw}" class="form-control"
               placeholder="Tìm tên / email / SĐT...">
    </div>

    <div class="col-md-2">
        <select name="role" class="form-select" ${isAdmin ? '' : 'disabled'}>
            <option value="">-- Vai trò --</option>
            <c:forEach items="${roles}" var="r">
                <option value="${r.name}"
                    <c:if test="${roleFilter == r.name}">selected</c:if>>
                    ${fn:replace(r.name, 'ROLE_', '')}
                </option>
            </c:forEach>
        </select>
        <c:if test="${!isAdmin}">
            <input type="hidden" name="role" value="${roleFilter}">
        </c:if>
    </div>

    <div class="col-md-3">
        <select name="branchId" class="form-select" ${isAdmin ? '' : 'disabled'}>
            <option value="">-- Chi nhánh --</option>
            <c:forEach items="${branches}" var="b">
                <option value="${b.id}"
                    <c:if test="${branchFilter == b.id}">selected</c:if>>
                    ${b.name}
                </option>
            </c:forEach>
        </select>
        <c:if test="${!isAdmin}">
            <input type="hidden" name="branchId" value="${branchFilter}">
        </c:if>
    </div>

    <div class="col-md-2">
        <select name="enabled" class="form-select">
            <option value="">-- Trạng thái --</option>
            <option value="true"  <c:if test="${enabledFilter == 'true'}">selected</c:if>>Đang hoạt động</option>
            <option value="false" <c:if test="${enabledFilter == 'false'}">selected</c:if>>Đã khóa</option>
        </select>
    </div>

    <div class="col-md-2 d-flex gap-1">
        <button class="btn btn-warning flex-grow-1">
            <i class="fas fa-search"></i> Lọc
        </button>
        <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-outline-secondary">
            <i class="fas fa-redo"></i>
        </a>
    </div>
</form>

<%-- URL giữ filter để nhúng vào action/returnUrl --%>
<c:url var="filterUrl" value="/admin/users">
    <c:if test="${not empty kw}"><c:param name="kw" value="${kw}"/></c:if>
    <c:if test="${not empty roleFilter}"><c:param name="role" value="${roleFilter}"/></c:if>
    <c:if test="${not empty branchFilter}"><c:param name="branchId" value="${branchFilter}"/></c:if>
    <c:if test="${not empty enabledFilter}"><c:param name="enabled" value="${enabledFilter}"/></c:if>
    <c:if test="${userPage.number > 0}"><c:param name="page" value="${userPage.number}"/></c:if>
</c:url>

<%-- ===== Table ===== --%>
<table class="table table-hover bg-white align-middle">
    <thead class="table-dark">
        <tr>
            <th style="width:60px">STT</th>
            <th>Email</th>
            <th>Họ tên</th>
            <th>SĐT</th>
            <th>Vai trò</th>
            <th>Chi nhánh</th>
            <th>Trạng thái</th>
            <th style="width:220px">Thao tác</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${userPage.content}" var="u" varStatus="st">
            <c:set var="isSelf" value="${u.id == currentUserId}"/>
            <tr>
                <td>${userPage.number * userPage.size + st.index + 1}</td>
                <td>
                    <c:if test="${u.avatarUrl != null}">
                        <img src="${u.avatarUrl}" class="rounded-circle me-2"
                             style="width:32px;height:32px;object-fit:cover">
                    </c:if>
                    ${u.email}
                    <c:if test="${isSelf}">
                        <span class="badge bg-info ms-1">Bạn</span>
                    </c:if>
                </td>
                <td>${u.fullName}</td>
                <td>${u.phone}</td>
                <td>
                    <c:forEach items="${u.roles}" var="r">
                        <span class="badge bg-secondary">
                            ${fn:replace(r.name, 'ROLE_', '')}
                        </span>
                    </c:forEach>
                </td>
                <td>${u.branch != null ? u.branch.name : '—'}</td>
                <td>
                    <c:choose>
                        <c:when test="${u.enabled}">
                            <span class="badge bg-success">Hoạt động</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge bg-danger">Đã khóa</span>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/users/edit/${u.id}"
                       class="btn btn-sm btn-primary" title="Sửa">
                        <i class="fas fa-edit"></i>
                    </a>

                    <%-- Lock/Unlock 1-click --%>
                    <c:choose>
                        <c:when test="${isSelf}">
                            <button class="btn btn-sm btn-secondary" disabled
                                    title="Không thể tự khóa chính mình">
                                <i class="fas fa-lock"></i>
                            </button>
                        </c:when>
                        <c:otherwise>
                            <form action="${pageContext.request.contextPath}/admin/users/toggle-enabled"
                                  method="post" style="display:inline">
                                <input type="hidden" name="id" value="${u.id}">
                                <input type="hidden" name="returnUrl" value="${filterUrl}">
                                <button class="btn btn-sm ${u.enabled ? 'btn-warning' : 'btn-success'}"
                                        onclick="return confirm('${u.enabled ? 'Khóa' : 'Mở khóa'} tài khoản ${u.email}?')"
                                        title="${u.enabled ? 'Khóa' : 'Mở khóa'}">
                                    <i class="fas fa-${u.enabled ? 'lock' : 'unlock'}"></i>
                                </button>
                            </form>
                        </c:otherwise>
                    </c:choose>

                    <%-- Reset password modal --%>
                    <button type="button" class="btn btn-sm btn-outline-danger"
                            data-bs-toggle="modal"
                            data-bs-target="#resetModal"
                            data-user-id="${u.id}"
                            data-user-email="${u.email}"
                            title="Cấp lại mật khẩu">
                        <i class="fas fa-key"></i>
                    </button>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty userPage.content}">
            <tr>
                <td colspan="8" class="text-center text-muted py-4">
                    Không có người dùng nào khớp bộ lọc.
                </td>
            </tr>
        </c:if>
    </tbody>
</table>

<%-- ===== Pagination ===== --%>
<c:if test="${userPage.totalPages > 1}">
    <nav>
        <ul class="pagination justify-content-center">
            <li class="page-item ${userPage.first ? 'disabled' : ''}">
                <c:url var="prevUrl" value="/admin/users">
                    <c:if test="${not empty kw}"><c:param name="kw" value="${kw}"/></c:if>
                    <c:if test="${not empty roleFilter}"><c:param name="role" value="${roleFilter}"/></c:if>
                    <c:if test="${not empty branchFilter}"><c:param name="branchId" value="${branchFilter}"/></c:if>
                    <c:if test="${not empty enabledFilter}"><c:param name="enabled" value="${enabledFilter}"/></c:if>
                    <c:param name="page" value="${userPage.number - 1}"/>
                </c:url>
                <a class="page-link" href="${prevUrl}">&laquo;</a>
            </li>

            <c:forEach begin="0" end="${userPage.totalPages - 1}" var="i">
                <c:url var="pUrl" value="/admin/users">
                    <c:if test="${not empty kw}"><c:param name="kw" value="${kw}"/></c:if>
                    <c:if test="${not empty roleFilter}"><c:param name="role" value="${roleFilter}"/></c:if>
                    <c:if test="${not empty branchFilter}"><c:param name="branchId" value="${branchFilter}"/></c:if>
                    <c:if test="${not empty enabledFilter}"><c:param name="enabled" value="${enabledFilter}"/></c:if>
                    <c:param name="page" value="${i}"/>
                </c:url>
                <li class="page-item ${i == userPage.number ? 'active' : ''}">
                    <a class="page-link" href="${pUrl}">${i + 1}</a>
                </li>
            </c:forEach>

            <li class="page-item ${userPage.last ? 'disabled' : ''}">
                <c:url var="nextUrl" value="/admin/users">
                    <c:if test="${not empty kw}"><c:param name="kw" value="${kw}"/></c:if>
                    <c:if test="${not empty roleFilter}"><c:param name="role" value="${roleFilter}"/></c:if>
                    <c:if test="${not empty branchFilter}"><c:param name="branchId" value="${branchFilter}"/></c:if>
                    <c:if test="${not empty enabledFilter}"><c:param name="enabled" value="${enabledFilter}"/></c:if>
                    <c:param name="page" value="${userPage.number + 1}"/>
                </c:url>
                <a class="page-link" href="${nextUrl}">&raquo;</a>
            </li>
        </ul>
    </nav>
    <p class="text-center text-muted small">
        Trang ${userPage.number + 1} / ${userPage.totalPages}
        — Tổng ${userPage.totalElements} người dùng
    </p>
</c:if>

<%-- ===== Reset Password Modal ===== --%>
<div class="modal fade" id="resetModal" tabindex="-1">
    <div class="modal-dialog">
        <form class="modal-content"
              action="${pageContext.request.contextPath}/admin/users/reset-password"
              method="post">
            <div class="modal-header">
                <h5 class="modal-title">🔑 Cấp lại mật khẩu</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="id" id="resetId">
                <input type="hidden" name="returnUrl" value="${filterUrl}">
                <p>Cấp lại mật khẩu cho tài khoản <b id="resetEmail"></b>?</p>
                <div class="mb-3">
                    <label class="form-label">Mật khẩu mới (tối thiểu 6 ký tự)</label>
                    <input type="text" name="newPassword" class="form-control"
                           required minlength="6" value="utetra@123">
                    <small class="text-muted">Mặc định gợi ý: <code>utetra@123</code></small>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                <button class="btn btn-danger">Xác nhận cấp lại</button>
            </div>
        </form>
    </div>
</div>

<script>
    document.getElementById('resetModal').addEventListener('show.bs.modal', function (event) {
        const btn = event.relatedTarget;
        document.getElementById('resetId').value    = btn.getAttribute('data-user-id');
        document.getElementById('resetEmail').textContent = btn.getAttribute('data-user-email');
    });
</script>