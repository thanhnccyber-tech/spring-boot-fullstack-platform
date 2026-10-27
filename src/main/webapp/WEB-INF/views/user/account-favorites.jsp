<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Sản phẩm yêu thích</title>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="small mb-3"><a href="${ctx}/">Trang chủ</a> / <a href="${ctx}/profile">Tài khoản</a> / Sản phẩm yêu thích</nav>
<div class="row g-4">
    <div class="col-lg-4 col-xl-3">
        <jsp:include page="/WEB-INF/views/user/_account-menu.jsp"><jsp:param name="active" value="favorites"/></jsp:include>
    </div>
    <div class="col-lg-8 col-xl-9">

        <div class="checkout-box">
            <h4 class="mb-3" style="color:#956542;font-weight:700">Sản phẩm yêu thích</h4>
            <div class="row g-3">
            <c:forEach items="${favorites}" var="f">
                <div class="col-sm-6 col-xl-4">
                    <div class="card h-100 shadow-sm">
                        <img src="${fn:escapeXml(f.product.thumbUrl)}" loading="lazy" class="card-img-top" style="height:150px;object-fit:cover" alt="">
                        <div class="card-body">
                            <h6>${fn:escapeXml(f.product.name)}</h6>
                            <p class="text-danger fw-bold mb-2"><fmt:formatNumber value="${f.product.basePrice}" pattern="#,##0"/> ₫</p>
                            <div class="d-flex gap-1">
                                <a href="${ctx}/product/${fn:escapeXml(f.product.id)}" class="btn btn-sm btn-warning flex-grow-1"><i class="fas fa-cart-plus"></i> Đặt mua</a>
                                <form action="${ctx}/account/favorites/toggle" method="post"><input type="hidden" name="productId" value="${fn:escapeXml(f.product.id)}"><button class="btn btn-sm btn-outline-danger" title="Bỏ yêu thích"><i class="fas fa-heart-crack"></i></button></form>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
            </div>
            <c:if test="${empty favorites}">
                <div class="empty-state">
                    <i class="far fa-heart"></i>
                    <h5>Chưa có món yêu thích</h5>
                    <p class="text-muted mb-3">Bấm ♥ trên thẻ món để lưu lại, lần sau đặt nhanh hơn.</p>
                    <a href="${ctx}/products" class="btn btn-warning">Khám phá menu</a>
                </div>
            </c:if>
        </div>
    </div>
</div>
