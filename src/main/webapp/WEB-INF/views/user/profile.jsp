<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Thông tin cá nhân</title>

<c:if test="${success != null}"><div class="alert alert-success">${fn:escapeXml(success)}</div></c:if>
<c:if test="${error != null}"><div class="alert alert-danger">${fn:escapeXml(error)}</div></c:if>

<div class="row">
    <div class="col-md-4 text-center">
        <img src="${fn:escapeXml(user.avatarUrl != null ? user.avatarUrl : 'https://via.placeholder.com/200')}"
             class="rounded-circle mb-3" style="width:200px;height:200px;object-fit:cover;">
        <h5>${fn:escapeXml(user.fullName)}</h5>
        <p class="text-muted">${fn:escapeXml(user.email)}</p>
    </div>
    <div class="col-md-8">
        <div class="card mb-3">
            <div class="card-header">Cập nhật thông tin</div>
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/profile/update"
                      method="post" enctype="multipart/form-data">
                    <div class="mb-3">
                        <label>Họ tên</label>
                        <input type="text" name="fullName" class="form-control"
                               value="${fn:escapeXml(not empty formFullName ? formFullName : user.fullName)}">
                    </div>
                    <div class="mb-3">
                        <label>Điện thoại</label>
                        <input type="text" name="phone" class="form-control"
                               value="${fn:escapeXml(not empty formPhone ? formPhone : user.phone)}">
                    </div>
                    <div class="mb-3">
                        <label>Địa chỉ</label>
                        <input type="text" name="address" class="form-control"
                               value="${fn:escapeXml(not empty formAddress ? formAddress : user.address)}">
                    </div>
                    <div class="mb-3">
                        <label>Avatar (upload lên Cloudinary)</label>
                        <input type="file" name="avatar" id="avatarInput"
                               accept="image/jpeg,image/png,image/gif,image/webp"
                               class="form-control">
                        <small class="text-muted d-block mt-1">
                            Chỉ chấp nhận JPG, PNG, GIF, WEBP. Tối đa 5 MB.
                        </small>
                        <small id="avatarError" class="text-danger d-none"></small>
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