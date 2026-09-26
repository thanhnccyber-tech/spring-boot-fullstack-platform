<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<title>${product.name}</title>

<div class="row">
    <div class="col-md-5">
        <img src="${product.imageUrl}" class="img-fluid rounded shadow">
    </div>
    <div class="col-md-7">
        <h2>${product.name}</h2>
        <p class="text-danger fs-3 fw-bold">
            <fmt:formatNumber value="${product.basePrice}" pattern="#,##0"/> ₫
        </p>
        <p>${product.description}</p>
        <form action="${pageContext.request.contextPath}/cart/add" method="post">
            <input type="hidden" name="productId" value="${product.id}">

            <label class="form-label fw-bold">Size:</label>
            <div class="mb-3">
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="size" value="S" checked>
                    <label class="form-check-label">S</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="size" value="M">
                    <label class="form-check-label">M (+5.000)</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="size" value="L">
                    <label class="form-check-label">L (+10.000)</label>
                </div>
            </div>

            <label class="form-label fw-bold">Mức đường:</label>
            <select name="sugarLevel" class="form-select mb-3">
                <option value="0">0%</option>
                <option value="30">30%</option>
                <option value="50" selected>50%</option>
                <option value="100">100%</option>
            </select>

            <label class="form-label fw-bold">Mức đá:</label>
            <select name="iceLevel" class="form-select mb-3">
                <option value="0">Không đá</option>
                <option value="50" selected>Ít đá</option>
                <option value="100">Nhiều đá</option>
            </select>

            <label class="form-label fw-bold">Topping:</label>
            <div class="mb-3">
                <c:forEach items="${toppings}" var="t">
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" name="toppingIds" value="${t.id}">
                        <label class="form-check-label">
                            ${t.name} (+<fmt:formatNumber value="${t.price}" pattern="#,##0"/> ₫)
                        </label>
                    </div>
                </c:forEach>
            </div>

            <label class="form-label">Số lượng:</label>
            <input type="number" name="quantity" value="1" min="1" class="form-control mb-3" style="width:120px">

            <button class="btn btn-warning btn-lg w-100">
                <i class="fas fa-cart-plus"></i> Thêm vào giỏ
            </button>
        </form>
    </div>
</div>