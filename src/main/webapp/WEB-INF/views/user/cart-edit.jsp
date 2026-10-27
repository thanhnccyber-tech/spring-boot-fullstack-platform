<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Sửa món trong giỏ</title>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="small mb-3"><a href="${ctx}/cart">Giỏ hàng</a> / Sửa món</nav>

<div class="checkout-box" style="max-width:640px">
    <div class="d-flex gap-3 mb-3">
        <c:if test="${not empty item.imageUrl}"><img src="${fn:escapeXml(item.thumbUrl)}" alt="" style="width:72px;height:72px;object-fit:cover;border-radius:10px"></c:if>
        <div>
            <h5 class="mb-1">${fn:escapeXml(product.name)}</h5>
            <div class="text-muted small">Giá gốc <fmt:formatNumber value="${product.basePrice}" pattern="#,##0"/> ₫</div>
        </div>
    </div>

    <form action="${ctx}/cart/edit" method="post">
        <input type="hidden" name="index" value="${fn:escapeXml(index)}">

        <c:if test="${not empty sizes}">
            <label class="form-label fw-bold">Size</label>
            <div class="mb-3">
                <c:forEach items="${sizes}" var="s">
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" name="sizeId" id="sz${fn:escapeXml(s.id)}" value="${fn:escapeXml(s.id)}"
                               <c:if test="${item.sizeId == s.id}">checked</c:if>>
                        <label class="form-check-label" for="sz${fn:escapeXml(s.id)}">${fn:escapeXml(s.name)}
                            <c:if test="${s.extraPrice > 0}">(+<fmt:formatNumber value="${s.extraPrice}" pattern="#,##0"/> ₫)</c:if></label>
                    </div>
                </c:forEach>
            </div>
        </c:if>

        <c:if test="${not empty sugars}">
            <label class="form-label fw-bold">Mức đường</label>
            <select name="sugarId" class="form-select mb-3">
                <c:forEach items="${sugars}" var="s">
                    <option value="${fn:escapeXml(s.id)}" <c:if test="${item.sugarId == s.id}">selected</c:if>>${fn:escapeXml(s.name)}</option>
                </c:forEach>
            </select>
        </c:if>

        <c:if test="${not empty ices}">
            <label class="form-label fw-bold">Mức đá</label>
            <select name="iceId" class="form-select mb-3">
                <c:forEach items="${ices}" var="s">
                    <option value="${fn:escapeXml(s.id)}" <c:if test="${item.iceId == s.id}">selected</c:if>>${fn:escapeXml(s.name)}</option>
                </c:forEach>
            </select>
        </c:if>

        <c:if test="${not empty toppings}">
            <label class="form-label fw-bold">Topping</label>
            <div class="mb-3">
                <c:forEach items="${toppings}" var="t">
                    <c:set var="tChecked" value="false"/>
                    <c:forEach items="${item.toppingIds}" var="tid"><c:if test="${tid == t.id}"><c:set var="tChecked" value="true"/></c:if></c:forEach>
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" name="toppingIds" id="tp${fn:escapeXml(t.id)}" value="${fn:escapeXml(t.id)}"
                               <c:if test="${tChecked}">checked</c:if>>
                        <label class="form-check-label" for="tp${fn:escapeXml(t.id)}">${fn:escapeXml(t.name)} (+<fmt:formatNumber value="${t.price}" pattern="#,##0"/> ₫)</label>
                    </div>
                </c:forEach>
            </div>
        </c:if>

        <label class="form-label fw-bold">Số lượng</label>
        <input type="number" name="quantity" min="1" max="50" value="${fn:escapeXml(item.quantity)}" class="form-control mb-4" style="width:120px">

        <div class="d-flex gap-2">
            <button class="btn btn-warning flex-grow-1 fw-bold">Lưu thay đổi</button>
            <a href="${ctx}/cart" class="btn btn-outline-secondary">Hủy</a>
        </div>
    </form>
</div>
