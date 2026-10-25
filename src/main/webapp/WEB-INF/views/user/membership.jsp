<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Hội viên</title>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<fmt:formatNumber value="${pointUnit}" pattern="#,##0" var="unitText"/>
<nav class="small mb-3"><a href="${ctx}/">Trang chủ</a> / Hội viên</nav>

<div class="row g-4">
    <div class="col-lg-5">
        <c:choose>
            <c:when test="${not empty me}">
                <div class="member-card mb-3">
                    <div class="text-white-50 small">Thẻ hội viên UTeTra</div>
                    <h4 class="mb-3">${fn:escapeXml(me.fullName)}</h4>
                    <div class="small text-white-50">Điểm tích luỹ</div>
                    <div class="points">${fn:escapeXml(me.points)}</div>
                    <div class="mt-3">
                        <div class="barcode-box"><svg id="barcode"></svg></div>
                        <div class="small text-white-50 mt-1">Mã hội viên: <b>${fn:escapeXml(memberCode)}</b> — đưa mã này cho nhân viên khi thanh toán tại quầy</div>
                    </div>
                </div>
                <script src="https://cdn.jsdelivr.net/npm/jsbarcode@3.11.6/dist/JsBarcode.all.min.js"></script>
                <script>
                    if (window.JsBarcode) {
                        JsBarcode('#barcode', '${fn:escapeXml(memberCode)}', { format: 'CODE128', height: 55, displayValue: false, margin: 4 });
                    }
                </script>
            </c:when>
            <c:otherwise>
                <div class="member-card mb-3">
                    <h4>Trở thành hội viên UTeTra</h4>
                    <p class="text-white-50">Đăng ký tài khoản miễn phí, đặt hàng và nhận điểm cho mỗi đơn hoàn thành.</p>
                    <a href="${ctx}/register" class="btn btn-warning me-2">Đăng ký</a>
                    <a href="${ctx}/login" class="btn btn-outline-light">Đăng nhập</a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="col-lg-7">
        <div class="checkout-box">
            <h4 class="mb-3">Câu hỏi thường gặp (FAQ)</h4>
            <div class="accordion" id="faq">
                <div class="accordion-item">
                    <h2 class="accordion-header"><button class="accordion-button" data-bs-toggle="collapse" data-bs-target="#f1">Làm sao để trở thành hội viên?</button></h2>
                    <div id="f1" class="accordion-collapse collapse show" data-bs-parent="#faq"><div class="accordion-body">Chỉ cần <a href="${ctx}/register">đăng ký tài khoản</a> và xác thực email. Mọi đơn đặt trên website sau đó sẽ được tích điểm tự động.</div></div>
                </div>
                <div class="accordion-item">
                    <h2 class="accordion-header"><button class="accordion-button collapsed" data-bs-toggle="collapse" data-bs-target="#f2">Điểm được tính như thế nào?</button></h2>
                    <div id="f2" class="accordion-collapse collapse" data-bs-parent="#faq"><div class="accordion-body">
                        Mỗi <b>${fn:escapeXml(unitText)}đ</b> tiền hàng (sau khi trừ mã giảm giá, không tính phí giao hàng) = <b>1 điểm</b>.
                        Điểm làm tròn xuống, phần dư không được bảo lưu. Ví dụ: hoá đơn 50.000đ = 5 điểm; hoá đơn 52.000đ cũng = 5 điểm.
                    </div></div>
                </div>
                <div class="accordion-item">
                    <h2 class="accordion-header"><button class="accordion-button collapsed" data-bs-toggle="collapse" data-bs-target="#f3">Khi nào điểm được cộng?</button></h2>
                    <div id="f3" class="accordion-collapse collapse" data-bs-parent="#faq"><div class="accordion-body">Điểm được cộng một lần khi đơn chuyển sang trạng thái <b>Hoàn thành</b>. Đơn bị huỷ hoặc giao thất bại không được cộng điểm. Bạn sẽ nhận thông báo khi được cộng.</div></div>
                </div>
                <div class="accordion-item">
                    <h2 class="accordion-header"><button class="accordion-button collapsed" data-bs-toggle="collapse" data-bs-target="#f4">Dùng mã giảm giá thế nào?</button></h2>
                    <div id="f4" class="accordion-collapse collapse" data-bs-parent="#faq"><div class="accordion-body">Nhập mã ở bước thanh toán rồi bấm “Áp dụng”. Xem các mã đang có tại <a href="${ctx}/khuyen-mai">trang Khuyến mãi</a>.</div></div>
                </div>
            </div>
        </div>

        <div class="checkout-box mt-3" id="dieu-khoan">
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
