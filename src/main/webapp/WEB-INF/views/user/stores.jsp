<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Cửa hàng</title>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="small mb-3"><a href="${ctx}/">Trang chủ</a> / Cửa hàng</nav>
<h3 class="mb-3">🏪 Danh sách cửa hàng</h3>

<div class="row g-3">
<c:forEach items="${branches}" var="b">
    <div class="col-md-6">
        <div class="checkout-box h-100">
            <h5>${fn:escapeXml(b.name)}</h5>
            <p class="mb-1"><i class="fas fa-location-dot text-warning"></i> ${fn:escapeXml(b.address)}</p>
            <p class="mb-2"><i class="fas fa-phone text-success"></i> <a href="tel:${fn:escapeXml(b.phone)}">${fn:escapeXml(b.phone)}</a></p>
            <form action="${ctx}/receive/set" method="post" class="d-inline">
                <input type="hidden" name="method" value="PICKUP">
                <input type="hidden" name="branchId" value="${fn:escapeXml(b.id)}">
                <input type="hidden" name="returnUrl" value="/products">
                <button class="btn btn-sm btn-warning">Đến lấy tại đây</button>
            </form>
            <c:url var="mapUrl" value="https://www.google.com/maps/search/"><c:param name="api" value="1"/><c:param name="query" value="${b.address}"/></c:url>
            <a class="btn btn-sm btn-outline-secondary" target="_blank" rel="noopener" href="${fn:escapeXml(mapUrl)}">Xem bản đồ</a>
        </div>
    </div>
</c:forEach>
</div>
<c:if test="${empty branches}"><div class="alert alert-light border text-center">Chưa có cửa hàng nào hoạt động.</div></c:if>
