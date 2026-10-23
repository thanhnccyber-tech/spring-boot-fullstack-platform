<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<title>${isNew ? 'Thêm' : 'Sửa'} danh mục</title>

<c:if test="${error != null}"><div class="alert alert-danger">${error}</div></c:if>

<form action="${pageContext.request.contextPath}/admin/categories/save"
      method="post" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${category.id}">

    <div class="row">
        <div class="col-md-8">
            <div class="mb-3">
                <label>Tên danh mục <span class="text-danger">*</span></label>
                <input type="text" name="name" value="${category.name}"
                       class="form-control" maxlength="100" required>
                <small class="text-muted">Tối đa 100 ký tự.</small>
            </div>
            <div class="mb-3">
                <label>Mô tả</label>
                <textarea name="description" class="form-control" rows="3"
                          maxlength="2000">${category.description}</textarea>
            </div>
        </div>
        <div class="col-md-4">
            <div class="mb-3">
                <label>Thứ tự hiển thị</label>
                <input type="number" name="sortOrder" value="${category.sortOrder}"
                       class="form-control" min="0">
                <small class="text-muted">Số nhỏ hiển thị trước.</small>
            </div>

            <div class="mb-3">
                <label>Ảnh đại diện</label>
                <input type="file" name="imageFile" id="categoryImage"
                       class="form-control" accept="image/jpeg,image/png,image/gif,image/webp">
                <small class="text-muted d-block">JPG, PNG, GIF, WEBP. Tối đa 5 MB.</small>
                <small id="categoryImageError" class="text-danger d-none"></small>
                <c:if test="${not empty category.imageUrl}">
                    <img src="${category.imageUrl}" class="img-fluid mt-2 rounded">
                </c:if>
            </div>

            <div class="form-check mb-3">
                <input type="checkbox" name="active" value="true"
                       id="activeCheckbox" class="form-check-input"
                       <c:if test="${category.active}">checked</c:if>>
                <label class="form-check-label" for="activeCheckbox">Đang hiển thị</label>
            </div>
        </div>
    </div>

    <button class="btn btn-warning">${isNew ? 'Tạo mới' : 'Lưu'}</button>
    <a href="${pageContext.request.contextPath}/admin/categories"
       class="btn btn-secondary">Hủy</a>
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