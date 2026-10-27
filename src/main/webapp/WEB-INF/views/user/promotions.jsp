<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Khuyến mãi</title>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="small mb-3"><a href="${ctx}/">Trang chủ</a> / Khuyến mãi</nav>
<h3 class="mb-4" style="color:var(--ute-navy-900); font-weight:800;"> Tin tức &amp; Khuyến mãi</h3>

<c:if test="${empty promotions}">
    <div class="alert alert-light border text-center py-5">
        <i class="fas fa-gift fa-3x text-muted mb-3 opacity-50"></i>
        <h5>Hiện chưa có chương trình khuyến mãi nào.</h5>
        <a href="${ctx}/products" class="btn btn-warning mt-2">Khám phá thực đơn</a>
    </div>
</c:if>

<div class="row g-4">
<c:forEach items="${promotions}" var="p">
    <div class="col-sm-6 col-lg-4 col-xl-3">
        <a href="${ctx}/khuyen-mai/${fn:escapeXml(p.id)}" class="promo-card h-100">
            <c:choose>
                <c:when test="${not empty p.imageUrl}">
                    <img src="${fn:escapeXml(p.imageUrl)}" loading="lazy" alt="${fn:escapeXml(p.title)}">
                </c:when>
                <c:otherwise>
                    <div class="promo-ph"><i class="fas fa-gift"></i></div>
                </c:otherwise>
            </c:choose>
            
            <div class="card-body">
                <div class="small text-muted mb-1">
                    <i class="far fa-eye"></i> ${fn:escapeXml(p.views)} lượt xem 
                    <span class="mx-1">·</span> 
                    ${fn:escapeXml(p.createdAtFormatted)}
                </div>
                <h6>${fn:escapeXml(p.title)}</h6>
                <c:if test="${not empty p.summary}">
                    <p class="mb-0 mt-auto">${fn:escapeXml(p.summary)}</p>
                </c:if>
            </div>
        </a>
    </div>
</c:forEach>
</div>