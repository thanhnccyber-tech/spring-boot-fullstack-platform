<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<title>Xác thực OTP - UTeTra</title>

<div class="row justify-content-center">
    <div class="col-md-5">
        <div class="card shadow">
            <div class="card-body p-4 text-center">
                <h3>📧 Xác thực OTP</h3>
                <p class="text-muted">Mã OTP đã được gửi tới <b>${email}</b>. Mã có hiệu lực trong 5 phút.</p>
                <c:if test="${error != null}"><div class="alert alert-danger">${error}</div></c:if>
                <c:if test="${success != null}"><div class="alert alert-success">${success}</div></c:if>
                <form:form action="${pageContext.request.contextPath}/otp/register/verify"
                           method="post" modelAttribute="otpRequest">
                    <form:hidden path="email"/>
                    <div class="mb-3">
                        <input type="text" name="code" class="form-control form-control-lg text-center"
                               maxlength="6" pattern="[0-9]{6}" required
                               placeholder="Nhập 6 chữ số OTP">
                    </div>
                    <button class="btn btn-warning w-100">Xác nhận</button>
                </form:form>
                <p class="mt-3">
                    <a href="${pageContext.request.contextPath}/otp/register/resend?email=${email}">
                        Gửi lại mã OTP
                    </a>
                </p>
            </div>
        </div>
    </div>
</div>