<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Mã giảm giá</title>

<c:if test="${success != null}"><div class="alert alert-success alert-dismissible fade show">${fn:escapeXml(success)}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div></c:if>
<c:if test="${error != null}"><div class="alert alert-danger alert-dismissible fade show">${fn:escapeXml(error)}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div></c:if>

<h5 class="mb-3">🎟️ Mã giảm giá</h5>

<form action="${pageContext.request.contextPath}/admin/vouchers/save" method="post" class="row g-2 bg-white p-3 rounded shadow-sm mb-4">
    <c:if test="${form.id != null}"><input type="hidden" name="id" value="${fn:escapeXml(form.id)}"></c:if>
    <div class="col-md-3">
        <label class="form-label small mb-0">Mã (chữ HOA, số)</label>
        <input name="code" class="form-control" required maxlength="30" value="${fn:escapeXml(form.code)}">
    </div>
    <div class="col-md-3">
        <label class="form-label small mb-0">Loại giảm</label>
        <select name="discountType" class="form-select">
            <option value="AMOUNT" <c:if test="${form.discountType == 'AMOUNT'}">selected</c:if>>Số tiền cố định (đ)</option>
            <option value="PERCENT" <c:if test="${form.discountType == 'PERCENT'}">selected</c:if>>Phần trăm (%)</option>
        </select>
    </div>
    <div class="col-md-3">
        <label class="form-label small mb-0">Giá trị giảm</label>
        <input name="discountValue" type="number" step="1" min="1" class="form-control" required value="${fn:escapeXml(form.id != null ? form.discountValue : '')}">
    </div>
    <div class="col-md-3">
        <label class="form-label small mb-0">Đơn tối thiểu (đ)</label>
        <input name="minOrderAmount" type="number" step="1" min="0" class="form-control" value="${fn:escapeXml(form.id != null ? form.minOrderAmount : 0)}">
    </div>
    <div class="col-md-3">
        <label class="form-label small mb-0">Giảm tối đa (chỉ loại %)</label>
        <input name="maxDiscountAmount" type="number" step="1" min="0" class="form-control" value="${fn:escapeXml(form.maxDiscountAmount)}">
    </div>
    <div class="col-md-3">
        <label class="form-label small mb-0">Hết hạn</label>
        <input name="expiresAt" type="datetime-local" class="form-control" value="${fn:escapeXml(form.expiresAt)}">
    </div>
    <div class="col-md-2">
        <label class="form-label small mb-0">Số lượt tối đa</label>
        <input name="usageLimit" type="number" min="1" class="form-control" placeholder="không giới hạn" value="${fn:escapeXml(form.usageLimit)}">
    </div>
    <div class="col-md-4">
        <label class="form-label small mb-0">Mô tả</label>
        <input name="description" class="form-control" maxlength="255" value="${fn:escapeXml(form.description)}">
    </div>
    <div class="col-12 d-flex align-items-center gap-3">
        <div class="form-check">
            <input class="form-check-input" type="checkbox" name="active" value="true" id="vact"
                   <c:if test="${form.id == null || form.active}">checked</c:if>>
            <label class="form-check-label" for="vact">Đang hoạt động</label>
        </div>
        <button class="btn btn-warning">${fn:escapeXml(form.id != null ? 'Cập nhật' : 'Tạo mã')}</button>
        <c:if test="${form.id != null}"><a href="${pageContext.request.contextPath}/admin/vouchers" class="btn btn-outline-secondary">Huỷ sửa</a></c:if>
    </div>
</form>

<table class="table table-bordered bg-white align-middle">
    <thead class="table-dark">
        <tr><th>Mã</th><th>Giảm</th><th>Đơn tối thiểu</th><th>Đã dùng</th><th>Hết hạn</th><th class="text-center">Trạng thái</th><th style="width:130px"></th></tr>
    </thead>
    <tbody>
    <c:forEach items="${vouchers}" var="v">
        <tr>
            <td><b>${fn:escapeXml(v.code)}</b><div class="small text-muted">${fn:escapeXml(v.description)}</div></td>
            <td>
                <c:choose>
                    <c:when test="${v.percent}">${fn:escapeXml(v.discountValue)}%
                        <c:if test="${v.maxDiscountAmount != null}"><span class="small text-muted">(tối đa <fmt:formatNumber value="${v.maxDiscountAmount}" pattern="#,##0"/>đ)</span></c:if>
                    </c:when>
                    <c:otherwise><fmt:formatNumber value="${v.discountValue}" pattern="#,##0"/>đ</c:otherwise>
                </c:choose>
            </td>
            <td><fmt:formatNumber value="${v.minOrderAmount}" pattern="#,##0"/>đ</td>
            <td>${fn:escapeXml(v.usedCount)}<c:if test="${v.usageLimit != null}"> / ${fn:escapeXml(v.usageLimit)}</c:if></td>
            <td>${fn:escapeXml(v.expiresAt != null ? fn:replace(v.expiresAt, 'T', ' ') : 'Không')}</td>
            <td class="text-center">
                <form action="${pageContext.request.contextPath}/admin/vouchers/toggle" method="post">
                    <input type="hidden" name="id" value="${fn:escapeXml(v.id)}">
                    <button class="badge border-0 ${fn:escapeXml(v.active ? 'bg-success' : 'bg-secondary')}">${fn:escapeXml(v.active ? 'Đang bật' : 'Đã tắt')}</button>
                </form>
            </td>
            <td><a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/admin/vouchers?edit=${fn:escapeXml(v.id)}"><i class="fas fa-edit"></i> Sửa</a></td>
        </tr>
    </c:forEach>
    <c:if test="${empty vouchers}"><tr><td colspan="7" class="text-center text-muted py-4">Chưa có mã giảm giá nào</td></tr></c:if>
    </tbody>
</table>
