<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<title>Dashboard</title>

<div class="row g-3">
    <div class="col-md-3">
        <div class="card text-bg-primary">
            <div class="card-body">
                <h6>Đơn hàng hôm nay</h6>
                <h2>${todayOrderCount}</h2>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-bg-success">
            <div class="card-body">
                <h6>Doanh thu hôm nay</h6>
                <h2><fmt:formatNumber value="${todayRevenue}" pattern="#,##0"/> ₫</h2>
            </div>
        </div>
    </div>
    <div class="col-md-6">
        <div class="card">
            <div class="card-header">Trạng thái đơn hàng</div>
            <div class="card-body">
                <c:forEach items="${statusCount}" var="e">
                    <span class="badge bg-secondary me-2">${e.key}: ${e.value}</span>
                </c:forEach>
            </div>
        </div>
    </div>
</div>

<div class="card mt-4">
    <div class="card-header">🔥 Top sản phẩm bán chạy</div>
    <div class="card-body">
        <table class="table">
            <thead><tr><th>Sản phẩm</th><th>Số lượng bán</th></tr></thead>
            <tbody>
                <c:forEach items="${topProducts}" var="row">
                    <tr><td>${row[0]}</td><td>${row[1]}</td></tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>