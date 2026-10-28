<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Đơn ${fn:escapeXml(order.orderCode)}</title>

<c:if test="${success != null}">
    <div class="alert alert-success alert-dismissible fade show">
        ${fn:escapeXml(success)}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>
<c:if test="${error != null}">
    <div class="alert alert-danger alert-dismissible fade show">
        ${fn:escapeXml(error)}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<div class="row g-4">
    <%-- ================= CỘT TRÁI ================= --%>
    <div class="col-lg-8">
        <div class="card mb-4">
            <div class="card-header bg-white d-flex justify-content-between align-items-center">
                <span class="fw-bold"><i class="fas fa-receipt text-primary me-1"></i> Chi tiết đơn hàng</span>
                <c:choose>
                    <c:when test="${order.status == 'PENDING'}"><span class="badge bg-warning text-dark px-3 py-2">Chờ xác nhận</span></c:when>
                    <c:when test="${order.status == 'PREPARING'}"><span class="badge bg-info text-dark px-3 py-2">Đang pha chế</span></c:when>
                    <c:when test="${order.status == 'READY'}"><span class="badge bg-warning text-dark px-3 py-2">Chờ giao lại</span></c:when>
                    <c:when test="${order.status == 'DELIVERING'}"><span class="badge bg-primary px-3 py-2">Đang giao</span></c:when>
                    <c:when test="${order.status == 'COMPLETED'}"><span class="badge bg-success px-3 py-2">Đã hoàn thành</span></c:when>
                    <c:when test="${order.status == 'FAILED'}"><span class="badge bg-secondary px-3 py-2">Giao thất bại</span></c:when>
                    <c:when test="${order.status == 'CANCELLED'}"><span class="badge bg-danger px-3 py-2">Đã hủy</span></c:when>
                    <c:when test="${order.status == 'COMPLAINED'}"><span class="badge bg-warning text-dark px-3 py-2">Đang khiếu nại</span></c:when>
                    <c:when test="${order.status == 'REFUNDED'}"><span class="badge bg-dark px-3 py-2">Đã hoàn tiền</span></c:when>
                </c:choose>
            </div>
            <div class="card-body">
                <div class="row g-4 mb-4">
                    <div class="col-md-6 border-end-md">
                        <h6 class="text-muted small text-uppercase fw-bold mb-3">Thông tin khách hàng</h6>
                        <div class="mb-2">
                            <span class="text-muted small">Khách hàng:</span>
                            <div class="fw-semibold">${fn:escapeXml(order.user.fullName)}</div>
                            <div class="small text-muted">${fn:escapeXml(order.user.email)}</div>
                        </div>
                        <div class="mb-2">
                            <span class="text-muted small">SĐT nhận hàng:</span>
                            <div class="fw-semibold">${fn:escapeXml(order.phone)}</div>
                        </div>
                        <div class="mb-2">
                            <span class="text-muted small">Hình thức:</span>
                            <div class="fw-semibold">${fn:escapeXml(order.pickup ? 'Đến lấy tại cửa hàng' : 'Giao tận nơi')}</div>
                        </div>
                        <div class="mb-2">
                            <span class="text-muted small">Thời gian:</span>
                            <div class="fw-semibold">${fn:escapeXml(order.deliveryTime == 'ASAP' ? 'Càng sớm càng tốt' : order.deliveryTime)}</div>
                        </div>
                        <div class="mb-2">
                            <span class="text-muted small">Địa chỉ:</span>
                            <div class="fw-semibold">${fn:escapeXml(order.shippingAddress)}</div>
                        </div>
                        <c:if test="${not empty order.vatInfo}">
                            <div class="mt-3 p-2 bg-light rounded small">
                                <b>Hoá đơn VAT:</b> ${fn:escapeXml(order.vatInfo)}
                            </div>
                        </c:if>
                    </div>

                    <div class="col-md-6">
                        <h6 class="text-muted small text-uppercase fw-bold mb-3">Thông tin thanh toán</h6>
                        <div class="mb-2">
                            <span class="text-muted small">Phương thức:</span>
                            <div class="fw-semibold">${fn:escapeXml(order.paymentMethod)}</div>
                        </div>
                        <div class="mb-2">
                            <span class="text-muted small">Ngày đặt:</span>
                            <div class="fw-semibold">${fn:escapeXml(order.createdAtFormatted)}</div>
                        </div>
                        <div class="mb-2">
                            <span class="text-muted small">Cập nhật cuối:</span>
                            <div class="fw-semibold">${fn:escapeXml(order.updatedAtFormatted)}</div>
                        </div>
                        <div class="mb-2">
                            <span class="text-muted small">Chi nhánh xử lý:</span>
                            <div class="fw-semibold">${fn:escapeXml(order.branch.name)}</div>
                        </div>
                        <c:if test="${not empty order.note}">
                            <div class="mt-3 p-2 bg-light rounded small">
                                <b>Ghi chú:</b> ${fn:escapeXml(order.note)}
                            </div>
                        </c:if>
                    </div>
                </div>

                <div class="bg-light rounded p-3 mt-3">
                    <div class="row align-items-center">
                        <div class="col-md-6">
                            <div class="d-flex justify-content-between mb-1 small">
                                <span class="text-muted">Tiền hàng:</span>
                                <span class="fw-semibold"><fmt:formatNumber value="${order.subtotalAmount}" pattern="#,##0"/> ₫</span>
                            </div>
                            <div class="d-flex justify-content-between mb-1 small">
                                <span class="text-muted">Phí vận chuyển:</span>
                                <span class="fw-semibold"><fmt:formatNumber value="${order.shippingFee}" pattern="#,##0"/> ₫</span>
                            </div>
                            <c:if test="${order.discountAmount != null && order.discountAmount > 0}">
                                <div class="d-flex justify-content-between mb-1 small text-success">
                                    <span>Mã giảm giá (${fn:escapeXml(order.voucherCode)}):</span>
                                    <span class="fw-semibold">-<fmt:formatNumber value="${order.discountAmount}" pattern="#,##0"/> ₫</span>
                                </div>
                            </c:if>
                            <c:if test="${order.pointsDiscount != null && order.pointsDiscount > 0}">
                                <div class="d-flex justify-content-between mb-1 small text-success">
                                    <span>Dùng điểm:</span>
                                    <span class="fw-semibold">-<fmt:formatNumber value="${order.pointsDiscount}" pattern="#,##0"/> ₫</span>
                                </div>
                            </c:if>
                        </div>
                        <div class="col-md-6 text-md-end mt-3 mt-md-0">
                            <div class="text-muted small text-uppercase mb-1">Tổng thanh toán</div>
                            <div class="fs-3 fw-bold text-danger">
                                <fmt:formatNumber value="${order.totalAmount}" pattern="#,##0"/> ₫
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <%-- Card khiếu nại / hoàn tiền --%>
        <c:if test="${order.status == 'COMPLAINED'}">
        <div class="card mb-4 border-warning">
            <div class="card-header bg-white fw-bold text-warning">
                <i class="fas fa-exclamation-triangle me-1"></i> Xử lý khiếu nại
            </div>
            <div class="card-body">
                <div class="mb-3 p-3 bg-light rounded">
                    <div class="small text-muted mb-1">Lý do khách khiếu nại:</div>
                    <div>${fn:escapeXml(order.complaintReason)}</div>
                    <c:if test="${not empty order.complaintAtFormatted}">
                        <div class="small text-muted mt-1">Gửi lúc ${fn:escapeXml(order.complaintAtFormatted)}</div>
                    </c:if>
                    <c:if test="${not empty order.complaintMediaUrl}">
                        <div class="mt-3">
                            <div class="small text-muted mb-1">Tệp đính kèm:</div>
                            <c:choose>
                                <c:when test="${fn:startsWith(order.complaintMediaType, 'video/')}">
                                    <video controls preload="metadata" style="max-width: 100%; max-height: 360px; border-radius: 8px;">
                                        <source src="${fn:escapeXml(order.complaintMediaUrl)}"
                                                type="${fn:escapeXml(order.complaintMediaType)}">
                                        Trình duyệt không hỗ trợ phát video.
                                    </video>
                                </c:when>
                                <c:otherwise>
                                    <a href="${fn:escapeXml(order.complaintMediaUrl)}" target="_blank" rel="noopener">
                                        <img src="${fn:escapeXml(order.complaintMediaUrl)}"
                                             alt="Ảnh đính kèm khiếu nại" loading="lazy"
                                             style="max-width: 100%; max-height: 360px; object-fit: contain; border-radius: 8px;">
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:if>
                </div>
                <form action="${pageContext.request.contextPath}/admin/orders/refund" method="post" class="row g-2 align-items-end mb-3">
                    <input type="hidden" name="id" value="${fn:escapeXml(order.id)}">
                    <div class="col-md-4">
                        <label class="form-label small fw-semibold">Số tiền hoàn (₫) *</label>
                        <input type="number" name="amount" class="form-control" required
                               min="0" step="1000" max="${fn:escapeXml(order.totalAmount)}"
                               value="${fn:escapeXml(order.totalAmount)}">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small fw-semibold">Ghi chú xử lý</label>
                        <input type="text" name="note" class="form-control" maxlength="500"
                               placeholder="VD: đã chuyển khoản hoàn tiền cho khách">
                    </div>
                    <div class="col-md-2">
                        <button class="btn btn-success w-100 fw-bold"
                                onclick="return confirm('Xác nhận hoàn tiền cho đơn này?')">
                            <i class="fas fa-money-bill-wave"></i> Hoàn tiền
                        </button>
                    </div>
                </form>
                <hr>
                <form action="${pageContext.request.contextPath}/admin/orders/reject-complaint" method="post" class="row g-2 align-items-end">
                    <input type="hidden" name="id" value="${fn:escapeXml(order.id)}">
                    <div class="col-md-10">
                        <label class="form-label small fw-semibold">Lý do từ chối (khách sẽ thấy)</label>
                        <input type="text" name="note" class="form-control" maxlength="500"
                               placeholder="Giải thích lý do không hoàn tiền">
                    </div>
                    <div class="col-md-2">
                        <button class="btn btn-outline-danger w-100"
                                onclick="return confirm('Từ chối khiếu nại và trả đơn về Hoàn thành?')">
                            <i class="fas fa-times"></i> Từ chối
                        </button>
                    </div>
                </form>
            </div>
        </div>
        </c:if>

        <c:if test="${order.refundAmount != null}">
        <div class="alert alert-success mb-4">
            <b><i class="fas fa-money-bill-wave"></i> Đã hoàn tiền:</b>
            <b><fmt:formatNumber value="${order.refundAmount}" pattern="#,##0"/> ₫</b>
            <c:if test="${not empty order.refundAtFormatted}">
                — lúc ${fn:escapeXml(order.refundAtFormatted)}
            </c:if>
            <c:if test="${not empty order.refundNote}">
                <div class="small mt-1">Ghi chú: ${fn:escapeXml(order.refundNote)}</div>
            </c:if>
        </div>
        </c:if>

        <%-- Danh sách món --%>
        <div class="card mb-4">
            <div class="card-header bg-white fw-bold"><i class="fas fa-box-open text-primary me-1"></i> Danh sách món</div>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Sản phẩm</th>
                            <th>Phân loại</th>
                            <th class="text-center">SL</th>
                            <th class="text-end">Đơn giá</th>
                            <th class="text-end">Thành tiền</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${order.details}" var="d">
                            <tr>
                                <td class="fw-semibold">${fn:escapeXml(d.product.name)}</td>
                                <td>
                                    <div class="small text-muted">
                                        <c:if test="${not empty d.size}">Size: ${fn:escapeXml(d.size)}</c:if>
                                        <c:if test="${not empty d.sugarLevel}"> · Đường: ${fn:escapeXml(d.sugarLevel)}</c:if>
                                        <c:if test="${not empty d.iceLevel}"> · Đá: ${fn:escapeXml(d.iceLevel)}</c:if>
                                    </div>
                                    <c:if test="${not empty d.toppings}">
                                        <div class="small text-muted mt-1">
                                            Topping:
                                            <c:forEach items="${d.toppings}" var="t" varStatus="st">${fn:escapeXml(t.name)}<c:if test="${!st.last}">, </c:if></c:forEach>
                                        </div>
                                    </c:if>
                                </td>
                                <td class="text-center fw-semibold">${fn:escapeXml(d.quantity)}</td>
                                <td class="text-end"><fmt:formatNumber value="${d.unitPrice}" pattern="#,##0"/> ₫</td>
                                <td class="text-end fw-bold"><fmt:formatNumber value="${d.subtotal}" pattern="#,##0"/> ₫</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <c:if test="${not empty deliveryFailures}">
            <div class="card mb-4 border-danger">
                <div class="card-header bg-white text-danger fw-bold"><i class="fas fa-triangle-exclamation me-1"></i> Lịch sử giao thất bại</div>
                <div class="card-body p-0">
                    <ul class="list-group list-group-flush">
                        <c:forEach var="failure" items="${deliveryFailures}">
                            <li class="list-group-item py-3">
                                <div class="d-flex justify-content-between mb-1">
                                    <span class="fw-semibold text-danger">Shipper: ${fn:escapeXml(failure.shipper.fullName)}</span>
                                    <span class="small text-muted">${fn:escapeXml(failure.failedAtFormatted)}</span>
                                </div>
                                <div class="small">Lý do: ${fn:escapeXml(failure.reason)}</div>
                            </li>
                        </c:forEach>
                    </ul>
                </div>
            </div>
        </c:if>
    </div>

    <%-- ================= CỘT PHẢI ================= --%>
    <div class="col-lg-4">
        <div class="sticky-top" style="top: 80px; z-index: 10;">

            <div class="card mb-4">
                <div class="card-header bg-white fw-bold"><i class="fas fa-exchange-alt text-primary me-1"></i> Cập nhật trạng thái</div>
                <div class="card-body">
                    <c:choose>
                        <c:when test="${order.status == 'COMPLAINED'}">
                            <div class="alert alert-warning py-2 mb-2">
                                <b>Đơn đang khiếu nại.</b><br>
                                <span class="small">Vui lòng dùng <b>Hoàn tiền</b> hoặc <b>Từ chối</b> ở card "Xử lý khiếu nại".</span>
                            </div>
                        </c:when>
                        <c:when test="${order.status == 'REFUNDED'}">
                            <div class="alert alert-dark py-2 mb-2">
                                <b>Đơn đã hoàn tiền — không thể đổi trạng thái.</b>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <form action="${pageContext.request.contextPath}/admin/orders/update-status" method="post">
                                <input type="hidden" name="id" value="${fn:escapeXml(order.id)}">
                                <div class="mb-3">
                                    <label class="form-label small fw-semibold">Trạng thái mới</label>
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
                                <button class="btn btn-warning w-100 fw-bold">
                                    <i class="fas fa-save me-1"></i> Lưu trạng thái
                                </button>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <c:if test="${!order.pickup}">
                <div class="card mb-4">
                    <div class="card-header bg-white fw-bold"><i class="fas fa-motorcycle text-primary me-1"></i> Phân công giao hàng</div>
                    <div class="card-body">
                        <c:if test="${order.shipper != null}">
                            <div class="alert alert-info py-2 mb-3">
                                <div class="small text-muted mb-1">Shipper hiện tại:</div>
                                <div class="fw-semibold">${fn:escapeXml(order.shipper.fullName)}</div>
                                <div class="small">${fn:escapeXml(order.shipper.phone != null ? order.shipper.phone : 'Chưa có SĐT')}</div>
                            </div>
                        </c:if>

                        <form action="${pageContext.request.contextPath}/admin/orders/assign-shipper" method="post">
                            <input type="hidden" name="id" value="${fn:escapeXml(order.id)}">
                            <div class="mb-3">
                                <label class="form-label small fw-semibold">Chọn shipper</label>
                                <select name="shipperId" class="form-select">
                                    <option value="">-- Bỏ phân công --</option>
                                    <c:forEach items="${shippers}" var="s">
                                        <option value="${fn:escapeXml(s.id)}" <c:if test="${order.shipper != null && order.shipper.id == s.id}">selected</c:if>>
                                            ${fn:escapeXml(s.fullName)} (${fn:escapeXml(s.phone != null ? s.phone : 'Chưa SĐT')})
                                        </option>
                                    </c:forEach>
                                </select>
                                <c:if test="${empty shippers}">
                                    <small class="text-danger d-block mt-1">Không có shipper nào trong chi nhánh này.</small>
                                </c:if>
                            </div>
                            <button class="btn btn-outline-primary w-100 fw-bold">
                                <i class="fas fa-user-check me-1"></i> Xác nhận
                            </button>
                        </form>
                    </div>
                </div>
            </c:if>

        </div>
    </div>
</div>

<style>
@media (min-width: 768px) {
    .border-end-md { border-right: 1px solid #dee2e6; }
}
</style>