<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Thống kê giao hàng</title>

<div class="sh-page-title">
    <i class="fas fa-chart-column"></i> Thống kê giao hàng
</div>

<c:set var="todayStats" value="${periods['Hôm nay']}"/>
<c:set var="weekStats"  value="${periods['Tuần này']}"/>
<c:set var="monthStats" value="${periods['Tháng này']}"/>

<%-- ==================== HERO: HÔM NAY ==================== --%>
<div class="sh-hero">
    <div class="sh-hero-head">
        <div>
            <div class="sh-hero-label">Hôm nay</div>
            <div class="sh-hero-value">${todayStats.delivered}</div>
            <div class="sh-hero-sub">đơn đã giao thành công</div>
        </div>
        <c:if test="${todayStats.failed > 0}">
            <div class="sh-hero-failed">
                <i class="fas fa-circle-xmark"></i>
                <span>${todayStats.failed} thất bại</span>
            </div>
        </c:if>
    </div>
    <div class="sh-hero-grid">
        <div class="sh-hero-cell">
            <i class="fas fa-money-bill-wave"></i>
            <div>
                <div class="sh-hero-cell-label">COD đã thu</div>
                <div class="sh-hero-cell-value">
                    <fmt:formatNumber value="${todayStats.codCollected}" pattern="#,##0"/>₫
                </div>
            </div>
        </div>
        <div class="sh-hero-cell">
            <i class="fas fa-route"></i>
            <div>
                <div class="sh-hero-cell-label">Giá trị đơn</div>
                <div class="sh-hero-cell-value">
                    <fmt:formatNumber value="${todayStats.revenue}" pattern="#,##0"/>₫
                </div>
            </div>
        </div>
    </div>
</div>

<%-- ==================== BIỂU ĐỒ 7 NGÀY ==================== --%>
<c:set var="total7" value="0"/>
<c:forEach var="v" items="${chartValues}"><c:set var="total7" value="${total7 + v}"/></c:forEach>
<c:set var="avg7" value="${total7 / 7.0}"/>

<div class="sh-card sh-card-pad mb-3">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <div class="sh-section-label mb-0">
            <i class="fas fa-chart-simple"></i> 7 ngày gần nhất
        </div>
        <div class="sh-small sh-muted">
            TB <b style="color:var(--sh-blue-600)">
                <fmt:formatNumber value="${avg7}" maxFractionDigits="1"/>
            </b> đơn/ngày
        </div>
    </div>
    <canvas id="deliveredChart" height="180"></canvas>
</div>

<%-- ==================== SO SÁNH TUẦN / THÁNG ==================== --%>
<div class="row g-3 mb-3">
    <div class="col-6">
        <div class="sh-card sh-card-pad h-100">
            <div class="sh-section-label">
                <i class="fas fa-calendar-week"></i> Tuần này
            </div>
            <div class="sh-compare-value">${weekStats.delivered}</div>
            <div class="sh-compare-label">đơn đã giao</div>
            <c:if test="${weekStats.failed > 0}">
                <div class="sh-compare-failed">
                    <i class="fas fa-circle-xmark"></i> ${weekStats.failed} thất bại
                </div>
            </c:if>
            <div class="sh-compare-divider"></div>
            <div class="sh-compare-row">
                <span><i class="fas fa-money-bill-wave"></i> COD</span>
                <b><fmt:formatNumber value="${weekStats.codCollected}" pattern="#,##0"/>₫</b>
            </div>
            <div class="sh-compare-row">
                <span><i class="fas fa-route"></i> Doanh thu</span>
                <b><fmt:formatNumber value="${weekStats.revenue}" pattern="#,##0"/>₫</b>
            </div>
        </div>
    </div>
    <div class="col-6">
        <div class="sh-card sh-card-pad h-100">
            <div class="sh-section-label">
                <i class="fas fa-calendar"></i> Tháng này
            </div>
            <div class="sh-compare-value">${monthStats.delivered}</div>
            <div class="sh-compare-label">đơn đã giao</div>
            <c:if test="${monthStats.failed > 0}">
                <div class="sh-compare-failed">
                    <i class="fas fa-circle-xmark"></i> ${monthStats.failed} thất bại
                </div>
            </c:if>
            <div class="sh-compare-divider"></div>
            <div class="sh-compare-row">
                <span><i class="fas fa-money-bill-wave"></i> COD</span>
                <b><fmt:formatNumber value="${monthStats.codCollected}" pattern="#,##0"/>₫</b>
            </div>
            <div class="sh-compare-row">
                <span><i class="fas fa-route"></i> Doanh thu</span>
                <b><fmt:formatNumber value="${monthStats.revenue}" pattern="#,##0"/>₫</b>
            </div>
        </div>
    </div>
