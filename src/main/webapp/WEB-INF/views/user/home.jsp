<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>UTETra - Trà sữa tươi mát mỗi ngày</title>

<!-- Hero — full-bleed tràn viền, dính liền header -->
<section class="hero">
    <div class="container">
        <div class="row align-items-center g-4">
            <div class="col-lg-7">
                <h1>
                    Trà sữa <span class="accent">tươi mát</span><br>
                    cho mọi khoảnh khắc
                </h1>
                <p class="lead">
                    Nguyên liệu chọn lọc, pha chế thủ công mỗi ngày. Đặt online – giao trong 30 phút,
                    hoặc đến lấy tại cửa hàng UTETra gần nhất.
                </p>
                <div class="hero-actions">
                    <a href="${pageContext.request.contextPath}/products" class="btn-hero-primary">
                        <i class="fas fa-mug-hot"></i> Xem thực đơn
                    </a>
                    <a href="${pageContext.request.contextPath}/khuyen-mai" class="btn-hero-secondary">
                        <i class="fas fa-gift"></i> Ưu đãi hôm nay
                    </a>
                </div>
            </div>
            <div class="col-lg-5 d-none d-lg-block text-center">
                <div class="hero-art"><i class="fas fa-mug-hot"></i></div>
            </div>
        </div>
    </div>
</section>

<!-- Danh mục -->
<c:if test="${not empty categories}">
<div class="section-title">
    <h3>Danh mục</h3>
    <a href="${pageContext.request.contextPath}/products">Xem tất cả <i class="fas fa-arrow-right ms-1"></i></a>
</div>
<div class="cat-chips mb-5">
    <a href="${pageContext.request.contextPath}/products" class="cat-chip active">Tất cả</a>
    <c:forEach items="${categories}" var="cat">
        <a href="${pageContext.request.contextPath}/products?categoryId=${fn:escapeXml(cat.id)}" class="cat-chip">
            ${fn:escapeXml(cat.name)}
        </a>
    </c:forEach>
</div>
</c:if>

<!-- Khuyến mãi -->
<c:if test="${not empty promotions}">
<div class="section-title">
    <h3>Khuyến mãi</h3>
    <a href="${pageContext.request.contextPath}/khuyen-mai">Xem tất cả <i class="fas fa-arrow-right ms-1"></i></a>
</div>
<div class="row g-3 mb-5">
    <c:forEach items="${promotions}" var="pr">
        <div class="col-sm-6 col-lg-3">
            <a href="${pageContext.request.contextPath}/khuyen-mai/${fn:escapeXml(pr.id)}" class="text-decoration-none text-dark">
                <div class="card promo-card h-100">
                    <c:choose>
                        <c:when test="${not empty pr.imageUrl}"><img src="${fn:escapeXml(pr.imageUrl)}" loading="lazy" alt="${fn:escapeXml(pr.title)}"></c:when>
                        <c:otherwise><div class="promo-ph"><i class="fas fa-gift"></i></div></c:otherwise>
                    </c:choose>
                    <div class="card-body">
                        <div class="small text-muted mb-1"><i class="far fa-eye"></i> ${fn:escapeXml(pr.views)} lượt xem</div>
                        <h6 class="mb-0">${fn:escapeXml(pr.title)}</h6>
                    </div>
                </div>
            </a>
        </div>
    </c:forEach>
</div>
</c:if>

<!-- Sản phẩm nổi bật -->
<div class="section-title">
    <h3>Sản phẩm nổi bật</h3>
    <a href="${pageContext.request.contextPath}/products">Tất cả món <i class="fas fa-arrow-right ms-1"></i></a>
</div>
<div class="row g-3 mb-5">
    <c:forEach items="${featured}" var="p">
        <div class="col-6 col-md-3">
            <c:set var="cardProduct" value="${p}" scope="request"/>
            <jsp:include page="/WEB-INF/views/user/_product-card.jsp"/>
        </div>
    </c:forEach>
</div>

<!-- Sản phẩm mới -->
<c:if test="${not empty newArrivals}">
<div class="section-title">
    <h3>Món mới ra mắt</h3>
    <a href="${pageContext.request.contextPath}/products?sort=newest">Xem thêm <i class="fas fa-arrow-right ms-1"></i></a>
</div>
<div class="row g-3">
    <c:forEach items="${newArrivals}" var="p">
        <div class="col-6 col-md-3">
            <c:set var="cardProduct" value="${p}" scope="request"/>
            <jsp:include page="/WEB-INF/views/user/_product-card.jsp"/>
        </div>
    </c:forEach>
</div>
</c:if>