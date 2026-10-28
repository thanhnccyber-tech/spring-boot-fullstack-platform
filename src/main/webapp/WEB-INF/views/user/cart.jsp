<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Giỏ hàng</title>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<h3 class="mb-3"> Giỏ hàng của bạn</h3>

<c:if test="${hasCartWarnings}">
    <div class="alert alert-warning">
        <i class="fas fa-exclamation-triangle"></i>
        <b>Một số món trong giỏ đã hết hàng tại chi nhánh đã chọn.</b>
        Vui lòng xoá món đó, hoặc
        <button type="button" class="btn btn-link p-0 align-baseline" data-bs-toggle="modal" data-bs-target="#receiveModal">chọn chi nhánh khác</button>
        trước khi thanh toán.
    </div>
</c:if>

<c:if test="${empty cart}">
    <div class="empty-state">
        <i class="fas fa-cart-shopping"></i>
        <h5>Giỏ hàng đang trống</h5>
        <p class="text-muted mb-3">Chọn vài món trà sữa yêu thích rồi quay lại đây nhé.</p>
        <a href="${ctx}/products" class="btn btn-warning">Xem menu và đặt mua</a>
    </div>
</c:if>

<c:if test="${not empty cart}">
<div class="row g-4">
    <div class="col-lg-8">
        <div class="checkout-box">
            <c:forEach items="${cart}" var="item" varStatus="st">
                <c:set var="lineWarning" value="${cartWarnings[st.index]}"/>
                <div class="cart-line">
                    <c:if test="${not empty item.imageUrl}"><img src="${fn:escapeXml(item.thumbUrl)}" loading="lazy" alt=""></c:if>
                    <div class="flex-grow-1">
                        <a class="fw-bold text-decoration-none text-dark" href="${ctx}/product/${fn:escapeXml(item.productId)}">${fn:escapeXml(item.productName)}</a>
                        <div class="small text-muted">
                            Size: ${fn:escapeXml(item.sizeName != null ? item.sizeName : '—')}
                            · Ngọt: ${fn:escapeXml(item.sugarName != null ? item.sugarName : '—')}
                            · Đá: ${fn:escapeXml(item.iceName != null ? item.iceName : '—')}
                            <c:if test="${not empty item.toppingNames}"><br>Topping: <c:forEach items="${item.toppingNames}" var="tn" varStatus="ts">${fn:escapeXml(tn)}<c:if test="${!ts.last}">, </c:if></c:forEach></c:if>
                        </div>
                        <c:if test="${not empty lineWarning}">
                            <div class="alert alert-warning py-1 px-2 mt-2 mb-0 small">
                                <i class="fas fa-circle-exclamation"></i> ${fn:escapeXml(lineWarning)}
                            </div>
                        </c:if>
                        <div class="d-flex justify-content-between align-items-center mt-2">
                            <span><fmt:formatNumber value="${item.unitPrice}" pattern="#,##0"/> ₫ × ${fn:escapeXml(item.quantity)} =
                                <b class="text-success"><fmt:formatNumber value="${item.subtotal}" pattern="#,##0"/> ₫</b></span>
                            <div class="d-flex align-items-center gap-1">
                                <form action="${ctx}/cart/update" method="post" class="d-inline">
                                    <input type="hidden" name="index" value="${fn:escapeXml(st.index)}">
                                    <input type="hidden" name="quantity" value="${fn:escapeXml(item.quantity - 1)}">
                                    <button class="qty-btn" aria-label="Giảm">−</button>
                                </form>
                                <span class="px-2">${fn:escapeXml(item.quantity)}</span>
                                <form action="${ctx}/cart/update" method="post" class="d-inline">
                                    <input type="hidden" name="index" value="${fn:escapeXml(st.index)}">
                                    <input type="hidden" name="quantity" value="${fn:escapeXml(item.quantity + 1)}">
                                    <button class="qty-btn" aria-label="Tăng">+</button>
                                </form>
                                <a href="${ctx}/cart/edit?index=${fn:escapeXml(st.index)}" class="btn btn-link p-0 ms-2" title="Sửa size, đường, đá, topping" aria-label="Sửa món"><i class="fas fa-pen"></i></a>
                                <form action="${ctx}/cart/remove" method="post" class="d-inline ms-2" onsubmit="return confirm('Bỏ món này khỏi giỏ?')">
                                    <input type="hidden" name="index" value="${fn:escapeXml(st.index)}">
                                    <button class="btn btn-link text-danger p-0" aria-label="Xoá món"><i class="fas fa-trash"></i></button>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
            <a href="${ctx}/products" class="btn btn-link px-0 mt-2">← Chọn thêm món</a>
        </div>
    </div>
    <div class="col-lg-4">
        <div class="checkout-box">
            <h5 class="mb-3">Tạm tính</h5>
            <div class="sum-row border-0"><b>Tổng cộng</b><span class="sum-total"><fmt:formatNumber value="${total}" pattern="#,##0"/> ₫</span></div>
            <div class="small text-muted mb-3">Phí giao hàng và mã giảm giá được tính ở bước thanh toán.</div>
            <a href="${ctx}/order/checkout" class="btn btn-warning btn-lg w-100 fw-bold">
                <c:choose><c:when test="${empty sessionScope.userId}">Đăng nhập để thanh toán</c:when><c:otherwise>Tiến hành thanh toán</c:otherwise></c:choose>
            </a>
            <c:if test="${empty sessionScope.userId}">
                <div class="small text-muted mt-2">Giỏ hàng được giữ nguyên sau khi bạn đăng nhập hoặc đăng ký hội viên.</div>
            </c:if>
        </div>
    </div>
</div>
</c:if>