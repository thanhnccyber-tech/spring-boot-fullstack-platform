<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Quên mật khẩu - UTETra</title>

<div class="row justify-content-center">
    <div class="col-md-5">
        <div class="card shadow">
            <div class="card-body p-4">
                <h3 class="text-center mb-3"> Quên mật khẩu</h3>
                <p class="text-muted text-center">
                    Nhập email đã đăng ký. Chúng tôi sẽ gửi mã OTP để đặt lại mật khẩu.
                </p>

                <c:if test="${error != null}">
                    <div class="alert alert-danger">${fn:escapeXml(error)}</div>
                </c:if>

                <form action="${pageContext.request.contextPath}/forgot-password" method="post">
                    <div class="mb-3">
                        <label class="form-label">Email</label>
                        <input type="email" name="email" class="form-control"
                               value="${fn:escapeXml(forgotRequest.email)}" required autofocus>
                    </div>
                    <button class="btn btn-warning w-100">Gửi mã OTP</button>
                </form>

                <p class="text-center mt-3">
                    <a href="${pageContext.request.contextPath}/login">← Quay lại đăng nhập</a>
                </p>
            </div>
        </div>
    </div>
</div>