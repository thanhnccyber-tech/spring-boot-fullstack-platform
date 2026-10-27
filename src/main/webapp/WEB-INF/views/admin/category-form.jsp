<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>${fn:escapeXml(isNew ? 'Thêm' : 'Sửa')} danh mục</title>

<c:if test="${error != null}">
    <div class="alert alert-danger alert-dismissible fade show">
        ${fn:escapeXml(error)}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<form action="${pageContext.request.contextPath}/admin/categories/save" method="post" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${fn:escapeXml(category.id)}">

    <div class="row g-4">
        <div class="col-lg-8">
            <div class="card mb-4">
                <div class="card-header bg-white">
                    <i class="fas fa-folder text-primary me-1"></i> <b>Thông tin danh mục</b>
                </div>
                <div class="card-body">
                    <div class="row g-3">
                        <div class="col-md-8">
                            <label class="form-label small fw-semibold">Tên danh mục <span class="text-danger">*</span></label>
                            <input type="text" name="name" value="${fn:escapeXml(category.name)}" class="form-control" maxlength="100" required>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label small fw-semibold">Thứ tự hiển thị</label>
                            <input type="number" name="sortOrder" value="${fn:escapeXml(category.sortOrder)}" class="form-control" min="0">
                            <small class="text-muted">Số nhỏ hiển thị trước.</small>
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Mô tả</label>
                            <textarea name="description" class="form-control" rows="3" maxlength="2000">${fn:escapeXml(category.description)}</textarea>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-4">
            <div class="card mb-4 sticky-top" style="top: 80px;">
                <div class="card-header bg-white">
                    <i class="fas fa-image text-primary me-1"></i> <b>Ảnh & Trạng thái</b>
                </div>
                <div class="card-body">
                    <div class="text-center mb-3">
                        <c:choose>
                            <c:when test="${not empty category.imageUrl}">
                                <img src="${fn:escapeXml(category.imageUrl)}" id="catImagePreview" class="img-fluid rounded mb-2" style="max-height: 150px;">
                            </c:when>
                            <c:otherwise>
                                <div id="catImagePlaceholder" class="bg-light rounded d-flex align-items-center justify-content-center mb-2" style="height: 120px;">
                                    <i class="fas fa-cloud-upload-alt fa-3x text-muted opacity-50"></i>
                                </div>
                                <img src="" id="catImagePreview" class="img-fluid rounded mb-2 d-none" style="max-height: 150px;">
                            </c:otherwise>
                        </c:choose>
                        <input type="file" name="imageFile" id="categoryImage" class="form-control form-control-sm" accept="image/jpeg,image/png,image/gif,image/webp">
                        <small class="text-muted d-block mt-1">Tối đa 5 MB.</small>
                        <small id="categoryImageError" class="text-danger d-none d-block mt-1"></small>
                    </div>
                    <hr>
                    <div class="form-check form-switch">
                        <input class="form-check-input" type="checkbox" name="active" value="true" id="cActive"
                               <c:if test="${category.active}">checked</c:if>>
                        <label class="form-check-label" for="cActive">Đang hiển thị</label>
                    </div>
                </div>
                <div class="card-footer bg-white py-3 d-flex gap-2">
                    <button type="submit" class="btn btn-warning flex-grow-1 fw-bold">
                        <i class="fas fa-save me-1"></i> Lưu
                    </button>
                    <a href="${pageContext.request.contextPath}/admin/categories" class="btn btn-outline-secondary">Hủy</a>
                </div>
            </div>
        </div>
    </div>
</form>

<script>
(function () {
    const input = document.getElementById('categoryImage');
    const errorEl = document.getElementById('categoryImageError');
    const preview = document.getElementById('catImagePreview');
    const placeholder = document.getElementById('catImagePlaceholder');
    const MAX = 5 * 1024 * 1024;
    const ALLOWED = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];

    input.addEventListener('change', function () {
        errorEl.classList.add('d-none');
        if (!input.files || input.files.length === 0) return;
        const f = input.files[0];
        if (f.size > MAX) { errorEl.textContent = 'Ảnh vượt quá 5 MB.'; errorEl.classList.remove('d-none'); input.value=''; return; }
        if (ALLOWED.indexOf(f.type.toLowerCase()) === -1) { errorEl.textContent = 'Sai định dạng ảnh.'; errorEl.classList.remove('d-none'); input.value=''; return; }

        const reader = new FileReader();
        reader.onload = function (e) {
            preview.src = e.target.result;
            preview.classList.remove('d-none');
            if (placeholder) placeholder.classList.add('d-none');
        };
        reader.readAsDataURL(f);
    });
})();
</script>