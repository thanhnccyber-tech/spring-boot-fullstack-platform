<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>${fn:escapeXml(isNew ? 'Thêm người dùng' : 'Sửa người dùng')}</title>

<c:if test="${error != null}">
    <div class="alert alert-danger alert-dismissible fade show">
        ${fn:escapeXml(error)}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<form action="${pageContext.request.contextPath}/admin/users/save" method="post" enctype="multipart/form-data">
    <c:if test="${!isNew}"><input type="hidden" name="id" value="${fn:escapeXml(user.id)}"></c:if>

    <div class="row g-4">
        <div class="col-lg-8">
            <div class="card mb-4">
                <div class="card-header bg-white">
                    <i class="fas fa-user text-primary me-1"></i> <b>Thông tin tài khoản</b>
                </div>
                <div class="card-body">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold">Email <span class="text-danger">*</span></label>
                            <input type="email" name="email" value="${fn:escapeXml(user.email)}" class="form-control"
                                   ${fn:escapeXml(isNew ? 'required' : 'readonly')}>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold">Mật khẩu ${fn:escapeXml(isNew ? '*' : '(Để trống nếu không đổi)')}</label>
                            <input type="password" name="newPassword" class="form-control" minlength="6" ${fn:escapeXml(isNew ? 'required' : '')}>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold">Họ tên <span class="text-danger">*</span></label>
                            <input type="text" name="fullName" value="${fn:escapeXml(user.fullName)}" class="form-control" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold">Điện thoại</label>
                            <input type="text" name="phone" value="${fn:escapeXml(user.phone)}" class="form-control">
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Địa chỉ</label>
                            <input type="text" name="address" value="${fn:escapeXml(user.address)}" class="form-control">
                        </div>
                    </div>
                </div>
            </div>

            <div class="card mb-4">
                <div class="card-header bg-white">
                    <i class="fas fa-user-shield text-primary me-1"></i> <b>Phân quyền</b>
                </div>
                <div class="card-body">
                    <div class="mb-3">
                        <label class="form-label small fw-semibold d-block mb-2">Vai trò</label>
                        <c:forEach items="${roles}" var="r">
                            <div class="form-check form-check-inline">
                                <input class="form-check-input role-checkbox" type="checkbox" name="roleIds" value="${fn:escapeXml(r.id)}"
                                       data-rolename="${fn:escapeXml(r.name)}" id="role${r.id}"
                                    <c:choose>
                                        <c:when test="${not empty selectedRoleIds}">
                                            <c:if test="${selectedRoleIds.contains(r.id)}">checked</c:if>
                                        </c:when>
                                        <c:otherwise>
                                            <c:if test="${user.roles.contains(r)}">checked</c:if>
                                        </c:otherwise>
                                    </c:choose>
                                >
                                <label class="form-check-label" for="role${r.id}">${fn:escapeXml(fn:replace(r.name, 'ROLE_', ''))}</label>
                            </div>
                        </c:forEach>
                    </div>

                    <div class="mb-0" id="branchGroup" style="display:none">
                        <label class="form-label small fw-semibold">Chi nhánh (Bắt buộc với Staff/Manager) <span class="text-danger">*</span></label>
                        <select name="branchId" class="form-select" id="branchSelect">
                            <option value="">-- Chọn chi nhánh --</option>
                            <c:forEach items="${branches}" var="b">
                                <option value="${fn:escapeXml(b.id)}"
                                    <c:choose>
                                        <c:when test="${not empty selectedBranchId}">
                                            <c:if test="${selectedBranchId == b.id}">selected</c:if>
                                        </c:when>
                                        <c:otherwise>
                                            <c:if test="${user.branch != null && user.branch.id == b.id}">selected</c:if>
                                        </c:otherwise>
                                    </c:choose>
                                >${fn:escapeXml(b.name)}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-4">
            <div class="card mb-4 sticky-top" style="top: 80px;">
                <div class="card-header bg-white">
                    <i class="fas fa-id-card text-primary me-1"></i> <b>Avatar & Trạng thái</b>
                </div>
                <div class="card-body text-center">
                    <div class="mb-3">
                        <c:choose>
                            <c:when test="${not empty user.avatarUrl}">
                                <img src="${fn:escapeXml(user.avatarUrl)}" id="userAvatarPreview" class="rounded-circle mb-2" width="100" height="100" style="object-fit:cover;">
                            </c:when>
                            <c:otherwise>
                                <div id="userAvatarPlaceholder" class="rounded-circle bg-light d-inline-flex align-items-center justify-content-center mb-2" style="width:100px;height:100px;">
                                    <i class="fas fa-user fa-3x text-muted opacity-50"></i>
                                </div>
                                <img src="" id="userAvatarPreview" class="rounded-circle mb-2 d-none" width="100" height="100" style="object-fit:cover;">
                            </c:otherwise>
                        </c:choose>
                        <input type="file" name="avatar" id="userAvatar" class="form-control form-control-sm" accept="image/jpeg,image/png,image/gif,image/webp">
                        <small class="text-muted d-block mt-1">Tối đa 5 MB.</small>
                        <small id="userAvatarError" class="text-danger d-none d-block mt-1"></small>
                    </div>
                    <hr>
                    <div class="form-check form-switch text-start">
                        <input class="form-check-input" type="checkbox" name="enabled" value="true" id="uEnabled"
                               <c:if test="${user.enabled}">checked</c:if>>
                        <label class="form-check-label" for="uEnabled">Kích hoạt tài khoản</label>
                    </div>
                </div>
                <div class="card-footer bg-white py-3 d-flex gap-2">
                    <button type="submit" class="btn btn-warning flex-grow-1 fw-bold">
                        <i class="fas fa-save me-1"></i> Lưu
                    </button>
                    <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-outline-secondary">Hủy</a>
                </div>
            </div>
        </div>
    </div>
</form>

<script>
(function() {
    // Toggle chi nhánh theo vai trò
    const branchGroup = document.getElementById('branchGroup');
    const branchSelect = document.getElementById('branchSelect');
    const roleBoxes = document.querySelectorAll('.role-checkbox');
    function updateRole() {
        let need = false;
        roleBoxes.forEach(cb => {
            if (cb.checked && (cb.dataset.rolename === 'ROLE_STAFF' || cb.dataset.rolename === 'ROLE_MANAGER')) need = true;
        });
        branchGroup.style.display = need ? 'block' : 'none';
        branchSelect.required = need;
    }
    roleBoxes.forEach(cb => cb.addEventListener('change', updateRole));
    updateRole();

    // Preview Avatar
    const input = document.getElementById('userAvatar');
    const errorEl = document.getElementById('userAvatarError');
    const preview = document.getElementById('userAvatarPreview');
    const placeholder = document.getElementById('userAvatarPlaceholder');
    const MAX = 5 * 1024 * 1024;
    const ALLOWED = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];

    input.addEventListener('change', function () {
        errorEl.classList.add('d-none');
        if (!input.files || input.files.length === 0) return;
        const f = input.files[0];
        if (f.size > MAX) { errorEl.textContent = 'Ảnh vượt quá 5 MB.'; errorEl.classList.remove('d-none'); input.value=''; return; }
        if (ALLOWED.indexOf(f.type.toLowerCase()) === -1) { errorEl.textContent = 'Sai định dạng.'; errorEl.classList.remove('d-none'); input.value=''; return; }

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