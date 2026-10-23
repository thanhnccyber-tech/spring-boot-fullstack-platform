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

            <c:if test="${product.allowSize}">
                <label class="form-label fw-bold">Size:</label>
                <div class="mb-3">
                    <c:forEach items="${sizes}" var="s" varStatus="st">
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="sizeId" value="${s.id}"
                                <c:if test="${(product.defaultSize != null && product.defaultSize.id == s.id)
                                              || (product.defaultSize == null && st.first)}">checked</c:if>>
                            <label class="form-check-label">
                                ${s.name}
                                <c:if test="${s.extraPrice > 0}">
                                    (+<fmt:formatNumber value="${s.extraPrice}" pattern="#,##0"/> ₫)
                                </c:if>
                            </label>
                        </div>
                    </c:forEach>
                </div>
            </c:if>

            <c:if test="${product.allowSugar}">
                <label class="form-label fw-bold">Mức đường:</label>
                <select name="sugarId" class="form-select mb-3">
                    <c:forEach items="${sugars}" var="s">
                        <option value="${s.id}"
                            <c:if test="${(product.defaultSugar != null && product.defaultSugar.id == s.id)
                                          || (product.defaultSugar == null && s.percent == 50)}">selected</c:if>>
                            ${s.name}
                        </option>
                    </c:forEach>
                </select>
            </c:if>

            <c:if test="${product.allowIce}">
                <label class="form-label fw-bold">Mức đá:</label>
                <select name="iceId" class="form-select mb-3">
                    <c:forEach items="${ices}" var="s">
                        <option value="${s.id}"
                            <c:if test="${(product.defaultIce != null && product.defaultIce.id == s.id)
                                          || (product.defaultIce == null && s.percent == 50)}">selected</c:if>>
                            ${s.name}
                        </option>
                    </c:forEach>
                </select>
            </c:if>

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