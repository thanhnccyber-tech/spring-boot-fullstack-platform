<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<title>Sửa người dùng</title>

<form action="${pageContext.request.contextPath}/admin/users/save" method="post">
    <input type="hidden" name="id" value="${user.id}">

    <div class="mb-3">
        <label class="form-label">Email</label>
        <input type="email" class="form-control" value="${user.email}" disabled>
    </div>

    <div class="mb-3">
        <label class="form-label">Họ tên</label>
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
        <label class="form-label">Vai trò (RBAC)</label><br/>
        <c:forEach items="${roles}" var="r">
            <div class="form-check form-check-inline">
                <input class="form-check-input" type="checkbox" name="roleIds" value="${r.id}"
                       id="role-${r.id}"
                    <c:if test="${user.roles.contains(r)}">checked</c:if>>
                <label class="form-check-label" for="role-${r.id}">${r.name}</label>
            </div>
        </c:forEach>
    </div>

    <div class="mb-3">
        <label class="form-label">Chi nhánh làm việc</label>
        <select name="branchId" class="form-select">
            <option value="">-- Không gán --</option>
            <c:forEach items="${branches}" var="b">
                <option value="${b.id}"
                    <c:if test="${user.branch != null && user.branch.id == b.id}">selected</c:if>>
                    ${b.name} - ${b.address}
                </option>
            </c:forEach>
        </select>
    </div>

    <div class="mb-3">
        <label class="form-label">Đổi mật khẩu (để trống nếu không đổi)</label>
        <input type="password" name="newPassword" class="form-control" minlength="6">
    </div>

    <div class="form-check mb-3">
        <input type="checkbox" name="enabled" value="true" class="form-check-input"
               id="enabledCheck"
            <c:if test="${user.enabled}">checked</c:if>>
        <label class="form-check-label" for="enabledCheck">Kích hoạt tài khoản</label>
    </div>

    <button class="btn btn-warning">Lưu</button>
    <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary">Hủy</a>
</form>