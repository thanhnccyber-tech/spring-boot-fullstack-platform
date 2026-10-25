<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Lịch sử giao hàng</title>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<h5 class="mb-3"><i class="fas fa-clock-rotate-left text-secondary"></i> Lịch sử giao hàng</h5>

<c:if test="${empty orders}">
    <div class="text-center text-muted py-5">
        <i class="fas fa-box-open fa-2x mb-2"></i>
        <div>Bạn chưa có đơn nào đã giao.</div>
    </div>
</c:if>

<c:if test="${not empty orders}">
    <div class="card order-card shadow-sm">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-dark">
                    <tr>
                        <th>Mã đơn</th>
                        <th>Thời gian</th>
                        <th>Khách hàng</th>
                        <th>Địa chỉ</th>
                        <th class="text-end">Tổng tiền</th>
                        <th class="text-center">Kết quả</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="o" items="${orders}">
                        <tr>
                            <td class="fw-semibold">#${fn:escapeXml(o.orderCode)}</td>
                            <td class="text-nowrap">${fn:escapeXml(o.updatedAtFormatted)}</td>
                            <td>
                                <c:if test="${o.user != null}">${fn:escapeXml(o.user.fullName)}</c:if>
                                <div class="small text-muted">${fn:escapeXml(o.phone)}</div>
                            </td>
                            <td>${fn:escapeXml(o.shippingAddress)}</td>
                            <td class="text-end text-nowrap">
                                <fmt:formatNumber value="${o.totalAmount}" pattern="#,##0"/> ₫
                            </td>
                            <td class="text-center">
                                <c:choose>
                                    <c:when test="${o.status == 'COMPLETED'}">
                                        <span class="badge bg-success">Đã giao</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-secondary">Thất bại</span>
                                        <c:if test="${not empty o.failedReason}">
                                            <div class="small text-muted">${fn:escapeXml(o.failedReason)}</div>
                                        </c:if>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <a href="${ctx}/shipper/orders/${fn:escapeXml(o.id)}" class="btn btn-sm btn-outline-dark">
                                    <i class="fas fa-eye"></i>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
    <c:if test="${total > limit}">
        <p class="small text-muted mt-2">Hiển thị ${limit} đơn gần nhất trong tổng số ${total} đơn.</p>
    </c:if>
</c:if>
