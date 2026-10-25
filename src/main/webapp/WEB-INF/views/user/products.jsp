<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Danh sách sản phẩm</title>

<div class="d-flex flex-wrap gap-2 mb-3">
    <a href="${pageContext.request.contextPath}/products" class="btn btn-sm ${fn:escapeXml(empty categoryId ? 'btn-dark' : 'btn-outline-dark')}">Tất cả</a>
    <c:forEach items="${categories}" var="cc">
        <a href="${pageContext.request.contextPath}/products?categoryId=${fn:escapeXml(cc.id)}"
           class="btn btn-sm ${fn:escapeXml(categoryId == cc.id ? 'btn-dark' : 'btn-outline-dark')}">${fn:escapeXml(cc.name)}</a>
    </c:forEach>
</div>

<form class="row g-2 mb-4">
    <div class="col-md-2">
        <input type="text" name="kw" value="${fn:escapeXml(kw)}" class="form-control" placeholder="Tìm theo tên...">
    </div>
    <div class="col-md-2">
        <select name="categoryId" class="form-select">
            <option value="">-- Danh mục --</option>
            <c:forEach items="${categories}" var="c">
                <option value="${fn:escapeXml(c.id)}" <c:if test="${categoryId == c.id}">selected</c:if>>${fn:escapeXml(c.name)}</option>
            </c:forEach>
        </select>
    </div>
    <div class="col-md-2"><input type="number" name="minPrice" value="${fn:escapeXml(minPrice)}" class="form-control" placeholder="Giá từ"></div>
    <div class="col-md-2"><input type="number" name="maxPrice" value="${fn:escapeXml(maxPrice)}" class="form-control" placeholder="Giá đến"></div>
    <div class="col-md-2">
        <select name="sort" class="form-select">
            <option value="newest"    <c:if test="${sort == 'newest'}">selected</c:if>>Mới nhất</option>
            <option value="priceAsc"  <c:if test="${sort == 'priceAsc'}">selected</c:if>>Giá tăng dần</option>
            <option value="priceDesc" <c:if test="${sort == 'priceDesc'}">selected</c:if>>Giá giảm dần</option>
            <option value="nameAsc"   <c:if test="${sort == 'nameAsc'}">selected</c:if>>Tên A → Z</option>
            <option value="nameDesc"  <c:if test="${sort == 'nameDesc'}">selected</c:if>>Tên Z → A</option>
        </select>
    </div>
    <div class="col-md-2">
        <button class="btn btn-warning w-100">Lọc</button>
    </div>
</form>

<c:if test="${empty pageData.content}">
    <div class="alert alert-light text-center border">Không tìm thấy sản phẩm phù hợp.</div>
</c:if>

<div class="row g-3">
    <c:forEach items="${pageData.content}" var="p">
        <div class="col-md-3">
            <div class="card h-100 shadow-sm position-relative">
                <img src="${fn:escapeXml(p.imageUrl)}" style="height:180px;object-fit:cover;" class="card-img-top">
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
                    <a href="${pageContext.request.contextPath}/product/${fn:escapeXml(p.id)}" class="btn btn-warning btn-sm w-100"><i class="fas fa-cart-plus"></i> Đặt mua</a>
                </div>
            </div>
        </div>
    </c:forEach>
</div>

<c:if test="${pageData.totalPages > 1}">
<nav class="mt-4">
    <ul class="pagination justify-content-center">
        <c:forEach begin="0" end="${pageData.totalPages - 1}" var="i">
            <li class="page-item ${fn:escapeXml(i == pageData.number ? 'active' : '')}">
                <a class="page-link" href="?page=${fn:escapeXml(i)}&kw=${fn:escapeXml(kw)}&categoryId=${fn:escapeXml(categoryId)}&minPrice=${fn:escapeXml(minPrice)}&maxPrice=${fn:escapeXml(maxPrice)}&sort=${fn:escapeXml(sort)}">${fn:escapeXml(i+1)}</a>
            </li>
        </c:forEach>
    </ul>
</nav>
</c:if>
