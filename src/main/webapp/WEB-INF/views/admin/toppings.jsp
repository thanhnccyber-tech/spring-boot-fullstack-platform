<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Quản lý Topping</title>

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
        <i class="fas fa-ice-cream me-1"></i>
        Tổng cộng <b class="text-dark">${fn:escapeXml(pageData.totalElements)}</b> topping
    </div>
    <c:if test="${isAdmin}">
        <a href="${pageContext.request.contextPath}/admin/toppings/new" class="btn btn-warning">
            <i class="fas fa-plus me-1"></i> Thêm topping
        </a>
    </c:if>
</div>

<div class="card mb-3">
    <div class="card-body py-3">
        <form method="get" class="row g-2 align-items-end">
            <div class="col-md-4">
                <label class="form-label small fw-semibold text-muted mb-1">Tìm kiếm</label>
                <input type="text" name="kw" value="${fn:escapeXml(kw)}" class="form-control"
                       placeholder="Tìm theo tên topping...">
            </div>
            <div class="col-md-3">
                <label class="form-label small fw-semibold text-muted mb-1">Hiển thị</label>
                <select name="active" class="form-select">
                    <option value="">Tất cả</option>
                    <option value="true"  <c:if test="${activeFilter == 'true'}">selected</c:if>>Đang hiện</option>
                    <option value="false" <c:if test="${activeFilter == 'false'}">selected</c:if>>Đã ẩn</option>
                </select>
            </div>
            <div class="col-md-3">
                <label class="form-label small fw-semibold text-muted mb-1">Tồn kho</label>
                <select name="outOfStock" class="form-select">
                    <option value="">Tất cả</option>
                    <option value="true"  <c:if test="${outOfStockFilter == 'true'}">selected</c:if>>Hết hàng</option>
                    <option value="false" <c:if test="${outOfStockFilter == 'false'}">selected</c:if>>Còn hàng</option>
                </select>
            </div>
            <div class="col-md-2">
                <button class="btn btn-warning w-100"><i class="fas fa-search me-1"></i> Tìm</button>
            </div>
        </form>
        <c:if test="${not empty kw or not empty activeFilter or not empty outOfStockFilter}">
            <a href="${pageContext.request.contextPath}/admin/toppings" class="small text-decoration-none d-inline-block mt-2">
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
                    <th>Tên topping</th>
                    <th class="text-end" style="width:150px">Giá</th>
                    <th class="text-center" style="width:140px">Hiển thị</th>
                    <th class="text-center" style="width:140px">Tồn kho</th>
                    <th class="text-end" style="width:140px">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${pageData.content}" var="t">
                    <tr>
                        <td class="text-muted small">#${fn:escapeXml(t.id)}</td>
                        <td class="fw-semibold">${fn:escapeXml(t.name)}</td>
                        <td class="text-end fw-semibold text-success">
                            <fmt:formatNumber value="${t.price}" pattern="#,##0"/> ₫
                        </td>
                        <td class="text-center">
                            <c:choose>
                                <c:when test="${isAdmin}">
                                    <form action="${pageContext.request.contextPath}/admin/toppings/toggle-active" method="post">
                                        <input type="hidden" name="id" value="${fn:escapeXml(t.id)}">
                                        <input type="hidden" name="returnUrl"
                                               value="/admin/toppings?page=${fn:escapeXml(pageData.number)}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}&outOfStock=${fn:escapeXml(outOfStockFilter)}">
                                        <button class="badge border-0 px-3 py-2 ${t.active ? 'bg-success' : 'bg-secondary'}"
                                                title="${t.active ? 'Bấm để ẩn' : 'Bấm để hiện'}">
                                            <i class="fas ${t.active ? 'fa-eye' : 'fa-eye-slash'} me-1"></i>${t.active ? 'Đang hiện' : 'Đã ẩn'}
                                        </button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge px-3 py-2 ${t.active ? 'bg-success' : 'bg-secondary'}">
                                        <i class="fas ${t.active ? 'fa-eye' : 'fa-eye-slash'} me-1"></i>${t.active ? 'Đang hiện' : 'Đã ẩn'}
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-center">
                            <form action="${pageContext.request.contextPath}/admin/toppings/toggle-stock" method="post">
                                <input type="hidden" name="id" value="${fn:escapeXml(t.id)}">
                                <input type="hidden" name="returnUrl"
                                       value="/admin/toppings?page=${fn:escapeXml(pageData.number)}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}&outOfStock=${fn:escapeXml(outOfStockFilter)}">
                                <button class="badge border-0 px-3 py-2 ${t.outOfStock ? 'bg-danger' : 'bg-success'}"
                                        title="${t.outOfStock ? 'Bấm để đánh dấu còn hàng' : 'Bấm để đánh dấu hết hàng'}">
                                    <i class="fas ${t.outOfStock ? 'fa-times-circle' : 'fa-check-circle'} me-1"></i>${t.outOfStock ? 'Hết hàng' : 'Còn hàng'}
                                </button>
                            </form>
                        </td>
                        <td class="text-end">
                            <c:if test="${isAdmin}">
                                <div class="d-inline-flex gap-1">
                                    <a href="${pageContext.request.contextPath}/admin/toppings/edit/${fn:escapeXml(t.id)}"
                                       class="btn btn-sm btn-outline-primary" title="Sửa topping">
                                        <i class="fas fa-pen"></i>
                                    </a>
                                    <form action="${pageContext.request.contextPath}/admin/toppings/delete" method="post"
                                          onsubmit="return confirm('Xóa topping này?')">
                                        <input type="hidden" name="id" value="${fn:escapeXml(t.id)}">
                                        <button class="btn btn-sm btn-outline-danger" title="Xóa topping">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    </form>
                                </div>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty pageData.content}">
                    <tr>
                        <td colspan="6" class="text-center text-muted py-5">
                            <i class="fas fa-ice-cream fa-2x d-block mb-2 opacity-50"></i>
                            Không có topping nào khớp điều kiện
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
                <a class="page-link" href="?page=${pageData.number - 1}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}&outOfStock=${fn:escapeXml(outOfStockFilter)}">
                    <i class="fas fa-chevron-left"></i>
                </a>
            </li>
            <c:forEach begin="0" end="${pageData.totalPages - 1}" var="i">
                <li class="page-item ${i == pageData.number ? 'active' : ''}">
                    <a class="page-link" href="?page=${i}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}&outOfStock=${fn:escapeXml(outOfStockFilter)}">${i + 1}</a>
                </li>
            </c:forEach>
            <li class="page-item ${pageData.last ? 'disabled' : ''}">
                <a class="page-link" href="?page=${pageData.number + 1}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}&outOfStock=${fn:escapeXml(outOfStockFilter)}">
                    <i class="fas fa-chevron-right"></i>
                </a>
            </li>
        </ul>
    </nav>
</c:if>