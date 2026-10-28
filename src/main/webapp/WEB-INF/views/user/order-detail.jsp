<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Chi tiết đơn hàng</title>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="od-head">
    <div>
        <h3 class="od-title">Đơn hàng ${fn:escapeXml(order.orderCode)}</h3>
        <div class="od-meta">
            <span class="badge status-${fn:escapeXml(fn:toLowerCase(order.status))}">${fn:escapeXml(order.statusLabel)}</span>
            <c:if test="${not empty order.createdAtFormatted}"><span><i class="far fa-clock"></i> Đặt lúc ${fn:escapeXml(order.createdAtFormatted)}</span></c:if>
        </div>
    </div>
    <div class="od-actions">
        <c:if test="${order.complainable}">
            <button class="btn btn-outline-warning" data-bs-toggle="modal" data-bs-target="#complaintModal">
                <i class="fas fa-exclamation-triangle"></i> Khiếu nại đơn hàng
            </button>
        </c:if>
        <c:if test="${order.cancellable}">
            <form action="${ctx}/order/cancel/${fn:escapeXml(order.id)}" method="post" onsubmit="return confirm('Hủy đơn hàng này? Mã giảm giá và điểm đã dùng sẽ được hoàn lại.')">
                <button class="btn btn-outline-danger"><i class="fas fa-ban"></i> Hủy đơn</button>
            </form>
        </c:if>
        <c:if test="${order.awaitingOnlinePayment}">
            <c:choose>
                <c:when test="${order.paymentMethod == 'MOMO'}">
                    <a href="${ctx}/payment/momo/${fn:escapeXml(order.id)}" class="btn btn-warning" data-no-lock="true"><i class="fas fa-wallet"></i> Thanh toán MoMo</a>
                </c:when>
                <c:otherwise>
                    <a href="${ctx}/payment/vnpay/${fn:escapeXml(order.id)}" class="btn btn-warning" data-no-lock="true"><i class="fas fa-credit-card"></i> Thanh toán VNPay</a>
                </c:otherwise>
            </c:choose>
        </c:if>
        <form action="${ctx}/order/reorder/${fn:escapeXml(order.id)}" method="post">
            <button class="btn btn-outline-dark"><i class="fas fa-rotate-right"></i> Đặt lại đơn này</button>
        </form>
    </div>
</div>

<c:choose>
    <c:when test="${order.progressStep > 0}">
        <div class="od-card od-progress-card">
            <ol class="order-progress" aria-label="Tiến trình đơn hàng">
                <li class="${order.progressStep >= 1 ? 'done' : ''} ${order.progressStep == 1 ? 'now' : ''}"><i class="fas fa-receipt"></i><span>Đã tiếp nhận</span></li>
                <li class="${order.progressStep >= 2 ? 'done' : ''} ${order.progressStep == 2 ? 'now' : ''}"><i class="fas fa-blender"></i><span>${order.status == 'READY' ? 'Chờ giao lại' : 'Đang pha chế'}</span></li>
                <li class="${order.progressStep >= 3 ? 'done' : ''} ${order.progressStep == 3 ? 'now' : ''}"><i class="${order.pickup ? 'fas fa-store' : 'fas fa-motorcycle'}"></i><span>${order.pickup ? 'Sẵn sàng nhận' : 'Đang giao'}</span></li>
                <li class="${order.progressStep >= 4 ? 'done' : ''} ${order.progressStep == 4 ? 'now' : ''}"><i class="fas fa-circle-check"></i><span>Hoàn thành</span></li>
            </ol>
        </div>
    </c:when>
    <c:otherwise>
        <div class="alert alert-secondary">Đơn này: <b>${fn:escapeXml(order.statusLabel)}</b>.</div>
    </c:otherwise>
</c:choose>

<c:if test="${not empty order.failedReason}">
    <div class="alert alert-secondary">
        <b>Lý do lần giao thất bại gần nhất:</b> ${fn:escapeXml(order.failedReason)}
    </div>
</c:if>

<c:if test="${not empty order.complaintReason}">
    <div class="alert alert-warning">
        <b><i class="fas fa-exclamation-triangle"></i> Khiếu nại của bạn:</b>
        ${fn:escapeXml(order.complaintReason)}
        <c:if test="${not empty order.complaintAtFormatted}">
            <div class="small text-muted mt-1">Gửi lúc ${fn:escapeXml(order.complaintAtFormatted)}</div>
        </c:if>
        <c:if test="${not empty order.complaintMediaUrl}">
            <div class="mt-2">
                <c:choose>
                    <c:when test="${fn:startsWith(order.complaintMediaType, 'video/')}">
                        <video controls preload="metadata" style="max-width: 100%; max-height: 360px; border-radius: 8px;">
                            <source src="${fn:escapeXml(order.complaintMediaUrl)}"
                                    type="${fn:escapeXml(order.complaintMediaType)}">
                            Trình duyệt không hỗ trợ phát video.
                        </video>
                    </c:when>
                    <c:otherwise>
                        <img src="${fn:escapeXml(order.complaintMediaUrl)}"
                             alt="Ảnh đính kèm khiếu nại" loading="lazy"
                             style="max-width: 100%; max-height: 360px; object-fit: contain; border-radius: 8px;">
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>
    </div>
