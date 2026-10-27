<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Lịch sử giao hàng</title>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<div class="sh-page-title">
    <i class="fas fa-clock-rotate-left"></i> Lịch sử giao hàng
</div>

<c:if test="${empty orders and empty failures}">
    <div class="sh-empty">
        <i class="fas fa-box-open"></i>
        <h5>Chưa có đơn nào</h5>
        <p>Các đơn đã giao sẽ xuất hiện tại đây.</p>
        <a href="${ctx}/shipper/dashboard" class="sh-btn sh-btn-primary sh-btn-sm">
            <i class="fas fa-box-open"></i> Xem đơn cần giao
        </a>
    </div>
</c:if>

<%-- ===== Đơn đã giao / thất bại ===== --%>
<c:if test="${not empty orders}">
    <div class="d-flex justify-content-between align-items-center mb-2">
        <div class="sh-section-label mb-0">
            <i class="fas fa-check-double"></i> Đơn gần đây
        </div>
        <div class="sh-small sh-muted">${fn:length(orders)} / ${total}</div>
    </div>

    <c:forEach var="o" items="${orders}">
        <div class="sh-order">
            <div class="sh-order-head">
                <div>
                    <div class="sh-order-code">#${fn:escapeXml(o.orderCode)}</div>
                    <div class="sh-order-time">${fn:escapeXml(o.updatedAtFormatted)}</div>
                </div>
                <c:choose>
                    <c:when test="${o.status == 'COMPLETED'}">
                        <span class="sh-status completed"><i class="fas fa-circle-check"></i>Đã giao</span>
                    </c:when>
                    <c:otherwise>
                        <span class="sh-status failed"><i class="fas fa-circle-xmark"></i>Thất bại</span>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="sh-order-body">
                <div class="sh-info-row">
                    <i class="fas fa-user"></i>
                    <span>${fn:escapeXml(o.user != null ? o.user.fullName : 'Khách')}</span>
                </div>
                <div class="sh-info-row">
                    <i class="fas fa-phone"></i>
                    <span>${fn:escapeXml(o.phone)}</span>
                </div>
                <div class="sh-info-row">
                    <i class="fas fa-location-dot"></i>
                    <span>${fn:escapeXml(o.shippingAddress)}</span>
                </div>
                <c:if test="${not empty o.failedReason}">
                    <div class="mt-2 p-2 rounded sh-small"
                         style="background:var(--sh-red-bg);color:var(--sh-red-text);border-left:3px solid var(--sh-red)">
                        <i class="fas fa-circle-exclamation"></i> ${fn:escapeXml(o.failedReason)}
                    </div>
                </c:if>
            </div>
            <div class="sh-order-foot">
                <div class="sh-order-total">
                    <small>Tổng tiền</small>
                    <fmt:formatNumber value="${o.totalAmount}" pattern="#,##0"/> ₫
                </div>
                <a href="${ctx}/shipper/orders/${fn:escapeXml(o.id)}" class="sh-btn sh-btn-outline sh-btn-sm">
                    <i class="fas fa-eye"></i> Xem chi tiết
                </a>
            </div>
        </div>
    </c:forEach>

    <c:if test="${total > limit}">
        <div class="sh-small sh-muted text-center mt-2">
            Hiển thị ${limit} đơn gần nhất trong tổng số ${total} đơn.
        </div>
    </c:if>
</c:if>

<%-- ===== Các lần giao thất bại ===== --%>
<c:if test="${not empty failures}">
    <div class="sh-section-label mt-4">
        <i class="fas fa-triangle-exclamation" style="color:var(--sh-red)"></i>
        Lần giao thất bại (${failureTotal})
    </div>
    <c:forEach var="f" items="${failures}">
        <div class="sh-card sh-card-pad mb-2" style="border-left:3px solid var(--sh-red)">
            <div class="d-flex justify-content-between mb-1">
                <div class="sh-order-code">#${fn:escapeXml(f.order.orderCode)}</div>
                <div class="sh-small sh-muted">${fn:escapeXml(f.failedAtFormatted)}</div>
            </div>
            <div class="sh-small mb-1">
                <i class="fas fa-user sh-muted"></i> ${fn:escapeXml(f.order.user != null ? f.order.user.fullName : 'Khách')}
                · ${fn:escapeXml(f.order.phone)}
            </div>
            <div class="sh-small" style="color:var(--sh-red-text)">
                <i class="fas fa-circle-exclamation"></i> ${fn:escapeXml(f.reason)}
            </div>
            <div class="mt-2 text-end">
                <a href="${ctx}/shipper/orders/${fn:escapeXml(f.order.id)}" class="sh-btn sh-btn-outline sh-btn-sm">
                    <i class="fas fa-eye"></i> Xem đơn
                </a>
            </div>
        </div>
    </c:forEach>
</c:if>