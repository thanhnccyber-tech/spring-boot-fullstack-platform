<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>${fn:escapeXml(product.name)}</title>

<c:if test="${not empty error}">
    <div class="alert alert-danger">${fn:escapeXml(error)}</div>
</c:if>

<div class="row">
    <div class="col-md-5">
        <img src="${fn:escapeXml(product.imageUrl)}" class="img-fluid rounded shadow">
    </div>
    <div class="col-md-7">
        <h2>${fn:escapeXml(product.name)}
            <c:if test="${product.outOfStock}">
                <span class="badge bg-danger fs-6 align-middle">Hết hàng</span>
            </c:if>
        </h2>
        <a href="#reviews" class="text-decoration-none d-inline-block mb-2">
            <span class="text-warning">
                <c:forEach begin="1" end="5" var="i"><i class="${fn:escapeXml(i <= ratingSummary.rounded ? 'fas' : 'far')} fa-star"></i></c:forEach>
            </span>
            <span class="text-muted small">
                <c:choose>
                    <c:when test="${ratingSummary.count > 0}">
                        <fmt:formatNumber value="${ratingSummary.average}" maxFractionDigits="1"/>/5 · ${fn:escapeXml(ratingSummary.count)} đánh giá
                    </c:when>
                    <c:otherwise>Chưa có đánh giá</c:otherwise>
                </c:choose>
            </span>
        </a>
        <p class="text-danger fs-3 fw-bold">
            <fmt:formatNumber value="${product.basePrice}" pattern="#,##0"/> ₫
        </p>
        <p>${fn:escapeXml(product.description)}</p>

        <c:if test="${product.outOfStock}">
            <div class="alert alert-warning">
                <i class="fas fa-exclamation-triangle"></i>
                Sản phẩm hiện đang <b>tạm hết hàng</b>. Vui lòng quay lại sau hoặc chọn sản phẩm khác.
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/cart/add" method="post">
            <input type="hidden" name="productId" value="${fn:escapeXml(product.id)}">

            <c:if test="${product.allowSize}">
                <label class="form-label fw-bold">Size:</label>
                <div class="mb-3">
                    <c:forEach items="${sizes}" var="s" varStatus="st">
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="sizeId" value="${fn:escapeXml(s.id)}" data-price="${fn:escapeXml(s.extraPrice)}"
                                <c:if test="${(product.defaultSize != null && product.defaultSize.id == s.id)
                                              || (product.defaultSize == null && st.first)}">checked</c:if>
                                <c:if test="${product.outOfStock}">disabled</c:if>>
                            <label class="form-check-label">
                                ${fn:escapeXml(s.name)}
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
                <select name="sugarId" class="form-select mb-3" <c:if test="${product.outOfStock}">disabled</c:if>>
                    <c:forEach items="${sugars}" var="s">
                        <option value="${fn:escapeXml(s.id)}"
                            <c:if test="${(product.defaultSugar != null && product.defaultSugar.id == s.id)
                                          || (product.defaultSugar == null && s.percent == 50)}">selected</c:if>>
                            ${fn:escapeXml(s.name)}
                        </option>
                    </c:forEach>
                </select>
            </c:if>

            <c:if test="${product.allowIce}">
                <label class="form-label fw-bold">Mức đá:</label>
                <select name="iceId" class="form-select mb-3" <c:if test="${product.outOfStock}">disabled</c:if>>
                    <c:forEach items="${ices}" var="s">
                        <option value="${fn:escapeXml(s.id)}"
                            <c:if test="${(product.defaultIce != null && product.defaultIce.id == s.id)
                                          || (product.defaultIce == null && s.percent == 50)}">selected</c:if>>
                            ${fn:escapeXml(s.name)}
                        </option>
                    </c:forEach>
                </select>
            </c:if>

            <label class="form-label fw-bold">Topping:</label>
            <div class="mb-3">
                <c:forEach items="${toppings}" var="t">
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" name="toppingIds" value="${fn:escapeXml(t.id)}" data-price="${fn:escapeXml(t.price)}"
                               <c:if test="${product.outOfStock}">disabled</c:if>>
                        <label class="form-check-label">
                            ${fn:escapeXml(t.name)} (+<fmt:formatNumber value="${t.price}" pattern="#,##0"/> ₫)
                        </label>
                    </div>
                </c:forEach>
            </div>

            <label class="form-label">Số lượng:</label>
            <input type="number" name="quantity" value="1" min="1" class="form-control mb-3"
                   style="width:120px" <c:if test="${product.outOfStock}">disabled</c:if>>

            <div class="d-flex justify-content-between align-items-center mb-3 p-3 bg-light rounded">
                <span class="fw-bold">Tạm tính:</span>
                <span class="text-danger fs-4 fw-bold" id="liveTotal">
                    <fmt:formatNumber value="${product.basePrice}" pattern="#,##0"/> ₫
                </span>
            </div>

            <c:choose>
                <c:when test="${product.outOfStock}">
                    <button type="button" class="btn btn-secondary btn-lg w-100" disabled>
                        <i class="fas fa-times-circle"></i> Sản phẩm đã hết hàng
                    </button>
                </c:when>
                <c:otherwise>
                    <button class="btn btn-warning btn-lg w-100">
                        <i class="fas fa-cart-plus"></i> Thêm vào giỏ
                    </button>
                </c:otherwise>
            </c:choose>
        </form>
    </div>
</div>


