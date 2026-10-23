<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<title>${size.id != null ? 'Sửa' : 'Thêm'} Size</title>

<c:if test="${error != null}"><div class="alert alert-danger">${error}</div></c:if>

<form action="${pageContext.request.contextPath}/admin/sizes/save" method="post">
    <input type="hidden" name="id" value="${size.id}">
    <div class="mb-3"><label>Tên *</label>
        <input type="text" name="name" value="${size.name}" class="form-control" required maxlength="20"></div>
    <div class="mb-3"><label>Phụ thu (VNĐ) *</label>
        <input type="number" name="extraPrice" value="${size.extraPrice}" class="form-control" required min="0" step="1000"></div>
    <div class="mb-3"><label>Thứ tự</label>
        <input type="number" name="sortOrder" value="${size.sortOrder}" class="form-control" min="0"></div>
    <div class="form-check mb-3">
        <input type="checkbox" name="active" value="true" class="form-check-input"
            <c:if test="${size.active}">checked</c:if>>
        <label class="form-check-label">Đang dùng</label>
    </div>
    <button class="btn btn-warning">Lưu</button>
    <a href="${pageContext.request.contextPath}/admin/sizes" class="btn btn-secondary">Hủy</a>
</form>