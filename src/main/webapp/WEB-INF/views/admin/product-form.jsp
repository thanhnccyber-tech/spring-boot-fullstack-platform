<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>${fn:escapeXml(product.id != null ? 'Sửa' : 'Thêm')} sản phẩm</title>

<c:if test="${error != null}"><div class="alert alert-danger">${fn:escapeXml(error)}</div></c:if>

<form action="${pageContext.request.contextPath}/admin/products/save"
      method="post" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${fn:escapeXml(product.id)}">

    <div class="row">
        <div class="col-md-8">
            <div class="mb-3"><label>Tên sản phẩm</label>
                <input type="text" name="name" value="${fn:escapeXml(product.name)}" class="form-control" required></div>
            <div class="mb-3"><label>Giá cơ bản (VNĐ)</label>
                <input type="number" name="basePrice" value="${fn:escapeXml(product.basePrice)}" class="form-control" required step="1000"></div>
            <div class="mb-3"><label>Mô tả</label>
                <textarea name="description" class="form-control" rows="3">${fn:escapeXml(product.description)}</textarea></div>
            <div class="mb-3"><label>Danh mục</label>
                <select name="categoryId" class="form-select">
                    <c:forEach items="${categories}" var="c">
                        <option value="${fn:escapeXml(c.id)}"
                            <c:if test="${product.category != null && product.category.id == c.id}">selected</c:if>>
                            ${fn:escapeXml(c.name)}
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="mb-3"><label>Topping áp dụng</label><br/>
                <c:forEach items="${toppings}" var="t">
                    <div class="form-check form-check-inline">
                        <input type="checkbox" name="toppingIds" value="${fn:escapeXml(t.id)}" class="form-check-input"
                            <c:if test="${product.toppings != null && product.toppings.contains(t)}">checked</c:if>>
                        <label class="form-check-label">${fn:escapeXml(t.name)}</label>
                    </div>
                </c:forEach>
            </div>

            <hr/>
            <h6 class="mb-3">⚙️ Cấu hình tùy chọn</h6>

            <!-- ===== SIZE ===== -->
            <div class="card mb-3">
                <div class="card-body">
                    <div class="form-check mb-2">
                        <input type="checkbox" name="allowSize" value="true" class="form-check-input"
                               id="allowSize" <c:if test="${product.allowSize}">checked</c:if>>
                        <label class="form-check-label fw-bold" for="allowSize">
                            Cho phép chọn Size
                        </label>
                    </div>
                    <div id="sizeGroup">
                        <label class="form-label small">Size được phép chọn
                            <span class="text-muted">(để trống = áp dụng tất cả size đang active)</span>
                        </label>
                        <div class="d-flex flex-wrap gap-2 mb-2">
                            <c:forEach items="${sizes}" var="s">
                                <div class="form-check form-check-inline">
                                    <input type="checkbox" name="sizeIds" value="${fn:escapeXml(s.id)}"
                                           class="form-check-input"
                                        <c:if test="${product.sizes != null && product.sizes.contains(s)}">checked</c:if>>
                                    <label class="form-check-label">
                                        ${fn:escapeXml(s.name)}
                                        <c:if test="${s.extraPrice > 0}">
                                            (+${fn:escapeXml(s.extraPrice)}đ)
                                        </c:if>
                                    </label>
                                </div>
                            </c:forEach>
                        </div>
                        <label class="form-label small">Size mặc định:</label>
                        <select name="defaultSizeId" class="form-select">
                            <option value="">-- Không --</option>
                            <c:forEach items="${sizes}" var="s">
                                <option value="${fn:escapeXml(s.id)}"
                                    <c:if test="${product.defaultSize != null && product.defaultSize.id == s.id}">selected</c:if>>
                                    ${fn:escapeXml(s.name)} (+${fn:escapeXml(s.extraPrice)}đ)
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </div>

            <!-- ===== SUGAR ===== -->
            <div class="card mb-3">
                <div class="card-body">
                    <div class="form-check mb-2">
                        <input type="checkbox" name="allowSugar" value="true" class="form-check-input"
                               id="allowSugar" <c:if test="${product.allowSugar}">checked</c:if>>
                        <label class="form-check-label fw-bold" for="allowSugar">
                            Cho phép chỉnh mức Đường
                        </label>
                    </div>
                    <div id="sugarGroup">
                        <label class="form-label small">Mức đường được phép chọn
                            <span class="text-muted">(để trống = áp dụng tất cả)</span>
                        </label>
                        <div class="d-flex flex-wrap gap-2 mb-2">
                            <c:forEach items="${sugars}" var="s">
                                <div class="form-check form-check-inline">
                                    <input type="checkbox" name="sugarIds" value="${fn:escapeXml(s.id)}"
                                           class="form-check-input"
                                        <c:if test="${product.sugars != null && product.sugars.contains(s)}">checked</c:if>>
                                    <label class="form-check-label">${fn:escapeXml(s.name)}</label>
                                </div>
                            </c:forEach>
                        </div>
                        <label class="form-label small">Mức đường mặc định:</label>
                        <select name="defaultSugarId" class="form-select">
                            <option value="">-- Không --</option>
                            <c:forEach items="${sugars}" var="s">
                                <option value="${fn:escapeXml(s.id)}"
                                    <c:if test="${product.defaultSugar != null && product.defaultSugar.id == s.id}">selected</c:if>>
                                    ${fn:escapeXml(s.name)}
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </div>

            <!-- ===== ICE ===== -->
            <div class="card mb-3">
                <div class="card-body">
                    <div class="form-check mb-2">
                        <input type="checkbox" name="allowIce" value="true" class="form-check-input"
                               id="allowIce" <c:if test="${product.allowIce}">checked</c:if>>
                        <label class="form-check-label fw-bold" for="allowIce">
                            Cho phép chỉnh mức Đá
                        </label>
                    </div>
                    <div id="iceGroup">
                        <label class="form-label small">Mức đá được phép chọn
                            <span class="text-muted">(để trống = áp dụng tất cả)</span>
                        </label>
                        <div class="d-flex flex-wrap gap-2 mb-2">
                            <c:forEach items="${ices}" var="s">
                                <div class="form-check form-check-inline">
                                    <input type="checkbox" name="iceIds" value="${fn:escapeXml(s.id)}"
                                           class="form-check-input"
                                        <c:if test="${product.ices != null && product.ices.contains(s)}">checked</c:if>>
                                    <label class="form-check-label">${fn:escapeXml(s.name)}</label>
                                </div>
                            </c:forEach>
                        </div>
                        <label class="form-label small">Mức đá mặc định:</label>
                        <select name="defaultIceId" class="form-select">
                            <option value="">-- Không --</option>
                            <c:forEach items="${ices}" var="s">
                                <option value="${fn:escapeXml(s.id)}"
                                    <c:if test="${product.defaultIce != null && product.defaultIce.id == s.id}">selected</c:if>>
                                    ${fn:escapeXml(s.name)}
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
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
            <div class="form-check mb-2">
                <input type="checkbox" name="active" value="true" class="form-check-input"
                    <c:if test="${product.active}">checked</c:if>>
                <label class="form-check-label">Đang bán (hiển thị trên menu khách)</label>
            </div>
            <div class="form-check mb-3">
                <input type="checkbox" name="outOfStock" value="true" class="form-check-input"
                    <c:if test="${product.outOfStock}">checked</c:if>>
                <label class="form-check-label text-danger fw-bold">
                    <i class="fas fa-times-circle"></i> Hết hàng (không cho khách thêm vào giỏ)
                </label>
            </div>
        </div>
        <div class="col-md-4">
            <label>Ảnh (Cloudinary)</label>
            <input type="hidden" name="imageUrl" value="${fn:escapeXml(product.imageUrl)}">
            <input type="file" name="imageFile" id="productImage"
                   class="form-control mb-2"
                   accept="image/jpeg,image/png,image/gif,image/webp">
            <small class="text-muted d-block">JPG, PNG, GIF, WEBP. Tối đa 5 MB.</small>
            <small id="productImageError" class="text-danger d-none"></small>
            <c:if test="${product.imageUrl != null}">
                <img src="${fn:escapeXml(product.imageUrl)}" class="img-fluid mt-2 rounded">
            </c:if>
        </div>
    </div>

    <button class="btn btn-warning">Lưu</button>
    <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-secondary">Hủy</a>
</form>

<script>
(function () {
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
        if (f.size > MAX) { showError('Ảnh không được vượt quá 5 MB.'); return; }
        if (ALLOWED.indexOf(f.type.toLowerCase()) === -1) {
            showError('Chỉ chấp nhận ảnh JPG, PNG, GIF hoặc WEBP.');
        }
    });
})();
</script>