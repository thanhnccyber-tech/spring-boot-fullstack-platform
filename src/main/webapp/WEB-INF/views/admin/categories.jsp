<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Quản lý danh mục</title>

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
        <i class="fas fa-folder me-1"></i>
        Tổng cộng <b class="text-dark">${fn:escapeXml(pageData.totalElements)}</b> danh mục
    </div>
    <a href="${pageContext.request.contextPath}/admin/categories/new" class="btn btn-warning">
        <i class="fas fa-plus me-1"></i> Thêm danh mục
    </a>
</div>

<div class="card mb-3">
    <div class="card-body py-3">
        <form method="get" class="row g-2 align-items-end">
            <div class="col-md-6">
                <label class="form-label small fw-semibold text-muted mb-1">Tìm kiếm</label>
                <input type="text" name="kw" value="${fn:escapeXml(kw)}" class="form-control"
                       placeholder="Tìm theo tên hoặc mô tả...">
            </div>
            <div class="col-md-3">
                <label class="form-label small fw-semibold text-muted mb-1">Trạng thái</label>
                <select name="active" class="form-select">
                    <option value="">Tất cả</option>
                    <option value="true"  <c:if test="${activeFilter == 'true'}">selected</c:if>>Đang hiển thị</option>
                    <option value="false" <c:if test="${activeFilter == 'false'}">selected</c:if>>Đã ẩn</option>
                </select>
            </div>
            <div class="col-md-3">
                <button class="btn btn-warning w-100"><i class="fas fa-search me-1"></i> Tìm</button>
            </div>
        </form>
        <c:if test="${not empty kw or not empty activeFilter}">
            <a href="${pageContext.request.contextPath}/admin/categories" class="small text-decoration-none d-inline-block mt-2">
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
                    <th style="width:80px">Ảnh</th>
                    <th>Danh mục</th>
                    <th>Mô tả</th>
                    <th class="text-center" style="width:110px">Sản phẩm</th>
                    <th class="text-center" style="width:90px">Thứ tự</th>
                    <th class="text-center" style="width:130px">Trạng thái</th>
                    <th class="text-end" style="width:140px">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${pageData.content}" var="c">
                    <tr>
                        <td>
                            <c:choose>
                                <c:when test="${not empty c.imageUrl}">
                                    <img src="${fn:escapeXml(c.imageUrl)}" alt="${fn:escapeXml(c.name)}"
                                         class="rounded" style="width:52px;height:52px;object-fit:cover">
                                </c:when>
                                <c:otherwise>
                                    <div class="rounded bg-light d-flex align-items-center justify-content-center"
                                         style="width:52px;height:52px">
                                        <i class="fas fa-image text-muted"></i>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <div class="fw-semibold">${fn:escapeXml(c.name)}</div>
                            <div class="small text-muted">ID #${fn:escapeXml(c.id)}</div>
                        </td>
                        <td class="text-muted small">${fn:escapeXml(c.description)}</td>
                        <td class="text-center">
                            <c:set var="cnt" value="${productCounts[c.id] != null ? productCounts[c.id] : 0}"/>
                            <span class="badge ${cnt > 0 ? 'bg-primary' : 'bg-secondary'}">${cnt}</span>
                        </td>
                        <td class="text-center">${fn:escapeXml(c.sortOrder)}</td>
                        <td class="text-center">
                            <form action="${pageContext.request.contextPath}/admin/categories/toggle-active" method="post">
                                <input type="hidden" name="id" value="${fn:escapeXml(c.id)}">
                                <input type="hidden" name="returnUrl"
                                       value="/admin/categories?page=${fn:escapeXml(pageData.number)}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}">
                                <button class="badge border-0 px-3 py-2 ${c.active ? 'bg-success' : 'bg-secondary'}"
                                        title="${c.active ? 'Bấm để ẩn' : 'Bấm để hiện'}">
                                    <i class="fas ${c.active ? 'fa-eye' : 'fa-eye-slash'} me-1"></i>${c.active ? 'Đang hiện' : 'Đã ẩn'}
                                </button>
                            </form>
                        </td>
                        <td class="text-end">
                            <div class="d-inline-flex gap-1">
                                <a href="${pageContext.request.contextPath}/admin/categories/edit/${fn:escapeXml(c.id)}"
                                   class="btn btn-sm btn-outline-primary" title="Sửa danh mục">
                                    <i class="fas fa-pen"></i>
                                </a>
                                <form action="${pageContext.request.contextPath}/admin/categories/delete" method="post"
                                      onsubmit="return confirm('Xóa danh mục này?')">
                                    <input type="hidden" name="id" value="${fn:escapeXml(c.id)}">
                                    <button class="btn btn-sm btn-outline-danger" title="Xóa danh mục"
                                            <c:if test="${productCounts[c.id] != null && productCounts[c.id] > 0}">disabled</c:if>>
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty pageData.content}">
                    <tr>
                        <td colspan="7" class="text-center text-muted py-5">
                            <i class="fas fa-folder-open fa-2x d-block mb-2 opacity-50"></i>
                            Không có danh mục nào khớp điều kiện
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
            <c:forEach begin="0" end="${pageData.totalPages - 1}" var="i">
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