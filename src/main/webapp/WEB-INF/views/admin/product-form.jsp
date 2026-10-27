<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>${fn:escapeXml(product.id != null ? 'Sửa' : 'Thêm')} sản phẩm</title>

<c:if test="${error != null}">
    <div class="alert alert-danger alert-dismissible fade show">
        ${fn:escapeXml(error)}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<form action="${pageContext.request.contextPath}/admin/products/save" method="post" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${fn:escapeXml(product.id)}">

    <div class="row g-4">
        <%-- ============ CỘT TRÁI: THÔNG TIN CHÍNH ============ --%>
        <div class="col-lg-8">
            <%-- Card 1: Thông tin cơ bản --%>
            <div class="card mb-4">
                <div class="card-header bg-white">
                    <i class="fas fa-info-circle text-primary me-1"></i> <b>Thông tin cơ bản</b>
                </div>
                <div class="card-body">
                    <div class="row g-3">
                        <div class="col-md-8">
                            <label class="form-label small fw-semibold">Tên sản phẩm <span class="text-danger">*</span></label>
                            <input name="name" value="${fn:escapeXml(product.name)}" class="form-control" required>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label small fw-semibold">Danh mục</label>
                            <select name="categoryId" class="form-select">
                                <option value="">-- Không chọn --</option>
                                <c:forEach items="${categories}" var="c">
                                    <option value="${fn:escapeXml(c.id)}"
                                        <c:if test="${product.category != null && product.category.id == c.id}">selected</c:if>>
                                        ${fn:escapeXml(c.name)}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold">Giá cơ bản (VNĐ) <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <input name="basePrice" type="number" min="0" step="1000" value="${fn:escapeXml(product.basePrice)}" class="form-control" required>
                                <span class="input-group-text">₫</span>
                            </div>
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Mô tả</label>
                            <textarea name="description" class="form-control" rows="3">${fn:escapeXml(product.description)}</textarea>
                        </div>
                    </div>
                </div>
            </div>

            <%-- Card 2: Cấu hình tuỳ chọn --%>
            <div class="card mb-4">
                <div class="card-header bg-white">
                    <i class="fas fa-cogs text-primary me-1"></i> <b>Cấu hình tuỳ chọn</b>
                </div>
                <div class="card-body p-0">

                    <%-- ----- SIZE ----- --%>
                    <div class="p-3 border-bottom">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <label class="fw-semibold mb-0"><i class="fas fa-ruler text-muted me-2"></i>Kích cỡ (Size)</label>
                            <div class="form-check form-switch">
                                <input type="checkbox" name="allowSize" value="true" class="form-check-input" id="allowSize"
                                       <c:if test="${product.allowSize}">checked</c:if>>
                                <label class="form-check-label small" for="allowSize">Cho phép chọn</label>
                            </div>
                        </div>
                        <div id="sizeGroup" class="ms-4">
                            <div class="d-flex flex-wrap gap-3 mb-2">
                                <c:forEach items="${sizes}" var="s">
                                    <div class="form-check">
                                        <input type="checkbox" name="sizeIds" value="${fn:escapeXml(s.id)}" class="form-check-input"
                                               id="sz${s.id}" <c:if test="${product.sizes != null && product.sizes.contains(s)}">checked</c:if>>
                                        <label class="form-check-label small" for="sz${s.id}">
                                            ${fn:escapeXml(s.name)} <span class="text-muted">(+${fn:escapeXml(s.extraPrice)}đ)</span>
                                        </label>
                                    </div>
                                </c:forEach>
                            </div>
                            <select name="defaultSizeId" class="form-select form-select-sm" style="max-width: 250px;">
                                <option value="">-- Size mặc định --</option>
                                <c:forEach items="${sizes}" var="s">
                                    <option value="${fn:escapeXml(s.id)}" <c:if test="${product.defaultSize != null && product.defaultSize.id == s.id}">selected</c:if>>
                                        ${fn:escapeXml(s.name)}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <%-- ----- SUGAR ----- --%>
                    <div class="p-3 border-bottom">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <label class="fw-semibold mb-0"><i class="fas fa-cubes-stacked text-muted me-2"></i>Mức đường</label>
                            <div class="form-check form-switch">
                                <input type="checkbox" name="allowSugar" value="true" class="form-check-input" id="allowSugar"
                                       <c:if test="${product.allowSugar}">checked</c:if>>
                                <label class="form-check-label small" for="allowSugar">Cho phép chọn</label>
                            </div>
                        </div>
                        <div id="sugarGroup" class="ms-4">
                            <div class="d-flex flex-wrap gap-3 mb-2">
                                <c:forEach items="${sugars}" var="s">
                                    <div class="form-check">
                                        <input type="checkbox" name="sugarIds" value="${fn:escapeXml(s.id)}" class="form-check-input"
                                               id="sg${s.id}" <c:if test="${product.sugars != null && product.sugars.contains(s)}">checked</c:if>>
                                        <label class="form-check-label small" for="sg${s.id}">${fn:escapeXml(s.name)}</label>
                                    </div>
                                </c:forEach>
                            </div>
                            <select name="defaultSugarId" class="form-select form-select-sm" style="max-width: 250px;">
                                <option value="">-- Mức đường mặc định --</option>
                                <c:forEach items="${sugars}" var="s">
                                    <option value="${fn:escapeXml(s.id)}" <c:if test="${product.defaultSugar != null && product.defaultSugar.id == s.id}">selected</c:if>>
                                        ${fn:escapeXml(s.name)}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <%-- ----- ICE ----- --%>
                    <div class="p-3 border-bottom">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <label class="fw-semibold mb-0"><i class="fas fa-snowflake text-muted me-2"></i>Mức đá</label>
                            <div class="form-check form-switch">
                                <input type="checkbox" name="allowIce" value="true" class="form-check-input" id="allowIce"
                                       <c:if test="${product.allowIce}">checked</c:if>>
                                <label class="form-check-label small" for="allowIce">Cho phép chọn</label>
                            </div>
                        </div>
                        <div id="iceGroup" class="ms-4">
                            <div class="d-flex flex-wrap gap-3 mb-2">
                                <c:forEach items="${ices}" var="s">
                                    <div class="form-check">
                                        <input type="checkbox" name="iceIds" value="${fn:escapeXml(s.id)}" class="form-check-input"
                                               id="ic${s.id}" <c:if test="${product.ices != null && product.ices.contains(s)}">checked</c:if>>
                                        <label class="form-check-label small" for="ic${s.id}">${fn:escapeXml(s.name)}</label>
                                    </div>
                                </c:forEach>
                            </div>
                            <select name="defaultIceId" class="form-select form-select-sm" style="max-width: 250px;">
                                <option value="">-- Mức đá mặc định --</option>
                                <c:forEach items="${ices}" var="s">
                                    <option value="${fn:escapeXml(s.id)}" <c:if test="${product.defaultIce != null && product.defaultIce.id == s.id}">selected</c:if>>
                                        ${fn:escapeXml(s.name)}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <%-- ----- TOPPING ----- --%>
                    <div class="p-3">
                        <label class="fw-semibold mb-2 d-block"><i class="fas fa-ice-cream text-muted me-2"></i>Topping áp dụng</label>
                        <div class="d-flex flex-wrap gap-3 ms-4">
                            <c:forEach items="${toppings}" var="t">
                                <div class="form-check">
                                    <input type="checkbox" name="toppingIds" value="${fn:escapeXml(t.id)}" class="form-check-input"
                                           id="tp${t.id}" <c:if test="${product.toppings != null && product.toppings.contains(t)}">checked</c:if>>
                                    <label class="form-check-label small" for="tp${t.id}">${fn:escapeXml(t.name)}</label>
                                </div>
                            </c:forEach>
                        </div>
                    </div>

                </div>
            </div>
        </div>

        <%-- ============ CỘT PHẢI: ẢNH & TRẠNG THÁI & HÀNH ĐỘNG ============ --%>
        <div class="col-lg-4">

            <%-- Card 3: Ảnh đại diện --%>
            <div class="card mb-4">
                <div class="card-header bg-white">
                    <i class="fas fa-image text-primary me-1"></i> <b>Ảnh đại diện</b>
                </div>
                <div class="card-body text-center">
                    <div class="mb-3">
                        <c:choose>
                            <c:when test="${not empty product.imageUrl}">
                                <img src="${fn:escapeXml(product.imageUrl)}" id="imagePreview" class="img-fluid rounded mb-2" style="max-height: 200px; object-fit: contain;">
                            </c:when>
                            <c:otherwise>
                                <div id="imagePlaceholder" class="bg-light rounded d-flex align-items-center justify-content-center mb-2" style="height: 150px;">
                                    <i class="fas fa-cloud-upload-alt fa-3x text-muted opacity-50"></i>
                                </div>
                                <img src="" id="imagePreview" class="img-fluid rounded mb-2 d-none" style="max-height: 200px; object-fit: contain;">
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <input type="file" name="imageFile" id="productImage" class="form-control form-control-sm" accept="image/jpeg,image/png,image/gif,image/webp">
                    <small class="text-muted d-block mt-2">JPG, PNG, GIF, WEBP. Tối đa 5 MB.</small>
                    <small id="productImageError" class="text-danger d-none d-block mt-1 fw-semibold"></small>
                </div>
            </div>

            <%-- Card 4: Trạng thái & Hành động --%>
            <div class="card mb-4 sticky-top" style="top: 80px; z-index: 10;">
                <div class="card-header bg-white">
                    <i class="fas fa-toggle-on text-primary me-1"></i> <b>Trạng thái</b>
                </div>
                <div class="card-body">
                    <div class="form-check form-switch mb-2">
                        <input class="form-check-input" type="checkbox" name="active" value="true" id="pActive"
                               <c:if test="${product.active}">checked</c:if>>
                        <label class="form-check-label" for="pActive">Đang bán (Hiển thị)</label>
                    </div>
                    <div class="form-check form-switch mb-2">
                        <input class="form-check-input" type="checkbox" name="outOfStock" value="true" id="pOutOfStock"
                               <c:if test="${product.outOfStock}">checked</c:if>>
                        <label class="form-check-label text-danger fw-semibold" for="pOutOfStock">Tạm hết hàng</label>
                    </div>
                    <hr class="my-3">
                    <div class="form-check form-switch mb-2">
                        <input class="form-check-input" type="checkbox" name="featured" value="true" id="pFeatured"
                               <c:if test="${product.featured}">checked</c:if>>
                        <label class="form-check-label" for="pFeatured">Sản phẩm nổi bật</label>
                    </div>
                    <div class="form-check form-switch">
                        <input class="form-check-input" type="checkbox" name="isNew" value="true" id="pIsNew"
                               <c:if test="${product.isNew}">checked</c:if>>
                        <label class="form-check-label" for="pIsNew">Sản phẩm mới</label>
                    </div>
                </div>
                <div class="card-footer bg-white py-3 d-flex gap-2">
                    <button type="submit" class="btn btn-warning flex-grow-1 fw-bold">
                        <i class="fas fa-save me-1"></i> Lưu sản phẩm
                    </button>
                    <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-outline-secondary">Hủy</a>
                </div>
            </div>

        </div>
    </div>
</form>

<script>
(function () {
    // Toggle hiển thị nhóm options
    function toggleGroup(cbId, grpId) {
        const cb = document.getElementById(cbId);
        const grp = document.getElementById(grpId);
        function update() { grp.style.display = cb.checked ? 'block' : 'none'; }
        cb.addEventListener('change', update);
        update();
    }
    toggleGroup('allowSize', 'sizeGroup');
    toggleGroup('allowSugar', 'sugarGroup');
    toggleGroup('allowIce', 'iceGroup');

    // Preview ảnh + Validate client-side
    const input = document.getElementById('productImage');
    const errorEl = document.getElementById('productImageError');
    const preview = document.getElementById('imagePreview');
    const placeholder = document.getElementById('imagePlaceholder');
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
        if (f.size > MAX) { showError('Ảnh không được vượt quá 5 MB.'); return; }
        if (ALLOWED.indexOf(f.type.toLowerCase()) === -1) {
            showError('Chỉ chấp nhận ảnh JPG, PNG, GIF hoặc WEBP.'); return;
        }
        // Preview
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