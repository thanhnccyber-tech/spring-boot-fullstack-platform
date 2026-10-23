<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>${isNew ? 'Thêm người dùng' : 'Sửa người dùng'}</title>

<c:if test="${error != null}"><div class="alert alert-danger">${error}</div></c:if>

<form action="${pageContext.request.contextPath}/admin/users/save" method="post" enctype="multipart/form-data">
    <c:if test="${!isNew}"><input type="hidden" name="id" value="${user.id}"></c:if>

    <div class="row">
        <div class="col-md-6">
            <div class="mb-3"><label>Email *</label>
                <input type="email" name="email" value="${user.email}" class="form-control"
                       ${isNew ? 'required' : 'readonly'}></div>
            <div class="mb-3"><label>Họ tên *</label>
                <input type="text" name="fullName" value="${user.fullName}" class="form-control" required></div>
            <div class="mb-3"><label>Điện thoại</label>
                <input type="text" name="phone" value="${user.phone}" class="form-control"></div>
            <div class="mb-3"><label>Địa chỉ</label>
                <input type="text" name="address" value="${user.address}" class="form-control"></div>
            <div class="mb-3"><label>Avatar</label>
                <input type="file" name="avatar" id="userAvatar"
                       class="form-control"
                       accept="image/jpeg,image/png,image/gif,image/webp">
                <small class="text-muted d-block">JPG, PNG, GIF, WEBP. Tối đa 5 MB.</small>
                <small id="userAvatarError" class="text-danger d-none"></small>
                <c:if test="${user.avatarUrl != null}">
                    <img src="${user.avatarUrl}" class="rounded-circle mt-2" width="80" height="80">
                </c:if>
            </div>
        </div>
        <div class="col-md-6">
            <div class="mb-3"><label>Mật khẩu ${isNew ? '*' : '(để trống nếu không đổi)'}</label>
                <input type="password" name="newPassword" class="form-control"
                       minlength="6" ${isNew ? 'required' : ''}></div>

            <div class="mb-3"><label>Vai trò</label><br/>
                <c:forEach items="${roles}" var="r">
                    <div class="form-check form-check-inline">
                        <input class="form-check-input role-checkbox" type="checkbox"
                               name="roleIds" value="${r.id}"
                               data-rolename="${r.name}"
                            <c:choose>
                                <c:when test="${not empty selectedRoleIds}">
                                    <c:if test="${selectedRoleIds.contains(r.id)}">checked</c:if>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${user.roles.contains(r)}">checked</c:if>
                                </c:otherwise>
                            </c:choose>
                        >
                        <label class="form-check-label">${fn:replace(r.name, 'ROLE_', '')}</label>
                    </div>
                </c:forEach>
            </div>

            <div class="mb-3" id="branchGroup" style="display:none">
                <label>Chi nhánh *</label>
                <select name="branchId" class="form-select" id="branchSelect">
                    <option value="">-- Chọn --</option>
                    <c:forEach items="${branches}" var="b">
                        <option value="${b.id}"
                            <c:choose>
                                <c:when test="${not empty selectedBranchId}">
                                    <c:if test="${selectedBranchId == b.id}">selected</c:if>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${user.branch != null && user.branch.id == b.id}">selected</c:if>
                                </c:otherwise>
                            </c:choose>
                        >
                            ${b.name}
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-check mb-3">
                <input type="checkbox" name="enabled" value="true" class="form-check-input"
                    <c:if test="${user.enabled}">checked</c:if>>
                <label class="form-check-label">Kích hoạt</label>
            </div>
        </div>
    </div>

    <button class="btn btn-warning">${isNew ? 'Tạo mới' : 'Lưu'}</button>
    <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary">Hủy</a>
</form>

<script>
(function() {
    const branchGroup = document.getElementById('branchGroup');
    const branchSelect = document.getElementById('branchSelect');
    const roleBoxes = document.querySelectorAll('.role-checkbox');
    function update() {
        let need = false;
        roleBoxes.forEach(cb => {
            if (cb.checked && (cb.dataset.rolename === 'ROLE_STAFF'
                            || cb.dataset.rolename === 'ROLE_MANAGER'))
                need = true;
        });
        branchGroup.style.display = need ? 'block' : 'none';
        branchSelect.required = need;
    }
    roleBoxes.forEach(cb => cb.addEventListener('change', update));
    update();
})();

(function () {
    const input = document.getElementById('userAvatar');
    const errorEl = document.getElementById('userAvatarError');
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