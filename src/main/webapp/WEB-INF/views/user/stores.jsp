<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Cửa hàng</title>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="small mb-3"><a href="${ctx}/">Trang chủ</a> / Cửa hàng</nav>
<h3 class="mb-4" style="color:var(--ute-navy-900); font-weight:800;"> Danh sách cửa hàng</h3>

<c:if test="${not empty branches}">
<div class="store-page-layout">
    <div class="store-map-box">
        <iframe id="storeMapFrame" title="Bản đồ cửa hàng UTETra" loading="lazy" referrerpolicy="no-referrer-when-downgrade" allowfullscreen></iframe>
    </div>
    
    <div class="store-list" id="storeList">
    <c:forEach items="${branches}" var="b">
        <div class="store-card" data-name="${fn:escapeXml(b.name)}" data-address="${fn:escapeXml(b.address)}" data-lat="${fn:escapeXml(b.latitude)}" data-lng="${fn:escapeXml(b.longitude)}">
            <h5 class="mb-2" style="font-weight:700;color:var(--ute-navy-900)">${fn:escapeXml(b.name)}</h5>
            <p class="mb-1 text-muted"><i class="fas fa-location-dot text-warning"></i> ${fn:escapeXml(b.address)}</p>
            <p class="mb-3 text-muted"><i class="fas fa-phone text-success"></i> <a href="tel:${fn:escapeXml(b.phone)}" class="text-decoration-none text-muted">${fn:escapeXml(b.phone)}</a></p>
            
            <div class="d-flex gap-2">
                <form action="${ctx}/receive/set" method="post" class="d-inline">
                    <input type="hidden" name="method" value="PICKUP">
                    <input type="hidden" name="branchId" value="${fn:escapeXml(b.id)}">
                    <input type="hidden" name="returnUrl" value="/products">
                    <button class="btn btn-warning btn-sm fw-bold px-3">Đến lấy tại đây</button>
                </form>
                <c:url var="mapUrl" value="https://www.google.com/maps/dir/"><c:param name="api" value="1"/><c:param name="destination" value="${b.address}"/></c:url>
                <a class="btn btn-outline-dark btn-sm px-3" target="_blank" rel="noopener" href="${fn:escapeXml(mapUrl)}">Chỉ đường</a>
            </div>
        </div>
    </c:forEach>
    </div>
</div>

<script>
(function () {
    var frame = document.getElementById('storeMapFrame');
    var cards = Array.prototype.slice.call(document.querySelectorAll('#storeList .store-card'));
    if (!frame || !cards.length) return;
    
    function show(card) {
        var lat = parseFloat(card.dataset.lat), lng = parseFloat(card.dataset.lng);
        var q = (isFinite(lat) && isFinite(lng)) ? lat + ',' + lng : (card.dataset.address || card.dataset.name);
        cards.forEach(function (c) { c.classList.toggle('is-active', c === card); });
        frame.src = 'https://www.google.com/maps?q=' + encodeURIComponent(q) + '&hl=vi&z=16&output=embed';
    }
    
    cards.forEach(function (card) {
        card.addEventListener('click', function (e) { 
            if (!e.target.closest('a, button, form')) show(card); 
        });
    });
    
    // Hiển thị bản đồ của cửa hàng đầu tiên khi mới tải trang
    show(cards[0]);
})();
</script>
</c:if>

<c:if test="${empty branches}">
    <div class="alert alert-light border text-center py-5">
        <i class="fas fa-store-slash fa-3x text-muted mb-3 opacity-50"></i>
        <h5>Chưa có cửa hàng nào hoạt động.</h5>
    </div>
</c:if>