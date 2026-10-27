<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Thông tin cá nhân</title>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="small mb-3"><a href="${ctx}/">Trang chủ</a> / Tài khoản</nav>

<div class="row g-4">
    <div class="col-lg-4 col-xl-3">
        <jsp:include page="/WEB-INF/views/user/_account-menu.jsp"><jsp:param name="active" value="profile"/></jsp:include>
    </div>

    <!-- ===== Nội dung (bên phải) ===== -->
    <div class="col-lg-8 col-xl-9">
        <c:if test="${success != null}"><div class="alert alert-success">${fn:escapeXml(success)}</div></c:if>
        <c:if test="${error != null}"><div class="alert alert-danger">${fn:escapeXml(error)}</div></c:if>

        <div class="checkout-box mb-3">
            <h3 class="mb-4" style="color:#956542;font-weight:700">Thông tin cá nhân</h3>
            <form action="${ctx}/profile/update" method="post" enctype="multipart/form-data" class="row g-3">
                <div class="col-md-6">
                    <label class="form-label small mb-1">Họ &amp; tên</label>
                    <input type="text" name="fullName" class="form-control" required maxlength="100"
                           value="${fn:escapeXml(not empty formFullName ? formFullName : user.fullName)}">
                </div>
                <div class="col-md-6">
                    <label class="form-label small mb-1">Số điện thoại</label>
                    <input type="text" name="phone" class="form-control" maxlength="15"
                           value="${fn:escapeXml(not empty formPhone ? formPhone : user.phone)}">
                </div>
                <div class="col-md-6">
                    <label class="form-label small mb-1">Email</label>
                    <input type="email" class="form-control" value="${fn:escapeXml(user.email)}" disabled>
                </div>
                <div class="col-md-6">
                    <label class="form-label small mb-1">Điểm tích luỹ</label>
                    <input type="text" class="form-control" value="${fn:escapeXml(user.points)} điểm" disabled>
                </div>
                <div class="col-12" id="dia-chi">
                    <label class="form-label small mb-1">Địa chỉ giao hàng mặc định</label>
                    <input type="text" name="address" class="form-control" maxlength="255"
                           placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành"
                           value="${fn:escapeXml(not empty formAddress ? formAddress : user.address)}">
                    <div class="form-text">Địa chỉ này được tự điền khi bạn thanh toán.</div>
                </div>
                <div class="col-12">
                    <label class="form-label small mb-1">Ảnh đại diện</label>
                    <input type="file" name="avatar" id="avatarInput"
                           accept="image/jpeg,image/png,image/gif,image/webp" class="form-control">
                    <small class="text-muted d-block mt-1">Chỉ chấp nhận JPG, PNG, GIF, WEBP. Tối đa 5 MB.</small>
                    <small id="avatarError" class="text-danger d-none"></small>
                </div>
                <div class="col-12"><button class="btn btn-warning px-4 fw-bold">Cập nhật</button></div>
            </form>
        </div>

        <div class="checkout-box">
            <h5 class="mb-3">Đổi mật khẩu</h5>
            <form action="${ctx}/profile/change-password" method="post" class="row g-3">
                <div class="col-md-6">
                    <label class="form-label small mb-1">Mật khẩu cũ</label>
                    <input type="password" name="oldPassword" class="form-control" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label small mb-1">Mật khẩu mới</label>
                    <input type="password" name="newPassword" class="form-control" required minlength="6">
                </div>
                <div class="col-12"><button class="btn btn-danger">Đổi mật khẩu</button></div>
            </form>
        </div>
    </div>
</div>

<script>
(function () {
    const input = document.getElementById('avatarInput');
    const errorEl = document.getElementById('avatarError');
    const MAX = 5 * 1024 * 1024;
    const ALLOWED = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];

    function showError(msg) {
        errorEl.textContent = msg;
        errorEl.classList.remove('d-none');
        input.value = '';
    }
    function clearError() {
        errorEl.textContent = '';
        errorEl.classList.add('d-none');
    }

    input.addEventListener('change', function () {
        clearError();
        if (!input.files || input.files.length === 0) return;
        const f = input.files[0];

        if (f.size > MAX) {
            showError('Ảnh không được vượt quá 5 MB (hiện tại: '
                      + (f.size / (1024 * 1024)).toFixed(2) + ' MB).');
            return;
        }
        if (ALLOWED.indexOf(f.type.toLowerCase()) === -1) {
            showError('Chỉ chấp nhận ảnh JPG, PNG, GIF hoặc WEBP.');
            return;
        }
        const reader = new FileReader();
        reader.onloadend = function (e) {
            const arr = new Uint8Array(e.target.result).subarray(0, 12);
            let hex = '';
            for (let i = 0; i < arr.length; i++) {
                hex += arr[i].toString(16).padStart(2, '0');
            }
            const isJpeg = hex.startsWith('ffd8ff');
            const isPng  = hex.startsWith('89504e470d0a1a0a');
            const isGif  = hex.startsWith('47494638');
            const isWebp = hex.startsWith('52494646') && hex.substring(16, 24) === '57454250';
            if (!(isJpeg || isPng || isGif || isWebp)) {
                showError('Nội dung tệp không phải là ảnh hợp lệ.');
            }
        };
        reader.readAsArrayBuffer(f.slice(0, 12));
    });
})();
</script>