<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- Thẻ sản phẩm dùng chung (trang chủ, danh sách). Cần request attribute: cardProduct; tuỳ chọn: ratings, favIds --%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="p" value="${cardProduct}"/>
<c:set var="rs" value="${ratings[p.id]}"/>
<c:set var="isFav" value="${not empty favIds and favIds.contains(p.id)}"/>
<c:set var="fq" value="${requestScope['jakarta.servlet.forward.query_string']}"/>
<c:set var="furi" value="${not empty requestScope['jakarta.servlet.forward.request_uri'] ? requestScope['jakarta.servlet.forward.request_uri'] : '/products'}"/>
<c:set var="here"><c:out value="${furi}" escapeXml="false"/><c:if test="${not empty fq}">?<c:out value="${fq}" escapeXml="false"/></c:if></c:set>
<div class="card h-100 shadow-sm position-relative product-card">
    <a href="${ctx}/product/${fn:escapeXml(p.id)}" class="d-block">
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
    <c:if test="${p.outOfStock}">
        <span class="badge bg-danger position-absolute top-0 start-0 m-2"><i class="fas fa-times-circle"></i> Hết hàng</span>
    </c:if>
    <div class="card-body d-flex flex-column">
        <h6 class="card-title">${fn:escapeXml(p.name)}</h6>
        <div class="small mb-1">
            <span class="text-warning">
                <c:forEach begin="1" end="5" var="i"><i class="${fn:escapeXml((not empty rs and i <= rs.rounded) ? 'fas' : 'far')} fa-star"></i></c:forEach>
            </span>
            <span class="text-muted">
                <c:choose>
                    <c:when test="${not empty rs}">(<fmt:formatNumber value="${rs.average}" maxFractionDigits="1"/> · ${fn:escapeXml(rs.count)})</c:when>
                    <c:otherwise>Chưa có đánh giá</c:otherwise>
                </c:choose>
            </span>
        </div>
        <p class="text-danger fw-bold mb-2"><fmt:formatNumber value="${p.basePrice}" pattern="#,##0"/> ₫</p>
        <a href="${ctx}/product/${fn:escapeXml(p.id)}" class="btn btn-warning btn-sm w-100 mt-auto"><i class="fas fa-cart-plus"></i> Đặt mua</a>
    </div>
</div>
