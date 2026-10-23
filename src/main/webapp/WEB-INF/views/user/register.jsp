<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<title>Đăng ký - UTeTra</title>

<div class="row justify-content-center">
    <div class="col-md-6">
        <div class="card shadow">
            <div class="card-body p-4">
                <h3 class="text-center mb-3">📝 Đăng ký tài khoản</h3>

                <c:if test="${error != null}">
                    <div class="alert alert-danger">${error}</div>
                </c:if>
                <c:if test="${success != null}">
                    <div class="alert alert-success">${success}</div>
                </c:if>

                <form:form action="${pageContext.request.contextPath}/auth/register"
                           method="post" modelAttribute="registerRequest">
                    <div class="mb-3">
                        <label>Họ tên</label>
                        <form:input path="fullName" cssClass="form-control"
                                    cssErrorClass="form-control is-invalid"/>
                        <form:errors path="fullName" cssClass="text-danger small"/>
                    </div>
                    <div class="mb-3">
                        <label>Email</label>
                        <form:input path="email" type="email" cssClass="form-control"
                                    cssErrorClass="form-control is-invalid"/>
                        <form:errors path="email" cssClass="text-danger small"/>
                    </div>
                    <div class="mb-3">
                        <label>Điện thoại</label>
                        <form:input path="phone" cssClass="form-control"
                                    cssErrorClass="form-control is-invalid"/>
                        <form:errors path="phone" cssClass="text-danger small"/>
                    </div>
                    <div class="mb-3">
                        <label>Mật khẩu (≥ 6 ký tự)</label>
                        <form:password path="password" cssClass="form-control"
                                       cssErrorClass="form-control is-invalid" showPassword="false"/>
                        <form:errors path="password" cssClass="text-danger small"/>
                    </div>
                    <button class="btn btn-warning w-100">Đăng ký</button>
                </form:form>

                <p class="text-center mt-3">
                    Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
                </p>
            </div>
        </div>
    </div>
</div>