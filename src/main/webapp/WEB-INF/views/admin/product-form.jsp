<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<title>${product.id != null ? 'Sửa' : 'Thêm'} sản phẩm</title>

<c:if test="${error != null}"><div class="alert alert-danger">${error}</div></c:if>

<form action="${pageContext.request.contextPath}/admin/products/save"
      method="post" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${product.id}">

    <div class="row">
        <div class="col-md-8">
            <div class="mb-3"><label>Tên sản phẩm</label>
                <input type="text" name="name" value="${product.name}" class="form-control" required></div>
            <div class="mb-3"><label>Giá cơ bản (VNĐ)</label>
                <input type="number" name="basePrice" value="${product.basePrice}" class="form-control" required step="1000"></div>
            <div class="mb-3"><label>Mô tả</label>
                <textarea name="description" class="form-control" rows="3">${product.description}</textarea></div>
            <div class="mb-3"><label>Danh mục</label>
                <select name="categoryId" class="form-select">
                    <c:forEach items="${categories}" var="c">
                        <option value="${c.id}"
                            <c:if test="${product.category != null && product.category.id == c.id}">selected</c:if>>
                            ${c.name}
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="mb-3"><label>Topping áp dụng</label><br/>
                <c:forEach items="${toppings}" var="t">
                    <div class="form-check form-check-inline">
                        <input type="checkbox" name="toppingIds" value="${t.id}" class="form-check-input"
                            <c:if test="${product.toppings != null && product.toppings.contains(t)}">checked</c:if>>
                        <label class="form-check-label">${t.name}</label>
                    </div>
                </c:forEach>
            </div>
            <div class="form-check mb-2">
                <input type="checkbox" name="featured" value="true" class="form-check-input"
                    <c:if test="${product.featured}">checked</c:if>>
                <label class="form-check-label">Sản phẩm nổi bật</label>
            </div>
            <div class="form-check mb-2">
                <input type="checkbox" name="isNew" value="true" class="form-check-input"
                    <c:if test="${product.isNew}">checked</c:if>>
                <label class="form-check-label">Sản phẩm mới</label>
            </div>
            <div class="form-check mb-3">
                <input type="checkbox" name="active" value="true" class="form-check-input"
                    <c:if test="${product.active}">checked</c:if>>
                <label class="form-check-label">Đang bán</label>
            </div>
        </div>
        <div class="col-md-4">
            <label>Ảnh (Cloudinary)</label>
            <input type="file" name="imageFile" id="productImage"
                   class="form-control mb-2"
                   accept="image/jpeg,image/png,image/gif,image/webp">
            <small class="text-muted d-block">JPG, PNG, GIF, WEBP. Tối đa 5 MB.</small>
            <small id="productImageError" class="text-danger d-none"></small>
            <c:if test="${product.imageUrl != null}">
                <img src="${product.imageUrl}" class="img-fluid mt-2 rounded">
            </c:if>
        </div>
    </div>

    <button class="btn btn-warning">Lưu</button>
    <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-secondary">Hủy</a>
</form>

<script>
(function () {
    const input = document.getElementById('productImage');
    const errorEl = document.getElementById('productImageError');
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