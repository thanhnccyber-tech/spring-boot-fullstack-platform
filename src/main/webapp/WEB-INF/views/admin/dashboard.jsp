<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<title>Dashboard</title>

<style>
    @media print {
        .no-print, .sidebar, nav { display: none !important; }
        .card { break-inside: avoid; }
    }
    .stat-sub { font-size: .8rem; opacity: .85; }
</style>

<!-- ============ BỘ LỌC ============ -->
<form method="get" action="${ctx}/admin/dashboard" class="card mb-3 no-print" id="filterForm">
    <div class="card-body py-2">
        <div class="row g-2 align-items-end">
            <div class="col-auto">
                <label class="form-label small mb-0">Khoảng thời gian</label>
                <div class="btn-group d-block" role="group">
                    <c:forEach items="${['today:Hôm nay','7d:7 ngày','30d:30 ngày','month:Tháng này','custom:Tùy chọn']}" var="opt">
                        <c:set var="k" value="${fn:substringBefore(opt, ':')}"/>
                        <c:set var="lbl" value="${fn:substringAfter(opt, ':')}"/>
                        <input type="radio" class="btn-check" name="range" id="r_${k}" value="${k}" ${range == k ? 'checked' : ''} onchange="onRange(this.value)">
                        <label class="btn btn-sm btn-outline-dark" for="r_${k}">${lbl}</label>
                    </c:forEach>
                </div>
            </div>
            <div class="col-auto" id="customDates" style="${range == 'custom' ? '' : 'display:none'}">
                <label class="form-label small mb-0">Từ - đến</label>
                <div class="input-group input-group-sm">
                    <input type="date" name="from" class="form-control" value="${fn:escapeXml(fromDate)}">
                    <input type="date" name="to" class="form-control" value="${fn:escapeXml(toDate)}">
                    <button class="btn btn-dark" type="submit">Xem</button>
                </div>
            </div>
            <div class="col-auto">
                <label class="form-label small mb-0" for="branchSel">Chi nhánh</label>
                <select name="branchId" id="branchSel" class="form-select form-select-sm" onchange="this.form.submit()">
                    <option value="">Tất cả chi nhánh</option>
                    <c:forEach items="${branches}" var="b">
                        <option value="${fn:escapeXml(b.id)}" ${branchId == b.id ? 'selected' : ''}>${fn:escapeXml(b.name)}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col text-end">
                <a class="btn btn-sm btn-outline-success"
                   href="${ctx}/admin/dashboard/export?range=${fn:escapeXml(range)}&from=${fn:escapeXml(fromDate)}&to=${fn:escapeXml(toDate)}<c:if test='${not empty branchId}'>&branchId=${fn:escapeXml(branchId)}</c:if>">
                    <i class="fas fa-file-excel"></i> Xuất Excel (CSV)</a>
                <button type="button" class="btn btn-sm btn-outline-secondary" onclick="window.print()"><i class="fas fa-print"></i> In / PDF</button>
            </div>
        </div>
        <div class="small text-muted mt-1">Đang xem: <b>${fn:escapeXml(fromDate)}</b> → <b>${fn:escapeXml(toDate)}</b></div>
    </div>
</form>

<!-- ============ CẢNH BÁO ============ -->
<c:forEach items="${alerts}" var="a">
    <div class="alert alert-${fn:escapeXml(a.level)} py-2 mb-2 d-flex justify-content-between align-items-center no-print">
        <span><i class="fas fa-triangle-exclamation"></i> ${fn:escapeXml(a.text)}</span>
        <a class="btn btn-sm btn-outline-dark" href="${ctx}${fn:escapeXml(a.link)}">Xem</a>
    </div>
</c:forEach>

