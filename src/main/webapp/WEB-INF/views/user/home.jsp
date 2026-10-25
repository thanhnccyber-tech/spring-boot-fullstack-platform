<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>UTeTra - Trà sữa chuẩn vị</title>

<div class="p-5 mb-4 bg-warning bg-gradient rounded-3 text-white">
    <h1 class="display-4 fw-bold">UTeTra - Trà sữa chuẩn vị</h1>
    <p class="lead">Đặt ngay trà sữa yêu thích, giao nhanh tận cửa!</p>
    <a href="${pageContext.request.contextPath}/products" class="btn btn-dark btn-lg">Xem menu</a>
</div>

<c:if test="${not empty promotions}">
<div class="d-flex justify-content-between align-items-center mb-3">
    <h3 class="mb-0">🎁 Tin tức &amp; Khuyến mãi</h3>
    <a href="${pageContext.request.contextPath}/khuyen-mai">Xem tất cả →</a>
</div>
<div class="row g-3 mb-5">
    <c:forEach items="${promotions}" var="pr">
        <div class="col-sm-6 col-lg-3">
            <a href="${pageContext.request.contextPath}/khuyen-mai/${fn:escapeXml(pr.id)}" class="text-decoration-none text-dark">
                <div class="card promo-card h-100 shadow-sm">
                    <c:choose>
                        <c:when test="${not empty pr.imageUrl}"><img src="${fn:escapeXml(pr.imageUrl)}" loading="lazy" alt="${fn:escapeXml(pr.title)}"></c:when>
                        <c:otherwise><div class="promo-ph"><i class="fas fa-gift"></i></div></c:otherwise>
                    </c:choose>
                    <div class="card-body">
                        <div class="small text-muted"><i class="far fa-eye"></i> ${fn:escapeXml(pr.views)}</div>
                        <h6 class="mb-0">${fn:escapeXml(pr.title)}</h6>
                    </div>
                </div>
            </a>
        </div>
    </c:forEach>
</div>
</c:if>

<h3 class="mb-3">⭐ Sản phẩm nổi bật</h3>
<div class="row g-3">
    <c:forEach items="${featured}" var="p">
        <div class="col-6 col-md-3">
            <c:set var="cardProduct" value="${p}" scope="request"/>
            <jsp:include page="/WEB-INF/views/user/_product-card.jsp"/>
        </div>
    </c:forEach>
</div>

<h3 class="mt-5 mb-3">🆕 Sản phẩm mới</h3>
<div class="row g-3">
    <c:forEach items="${newArrivals}" var="p">
        <div class="col-6 col-md-3">
            <c:set var="cardProduct" value="${p}" scope="request"/>
            <jsp:include page="/WEB-INF/views/user/_product-card.jsp"/>
        </div>
    </c:forEach>
</div>