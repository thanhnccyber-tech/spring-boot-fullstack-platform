<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Đơn cần giao</title>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<c:if test="${not hasBranch}">
    <div class="alert alert-warning">
        <i class="fas fa-triangle-exclamation"></i>
        Tài khoản của bạn chưa được gán chi nhánh nên chưa có đơn để nhận.
        Vui lòng liên hệ quản lý để được gán chi nhánh.
    </div>
</c:if>

<%-- ========== THỐNG KÊ HÔM NAY ========== --%>
<div class="row g-2 mb-4">
    <div class="col-4">
        <div class="card stat-card shadow-sm text-center h-100">
            <div class="card-body p-2">
                <div class="fs-4 fw-bold text-success">${today.delivered}</div>
                <div class="small text-muted">Giao xong hôm nay</div>
            </div>
        </div>
    </div>
    <div class="col-4">
        <div class="card stat-card shadow-sm text-center h-100">
            <div class="card-body p-2">
                <div class="fs-4 fw-bold text-secondary">${today.failed}</div>
                <div class="small text-muted">Thất bại hôm nay</div>
            </div>
        </div>
    </div>
    <div class="col-4">
        <div class="card stat-card shadow-sm text-center h-100">
            <div class="card-body p-2">
                <div class="fs-5 fw-bold text-danger"><fmt:formatNumber value="${today.codCollected}" pattern="#,##0"/>₫</div>
                <div class="small text-muted">Đã thu COD</div>
            </div>
        </div>
    </div>
</div>

<%-- ========== ĐANG GIAO ========== --%>
<h5 class="mb-3">
    <i class="fas fa-motorcycle text-primary"></i> Đang giao
    <span class="badge bg-primary">${fn:length(delivering)}</span>
</h5>
<c:if test="${empty delivering}">
    <p class="text-muted">Bạn chưa có đơn nào đang giao.</p>
</c:if>
<c:forEach var="o" items="${delivering}">
    <c:set var="card" value="${o}" scope="request"/>
    <c:set var="cardMode" value="delivering" scope="request"/>
    <jsp:include page="/WEB-INF/views/shipper/_order-card.jsp"/>
</c:forEach>

<%-- ========== CHỜ NHẬN ========== --%>
<h5 class="mt-4 mb-3">
    <i class="fas fa-box-open text-warning"></i> Đơn chờ nhận tại chi nhánh
    <span class="badge bg-warning text-dark">${fn:length(available)}</span>
</h5>
<c:if test="${empty available}">
    <div class="text-center text-muted py-4">
        <i class="fas fa-mug-hot fa-2x mb-2"></i>
        <div>Hiện chưa có đơn nào cần giao. Tải lại trang sau ít phút nhé.</div>
        <a href="${ctx}/shipper/dashboard" class="btn btn-outline-secondary btn-sm mt-2">
            <i class="fas fa-rotate"></i> Tải lại
        </a>
    </div>
</c:if>
<c:forEach var="o" items="${available}">
    <c:set var="card" value="${o}" scope="request"/>
    <c:set var="cardMode" value="available" scope="request"/>
    <jsp:include page="/WEB-INF/views/shipper/_order-card.jsp"/>
</c:forEach>
