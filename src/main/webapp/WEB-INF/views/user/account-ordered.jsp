<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Sản phẩm đã đặt</title>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="small mb-3"><a href="${ctx}/">Trang chủ</a> / <a href="${ctx}/profile">Tài khoản</a> / Sản phẩm đã đặt</nav>
<div class="row g-4">
    <div class="col-lg-4 col-xl-3">
        <jsp:include page="/WEB-INF/views/user/_account-menu.jsp"><jsp:param name="active" value="ordered"/></jsp:include>
    </div>
    <div class="col-lg-8 col-xl-9">

        <div class="checkout-box">
            <h4 class="mb-3" style="color:#8a6d00;font-weight:700">Sản phẩm đã đặt</h4>
            <c:forEach items="${items}" var="it">
                <div class="cart-line align-items-center">
                    <img src="${fn:escapeXml(it.product.thumbUrl)}" loading="lazy" alt="">
                    <div class="flex-grow-1">
                        <div class="fw-bold">${fn:escapeXml(it.product.name)}</div>
                        <div class="small text-muted">Đã đặt ${fn:escapeXml(it.quantity)} ly · lần gần nhất ${fn:escapeXml(it.last)}</div>
                    </div>
                    <a href="${ctx}/product/${fn:escapeXml(it.product.id)}" class="btn btn-sm btn-warning">Đặt lại</a>
                </div>
            </c:forEach>
            <c:if test="${empty items}"><p class="text-center text-muted my-4">Không có dữ liệu</p></c:if>
        </div>
    </div>
</div>
