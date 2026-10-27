<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Đơn ${fn:escapeXml(order.orderCode)}</title>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:url var="mapUrl" value="https://www.google.com/maps/search/">
    <c:param name="api" value="1"/>
    <c:param name="query" value="${order.shippingAddress}"/>
</c:url>

<a href="${ctx}/shipper/dashboard" class="d-inline-flex align-items-center gap-2 mb-3 text-decoration-none"
   style="color:var(--sh-text-muted);font-size:.85rem;font-weight:600">
    <i class="fas fa-arrow-left"></i> Về danh sách đơn
</a>

<%-- ===== Header ===== --%>
<div class="sh-card sh-card-pad mb-3">
    <div class="d-flex justify-content-between align-items-start mb-2">
        <div>
            <div class="sh-section-label mb-0">Mã đơn</div>
            <div class="sh-order-code" style="font-size:1.25rem">#${fn:escapeXml(order.orderCode)}</div>
        </div>
        <c:choose>
            <c:when test="${order.status == 'PREPARING'}"><span class="sh-status preparing"><i class="fas fa-hourglass-half"></i>Chờ nhận</span></c:when>
            <c:when test="${order.status == 'READY'}"><span class="sh-status ready"><i class="fas fa-rotate"></i>Chờ giao lại</span></c:when>
            <c:when test="${order.status == 'DELIVERING'}"><span class="sh-status delivering"><i class="fas fa-motorcycle"></i>Đang giao</span></c:when>
            <c:when test="${order.status == 'COMPLETED'}"><span class="sh-status completed"><i class="fas fa-circle-check"></i>Hoàn thành</span></c:when>
            <c:when test="${order.status == 'FAILED'}"><span class="sh-status failed"><i class="fas fa-circle-xmark"></i>Thất bại</span></c:when>
            <c:otherwise><span class="sh-status pending">${fn:escapeXml(order.statusLabel)}</span></c:otherwise>
        </c:choose>
    </div>
    <div class="sh-small sh-muted">Đặt lúc ${fn:escapeXml(order.createdAtFormatted)}</div>

    <%-- Quick actions --%>
    <div class="d-flex gap-2 mt-3">
        <a href="tel:${fn:escapeXml(order.phone)}" class="sh-btn sh-btn-outline flex-fill">
            <i class="fas fa-phone"></i> Gọi khách
        </a>
        <a href="${fn:escapeXml(mapUrl)}" target="_blank" rel="noopener noreferrer"
           class="sh-btn sh-btn-outline flex-fill">
            <i class="fas fa-map-location-dot"></i> Bản đồ
        </a>
    </div>
</div>

<%-- ===== Info giao hàng ===== --%>
<div class="sh-card sh-card-pad mb-3">
    <div class="sh-section-label">
        <i class="fas fa-truck-fast"></i> Thông tin giao hàng
    </div>
    <div class="sh-info-row">
        <i class="fas fa-user"></i>
        <span>${fn:escapeXml(order.user != null ? order.user.fullName : 'Khách')}</span>
    </div>
    <div class="sh-info-row">
        <i class="fas fa-phone"></i>
        <a href="tel:${fn:escapeXml(order.phone)}" class="text-decoration-none" style="color:var(--sh-blue-600)">
            ${fn:escapeXml(order.phone)}
        </a>
    </div>
    <div class="sh-info-row">
        <i class="fas fa-location-dot"></i>
        <span>${fn:escapeXml(order.shippingAddress)}</span>
    </div>
    <c:if test="${order.branch != null}">
        <div class="sh-info-row">
            <i class="fas fa-store"></i>
            <span>Lấy hàng tại: <b>${fn:escapeXml(order.branch.name)}</b></span>
        </div>
    </c:if>
    <c:if test="${not empty order.deliveryTime and order.deliveryTime != 'ASAP'}">
        <div class="sh-info-row">
            <i class="fas fa-clock" style="color:var(--sh-amber)"></i>
            <span class="fw-semibold">Hẹn giờ: ${fn:escapeXml(order.deliveryTime)}</span>
        </div>
    </c:if>
    <c:if test="${order.distanceKm != null}">
        <div class="sh-info-row">
            <i class="fas fa-route"></i>
            <span>Cách cửa hàng <b><fmt:formatNumber value="${order.distanceKm}" maxFractionDigits="1"/> km</b></span>
        </div>
    </c:if>
    <c:if test="${not empty order.note}">
        <div class="sh-info-row">
            <i class="fas fa-note-sticky" style="color:var(--sh-amber)"></i>
            <span>${fn:escapeXml(order.note)}</span>
        </div>
    </c:if>
