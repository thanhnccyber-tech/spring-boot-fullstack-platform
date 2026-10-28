<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Quản lý sản phẩm</title>

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

<%-- ===================== TOOLBAR ===================== --%>
<div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
    <div class="text-muted">
        <i class="fas fa-box me-1"></i>
        Tổng cộng <b class="text-dark">${fn:escapeXml(pageData.totalElements)}</b> sản phẩm
    </div>
    <c:if test="${isAdmin}">
        <a href="${pageContext.request.contextPath}/admin/products/new" class="btn btn-warning">
            <i class="fas fa-plus me-1"></i> Thêm sản phẩm
        </a>
    </c:if>
</div>

<%-- ===================== BỘ LỌC ===================== --%>
<div class="card mb-3">
    <div class="card-body py-3">
        <form method="get" class="row g-2 align-items-end">
            <div class="col-md-3">
                <label class="form-label small fw-semibold text-muted mb-1">Tìm kiếm</label>
                <input type="text" name="kw" value="${fn:escapeXml(kw)}" class="form-control"
                       placeholder="Nhập tên sản phẩm...">
            </div>
            <div class="col-md-2">
                <label class="form-label small fw-semibold text-muted mb-1">Danh mục</label>
                <select name="categoryId" class="form-select">
                    <option value="">Tất cả danh mục</option>
                    <c:forEach items="${categories}" var="c">
                        <option value="${fn:escapeXml(c.id)}"
                            <c:if test="${categoryId == c.id}">selected</c:if>>${fn:escapeXml(c.name)}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-2">
                <label class="form-label small fw-semibold text-muted mb-1">Trạng thái</label>
                <select name="active" class="form-select">
                    <option value="">Tất cả</option>
                    <option value="true"  <c:if test="${activeFilter == 'true'}">selected</c:if>>Đang hiển thị</option>
                    <option value="false" <c:if test="${activeFilter == 'false'}">selected</c:if>>Đã ẩn</option>
                </select>
            </div>
            <div class="col-md-1">
                <label class="form-label small fw-semibold text-muted mb-1">Giá từ</label>
                <input type="number" name="minPrice" value="${fn:escapeXml(minPrice)}"
                       class="form-control" placeholder="0" min="0" step="1000">
            </div>
            <div class="col-md-1">
                <label class="form-label small fw-semibold text-muted mb-1">Đến</label>
                <input type="number" name="maxPrice" value="${fn:escapeXml(maxPrice)}"
                       class="form-control" placeholder="Max" min="0" step="1000">
            </div>
            <div class="col-md-2">
                <label class="form-label small fw-semibold text-muted mb-1">Sắp xếp</label>
                <select name="sort" class="form-select">
                    <option value="newest"    <c:if test="${sort == 'newest'}">selected</c:if>>Mới nhất</option>
                    <option value="priceAsc"  <c:if test="${sort == 'priceAsc'}">selected</c:if>>Giá tăng dần</option>
                    <option value="priceDesc" <c:if test="${sort == 'priceDesc'}">selected</c:if>>Giá giảm dần</option>
                    <option value="nameAsc"   <c:if test="${sort == 'nameAsc'}">selected</c:if>>Tên A → Z</option>
                    <option value="nameDesc"  <c:if test="${sort == 'nameDesc'}">selected</c:if>>Tên Z → A</option>
                </select>
            </div>
            <div class="col-md-1">
                <label class="form-label small fw-semibold text-muted mb-1">&nbsp;</label>
                <button class="btn btn-warning w-100" title="Lọc"><i class="fas fa-search"></i></button>
            </div>
        </form>
        <c:if test="${not empty kw or not empty categoryId or not empty activeFilter or not empty minPrice or not empty maxPrice}">
            <a href="${pageContext.request.contextPath}/admin/products"
               class="small text-decoration-none d-inline-block mt-2">
                <i class="fas fa-times"></i> Xóa bộ lọc
            </a>
        </c:if>
    </div>
</div>

