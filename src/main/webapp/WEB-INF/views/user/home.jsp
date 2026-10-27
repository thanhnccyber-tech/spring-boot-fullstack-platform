<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>UTETra - Trà sữa tươi mát mỗi ngày</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/tea-book.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/homepage-immersive.css">
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css">

<section class="ute-home-banner" aria-labelledby="ute-banner-heading">
    <div class="ute-banner-glow ute-banner-glow-one" aria-hidden="true"></div>
    <div class="ute-banner-glow ute-banner-glow-two" aria-hidden="true"></div>
    <div class="ute-banner-inner container">
        <div class="ute-banner-art" aria-label="Một vài món trà sữa của UTETra">
            <div class="ute-banner-stamp"><i class="fas fa-mug-hot" aria-hidden="true"></i><span>UTETra</span><small>TEA MOMENTS</small></div>
            <figure class="ute-banner-photo ute-banner-photo-one">
                <img src="${pageContext.request.contextPath}/static/images/tea-book/pearl-milk-tea.png" alt="Trà sữa trân châu">
                <figcaption>01 / TRÂN CHÂU</figcaption>
            </figure>
            <figure class="ute-banner-photo ute-banner-photo-two">
                <img src="${pageContext.request.contextPath}/static/images/tea-book/fruit-milk-tea.png" alt="Trà sữa trái cây">
                <figcaption>02 / TRÁI CÂY</figcaption>
            </figure>
            <figure class="ute-banner-photo ute-banner-photo-three">
                <img src="${pageContext.request.contextPath}/static/images/tea-book/matcha-milk-tea.png" alt="Trà sữa matcha">
                <figcaption>03 / MATCHA</figcaption>
            </figure>
            <span class="ute-banner-orbit ute-banner-orbit-one" aria-hidden="true"></span>
            <span class="ute-banner-orbit ute-banner-orbit-two" aria-hidden="true"></span>
            <p class="ute-banner-art-note">Pha tươi · Uống vui · Mỗi ngày</p>
        </div>
        <div class="ute-banner-copy">
            <p class="ute-banner-eyebrow"><span></span> TRÀ SỮA TƯƠI MÁT MỖI NGÀY</p>
            <h1 id="ute-banner-heading">THỬ VỊ MỚI.<br><em>TRÒN VỊ AN YÊN.</em></h1>
            <p class="ute-banner-lead">Một chút ngọt dịu, một chút thơm trà, một khoảnh khắc dành riêng cho bạn. Chọn món yêu thích và để UTETra mang niềm vui đến gần hơn.</p>
            <div class="ute-banner-actions">
                <a class="ute-banner-button ute-banner-button-primary" href="#thuc-don">Khám phá thực đơn <i class="fas fa-arrow-down" aria-hidden="true"></i></a>
                <a class="ute-banner-button ute-banner-button-light" href="#khuyen-mai">Ưu đãi hôm nay</a>
            </div>
            <div class="ute-banner-proof"><span><i class="fas fa-leaf" aria-hidden="true"></i> Nguyên liệu chọn lọc</span><span><i class="fas fa-heart" aria-hidden="true"></i> Pha chế mỗi ngày</span></div>
        </div>
    </div>
    <a class="ute-banner-scroll" href="#thuc-don"><span>CUỘN XUỐNG ĐỂ KHÁM PHÁ</span><i class="fas fa-arrow-down" aria-hidden="true"></i></a>
</section>

