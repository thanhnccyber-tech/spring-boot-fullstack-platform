<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Đơn cần giao</title>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<div class="sh-dashboard-hero mb-3">
    <div class="sh-dashboard-hero-copy">
        <div class="sh-dashboard-kicker"><i class="fas fa-route"></i> TRUNG TÂM GIAO HÀNG UTETRA</div>
        <h1>Quản lý đơn giao hàng</h1>
        <p>Theo dõi đơn chờ nhận, đơn đang giao và kết quả giao hàng trong một màn hình.</p>
    </div>
    <a href="${ctx}/shipper/dashboard" class="sh-btn sh-btn-outline sh-btn-sm" title="Làm mới danh sách"><i class="fas fa-rotate"></i> Làm mới</a>
</div>

<div class="sh-dashboard-search mb-3">
    <i class="fas fa-magnifying-glass"></i>
    <input type="search" id="shipperOrderSearch" placeholder="Tìm theo mã đơn, tên khách, số điện thoại hoặc địa chỉ..." aria-label="Tìm đơn giao hàng">
    <span class="sh-search-hint">Lọc nhanh</span>
</div>

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

<div class="sh-empty sh-filter-empty" id="shipperNoResults" hidden>
    <i class="fas fa-magnifying-glass"></i><h5>Không tìm thấy đơn phù hợp</h5>
    <p>Thử nhập mã đơn, tên khách, số điện thoại hoặc địa chỉ khác.</p>
</div>
<script>
(function () {
    var tabs = document.querySelectorAll('.sh-tab');
    var panes = document.querySelectorAll('.sh-tab-pane');
    tabs.forEach(function (btn) {
        btn.addEventListener('click', function () {
            var tab = btn.dataset.tab;
            tabs.forEach(function (b) { b.classList.toggle('active', b === btn); });
            panes.forEach(function (p) { p.classList.toggle('active', p.dataset.pane === tab); });
            refreshEmptyState();
        });
    });

    var search = document.getElementById('shipperOrderSearch');
    var noResults = document.getElementById('shipperNoResults');
    var cards = Array.prototype.slice.call(document.querySelectorAll('.sh-tab-pane .sh-order'));
    function refreshEmptyState() {
        var activePane = document.querySelector('.sh-tab-pane.active');
        var visible = activePane ? activePane.querySelectorAll('.sh-order:not([hidden])').length : 0;
        var hasQuery = search && search.value.trim() !== '';
        var staticEmpty = activePane ? activePane.querySelector('.sh-empty:not(.sh-filter-empty)') : null;
        if (staticEmpty) staticEmpty.hidden = !!hasQuery;
        noResults.hidden = !(hasQuery && visible === 0);
    }
    if (search) search.addEventListener('input', function () {
        var q = search.value.trim().toLocaleLowerCase('vi');
        cards.forEach(function (card) {
            card.hidden = q !== '' && !card.textContent.toLocaleLowerCase('vi').includes(q);
        });
        refreshEmptyState();
    });
})();
</script>