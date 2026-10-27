<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Người dùng</title>

<c:if test="${success != null}">
    <div class="alert alert-success alert-dismissible fade show">
        ${fn:escapeXml(success)}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>
<c:if test="${error != null}">
    <div class="alert alert-danger alert-dismissible fade show">
        ${fn:escapeXml(error)}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<c:if test="${!isAdmin && managerBranchName != null}">
    <div class="alert alert-info d-flex align-items-center mb-3">
        <i class="fas fa-circle-info me-2"></i>
        <span>Chi nhánh: <b>${fn:escapeXml(managerBranchName)}</b> — Bạn chỉ thấy nhân viên (Staff) trong chi nhánh của mình.</span>
    </div>
</c:if>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
    <div class="text-muted">
        <i class="fas fa-users me-1"></i>
        Tổng cộng <b class="text-dark">${fn:escapeXml(userPage.totalElements)}</b> người dùng
    </div>
    <a href="${pageContext.request.contextPath}/admin/users/new" class="btn btn-warning">
        <i class="fas fa-user-plus me-1"></i> Thêm người dùng
    </a>
</div>

<div class="card mb-3">
    <div class="card-body py-3">
        <form method="get" class="row g-2 align-items-end">
            <div class="col-md-3">
                <label class="form-label small fw-semibold text-muted mb-1">Tìm kiếm</label>
                <input type="text" name="kw" value="${fn:escapeXml(kw)}" class="form-control"
                       placeholder="Tên / email / SĐT...">
            </div>

            <c:choose>
                <c:when test="${!isAdmin}">
                    <div class="col-md-2">
                        <label class="form-label small fw-semibold text-muted mb-1">Vai trò</label>
                        <input type="text" class="form-control" value="Staff" disabled>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label small fw-semibold text-muted mb-1">Chi nhánh</label>
                        <input type="text" class="form-control" value="${fn:escapeXml(managerBranchName)}" disabled>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="col-md-2">
                        <label class="form-label small fw-semibold text-muted mb-1">Vai trò</label>
                        <select name="role" class="form-select">
                            <option value="">Tất cả</option>
                            <c:forEach items="${roles}" var="r">
                                <option value="${fn:escapeXml(r.name)}" <c:if test="${roleFilter == r.name}">selected</c:if>>
                                    ${fn:escapeXml(fn:replace(r.name, 'ROLE_', ''))}
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label small fw-semibold text-muted mb-1">Chi nhánh</label>
                        <select name="branchId" class="form-select">
                            <option value="">Tất cả</option>
                            <c:forEach items="${branches}" var="b">
                                <option value="${fn:escapeXml(b.id)}" <c:if test="${branchFilter == b.id}">selected</c:if>>${fn:escapeXml(b.name)}</option>
                            </c:forEach>
                        </select>
                    </div>
                </c:otherwise>
            </c:choose>

            <div class="col-md-2">
                <label class="form-label small fw-semibold text-muted mb-1">Trạng thái</label>
                <select name="enabled" class="form-select">
                    <option value="">Tất cả</option>
                    <option value="true"  <c:if test="${enabledFilter == 'true'}">selected</c:if>>Hoạt động</option>
                    <option value="false" <c:if test="${enabledFilter == 'false'}">selected</c:if>>Đã khóa</option>
                </select>
            </div>

            <div class="col-md-2">
                <button class="btn btn-warning w-100"><i class="fas fa-search me-1"></i> Lọc</button>
            </div>
        </form>
        <c:if test="${not empty kw or not empty roleFilter or not empty branchFilter or not empty enabledFilter}">
            <a href="${pageContext.request.contextPath}/admin/users" class="small text-decoration-none d-inline-block mt-2">
                <i class="fas fa-times"></i> Xóa bộ lọc
            </a>
        </c:if>
    </div>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-dark">
                <tr>
                    <th style="width:70px" class="text-center">STT</th>
                    <th>Email</th>
                    <th>Họ tên</th>
                    <th style="width:160px">Vai trò</th>
                    <th style="width:150px">Chi nhánh</th>
                    <th class="text-center" style="width:120px">Trạng thái</th>
                    <th class="text-end" style="width:170px">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${userPage.content}" var="u" varStatus="st">
                    <tr>
                        <td class="text-center text-muted small">${userPage.number * userPage.size + st.index + 1}</td>
                        <td>
                            <div class="fw-semibold">${fn:escapeXml(u.email)}</div>
                            <c:if test="${u.id == currentUserId}">
                                <span class="badge bg-info text-dark">Bạn</span>
                            </c:if>
                        </td>
                        <td>${fn:escapeXml(u.fullName)}</td>
                        <td>
                            <c:forEach items="${u.roles}" var="r">
                                <span class="badge bg-secondary">${fn:escapeXml(fn:replace(r.name, 'ROLE_', ''))}</span>
                            </c:forEach>
                        </td>
                        <td>${fn:escapeXml(u.branch != null ? u.branch.name : '—')}</td>
                        <td class="text-center">
                            <span class="badge px-3 py-2 ${u.enabled ? 'bg-success' : 'bg-danger'}">
                                <i class="fas ${u.enabled ? 'fa-check-circle' : 'fa-lock'} me-1"></i>${u.enabled ? 'Hoạt động' : 'Đã khóa'}
                            </span>
                        </td>
                        <td class="text-end">
                            <div class="d-inline-flex gap-1">
                                <a href="${pageContext.request.contextPath}/admin/users/edit/${fn:escapeXml(u.id)}"
                                   class="btn btn-sm btn-outline-primary" title="Sửa người dùng">
                                    <i class="fas fa-pen"></i>
                                </a>
                                <c:if test="${u.id != currentUserId}">
                                    <form action="${pageContext.request.contextPath}/admin/users/toggle-enabled" method="post"
                                          onsubmit="return confirm('${u.enabled ? 'Khóa' : 'Mở khóa'} tài khoản này?')">
                                        <input type="hidden" name="id" value="${fn:escapeXml(u.id)}">
                                        <button class="btn btn-sm ${u.enabled ? 'btn-outline-warning' : 'btn-outline-success'}"
                                                title="${u.enabled ? 'Khóa tài khoản' : 'Mở khóa tài khoản'}">
                                            <i class="fas ${u.enabled ? 'fa-lock' : 'fa-unlock'}"></i>
                                        </button>
                                    </form>
                                </c:if>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty userPage.content}">
                    <tr>
                        <td colspan="7" class="text-center text-muted py-5">
                            <i class="fas fa-user-slash fa-2x d-block mb-2 opacity-50"></i>
                            Không có người dùng nào
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<c:if test="${userPage.totalPages > 1}">
    <nav class="mt-3">
        <ul class="pagination justify-content-center mb-0">
            <c:forEach begin="0" end="${userPage.totalPages - 1}" var="i">
                <li class="page-item ${i == userPage.number ? 'active' : ''}">
                    <a class="page-link" href="?page=${i}&kw=${fn:escapeXml(kw)}&role=${fn:escapeXml(roleFilter)}&branchId=${fn:escapeXml(branchFilter)}&enabled=${fn:escapeXml(enabledFilter)}">
                        ${i + 1}
                    </a>
                </li>
            </c:forEach>
        </ul>
    </nav>
</c:if>