<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Hội viên</title>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<fmt:formatNumber value="${pointUnit}" pattern="#,##0" var="unitText"/>
<nav class="small mb-3"><a href="${ctx}/">Trang chủ</a> / Hội viên</nav>

<div class="row g-4">
    <div class="col-lg-3">
        <div class="checkout-box p-3">
            <h6 class="text-uppercase" style="color:#956542">Hội viên</h6>
            <div class="list-group list-group-flush account-menu">
                <a class="list-group-item list-group-item-action" href="#faq"><i class="fas fa-circle-question"></i> Câu hỏi thường gặp (FAQ)</a>
                <a class="list-group-item list-group-item-action" href="#dieu-khoan"><i class="fas fa-file-contract"></i> Điều khoản và điều kiện chương trình hội viên</a>
            </div>
        </div>
    </div>

    <div class="col-lg-9">
        <div class="checkout-box" id="faq">
            <h2 class="text-center mb-4" style="color:#956542;font-weight:700">CÂU HỎI THƯỜNG GẶP (FAQ)</h2>
            <div class="accordion" id="faqList">
                <div class="accordion-item">
                    <h2 class="accordion-header"><button class="accordion-button" data-bs-toggle="collapse" data-bs-target="#f1">Làm sao để tôi có thể đăng ký thành viên UTeTra?</button></h2>
                    <div id="f1" class="accordion-collapse collapse show" data-bs-parent="#faqList"><div class="accordion-body">
                        Bạn <a href="${ctx}/register">đăng ký tài khoản</a> miễn phí và xác thực email là đã trở thành hội viên. Mọi đơn đặt sau khi đăng nhập sẽ được tích điểm tự động.
                    </div></div>
                </div>
                <div class="accordion-item">
                    <h2 class="accordion-header"><button class="accordion-button collapsed" data-bs-toggle="collapse" data-bs-target="#f2">Điểm được tính như thế nào?</button></h2>
                    <div id="f2" class="accordion-collapse collapse" data-bs-parent="#faqList"><div class="accordion-body">
                        Với mỗi <b>${fn:escapeXml(unitText)} đồng</b> chi trả cho thức uống, bánh và sản phẩm (sau khi trừ mã giảm giá, không tính phí giao hàng), bạn tích luỹ được <b>1 điểm</b>.<br>
                        Ví dụ: hoá đơn 50.000 đồng = 5 điểm.<br>
                        Điểm được làm tròn xuống và phần dư không được bảo lưu cho giao dịch sau. Ví dụ: hoá đơn 52.000 đồng = 5 điểm.
                    </div></div>
                </div>
                <div class="accordion-item">
                    <h2 class="accordion-header"><button class="accordion-button collapsed" data-bs-toggle="collapse" data-bs-target="#f3">Khi nào điểm được cộng?</button></h2>
                    <div id="f3" class="accordion-collapse collapse" data-bs-parent="#faqList"><div class="accordion-body">
                        Điểm được cộng một lần khi đơn chuyển sang trạng thái <b>Hoàn thành</b>. Đơn bị huỷ hoặc giao thất bại không được cộng điểm. Bạn nhận thông báo mỗi khi được cộng điểm.
                    </div></div>
                </div>
                <div class="accordion-item">
                    <h2 class="accordion-header"><button class="accordion-button collapsed" data-bs-toggle="collapse" data-bs-target="#f4">Xem điểm, hạng và thẻ hội viên ở đâu?</button></h2>
                    <div id="f4" class="accordion-collapse collapse" data-bs-parent="#faqList"><div class="accordion-body">
                        Đăng nhập, mở menu tài khoản và chọn <b>Khách hàng thành viên</b> để xem hạng, điểm tích luỹ, mã vạch hội viên và lịch sử cộng điểm.
                        <c:if test="${empty sessionScope.userId}"><div class="mt-2"><a class="btn btn-sm btn-warning" href="${ctx}/login">Đăng nhập</a> <a class="btn btn-sm btn-outline-dark" href="${ctx}/register">Đăng ký</a></div></c:if>
                    </div></div>
                </div>
                <div class="accordion-item">
                    <h2 class="accordion-header"><button class="accordion-button collapsed" data-bs-toggle="collapse" data-bs-target="#f5">Dùng mã giảm giá thế nào?</button></h2>
                    <div id="f5" class="accordion-collapse collapse" data-bs-parent="#faqList"><div class="accordion-body">
                        Nhập mã ở bước thanh toán rồi bấm "Áp dụng". Các chương trình đang có xem tại <a href="${ctx}/khuyen-mai">Khuyến mãi</a>.
                    </div></div>
                </div>
            </div>
        </div>

        <div class="checkout-box mt-3" id="dieu-khoan">
            <h5>Điều khoản và điều kiện chương trình hội viên</h5>
            <ul class="small text-muted mb-0">
                <li>Đơn hàng được xác nhận sau khi cửa hàng tiếp nhận; thời gian giao có thể thay đổi tuỳ khoảng cách và giờ cao điểm.</li>
                <li>Mã giảm giá có điều kiện (đơn tối thiểu, hạn dùng, số lượt) và không quy đổi thành tiền mặt.</li>
                <li>Điểm hội viên chỉ được cộng cho đơn đặt khi đã đăng nhập tài khoản.</li>
                <li>Thông tin cá nhân chỉ dùng để xử lý đơn hàng và chăm sóc khách hàng.</li>
            </ul>
        </div>
    </div>
</div>
