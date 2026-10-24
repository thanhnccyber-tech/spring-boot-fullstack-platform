<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>${fn:escapeXml(ice.id != null ? 'Sửa' : 'Thêm')} Mức Đá</title>

<c:if test="${error != null}"><div class="alert alert-danger">${fn:escapeXml(error)}</div></c:if>

<form action="${pageContext.request.contextPath}/admin/ices/save" method="post">
    <input type="hidden" name="id" value="${fn:escapeXml(ice.id)}">
    <div class="mb-3"><label>Tên *</label>
        <input type="text" name="name" value="${fn:escapeXml(ice.name)}" class="form-control" required maxlength="30"></div>
    <div class="mb-3"><label>Phần trăm (%) *</label>
        <input type="number" name="percent" value="${fn:escapeXml(ice.percent)}" class="form-control" required min="0" max="100"></div>
    <div class="mb-3"><label>Thứ tự</label>
        <input type="number" name="sortOrder" value="${fn:escapeXml(ice.sortOrder)}" class="form-control" min="0"></div>
    <div class="form-check mb-3">
        <input type="checkbox" name="active" value="true" class="form-check-input"
            <c:if test="${ice.active}">checked</c:if>>
        <label class="form-check-label">Đang dùng</label>
    </div>
    <button class="btn btn-warning">Lưu</button>
    <a href="${pageContext.request.contextPath}/admin/ices" class="btn btn-secondary">Hủy</a>
</form>