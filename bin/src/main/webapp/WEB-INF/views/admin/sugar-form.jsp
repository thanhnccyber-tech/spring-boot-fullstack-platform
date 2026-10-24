<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<title>${sugar.id != null ? 'Sửa' : 'Thêm'} Mức Đường</title>

<c:if test="${error != null}"><div class="alert alert-danger">${error}</div></c:if>

<form action="${pageContext.request.contextPath}/admin/sugars/save" method="post">
    <input type="hidden" name="id" value="${sugar.id}">
    <div class="mb-3"><label>Tên *</label>
        <input type="text" name="name" value="${sugar.name}" class="form-control" required maxlength="30"></div>
    <div class="mb-3"><label>Phần trăm (%) *</label>
        <input type="number" name="percent" value="${sugar.percent}" class="form-control" required min="0" max="200"></div>
    <div class="mb-3"><label>Thứ tự</label>
        <input type="number" name="sortOrder" value="${sugar.sortOrder}" class="form-control" min="0"></div>
    <div class="form-check mb-3">
        <input type="checkbox" name="active" value="true" class="form-check-input"
            <c:if test="${sugar.active}">checked</c:if>>
        <label class="form-check-label">Đang dùng</label>
    </div>
    <button class="btn btn-warning">Lưu</button>
    <a href="${pageContext.request.contextPath}/admin/sugars" class="btn btn-secondary">Hủy</a>
</form>