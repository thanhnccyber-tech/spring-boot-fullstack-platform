<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
  Thẻ đơn hàng trên dashboard shipper.
  Request attribute:
    card     — Order
    cardMode — "available" (chờ nhận) | "delivering" (đang giao)
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="o" value="${requestScope.card}"/>
<c:url var="mapUrl" value="https://www.google.com/maps/search/">
    <c:param name="api" value="1"/>
    <c:param name="query" value="${o.shippingAddress}"/>
</c:url>

<div class="sh-order">

    <%-- HEAD: mã đơn + trạng thái --%>
    <div class="sh-order-head">
        <div>
            <div class="sh-order-code">#${fn:escapeXml(o.orderCode)}</div>
            <div class="sh-order-time">${fn:escapeXml(o.createdAtShort)}</div>
        </div>
        <c:choose>
            <c:when test="${o.status == 'PREPARING'}"><span class="sh-status preparing"><i class="fas fa-hourglass-half"></i>Chờ nhận</span></c:when>
            <c:when test="${o.status == 'READY'}"><span class="sh-status ready"><i class="fas fa-rotate"></i>Giao lại</span></c:when>
            <c:when test="${o.status == 'DELIVERING'}"><span class="sh-status delivering"><i class="fas fa-motorcycle"></i>Đang giao</span></c:when>
            <c:when test="${o.status == 'COMPLETED'}"><span class="sh-status completed"><i class="fas fa-circle-check"></i>Đã giao</span></c:when>
            <c:when test="${o.status == 'FAILED'}"><span class="sh-status failed"><i class="fas fa-circle-xmark"></i>Thất bại</span></c:when>
            <c:otherwise><span class="sh-status pending">${fn:escapeXml(o.statusLabel)}</span></c:otherwise>
        </c:choose>
    </div>

    <%-- BODY: khách hàng + địa chỉ + chips --%>
    <div class="sh-order-body">

        <div class="sh-info-row">
            <i class="fas fa-user"></i>
            <span class="flex-grow-1 fw-semibold">${fn:escapeXml(o.user != null ? o.user.fullName : 'Khách')}</span>
        </div>

        <div class="sh-info-row">
            <i class="fas fa-phone"></i>
            <a href="tel:${fn:escapeXml(o.phone)}" class="flex-grow-1 text-decoration-none fw-semibold" style="color:var(--sh-blue-600)">
                ${fn:escapeXml(o.phone)}
            </a>
        </div>

        <div class="sh-info-row">
            <i class="fas fa-location-dot"></i>
            <span class="flex-grow-1">${fn:escapeXml(o.shippingAddress)}</span>
            <a href="${fn:escapeXml(mapUrl)}" target="_blank" rel="noopener noreferrer"
               class="sh-btn sh-btn-outline sh-btn-sm flex-shrink-0" title="Mở Google Maps">
                <i class="fas fa-map-location-dot"></i>
            </a>
        </div>

        <%-- Chips thông tin phụ --%>
        <c:if test="${o.distanceKm != null or (not empty o.deliveryTime and o.deliveryTime != 'ASAP') or o.paymentMethod == 'COD' or o.paymentStatus == 'PAID' or not empty o.note}">
            <div class="sh-chips mt-2">
                <c:if test="${o.distanceKm != null}">
                    <span class="sh-chip blue"><i class="fas fa-route"></i> <fmt:formatNumber value="${o.distanceKm}" maxFractionDigits="1"/> km</span>
                </c:if>
                <c:if test="${not empty o.deliveryTime and o.deliveryTime != 'ASAP'}">
                    <span class="sh-chip amber"><i class="fas fa-clock"></i> ${fn:escapeXml(o.deliveryTime)}</span>
                </c:if>
                <c:if test="${o.paymentMethod == 'COD' and o.paymentStatus != 'PAID'}">
                    <span class="sh-chip red"><i class="fas fa-money-bill-wave"></i> Thu COD</span>
                </c:if>
                <c:if test="${o.paymentStatus == 'PAID'}">
                    <span class="sh-chip green"><i class="fas fa-circle-check"></i> Đã trả</span>
                </c:if>
            </div>
        </c:if>

        <c:if test="${not empty o.note}">
            <div class="mt-2 p-2 rounded sh-small"
                 style="background:#fffbeb;color:var(--sh-amber-text);border-left:3px solid var(--sh-amber)">
                <i class="fas fa-note-sticky"></i> ${fn:escapeXml(o.note)}
            </div>
        </c:if>

    </div>

    <%-- FOOT: tổng tiền + CTA --%>
    <div class="sh-order-foot">
        <div>
            <div class="sh-order-total ${o.paymentMethod == 'COD' and o.paymentStatus != 'PAID' ? 'cod' : ''}">
                <small>Tổng tiền</small>
                <fmt:formatNumber value="${o.totalAmount}" pattern="#,##0"/> ₫
            </div>
        </div>
        <div class="d-flex gap-2">
            <a href="${ctx}/shipper/orders/${fn:escapeXml(o.id)}"
               class="sh-btn sh-btn-outline sh-btn-sm" title="Xem chi tiết">
                <i class="fas fa-eye"></i>
            </a>
            <c:choose>
                <c:when test="${requestScope.cardMode == 'available'}">
                    <form action="${ctx}/shipper/orders/${fn:escapeXml(o.id)}/accept" method="post" class="m-0"
                          onsubmit="return confirm('Nhận giao đơn #${fn:escapeXml(o.orderCode)}?')">
                        <button class="sh-btn sh-btn-primary">
                            <i class="fas fa-hand-pointer"></i> Nhận đơn
                        </button>
                    </form>
                </c:when>
                <c:otherwise>
                    <a href="${ctx}/shipper/orders/${fn:escapeXml(o.id)}" class="sh-btn sh-btn-primary">
                        <i class="fas fa-arrow-right"></i> Xử lý
                    </a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

</div>