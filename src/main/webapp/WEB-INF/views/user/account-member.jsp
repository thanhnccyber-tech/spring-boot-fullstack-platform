<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Khách hàng thành viên</title>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="small mb-3"><a href="${ctx}/">Trang chủ</a> / <a href="${ctx}/profile">Tài khoản</a> / Khách hàng thành viên</nav>
<div class="row g-4">
    <div class="col-lg-4 col-xl-3">
        <jsp:include page="/WEB-INF/views/user/_account-menu.jsp"><jsp:param name="active" value="member"/></jsp:include>
    </div>
    <div class="col-lg-8 col-xl-9">

        <div class="member-banner">
            <div class="member-panel">
                <h5 class="mb-3">Hạng thành viên: <b class="text-capitalize">${fn:escapeXml(fn:toLowerCase(rank))}</b></h5>
                <div class="rank-bar">
                    <div class="rank-fill" style="width:${fn:escapeXml(progress)}%"></div>
                    <span class="rank-dot" style="left:0"></span>
                    <span class="rank-dot" style="left:50%"></span>
                    <span class="rank-dot" style="left:100%"></span>
                </div>
                <div class="d-flex justify-content-between small fw-bold mb-3">
                    <span class="${fn:escapeXml(rank == 'MEMBER' ? 'text-success' : 'text-muted')}">MEMBER</span>
                    <span class="${fn:escapeXml(rank == 'VIP' ? 'text-success' : 'text-muted')}">VIP (${fn:escapeXml(vipPoints)} điểm)</span>
                    <span class="${fn:escapeXml(rank == 'DIAMOND' ? 'text-success' : 'text-muted')}">DIAMOND (${fn:escapeXml(diamondPoints)} điểm)</span>
                </div>
                <c:choose>
                    <c:when test="${not empty nextRank}">Tích thêm <b class="text-success">${fn:escapeXml(remaining)}</b> điểm nữa để lên hạng <b class="text-success">${fn:escapeXml(nextRank)}</b></c:when>
                    <c:otherwise>Bạn đang ở hạng cao nhất. Cảm ơn bạn đã đồng hành cùng UTeTra!</c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="row g-3 mt-1">
            <div class="col-md-6">
                <div class="checkout-box h-100">
                    <h5>Điểm tích luỹ</h5>
                    <div class="display-6 fw-bold text-success mb-2">${fn:escapeXml(me.points)} điểm</div>
                    <div class="small text-muted mb-2">Mỗi <fmt:formatNumber value="${pointUnit}" pattern="#,##0"/>đ tiền hàng = 1 điểm. Điểm được cộng khi đơn hoàn thành.</div>
                    <div class="barcode-box border"><svg id="barcode"></svg></div>
                    <div class="small text-muted">Mã hội viên: <b>${fn:escapeXml(memberCode)}</b></div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="checkout-box h-100">
                    <h5>Ưu đãi thành viên</h5>
                    <ul class="mb-0 small">
                        <li>Member: tích điểm cho mọi đơn hoàn thành.</li>
                        <li>VIP (từ ${fn:escapeXml(vipPoints)} điểm): nhận mã ưu đãi riêng theo từng đợt khuyến mãi.</li>
                        <li>Diamond (từ ${fn:escapeXml(diamondPoints)} điểm): ưu tiên các chương trình dành riêng cho hội viên.</li>
                    </ul>
                </div>
            </div>
        </div>

        <div class="checkout-box mt-3">
            <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-2">
                <h5 class="mb-0">Lịch sử giao dịch (cộng điểm)</h5>
                <form method="get" class="d-flex gap-2">
                    <input type="month" name="month" class="form-control form-control-sm" value="${fn:escapeXml(month)}">
                    <button class="btn btn-sm btn-outline-dark">Lọc</button>
                    <c:if test="${not empty month}"><a class="btn btn-sm btn-link" href="${ctx}/account/member">Bỏ lọc</a></c:if>
                </form>
            </div>
            <table class="table align-middle mb-0">
                <thead><tr><th>Thời gian</th><th>Đơn hàng</th><th class="text-end">Giá trị</th><th class="text-end">Điểm</th></tr></thead>
                <tbody>
                <c:forEach items="${history}" var="o">
                    <tr>
                        <td>${fn:escapeXml(o.createdAtFormatted)}</td>
                        <td><a href="${ctx}/order/detail/${fn:escapeXml(o.id)}">${fn:escapeXml(o.orderCode)}</a></td>
                        <td class="text-end"><fmt:formatNumber value="${o.totalAmount}" pattern="#,##0"/> ₫</td>
                        <td class="text-end text-success fw-bold">+${fn:escapeXml(o.pointsEarned)}</td>
                    </tr>
                </c:forEach>
                <c:if test="${empty history}"><tr><td colspan="4" class="text-center text-muted py-4">Không có dữ liệu</td></tr></c:if>
                </tbody>
            </table>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/jsbarcode@3.11.6/dist/JsBarcode.all.min.js"></script>
        <script>
            if (window.JsBarcode) { JsBarcode('#barcode', '${fn:escapeXml(memberCode)}', { format: 'CODE128', height: 50, displayValue: false, margin: 4 }); }
        </script>
    </div>
</div>
