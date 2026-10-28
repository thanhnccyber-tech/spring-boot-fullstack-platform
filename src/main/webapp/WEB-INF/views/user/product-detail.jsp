<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>${fn:escapeXml(product.name)}</title>

<c:if test="${not empty success}">
    <div class="alert alert-success alert-dismissible fade show auto-hide">${fn:escapeXml(success)}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>
</c:if>
<c:if test="${not empty error}">
    <div class="alert alert-danger">${fn:escapeXml(error)}</div>
</c:if>

<nav class="pd-breadcrumb" aria-label="breadcrumb">
    <a href="${pageContext.request.contextPath}/">Trang chủ</a>
    <span>/</span>
    <a href="${pageContext.request.contextPath}/products">Sản phẩm</a>
</nav>

<div class="pd-wrap">
    <div class="pd-media">
        <div class="pd-media-frame">
            <img src="${fn:escapeXml(product.largeUrl)}" alt="${fn:escapeXml(product.name)}">
        </div>
    </div>

    <div class="pd-info">
        <div class="pd-title-row">
            <h1 class="pd-title">${fn:escapeXml(product.name)}
                <c:if test="${product.outOfStock}">
                    <span class="badge bg-danger align-middle">Hết hàng</span>
                </c:if>
            </h1>
            <c:if test="${not empty sessionScope.userId}">
                <form action="${pageContext.request.contextPath}/account/favorites/toggle" method="post" class="pd-fav">
                    <input type="hidden" name="productId" value="${fn:escapeXml(product.id)}">
                    <input type="hidden" name="back" value="/product/${fn:escapeXml(product.id)}">
                    <button class="btn btn-sm ${fn:escapeXml(isFavorite ? 'btn-danger' : 'btn-outline-danger')}" title="Yêu thích"><i class="fas fa-heart"></i></button>
                </form>
            </c:if>
        </div>

        <a href="#reviews" class="pd-rating">
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

        <c:if test="${product.outOfStock}">
            <div class="alert alert-warning mt-3 mb-0">
                <i class="fas fa-exclamation-triangle"></i>
                Sản phẩm hiện đang <b>tạm hết hàng</b>. Vui lòng quay lại sau hoặc chọn sản phẩm khác.
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/cart/add" method="post" class="pd-form">
            <input type="hidden" name="productId" value="${fn:escapeXml(product.id)}">

            <%-- Giá + số lượng cùng một hàng --%>
            <div class="pd-price-row">
                <div class="pd-price"><fmt:formatNumber value="${product.basePrice}" pattern="#,##0"/> ₫</div>
                <div class="pd-qty" role="group" aria-label="Số lượng">
                    <button type="button" class="pd-qty-btn" data-qty="-1" aria-label="Giảm" <c:if test="${product.outOfStock}">disabled</c:if>><i class="fas fa-minus"></i></button>
                    <input type="number" name="quantity" value="${productDetailQuantity}" min="1" max="50" aria-label="Số lượng"
                           <c:if test="${product.outOfStock}">disabled</c:if>>
                    <button type="button" class="pd-qty-btn" data-qty="1" aria-label="Tăng" <c:if test="${product.outOfStock}">disabled</c:if>><i class="fas fa-plus"></i></button>
                </div>
            </div>

            <c:if test="${product.allowSize}">
                <div class="pd-group">
                    <div class="pd-group-title">Chọn kích cỡ</div>
                    <div class="pd-options">
                        <c:forEach items="${sizes}" var="s" varStatus="st">
                            <label class="pd-chip pd-chip-size">
                                <input type="radio" name="sizeId" value="${fn:escapeXml(s.id)}" data-price="${fn:escapeXml(s.extraPrice)}"
                                    <c:if test="${(product.defaultSize != null && product.defaultSize.id == s.id)
                                                  || (product.defaultSize == null && st.first)}">checked</c:if>
                                    <c:if test="${product.outOfStock}">disabled</c:if>>
                                <span>
                                    <b>${fn:escapeXml(s.name)}</b>
                                    <small><c:choose>
                                        <c:when test="${s.extraPrice > 0}">+<fmt:formatNumber value="${s.extraPrice}" pattern="#,##0"/> ₫</c:when>
                                        <c:otherwise>0 ₫</c:otherwise>
                                    </c:choose></small>
                                </span>
                            </label>
                        </c:forEach>
                    </div>
                </div>
            </c:if>

            <c:if test="${product.allowSugar}">
                <div class="pd-group">
                    <div class="pd-group-title">Mức đường</div>
                    <div class="pd-options">
                        <c:forEach items="${sugars}" var="s">
                            <label class="pd-chip">
                                <input type="radio" name="sugarId" value="${fn:escapeXml(s.id)}"
                                    <c:if test="${(product.defaultSugar != null && product.defaultSugar.id == s.id)
                                                  || (product.defaultSugar == null && s.percent == 50)}">checked</c:if>
                                    <c:if test="${product.outOfStock}">disabled</c:if>>
                                <span>${fn:escapeXml(s.name)}</span>
                            </label>
                        </c:forEach>
                    </div>
                </div>
            </c:if>

            <c:if test="${product.allowIce}">
                <div class="pd-group">
                    <div class="pd-group-title">Mức đá</div>
                    <div class="pd-options">
                        <c:forEach items="${ices}" var="s">
                            <label class="pd-chip">
                                <input type="radio" name="iceId" value="${fn:escapeXml(s.id)}"
                                    <c:if test="${(product.defaultIce != null && product.defaultIce.id == s.id)
                                                  || (product.defaultIce == null && s.percent == 50)}">checked</c:if>
                                    <c:if test="${product.outOfStock}">disabled</c:if>>
                                <span>${fn:escapeXml(s.name)}</span>
                            </label>
                        </c:forEach>
                    </div>
                </div>
            </c:if>

            <c:if test="${not empty toppings}">
                <div class="pd-group">
                    <div class="pd-group-title">Topping</div>
                    <div class="pd-options">
                        <c:forEach items="${toppings}" var="t">
                            <label class="pd-chip">
                                <input type="checkbox" name="toppingIds" value="${fn:escapeXml(t.id)}" data-price="${fn:escapeXml(t.price)}"
                                       <c:if test="${product.outOfStock}">disabled</c:if>>
                                <span>${fn:escapeXml(t.name)} <small>+<fmt:formatNumber value="${t.price}" pattern="#,##0"/> ₫</small></span>
                            </label>
                        </c:forEach>
                    </div>
                </div>
            </c:if>

            <c:choose>
                <c:when test="${product.outOfStock}">
                    <button type="button" class="pd-add" disabled>
                        <i class="fas fa-times-circle"></i> Sản phẩm đã hết hàng
                    </button>
                </c:when>
                <c:otherwise>
                    <button class="pd-add">
                        <i class="fas fa-cart-plus"></i> Thêm vào giỏ hàng :
                        <span id="liveTotal"><fmt:formatNumber value="${product.basePrice}" pattern="#,##0"/> ₫</span>
                    </button>
                </c:otherwise>
            </c:choose>
        </form>

        <c:if test="${not empty product.description}">
            <p class="pd-desc">${fn:escapeXml(product.description)}</p>
        </c:if>
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
                  enctype="multipart/form-data"
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
                <label for="reviewComment" class="form-label fw-semibold">Chia sẻ cảm nhận <span class="text-muted fw-normal">(không bắt buộc)</span></label>
                <textarea id="reviewComment" name="comment" class="form-control mb-2" rows="3"
                          placeholder="Bạn cảm thấy thế nào về sản phẩm? Hãy chia sẻ nếu bạn muốn."><c:out value="${myReview.comment}"/></textarea>
                <label for="reviewMedia" class="form-label">Ảnh hoặc video (không bắt buộc)</label>
                <input id="reviewMedia" type="file" name="media" class="form-control mb-2"
                       accept="image/jpeg,image/png,image/gif,image/webp,video/mp4,video/webm,video/x-msvideo">
                <small class="text-muted d-block mb-2">Ảnh tối đa 5 MB; video MP4, WEBM hoặc AVI tối đa 10 MB.</small>
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
                <c:if test="${not empty r.mediaUrl}">
                    <c:choose>
                        <c:when test="${not empty r.mediaType and fn:startsWith(r.mediaType, 'video/')}">
                            <video class="mt-2" controls preload="metadata" style="max-width: 100%; max-height: 360px;">
                                <source src="${fn:escapeXml(r.mediaUrl)}" type="${fn:escapeXml(r.mediaType)}">
                                Trình duyệt không hỗ trợ phát video.
                            </video>
                        </c:when>
                        <c:otherwise>
                            <img class="mt-2" src="${fn:escapeXml(r.mediaUrl)}" alt="Ảnh đính kèm đánh giá"
                                 loading="lazy" style="max-width: 100%; max-height: 360px; object-fit: contain;">
                        </c:otherwise>
                    </c:choose>
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
    form.querySelectorAll('.pd-qty-btn').forEach(function (b) {
        b.addEventListener('click', function () {
            var q = form.querySelector('input[name="quantity"]');
            var v = (parseInt(q.value, 10) || 1) + parseInt(b.dataset.qty, 10);
            q.value = Math.min(50, Math.max(1, v));
            recalc();
        });
    });
    form.addEventListener('change', recalc);
    form.addEventListener('input', recalc);
    recalc();
})();
</script>
