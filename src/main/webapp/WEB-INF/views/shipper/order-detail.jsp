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

<a href="${ctx}/shipper/dashboard" class="btn btn-link px-0 mb-2">
    <i class="fas fa-arrow-left"></i> Về danh sách đơn
</a>

<div class="card order-card shadow-sm mb-3">
    <div class="card-body">
        <div class="d-flex justify-content-between align-items-start">
            <h5 class="mb-0">#${fn:escapeXml(order.orderCode)}</h5>
            <c:choose>
                <c:when test="${order.status == 'PREPARING'}"><span class="badge bg-info text-dark">Chờ nhận</span></c:when>
                <c:when test="${order.status == 'READY'}"><span class="badge bg-warning text-dark">Chờ giao lại</span></c:when>
                <c:when test="${order.status == 'DELIVERING'}"><span class="badge bg-primary">Đang giao</span></c:when>
                <c:when test="${order.status == 'COMPLETED'}"><span class="badge bg-success">Đã giao</span></c:when>
                <c:when test="${order.status == 'FAILED'}"><span class="badge bg-secondary">Giao thất bại</span></c:when>
                <c:otherwise><span class="badge bg-light text-dark">${fn:escapeXml(order.statusLabel)}</span></c:otherwise>
            </c:choose>
        </div>
        <div class="small text-muted mb-3">Đặt lúc ${fn:escapeXml(order.createdAtFormatted)}</div>

        <c:if test="${order.user != null}">
            <div><i class="fas fa-user text-muted me-1"></i> ${fn:escapeXml(order.user.fullName)}</div>
        </c:if>
        <div>
            <i class="fas fa-phone text-muted me-1"></i>
            <a href="tel:${fn:escapeXml(order.phone)}">${fn:escapeXml(order.phone)}</a>
        </div>
        <div>
            <i class="fas fa-location-dot text-muted me-1"></i> ${fn:escapeXml(order.shippingAddress)}
            <a href="${fn:escapeXml(mapUrl)}" target="_blank" rel="noopener noreferrer" class="small ms-1">
                <i class="fas fa-map"></i> Bản đồ
            </a>
        </div>
        <c:if test="${order.branch != null}">
            <div class="small text-muted mt-1">
                <i class="fas fa-store me-1"></i> Lấy hàng tại: ${fn:escapeXml(order.branch.name)}
            </div>
        </c:if>
        <c:if test="${not empty order.deliveryTime and order.deliveryTime != 'ASAP'}">
            <div class="small text-warning-emphasis mt-1">
                <i class="fas fa-clock me-1"></i> Hẹn giờ: ${fn:escapeXml(order.deliveryTime)}
            </div>
        </c:if>
        <c:if test="${not empty order.note}">
            <div class="small text-muted mt-1"><i class="fas fa-note-sticky me-1"></i> ${fn:escapeXml(order.note)}</div>
        </c:if>
        <c:if test="${not empty order.failedReason}">
            <div class="alert alert-secondary py-2 mt-3 mb-0">
                <b>Lý do lần giao thất bại gần nhất:</b> ${fn:escapeXml(order.failedReason)}
            </div>
        </c:if>
    </div>
</div>

<c:if test="${not empty deliveryFailures}">
    <div class="card order-card shadow-sm mb-3">
        <div class="card-body">
            <h6><i class="fas fa-clock-rotate-left text-secondary"></i> Lịch sử giao thất bại</h6>
            <c:forEach var="failure" items="${deliveryFailures}">
                <div class="border-top py-2">
                    <div class="small text-muted">${fn:escapeXml(failure.failedAtFormatted)}
                        · Shipper: ${fn:escapeXml(failure.shipper.fullName)}</div>
                    <div>${fn:escapeXml(failure.reason)}</div>
                </div>
            </c:forEach>
        </div>
    </div>
</c:if>

