<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Trung tâm trợ giúp</title>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="small mb-3"><a href="${ctx}/">Trang chủ</a> / <a href="${ctx}/profile">Tài khoản</a> / Trung tâm trợ giúp</nav>
<div class="row g-4">
    <div class="col-lg-4 col-xl-3">
        <jsp:include page="/WEB-INF/views/user/_account-menu.jsp"><jsp:param name="active" value="help"/></jsp:include>
    </div>
    <div class="col-lg-8 col-xl-9">

        <div class="checkout-box mb-3">
            <h4 class="mb-3" style="color:#956542;font-weight:700">Trung tâm trợ giúp</h4>
            <p class="mb-1"><i class="fas fa-phone text-success"></i> Hotline: <a href="tel:19001234"><b>1900 1234</b></a></p>
            <p class="mb-3"><i class="fas fa-store text-warning"></i> Địa chỉ và số điện thoại từng cửa hàng xem ở mục <a href="${ctx}/cua-hang">Cửa hàng</a>.</p>
            <div class="accordion" id="faq">
                <div class="accordion-item"><h2 class="accordion-header"><button class="accordion-button" data-bs-toggle="collapse" data-bs-target="#h1">Điểm hội viên được tính thế nào?</button></h2>
                    <div id="h1" class="accordion-collapse collapse show" data-bs-parent="#faq"><div class="accordion-body">Mỗi <b><fmt:formatNumber value="${pointUnit}" pattern="#,##0"/>đ</b> tiền hàng (sau giảm giá, không tính phí giao) = 1 điểm, làm tròn xuống. Điểm cộng một lần khi đơn hoàn thành.</div></div></div>
                <div class="accordion-item"><h2 class="accordion-header"><button class="accordion-button collapsed" data-bs-toggle="collapse" data-bs-target="#h2">Làm sao dùng mã giảm giá?</button></h2>
                    <div id="h2" class="accordion-collapse collapse" data-bs-parent="#faq"><div class="accordion-body">Vào mục <a href="${ctx}/account/vouchers">Ưu đãi của tôi</a> để lấy mã, nhập vào ô "Mã giảm giá" ở bước thanh toán rồi bấm Áp dụng.</div></div></div>
                <div class="accordion-item"><h2 class="accordion-header"><button class="accordion-button collapsed" data-bs-toggle="collapse" data-bs-target="#h3">Tôi muốn đổi địa chỉ giao hàng?</button></h2>
                    <div id="h3" class="accordion-collapse collapse" data-bs-parent="#faq"><div class="accordion-body">Lưu nhiều địa chỉ ở mục <a href="${ctx}/account/addresses">Số địa chỉ</a>; địa chỉ mặc định sẽ tự điền khi thanh toán.</div></div></div>
                <div class="accordion-item"><h2 class="accordion-header"><button class="accordion-button collapsed" data-bs-toggle="collapse" data-bs-target="#h4">Theo dõi đơn hàng ở đâu?</button></h2>
                    <div id="h4" class="accordion-collapse collapse" data-bs-parent="#faq"><div class="accordion-body">Mục <a href="${ctx}/order/history">Đơn hàng</a> hiển thị trạng thái theo thời gian thực. Bạn cũng nhận thông báo mỗi khi đơn thay đổi.</div></div></div>
            </div>
        </div>
        <div class="checkout-box" id="dieu-khoan">
            <h5>Điều khoản &amp; chính sách</h5>
            <ul class="small text-muted mb-0">
                <li>Đơn hàng được xác nhận sau khi cửa hàng tiếp nhận; thời gian giao có thể thay đổi tuỳ khoảng cách và giờ cao điểm.</li>
                <li>Mã giảm giá có điều kiện (đơn tối thiểu, hạn dùng, số lượt) và không quy đổi thành tiền mặt.</li>
                <li>Điểm hội viên chỉ được cộng cho đơn đặt khi đã đăng nhập tài khoản.</li>
                <li>Thông tin cá nhân chỉ dùng để xử lý đơn hàng và chăm sóc khách hàng.</li>
            </ul>
        </div>
    </div>
</div>
