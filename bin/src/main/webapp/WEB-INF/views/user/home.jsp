<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<title>UTeTra - Trà sữa chuẩn vị</title>

<div class="p-5 mb-4 bg-warning bg-gradient rounded-3 text-white">
    <h1 class="display-4 fw-bold">UTeTra - Trà sữa chuẩn vị</h1>
    <p class="lead">Đặt ngay trà sữa yêu thích, giao nhanh tận cửa!</p>
    <a href="${pageContext.request.contextPath}/products" class="btn btn-dark btn-lg">Xem menu</a>
</div>

<h3 class="mb-3">⭐ Sản phẩm nổi bật</h3>
<div class="row g-3">
    <c:forEach items="${featured}" var="p">
        <div class="col-md-3">
            <div class="card h-100 shadow-sm position-relative">
                <img src="${p.imageUrl}" class="card-img-top" style="height:180px;object-fit:cover;" alt="${p.name}">
                <c:if test="${p.outOfStock}">
                    <span class="badge bg-danger position-absolute top-0 end-0 m-2">
                        <i class="fas fa-times-circle"></i> Hết hàng
                    </span>
                </c:if>
                <div class="card-body">
                    <h6 class="card-title">${p.name}</h6>
                    <c:set var="rs" value="${ratings[p.id]}"/>
                    <div class="small mb-1">
                        <span class="text-warning">
                            <c:forEach begin="1" end="5" var="i"><i class="${(not empty rs and i <= rs.rounded) ? 'fas' : 'far'} fa-star"></i></c:forEach>
                        </span>
                        <span class="text-muted">
                            <c:choose>
                                <c:when test="${not empty rs}">(<fmt:formatNumber value="${rs.average}" maxFractionDigits="1"/> · ${rs.count})</c:when>
                                <c:otherwise>Chưa có đánh giá</c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <p class="text-danger fw-bold"><fmt:formatNumber value="${p.basePrice}" pattern="#,##0"/> ₫</p>
                    <a href="${pageContext.request.contextPath}/product/${p.id}" class="btn btn-sm btn-warning w-100">Xem</a>
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
                <img src="${p.imageUrl}" class="card-img-top" style="height:180px;object-fit:cover;">
                <c:if test="${p.outOfStock}">
                    <span class="badge bg-danger position-absolute top-0 end-0 m-2">
                        <i class="fas fa-times-circle"></i> Hết hàng
                    </span>
                </c:if>
                <div class="card-body">
                    <h6>${p.name}</h6>
                    <c:set var="rs" value="${ratings[p.id]}"/>
                    <div class="small mb-1">
                        <span class="text-warning">
                            <c:forEach begin="1" end="5" var="i"><i class="${(not empty rs and i <= rs.rounded) ? 'fas' : 'far'} fa-star"></i></c:forEach>
                        </span>
                        <span class="text-muted">
                            <c:choose>
                                <c:when test="${not empty rs}">(<fmt:formatNumber value="${rs.average}" maxFractionDigits="1"/> · ${rs.count})</c:when>
                                <c:otherwise>Chưa có đánh giá</c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <p class="text-danger fw-bold"><fmt:formatNumber value="${p.basePrice}" pattern="#,##0"/> ₫</p>
                    <a href="${pageContext.request.contextPath}/product/${p.id}" class="btn btn-sm btn-warning w-100">Xem</a>
                </div>
            </div>
        </div>
    </c:forEach>
</div>