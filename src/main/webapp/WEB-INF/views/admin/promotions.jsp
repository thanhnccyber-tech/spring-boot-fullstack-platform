<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Tin tức &amp; Khuyến mãi</title>

<c:if test="${success != null}"><div class="alert alert-success alert-dismissible fade show">${fn:escapeXml(success)}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div></c:if>
<c:if test="${error != null}"><div class="alert alert-danger alert-dismissible fade show">${fn:escapeXml(error)}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div></c:if>

<h5 class="mb-3">📣 Tin tức &amp; Khuyến mãi</h5>

<form action="${pageContext.request.contextPath}/admin/promotions/save" method="post" enctype="multipart/form-data" class="row g-2 bg-white p-3 rounded shadow-sm mb-4">
    <c:if test="${form.id != null}"><input type="hidden" name="id" value="${fn:escapeXml(form.id)}"></c:if>
    <div class="col-md-8">
        <label class="form-label small mb-0">Tiêu đề</label>
        <input name="title" class="form-control" required maxlength="200" value="${fn:escapeXml(form.title)}">
    </div>
    <div class="col-md-4">
        <label class="form-label small mb-0">Mã giảm giá đính kèm (tuỳ chọn)</label>
        <input name="voucherCode" class="form-control" maxlength="30" value="${fn:escapeXml(form.voucherCode)}">
    </div>
    <div class="col-12">
        <label class="form-label small mb-0">Tóm tắt (hiện ở danh sách)</label>
        <input name="summary" class="form-control" maxlength="500" value="${fn:escapeXml(form.summary)}">
    </div>
    <div class="col-12">
        <label class="form-label small mb-0">Nội dung</label>
        <textarea name="content" rows="4" class="form-control">${fn:escapeXml(form.content)}</textarea>
    </div>
    <div class="col-md-6">
        <label class="form-label small mb-0">Ảnh (đường dẫn)</label>
        <input name="imageUrl" class="form-control" value="${fn:escapeXml(form.imageUrl)}" placeholder="https://...">
    </div>
    <div class="col-md-6">
        <label class="form-label small mb-0">hoặc tải ảnh lên</label>
        <input type="file" name="imageFile" accept="image/*" class="form-control">
    </div>
    <div class="col-12 d-flex align-items-center gap-3">
        <div class="form-check">
            <input class="form-check-input" type="checkbox" name="active" value="true" id="pact" <c:if test="${form.id == null || form.active}">checked</c:if>>
            <label class="form-check-label" for="pact">Hiển thị</label>
        </div>
        <button class="btn btn-warning">${fn:escapeXml(form.id != null ? 'Cập nhật' : 'Đăng bài')}</button>
        <c:if test="${form.id != null}"><a href="${pageContext.request.contextPath}/admin/promotions" class="btn btn-outline-secondary">Huỷ sửa</a></c:if>
    </div>
</form>

<table class="table table-bordered bg-white align-middle">
    <thead class="table-dark"><tr><th style="width:90px">Ảnh</th><th>Tiêu đề</th><th style="width:90px">Lượt xem</th><th class="text-center" style="width:120px">Hiển thị</th><th style="width:170px"></th></tr></thead>
    <tbody>
    <c:forEach items="${promotions}" var="p">
        <tr>
            <td><c:if test="${not empty p.imageUrl}"><img src="${fn:escapeXml(p.imageUrl)}" style="width:70px;height:50px;object-fit:cover" class="rounded"></c:if></td>
            <td><b>${fn:escapeXml(p.title)}</b><div class="small text-muted">${fn:escapeXml(p.createdAtFormatted)}</div></td>
            <td>${fn:escapeXml(p.views)}</td>
            <td class="text-center">
                <form action="${pageContext.request.contextPath}/admin/promotions/toggle" method="post">
                    <input type="hidden" name="id" value="${fn:escapeXml(p.id)}">
                    <button class="badge border-0 ${fn:escapeXml(p.active ? 'bg-success' : 'bg-secondary')}">${fn:escapeXml(p.active ? 'Đang hiện' : 'Đã ẩn')}</button>
                </form>
            </td>
            <td>
                <a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/admin/promotions?edit=${fn:escapeXml(p.id)}"><i class="fas fa-edit"></i> Sửa</a>
                <form action="${pageContext.request.contextPath}/admin/promotions/delete" method="post" style="display:inline" onsubmit="return confirm('Xoá bài viết này?')">
                    <input type="hidden" name="id" value="${fn:escapeXml(p.id)}">
                    <button class="btn btn-sm btn-danger"><i class="fas fa-trash"></i></button>
                </form>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty promotions}"><tr><td colspan="5" class="text-center text-muted py-4">Chưa có bài viết nào</td></tr></c:if>
    </tbody>
</table>