<%-- ========== DANH SÁCH MÓN ========== --%>
<div class="card order-card shadow-sm mb-3">
    <div class="card-body">
        <h6 class="mb-3"><i class="fas fa-mug-hot text-warning"></i> Món trong đơn</h6>
        <c:forEach var="d" items="${order.details}">
            <div class="d-flex justify-content-between border-bottom py-2">
                <div>
                    <div class="fw-semibold">${fn:escapeXml(d.product.name)} <span class="text-muted">x${d.quantity}</span></div>
                    <div class="small text-muted">
                        <c:if test="${not empty d.size}">Size ${fn:escapeXml(d.size)}</c:if>
                        <c:if test="${not empty d.sugarLevel}"> · Đường ${fn:escapeXml(d.sugarLevel)}</c:if>
                        <c:if test="${not empty d.iceLevel}"> · Đá ${fn:escapeXml(d.iceLevel)}</c:if>
                    </div>
                    <c:if test="${not empty d.toppings}">
                        <div class="small text-muted">
                            Topping:
                            <c:forEach var="t" items="${d.toppings}" varStatus="st">${fn:escapeXml(t.name)}<c:if test="${!st.last}">, </c:if></c:forEach>
                        </div>
                    </c:if>
                </div>
                <div class="text-nowrap"><fmt:formatNumber value="${d.subtotal}" pattern="#,##0"/> ₫</div>
            </div>
        </c:forEach>

        <div class="mt-3 small">
            <div class="d-flex justify-content-between"><span>Tiền hàng</span>
                <span><fmt:formatNumber value="${order.subtotalAmount}" pattern="#,##0"/> ₫</span></div>
            <c:if test="${order.discountAmount != null and order.discountAmount > 0}">
                <div class="d-flex justify-content-between"><span>Giảm giá</span>
                    <span>-<fmt:formatNumber value="${order.discountAmount}" pattern="#,##0"/> ₫</span></div>
            </c:if>
            <c:if test="${order.pointsDiscount != null and order.pointsDiscount > 0}">
                <div class="d-flex justify-content-between"><span>Dùng điểm</span>
                    <span>-<fmt:formatNumber value="${order.pointsDiscount}" pattern="#,##0"/> ₫</span></div>
            </c:if>
            <div class="d-flex justify-content-between"><span>Phí giao hàng</span>
                <span><fmt:formatNumber value="${order.shippingFee}" pattern="#,##0"/> ₫</span></div>
            <div class="d-flex justify-content-between fw-bold fs-6 mt-2">
                <span>Tổng cộng</span>
                <span><fmt:formatNumber value="${order.totalAmount}" pattern="#,##0"/> ₫</span>
            </div>
        </div>

        <div class="mt-3">
            <c:choose>
                <c:when test="${order.paymentMethod == 'COD' and order.paymentStatus != 'PAID'}">
                    <div class="alert alert-danger py-2 mb-0">
                        <i class="fas fa-money-bill-wave"></i>
                        Thu tiền mặt của khách: <b><fmt:formatNumber value="${order.totalAmount}" pattern="#,##0"/> ₫</b>
                    </div>
                </c:when>
                <c:when test="${order.paymentStatus == 'PAID'}">
                    <div class="alert alert-success py-2 mb-0">
                        <i class="fas fa-circle-check"></i> Đơn đã thanh toán, không thu thêm tiền.
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="alert alert-warning py-2 mb-0">
                        <i class="fas fa-circle-info"></i> Đơn chưa thanh toán (${fn:escapeXml(order.paymentMethod)}).
                        Vui lòng hỏi cửa hàng trước khi giao.
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%-- ========== THAO TÁC ========== --%>
<c:if test="${order.status == 'PREPARING' or order.status == 'READY'}">
    <form action="${ctx}/shipper/orders/${fn:escapeXml(order.id)}/accept" method="post"
          onsubmit="return confirm('Nhận giao đơn này?')">
        <button class="btn btn-warning btn-lg w-100 fw-semibold">
            <i class="fas fa-hand-pointer"></i>
            <c:choose>
                <c:when test="${order.status == 'READY'}">Nhận giao lại</c:when>
                <c:otherwise>Nhận đơn</c:otherwise>
            </c:choose>
        </button>
    </form>
</c:if>

<c:if test="${order.status == 'DELIVERING'}">
    <form action="${ctx}/shipper/orders/${fn:escapeXml(order.id)}/complete" method="post"
          onsubmit="return confirm('Xác nhận đã giao thành công cho khách?')" class="mb-3">
        <button class="btn btn-success btn-lg w-100 fw-semibold">
            <i class="fas fa-circle-check"></i> Giao thành công
        </button>
    </form>

    <div class="card order-card shadow-sm border-secondary-subtle">
        <div class="card-body">
            <h6 class="mb-2"><i class="fas fa-circle-xmark text-secondary"></i> Giao thất bại</h6>
            <form action="${ctx}/shipper/orders/${fn:escapeXml(order.id)}/fail" method="post"
                  onsubmit="return confirm('Đánh dấu đơn này giao thất bại?')">
                <textarea name="reason" class="form-control mb-2" rows="2" maxlength="500" required
                          placeholder="Lý do (ví dụ: khách không nghe máy, sai địa chỉ...)"></textarea>
                <button class="btn btn-outline-secondary w-100">
                    <i class="fas fa-ban"></i> Báo giao thất bại
                </button>
            </form>
        </div>
    </div>
</c:if>
