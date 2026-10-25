<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Ưu đãi của tôi</title>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="small mb-3"><a href="${ctx}/">Trang chủ</a> / <a href="${ctx}/profile">Tài khoản</a> / Ưu đãi của tôi</nav>
<div class="row g-4">
    <div class="col-lg-4 col-xl-3">
        <jsp:include page="/WEB-INF/views/user/_account-menu.jsp"><jsp:param name="active" value="vouchers"/></jsp:include>
    </div>
    <div class="col-lg-8 col-xl-9">

        <div class="checkout-box">
            <h4 class="mb-3" style="color:#8a6d00;font-weight:700">Ưu đãi của tôi</h4>
            <div class="row g-3">
            <c:forEach items="${vouchers}" var="v">
                <div class="col-md-6">
                    <div class="voucher-card">
                        <div class="voucher-code" id="vc-${fn:escapeXml(v.id)}">${fn:escapeXml(v.code)}</div>
                        <div class="fw-bold">
                            <c:choose>
                                <c:when test="${v.percent}">Giảm ${fn:escapeXml(v.discountValue)}%<c:if test="${v.maxDiscountAmount != null}"> (tối đa <fmt:formatNumber value="${v.maxDiscountAmount}" pattern="#,##0"/>đ)</c:if></c:when>
                                <c:otherwise>Giảm <fmt:formatNumber value="${v.discountValue}" pattern="#,##0"/>đ</c:otherwise>
                            </c:choose>
                        </div>
                        <div class="small text-muted">Đơn từ <fmt:formatNumber value="${v.minOrderAmount}" pattern="#,##0"/>đ
                            <c:if test="${v.expiresAt != null}"> · HSD ${fn:escapeXml(fn:replace(v.expiresAt, 'T', ' '))}</c:if></div>
                        <c:if test="${not empty v.description}"><div class="small">${fn:escapeXml(v.description)}</div></c:if>
                        <button type="button" class="btn btn-sm btn-dark mt-2 copy-code" data-code="${fn:escapeXml(v.code)}">Sao chép mã</button>
                    </div>
                </div>
            </c:forEach>
            </div>
            <c:if test="${empty vouchers}"><p class="text-center text-muted my-4">Không có dữ liệu</p></c:if>
            <div class="small text-muted mt-3">Nhập mã ở bước thanh toán để được giảm giá.</div>
        </div>
        <script>
            document.querySelectorAll('.copy-code').forEach(function (b) {
                b.addEventListener('click', function () {
                    if (navigator.clipboard) navigator.clipboard.writeText(b.dataset.code);
                    b.textContent = 'Đã sao chép!';
                });
            });
        </script>
    </div>
</div>
