<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Khuyến mãi</title>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="small mb-3"><a href="${ctx}/">Trang chủ</a> / Khuyến mãi</nav>
<h3 class="mb-3">🎁 Tin tức &amp; Khuyến mãi</h3>

<c:if test="${empty promotions}"><div class="alert alert-light border text-center">Hiện chưa có chương trình khuyến mãi nào.</div></c:if>

<div class="row g-3">
<c:forEach items="${promotions}" var="p">
    <div class="col-sm-6 col-lg-3">
        <a href="${ctx}/khuyen-mai/${fn:escapeXml(p.id)}" class="text-decoration-none text-dark">
            <div class="card promo-card h-100 shadow-sm">
                <c:choose>
                    <c:when test="${not empty p.imageUrl}"><img src="${fn:escapeXml(p.imageUrl)}" loading="lazy" alt="${fn:escapeXml(p.title)}"></c:when>
                    <c:otherwise><div class="promo-ph"><i class="fas fa-gift"></i></div></c:otherwise>
                </c:choose>
                <div class="card-body">
                    <div class="small text-muted mb-1"><i class="far fa-eye"></i> ${fn:escapeXml(p.views)} · ${fn:escapeXml(p.createdAtFormatted)}</div>
                    <h6 class="mb-1">${fn:escapeXml(p.title)}</h6>
                    <c:if test="${not empty p.summary}"><p class="small text-muted mb-0">${fn:escapeXml(p.summary)}</p></c:if>
                </div>
            </div>
        </a>
    </div>
</c:forEach>
</div>