<%-- ===================== BẢNG ===================== --%>
<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-dark">
                <tr>
                    <th style="width:80px">Ảnh</th>
                    <th>Sản phẩm</th>
                    <th style="width:150px">Danh mục</th>
                    <th class="text-end" style="width:140px">Giá bán</th>
                    <th class="text-center" style="width:130px">Hiển thị</th>
                    <th class="text-center" style="width:130px">Tồn kho</th>
                    <th class="text-end" style="width:120px">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${pageData.content}" var="p">
                    <tr>
                        <td>
                            <c:choose>
                                <c:when test="${not empty p.imageUrl}">
                                    <img src="${fn:escapeXml(p.imageUrl)}" alt="${fn:escapeXml(p.name)}"
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
                            <div class="fw-semibold">${fn:escapeXml(p.name)}</div>
                            <div class="small text-muted">ID #${fn:escapeXml(p.id)}</div>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${p.category != null}">
                                    <span class="badge bg-light text-dark border">${fn:escapeXml(p.category.name)}</span>
                                </c:when>
                                <c:otherwise><span class="text-muted small">—</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-end fw-semibold text-success">
                            <fmt:formatNumber value="${p.basePrice}" pattern="#,##0"/> ₫
                        </td>
                        <td class="text-center">
                            <c:choose>
                                <c:when test="${isAdmin}">
                                    <form action="${pageContext.request.contextPath}/admin/products/toggle-active" method="post">
                                        <input type="hidden" name="id" value="${fn:escapeXml(p.id)}">
                                        <input type="hidden" name="returnUrl"
                                               value="/admin/products?page=${fn:escapeXml(pageData.number)}&kw=${fn:escapeXml(kw)}&categoryId=${fn:escapeXml(categoryId)}&active=${fn:escapeXml(activeFilter)}&minPrice=${fn:escapeXml(minPrice)}&maxPrice=${fn:escapeXml(maxPrice)}&sort=${fn:escapeXml(sort)}">
                                        <button class="badge border-0 px-3 py-2 ${fn:escapeXml(p.active ? 'bg-success' : 'bg-secondary')}"
                                                title="${fn:escapeXml(p.active ? 'Bấm để ẩn sản phẩm' : 'Bấm để hiện sản phẩm')}">
                                            <i class="fas ${fn:escapeXml(p.active ? 'fa-eye' : 'fa-eye-slash')} me-1"></i>${fn:escapeXml(p.active ? 'Đang hiện' : 'Đã ẩn')}
                                        </button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge px-3 py-2 ${fn:escapeXml(p.active ? 'bg-success' : 'bg-secondary')}">
                                        <i class="fas ${fn:escapeXml(p.active ? 'fa-eye' : 'fa-eye-slash')} me-1"></i>${fn:escapeXml(p.active ? 'Đang hiện' : 'Đã ẩn')}
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-center">
                            <form action="${pageContext.request.contextPath}/admin/products/toggle-stock" method="post">
                                <input type="hidden" name="id" value="${fn:escapeXml(p.id)}">
                                <input type="hidden" name="returnUrl"
                                       value="/admin/products?page=${fn:escapeXml(pageData.number)}&kw=${fn:escapeXml(kw)}&categoryId=${fn:escapeXml(categoryId)}&active=${fn:escapeXml(activeFilter)}&minPrice=${fn:escapeXml(minPrice)}&maxPrice=${fn:escapeXml(maxPrice)}&sort=${fn:escapeXml(sort)}">
                                <button class="badge border-0 px-3 py-2 ${fn:escapeXml(p.outOfStock ? 'bg-danger' : 'bg-success')}"
                                        title="${fn:escapeXml(p.outOfStock ? 'Bấm để đánh dấu còn hàng' : 'Bấm để đánh dấu hết hàng')}">
                                    <i class="fas ${fn:escapeXml(p.outOfStock ? 'fa-times-circle' : 'fa-check-circle')} me-1"></i>${fn:escapeXml(p.outOfStock ? 'Hết hàng' : 'Còn hàng')}
                                </button>
                            </form>
                        </td>
                        <td class="text-end">
                            <c:if test="${isAdmin}">
                                <div class="d-inline-flex gap-1">
                                    <a href="${pageContext.request.contextPath}/admin/products/edit/${fn:escapeXml(p.id)}"
                                       class="btn btn-sm btn-outline-primary" title="Sửa sản phẩm">
                                        <i class="fas fa-pen"></i>
                                    </a>
                                    <form action="${pageContext.request.contextPath}/admin/products/delete"
                                          method="post" onsubmit="return confirm('Xóa sản phẩm này?')">
                                        <input type="hidden" name="id" value="${fn:escapeXml(p.id)}">
                                        <button class="btn btn-sm btn-outline-danger" title="Xóa sản phẩm">
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
                        <td colspan="7" class="text-center text-muted py-5">
                            <i class="fas fa-box-open fa-2x d-block mb-2 opacity-50"></i>
                            Không có sản phẩm nào khớp điều kiện
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<%-- ===================== PHÂN TRANG ===================== --%>
<c:if test="${pageData.totalPages > 1}">
    <nav class="mt-3">
        <ul class="pagination justify-content-center mb-0">
            <li class="page-item ${fn:escapeXml(pageData.first ? 'disabled' : '')}">
                <a class="page-link"
                   href="?page=${fn:escapeXml(pageData.number - 1)}&kw=${fn:escapeXml(kw)}&categoryId=${fn:escapeXml(categoryId)}&active=${fn:escapeXml(activeFilter)}&minPrice=${fn:escapeXml(minPrice)}&maxPrice=${fn:escapeXml(maxPrice)}&sort=${fn:escapeXml(sort)}">
                    <i class="fas fa-chevron-left"></i>
                </a>
            </li>

            <c:set var="startPage" value="${pageData.number - 2}" />
            <c:if test="${startPage < 0}"><c:set var="startPage" value="0" /></c:if>
            <c:set var="endPage" value="${startPage + 4}" />
            <c:if test="${endPage >= pageData.totalPages}">
                <c:set var="endPage" value="${pageData.totalPages - 1}" />
                <c:set var="startPage" value="${endPage - 4}" />
                <c:if test="${startPage < 0}"><c:set var="startPage" value="0" /></c:if>
            </c:if>

            <c:forEach begin="${startPage}" end="${endPage}" var="i">
                <li class="page-item ${fn:escapeXml(i == pageData.number ? 'active' : '')}">
                    <a class="page-link"
                       href="?page=${fn:escapeXml(i)}&kw=${fn:escapeXml(kw)}&categoryId=${fn:escapeXml(categoryId)}&active=${fn:escapeXml(activeFilter)}&minPrice=${fn:escapeXml(minPrice)}&maxPrice=${fn:escapeXml(maxPrice)}&sort=${fn:escapeXml(sort)}">${fn:escapeXml(i + 1)}</a>
                </li>
            </c:forEach>

            <li class="page-item ${fn:escapeXml(pageData.last ? 'disabled' : '')}">
                <a class="page-link"
                   href="?page=${fn:escapeXml(pageData.number + 1)}&kw=${fn:escapeXml(kw)}&categoryId=${fn:escapeXml(categoryId)}&active=${fn:escapeXml(activeFilter)}&minPrice=${fn:escapeXml(minPrice)}&maxPrice=${fn:escapeXml(maxPrice)}&sort=${fn:escapeXml(sort)}">
                    <i class="fas fa-chevron-right"></i>
                </a>
            </li>
        </ul>
    </nav>
</c:if>