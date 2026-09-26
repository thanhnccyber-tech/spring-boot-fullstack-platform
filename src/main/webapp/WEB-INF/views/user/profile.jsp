<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<title>Thông tin cá nhân</title>

<c:if test="${success != null}"><div class="alert alert-success">${success}</div></c:if>
<c:if test="${error != null}"><div class="alert alert-danger">${error}</div></c:if>

<div class="row">
    <div class="col-md-4 text-center">
        <img src="${user.avatarUrl != null ? user.avatarUrl : 'https://via.placeholder.com/200'}"
             class="rounded-circle mb-3" style="width:200px;height:200px;object-fit:cover;">
        <h5>${user.fullName}</h5>
        <p class="text-muted">${user.email}</p>
    </div>
    <div class="col-md-8">
        <div class="card mb-3">
            <div class="card-header">Cập nhật thông tin</div>
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/profile/update" method="post" enctype="multipart/form-data">
                    <div class="mb-3">
                        <label>Họ tên</label>
                        <input type="text" name="fullName" value="${user.fullName}" class="form-control">
                    </div>
                    <div class="mb-3">
                        <label>Điện thoại</label>
                        <input type="text" name="phone" value="${user.phone}" class="form-control">
                    </div>
                    <div class="mb-3">
                        <label>Địa chỉ</label>
                        <input type="text" name="address" value="${user.address}" class="form-control">
                    </div>
                    <div class="mb-3">
                        <label>Avatar (upload lên Cloudinary)</label>
                        <input type="file" name="avatar" accept="image/*" class="form-control">
                    </div>
                    <button class="btn btn-warning">Cập nhật</button>
                </form>
            </div>
        </div>

        <div class="card">
            <div class="card-header">Đổi mật khẩu</div>
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/profile/change-password" method="post">
                    <div class="mb-3">
                        <label>Mật khẩu cũ</label>
                        <input type="password" name="oldPassword" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label>Mật khẩu mới</label>
                        <input type="password" name="newPassword" class="form-control" required minlength="6">
                    </div>
                    <button class="btn btn-danger">Đổi mật khẩu</button>
                </form>
            </div>
        </div>
    </div>
</div>