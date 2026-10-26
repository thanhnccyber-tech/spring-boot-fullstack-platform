<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Đơn ${fn:escapeXml(order.orderCode)}</title>

<c:if test="${success != null}">
    <div class="alert alert-success alert-dismissible fade show">
        ${fn:escapeXml(success)}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>
<c:if test="${error != null}">
    <div class="alert alert-danger alert-dismissible fade show">
        ${fn:escapeXml(error)}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<!-- ============ THÔNG TIN ĐƠN ============ -->
<div class="card mb-3">
    <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
        <span><i class="fas fa-receipt"></i> Đơn hàng <b>${fn:escapeXml(order.orderCode)}</b></span>
        <c:choose>
            <c:when test="${order.status == 'PENDING'}">
                <span class="badge bg-warning text-dark fs-6">Chờ xác nhận</span>
            </c:when>
            <c:when test="${order.status == 'PREPARING'}">
                <span class="badge bg-info text-dark fs-6">Đang pha chế</span>
            </c:when>
            <c:when test="${order.status == 'READY'}">
                <span class="badge bg-warning text-dark fs-6">Chờ giao lại</span>
            </c:when>
            <c:when test="${order.status == 'DELIVERING'}">
                <span class="badge bg-primary fs-6">Đang giao</span>
            </c:when>
            <c:when test="${order.status == 'COMPLETED'}">
                <span class="badge bg-success fs-6">Đã hoàn thành</span>
            </c:when>
            <c:when test="${order.status == 'FAILED'}">
                <span class="badge bg-secondary fs-6">Giao thất bại</span>
            </c:when>
            <c:when test="${order.status == 'CANCELLED'}">
                <span class="badge bg-danger fs-6">Đã hủy</span>
            </c:when>
        </c:choose>
    </div>
    <div class="card-body">
        <div class="row">
            <div class="col-md-6">
                <p><b>Khách:</b> ${fn:escapeXml(order.user.fullName)} (${fn:escapeXml(order.user.email)})</p>
                <p><b>Điện thoại:</b> ${fn:escapeXml(order.user.phone != null ? order.user.phone : '—')}</p>
                <p><b>Nhận hàng:</b> ${fn:escapeXml(order.pickup ? 'Đến lấy tại cửa hàng' : 'Giao tận nơi')}
   · <b>SĐT:</b> ${fn:escapeXml(order.phone)}
   · <b>Thời gian:</b> ${fn:escapeXml(order.deliveryTime == 'ASAP' ? 'Càng sớm càng tốt' : order.deliveryTime)}</p>
                <p><b>Địa chỉ:</b> ${fn:escapeXml(order.shippingAddress)}</p>
                <c:if test="${not empty order.vatInfo}"><p><b>Hoá đơn VAT:</b> ${fn:escapeXml(order.vatInfo)}</p></c:if>
                <p class="small text-muted mb-1">Tiền hàng <fmt:formatNumber value="${order.subtotalAmount}" pattern="#,##0"/> ₫
                   · Ship <fmt:formatNumber value="${order.shippingFee != null ? order.shippingFee : 0}" pattern="#,##0"/> ₫
                   · Giảm <fmt:formatNumber value="${order.discountAmount != null ? order.discountAmount : 0}" pattern="#,##0"/> ₫
                   <c:if test="${not empty order.voucherCode}">(mã ${fn:escapeXml(order.voucherCode)})</c:if></p>
                <p><b>Chi nhánh:</b> ${fn:escapeXml(order.branch.name)}</p>
            </div>
            <div class="col-md-6">
                <p><b>Thanh toán:</b> ${fn:escapeXml(order.paymentMethod)}</p>
                <p><b>Ngày đặt:</b> ${fn:escapeXml(order.createdAtFormatted)}</p>
                <p><b>Cập nhật lần cuối:</b> ${fn:escapeXml(order.updatedAtFormatted)}</p>
                <p><b>Tổng tiền:</b>
                    <span class="text-danger fs-5 fw-bold">
                        <fmt:formatNumber value="${order.totalAmount}" pattern="#,##0"/> ₫
                    </span>
                </p>
                <c:if test="${order.note != null && !order.note.isBlank()}">
                    <p><b>Ghi chú:</b> ${fn:escapeXml(order.note)}</p>
                </c:if>
                <c:if test="${order.failedReason != null}">
                    <p><b>Lý do lần giao thất bại gần nhất:</b>
                        <span class="text-danger">${fn:escapeXml(order.failedReason)}</span>
                    </p>
                </c:if>
            </div>
        </div>
    </div>
</div>

<c:if test="${not empty deliveryFailures}">
    <div class="card mb-3">
        <div class="card-header"><i class="fas fa-clock-rotate-left"></i> Lịch sử giao thất bại</div>
        <div class="card-body">
            <c:forEach var="failure" items="${deliveryFailures}">
                <div class="border-bottom py-2">
                    <div class="small text-muted">${fn:escapeXml(failure.failedAtFormatted)}
                        · Shipper: ${fn:escapeXml(failure.shipper.fullName)}</div>
                    <div>${fn:escapeXml(failure.reason)}</div>
                </div>
            </c:forEach>
        </div>
    </div>
