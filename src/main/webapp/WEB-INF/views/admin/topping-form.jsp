<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>${fn:escapeXml(isNew ? 'Thêm' : 'Sửa')} topping</title>

<c:if test="${error != null}"><div class="alert alert-danger">${fn:escapeXml(error)}</div></c:if>

<form action="${pageContext.request.contextPath}/admin/toppings/save" method="post">
    <input type="hidden" name="id" value="${fn:escapeXml(topping.id)}">

    <div class="mb-3">
        <label>Tên topping <span class="text-danger">*</span></label>
        <input type="text" name="name" value="${fn:escapeXml(topping.name)}"
               class="form-control" maxlength="100" required>
        <small class="text-muted">Tối đa 100 ký tự. Không được trùng với topping khác.</small>
    </div>

    <div class="mb-3">
        <label>Giá (VNĐ) <span class="text-danger">*</span></label>
        <input type="number" name="price" value="${fn:escapeXml(topping.price)}"
               class="form-control" min="1" max="10000000" step="1000" required>
        <small class="text-muted">Lớn hơn 0 và tối đa 10.000.000 ₫.</small>
    </div>

    <div class="form-check mb-2">
        <input type="checkbox" name="active" value="true"
               id="activeCheckbox" class="form-check-input"
               <c:if test="${topping.active}">checked</c:if>>
        <label class="form-check-label" for="activeCheckbox">Đang hiển thị</label>
    </div>

    <div class="form-check mb-3">
        <input type="checkbox" name="outOfStock" value="true"
               id="outOfStockCheckbox" class="form-check-input"
               <c:if test="${topping.outOfStock}">checked</c:if>>
        <label class="form-check-label text-danger" for="outOfStockCheckbox">
            Hết hàng (khách không thể chọn topping này)
        </label>
    </div>

    <button class="btn btn-warning">${fn:escapeXml(isNew ? 'Tạo mới' : 'Lưu')}</button>
    <a href="${pageContext.request.contextPath}/admin/toppings"
       class="btn btn-secondary">Hủy</a>
</form>