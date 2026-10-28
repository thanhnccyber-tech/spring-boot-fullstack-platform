<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Mã giảm giá</title>

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

<div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
    <div class="text-muted">
        <i class="fas fa-ticket me-1"></i>
        Tổng cộng <b class="text-dark">${fn:length(vouchers)}</b> mã giảm giá
    </div>
    <c:if test="${isAdmin and form.id != null}">
        <a href="${pageContext.request.contextPath}/admin/vouchers" class="btn btn-outline-secondary">
            <i class="fas fa-plus me-1"></i> Tạo mã mới
        </a>
    </c:if>
</div>

<c:if test="${isAdmin}">
    <div class="card mb-3">
        <div class="card-header bg-white">
            <b>${form.id != null ? 'Cập nhật mã giảm giá' : 'Tạo mã giảm giá mới'}</b>
        </div>
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/admin/vouchers/save" method="post" class="row g-2">
                <c:if test="${form.id != null}">
                    <input type="hidden" name="id" value="${fn:escapeXml(form.id)}">
                </c:if>
                <div class="col-md-3">
                    <label class="form-label small fw-semibold mb-1">Mã (chữ HOA, số) *</label>
                    <input name="code" class="form-control text-uppercase" required maxlength="30"
                           value="${fn:escapeXml(form.code)}" placeholder="VD: WELCOME10">
                </div>
                <div class="col-md-3">
                    <label class="form-label small fw-semibold mb-1">Loại giảm</label>
                    <select name="discountType" class="form-select">
                        <option value="AMOUNT"  <c:if test="${form.discountType == 'AMOUNT'}">selected</c:if>>Số tiền cố định (đ)</option>
                        <option value="PERCENT" <c:if test="${form.discountType == 'PERCENT'}">selected</c:if>>Phần trăm (%)</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label small fw-semibold mb-1">Giá trị giảm *</label>
                    <input name="discountValue" type="number" step="1" min="1" class="form-control" required
                           value="${fn:escapeXml(form.id != null ? form.discountValue : '')}">
                </div>
                <div class="col-md-3">
                    <label class="form-label small fw-semibold mb-1">Đơn tối thiểu (đ)</label>
                    <input name="minOrderAmount" type="number" step="1" min="0" class="form-control"
                           value="${fn:escapeXml(form.id != null ? form.minOrderAmount : 0)}">
                </div>
                <div class="col-md-3">
                    <label class="form-label small fw-semibold mb-1">Giảm tối đa (chỉ loại %)</label>
                    <input name="maxDiscountAmount" type="number" step="1" min="0" class="form-control"
                           value="${fn:escapeXml(form.maxDiscountAmount)}">
                </div>
                <div class="col-md-3">
                    <label class="form-label small fw-semibold mb-1">Hết hạn</label>
                    <input name="expiresAt" type="datetime-local" class="form-control"
                           value="${fn:escapeXml(form.expiresAt)}">
                </div>
                <div class="col-md-3">
                    <label class="form-label small fw-semibold mb-1">Số lượt tối đa</label>
                    <input name="usageLimit" type="number" min="1" class="form-control"
                           placeholder="Không giới hạn" value="${fn:escapeXml(form.usageLimit)}">
                </div>
                <div class="col-md-3">
                    <label class="form-label small fw-semibold mb-1">Mô tả</label>
                    <input name="description" class="form-control" maxlength="255"
                           value="${fn:escapeXml(form.description)}">
                </div>
                <div class="col-12 d-flex align-items-center gap-3 pt-2 border-top">
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" name="active" value="true" id="vact"
                               <c:if test="${form.id == null || form.active}">checked</c:if>>
                        <label class="form-check-label" for="vact">Đang hoạt động</label>
                    </div>
                    <button class="btn btn-warning">
                        <i class="fas fa-save me-1"></i> ${form.id != null ? 'Cập nhật' : 'Tạo mã'}
                    </button>
                    <c:if test="${form.id != null}">
                        <a href="${pageContext.request.contextPath}/admin/vouchers" class="btn btn-outline-secondary">Hủy</a>
                    </c:if>
                </div>
            </form>
        </div>
    </div>
</c:if>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-dark">
                <tr>
                    <th style="width:180px">Mã</th>
                    <th class="text-center">Giảm</th>
                    <th class="text-end">Đơn tối thiểu</th>
                    <th class="text-center">Đã dùng</th>
                    <th>Hết hạn</th>
                    <th class="text-center" style="width:130px">Trạng thái</th>
                    <th class="text-end" style="width:120px">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${vouchers}" var="v">
                    <tr>
                        <td>
                            <div class="fw-semibold">${fn:escapeXml(v.code)}</div>
                            <c:if test="${not empty v.description}">
                                <div class="small text-muted">${fn:escapeXml(v.description)}</div>
                            </c:if>
                        </td>
                        <td class="text-center">
                            <c:choose>
                                <c:when test="${v.percent}">
                                    <span class="badge bg-info text-dark">${fn:escapeXml(v.discountValue)}%</span>
                                    <c:if test="${v.maxDiscountAmount != null}">
                                        <div class="small text-muted">tối đa <fmt:formatNumber value="${v.maxDiscountAmount}" pattern="#,##0"/>đ</div>
                                    </c:if>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-primary"><fmt:formatNumber value="${v.discountValue}" pattern="#,##0"/>đ</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-end"><fmt:formatNumber value="${v.minOrderAmount}" pattern="#,##0"/>đ</td>
                        <td class="text-center">
                            ${fn:escapeXml(v.usedCount)}<c:if test="${v.usageLimit != null}"><span class="text-muted"> / ${fn:escapeXml(v.usageLimit)}</span></c:if>
                        </td>
                        <td class="small">${fn:escapeXml(v.expiresAt != null ? fn:replace(v.expiresAt, 'T', ' ') : 'Không')}</td>
                        <td class="text-center">
                            <c:choose>
                                <c:when test="${isAdmin}">
                                    <form action="${pageContext.request.contextPath}/admin/vouchers/toggle" method="post">
                                        <input type="hidden" name="id" value="${fn:escapeXml(v.id)}">
                                        <button class="badge border-0 px-3 py-2 ${v.active ? 'bg-success' : 'bg-secondary'}"
                                                title="${v.active ? 'Bấm để tắt' : 'Bấm để bật'}">
                                            <i class="fas ${v.active ? 'fa-check-circle' : 'fa-ban'} me-1"></i>${v.active ? 'Đang bật' : 'Đã tắt'}
                                        </button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge px-3 py-2 ${v.active ? 'bg-success' : 'bg-secondary'}">
                                        <i class="fas ${v.active ? 'fa-check-circle' : 'fa-ban'} me-1"></i>${v.active ? 'Đang bật' : 'Đã tắt'}
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-end">
                            <c:if test="${isAdmin}">
                                <a href="${pageContext.request.contextPath}/admin/vouchers?edit=${fn:escapeXml(v.id)}"
                                   class="btn btn-sm btn-outline-primary" title="Sửa mã giảm giá">
                                    <i class="fas fa-pen"></i>
                                </a>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty vouchers}">
                    <tr>
                        <td colspan="7" class="text-center text-muted py-5">
                            <i class="fas fa-ticket fa-2x d-block mb-2 opacity-50"></i>
                            Chưa có mã giảm giá nào
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>