<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Thống kê giao hàng</title>

<h5 class="mb-3"><i class="fas fa-chart-column text-primary"></i> Thống kê giao hàng</h5>

<%-- ========== HÔM NAY / TUẦN NÀY / THÁNG NÀY ========== --%>
<div class="row g-3 mb-4">
    <c:forEach var="p" items="${periods}">
        <div class="col-12 col-md-4">
            <div class="card stat-card shadow-sm h-100">
                <div class="card-body">
                    <div class="text-muted small mb-2">${fn:escapeXml(p.key)}</div>
                    <div class="d-flex justify-content-between">
                        <span>Giao thành công</span>
                        <b class="text-success">${p.value.delivered}</b>
                    </div>
                    <div class="d-flex justify-content-between">
                        <span>Giao thất bại</span>
                        <b class="text-secondary">${p.value.failed}</b>
                    </div>
                    <div class="d-flex justify-content-between">
                        <span>Giá trị đơn đã giao</span>
                        <b><fmt:formatNumber value="${p.value.revenue}" pattern="#,##0"/> ₫</b>
                    </div>
                    <div class="d-flex justify-content-between">
                        <span>Tiền COD đã thu</span>
                        <b class="text-danger"><fmt:formatNumber value="${p.value.codCollected}" pattern="#,##0"/> ₫</b>
                    </div>
                </div>
            </div>
        </div>
    </c:forEach>
</div>

<%-- ========== BIỂU ĐỒ 7 NGÀY GẦN NHẤT ========== --%>
<div class="card order-card shadow-sm">
    <div class="card-body">
        <h6 class="mb-3">Số đơn giao thành công 7 ngày gần nhất</h6>
        <canvas id="deliveredChart" height="120"></canvas>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
<script>
    new Chart(document.getElementById('deliveredChart'), {
        type: 'bar',
        data: {
            labels: [<c:forEach var="l" items="${chartLabels}" varStatus="st">'${fn:escapeXml(l)}'<c:if test="${!st.last}">,</c:if></c:forEach>],
            datasets: [{
                label: 'Đơn đã giao',
                data: [<c:forEach var="v" items="${chartValues}" varStatus="st">${v}<c:if test="${!st.last}">,</c:if></c:forEach>],
                backgroundColor: '#ffc107',
                borderRadius: 6
            }]
        },
        options: {
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
        }
    });
</script>
