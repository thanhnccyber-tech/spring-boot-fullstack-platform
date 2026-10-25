<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Thanh toán</title>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<fmt:formatNumber value="${subtotal}" pattern="0" var="subRaw"/>
<fmt:formatNumber value="${shipFeeDelivery}" pattern="0" var="feeRaw"/>

<h3 class="mb-3">🧾 Thanh toán</h3>

<%-- Form đặt hàng rỗng: các ô nhập ở cả 2 cột gắn vào đây bằng thuộc tính form="placeForm" --%>
<form id="placeForm" action="${ctx}/order/place" method="post"></form>

<div class="row g-4">
    <!-- ================= CỘT TRÁI: thông tin nhận hàng ================= -->
    <div class="col-lg-7">
        <div class="checkout-box mb-3">
            <h5 class="mb-3"><i class="fas fa-location-dot"></i> Cách nhận hàng</h5>

            <div class="btn-group w-100 recv-choice mb-3" role="group">
                <input type="radio" class="btn-check" name="deliveryMethod" id="mDel" value="DELIVERY" form="placeForm"
                       <c:if test="${recvMethod != 'PICKUP'}">checked</c:if>>
                <label class="btn btn-outline-secondary" for="mDel"><i class="fas fa-truck-fast"></i> Giao hàng</label>
                <input type="radio" class="btn-check" name="deliveryMethod" id="mPick" value="PICKUP" form="placeForm"
                       <c:if test="${recvMethod == 'PICKUP'}">checked</c:if>>
                <label class="btn btn-outline-secondary" for="mPick"><i class="fas fa-store"></i> Đến lấy tại cửa hàng</label>
            </div>

            <div class="mb-3">
                <label class="form-label" id="branchLabel">Cửa hàng phục vụ</label>
                <select name="branchId" class="form-select" required form="placeForm">
                    <c:forEach items="${branches}" var="b" varStatus="st">
                        <option value="${fn:escapeXml(b.id)}"
                                <c:if test="${(not empty recvBranchId and recvBranchId == b.id)}">selected</c:if>>${fn:escapeXml(b.name)} - ${fn:escapeXml(b.address)}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="mb-3" id="addrBox">
                <label class="form-label">Địa chỉ nhận hàng</label>
                <textarea name="shippingAddress" id="addr" rows="2" maxlength="500" class="form-control" form="placeForm"
                          placeholder="Số nhà, đường, phường/xã, quận/huyện">${fn:escapeXml(recvAddress)}</textarea>
            </div>

            <div class="mb-3">
                <label class="form-label">Số điện thoại người nhận</label>
                <input type="tel" name="phone" class="form-control" required maxlength="15" form="placeForm"
                       pattern="(0|\+84)[0-9]{9,10}" placeholder="0912345678" value="${fn:escapeXml(userPhone)}">
            </div>

            <div class="mb-3">
                <label class="form-label">Thời gian nhận hàng</label>
                <div class="d-flex gap-3 align-items-center flex-wrap">
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="timeMode" id="tAsap" value="ASAP" checked>
                        <label class="form-check-label" for="tAsap">Càng sớm càng tốt</label>
                    </div>
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="timeMode" id="tLater" value="LATER">
                        <label class="form-check-label" for="tLater">Hẹn giờ</label>
                    </div>
                    <input type="datetime-local" id="tSlot" class="form-control form-control-sm" style="max-width:220px" disabled>
                </div>
                <input type="hidden" name="deliveryTime" id="deliveryTime" value="ASAP" form="placeForm">
            </div>

            <div class="mb-0">
                <label class="form-label">Ghi chú cho cửa hàng</label>
                <textarea name="note" rows="2" maxlength="1000" class="form-control" form="placeForm"
                          placeholder="Ví dụ: ít đá, gọi trước khi giao..."></textarea>
            </div>
        </div>

        <div class="checkout-box">
            <h5 class="mb-2"><i class="fas fa-file-invoice"></i> Thông tin xuất hoá đơn VAT</h5>
            <div class="form-check mb-2">
                <input class="form-check-input" type="checkbox" name="vat" value="true" id="vat" form="placeForm">
                <label class="form-check-label" for="vat">Tôi cần xuất hoá đơn VAT</label>
            </div>
            <div id="vatBox" class="row g-2" style="display:none">
                <div class="col-12"><input name="vatCompany" class="form-control" maxlength="200" placeholder="Tên công ty" form="placeForm"></div>
                <div class="col-md-6"><input name="vatTaxCode" class="form-control" maxlength="14" placeholder="Mã số thuế" form="placeForm"></div>
                <div class="col-md-6"><input type="email" name="vatEmail" class="form-control" maxlength="150" placeholder="Email nhận hoá đơn" form="placeForm"></div>
            </div>
            <div class="small text-muted mt-2">Hoá đơn điện tử sẽ được gửi qua email bạn nhập. Cần hỗ trợ gọi hotline 1900 1234.</div>
        </div>
    </div>

    <!-- ================= CỘT PHẢI: món đã chọn + thanh toán ================= -->
    <div class="col-lg-5">
        <div class="checkout-box mb-3">
            <h5 class="mb-2"><i class="fas fa-mug-hot"></i> Món đã chọn</h5>
            <c:forEach items="${cart}" var="item" varStatus="st">
                <div class="cart-line">
                    <c:if test="${not empty item.imageUrl}"><img src="${fn:escapeXml(item.imageUrl)}" alt=""></c:if>
                    <div class="flex-grow-1">
                        <div class="fw-bold">${fn:escapeXml(item.productName)}</div>
                        <div class="small text-muted">
                            <c:if test="${not empty item.sizeName}">Size: ${fn:escapeXml(item.sizeName)}</c:if>
                            <c:if test="${not empty item.iceName}">· Đá: ${fn:escapeXml(item.iceName)}</c:if>
                            <c:if test="${not empty item.sugarName}">· Ngọt: ${fn:escapeXml(item.sugarName)}</c:if>
                            <c:if test="${not empty item.toppingNames}">· Topping: <c:forEach items="${item.toppingNames}" var="tn" varStatus="ts">${fn:escapeXml(tn)}<c:if test="${!ts.last}">, </c:if></c:forEach></c:if>
                        </div>
                        <div class="d-flex justify-content-between align-items-center mt-1">
                            <span class="text-success fw-bold"><fmt:formatNumber value="${item.subtotal}" pattern="#,##0"/> ₫</span>
                            <div class="d-flex align-items-center gap-1">
                                <form action="${ctx}/cart/update" method="post" class="d-inline">
                                    <input type="hidden" name="index" value="${fn:escapeXml(st.index)}">
                                    <input type="hidden" name="back" value="/order/checkout">
                                    <input type="hidden" name="quantity" value="${fn:escapeXml(item.quantity - 1)}">
                                    <button class="qty-btn" aria-label="Giảm">−</button>
                                </form>
                                <span class="px-2">${fn:escapeXml(item.quantity)}</span>
                                <form action="${ctx}/cart/update" method="post" class="d-inline">
                                    <input type="hidden" name="index" value="${fn:escapeXml(st.index)}">
                                    <input type="hidden" name="back" value="/order/checkout">
                                    <input type="hidden" name="quantity" value="${fn:escapeXml(item.quantity + 1)}">
                                    <button class="qty-btn" aria-label="Tăng">+</button>
                                </form>
                                <form action="${ctx}/cart/remove" method="post" class="d-inline ms-2">
                                    <input type="hidden" name="index" value="${fn:escapeXml(st.index)}">
                                    <input type="hidden" name="back" value="/order/checkout">
                                    <button class="btn btn-link text-danger p-0" aria-label="Xoá món"><i class="fas fa-trash"></i></button>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>

        <div class="checkout-box mb-3">
            <h5 class="mb-2">Thông tin thanh toán</h5>
            <div class="input-group mb-1">
                <input type="text" id="voucherInput" class="form-control text-uppercase" maxlength="30" placeholder="Mã giảm giá (ví dụ WELCOME10)">
                <button type="button" class="btn btn-outline-dark" id="voucherBtn">Áp dụng</button>
            </div>
            <div class="small mb-2" id="voucherMsg"></div>
            <input type="hidden" name="voucherCode" id="voucherCode" value="" form="placeForm">

            <div class="sum-row"><span>Tổng tiền tạm tính</span><span id="sSub"><fmt:formatNumber value="${subtotal}" pattern="#,##0"/> ₫</span></div>
            <div class="sum-row"><span>Phí vận chuyển</span><span id="sFee"></span></div>
            <div class="sum-row"><span>Mã giảm giá</span><span id="sDis">0 ₫</span></div>
            <div class="sum-row border-0"><b>Tổng tiền</b><span class="sum-total" id="sTotal"></span></div>
            <c:if test="${freeShipFrom > 0}">
                <div class="small text-muted">Miễn phí giao hàng cho đơn từ <fmt:formatNumber value="${freeShipFrom}" pattern="#,##0"/> ₫.</div>
            </c:if>
        </div>

        <div class="checkout-box">
            <h5 class="mb-2">Phương thức thanh toán</h5>
            <div class="form-check mb-1" id="payCodRow">
                <input class="form-check-input" type="radio" name="paymentMethod" id="pCod" value="COD" form="placeForm" checked>
                <label class="form-check-label" for="pCod">Thanh toán khi nhận hàng (COD)</label>
            </div>
            <div class="form-check mb-1" id="payBankRow">
                <input class="form-check-input" type="radio" name="paymentMethod" id="pBank" value="BANK" form="placeForm">
                <label class="form-check-label" for="pBank">Chuyển khoản ngân hàng <span class="small text-muted">(cửa hàng sẽ liên hệ xác nhận)</span></label>
            </div>
            <div class="form-check mb-3" id="payCashRow">
                <input class="form-check-input" type="radio" name="paymentMethod" id="pCash" value="CASH" form="placeForm">
                <label class="form-check-label" for="pCash">Tiền mặt tại quầy <span class="small text-muted">(khi đến lấy)</span></label>
            </div>

            <div class="form-check mb-3">
                <input class="form-check-input" type="checkbox" name="agreeTerms" value="true" id="agree" form="placeForm">
                <label class="form-check-label small" for="agree">Tôi đã đọc, hiểu và đồng ý với các
                    <a href="${ctx}/hoi-vien#dieu-khoan" target="_blank">điều khoản, điều kiện và chính sách</a> liên quan.</label>
            </div>
            <button type="submit" form="placeForm" id="payBtn" class="btn btn-warning btn-lg w-100 fw-bold" disabled>TIẾN HÀNH ĐẶT HÀNG</button>
        </div>
    </div>
