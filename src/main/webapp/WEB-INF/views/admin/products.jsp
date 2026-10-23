<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<title>Quản lý sản phẩm</title>

<c:if test="${success != null}"><div class="alert alert-success">${success}</div></c:if>
<c:if test="${error != null}"><div class="alert alert-danger">${error}</div></c:if>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">☕ Quản lý sản phẩm</h5>
    <a href="${pageContext.request.contextPath}/admin/products/new" class="btn btn-warning">
        <i class="fas fa-plus"></i> Thêm sản phẩm
    </a>
</div>

<%-- ===================== FORM LỌC ===================== --%>
<form class="row g-2 mb-3 bg-white p-3 rounded shadow-sm" method="get">

    <div class="col-md-3">
        <input type="text" name="kw" value="${kw}"
               class="form-control" placeholder="Tìm theo tên...">
    </div>

    <div class="col-md-2">
        <select name="categoryId" class="form-select">
            <option value="">-- Danh mục --</option>
            <c:forEach items="${categories}" var="c">
                <option value="${c.id}"
                    <c:if test="${categoryId == c.id}">selected</c:if>>
                    ${c.name}
                </option>
            </c:forEach>
        </select>
    </div>

    <div class="col-md-2">
        <select name="active" class="form-select">
            <option value="">-- Trạng thái --</option>
            <option value="true"  <c:if test="${activeFilter == 'true'}">selected</c:if>>Đang hiển thị</option>
            <option value="false" <c:if test="${activeFilter == 'false'}">selected</c:if>>Đã ẩn</option>
        </select>
    </div>

    <div class="col-md-2">
        <input type="number" name="minPrice" value="${minPrice}"
               class="form-control" placeholder="Giá từ" min="0" step="1000">
    </div>

    <div class="col-md-2">
        <input type="number" name="maxPrice" value="${maxPrice}"
               class="form-control" placeholder="Giá đến" min="0" step="1000">
    </div>

    <div class="col-md-1">
        <button class="btn btn-warning w-100" title="Lọc">
            <i class="fas fa-search"></i>
        </button>
    </div>

    <div class="col-md-12 d-flex gap-2 mt-1">
        <select name="sort" class="form-select form-select-sm" style="width:200px;">
            <option value="newest"    <c:if test="${sort == 'newest'}">selected</c:if>>Mới nhất</option>
            <option value="priceAsc"  <c:if test="${sort == 'priceAsc'}">selected</c:if>>Giá tăng dần</option>
            <option value="priceDesc" <c:if test="${sort == 'priceDesc'}">selected</c:if>>Giá giảm dần</option>
            <option value="nameAsc"   <c:if test="${sort == 'nameAsc'}">selected</c:if>>Tên A → Z</option>
            <option value="nameDesc"  <c:if test="${sort == 'nameDesc'}">selected</c:if>>Tên Z → A</option>
        </select>

        <a href="${pageContext.request.contextPath}/admin/products"
           class="btn btn-sm btn-outline-secondary">
            <i class="fas fa-redo"></i> Xóa lọc
        </a>

        <span class="ms-auto align-self-center text-muted small">
            Tổng: <b>${pageData.totalElements}</b> sản phẩm
        </span>
    </div>
</form>