<!-- ============ HÔM NAY ============ -->
<div class="row g-3">
    <div class="col-md-3">
        <div class="card text-bg-primary h-100"><div class="card-body">
            <h6><i class="fas fa-receipt"></i> Đơn hôm nay</h6>
            <h2>${fn:escapeXml(todayOrderCount)}</h2>
            <div class="stat-sub">Đang chờ xác nhận (toàn bộ): <b>${fn:escapeXml(pendingNow)}</b></div>
        </div></div>
    </div>
    <div class="col-md-3">
        <div class="card text-bg-success h-100"><div class="card-body">
            <h6><i class="fas fa-money-bill-wave"></i> Đã thu hôm nay</h6>
            <h2><fmt:formatNumber value="${todayCollected}" pattern="#,##0"/> ₫</h2>
            <div class="stat-sub">Dự kiến: <fmt:formatNumber value="${todayExpected}" pattern="#,##0"/> ₫</div>
        </div></div>
    </div>
    <div class="col-md-3">
        <div class="card h-100"><div class="card-body">
            <h6 class="text-muted"><i class="fas fa-calendar"></i> Tháng này (đã thu)</h6>
            <h3><fmt:formatNumber value="${thisMonth}" pattern="#,##0"/> ₫</h3>
            <div class="small text-muted">Tháng trước: <fmt:formatNumber value="${lastMonth}" pattern="#,##0"/> ₫
                <c:choose>
                    <c:when test="${not empty monthGrowth}">
                        <span class="badge ${monthGrowth >= 0 ? 'bg-success' : 'bg-danger'}"><fmt:formatNumber value="${monthGrowth}" pattern="+0.0;-0.0"/>%</span>
                    </c:when>
                    <c:otherwise><span class="badge bg-secondary">chưa có dữ liệu so sánh</span></c:otherwise>
                </c:choose>
            </div>
        </div></div>
    </div>
    <div class="col-md-3">
        <div class="card h-100"><div class="card-body">
            <h6 class="text-muted"><i class="fas fa-users"></i> Khách mới (kỳ đang xem)</h6>
            <h3>${fn:escapeXml(newCustomers)}</h3>
            <div class="small text-muted">Tài khoản đã kích hoạt</div>
        </div></div>
    </div>
</div>

<!-- ============ KỲ ĐANG XEM ============ -->
<div class="row g-3 mt-1">
    <div class="col-6 col-md-3">
        <div class="card h-100"><div class="card-body">
            <div class="small text-muted">Doanh thu đã thu</div>
            <div class="fs-4 fw-bold text-success"><fmt:formatNumber value="${collected}" pattern="#,##0"/> ₫</div>
            <c:if test="${not empty periodGrowth}">
                <span class="badge ${periodGrowth >= 0 ? 'bg-success' : 'bg-danger'}"><fmt:formatNumber value="${periodGrowth}" pattern="+0.0;-0.0"/>% so với kỳ trước</span>
            </c:if>
        </div></div>
    </div>
    <div class="col-6 col-md-3">
        <div class="card h-100"><div class="card-body">
            <div class="small text-muted">Doanh thu dự kiến</div>
            <div class="fs-4 fw-bold"><fmt:formatNumber value="${expected}" pattern="#,##0"/> ₫</div>
            <div class="small text-muted">Chưa thu: <fmt:formatNumber value="${uncollected}" pattern="#,##0"/> ₫</div>
        </div></div>
    </div>
    <div class="col-6 col-md-3">
        <div class="card h-100"><div class="card-body">
            <div class="small text-muted">Giá trị đơn trung bình</div>
            <div class="fs-4 fw-bold"><fmt:formatNumber value="${avgOrder}" pattern="#,##0"/> ₫</div>
            <div class="small text-muted">${fn:escapeXml(liveCount)} đơn hợp lệ / ${fn:escapeXml(totalOrders)} đơn</div>
        </div></div>
    </div>
    <div class="col-6 col-md-3">
        <div class="card h-100"><div class="card-body">
            <div class="small text-muted">Tỉ lệ hủy</div>
            <div class="fs-4 fw-bold ${cancelRate >= 15 ? 'text-danger' : ''}">${fn:escapeXml(cancelRate)}%</div>
            <div class="small text-muted">${fn:escapeXml(statusCount['CANCELLED'])} đơn bị hủy</div>
        </div></div>
    </div>
</div>

<!-- ============ BIỂU ĐỒ ============ -->
<div class="row g-3 mt-1">
    <div class="col-lg-8">
        <div class="card h-100">
            <div class="card-header"><i class="fas fa-chart-bar"></i> Doanh thu theo ngày <span class="text-muted small float-end">Đơn vị: VNĐ</span></div>
            <div class="card-body"><canvas id="dayChart" style="max-height:320px;"></canvas></div>
        </div>
    </div>
    <div class="col-lg-4">
        <div class="card h-100">
            <div class="card-header"><i class="fas fa-chart-pie"></i> Đơn theo trạng thái</div>
            <div class="card-body"><canvas id="statusChart" style="max-height:320px;"></canvas></div>
        </div>
    </div>
</div>

