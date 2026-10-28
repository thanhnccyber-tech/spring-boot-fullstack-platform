<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- Thẻ sản phẩm dùng chung. Request attributes:
     cardProduct          — sản phẩm
     ratings              — Map<Long, RatingSummary> (tuỳ chọn)
     favIds               — Set<Long> (tuỳ chọn)
     effectiveProductOOS  — Map<Long, Boolean> (tuỳ chọn) các món hết tại chi nhánh đã chọn
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="p" value="${cardProduct}"/>
<c:set var="rs" value="${ratings[p.id]}"/>
<c:set var="isFav" value="${not empty favIds and favIds.contains(p.id)}"/>

<%-- "Hết tại chi nhánh" ưu tiên hiển thị khác biệt so với "Hết hàng toàn hệ thống" --%>
<c:set var="branchOOS" value="${not empty effectiveProductOOS and effectiveProductOOS[p.id] == true}"/>
<c:set var="effectiveOOS" value="${p.outOfStock or branchOOS}"/>

<c:set var="furi" value="${not empty requestScope['jakarta.servlet.forward.request_uri']
                            ? requestScope['jakarta.servlet.forward.request_uri']
                            : pageContext.request.requestURI}"/>
<c:set var="fq"   value="${not empty requestScope['jakarta.servlet.forward.query_string']
                            ? requestScope['jakarta.servlet.forward.query_string']
                            : pageContext.request.queryString}"/>
<c:set var="here"><c:out value="${furi}" escapeXml="false"/><c:if test="${not empty fq}">?<c:out value="${fq}" escapeXml="false"/></c:if></c:set>

<div class="card h-100 position-relative product-card">
    <a href="${ctx}/product/${fn:escapeXml(p.id)}" class="d-block overflow-hidden">
        <img src="${fn:escapeXml(p.thumbUrl)}" loading="lazy" decoding="async" class="card-img-top" alt="${fn:escapeXml(p.name)}">
    </a>
    <c:choose>
        <c:when test="${not empty sessionScope.userId}">
            <form action="${ctx}/account/favorites/toggle" method="post" class="fav-form">
                <input type="hidden" name="productId" value="${fn:escapeXml(p.id)}">
                <input type="hidden" name="back" value="${fn:escapeXml(here)}">
                <button class="fav-btn ${isFav ? 'is-fav' : ''}" data-no-lock="true"
                        title="${isFav ? 'Bỏ yêu thích' : 'Thêm vào yêu thích'}" aria-label="Yêu thích"><i class="${isFav ? 'fas' : 'far'} fa-heart"></i></button>
            </form>
        </c:when>
        <c:otherwise>
            <c:url var="favLogin" value="/login"><c:param name="need" value="1"/><c:param name="next" value="/product/${p.id}"/></c:url>
            <a class="fav-btn" href="${fn:escapeXml(favLogin)}" title="Đăng nhập để lưu món yêu thích" aria-label="Yêu thích"><i class="far fa-heart"></i></a>
        </c:otherwise>
    </c:choose>
    <c:if test="${effectiveOOS}">
        <span class="badge bg-danger position-absolute top-0 start-0 m-2">
            <i class="fas fa-times-circle"></i>
            <c:choose>
                <c:when test="${p.outOfStock}">Hết hàng</c:when>
                <c:otherwise>Hết tại chi nhánh</c:otherwise>
            </c:choose>
        </span>
    </c:if>
    <div class="card-body d-flex flex-column">
        <h6 class="card-title mb-1" style="font-size:.95rem">${fn:escapeXml(p.name)}</h6>
        <div class="small mb-2">
            <span class="text-warning">
                <c:forEach begin="1" end="5" var="i"><i class="${fn:escapeXml((not empty rs and i <= rs.rounded) ? 'fas' : 'far')} fa-star"></i></c:forEach>
            </span>
            <span class="text-muted ms-1" style="font-size:.8rem">
                <c:choose>
                    <c:when test="${not empty rs}">(<fmt:formatNumber value="${rs.average}" maxFractionDigits="1"/> · ${fn:escapeXml(rs.count)})</c:when>
                    <c:otherwise>Chưa đánh giá</c:otherwise>
                </c:choose>
            </span>
        </div>
        <p class="fw-bold mb-2" style="color:var(--ute-blue-700);font-size:1.05rem"><fmt:formatNumber value="${p.basePrice}" pattern="#,##0"/> ₫</p>
        <a href="${ctx}/product/${fn:escapeXml(p.id)}"
           class="btn btn-warning btn-sm w-100 mt-auto fw-bold ${effectiveOOS ? 'disabled' : ''}"
           <c:if test="${effectiveOOS}">aria-disabled="true" tabindex="-1"</c:if>>
            <i class="fas fa-cart-plus"></i>
            <c:choose>
                <c:when test="${p.outOfStock}">Hết hàng</c:when>
                <c:when test="${branchOOS}">Xem chi tiết</c:when>
                <c:otherwise>Đặt mua</c:otherwise>
            </c:choose>
        </a>
    </div>
</div>