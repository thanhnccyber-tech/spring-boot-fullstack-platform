<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<title>Đăng nhập</title>

<div class="row justify-content-center">
    <div class="col-md-5">
        <div class="card shadow">
            <div class="card-body p-4">
                <h3 class="text-center mb-3">Đăng nhập</h3>
                <c:if test="${error != null}"><div class="alert alert-danger">${error}</div></c:if>
                <c:if test="${success != null}"><div class="alert alert-success">${success}</div></c:if>
                <form:form action="${pageContext.request.contextPath}/auth/login" method="post" modelAttribute="loginRequest">
                    <div class="mb-3">
                        <label class="form-label">Email</label>
                        <input type="email" name="email" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Mật khẩu</label>
                        <input type="password" name="password" class="form-control" required>
                    </div>
                    <button type="submit" class="btn btn-warning w-100">Đăng nhập</button>
                </form:form>
                <p class="text-center mt-3">
                    Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký</a>
                </p>
            </div>
        </div>
    </div>
</div>