</c:if>

<c:if test="${order.refundAmount != null}">
    <div class="alert alert-success">
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

<div class="row g-3 g-lg-4 align-items-start">
    <div class="col-lg-8">
        <div class="od-card">
            <h5 class="od-card-title"><i class="fas fa-circle-info"></i> Thông tin đơn hàng</h5>
            <div class="od-info-grid">
                <div class="od-info">
                    <i class="${order.pickup ? 'fas fa-store' : 'fas fa-motorcycle'}"></i>
                    <div><small>Hình thức nhận hàng</small>
                        <b>${fn:escapeXml(order.pickup ? 'Đến lấy tại cửa hàng' : 'Giao tận nơi')}</b>
                        <c:if test="${order.branch != null}"><span>${fn:escapeXml(order.branch.name)}</span></c:if></div>
                </div>
                <div class="od-info">
                    <i class="fas fa-location-dot"></i>
                    <div><small>${order.pickup ? 'Địa chỉ' : 'Địa chỉ giao hàng'}</small>
                        <b>${not empty order.shippingAddress ? fn:escapeXml(order.shippingAddress) : '—'}</b></div>
                </div>
                <div class="od-info">
                    <i class="fas fa-phone"></i>
                    <div><small>Số điện thoại</small>
                        <b>${not empty order.phone ? fn:escapeXml(order.phone) : '—'}</b></div>
                </div>
                <div class="od-info">
                    <i class="far fa-clock"></i>
                    <div><small>Thời gian nhận</small>
                        <b>${fn:escapeXml(empty order.deliveryTime ? '—' : (order.deliveryTime == 'ASAP' ? 'Càng sớm càng tốt' : order.deliveryTime))}</b></div>
                </div>
                <div class="od-info">
                    <i class="fas fa-credit-card"></i>
                    <div><small>Phương thức thanh toán</small>
                        <b>
                            <c:choose>
                                <c:when test="${order.paymentMethod == 'COD'}">Thanh toán khi nhận hàng (COD)</c:when>
                                <c:when test="${order.paymentMethod == 'CASH'}">Tiền mặt tại quầy</c:when>
                                <c:when test="${order.paymentMethod == 'BANK'}">Chuyển khoản ngân hàng</c:when>
                                <c:when test="${order.paymentMethod == 'VNPAY'}">VNPay</c:when>
                                <c:when test="${order.paymentMethod == 'MOMO'}">MoMo</c:when>
                                <c:otherwise>${fn:escapeXml(order.paymentMethod)}</c:otherwise>
                            </c:choose>
                        </b>
                        <c:if test="${order.paymentMethod == 'VNPAY' or order.paymentMethod == 'MOMO'}">
                            <span class="od-pay ${order.paymentStatus == 'PAID' ? 'paid' : 'unpaid'}">
                                <i class="fas ${order.paymentStatus == 'PAID' ? 'fa-circle-check' : 'fa-hourglass-half'}"></i>
                                ${order.paymentStatus == 'PAID' ? 'Đã thanh toán' : (order.paymentStatus == 'REFUNDED' ? 'Đã hoàn tiền' : 'Chưa thanh toán')}
                            </span>
                        </c:if></div>
                </div>
                <c:if test="${not empty order.vatInfo}">
                    <div class="od-info">
                        <i class="fas fa-file-invoice"></i>
                        <div><small>Hoá đơn VAT</small><b>${fn:escapeXml(order.vatInfo)}</b></div>
                    </div>
                </c:if>
                <c:if test="${not empty order.note}">
                    <div class="od-info od-info-wide">
                        <i class="far fa-note-sticky"></i>
                        <div><small>Ghi chú</small><b>${fn:escapeXml(order.note)}</b></div>
                    </div>
                </c:if>
            </div>
        </div>

        <div class="od-card">
            <h5 class="od-card-title"><i class="fas fa-mug-hot"></i> Sản phẩm đã đặt
                <span class="od-count">${fn:length(order.details)} món</span></h5>
            <div class="table-responsive">
                <table class="table od-table align-middle mb-0">
                    <thead><tr><th>Sản phẩm</th><th class="text-center">SL</th><th class="text-end">Đơn giá</th><th class="text-end">Thành tiền</th></tr></thead>
                    <tbody>
                        <c:forEach items="${order.details}" var="d">
                            <tr>
                                <td>
                                    <div class="od-prod">
                                        <c:if test="${not empty d.product.imageUrl}"><img src="${fn:escapeXml(d.product.thumbUrl)}" loading="lazy" alt=""></c:if>
                                        <div>
                                            <div class="od-prod-name">${fn:escapeXml(d.product.name)}</div>
                                            <div class="od-opts">
                                                <c:if test="${not empty d.size}"><span>Size ${fn:escapeXml(d.size)}</span></c:if>
                                                <c:if test="${not empty d.sugarLevel}"><span>Đường ${fn:escapeXml(d.sugarLevel)}</span></c:if>
                                                <c:if test="${not empty d.iceLevel}"><span>Đá ${fn:escapeXml(d.iceLevel)}</span></c:if>
                                            </div>
                                        </div>
                                    </div>
                                </td>
                                <td class="text-center">×${fn:escapeXml(d.quantity)}</td>
                                <td class="text-end text-nowrap"><fmt:formatNumber value="${d.unitPrice}" pattern="#,##0"/> ₫</td>
                                <td class="text-end text-nowrap fw-semibold"><fmt:formatNumber value="${d.subtotal}" pattern="#,##0"/> ₫</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="col-lg-4">
        <div class="od-card od-summary">
            <h5 class="od-card-title"><i class="fas fa-receipt"></i> Tóm tắt thanh toán</h5>
            <div class="sum-row"><span>Tiền hàng</span><span><fmt:formatNumber value="${order.subtotalAmount}" pattern="#,##0"/> ₫</span></div>
            <div class="sum-row"><span>Phí vận chuyển</span><span><fmt:formatNumber value="${order.shippingFee != null ? order.shippingFee : 0}" pattern="#,##0"/> ₫</span></div>
            <div class="sum-row"><span>Giảm giá<c:if test="${not empty order.voucherCode}"> <em class="od-voucher">${fn:escapeXml(order.voucherCode)}</em></c:if></span><span class="text-success">− <fmt:formatNumber value="${order.discountAmount != null ? order.discountAmount : 0}" pattern="#,##0"/> ₫</span></div>
            <c:if test="${order.shippingDiscountAmount != null && order.shippingDiscountAmount > 0}">
                <div class="sum-row"><span>Giảm phí vận chuyển</span><span class="text-success">− <fmt:formatNumber value="${order.shippingDiscountAmount}" pattern="#,##0"/> ₫</span></div>
            </c:if>
            <c:if test="${order.pointsUsed != null && order.pointsUsed > 0}">
                <div class="sum-row"><span>Dùng ${fn:escapeXml(order.pointsUsed)} điểm</span><span class="text-success">− <fmt:formatNumber value="${order.pointsDiscount}" pattern="#,##0"/> ₫</span></div>
            </c:if>
            <div class="od-total"><b>Tổng thanh toán</b><span class="sum-total"><fmt:formatNumber value="${order.totalAmount}" pattern="#,##0"/> ₫</span></div>
            <c:if test="${order.pointsEarned != null && order.pointsEarned > 0}"><div class="od-points"><i class="fas fa-gift"></i> +${fn:escapeXml(order.pointsEarned)} điểm hội viên</div></c:if>
        </div>
    </div>