</div>

<%-- ===== Thanh toán ===== --%>
<div class="sh-card sh-card-pad mb-3">
    <div class="sh-section-label">
        <i class="fas fa-wallet"></i> Thanh toán
    </div>
    <div class="d-flex justify-content-between align-items-center p-3 rounded" style="background:var(--sh-bg)">
        <div>
            <div class="sh-section-label mb-0">Tổng cộng</div>
            <div class="fw-bold" style="font-size:1.45rem;letter-spacing:-.02em;color:var(--sh-red)">
                <fmt:formatNumber value="${order.totalAmount}" pattern="#,##0"/> ₫
            </div>
        </div>
        <c:choose>
            <c:when test="${order.paymentMethod == 'COD' and order.paymentStatus != 'PAID'}">
                <span class="sh-status failed"><i class="fas fa-money-bill-wave"></i>Thu COD</span>
            </c:when>
            <c:when test="${order.paymentStatus == 'PAID'}">
                <span class="sh-status completed"><i class="fas fa-circle-check"></i>Đã trả</span>
            </c:when>
            <c:otherwise>
                <span class="sh-status pending">${fn:escapeXml(order.paymentMethod)}</span>
            </c:otherwise>
        </c:choose>
    </div>
    <div class="sh-small sh-muted mt-2">
        <c:if test="${order.paymentMethod == 'COD' and order.paymentStatus != 'PAID'}">
            <i class="fas fa-circle-info"></i> Vui lòng thu đủ <b><fmt:formatNumber value="${order.totalAmount}" pattern="#,##0"/> ₫</b> từ khách.
        </c:if>
        <c:if test="${order.paymentStatus == 'PAID'}">
            <i class="fas fa-circle-check" style="color:var(--sh-green)"></i> Khách đã thanh toán trước — không thu thêm tiền.
        </c:if>
        <c:if test="${order.paymentMethod != 'COD' and order.paymentStatus != 'PAID'}">
            <i class="fas fa-circle-info"></i> Đơn chưa thanh toán (${fn:escapeXml(order.paymentMethod)}). Hỏi cửa hàng trước khi giao.
        </c:if>
    </div>
</div>

<%-- ===== Món trong đơn ===== --%>
<div class="sh-card sh-card-pad mb-3">
    <div class="sh-section-label">
        <i class="fas fa-mug-hot"></i> Món trong đơn
    </div>
    <c:forEach var="d" items="${order.details}">
        <div class="d-flex justify-content-between py-2" style="border-bottom:1px dashed var(--sh-border)">
            <div class="flex-grow-1">
                <div class="fw-semibold">${fn:escapeXml(d.product.name)}
                    <span class="sh-muted fw-normal">×${d.quantity}</span>
                </div>
                <div class="sh-small sh-muted">
                    <c:if test="${not empty d.size}">Size ${fn:escapeXml(d.size)}</c:if>
                    <c:if test="${not empty d.sugarLevel}"> · Đường ${fn:escapeXml(d.sugarLevel)}</c:if>
                    <c:if test="${not empty d.iceLevel}"> · Đá ${fn:escapeXml(d.iceLevel)}</c:if>
                </div>
                <c:if test="${not empty d.toppings}">
                    <div class="sh-small sh-muted">
                        Topping: <c:forEach var="t" items="${d.toppings}" varStatus="st">${fn:escapeXml(t.name)}<c:if test="${!st.last}">, </c:if></c:forEach>
                    </div>
                </c:if>
            </div>
            <div class="fw-semibold sh-nowrap"><fmt:formatNumber value="${d.subtotal}" pattern="#,##0"/> ₫</div>
        </div>
    </c:forEach>