<div class="row g-3 mt-1">
    <div class="col-lg-8">
        <div class="card h-100">
            <div class="card-header"><i class="fas fa-clock"></i> Đơn hàng theo giờ trong ngày <span class="text-muted small float-end">biết khung giờ đông khách</span></div>
            <div class="card-body"><canvas id="hourChart" style="max-height:280px;"></canvas></div>
        </div>
    </div>
    <div class="col-lg-4">
        <div class="card h-100">
            <div class="card-header"><i class="fas fa-credit-card"></i> Theo phương thức thanh toán</div>
            <div class="card-body p-0">
                <table class="table table-sm mb-0">
                    <thead><tr><th>Phương thức</th><th class="text-end">Đơn</th><th class="text-end">Doanh thu</th></tr></thead>
                    <tbody>
                    <c:forEach items="${payments}" var="m">
                        <tr><td>${fn:escapeXml(m.label)}</td><td class="text-end">${fn:escapeXml(m.count)}</td>
                            <td class="text-end"><fmt:formatNumber value="${m.total}" pattern="#,##0"/> ₫</td></tr>
                    </c:forEach>
                    <c:if test="${empty payments}"><tr><td colspan="3" class="text-center text-muted">Chưa có dữ liệu</td></tr></c:if>
                    </tbody>
                </table>
                <div class="p-2"><canvas id="payChart" style="max-height:180px;"></canvas></div>
            </div>
        </div>
    </div>
</div>

<div class="row g-3 mt-1">
    <div class="col-12">
        <div class="card">
            <div class="card-header"><i class="fas fa-chart-line"></i> Doanh thu 12 tháng gần nhất</div>
            <div class="card-body"><canvas id="monthChart" style="max-height:320px;"></canvas></div>
        </div>
    </div>
</div>

<!-- ============ TOP SẢN PHẨM & TOPPING ============ -->
<div class="row g-3 mt-1 mb-4">
    <div class="col-md-6">
        <div class="card">
            <div class="card-header"><i class="fas fa-fire"></i> Top 10 sản phẩm bán chạy</div>
            <div class="card-body p-0">
                <table class="table table-striped mb-0">
                    <thead><tr><th>Sản phẩm</th><th class="text-end">Số lượng bán</th></tr></thead>
                    <tbody>
                    <c:forEach items="${topProducts}" var="row">
                        <tr><td>${fn:escapeXml(row[0])}</td><td class="text-end">${fn:escapeXml(row[1])}</td></tr>
                    </c:forEach>
                    <c:if test="${empty topProducts}"><tr><td colspan="2" class="text-center text-muted">Chưa có dữ liệu</td></tr></c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <div class="col-md-6">
        <div class="card">
            <div class="card-header"><i class="fas fa-ice-cream"></i> Top 10 topping bán chạy</div>
            <div class="card-body p-0">
                <table class="table table-striped mb-0">
                    <thead><tr><th>Topping</th><th class="text-end">Số lượt dùng</th></tr></thead>
                    <tbody>
                    <c:forEach items="${topToppings}" var="row">
                        <tr><td>${fn:escapeXml(row[0])}</td><td class="text-end">${fn:escapeXml(row[1])}</td></tr>
                    </c:forEach>
                    <c:if test="${empty topToppings}"><tr><td colspan="2" class="text-center text-muted">Chưa có dữ liệu</td></tr></c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>
function onRange(v) {
    if (v === 'custom') { document.getElementById('customDates').style.display = ''; return; }
    document.getElementById('filterForm').submit();
}