<section class="tea-book-hero" id="thuc-don" aria-labelledby="tea-book-heading">
    <div class="tea-book-wash" aria-hidden="true"></div>
    <div class="container tea-book-container">
        <p class="tea-book-kicker"><span></span> SỔ THỰC ĐƠN · UTETra · TRÀ SỮA TƯƠI MÁT MỖI NGÀY <span></span></p>

        <div class="tea-book-stage" id="teaBookStage">
            <button type="button" class="tea-book-edge tea-book-prev" id="teaBookPrev" aria-label="Lật về trang trước">
                <i class="fas fa-chevron-left" aria-hidden="true"></i>
            </button>

            <div class="tea-book-frame" id="teaBookFrame">
                <div class="tea-book-pages" id="teaBookPages" role="region" aria-label="Cuốn sổ thực đơn trà sữa có thể lật trang" tabindex="0">
                    <section class="tea-spread-page tea-spread-left" id="teaSpreadLeft" aria-label="Trang bên trái">
                        <div class="tea-cover-collage">
                            <div class="tea-cover-photo tea-cover-photo-one">
                                <span class="tea-photo-tape"></span>
                                <img src="${pageContext.request.contextPath}/static/images/tea-book/pearl-milk-tea.png" alt="Trà sữa trân châu UTETra">
                            </div>
                            <div class="tea-cover-photo tea-cover-photo-two">
                                <span class="tea-photo-tape"></span>
                                <img src="${pageContext.request.contextPath}/static/images/tea-book/fruit-milk-tea.png" alt="Trà sữa trái cây UTETra">
                            </div>
                            <div class="tea-cover-photo tea-cover-photo-three">
                                <span class="tea-photo-tape"></span>
                                <img src="${pageContext.request.contextPath}/static/images/tea-book/matcha-milk-tea.png" alt="Trà sữa matcha UTETra">
                            </div>
                            <p class="tea-cover-side-note">Pha tươi · Uống vui · Mỗi ngày</p>
                            <div class="tea-page-number">— 1 —</div>
                        </div>
                    </section>

                    <section class="tea-spread-page tea-spread-right" id="teaSpreadRight" aria-label="Trang giới thiệu thực đơn">
                        <div class="tea-cover-promo">
                            <div class="tea-cup-doodle" aria-hidden="true">
                                <svg viewBox="0 0 64 82" fill="none">
                                    <path d="M39 5 49 1M39 5 32 17M15 23h35l-4 46a5 5 0 0 1-5 4H25a5 5 0 0 1-5-4l-5-46Z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/>
                                    <path d="M13 23c2-6 7-9 18-9h10c7 0 11 3 13 9" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
                                    <circle cx="26" cy="49" r="3" stroke="currentColor" stroke-width="1.5"/><circle cx="36" cy="55" r="3" stroke="currentColor" stroke-width="1.5"/><circle cx="29" cy="62" r="3" stroke="currentColor" stroke-width="1.5"/><circle cx="40" cy="44" r="3" stroke="currentColor" stroke-width="1.5"/>
                                </svg>
                            </div>
                            <p class="tea-promo-eyebrow">UTETra · SỔ THỰC ĐƠN</p>
                            <h1 id="tea-book-heading">Trà sữa <em>tươi mát</em><br>cho mọi khoảnh khắc</h1>
                            <p class="tea-promo-copy">Nguyên liệu chọn lọc, pha chế thủ công mỗi ngày. Đặt online — chọn món yêu thích, tùy chỉnh size, đường, đá và topping ngay tại UTETra.</p>
                            <div class="tea-promo-actions">
                                <a class="tea-primary-button" href="${pageContext.request.contextPath}/products">Xem thực đơn <i class="fas fa-arrow-right" aria-hidden="true"></i></a>
                                <a class="tea-secondary-button" href="${pageContext.request.contextPath}/khuyen-mai">Ưu đãi hôm nay</a>
                            </div>
                            <p class="tea-promo-hint">Lật trang để chọn món — giá được lấy từ thực đơn <span aria-hidden="true">→</span></p>
                            <div class="tea-page-number">— 2 —</div>
                        </div>
                    </section>
                    <div class="tea-book-spine" aria-hidden="true"></div>
                    <div class="tea-book-curl-host" id="teaBookCurlHost" aria-hidden="true"></div>
                </div>
            </div>

            <button type="button" class="tea-book-edge tea-book-next" id="teaBookNext" aria-label="Lật sang trang tiếp theo">
                <i class="fas fa-chevron-right" aria-hidden="true"></i>
            </button>

            <div class="tea-magnifier" id="teaMagnifier" role="button" tabindex="0" aria-label="Kéo kính lúp qua cuốn sách để phóng to">
                <div class="tea-magnifier-glass"><div class="tea-magnifier-copy" id="teaMagnifierCopy"></div></div>
                <span class="tea-magnifier-handle" aria-hidden="true"></span>
            </div>
        </div>

        <div class="tea-book-toolbar" role="group" aria-label="Điều khiển sổ thực đơn">
            <button type="button" class="tea-tool-button" id="teaZoomOut" aria-label="Thu nhỏ"><i class="fas fa-search-minus" aria-hidden="true"></i></button>
            <span class="tea-zoom-readout" id="teaZoomReadout">100%</span>
            <button type="button" class="tea-tool-button" id="teaZoomIn" aria-label="Phóng to"><i class="fas fa-search-plus" aria-hidden="true"></i></button>
            <span class="tea-tool-separator" aria-hidden="true"></span>
            <button type="button" class="tea-tool-button tea-loupe-toggle" id="teaLoupeToggle" aria-label="Bật/tắt kính lúp" aria-pressed="true"><i class="fas fa-search" aria-hidden="true"></i></button>
        </div>
        <p class="tea-book-instruction"><span aria-hidden="true">↔</span> KÉO TRANG ĐỂ LẬT · KÉO KÍNH LÚP LÊN TRANG ĐỂ XEM RÕ</p>
    </div>

    <div class="tea-book-data" id="teaBookData" hidden>
        <c:forEach items="${bookProducts}" var="bookProduct">
            <c:set var="bookNameLower" value="${fn:toLowerCase(bookProduct.name)}"/>
            <div class="tea-book-record"
                 data-title="${fn:escapeXml(bookProduct.name)}"
                 data-description="${fn:escapeXml(bookProduct.description)}"
                 data-price="${fn:escapeXml(bookProduct.basePrice)}"
                 data-url="${pageContext.request.contextPath}/product/${fn:escapeXml(bookProduct.id)}"
                 data-out-of-stock="${bookProduct.outOfStock}">
                <c:choose>
                    <c:when test="${fn:contains(bookNameLower, 'matcha') or fn:contains(bookNameLower, 'trà xanh')}">
                        <span data-image="${pageContext.request.contextPath}/static/images/tea-book/matcha-milk-tea.png"></span>
                    </c:when>
                    <c:when test="${fn:contains(bookNameLower, 'trân châu') or fn:contains(bookNameLower, 'boba')}">
                        <span data-image="${pageContext.request.contextPath}/static/images/tea-book/pearl-milk-tea.png"></span>
                    </c:when>
                    <c:otherwise>
                        <span data-image="${pageContext.request.contextPath}/static/images/tea-book/fruit-milk-tea.png"></span>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:forEach>
    </div>
