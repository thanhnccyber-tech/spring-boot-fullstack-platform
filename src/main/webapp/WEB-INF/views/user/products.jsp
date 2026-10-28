<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Danh sách sản phẩm</title>

<%-- Giữ sort khi đổi danh mục --%>
<c:set var="sortQuery" value="${not empty sort and sort != 'newest' ? '&sort='.concat(sort) : ''}"/>

<div class="d-flex flex-wrap gap-2 mb-3">
    <a href="${pageContext.request.contextPath}/products${sortQuery}"
       class="btn btn-sm ${fn:escapeXml(empty categoryId ? 'btn-dark' : 'btn-outline-dark')}">Tất cả</a>
    <c:forEach items="${categories}" var="cc">
        <a href="${pageContext.request.contextPath}/products?categoryId=${fn:escapeXml(cc.id)}${sortQuery}"
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
    <div class="col-md-3">
        <select name="sort" class="form-select">
            <option value="newest"       <c:if test="${sort == 'newest'}">selected</c:if>>Mới nhất</option>
            <option value="bestSeller"   <c:if test="${sort == 'bestSeller'}">selected</c:if>>Bán chạy nhất</option>
            <option value="topRated"     <c:if test="${sort == 'topRated'}">selected</c:if>>Đánh giá cao</option>
            <option value="mostFavorite" <c:if test="${sort == 'mostFavorite'}">selected</c:if>>Yêu thích nhất</option>
            <option value="priceAsc"     <c:if test="${sort == 'priceAsc'}">selected</c:if>>Giá tăng dần</option>
            <option value="priceDesc"    <c:if test="${sort == 'priceDesc'}">selected</c:if>>Giá giảm dần</option>
            <option value="nameAsc"      <c:if test="${sort == 'nameAsc'}">selected</c:if>>Tên A → Z</option>
            <option value="nameDesc"     <c:if test="${sort == 'nameDesc'}">selected</c:if>>Tên Z → A</option>
        </select>
    </div>
    <div class="col-md-1">
        <button class="btn btn-warning w-100">Lọc</button>
    </div>
</form>

<c:if test="${not empty pageData.content}">
    <div class="text-muted small mb-2">
        Hiển thị <b>${pageData.numberOfElements}</b> / <b>${pageData.totalElements}</b> sản phẩm
        <c:if test="${pageData.totalPages > 1}"> — Trang ${pageData.number + 1} / ${pageData.totalPages}</c:if>
    </div>
</c:if>

<c:if test="${empty pageData.content}">
    <div class="empty-state">
        <i class="fas fa-magnifying-glass"></i>
        <h5>Không tìm thấy món phù hợp</h5>
        <p class="text-muted mb-3">Thử bớt bộ lọc, đổi từ khoá hoặc xem toàn bộ thực đơn nhé.</p>
        <a href="${pageContext.request.contextPath}/products" class="btn btn-warning">Xem tất cả món</a>
    </div>
</c:if>

<div class="row g-3">
    <c:forEach items="${pageData.content}" var="p">
        <div class="col-6 col-md-3">
            <c:set var="cardProduct" value="${p}" scope="request"/>
            <jsp:include page="/WEB-INF/views/user/_product-card.jsp"/>
        </div>
    </c:forEach>
</div>

<c:if test="${pageData.totalPages > 1}">
<nav class="mt-4">
    <ul class="pagination justify-content-center flex-wrap">
        <%-- Nút Trang trước --%>
        <li class="page-item ${pageData.first ? 'disabled' : ''}">
            <a class="page-link"
               href="?page=${pageData.number - 1}&kw=${fn:escapeXml(kw)}&categoryId=${fn:escapeXml(categoryId)}&minPrice=${fn:escapeXml(minPrice)}&maxPrice=${fn:escapeXml(maxPrice)}&sort=${fn:escapeXml(sort)}"
               aria-label="Trang trước">
                <i class="fas fa-chevron-left"></i>
            </a>
        </li>

        <%-- Cửa sổ 5 trang quanh trang hiện tại --%>
        <c:set var="startPage" value="${pageData.number - 2}"/>
        <c:if test="${startPage < 0}"><c:set var="startPage" value="0"/></c:if>
        <c:set var="endPage" value="${startPage + 4}"/>
        <c:if test="${endPage >= pageData.totalPages}">
            <c:set var="endPage" value="${pageData.totalPages - 1}"/>
            <c:set var="startPage" value="${endPage - 4}"/>
            <c:if test="${startPage < 0}"><c:set var="startPage" value="0"/></c:if>
        </c:if>

        <c:if test="${startPage > 0}">
            <li class="page-item"><a class="page-link"
                href="?page=0&kw=${fn:escapeXml(kw)}&categoryId=${fn:escapeXml(categoryId)}&minPrice=${fn:escapeXml(minPrice)}&maxPrice=${fn:escapeXml(maxPrice)}&sort=${fn:escapeXml(sort)}">1</a></li>
            <c:if test="${startPage > 1}">
                <li class="page-item disabled"><span class="page-link">…</span></li>
            </c:if>
        </c:if>

        <c:forEach begin="${startPage}" end="${endPage}" var="i">
            <li class="page-item ${i == pageData.number ? 'active' : ''}">
                <a class="page-link"
                   href="?page=${i}&kw=${fn:escapeXml(kw)}&categoryId=${fn:escapeXml(categoryId)}&minPrice=${fn:escapeXml(minPrice)}&maxPrice=${fn:escapeXml(maxPrice)}&sort=${fn:escapeXml(sort)}">${i + 1}</a>
            </li>
        </c:forEach>

        <c:if test="${endPage < pageData.totalPages - 1}">
            <c:if test="${endPage < pageData.totalPages - 2}">
                <li class="page-item disabled"><span class="page-link">…</span></li>
            </c:if>
            <li class="page-item"><a class="page-link"
                href="?page=${pageData.totalPages - 1}&kw=${fn:escapeXml(kw)}&categoryId=${fn:escapeXml(categoryId)}&minPrice=${fn:escapeXml(minPrice)}&maxPrice=${fn:escapeXml(maxPrice)}&sort=${fn:escapeXml(sort)}">${pageData.totalPages}</a></li>
        </c:if>

        <%-- Nút Trang sau --%>
        <li class="page-item ${pageData.last ? 'disabled' : ''}">
            <a class="page-link"
               href="?page=${pageData.number + 1}&kw=${fn:escapeXml(kw)}&categoryId=${fn:escapeXml(categoryId)}&minPrice=${fn:escapeXml(minPrice)}&maxPrice=${fn:escapeXml(maxPrice)}&sort=${fn:escapeXml(sort)}"
               aria-label="Trang sau">
                <i class="fas fa-chevron-right"></i>
            </a>
        </li>
    </ul>
</nav>
</c:if>