<%-- ===================== BẢNG ===================== --%>
<table class="table table-bordered bg-white align-middle">
    <thead class="table-dark">
        <tr>
            <th>ID</th>
            <th>Ảnh</th>
            <th>Tên</th>
            <th>Danh mục</th>
            <th>Giá</th>
            <th>Hiển thị</th>
            <th>Tồn kho</th>
            <th>Hành động</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${pageData.content}" var="p">
            <tr>
                <td>${p.id}</td>
                <td>
                    <img src="${p.imageUrl}" width="60"
                         style="object-fit:cover;height:60px" alt="${p.name}">
                </td>
                <td>${p.name}</td>
                <td>${p.category != null ? p.category.name : '-'}</td>
                <td><fmt:formatNumber value="${p.basePrice}" pattern="#,##0"/> ₫</td>

                <%-- Cột Hiển thị: nút POST toggle-active --%>
                <td>
                    <form action="${pageContext.request.contextPath}/admin/products/toggle-active"
                          method="post" style="display:inline">
                        <input type="hidden" name="id" value="${p.id}">
                        <input type="hidden" name="returnUrl"
                               value="/admin/products?page=${pageData.number}&kw=${kw}&categoryId=${categoryId}&active=${activeFilter}&minPrice=${minPrice}&maxPrice=${maxPrice}&sort=${sort}">
                        <button class="badge ${p.active ? 'bg-success' : 'bg-secondary'} border-0"
                                title="${p.active ? 'Bấm để ẩn sản phẩm' : 'Bấm để hiện sản phẩm'}">
                            <i class="fas ${p.active ? 'fa-eye' : 'fa-eye-slash'}"></i>
                            ${p.active ? 'Đang hiện' : 'Đã ẩn'}
                        </button>
                    </form>
                </td>

                <%-- Cột Tồn kho: nút POST toggle-stock --%>
                <td>
                    <form action="${pageContext.request.contextPath}/admin/products/toggle-stock"
                          method="post" style="display:inline">
                        <input type="hidden" name="id" value="${p.id}">
                        <input type="hidden" name="returnUrl"
                               value="/admin/products?page=${pageData.number}&kw=${kw}&categoryId=${categoryId}&active=${activeFilter}&minPrice=${minPrice}&maxPrice=${maxPrice}&sort=${sort}">
                        <button class="badge ${p.outOfStock ? 'bg-danger' : 'bg-success'} border-0"
                                title="${p.outOfStock ? 'Bấm để đánh dấu còn hàng' : 'Bấm để đánh dấu hết hàng'}">
                            <i class="fas ${p.outOfStock ? 'fa-times-circle' : 'fa-check-circle'}"></i>
                            ${p.outOfStock ? 'Hết hàng' : 'Còn hàng'}
                        </button>
                    </form>
                </td>

                <td>
                    <a href="${pageContext.request.contextPath}/admin/products/edit/${p.id}"
                       class="btn btn-sm btn-primary">Sửa</a>
                    <form action="${pageContext.request.contextPath}/admin/products/delete"
                          method="post" style="display:inline"
                          onsubmit="return confirm('Xóa sản phẩm này?')">
                        <input type="hidden" name="id" value="${p.id}">
                        <button class="btn btn-sm btn-danger">Xóa</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty pageData.content}">
            <tr>
                <td colspan="8" class="text-center text-muted">
                    Không có sản phẩm nào khớp điều kiện
                </td>
            </tr>
        </c:if>
    </tbody>
</table>

<%-- ===================== PHÂN TRANG ===================== --%>
<c:if test="${pageData.totalPages > 1}">
    <nav>
        <ul class="pagination justify-content-center">
            <li class="page-item ${pageData.first ? 'disabled' : ''}">
                <a class="page-link"
                   href="?page=${pageData.number - 1}&kw=${kw}&categoryId=${categoryId}&active=${activeFilter}&minPrice=${minPrice}&maxPrice=${maxPrice}&sort=${sort}">«</a>
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
                <li class="page-item ${i == pageData.number ? 'active' : ''}">
                    <a class="page-link"
                       href="?page=${i}&kw=${kw}&categoryId=${categoryId}&active=${activeFilter}&minPrice=${minPrice}&maxPrice=${maxPrice}&sort=${sort}">${i + 1}</a>
                </li>
            </c:forEach>

            <li class="page-item ${pageData.last ? 'disabled' : ''}">
                <a class="page-link"
                   href="?page=${pageData.number + 1}&kw=${kw}&categoryId=${categoryId}&active=${activeFilter}&minPrice=${minPrice}&maxPrice=${maxPrice}&sort=${sort}">»</a>
            </li>
        </ul>
    </nav>
</c:if>