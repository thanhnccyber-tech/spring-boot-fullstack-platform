<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Quản lý Topping</title>

<c:if test="${success != null}"><div class="alert alert-success">${fn:escapeXml(success)}</div></c:if>
<c:if test="${error != null}"><div class="alert alert-danger">${fn:escapeXml(error)}</div></c:if>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">🍦 Quản lý Topping</h5>
    <a href="${pageContext.request.contextPath}/admin/toppings/new"
       class="btn btn-warning">
        <i class="fas fa-plus"></i> Thêm topping
    </a>
</div>

<form class="row g-2 mb-3 bg-white p-3 rounded shadow-sm" method="get">
    <div class="col-md-4">
        <input type="text" name="kw" value="${fn:escapeXml(kw)}" class="form-control"
               placeholder="Tìm theo tên topping...">
    </div>
    <div class="col-md-2">
        <select name="active" class="form-select">
            <option value="">-- Hiển thị --</option>
            <option value="true"  <c:if test="${activeFilter == 'true'}">selected</c:if>>Đang hiện</option>
            <option value="false" <c:if test="${activeFilter == 'false'}">selected</c:if>>Đã ẩn</option>
        </select>
    </div>
    <div class="col-md-2">
        <select name="outOfStock" class="form-select">
            <option value="">-- Tồn kho --</option>
            <option value="true"  <c:if test="${outOfStockFilter == 'true'}">selected</c:if>>Hết hàng</option>
            <option value="false" <c:if test="${outOfStockFilter == 'false'}">selected</c:if>>Còn hàng</option>
        </select>
    </div>
    <div class="col-md-2">
        <button class="btn btn-warning w-100"><i class="fas fa-search"></i> Tìm</button>
    </div>
    <div class="col-md-2">
        <a href="${pageContext.request.contextPath}/admin/toppings"
           class="btn btn-outline-secondary w-100">
            <i class="fas fa-redo"></i> Xóa lọc
        </a>
    </div>
</form>

<table class="table table-bordered bg-white align-middle">
    <thead class="table-dark">
        <tr>
            <th style="width:60px">ID</th>
            <th>Tên</th>
            <th class="text-end" style="width:150px">Giá</th>
            <th class="text-center" style="width:140px">Hiển thị</th>
            <th class="text-center" style="width:140px">Tồn kho</th>
            <th style="width:160px">Hành động</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${pageData.content}" var="t">
            <tr>
                <td>${fn:escapeXml(t.id)}</td>
                <td><b>${fn:escapeXml(t.name)}</b></td>
                <td class="text-end">
                    <fmt:formatNumber value="${t.price}" pattern="#,##0"/> ₫
                </td>
                <td class="text-center">
                    <form action="${pageContext.request.contextPath}/admin/toppings/toggle-active"
                          method="post" style="display:inline">
                        <input type="hidden" name="id" value="${fn:escapeXml(t.id)}">
                        <input type="hidden" name="returnUrl"
                               value="/admin/toppings?page=${fn:escapeXml(pageData.number)}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}&outOfStock=${fn:escapeXml(outOfStockFilter)}">
                        <button class="badge ${fn:escapeXml(t.active ? 'bg-success' : 'bg-secondary')} border-0"
                                title="${fn:escapeXml(t.active ? 'Bấm để ẩn' : 'Bấm để hiện')}">
                            <i class="fas ${fn:escapeXml(t.active ? 'fa-eye' : 'fa-eye-slash')}"></i>
                            ${fn:escapeXml(t.active ? 'Đang hiện' : 'Đã ẩn')}
                        </button>
                    </form>
                </td>
                <td class="text-center">
                    <form action="${pageContext.request.contextPath}/admin/toppings/toggle-stock"
                          method="post" style="display:inline">
                        <input type="hidden" name="id" value="${fn:escapeXml(t.id)}">
                        <input type="hidden" name="returnUrl"
                               value="/admin/toppings?page=${fn:escapeXml(pageData.number)}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}&outOfStock=${fn:escapeXml(outOfStockFilter)}">
                        <button class="badge ${fn:escapeXml(t.outOfStock ? 'bg-danger' : 'bg-success')} border-0"
                                title="${fn:escapeXml(t.outOfStock ? 'Bấm để đánh dấu còn hàng' : 'Bấm để đánh dấu hết hàng')}">
                            <i class="fas ${fn:escapeXml(t.outOfStock ? 'fa-times-circle' : 'fa-check-circle')}"></i>
                            ${fn:escapeXml(t.outOfStock ? 'Hết hàng' : 'Còn hàng')}
                        </button>
                    </form>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/toppings/edit/${fn:escapeXml(t.id)}"
                       class="btn btn-sm btn-primary">Sửa</a>
                    <form action="${pageContext.request.contextPath}/admin/toppings/delete"
                          method="post" style="display:inline"
                          onsubmit="return confirm('Xóa topping này?')">
                        <input type="hidden" name="id" value="${fn:escapeXml(t.id)}">
                        <button class="btn btn-sm btn-danger">Xóa</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty pageData.content}">
            <tr><td colspan="6" class="text-center text-muted py-4">
                Không có topping nào khớp điều kiện
            </td></tr>
        </c:if>
    </tbody>
</table>

<c:if test="${pageData.totalPages > 1}">
    <nav>
        <ul class="pagination justify-content-center">
            <li class="page-item ${fn:escapeXml(pageData.first ? 'disabled' : '')}">
                <a class="page-link"
                   href="?page=${fn:escapeXml(pageData.number - 1)}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}&outOfStock=${fn:escapeXml(outOfStockFilter)}">«</a>
            </li>
            <c:forEach begin="0" end="${pageData.totalPages - 1}" var="i">
                <li class="page-item ${fn:escapeXml(i == pageData.number ? 'active' : '')}">
                    <a class="page-link"
                       href="?page=${fn:escapeXml(i)}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}&outOfStock=${fn:escapeXml(outOfStockFilter)}">${fn:escapeXml(i + 1)}</a>
                </li>
            </c:forEach>
            <li class="page-item ${fn:escapeXml(pageData.last ? 'disabled' : '')}">
                <a class="page-link"
                   href="?page=${fn:escapeXml(pageData.number + 1)}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}&outOfStock=${fn:escapeXml(outOfStockFilter)}">»</a>
            </li>
        </ul>
    </nav>
</c:if>