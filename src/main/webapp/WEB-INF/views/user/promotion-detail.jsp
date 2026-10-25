<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title><c:out value="${promo.title}"/></title>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="small mb-3"><a href="${ctx}/">Trang chủ</a> / <a href="${ctx}/khuyen-mai">Khuyến mãi</a> / ${fn:escapeXml(promo.title)}</nav>

<div class="checkout-box">
    <h3>${fn:escapeXml(promo.title)}</h3>
    <div class="small text-muted mb-3"><i class="far fa-eye"></i> ${fn:escapeXml(promo.views + 1)} lượt xem · ${fn:escapeXml(promo.createdAtFormatted)}</div>
    <c:if test="${not empty promo.imageUrl}"><img src="${fn:escapeXml(promo.imageUrl)}" class="img-fluid rounded mb-3" alt=""></c:if>
    <c:if test="${not empty promo.summary}"><p class="lead">${fn:escapeXml(promo.summary)}</p></c:if>
    <c:if test="${not empty promo.content}"><p style="white-space:pre-line">${fn:escapeXml(promo.content)}</p></c:if>

    <c:if test="${not empty promo.voucherCode}">
        <div class="alert alert-warning d-flex align-items-center justify-content-between flex-wrap gap-2">
            <span>Mã giảm giá: <b class="fs-5" id="promoCode">${fn:escapeXml(promo.voucherCode)}</b></span>
            <button type="button" class="btn btn-sm btn-dark" id="copyCode">Sao chép mã</button>
        </div>
        <script>
            document.getElementById('copyCode').addEventListener('click', function () {
                var t = document.getElementById('promoCode').textContent;
                if (navigator.clipboard) { navigator.clipboard.writeText(t); }
                this.textContent = 'Đã sao chép!';
            });
        </script>
    </c:if>
    <a href="${ctx}/products" class="btn btn-warning mt-2">Đặt mua ngay</a>
</div>
