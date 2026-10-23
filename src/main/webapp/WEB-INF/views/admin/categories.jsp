<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<title>Quản lý danh mục</title>

<c:if test="${success != null}"><div class="alert alert-success">${success}</div></c:if>
<c:if test="${error != null}"><div class="alert alert-danger">${error}</div></c:if>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">📁 Quản lý danh mục</h5>
    <a href="${pageContext.request.contextPath}/admin/categories/new"
       class="btn btn-warning">
        <i class="fas fa-plus"></i> Thêm danh mục
    </a>
</div>

<form class="row g-2 mb-3 bg-white p-3 rounded shadow-sm" method="get">
    <div class="col-md-5">
        <input type="text" name="kw" value="${kw}" class="form-control"
               placeholder="Tìm theo tên hoặc mô tả...">
    </div>
    <div class="col-md-3">
        <select name="active" class="form-select">
            <option value="">-- Trạng thái --</option>
            <option value="true"  <c:if test="${activeFilter == 'true'}">selected</c:if>>Đang hiển thị</option>
            <option value="false" <c:if test="${activeFilter == 'false'}">selected</c:if>>Đã ẩn</option>
        </select>
    </div>
    <div class="col-md-2">
        <button class="btn btn-warning w-100"><i class="fas fa-search"></i> Tìm</button>
    </div>
    <div class="col-md-2">
        <a href="${pageContext.request.contextPath}/admin/categories"
           class="btn btn-outline-secondary w-100">
            <i class="fas fa-redo"></i> Xóa lọc
        </a>
    </div>
</form>

<table class="table table-bordered bg-white align-middle">
    <thead class="table-dark">
        <tr>
            <th style="width:60px">ID</th>
            <th style="width:80px">Ảnh</th>
            <th>Tên</th>
            <th>Mô tả</th>
            <th class="text-center" style="width:100px">Sản phẩm</th>
            <th class="text-center" style="width:80px">Thứ tự</th>
            <th class="text-center" style="width:120px">Trạng thái</th>
            <th style="width:160px">Hành động</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${pageData.content}" var="c">
            <tr>
                <td>${c.id}</td>
                <td>
                    <c:choose>
                        <c:when test="${not empty c.imageUrl}">
                            <img src="${c.imageUrl}" width="60"
                                 style="height:60px;object-fit:cover" class="rounded">
                        </c:when>
                        <c:otherwise>
                            <span class="text-muted small">—</span>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td><b>${c.name}</b></td>
                <td class="text-muted small">${c.description}</td>
                <td class="text-center">
                    <c:set var="cnt" value="${productCounts[c.id] != null ? productCounts[c.id] : 0}"/>
                    <span class="badge ${cnt > 0 ? 'bg-primary' : 'bg-secondary'}">${cnt}</span>
                </td>
                <td class="text-center">${c.sortOrder}</td>
                <td class="text-center">
                    <form action="${pageContext.request.contextPath}/admin/categories/toggle-active"
                          method="post" style="display:inline">
                        <input type="hidden" name="id" value="${c.id}">
                        <input type="hidden" name="returnUrl"
                               value="/admin/categories?page=${pageData.number}&kw=${kw}&active=${activeFilter}">
                        <button class="badge ${c.active ? 'bg-success' : 'bg-secondary'} border-0"
                                title="${c.active ? 'Bấm để ẩn' : 'Bấm để hiện'}">
                            <i class="fas ${c.active ? 'fa-eye' : 'fa-eye-slash'}"></i>
                            ${c.active ? 'Đang hiện' : 'Đã ẩn'}
                        </button>
                    </form>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/categories/edit/${c.id}"
                       class="btn btn-sm btn-primary">Sửa</a>
                    <form action="${pageContext.request.contextPath}/admin/categories/delete"
                          method="post" style="display:inline"
                          onsubmit="return confirm('Xóa danh mục này?')">
                        <input type="hidden" name="id" value="${c.id}">
                        <button class="btn btn-sm btn-danger"
                                <c:if test="${productCounts[c.id] != null && productCounts[c.id] > 0}">disabled
                                    title="Không thể xóa: còn sản phẩm"</c:if>>
                            Xóa
                        </button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty pageData.content}">
            <tr><td colspan="8" class="text-center text-muted py-4">
                Không có danh mục nào khớp điều kiện
            </td></tr>
        </c:if>
    </tbody>
</table>

<c:if test="${pageData.totalPages > 1}">
    <nav>
        <ul class="pagination justify-content-center">
            <li class="page-item ${pageData.first ? 'disabled' : ''}">
                <a class="page-link" href="?page=${pageData.number - 1}&kw=${kw}&active=${activeFilter}">«</a>
            </li>
            <c:forEach begin="0" end="${pageData.totalPages - 1}" var="i">
                <li class="page-item ${i == pageData.number ? 'active' : ''}">
                    <a class="page-link" href="?page=${i}&kw=${kw}&active=${activeFilter}">${i + 1}</a>
                </li>
            </c:forEach>
            <li class="page-item ${pageData.last ? 'disabled' : ''}">
                <a class="page-link" href="?page=${pageData.number + 1}&kw=${kw}&active=${activeFilter}">»</a>
            </li>
        </ul>
    </nav>
</c:if>