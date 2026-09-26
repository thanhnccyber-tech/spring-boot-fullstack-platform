<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<title>Danh sách sản phẩm</title>

<form class="row g-2 mb-4">
    <div class="col-md-3">
        <input type="text" name="kw" value="${kw}" class="form-control" placeholder="Tìm theo tên...">
    </div>
    <div class="col-md-3">
        <select name="categoryId" class="form-select">
            <option value="">-- Danh mục --</option>
            <c:forEach items="${categories}" var="c">
                <option value="${c.id}" <c:if test="${categoryId == c.id}">selected</c:if>>${c.name}</option>
            </c:forEach>
        </select>
    </div>
    <div class="col-md-2"><input type="number" name="minPrice" value="${minPrice}" class="form-control" placeholder="Giá từ"></div>
    <div class="col-md-2"><input type="number" name="maxPrice" value="${maxPrice}" class="form-control" placeholder="Giá đến"></div>
    <div class="col-md-2">
        <button class="btn btn-warning w-100">Lọc</button>
    </div>
</form>

<div class="row g-3">
    <c:forEach items="${pageData.content}" var="p">
        <div class="col-md-3">
            <div class="card h-100 shadow-sm">
                <img src="${p.imageUrl}" style="height:180px;object-fit:cover;" class="card-img-top">
                <div class="card-body">
                    <h6>${p.name}</h6>
                    <p class="text-danger fw-bold"><fmt:formatNumber value="${p.basePrice}" pattern="#,##0"/> ₫</p>
                    <a href="${pageContext.request.contextPath}/product/${p.id}" class="btn btn-warning btn-sm w-100">Chi tiết</a>
                </div>
            </div>
        </div>
    </c:forEach>
</div>

<nav class="mt-4">
    <ul class="pagination justify-content-center">
        <c:forEach begin="0" end="${pageData.totalPages - 1}" var="i">
            <li class="page-item ${i == pageData.number ? 'active' : ''}">
                <a class="page-link" href="?page=${i}&kw=${kw}&categoryId=${categoryId}&minPrice=${minPrice}&maxPrice=${maxPrice}&sort=${sort}">${i+1}</a>
            </li>
        </c:forEach>
    </ul>
</nav>