</div>

<%-- ===== Lịch sử giao thất bại ===== --%>
<c:if test="${not empty deliveryFailures}">
    <div class="sh-card sh-card-pad mb-3" style="border-left:4px solid var(--sh-red)">
        <div class="sh-section-label" style="color:var(--sh-red-text)">
            <i class="fas fa-triangle-exclamation"></i> Lịch sử giao thất bại
        </div>
        <c:forEach var="f" items="${deliveryFailures}">
            <div class="py-2" style="border-bottom:1px dashed var(--sh-border)">
                <div class="sh-small sh-muted">${fn:escapeXml(f.failedAtFormatted)} · ${fn:escapeXml(f.shipper.fullName)}</div>
                <div class="sh-small">${fn:escapeXml(f.reason)}</div>
            </div>
        </c:forEach>
    </div>
</c:if>

<%-- ===== Sticky action bar ===== --%>
<c:if test="${order.status == 'PREPARING' or order.status == 'READY' or order.status == 'DELIVERING'}">
<div class="sh-sticky-bar">
    <div class="sh-sticky-bar-inner">

        <c:if test="${order.status == 'PREPARING' or order.status == 'READY'}">
            <form action="${ctx}/shipper/orders/${fn:escapeXml(order.id)}/accept" method="post"
                  onsubmit="return confirm('Nhận giao đơn này?')" class="m-0">
                <button class="sh-btn sh-btn-primary sh-btn-block" style="padding:14px">
                    <i class="fas fa-hand-pointer"></i>
                    ${order.status == 'READY' ? 'Nhận giao lại' : 'Nhận đơn'}
                </button>
            </form>
        </c:if>

        <c:if test="${order.status == 'DELIVERING'}">
            <div class="d-flex gap-2">
                <form action="${ctx}/shipper/orders/${fn:escapeXml(order.id)}/complete" method="post"
                      onsubmit="return confirm('Xác nhận đã giao thành công cho khách?')"
                      class="flex-grow-1 m-0">
                    <button class="sh-btn sh-btn-success sh-btn-block" style="padding:14px">
                        <i class="fas fa-circle-check"></i> Đã giao thành công
                    </button>
                </form>
                <button type="button" class="sh-btn sh-btn-outline" style="padding:14px 16px"
                        onclick="document.getElementById('failBox').style.display='block'; this.style.display='none';"
                        title="Báo giao thất bại">
                    <i class="fas fa-circle-xmark" style="color:var(--sh-red)"></i>
                </button>
            </div>

            <div id="failBox" style="display:none;margin-top:8px">
                <form action="${ctx}/shipper/orders/${fn:escapeXml(order.id)}/fail" method="post"
                      onsubmit="return confirm('Đánh dấu đơn giao thất bại?')" class="m-0">
                    <textarea name="reason" class="form-control mb-2" rows="2" maxlength="500" required
                              placeholder="Lý do (ví dụ: khách không nghe máy, sai địa chỉ...)"
                              style="border-radius:10px;font-size:.88rem"></textarea>
                    <div class="d-flex gap-2">
                        <button type="submit" class="sh-btn sh-btn-danger flex-grow-1">
                            <i class="fas fa-ban"></i> Báo thất bại
                        </button>
                        <button type="button" class="sh-btn sh-btn-outline"
                                onclick="document.getElementById('failBox').style.display='none'; document.querySelectorAll('.sh-sticky-bar .sh-btn-outline')[0].style.display='';">
                            Hủy
                        </button>
                    </div>
                </form>
            </div>
        </c:if>

    </div>
</div>
</c:if>