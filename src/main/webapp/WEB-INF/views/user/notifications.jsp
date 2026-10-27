<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Thông báo</title>

<div class="ute-all-notifications">
    <div class="ute-all-notifications-head">
        <div><div class="small text-primary fw-bold text-uppercase">UTETra cập nhật</div><h3><i class="fas fa-bell"></i> Tất cả thông báo</h3><p>Tin tức ưu đãi và cập nhật đơn hàng của bạn.</p></div>
        <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-sm"><i class="fas fa-mug-hot"></i> Đặt món</a>
    </div>
    <div class="ute-all-notifications-list">
        <c:forEach items="${notifications}" var="n">
            <article class="ute-all-notification-row">
                <div class="ute-all-notification-icon ${n.type == 'ORDER' ? 'order' : (n.type == 'VOUCHER' ? 'voucher' : 'news')}">
                    <i class="fas ${n.type == 'ORDER' ? 'fa-receipt' : (n.type == 'VOUCHER' ? 'fa-ticket' : 'fa-bullhorn')}"></i>
                </div>
                <div class="flex-grow-1">
                    <div class="d-flex flex-wrap justify-content-between gap-2"><strong>${fn:escapeXml(n.title)}</strong><small class="text-muted">${fn:escapeXml(n.createdAtFormatted)}</small></div>
                    <p>${fn:escapeXml(n.content)}</p>
                    <c:choose>
                        <c:when test="${n.type == 'VOUCHER'}"><a href="${pageContext.request.contextPath}/account/vouchers">Xem ưu đãi <i class="fas fa-arrow-right"></i></a></c:when>
                        <c:when test="${n.type == 'ORDER'}"><a href="${pageContext.request.contextPath}/order/history">Theo dõi đơn hàng <i class="fas fa-arrow-right"></i></a></c:when>
                    </c:choose>
                </div>
            </article>
        </c:forEach>
        <c:if test="${empty notifications}"><div class="ute-notification-empty"><i class="fas fa-inbox"></i><b>Chưa có thông báo nào</b><span>Khi có ưu đãi hoặc cập nhật đơn hàng, thông tin sẽ hiển thị tại đây.</span></div></c:if>
    </div>
</div>