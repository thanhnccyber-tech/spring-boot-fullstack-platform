<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<title>Đặt lại mật khẩu - UTeTra</title>

<div class="row justify-content-center">
    <div class="col-md-5">
        <div class="card shadow">
            <div class="card-body p-4">
                <h3 class="text-center mb-3">🔒 Đặt mật khẩu mới</h3>
                <p class="text-muted text-center">
                    Tài khoản: <b>${resetRequest.email}</b>
                </p>

                <c:if test="${error != null}">
                    <div class="alert alert-danger">${error}</div>
                </c:if>

                <form action="${pageContext.request.contextPath}/reset-password" method="post">
                    <input type="hidden" name="email" value="${resetRequest.email}">
                    <div class="mb-3">
                        <label class="form-label">Mật khẩu mới (≥ 6 ký tự)</label>
                        <input type="password" name="newPassword" class="form-control"
                               minlength="6" required autofocus>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Xác nhận mật khẩu</label>
                        <input type="password" name="confirmPassword" class="form-control"
                               minlength="6" required>
                    </div>
                    <button class="btn btn-warning w-100">Đặt lại mật khẩu</button>
                </form>
            </div>
        </div>
    </div>
</div>