<%-- ===================== ĐÁNH GIÁ & BÌNH LUẬN ===================== --%>
<hr class="my-5">
<section id="reviews">
    <h4 class="mb-3"><i class="fas fa-star text-warning"></i> Đánh giá &amp; bình luận
        <c:if test="${ratingSummary.count > 0}">
            <small class="text-muted fs-6">
                — <fmt:formatNumber value="${ratingSummary.average}" maxFractionDigits="1"/>/5
                (${fn:escapeXml(ratingSummary.count)} đánh giá)
            </small>
        </c:if>
    </h4>

    <c:if test="${not empty reviewSuccess}"><div class="alert alert-success">${fn:escapeXml(reviewSuccess)}</div></c:if>
    <c:if test="${not empty reviewError}"><div class="alert alert-danger">${fn:escapeXml(reviewError)}</div></c:if>

    <c:choose>
        <c:when test="${not loggedIn}">
            <div class="alert alert-light border">
                <a href="${pageContext.request.contextPath}/login">Đăng nhập</a> để đánh giá sản phẩm này.
            </div>
        </c:when>
        <c:when test="${not canReview}">
            <div class="alert alert-light border">
                Bạn cần đặt và nhận sản phẩm này trước khi đánh giá.
            </div>
        </c:when>
        <c:otherwise>
            <form action="${pageContext.request.contextPath}/review/${fn:escapeXml(product.id)}" method="post"
                  class="card card-body shadow-sm mb-4">
                <label class="form-label fw-bold">
                    <c:choose>
                        <c:when test="${not empty myReview}">Cập nhật đánh giá của bạn</c:when>
                        <c:otherwise>Đánh giá của bạn</c:otherwise>
                    </c:choose>
                </label>
                <div class="star-input mb-2">
                        <input type="radio" id="star5" name="rating" value="5" required
                               <c:if test="${myReview.rating == 5}">checked</c:if>>
                        <label for="star5" title="5 sao"><i class="fas fa-star"></i></label>
                        <input type="radio" id="star4" name="rating" value="4" required
                               <c:if test="${myReview.rating == 4}">checked</c:if>>
                        <label for="star4" title="4 sao"><i class="fas fa-star"></i></label>
                        <input type="radio" id="star3" name="rating" value="3" required
                               <c:if test="${myReview.rating == 3}">checked</c:if>>
                        <label for="star3" title="3 sao"><i class="fas fa-star"></i></label>
                        <input type="radio" id="star2" name="rating" value="2" required
                               <c:if test="${myReview.rating == 2}">checked</c:if>>
                        <label for="star2" title="2 sao"><i class="fas fa-star"></i></label>
                        <input type="radio" id="star1" name="rating" value="1" required
                               <c:if test="${myReview.rating == 1}">checked</c:if>>
                        <label for="star1" title="1 sao"><i class="fas fa-star"></i></label>
                </div>
                <textarea name="comment" class="form-control mb-2" rows="3" maxlength="1000"
                          placeholder="Chia sẻ cảm nhận của bạn về món này..."><c:out value="${myReview.comment}"/></textarea>
                <div>
                    <button class="btn btn-warning">
                        <i class="fas fa-paper-plane"></i>
                        <c:choose>
                            <c:when test="${not empty myReview}">Cập nhật</c:when>
                            <c:otherwise>Gửi đánh giá</c:otherwise>
                        </c:choose>
                    </button>
                </div>
            </form>
        </c:otherwise>
    </c:choose>

    <c:if test="${empty reviews}">
        <p class="text-muted">Chưa có đánh giá nào. Hãy là người đầu tiên!</p>
    </c:if>

    <c:forEach items="${reviews}" var="r">
        <div class="d-flex border-bottom py-3">
            <i class="fas fa-user-circle fa-2x text-secondary me-3"></i>
            <div class="flex-grow-1">
                <div class="d-flex justify-content-between">
                    <div>
                        <strong><c:out value="${empty r.user.fullName ? 'Khách hàng' : r.user.fullName}"/></strong>
                        <span class="text-warning ms-2">
                            <c:forEach begin="1" end="5" var="i"><i class="${fn:escapeXml(i <= r.rating ? 'fas' : 'far')} fa-star"></i></c:forEach>
                        </span>
                    </div>
                    <small class="text-muted">
                        ${fn:escapeXml(r.createdAtFormatted)}<c:if test="${r.edited}"> (đã chỉnh sửa)</c:if>
                    </small>
                </div>
                <c:if test="${not empty r.comment}">
                    <div class="review-text mt-1"><c:out value="${r.comment}"/></div>
                </c:if>
                <c:if test="${loggedIn and (r.user.id == currentUserId or isAdminUser)}">
                    <form action="${pageContext.request.contextPath}/review/${fn:escapeXml(r.id)}/delete" method="post"
                          class="mt-1" onsubmit="return confirm('Xoá đánh giá này?');">
                        <input type="hidden" name="productId" value="${fn:escapeXml(product.id)}">
                        <button class="btn btn-link btn-sm text-danger p-0">
                            <i class="fas fa-trash"></i> Xoá
                        </button>
                    </form>
                </c:if>
            </div>
        </div>
    </c:forEach>
</section>

<script>
(function () {
    var base = parseFloat('${product.basePrice}') || 0;
    var out = document.getElementById('liveTotal');
    var form = document.querySelector('form[action$="/cart/add"]');
    if (!out || !form) return;
    function fmt(n) { return Math.round(n).toLocaleString('vi-VN') + ' ₫'; }
    function recalc() {
        var unit = base;
        var size = form.querySelector('input[name="sizeId"]:checked');
        if (size) unit += parseFloat(size.dataset.price) || 0;
        form.querySelectorAll('input[name="toppingIds"]:checked').forEach(function (t) {
            unit += parseFloat(t.dataset.price) || 0;
        });
        var qty = parseInt(form.querySelector('input[name="quantity"]').value, 10) || 1;
        out.textContent = fmt(unit * Math.max(qty, 1));
    }
    form.addEventListener('change', recalc);
    form.addEventListener('input', recalc);
    recalc();
})();
</script>
