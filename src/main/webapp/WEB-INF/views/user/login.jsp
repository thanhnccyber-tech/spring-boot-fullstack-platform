<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Đăng nhập - UTeTra</title>

<div class="row justify-content-center">
    <div class="col-md-5">
        <div class="card shadow">
            <div class="card-body p-4">
                <h3 class="text-center mb-3">🔐 Đăng nhập</h3>

                <c:if test="${param.activated == 'true'}">
                    <div class="alert alert-success">
                        Kích hoạt tài khoản thành công! Hãy đăng nhập.
                    </div>
                </c:if>
                <c:if test="${param.reset == 'true'}">
                    <div class="alert alert-success">
                        Đặt lại mật khẩu thành công! Hãy đăng nhập bằng mật khẩu mới.
                    </div>
                </c:if>
                <c:if test="${error != null}">
                    <div class="alert alert-danger">${fn:escapeXml(error)}</div>
                </c:if>

                <form action="${pageContext.request.contextPath}/auth/login" method="post">
                    <div class="mb-3">
                        <label class="form-label">Email</label>
                        <input type="email" name="email" class="form-control"
                               value="${fn:escapeXml(loginRequest.email)}" required autofocus>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Mật khẩu</label>
                        <input type="password" name="password" class="form-control" required>
                    </div>
                    <div class="d-flex justify-content-between mb-2">
                        <a href="${pageContext.request.contextPath}/forgot-password"
                           class="small text-decoration-none">Quên mật khẩu?</a>
                    </div>
                    <button type="submit" class="btn btn-warning w-100">Đăng nhập</button>
                </form>

                <p class="text-center mt-3">
                    Chưa có tài khoản?
                    <a href="${pageContext.request.contextPath}/register">Đăng ký ngay</a>
                </p>
            </div>
        </div>
    </div>
</div>