</c:if>

<!-- ============ PHÂN CÔNG SHIPPER ============ -->
<div class="card mb-3">
    <div class="card-header bg-info text-dark">
        <i class="fas fa-motorcycle"></i> Phân công shipper
    </div>
    <div class="card-body">
        <c:if test="${order.shipper != null}">
            <div class="alert alert-info mb-3">
                Shipper hiện tại: <b>${fn:escapeXml(order.shipper.fullName)}</b>
                (${fn:escapeXml(order.shipper.phone != null ? order.shipper.phone : 'Chưa có SĐT')})
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/admin/orders/assign-shipper"
              method="post" class="row g-2 align-items-center">
            <input type="hidden" name="id" value="${fn:escapeXml(order.id)}">
            <div class="col-md-6">
                <label class="form-label small text-muted">Chọn shipper:</label>
                <select name="shipperId" class="form-select">
                    <option value="">-- Bỏ phân công --</option>
                    <c:forEach items="${shippers}" var="s">
                        <option value="${fn:escapeXml(s.id)}"
                            <c:if test="${order.shipper != null && order.shipper.id == s.id}">selected</c:if>>
                            ${fn:escapeXml(s.fullName)} - ${fn:escapeXml(s.phone != null ? s.phone : 'Chưa có SĐT')}
                        </option>
                    </c:forEach>
                </select>
                <c:if test="${empty shippers}">
                    <small class="text-muted">Chưa có shipper nào trong chi nhánh này</small>
                </c:if>
            </div>
            <div class="col-md-3">
                <button class="btn btn-info text-dark w-100 mt-4">
                    <i class="fas fa-user-check"></i> Phân công
                </button>
            </div>
        </form>
    </div>
</div>

<!-- ============ CẬP NHẬT TRẠNG THÁI ============ -->
<div class="card mb-3">
    <div class="card-header bg-warning text-dark">
        <i class="fas fa-exchange-alt"></i> Cập nhật trạng thái
    </div>
    <div class="card-body">
        <form action="${pageContext.request.contextPath}/admin/orders/update-status"
              method="post" class="row g-2">
            <input type="hidden" name="id" value="${fn:escapeXml(order.id)}">
            <div class="col-md-6">
                <select name="status" class="form-select">
                    <option value="PENDING"    <c:if test="${order.status=='PENDING'}">selected</c:if>>Chờ xác nhận</option>
                    <option value="PREPARING"  <c:if test="${order.status=='PREPARING'}">selected</c:if>>Đang pha chế</option>
                    <option value="READY"      <c:if test="${order.status=='READY'}">selected</c:if>>Chờ giao lại</option>
                    <option value="DELIVERING" <c:if test="${order.status=='DELIVERING'}">selected</c:if>>Đang giao</option>
                    <option value="COMPLETED"  <c:if test="${order.status=='COMPLETED'}">selected</c:if>>Đã hoàn thành</option>
                    <option value="FAILED"     <c:if test="${order.status=='FAILED'}">selected</c:if>>Giao thất bại</option>
                    <option value="CANCELLED"  <c:if test="${order.status=='CANCELLED'}">selected</c:if>>Đã hủy</option>
                </select>
            </div>
            <div class="col-md-3">
                <button class="btn btn-warning w-100">
                    <i class="fas fa-save"></i> Cập nhật
                </button>
            </div>
        </form>
    </div>
</div>

<!-- ============ CHI TIẾT MÓN ============ -->
<div class="card">
    <div class="card-header"><i class="fas fa-list"></i> Chi tiết món</div>
    <div class="card-body p-0">
        <table class="table mb-0 align-middle">
            <thead class="table-dark">
                <tr>
                    <th>Sản phẩm</th>
                    <th>Size</th>
                    <th>Đường</th>
                    <th>Đá</th>
                    <th>Topping</th>
                    <th class="text-center">SL</th>
                    <th class="text-end">Đơn giá</th>
                    <th class="text-end">Thành tiền</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${order.details}" var="d">
                    <tr>
                        <td>${fn:escapeXml(d.product.name)}</td>
                        <td>${fn:escapeXml(d.size)}</td>
                        <td>${fn:escapeXml(d.sugarLevel)}%</td>
                        <td>${fn:escapeXml(d.iceLevel)}%</td>
                        <td>
                            <c:forEach items="${d.toppings}" var="t">
                                <span class="badge bg-light text-dark border me-1">${fn:escapeXml(t.name)}</span>
                            </c:forEach>
                        </td>
                        <td class="text-center">${fn:escapeXml(d.quantity)}</td>
                        <td class="text-end">
                            <fmt:formatNumber value="${d.unitPrice}" pattern="#,##0"/> ₫
                        </td>
                        <td class="text-end text-danger fw-bold">
                            <fmt:formatNumber value="${d.subtotal}" pattern="#,##0"/> ₫
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
            <tfoot>
                <tr>
                    <th colspan="7" class="text-end">Tổng cộng:</th>
                    <th class="text-end text-danger fs-5">
                        <fmt:formatNumber value="${order.totalAmount}" pattern="#,##0"/> ₫
                    </th>
                </tr>
            </tfoot>
        </table>
    </div>
</div>