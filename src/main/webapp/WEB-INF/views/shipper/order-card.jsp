<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
  Thẻ đơn hàng dùng chung trên trang shipper.
  Nhận qua request attribute: card (đơn hàng), cardMode ("available" = chờ nhận, "delivering" = đang giao).
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="o" value="${requestScope.card}"/>
<c:url var="mapUrl" value="https://www.google.com/maps/search/">
    <c:param name="api" value="1"/>
    <c:param name="query" value="${o.shippingAddress}"/>
</c:url>

<div class="card order-card shadow-sm mb-3">
    <div class="card-body">
        <div class="d-flex justify-content-between align-items-start">
            <div>
                <span class="fw-bold">#${fn:escapeXml(o.orderCode)}</span>
                <small class="text-muted ms-1">${fn:escapeXml(o.createdAtShort)}</small>
            </div>
            <c:choose>
                <c:when test="${o.paymentMethod == 'COD'}">
                    <span class="badge bg-danger">Thu hộ COD</span>
                </c:when>
                <c:when test="${o.paymentStatus == 'PAID'}">
                    <span class="badge bg-success">Đã thanh toán</span>
                </c:when>
                <c:otherwise>
                    <span class="badge bg-secondary">Chưa thanh toán</span>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="mt-2">
            <c:if test="${o.user != null}">
                <div><i class="fas fa-user text-muted me-1"></i> ${fn:escapeXml(o.user.fullName)}</div>
            </c:if>
            <div>
                <i class="fas fa-phone text-muted me-1"></i>
                <a href="tel:${fn:escapeXml(o.phone)}">${fn:escapeXml(o.phone)}</a>
            </div>
            <div>
                <i class="fas fa-location-dot text-muted me-1"></i>
                ${fn:escapeXml(o.shippingAddress)}
                <a href="${fn:escapeXml(mapUrl)}" target="_blank" rel="noopener noreferrer" class="small ms-1">
                    <i class="fas fa-map"></i> Bản đồ
                </a>
            </div>
            <c:if test="${o.distanceKm != null}">
                <div class="small text-muted">
                    <i class="fas fa-route me-1"></i>
                    <fmt:formatNumber value="${o.distanceKm}" maxFractionDigits="1"/> km từ cửa hàng
                </div>
            </c:if>
            <c:if test="${not empty o.deliveryTime and o.deliveryTime != 'ASAP'}">
                <div class="small text-warning-emphasis">
                    <i class="fas fa-clock me-1"></i> Hẹn giờ: ${fn:escapeXml(o.deliveryTime)}
                </div>
            </c:if>
            <c:if test="${not empty o.note}">
                <div class="small text-muted"><i class="fas fa-note-sticky me-1"></i> ${fn:escapeXml(o.note)}</div>
            </c:if>
        </div>

        <div class="d-flex justify-content-between align-items-center mt-3">
            <div class="fw-bold">
                <c:choose>
                    <c:when test="${o.paymentMethod == 'COD'}">
                        <span class="text-danger">Thu: <fmt:formatNumber value="${o.totalAmount}" pattern="#,##0"/> ₫</span>
                    </c:when>
                    <c:otherwise>
                        Tổng: <fmt:formatNumber value="${o.totalAmount}" pattern="#,##0"/> ₫
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="d-flex gap-2">
                <a href="${ctx}/shipper/orders/${fn:escapeXml(o.id)}" class="btn btn-outline-dark">
                    <i class="fas fa-eye"></i> Chi tiết
                </a>
                <c:if test="${requestScope.cardMode == 'available'}">
                    <form action="${ctx}/shipper/orders/${fn:escapeXml(o.id)}/accept" method="post" class="m-0"
                          onsubmit="return confirm('Nhận giao đơn này?')">
                        <button class="btn btn-warning fw-semibold">
                            <i class="fas fa-hand-pointer"></i> Nhận đơn
                        </button>
                    </form>
                </c:if>
            </div>
        </div>
    </div>
</div>
