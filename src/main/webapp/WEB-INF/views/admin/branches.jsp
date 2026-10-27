<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Chi nhánh</title>

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

<div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
    <div class="text-muted">
        <i class="fas fa-store me-1"></i>
        Tổng cộng <b class="text-dark">${fn:escapeXml(pageData.totalElements)}</b> chi nhánh
    </div>
    <a href="${pageContext.request.contextPath}/admin/branches/new" class="btn btn-warning">
        <i class="fas fa-plus me-1"></i> Thêm chi nhánh
    </a>
</div>

<div class="card mb-3">
    <div class="card-body py-3">
        <form method="get" class="row g-2 align-items-end">
            <div class="col-md-6">
                <label class="form-label small fw-semibold text-muted mb-1">Tìm kiếm</label>
                <input type="text" name="kw" value="${fn:escapeXml(kw)}" class="form-control"
                       placeholder="Tìm theo tên, địa chỉ hoặc SĐT...">
            </div>
            <div class="col-md-3">
                <label class="form-label small fw-semibold text-muted mb-1">Trạng thái</label>
                <select name="active" class="form-select">
                    <option value="">Tất cả</option>
                    <option value="true"  <c:if test="${activeFilter == 'true'}">selected</c:if>>Đang hoạt động</option>
                    <option value="false" <c:if test="${activeFilter == 'false'}">selected</c:if>>Đã khóa</option>
                </select>
            </div>
            <div class="col-md-3">
                <button class="btn btn-warning w-100"><i class="fas fa-search me-1"></i> Tìm</button>
            </div>
        </form>
        <c:if test="${not empty kw or not empty activeFilter}">
            <a href="${pageContext.request.contextPath}/admin/branches" class="small text-decoration-none d-inline-block mt-2">
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
                    <th style="width:80px">ID</th>
                    <th>Tên chi nhánh</th>
                    <th>Địa chỉ</th>
                    <th style="width:150px">Điện thoại</th>
                    <th class="text-center" style="width:150px">Trạng thái</th>
                    <th class="text-end" style="width:170px">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${pageData.content}" var="b">
                    <tr>
                        <td class="text-muted small">#${fn:escapeXml(b.id)}</td>
                        <td class="fw-semibold">${fn:escapeXml(b.name)}</td>
                        <td class="text-muted small">${fn:escapeXml(b.address)}</td>
                        <td>${fn:escapeXml(b.phone)}</td>
                        <td class="text-center">
                            <form action="${pageContext.request.contextPath}/admin/branches/toggle-active" method="post">
                                <input type="hidden" name="id" value="${fn:escapeXml(b.id)}">
                                <input type="hidden" name="returnUrl"
                                       value="/admin/branches?page=${fn:escapeXml(pageData.number)}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}">
                                <button class="badge border-0 px-3 py-2 ${b.active ? 'bg-success' : 'bg-secondary'}"
                                        title="${b.active ? 'Bấm để khóa' : 'Bấm để mở khóa'}">
                                    <i class="fas ${b.active ? 'fa-check-circle' : 'fa-lock'} me-1"></i>${b.active ? 'Đang hoạt động' : 'Đã khóa'}
                                </button>
                            </form>
                        </td>
                        <td class="text-end">
                            <div class="d-inline-flex gap-1">
                                <a href="${pageContext.request.contextPath}/admin/branches/edit/${fn:escapeXml(b.id)}"
                                   class="btn btn-sm btn-outline-primary" title="Sửa chi nhánh">
                                    <i class="fas fa-pen"></i>
                                </a>
                                <c:choose>
                                    <c:when test="${b.active}">
                                        <form action="${pageContext.request.contextPath}/admin/branches/delete" method="post"
                                              onsubmit="return confirm('Khóa chi nhánh này?')">
                                            <input type="hidden" name="id" value="${fn:escapeXml(b.id)}">
                                            <button class="btn btn-sm btn-outline-danger" title="Khóa chi nhánh">
                                                <i class="fas fa-lock"></i>
                                            </button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <form action="${pageContext.request.contextPath}/admin/branches/restore" method="post"
                                              onsubmit="return confirm('Mở khóa chi nhánh này?')">
                                            <input type="hidden" name="id" value="${fn:escapeXml(b.id)}">
                                            <button class="btn btn-sm btn-outline-success" title="Mở khóa chi nhánh">
                                                <i class="fas fa-unlock"></i>
                                            </button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty pageData.content}">
                    <tr>
                        <td colspan="6" class="text-center text-muted py-5">
                            <i class="fas fa-store-slash fa-2x d-block mb-2 opacity-50"></i>
                            Không có chi nhánh nào khớp điều kiện
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<c:if test="${pageData.totalPages > 1}">
    <nav class="mt-3">
        <ul class="pagination justify-content-center mb-0">
            <li class="page-item ${pageData.first ? 'disabled' : ''}">
                <a class="page-link" href="?page=${pageData.number - 1}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}">
                    <i class="fas fa-chevron-left"></i>
                </a>
            </li>
            <c:set var="startPage" value="${pageData.number - 2}"/>
            <c:if test="${startPage < 0}"><c:set var="startPage" value="0"/></c:if>
            <c:set var="endPage" value="${startPage + 4}"/>
            <c:if test="${endPage >= pageData.totalPages}">
                <c:set var="endPage" value="${pageData.totalPages - 1}"/>
                <c:set var="startPage" value="${endPage - 4}"/>
                <c:if test="${startPage < 0}"><c:set var="startPage" value="0"/></c:if>
            </c:if>
            <c:forEach begin="${startPage}" end="${endPage}" var="i">
                <li class="page-item ${i == pageData.number ? 'active' : ''}">
                    <a class="page-link" href="?page=${i}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}">${i + 1}</a>
                </li>
            </c:forEach>
            <li class="page-item ${pageData.last ? 'disabled' : ''}">
                <a class="page-link" href="?page=${pageData.number + 1}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}">
                    <i class="fas fa-chevron-right"></i>
                </a>
            </li>
        </ul>
    </nav>
</c:if>