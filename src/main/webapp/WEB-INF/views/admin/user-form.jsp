<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>${isNew ? 'Thêm người dùng' : 'Sửa người dùng'}</title>

<c:if test="${error != null}">
    <div class="alert alert-danger">${error}</div>
</c:if>

<form action="${pageContext.request.contextPath}/admin/users/save"
      method="post" enctype="multipart/form-data">
    <c:if test="${!isNew}">
        <input type="hidden" name="id" value="${user.id}">
    </c:if>

    <div class="row">
        <!-- ===== Cột trái ===== -->
        <div class="col-md-6">
            <div class="mb-3">
                <label class="form-label">Email <span class="text-danger">*</span></label>
                <input type="email" name="email" value="${user.email}"
                       class="form-control" ${isNew ? 'required' : 'readonly'}>
                <c:if test="${!isNew}">
                    <small class="text-muted">Không thể sửa email sau khi tạo.</small>
                </c:if>
            </div>

            <div class="mb-3">
                <label class="form-label">Họ tên <span class="text-danger">*</span></label>
                <input type="text" name="fullName" value="${user.fullName}"
                       class="form-control" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Điện thoại</label>
                <input type="text" name="phone" value="${user.phone}" class="form-control">
            </div>

            <div class="mb-3">
                <label class="form-label">Địa chỉ</label>
                <input type="text" name="address" value="${user.address}" class="form-control">
            </div>

            <div class="mb-3">
                <label class="form-label">Avatar (upload Cloudinary)</label>
                <input type="file" name="avatar" class="form-control" accept="image/*">
                <c:if test="${user.avatarUrl != null}">
                    <img src="${user.avatarUrl}" class="rounded-circle mt-2"
                         style="width:80px;height:80px;object-fit:cover">
                </c:if>
            </div>
        </div>

        <!-- ===== Cột phải ===== -->
        <div class="col-md-6">
            <div class="mb-3">
                <label class="form-label">
                    Mật khẩu
                    <c:if test="${isNew}"><span class="text-danger">*</span></c:if>
                </label>
                <input type="password" name="newPassword" class="form-control"
                       minlength="6" ${isNew ? 'required' : ''}>
                <c:if test="${!isNew}">
                    <small class="text-muted">Để trống nếu không đổi mật khẩu.</small>
                </c:if>
            </div>

            <div class="mb-3">
                <label class="form-label">
                    Vai trò (RBAC) <span class="text-danger">*</span>
                </label><br/>
                <c:forEach items="${roles}" var="r">
                    <div class="form-check form-check-inline">
                        <input class="form-check-input role-checkbox"
                               type="checkbox"
                               name="roleIds"
                               value="${r.id}"
                               id="role-${r.id}"
                               data-rolename="${r.name}"
                            <c:if test="${user.roles.contains(r)}">checked</c:if>>
                        <label class="form-check-label" for="role-${r.id}">
                            ${fn:replace(r.name, 'ROLE_', '')}
                        </label>
                    </div>
                </c:forEach>
                <c:if test="${!isAdmin}">
                    <small class="d-block text-muted mt-1">
                        Quản lý chi nhánh chỉ được cấp tài khoản STAFF.
                    </small>
                </c:if>
            </div>

            <%-- Chỉ hiện khi role chọn có STAFF hoặc MANAGER --%>
            <div class="mb-3" id="branchGroup" style="display:none;">
                <label class="form-label">
                    <i class="fas fa-store"></i> Chi nhánh làm việc
                    <span class="text-danger">*</span>
                </label>
                <select name="branchId" class="form-select" id="branchSelect">
                    <option value="">-- Chọn chi nhánh --</option>
                    <c:forEach items="${branches}" var="b">
                        <option value="${b.id}"
                            <c:if test="${user.branch != null && user.branch.id == b.id}">selected</c:if>>
                            ${b.name} — ${b.address}
                        </option>
                    </c:forEach>
                </select>
                <small class="text-muted">
                    Bắt buộc với vai trò STAFF / MANAGER.
                </small>
            </div>

            <div class="form-check mb-3">
                <input type="checkbox" name="enabled" value="true"
                       class="form-check-input" id="enabledCheck"
                    <c:if test="${user.enabled}">checked</c:if>>
                <label class="form-check-label" for="enabledCheck">
                    Kích hoạt tài khoản
                </label>
            </div>
        </div>
    </div>

    <hr/>
    <button class="btn btn-warning">
        <i class="fas fa-save"></i> ${isNew ? 'Tạo mới' : 'Lưu thay đổi'}
    </button>
    <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary">Hủy</a>
</form>

<script>
    // Toggle "Chi nhánh" field + required theo role STAFF/MANAGER
    (function () {
        const branchGroup  = document.getElementById('branchGroup');
        const branchSelect = document.getElementById('branchSelect');
        const roleBoxes    = document.querySelectorAll('.role-checkbox');

        function update() {
            let needsBranch = false;
            roleBoxes.forEach(cb => {
                if (cb.checked && (cb.dataset.rolename === 'ROLE_STAFF'
                                || cb.dataset.rolename === 'ROLE_MANAGER')) {
                    needsBranch = true;
                }
            });
            branchGroup.style.display = needsBranch ? 'block' : 'none';
            branchSelect.required     = needsBranch;
        }

        roleBoxes.forEach(cb => cb.addEventListener('change', update));
        update();
    })();
</script>