</section>
<script src="${pageContext.request.contextPath}/static/js/tea-book.js" defer></script>

<!-- Khuyến mãi: luôn nằm ngay sau cuốn sổ thực đơn -->
<section class="ute-home-promotions" id="khuyen-mai" aria-labelledby="ute-promotions-heading">
    <div class="container ute-home-section-inner">
        <div class="ute-home-section-heading">
            <div>
                <p class="ute-home-eyebrow"><span></span> MÓN NGON · ƯU ĐÃI HAY</p>
                <h2 id="ute-promotions-heading">Thêm niềm vui, <em>trọn vị ngon</em></h2>
                <p>Khám phá những chương trình đang diễn ra tại UTETra.</p>
            </div>
            <a class="ute-section-link" href="${pageContext.request.contextPath}/khuyen-mai">Tất cả ưu đãi <i class="fas fa-arrow-right" aria-hidden="true"></i></a>
        </div>
        <c:choose>
            <c:when test="${not empty promotions}">
                <div class="ute-promo-grid">
                    <c:forEach items="${promotions}" var="pr" varStatus="promoStatus">
                        <a href="${pageContext.request.contextPath}/khuyen-mai/${fn:escapeXml(pr.id)}" class="ute-promo-card">
                            <div class="ute-promo-art">
                                <c:choose>
                                    <c:when test="${not empty pr.imageUrl}"><img src="${fn:escapeXml(pr.imageUrl)}" loading="lazy" alt="${fn:escapeXml(pr.title)}"></c:when>
                                    <c:otherwise><div class="ute-promo-art-placeholder"><span>UTETra</span><i class="fas fa-gift" aria-hidden="true"></i><small>ƯU ĐÃI ĐẶC BIỆT</small></div></c:otherwise>
                                </c:choose>
                                <span class="ute-promo-index">ƯU ĐÃI 0${promoStatus.count}</span>
                            </div>
                            <div class="ute-promo-body">
                                <p class="ute-promo-meta"><i class="far fa-eye" aria-hidden="true"></i> ${fn:escapeXml(pr.views)} lượt xem <span>·</span> ${fn:escapeXml(pr.createdAtFormatted)}</p>
                                <h3>${fn:escapeXml(pr.title)}</h3>
                                <c:if test="${not empty pr.summary}"><p>${fn:escapeXml(pr.summary)}</p></c:if>
                                <span class="ute-promo-read">Xem chi tiết <i class="fas fa-arrow-right" aria-hidden="true"></i></span>
                            </div>
                        </a>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="ute-promo-empty"><span class="ute-promo-empty-icon"><i class="fas fa-ticket-alt" aria-hidden="true"></i></span><div><h3>Ưu đãi mới đang được chuẩn bị</h3><p>Trong lúc chờ chương trình tiếp theo, bạn có thể khám phá menu và chọn món yêu thích.</p></div><a href="${pageContext.request.contextPath}/products">Xem thực đơn <i class="fas fa-arrow-right" aria-hidden="true"></i></a></div>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<!-- Bản đồ và danh sách cửa hàng -->
