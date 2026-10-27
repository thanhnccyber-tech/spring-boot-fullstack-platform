<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Thông tin cá nhân</title>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<div class="sh-page-title">
    <i class="fas fa-user-circle"></i> Thông tin cá nhân
</div>

<%-- ===== Hero: avatar + tên + vai trò + chi nhánh ===== --%>
<div class="sh-card sh-card-pad sh-profile-hero mb-3">
    <c:choose>
        <c:when test="${not empty me.avatarUrl}">
            <img src="${fn:escapeXml(me.avatarUrl)}" alt="" class="sh-profile-avatar">
        </c:when>
        <c:otherwise>
            <div class="sh-profile-avatar sh-profile-avatar-fallback">
                ${fn:escapeXml(fn:substring(me.fullName, 0, 1))}
            </div>
        </c:otherwise>
    </c:choose>
    <div class="sh-profile-hero-info">
        <div class="sh-profile-name">${fn:escapeXml(me.fullName)}</div>
        <div class="sh-profile-email">${fn:escapeXml(me.email)}</div>
        <div class="sh-chips mt-2">
            <span class="sh-chip blue"><i class="fas fa-motorcycle"></i> Shipper</span>
            <c:if test="${me.branch != null}">
                <span class="sh-chip green"><i class="fas fa-store"></i> ${fn:escapeXml(me.branch.name)}</span>
            </c:if>
        </div>
    </div>
</div>

<%-- ===== Cập nhật thông tin ===== --%>
<div class="sh-card sh-card-pad mb-3">
    <div class="sh-section-label"><i class="fas fa-id-card"></i> Cập nhật thông tin</div>
    <form action="${ctx}/shipper/profile/update" method="post" enctype="multipart/form-data">
        <div class="mb-2">
            <label class="form-label sh-small fw-semibold">Họ và tên</label>
            <input type="text" name="fullName" class="form-control" required maxlength="100"
                   value="${fn:escapeXml(me.fullName)}">
        </div>
        <div class="mb-2">
            <label class="form-label sh-small fw-semibold">Số điện thoại</label>
            <input type="tel" name="phone" class="form-control" maxlength="15"
                   value="${fn:escapeXml(me.phone)}">
        </div>
        <div class="mb-2">
            <label class="form-label sh-small fw-semibold">Địa chỉ</label>
            <input type="text" name="address" class="form-control" maxlength="255"
                   value="${fn:escapeXml(me.address)}">
        </div>
        <div class="mb-2">
            <label class="form-label sh-small fw-semibold">Email (không đổi được)</label>
            <input type="email" class="form-control" value="${fn:escapeXml(me.email)}" disabled>
        </div>
        <div class="mb-3">
            <label class="form-label sh-small fw-semibold">Ảnh đại diện</label>
            <input type="file" name="avatar" id="avatarInput" class="form-control"
                   accept="image/jpeg,image/png,image/gif,image/webp">
            <small class="text-muted sh-small">JPG, PNG, GIF, WEBP. Tối đa 5 MB.</small>
            <small id="avatarError" class="text-danger sh-small d-none d-block"></small>
        </div>
        <button class="sh-btn sh-btn-primary sh-btn-block">
            <i class="fas fa-save"></i> Lưu thay đổi
        </button>
    </form>
</div>

<%-- ===== Đổi mật khẩu ===== --%>
<div class="sh-card sh-card-pad">
    <div class="sh-section-label"><i class="fas fa-lock"></i> Đổi mật khẩu</div>
    <form action="${ctx}/shipper/profile/change-password" method="post">
        <div class="mb-2">
            <label class="form-label sh-small fw-semibold">Mật khẩu hiện tại</label>
            <input type="password" name="oldPassword" class="form-control" required
                   autocomplete="current-password">
        </div>
        <div class="mb-2">
            <label class="form-label sh-small fw-semibold">Mật khẩu mới</label>
            <input type="password" name="newPassword" class="form-control" required
                   minlength="8" autocomplete="new-password">
            <small class="text-muted sh-small">Tối thiểu 8 ký tự, có chữ cái và chữ số.</small>
        </div>
        <div class="mb-3">
            <label class="form-label sh-small fw-semibold">Xác nhận mật khẩu mới</label>
            <input type="password" name="confirmPassword" class="form-control" required
                   minlength="8" autocomplete="new-password">
        </div>
        <button class="sh-btn sh-btn-danger sh-btn-block">
            <i class="fas fa-key"></i> Đổi mật khẩu
        </button>
    </form>
</div>

<script>
(function () {
    var input = document.getElementById('avatarInput');
    var errorEl = document.getElementById('avatarError');
    var MAX = 5 * 1024 * 1024;
    var ALLOWED = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    if (!input) return;

    input.addEventListener('change', function () {
        errorEl.classList.add('d-none');
        errorEl.textContent = '';
        if (!input.files || !input.files.length) return;
        var f = input.files[0];
        if (f.size > MAX) {
            errorEl.textContent = 'Ảnh vượt quá 5 MB.';
            errorEl.classList.remove('d-none'); input.value = ''; return;
        }
        if (ALLOWED.indexOf(f.type.toLowerCase()) === -1) {
            errorEl.textContent = 'Chỉ chấp nhận JPG, PNG, GIF hoặc WEBP.';
            errorEl.classList.remove('d-none'); input.value = ''; return;
        }
        var reader = new FileReader();
        reader.onloadend = function (e) {
            var arr = new Uint8Array(e.target.result).subarray(0, 12);
            var hex = '';
            for (var i = 0; i < arr.length; i++) hex += arr[i].toString(16).padStart(2, '0');
            var ok = hex.startsWith('ffd8ff')
                  || hex.startsWith('89504e470d0a1a0a')
                  || hex.startsWith('47494638')
                  || (hex.startsWith('52494646') && hex.substring(16, 24) === '57454250');
            if (!ok) {
                errorEl.textContent = 'Nội dung tệp không phải là ảnh hợp lệ.';
                errorEl.classList.remove('d-none');
                input.value = '';
            }
        };
        reader.readAsArrayBuffer(f.slice(0, 12));
    });
})();
</script>