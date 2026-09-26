<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<title>Đăng ký</title>

<div class="row justify-content-center">
    <div class="col-md-6">
        <div class="card shadow">
            <div class="card-body p-4">
                <h3 class="text-center">Đăng ký tài khoản</h3>
                <c:if test="${error != null}"><div class="alert alert-danger">${error}</div></c:if>
                <form:form action="${pageContext.request.contextPath}/auth/register" method="post"
                           modelAttribute="registerRequest">
                    <div class="mb-3">
                        <label>Họ tên</label>
                        <input type="text" name="fullName" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label>Email</label>
                        <input type="email" name="email" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label>Điện thoại</label>
                        <input type="text" name="phone" class="form-control">
                    </div>
                    <div class="mb-3">
                        <label>Mật khẩu (>= 6 ký tự)</label>
                        <input type="password" name="password" class="form-control" required minlength="6">
                    </div>
                    <button class="btn btn-warning w-100">Đăng ký</button>
                </form:form>
            </div>
        </div>
    </div>
</div>