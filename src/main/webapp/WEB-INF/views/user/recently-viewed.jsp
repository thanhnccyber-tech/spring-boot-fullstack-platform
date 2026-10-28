<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Sản phẩm đã xem</title>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="small mb-3"><a href="${ctx}/">Trang chủ</a> / <a href="${ctx}/profile">Tài khoản</a> / Sản phẩm đã xem</nav>
<div class="row g-4">
    <div class="col-lg-4 col-xl-3">
        <jsp:include page="/WEB-INF/views/user/_account-menu.jsp"><jsp:param name="active" value="viewed"/></jsp:include>
    </div>
    <div class="col-lg-8 col-xl-9">

        <div class="checkout-box">
            <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
                <div>
                    <h4 class="mb-1" style="color:#956542;font-weight:700">
                        <i class="fas fa-clock-rotate-left" style="color:var(--ute-blue-fresh)"></i>
                        Sản phẩm đã xem
                    </h4>
                    <c:if test="${not empty products}">
                        <div class="small text-muted">${fn:length(products)} sản phẩm</div>
                    </c:if>
                </div>
                <c:if test="${not empty products}">
                    <form action="${ctx}/products/recently-viewed/clear" method="post"
                          onsubmit="return confirm('Xóa toàn bộ lịch sử xem?')">
                        <button class="btn btn-sm btn-outline-danger">
                            <i class="fas fa-trash"></i> Xóa lịch sử
                        </button>
                    </form>
                </c:if>
            </div>

            <c:if test="${empty products}">
                <div class="empty-state">
                    <i class="fas fa-clock-rotate-left"></i>
                    <h5>Chưa có sản phẩm nào</h5>
                    <p class="text-muted mb-3">Các món bạn xem gần đây sẽ xuất hiện tại đây.</p>
                    <a href="${ctx}/products" class="btn btn-warning">Khám phá menu</a>
                </div>
            </c:if>

            <c:if test="${not empty products}">
                <div class="row g-3">
                    <c:forEach items="${products}" var="p">
                        <div class="col-6 col-md-4">
                            <c:set var="cardProduct" value="${p}" scope="request"/>
                            <jsp:include page="/WEB-INF/views/user/_product-card.jsp"/>
                        </div>
                    </c:forEach>
                </div>
            </c:if>
        </div>
    </div>
</div>