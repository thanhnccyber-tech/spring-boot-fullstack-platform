<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>${fn:escapeXml(isNew ? 'Thêm' : 'Sửa')} chi nhánh</title>

<c:if test="${error != null}">
    <div class="alert alert-danger">${fn:escapeXml(error)}</div>
</c:if>

<form action="${pageContext.request.contextPath}/admin/branches/save" method="post">
    <input type="hidden" name="id" value="${fn:escapeXml(branch.id)}">

    <div class="mb-3">
        <label>Tên chi nhánh <span class="text-danger">*</span></label>
        <input type="text" name="name" value="${fn:escapeXml(branch.name)}"
               class="form-control" maxlength="150" required>
        <small class="text-muted">Tối đa 150 ký tự.</small>
    </div>

    <div class="mb-3">
        <label>Địa chỉ <span class="text-danger">*</span></label>
        <input type="text" name="address" value="${fn:escapeXml(branch.address)}"
               class="form-control" maxlength="255" required>
        <small class="text-muted">Tối đa 255 ký tự.</small>
    </div>

    <div class="mb-3">
        <label>Điện thoại <span class="text-danger">*</span></label>
        <input type="tel" name="phone" value="${fn:escapeXml(branch.phone)}"
               class="form-control" pattern="0[0-9]{9,10}" maxlength="11" required>
        <small class="text-muted">
            Bắt đầu bằng 0, từ 10 đến 11 chữ số (VD: 0901234567).
        </small>
    </div>

    <div class="row g-2 mb-3">
        <div class="col-md-6">
            <label>Vĩ độ (latitude)</label>
            <input type="number" step="any" min="-90" max="90" name="latitude" value="${fn:escapeXml(branch.latitude)}"
                   class="form-control" placeholder="VD: 10.7743">
        </div>
        <div class="col-md-6">
            <label>Kinh độ (longitude)</label>
            <input type="number" step="any" min="-180" max="180" name="longitude" value="${fn:escapeXml(branch.longitude)}"
                   class="form-control" placeholder="VD: 106.7040">
        </div>
        <small class="text-muted">Dùng để gợi ý "cửa hàng gần nhất" và tính phí ship theo khoảng cách.
            Lấy toạ độ: mở Google Maps, chuột phải vào vị trí cửa hàng, bấm vào dòng số toạ độ để sao chép.</small>
    </div>

    <div class="form-check mb-3">
        <input type="checkbox" name="active" value="true"
               id="activeCheckbox" class="form-check-input"
               <c:if test="${branch.active}">checked</c:if>>
        <label class="form-check-label" for="activeCheckbox">Đang hoạt động</label>
    </div>

    <button class="btn btn-warning">Lưu</button>
    <a href="${pageContext.request.contextPath}/admin/branches"
       class="btn btn-secondary">Hủy</a>
</form>

<script>
(function () {
    const form = document.querySelector('form');
    const phone = document.querySelector('input[name="phone"]');
    phone.addEventListener('input', function () {
        phone.value = phone.value.replace(/[^0-9]/g, '');
    });
})();
</script>