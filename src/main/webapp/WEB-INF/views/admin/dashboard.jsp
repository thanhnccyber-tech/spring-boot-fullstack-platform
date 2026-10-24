<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Dashboard</title>

<!-- ============ CARDS THỐNG KÊ ============ -->
<div class="row g-3">
    <div class="col-md-3">
        <div class="card text-bg-primary">
            <div class="card-body">
                <h6><i class="fas fa-receipt"></i> Đơn hàng hôm nay</h6>
                <h2>${fn:escapeXml(todayOrderCount)}</h2>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-bg-success">
            <div class="card-body">
                <h6><i class="fas fa-money-bill-wave"></i> Doanh thu hôm nay</h6>
                <h2><fmt:formatNumber value="${todayRevenue}" pattern="#,##0"/> ₫</h2>
            </div>
        </div>
    </div>
    <div class="col-md-6">
        <div class="card">
            <div class="card-header"><i class="fas fa-chart-pie"></i> Trạng thái đơn hàng</div>
            <div class="card-body">
                <c:forEach items="${statusCount}" var="e">
                    <span class="badge bg-secondary me-2 fs-6 mb-1">${fn:escapeXml(e.key)}: ${fn:escapeXml(e.value)}</span>
                </c:forEach>
                <c:if test="${empty statusCount}">
                    <span class="text-muted">Chưa có đơn hàng</span>
                </c:if>
            </div>
        </div>
    </div>
</div>

<!-- ============ BIỂU ĐỒ ============ -->
<div class="row g-3 mt-1">
    <div class="col-md-8">
        <div class="card">
            <div class="card-header">
                <i class="fas fa-chart-bar"></i> Doanh thu 7 ngày gần nhất
                <span class="text-muted small float-end">Đơn vị: VNĐ</span>
            </div>
            <div class="card-body">
                <canvas id="revenue7Chart" style="max-height: 300px;"></canvas>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card h-100">
            <div class="card-header"><i class="fas fa-chart-pie"></i> Tỉ lệ đơn theo trạng thái</div>
            <div class="card-body">
                <canvas id="statusChart" style="max-height: 300px;"></canvas>
            </div>
        </div>
    </div>
</div>

<div class="row g-3 mt-1">
    <div class="col-md-12">
        <div class="card">
            <div class="card-header"><i class="fas fa-chart-line"></i> Doanh thu 12 tháng gần nhất</div>
            <div class="card-body">
                <canvas id="revenue12Chart" style="max-height: 320px;"></canvas>
            </div>
        </div>
    </div>
</div>

<!-- ============ TOP SẢN PHẨM & TOPPING ============ -->
<div class="row g-3 mt-1">
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
                        <c:if test="${empty topProducts}">
                            <tr><td colspan="2" class="text-center text-muted">Chưa có dữ liệu</td></tr>
                        </c:if>
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
                    <thead><tr><th>Topping</th><th class="text-end">Số lượng dùng</th></tr></thead>
                    <tbody>
                        <c:forEach items="${topToppings}" var="row">
                            <tr><td>${fn:escapeXml(row[0])}</td><td class="text-end">${fn:escapeXml(row[1])}</td></tr>
                        </c:forEach>
                        <c:if test="${empty topToppings}">
                            <tr><td colspan="2" class="text-center text-muted">Chưa có dữ liệu</td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- ============ SCRIPT VẼ CHART ============ -->
<script>
document.addEventListener('DOMContentLoaded', function () {

    // ---------- Chart doanh thu 7 ngày ----------
    const labels7 = [<c:forEach items="${labels7}" var="l" varStatus="s">'${fn:replace(fn:replace(fn:replace(fn:replace(fn:replace(fn:replace(l, '\\', ''), "'", ''), '"', ''), '<', ''), '>', ''), '&', '')}'<c:if test="${!s.last}">,</c:if></c:forEach>];
    const data7   = [<c:forEach items="${data7}"   var="d" varStatus="s">${d}<c:if test="${!s.last}">,</c:if></c:forEach>];

    new Chart(document.getElementById('revenue7Chart'), {
        type: 'bar',
        data: {
            labels: labels7,
            datasets: [{
                label: 'Doanh thu (VNĐ)',
                data: data7,
                backgroundColor: 'rgba(255, 193, 7, 0.7)',
                borderColor: 'rgba(255, 193, 7, 1)',
                borderWidth: 2,
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: ctx => Number(ctx.raw).toLocaleString('vi-VN') + ' ₫'
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { callback: v => Number(v).toLocaleString('vi-VN') + ' ₫' }
                }
            }
        }
    });

    // ---------- Chart doanh thu 12 tháng ----------
    const labels12 = [<c:forEach items="${labels12}" var="l" varStatus="s">'${fn:replace(fn:replace(fn:replace(fn:replace(fn:replace(fn:replace(l, '\\', ''), "'", ''), '"', ''), '<', ''), '>', ''), '&', '')}'<c:if test="${!s.last}">,</c:if></c:forEach>];
    const data12   = [<c:forEach items="${data12}"   var="d" varStatus="s">${d}<c:if test="${!s.last}">,</c:if></c:forEach>];

    new Chart(document.getElementById('revenue12Chart'), {
        type: 'line',
        data: {
            labels: labels12,
            datasets: [{
                label: 'Doanh thu (VNĐ)',
                data: data12,
                backgroundColor: 'rgba(25, 135, 84, 0.2)',
                borderColor: 'rgba(25, 135, 84, 1)',
                borderWidth: 3,
                fill: true,
                tension: 0.3,
                pointRadius: 5,
                pointBackgroundColor: 'rgba(25, 135, 84, 1)'
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: ctx => Number(ctx.raw).toLocaleString('vi-VN') + ' ₫'
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { callback: v => Number(v).toLocaleString('vi-VN') + ' ₫' }
                }
            }
        }
    });

    // ---------- Chart trạng thái (Pie) ----------
    const statusLabels = [<c:forEach items="${statusCount}" var="e" varStatus="s">'${fn:replace(fn:replace(fn:replace(fn:replace(fn:replace(fn:replace(e.key, '\\', ''), "'", ''), '"', ''), '<', ''), '>', ''), '&', '')}'<c:if test="${!s.last}">,</c:if></c:forEach>];
    const statusData   = [<c:forEach items="${statusCount}" var="e" varStatus="s">${e.value}<c:if test="${!s.last}">,</c:if></c:forEach>];

    new Chart(document.getElementById('statusChart'), {
        type: 'doughnut',
        data: {
            labels: statusLabels,
            datasets: [{
                data: statusData,
                backgroundColor: [
                    '#ffc107',
                    '#0dcaf0',
                    '#0d6efd',
                    '#198754',
                    '#dc3545',
                    '#6c757d'
                ],
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { position: 'bottom' },
                tooltip: {
                    callbacks: {
                        label: ctx => ctx.label + ': ' + ctx.raw + ' đơn'
                    }
                }
            }
        }
    });
});
</script>