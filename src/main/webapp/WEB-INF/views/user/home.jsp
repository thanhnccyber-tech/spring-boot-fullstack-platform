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
                        <c:when test="${not empty pr.imageUrl}"><img src="${fn:escapeXml(pr.imageUrl)}" alt="${fn:escapeXml(pr.title)}"></c:when>
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
        <div class="col-md-3">
            <div class="card h-100 shadow-sm position-relative">
                <img src="${fn:escapeXml(p.imageUrl)}" class="card-img-top" style="height:180px;object-fit:cover;" alt="${fn:escapeXml(p.name)}">
                <c:if test="${p.outOfStock}">
                    <span class="badge bg-danger position-absolute top-0 end-0 m-2">
                        <i class="fas fa-times-circle"></i> Hết hàng
                    </span>
                </c:if>
                <div class="card-body">
                    <h6 class="card-title">${fn:escapeXml(p.name)}</h6>
                    <c:set var="rs" value="${ratings[p.id]}"/>
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
                    <p class="text-danger fw-bold"><fmt:formatNumber value="${p.basePrice}" pattern="#,##0"/> ₫</p>
                    <a href="${pageContext.request.contextPath}/product/${fn:escapeXml(p.id)}" class="btn btn-sm btn-warning w-100">Xem</a>
                </div>
            </div>
        </div>
    </c:forEach>
</div>

<h3 class="mt-5 mb-3">🆕 Sản phẩm mới</h3>
<div class="row g-3">
    <c:forEach items="${newArrivals}" var="p">
        <div class="col-md-3">
            <div class="card h-100 shadow-sm position-relative">
                <img src="${fn:escapeXml(p.imageUrl)}" class="card-img-top" style="height:180px;object-fit:cover;">
                <c:if test="${p.outOfStock}">
                    <span class="badge bg-danger position-absolute top-0 end-0 m-2">
                        <i class="fas fa-times-circle"></i> Hết hàng
                    </span>
                </c:if>
                <div class="card-body">
                    <h6>${fn:escapeXml(p.name)}</h6>
                    <c:set var="rs" value="${ratings[p.id]}"/>
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
                    <p class="text-danger fw-bold"><fmt:formatNumber value="${p.basePrice}" pattern="#,##0"/> ₫</p>
                    <a href="${pageContext.request.contextPath}/product/${fn:escapeXml(p.id)}" class="btn btn-sm btn-warning w-100">Xem</a>
                </div>
            </div>
        </div>
    </c:forEach>
</div>