</div>

<c:if test="${order.complainable}">
<div class="modal fade" id="complaintModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <form action="${ctx}/order/complain/${fn:escapeXml(order.id)}" method="post"
          enctype="multipart/form-data" class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title"><i class="fas fa-exclamation-triangle text-warning"></i> Gửi khiếu nại</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <label class="form-label">Lý do khiếu nại <span class="text-danger">*</span></label>
        <textarea name="reason" class="form-control" rows="4"
                  minlength="10" maxlength="1000" required
                  placeholder="Ví dụ: thiếu món, sai món, chất lượng không đảm bảo, giao trễ..."></textarea>
        <div class="form-text">Tối thiểu 10, tối đa 1000 ký tự.</div>

        <label for="complaintMedia" class="form-label mt-3">
            Ảnh hoặc video đính kèm <span class="text-muted fw-normal">(không bắt buộc)</span>
        </label>
        <input id="complaintMedia" type="file" name="media" class="form-control"
               accept="image/jpeg,image/png,image/gif,image/webp,video/mp4,video/webm,video/x-msvideo">
        <small class="text-muted d-block mt-1">
            Ảnh JPG, PNG, GIF, WEBP tối đa <b>5 MB</b>; video MP4, WEBM hoặc AVI tối đa <b>10 MB</b>.
        </small>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy</button>
        <button type="submit" class="btn btn-warning">
            <i class="fas fa-paper-plane"></i> Gửi khiếu nại
        </button>
      </div>
    </form>
  </div>
</div>
</c:if>

<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
<script>
    const userId = ${sessionScope.userId != null ? sessionScope.userId : 0};

    if (userId) {
        const socket = new SockJS('${pageContext.request.contextPath}/ws');
        const stomp = Stomp.over(socket);
        stomp.debug = null;

        stomp.connect({}, function () {
            stomp.subscribe('/topic/order-status/' + userId, function (msg) {
                const data = JSON.parse(msg.body);
                if (data.orderId == ${order.id}) {
                    location.reload();
                }
            });
        });
    }
</script>