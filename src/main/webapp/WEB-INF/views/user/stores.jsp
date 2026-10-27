<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Cửa hàng</title>

<style>
    .store-page-layout { display: grid; grid-template-columns: minmax(0, 1.6fr) minmax(300px, 1fr); gap: 20px; align-items: start; }
    .store-map-box { position: sticky; top: 90px; height: min(640px, 75vh); min-height: 360px; overflow: hidden; border-radius: 12px; background: #e9dfca; box-shadow: 0 6px 20px rgba(0,0,0,.1); }
    .store-map-box iframe { display: block; width: 100%; height: 100%; border: 0; }
    .store-list { display: flex; flex-direction: column; gap: 12px; max-height: 640px; overflow-y: auto; padding-right: 4px; }
    .store-card { cursor: pointer; transition: box-shadow .18s ease, border-color .18s ease; }
    .store-card.is-active { border-color: #9a5034 !important; box-shadow: 0 0 0 2px rgba(154,80,52,.25); }
    @media (max-width: 991px) {
        .store-page-layout { grid-template-columns: 1fr; }
        .store-map-box { position: static; height: 360px; }
        .store-list { max-height: none; overflow: visible; }
    }
</style>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="small mb-3"><a href="${ctx}/">Trang chủ</a> / Cửa hàng</nav>
<h3 class="mb-3">🏪 Danh sách cửa hàng</h3>

<c:if test="${not empty branches}">
<div class="store-page-layout">
    <div class="store-map-box"><iframe id="storeMapFrame" title="Bản đồ cửa hàng UTETra" loading="lazy" referrerpolicy="no-referrer-when-downgrade" allowfullscreen></iframe></div>
    <div class="store-list" id="storeList">
    <c:forEach items="${branches}" var="b">
        <div class="checkout-box store-card" data-name="${fn:escapeXml(b.name)}" data-address="${fn:escapeXml(b.address)}" data-lat="${fn:escapeXml(b.latitude)}" data-lng="${fn:escapeXml(b.longitude)}">
            <h5>${fn:escapeXml(b.name)}</h5>
            <p class="mb-1"><i class="fas fa-location-dot text-warning"></i> ${fn:escapeXml(b.address)}</p>
            <p class="mb-2"><i class="fas fa-phone text-success"></i> <a href="tel:${fn:escapeXml(b.phone)}">${fn:escapeXml(b.phone)}</a></p>
            <form action="${ctx}/receive/set" method="post" class="d-inline">
                <input type="hidden" name="method" value="PICKUP">
                <input type="hidden" name="branchId" value="${fn:escapeXml(b.id)}">
                <input type="hidden" name="returnUrl" value="/products">
                <button class="btn btn-sm btn-warning">Đến lấy tại đây</button>
            </form>
            <c:url var="mapUrl" value="https://www.google.com/maps/dir/"><c:param name="api" value="1"/><c:param name="destination" value="${b.address}"/></c:url>
            <a class="btn btn-sm btn-outline-secondary" target="_blank" rel="noopener" href="${fn:escapeXml(mapUrl)}">Chỉ đường</a>
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
        card.addEventListener('click', function (e) { if (!e.target.closest('a, button, form')) show(card); });
    });
    show(cards[0]);
})();
</script>
</c:if>
<c:if test="${empty branches}"><div class="alert alert-light border text-center">Chưa có cửa hàng nào hoạt động.</div></c:if>
