<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Đơn cần giao</title>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<c:if test="${not hasBranch}">
    <div class="sh-card sh-card-pad mb-3" style="border-left:4px solid var(--sh-amber)">
        <div class="d-flex gap-3">
            <i class="fas fa-triangle-exclamation" style="color:var(--sh-amber);font-size:1.4rem"></i>
            <div>
                <div class="fw-bold mb-1">Chưa được gán chi nhánh</div>
                <div class="sh-small sh-muted">Tài khoản của bạn chưa được gán chi nhánh nên chưa có đơn để nhận. Vui lòng liên hệ quản lý để được gán chi nhánh.</div>
            </div>
        </div>
    </div>
</c:if>

<%-- ===== Stats hôm nay ===== --%>
<div class="sh-stats-row">
    <div class="sh-stat green">
        <div class="sh-stat-value">${today.delivered}</div>
        <div class="sh-stat-label">Đã giao</div>
    </div>
    <div class="sh-stat grey">
        <div class="sh-stat-value">${today.failed}</div>
        <div class="sh-stat-label">Thất bại</div>
    </div>
    <div class="sh-stat red">
        <div class="sh-stat-value money"><fmt:formatNumber value="${today.codCollected}" pattern="#,##0"/>₫</div>
        <div class="sh-stat-label">Thu COD</div>
    </div>
</div>

<%-- ===== Tabs ===== --%>
<c:set var="availCount" value="${fn:length(available)}"/>
<c:set var="delivCount" value="${fn:length(delivering)}"/>
<c:set var="defaultTab" value="${availCount > 0 ? 'available' : 'delivering'}"/>

<div class="sh-tabs">
    <button type="button"
            class="sh-tab ${defaultTab == 'available' ? 'active' : ''}"
            data-tab="available">
        <i class="fas fa-box-open"></i>
        Chờ nhận
        <span class="sh-tab-badge">${availCount}</span>
    </button>
    <button type="button"
            class="sh-tab ${defaultTab == 'delivering' ? 'active' : ''}"
            data-tab="delivering">
        <i class="fas fa-motorcycle"></i>
        Đang giao
        <span class="sh-tab-badge">${delivCount}</span>
    </button>
</div>

<%-- ===== Tab: Chờ nhận ===== --%>
<div class="sh-tab-pane ${defaultTab == 'available' ? 'active' : ''}" data-pane="available">
    <c:if test="${empty available}">
        <div class="sh-empty">
            <i class="fas fa-mug-hot"></i>
            <h5>Chưa có đơn chờ nhận</h5>
            <p>Hiện không có đơn nào cần giao tại chi nhánh của bạn.</p>
            <a href="${ctx}/shipper/dashboard" class="sh-btn sh-btn-outline sh-btn-sm">
                <i class="fas fa-rotate"></i> Tải lại trang
            </a>
        </div>
    </c:if>
    <c:forEach var="o" items="${available}">
        <c:set var="card" value="${o}" scope="request"/>
        <c:set var="cardMode" value="available" scope="request"/>
        <jsp:include page="/WEB-INF/views/shipper/order-card.jsp"/>
    </c:forEach>
</div>

<%-- ===== Tab: Đang giao ===== --%>
<div class="sh-tab-pane ${defaultTab == 'delivering' ? 'active' : ''}" data-pane="delivering">
    <c:if test="${empty delivering}">
        <div class="sh-empty">
            <i class="fas fa-motorcycle"></i>
            <h5>Không có đơn đang giao</h5>
            <p>Nhận đơn ở tab <b>Chờ nhận</b> để bắt đầu giao hàng.</p>
            <button type="button" class="sh-btn sh-btn-primary sh-btn-sm" onclick="document.querySelector('.sh-tab[data-tab=available]').click()">
                <i class="fas fa-arrow-left"></i> Về tab chờ nhận
            </button>
        </div>
    </c:if>
    <c:forEach var="o" items="${delivering}">
        <c:set var="card" value="${o}" scope="request"/>
        <c:set var="cardMode" value="delivering" scope="request"/>
        <jsp:include page="/WEB-INF/views/shipper/order-card.jsp"/>
    </c:forEach>
</div>

<script>
document.querySelectorAll('.sh-tab').forEach(function (btn) {
    btn.addEventListener('click', function () {
        var tab = btn.dataset.tab;
        document.querySelectorAll('.sh-tab').forEach(function (b) { b.classList.toggle('active', b === btn); });
        document.querySelectorAll('.sh-tab-pane').forEach(function (p) { p.classList.toggle('active', p.dataset.pane === tab); });
    });
});
</script>