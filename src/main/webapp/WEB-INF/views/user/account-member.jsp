<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
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

        <section class="member-banner" aria-labelledby="memberRankTitle">
            <div class="member-panel">
                <div class="member-progress-heading">
                    <div>
                        <div class="member-eyebrow"><i class="fas fa-medal"></i> HỘI VIÊN UTETRA</div>
                        <h5 id="memberRankTitle" class="mb-1" style="color: #fff;">Hạng thành viên: <b class="member-rank-label rank-${fn:escapeXml(fn:toLowerCase(rank))}">${fn:escapeXml(rank)}</b></h5>
                        <div class="member-progress-subtitle">Tích lũy qua các đơn hàng hoàn thành để nâng hạng</div>
                    </div>
                    <div class="member-total-points">
                        <strong>${fn:escapeXml(tierPoints)}</strong>
                        <span>điểm xét hạng</span>
                    </div>
                </div>
                <div class="rank-bar" role="progressbar" aria-label="Tiến độ nâng hạng" aria-valuemin="0" aria-valuemax="100" aria-valuenow="${fn:escapeXml(progress)}">
                    <div class="rank-fill" style="width:${fn:escapeXml(progress)}%"></div>
                    <span class="rank-dot ${tierPoints >= 0 ? 'reached' : ''}" style="left:0"><i class="fas fa-check"></i></span>
                    <span class="rank-dot ${tierPoints >= vipPoints ? 'reached' : ''}" style="left:50%"><i class="fas fa-star"></i></span>
                    <span class="rank-dot ${tierPoints >= diamondPoints ? 'reached' : ''}" style="left:100%"><i class="fas fa-crown"></i></span>
                </div>
                <div class="rank-milestones">
                    <div class="rank-milestone ${rank == 'MEMBER' ? 'current' : (tierPoints >= 0 ? 'reached' : '')}">
                        <b>MEMBER</b><span>0 điểm</span>
                    </div>
                    <div class="rank-milestone text-center ${rank == 'VIP' ? 'current' : (tierPoints >= vipPoints ? 'reached' : '')}">
                        <b>VIP</b><span>${fn:escapeXml(vipPoints)} điểm</span>
                    </div>
                    <div class="rank-milestone text-end ${rank == 'DIAMOND' ? 'current' : (tierPoints >= diamondPoints ? 'reached' : '')}">
                        <b>DIAMOND</b><span>${fn:escapeXml(diamondPoints)} điểm</span>
                    </div>
                </div>
                <div class="member-next-rank">
                    <i class="fas fa-bullseye"></i>
                    <c:choose>
                        <c:when test="${not empty nextRank}">Tích thêm <b>${fn:escapeXml(remaining)} điểm</b> để đạt hạng <b>${fn:escapeXml(nextRank)}</b>.</c:when>
                        <c:otherwise>Chúc mừng! Bạn đã đạt hạng cao nhất của UTeTra.</c:otherwise>
                    </c:choose>
                    <span>Điểm đã dùng đổi ưu đãi không làm giảm tiến độ xét hạng.</span>
                </div>
            </div>
        </section>

        <div class="row g-3 mt-1">
            <div class="col-md-6">
                <div class="checkout-box h-100">
                    <h5>Điểm khả dụng</h5>
                    <div class="display-6 fw-bold text-success mb-2">${fn:escapeXml(me.points)} điểm</div>
                    <div class="small text-muted mb-2">Đây là số điểm hiện có để đổi giảm giá. Mỗi <fmt:formatNumber value="${pointUnit}" pattern="#,##0"/>đ tiền hàng = 1 điểm; điểm được cộng khi đơn hoàn thành.</div>
                    <div class="small text-success mb-2">Tổng điểm đã tích lũy xét hạng: <b>${fn:escapeXml(tierPoints)} điểm</b>. Dùng điểm không làm giảm tổng này.</div>
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
