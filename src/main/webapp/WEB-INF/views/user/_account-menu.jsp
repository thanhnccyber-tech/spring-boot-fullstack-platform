<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="act" value="${param.active}"/>
<div class="checkout-box p-3">
    <div class="d-flex align-items-center gap-3 pb-3 mb-2 border-bottom">
        <i class="fas fa-user-circle" style="font-size:44px;color:#b8860b"></i>
        <div class="fw-bold">${fn:escapeXml(sessionScope.fullName)}</div>
    </div>
    <div class="list-group list-group-flush account-menu">
        <a class="list-group-item list-group-item-action ${fn:escapeXml(act == 'profile' ? 'active' : '')}" href="${ctx}/profile"><i class="fas fa-id-badge"></i> Thông tin cá nhân</a>
        <a class="list-group-item list-group-item-action ${fn:escapeXml(act == 'member' ? 'active' : '')}" href="${ctx}/account/member"><i class="fas fa-id-card"></i> Khách hàng thành viên</a>
        <a class="list-group-item list-group-item-action ${fn:escapeXml(act == 'vouchers' ? 'active' : '')}" href="${ctx}/account/vouchers"><i class="fas fa-ticket"></i> Ưu đãi của tôi</a>
        <a class="list-group-item list-group-item-action ${fn:escapeXml(act == 'addresses' ? 'active' : '')}" href="${ctx}/account/addresses"><i class="fas fa-location-dot"></i> Số địa chỉ</a>
        <a class="list-group-item list-group-item-action ${fn:escapeXml(act == 'orders' ? 'active' : '')}" href="${ctx}/order/history"><i class="fas fa-cart-shopping"></i> Đơn hàng</a>
        <a class="list-group-item list-group-item-action ${fn:escapeXml(act == 'favorites' ? 'active' : '')}" href="${ctx}/account/favorites"><i class="fas fa-heart"></i> Sản phẩm yêu thích</a>
        <a class="list-group-item list-group-item-action ${fn:escapeXml(act == 'ordered' ? 'active' : '')}" href="${ctx}/account/ordered"><i class="fas fa-box-open"></i> Sản phẩm đã đặt</a>
        <a class="list-group-item list-group-item-action ${fn:escapeXml(act == 'help' ? 'active' : '')}" href="${ctx}/account/help"><i class="fas fa-headset"></i> Trung tâm trợ giúp</a>
        <a class="list-group-item list-group-item-action text-danger" href="${ctx}/logout"><i class="fas fa-right-from-bracket"></i> Đăng xuất</a>
    </div>
</div>
