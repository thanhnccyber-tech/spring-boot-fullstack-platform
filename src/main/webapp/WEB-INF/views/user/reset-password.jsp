<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Đặt lại mật khẩu - UTETra</title>

<div class="row justify-content-center">
    <div class="col-md-5">
        <div class="card shadow">
            <div class="card-body p-4">
                <h3 class="text-center mb-3"> Đặt mật khẩu mới</h3>
                <p class="text-muted text-center">
                    Tài khoản: <b>${fn:escapeXml(resetRequest.email)}</b>
                </p>

                <c:if test="${error != null}">
                    <div class="alert alert-danger">${fn:escapeXml(error)}</div>
                </c:if>

                <form action="${pageContext.request.contextPath}/reset-password" method="post">
                    <input type="hidden" name="email" value="${fn:escapeXml(resetRequest.email)}">
                    <input type="hidden" name="token" value="${fn:escapeXml(resetRequest.token)}">
                    <div class="mb-3">
                        <label class="form-label">Mật khẩu mới (≥ 8 ký tự, có chữ và số)</label>
                        <input type="password" name="newPassword" class="form-control"
                               minlength="8" maxlength="72" required autofocus>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Xác nhận mật khẩu</label>
                        <input type="password" name="confirmPassword" class="form-control"
                               minlength="8" maxlength="72" required>
                    </div>
                    <button class="btn btn-warning w-100">Đặt lại mật khẩu</button>
                </form>
            </div>
        </div>
    </div>
</div>