</div>

<script>
(function () {
    var SUB = ${subRaw};
    var FEE_DELIVERY = ${feeRaw};
    var discount = 0;
    function money(n) { return Math.round(n).toLocaleString('vi-VN') + ' ₫'; }
    function $(id) { return document.getElementById(id); }
    function method() { return $('mPick').checked ? 'PICKUP' : 'DELIVERY'; }

    function refresh() {
        var pickup = method() === 'PICKUP';
        var fee = pickup ? 0 : FEE_DELIVERY;
        $('sFee').textContent = pickup ? 'Miễn phí (đến lấy)' : money(fee);
        $('sDis').textContent = discount > 0 ? '− ' + money(discount) : '0 ₫';
        $('sTotal').textContent = money(Math.max(0, SUB - discount) + fee);

        // Giao hàng cần địa chỉ; đến lấy thì không
        $('addrBox').style.display = pickup ? 'none' : '';
        $('addr').required = !pickup;
        $('branchLabel').textContent = pickup ? 'Cửa hàng bạn sẽ đến lấy' : 'Cửa hàng phục vụ';

        // COD chỉ khi giao tận nơi, tiền mặt chỉ khi đến lấy
        $('pCod').disabled = pickup;  $('payCodRow').style.display = pickup ? 'none' : '';
        $('pCash').disabled = !pickup; $('payCashRow').style.display = pickup ? '' : 'none';
        var cur = document.querySelector('input[name=paymentMethod]:checked');
        if (!cur || cur.disabled) { (pickup ? $('pCash') : $('pCod')).checked = true; }
    }
    document.querySelectorAll('input[name=deliveryMethod]').forEach(function (r) { r.addEventListener('change', refresh); });

    // Hoá đơn VAT
    $('vat').addEventListener('change', function () {
        $('vatBox').style.display = this.checked ? '' : 'none';
        $('vatBox').querySelectorAll('input').forEach(function (i) { i.required = $('vat').checked; });
    });

    // Thời gian nhận hàng
    function pad(n) { return String(n).padStart(2, '0'); }
    function toLocalInput(d) { return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + 'T' + pad(d.getHours()) + ':' + pad(d.getMinutes()); }
    var slot = $('tSlot');
    slot.min = toLocalInput(new Date(Date.now() + 25 * 60000));
    slot.max = toLocalInput(new Date(Date.now() + 3 * 24 * 3600000));
    document.querySelectorAll('input[name=timeMode]').forEach(function (r) {
        r.addEventListener('change', function () {
            var later = $('tLater').checked;
            slot.disabled = !later; slot.required = later;
            $('deliveryTime').value = later ? slot.value : 'ASAP';
        });
    });
    slot.addEventListener('input', function () { $('deliveryTime').value = slot.value; });

    // Mã giảm giá (server kiểm tra; lúc đặt hàng server tính lại một lần nữa)
    $('voucherBtn').addEventListener('click', function () {
        var code = $('voucherInput').value.trim();
        var msg = $('voucherMsg');
        if (!code) { msg.className = 'small mb-2 text-danger'; msg.textContent = 'Vui lòng nhập mã giảm giá.'; return; }
        fetch('${ctx}/order/voucher?code=' + encodeURIComponent(code) + '&method=' + method(), { credentials: 'same-origin' })
            .then(function (r) { return r.json(); })
            .then(function (d) {
                msg.className = 'small mb-2 ' + (d.valid ? 'text-success' : 'text-danger');
                msg.textContent = d.message;
                discount = d.valid ? Number(d.discount) : 0;
                $('voucherCode').value = d.valid ? d.code : '';
                refresh();
            })
            .catch(function () { msg.className = 'small mb-2 text-danger'; msg.textContent = 'Không kiểm tra được mã, thử lại sau.'; });
    });

    // Chỉ cho bấm đặt hàng khi đã đồng ý điều khoản
    $('agree').addEventListener('change', function () { $('payBtn').disabled = !this.checked; });

    refresh();
})();
</script>