</div>

<%-- ==================== TỶ LỆ THÀNH CÔNG ==================== --%>
<c:set var="monthTotal" value="${monthStats.delivered + monthStats.failed}"/>
<c:if test="${monthTotal > 0}">
    <c:set var="successRate" value="${(monthStats.delivered * 100.0) / monthTotal}"/>
    <c:set var="dashLen" value="${(successRate * 163.4) / 100.0}"/>

    <div class="sh-card sh-card-pad">
        <div class="sh-section-label">
            <i class="fas fa-bullseye"></i> Tỷ lệ giao thành công tháng này
        </div>
        <div class="d-flex align-items-center gap-3 mt-2">
            <div class="sh-progress-circle">
                <svg viewBox="0 0 60 60">
                    <defs>
                        <linearGradient id="gradSuccess" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" stop-color="#3b82f6"/>
                            <stop offset="100%" stop-color="#10b981"/>
                        </linearGradient>
                    </defs>
                    <circle cx="30" cy="30" r="26" fill="none" stroke="#e2e8f0" stroke-width="6"/>
                    <circle cx="30" cy="30" r="26" fill="none" stroke="url(#gradSuccess)" stroke-width="6"
                            stroke-linecap="round"
                            stroke-dasharray="${dashLen} 163.4"
                            transform="rotate(-90 30 30)"/>
                </svg>
                <div class="sh-progress-circle-value">
                    <fmt:formatNumber value="${successRate}" maxFractionDigits="0"/>%
                </div>
            </div>
            <div>
                <div class="fw-bold" style="font-size:1.15rem;color:var(--sh-green)">
                    ${monthStats.delivered}
                    <span class="sh-muted fw-normal" style="font-size:.85rem">thành công</span>
                </div>
                <div class="sh-small sh-muted mt-1">
                    ${monthStats.failed} đơn thất bại trong tổng ${monthTotal} đơn
                </div>
            </div>
        </div>
    </div>
</c:if>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
<script>
(function () {
    var labels = [<c:forEach var="l" items="${chartLabels}" varStatus="st">'${fn:escapeXml(l)}'<c:if test="${!st.last}">,</c:if></c:forEach>];
    var data   = [<c:forEach var="v" items="${chartValues}" varStatus="st">${v}<c:if test="${!st.last}">,</c:if></c:forEach>];

    // Highlight ngày có số đơn cao nhất
    var maxVal = Math.max.apply(null, data.concat([0]));
    var bgColors = data.map(function (v) {
        return v === maxVal && v > 0 ? 'rgba(37, 99, 235, 1)' : 'rgba(37, 99, 235, .55)';
    });

    new Chart(document.getElementById('deliveredChart'), {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: bgColors,
                hoverBackgroundColor: 'rgba(37, 99, 235, 1)',
                borderRadius: 8,
                borderSkipped: false,
                maxBarThickness: 36
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: '#0f172a',
                    padding: 10,
                    cornerRadius: 8,
                    titleFont: { weight: '700', size: 12 },
                    bodyFont: { size: 13 },
                    displayColors: false,
                    callbacks: {
                        label: function (item) {
                            var v = item.raw;
                            return v + ' đơn' + (v === maxVal && v > 0 ? ' 🏆' : '');
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { precision: 0, color: '#64748b', font: { size: 11 }, stepSize: 1 },
                    grid: { color: '#e2e8f0', drawBorder: false }
                },
                x: {
                    ticks: { color: '#64748b', font: { size: 11, weight: '600' } },
                    grid: { display: false }
                }
            }
        }
    });
})();
</script>