document.addEventListener('DOMContentLoaded', function () {
    const vnd = v => Number(v).toLocaleString('vi-VN') + ' ₫';
    // Bảng màu biểu đồ nổi bật nhưng vẫn đồng bộ nhận diện UTETra.
    const chartColors = {
        navy: '#243B53', blue: '#3B82F6', sky: '#38BDF8', teal: '#14B8A6',
        green: '#22A06B', coral: '#F06C52', amber: '#F4B942', violet: '#8B5CF6',
        rose: '#D9465F', slate: '#829AB1'
    };
    Chart.defaults.color = '#486581';
    Chart.defaults.borderColor = 'rgba(72, 101, 129, .16)';
    Chart.defaults.font.family = '"Be Vietnam Pro", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif';
    const moneyScale = { y: { beginAtZero: true, grid: { color: 'rgba(72, 101, 129, .14)' }, ticks: { callback: v => vnd(v) } } };
    const moneyTip = { callbacks: { label: ctx => ctx.dataset.label + ': ' + vnd(ctx.raw) } };

    const labelsDay = [<c:forEach items="${labelsDay}" var="l" varStatus="s">"${fn:escapeXml(l)}"<c:if test="${!s.last}">,</c:if></c:forEach>];
    const dayCollected = [<c:forEach items="${dataDayCollected}" var="d" varStatus="s">${d}<c:if test="${!s.last}">,</c:if></c:forEach>];
    const dayExpected = [<c:forEach items="${dataDayExpected}" var="d" varStatus="s">${d}<c:if test="${!s.last}">,</c:if></c:forEach>];

    new Chart(document.getElementById('dayChart'), {
        type: 'bar',
        data: { labels: labelsDay, datasets: [
            { label: 'Đã thu', data: dayCollected, backgroundColor: 'rgba(59,130,246,.92)', borderColor: chartColors.blue, borderWidth: 1, borderRadius: 5 },
            { label: 'Dự kiến', data: dayExpected, backgroundColor: 'rgba(244,185,66,.88)', borderColor: '#D99B16', borderWidth: 1, borderRadius: 5 }
        ]},
        options: { responsive: true, plugins: { tooltip: moneyTip }, scales: moneyScale }
    });

    // Thứ tự màu tương ứng: chờ xác nhận, pha chế, đang giao, hoàn thành, đã hủy, giao thất bại.
    const statusColors = [chartColors.amber, chartColors.blue, chartColors.teal,
                          chartColors.green, chartColors.coral, chartColors.rose];
    new Chart(document.getElementById('statusChart'), {
        type: 'doughnut',
        data: {
            labels: [<c:forEach items="${statusLabels}" var="l" varStatus="s">"${fn:escapeXml(l)}"<c:if test="${!s.last}">,</c:if></c:forEach>],
            datasets: [{ data: [<c:forEach items="${statusData}" var="d" varStatus="s">${d}<c:if test="${!s.last}">,</c:if></c:forEach>],
                         backgroundColor: statusColors, borderColor: '#ffffff', borderWidth: 3, hoverOffset: 8 }]
        },
        options: { responsive: true, plugins: { legend: { position: 'bottom' },
            tooltip: { callbacks: { label: ctx => ctx.label + ': ' + ctx.raw + ' đơn' } } } }
    });

    new Chart(document.getElementById('hourChart'), {
        type: 'bar',
        data: {
            labels: [<c:forEach items="${hourLabels}" var="l" varStatus="s">"${fn:escapeXml(l)}"<c:if test="${!s.last}">,</c:if></c:forEach>],
            datasets: [{ label: 'Số đơn', data: [<c:forEach items="${hourOrders}" var="d" varStatus="s">${d}<c:if test="${!s.last}">,</c:if></c:forEach>],
                         backgroundColor: context => {
                             const values = context.chart.data.datasets[0].data;
                             const peak = Math.max(0, ...values.map(Number));
                             return Number(context.raw) === peak && peak > 0 ? chartColors.coral : 'rgba(59,130,246,.86)';
                         },
                         hoverBackgroundColor: chartColors.teal, borderRadius: 5, borderSkipped: false }]
        },
        options: { responsive: true, plugins: { legend: { display: false },
                   tooltip: { callbacks: { label: ctx => 'Số đơn: ' + ctx.raw } } },
                   scales: { x: { grid: { display: false } }, y: { beginAtZero: true, grid: { color: 'rgba(72, 101, 129, .14)' }, ticks: { precision: 0 } } } }
    });

    new Chart(document.getElementById('payChart'), {
        type: 'doughnut',
        data: {
            labels: [<c:forEach items="${payLabels}" var="l" varStatus="s">"${fn:escapeXml(l)}"<c:if test="${!s.last}">,</c:if></c:forEach>],
            datasets: [{ data: [<c:forEach items="${payTotals}" var="d" varStatus="s">${d}<c:if test="${!s.last}">,</c:if></c:forEach>],
                         backgroundColor: [chartColors.blue, chartColors.teal, chartColors.amber, chartColors.coral, chartColors.violet],
                         borderColor: '#ffffff', borderWidth: 3, hoverOffset: 7 }]
        },
        options: { responsive: true, plugins: { legend: { position: 'bottom', labels: { boxWidth: 12 } },
            tooltip: { callbacks: { label: ctx => ctx.label + ': ' + vnd(ctx.raw) } } } }
    });

    new Chart(document.getElementById('monthChart'), {
        type: 'line',
        data: {
            labels: [<c:forEach items="${labels12}" var="l" varStatus="s">"${fn:escapeXml(l)}"<c:if test="${!s.last}">,</c:if></c:forEach>],
            datasets: [
                { label: 'Đã thu', data: [<c:forEach items="${data12Collected}" var="d" varStatus="s">${d}<c:if test="${!s.last}">,</c:if></c:forEach>],
                  borderColor: chartColors.blue, backgroundColor: 'rgba(59,130,246,.14)', fill: true, tension: .3, borderWidth: 3, pointRadius: 4, pointHoverRadius: 6 },
                { label: 'Dự kiến', data: [<c:forEach items="${data12Expected}" var="d" varStatus="s">${d}<c:if test="${!s.last}">,</c:if></c:forEach>],
                  borderColor: chartColors.coral, backgroundColor: 'rgba(240,108,82,.05)', tension: .3, borderWidth: 3, borderDash: [7, 4], pointRadius: 3, pointHoverRadius: 5 }
            ]
        },
        options: { responsive: true, plugins: { tooltip: moneyTip }, scales: moneyScale }
    });
});
</script>