<section class="ute-home-stores" id="cua-hang" aria-labelledby="ute-stores-heading">
    <div class="container ute-home-section-inner">
        <div class="ute-home-section-heading ute-store-heading">
            <div>
                <p class="ute-home-eyebrow"><span></span> GHÉ THĂM CHÚNG MÌNH</p>
                <h2 id="ute-stores-heading">Tìm UTETra <em>gần bạn</em></h2>
                <p>Chọn cửa hàng thuận tiện nhất để ghé lấy món hoặc xem đường đi.</p>
            </div>
            <a class="ute-section-link" href="${pageContext.request.contextPath}/cua-hang">Xem tất cả cửa hàng <i class="fas fa-arrow-right" aria-hidden="true"></i></a>
        </div>
        <div class="ute-store-layout">
            <div class="ute-store-map-panel">
                <div class="ute-store-map-heading"><span><i class="fas fa-map-marked-alt" aria-hidden="true"></i> BẢN ĐỒ CỬA HÀNG</span><small><i class="fas fa-circle" aria-hidden="true"></i> Đang hoạt động</small></div>
                <div id="uteStoreMap" class="ute-store-map" aria-label="Bản đồ các cửa hàng UTETra"></div>
                <p class="ute-store-map-note" id="uteMapNotice" hidden><i class="fas fa-info-circle" aria-hidden="true"></i> Chưa có tọa độ cho các cửa hàng. Bạn vẫn có thể mở chỉ đường từ danh sách bên cạnh.</p>
                <div class="ute-map-footer"><span><i class="fas fa-location-dot" aria-hidden="true"></i> Cửa hàng đang hoạt động</span><span>Bản đồ © OpenStreetMap</span></div>
            </div>
            <div class="ute-store-directory">
                <label class="ute-store-search"><i class="fas fa-search" aria-hidden="true"></i><input id="uteStoreSearch" type="search" placeholder="Tìm tên cửa hàng, địa chỉ..." autocomplete="off"><kbd>/</kbd></label>
                <div class="ute-store-count"><span>Danh sách cửa hàng</span><b id="uteStoreCount">${fn:length(branches)} địa điểm</b></div>
                <div id="uteStoreList" class="ute-store-list">
                    <c:forEach items="${branches}" var="b" varStatus="branchStatus">
                        <article class="ute-store-item" data-branch-id="${fn:escapeXml(b.id)}" data-name="${fn:escapeXml(b.name)}" data-address="${fn:escapeXml(b.address)}" data-phone="${fn:escapeXml(b.phone)}" data-lat="${fn:escapeXml(b.latitude)}" data-lng="${fn:escapeXml(b.longitude)}">
                            <div class="ute-store-number"><i class="fas fa-store" aria-hidden="true"></i></div>
                            <div class="ute-store-info">
                                <h3>${fn:escapeXml(b.name)}</h3>
                                <p><i class="fas fa-location-dot" aria-hidden="true"></i> ${fn:escapeXml(b.address)}</p>
                                <p><i class="fas fa-phone" aria-hidden="true"></i> <a href="tel:${fn:escapeXml(b.phone)}">${fn:escapeXml(b.phone)}</a></p>
                                <div class="ute-store-actions">
                                    <c:url var="storeDirectionsUrl" value="https://www.google.com/maps/dir/"><c:param name="api" value="1"/><c:param name="destination" value="${b.address}"/></c:url>
                                    <a href="${fn:escapeXml(storeDirectionsUrl)}" target="_blank" rel="noopener noreferrer" class="ute-store-directions"><i class="fas fa-diamond-turn-right" aria-hidden="true"></i> Chỉ đường</a>
                                    <form action="${pageContext.request.contextPath}/receive/set" method="post">
                                        <input type="hidden" name="method" value="PICKUP">
                                        <input type="hidden" name="branchId" value="${fn:escapeXml(b.id)}">
                                        <input type="hidden" name="returnUrl" value="/">
                                        <button type="submit" class="ute-store-pickup">Đến lấy tại đây</button>
                                    </form>
                                </div>
                            </div>
                        </article>
                    </c:forEach>
                    <c:if test="${empty branches}"><div class="ute-store-empty"><i class="fas fa-store-slash" aria-hidden="true"></i><h3>Chưa có cửa hàng hoạt động</h3><p>Danh sách cửa hàng sẽ được cập nhật tại đây.</p></div></c:if>
                    <div class="ute-store-no-results" id="uteStoreNoResults" hidden>Không tìm thấy cửa hàng phù hợp.</div>
                </div>
            </div>
        </div>
    </div>
</section>
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" defer></script>
<script src="${pageContext.request.contextPath}/static/js/homepage-map.js" defer></script>
