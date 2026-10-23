<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<title>${category.id != null ? 'Sửa' : 'Thêm'} danh mục</title>

<c:if test="${error != null}"><div class="alert alert-danger">${error}</div></c:if>

<form action="${pageContext.request.contextPath}/admin/categories/save"
      method="post" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${category.id}">
    <div class="mb-3"><label>Tên</label>
        <input type="text" name="name" value="${category.name}" class="form-control" required></div>
    <div class="mb-3"><label>Mô tả</label>
        <textarea name="description" class="form-control">${category.description}</textarea></div>
    <div class="mb-3"><label>Ảnh</label>
        <input type="file" name="imageFile" id="categoryImage"
               class="form-control" accept="image/jpeg,image/png,image/gif,image/webp">
        <small class="text-muted d-block">JPG, PNG, GIF, WEBP. Tối đa 5 MB.</small>
        <small id="categoryImageError" class="text-danger d-none"></small>
    </div>
    <div class="form-check mb-3">
        <input type="checkbox" name="active" value="true" class="form-check-input"
            <c:if test="${category.active}">checked</c:if>>
        <label class="form-check-label">Active</label>
    </div>
    <button class="btn btn-warning">Lưu</button>
</form>

<script>
(function () {
    const input = document.getElementById('categoryImage');
    const errorEl = document.getElementById('categoryImageError');
    if (!input) return;
    const MAX = 5 * 1024 * 1024;
    const ALLOWED = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    function showError(msg) {
        errorEl.textContent = msg;
        errorEl.classList.remove('d-none');
        input.value = '';
    }
    input.addEventListener('change', function () {
        errorEl.textContent = '';
        errorEl.classList.add('d-none');
        if (!input.files || input.files.length === 0) return;
        const f = input.files[0];
        if (f.size > MAX) {
            showError('Ảnh không được vượt quá 5 MB.');
            return;
        }
        if (ALLOWED.indexOf(f.type.toLowerCase()) === -1) {
            showError('Chỉ chấp nhận ảnh JPG, PNG, GIF hoặc WEBP.');
        }
    });
})();
</script>