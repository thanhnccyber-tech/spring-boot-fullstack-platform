<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>${fn:escapeXml(isNew ? 'Thêm' : 'Sửa')} chi nhánh</title>

<c:if test="${error != null}">
    <div class="alert alert-danger alert-dismissible fade show">
        ${fn:escapeXml(error)}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<form action="${pageContext.request.contextPath}/admin/branches/save" method="post">
    <input type="hidden" name="id" value="${fn:escapeXml(branch.id)}">

    <div class="row g-4">
        <div class="col-lg-8">
            <div class="card mb-4">
                <div class="card-header bg-white">
                    <i class="fas fa-store text-primary me-1"></i> <b>Thông tin chi nhánh</b>
                </div>
                <div class="card-body">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold">Tên chi nhánh <span class="text-danger">*</span></label>
                            <input type="text" name="name" value="${fn:escapeXml(branch.name)}" class="form-control" maxlength="150" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold">Điện thoại <span class="text-danger">*</span></label>
                            <input type="tel" name="phone" id="branchPhone" value="${fn:escapeXml(branch.phone)}"
                                   class="form-control" pattern="0[0-9]{9,10}" maxlength="11" required>
                            <small class="text-muted">Bắt đầu bằng 0, từ 10-11 chữ số.</small>
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Địa chỉ <span class="text-danger">*</span></label>
                            <input type="text" name="address" value="${fn:escapeXml(branch.address)}" class="form-control" maxlength="255" required>
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Toạ độ (Tuỳ chọn)</label>
                            <div class="row g-2">
                                <div class="col-md-6">
                                    <input type="number" step="any" min="-90" max="90" name="latitude" value="${fn:escapeXml(branch.latitude)}" class="form-control" placeholder="Vĩ độ (VD: 10.7743)">
                                </div>
                                <div class="col-md-6">
                                    <input type="number" step="any" min="-180" max="180" name="longitude" value="${fn:escapeXml(branch.longitude)}" class="form-control" placeholder="Kinh độ (VD: 106.7040)">
                                </div>
                            </div>
                            <small class="text-muted">Dùng để gợi ý cửa hàng gần nhất và tính phí ship.</small>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-4">
            <div class="card mb-4 sticky-top" style="top: 80px;">
                <div class="card-header bg-white">
                    <i class="fas fa-toggle-on text-primary me-1"></i> <b>Trạng thái</b>
                </div>
                <div class="card-body">
                    <div class="form-check form-switch">
                        <input class="form-check-input" type="checkbox" name="active" value="true" id="bActive"
                               <c:if test="${branch.active}">checked</c:if>>
                        <label class="form-check-label" for="bActive">Đang hoạt động</label>
                    </div>
                </div>
                <div class="card-footer bg-white py-3 d-flex gap-2">
                    <button type="submit" class="btn btn-warning flex-grow-1 fw-bold">
                        <i class="fas fa-save me-1"></i> Lưu
                    </button>
                    <a href="${pageContext.request.contextPath}/admin/branches" class="btn btn-outline-secondary">Hủy</a>
                </div>
            </div>
        </div>
    </div>
</form>

<script>
document.getElementById('branchPhone').addEventListener('input', function () {
    this.value = this.value.replace(/[^0-9]/g, '');
});
</script>