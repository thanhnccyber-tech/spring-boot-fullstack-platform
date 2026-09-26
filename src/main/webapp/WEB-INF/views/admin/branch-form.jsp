<%@ page contentType="text/html;charset=UTF-8" %>
<title>Chi nhánh</title>
<form action="${pageContext.request.contextPath}/admin/branches/save" method="post">
    <input type="hidden" name="id" value="${branch.id}">
    <div class="mb-3"><label>Tên</label><input type="text" name="name" value="${branch.name}" class="form-control" required></div>
    <div class="mb-3"><label>Địa chỉ</label><input type="text" name="address" value="${branch.address}" class="form-control"></div>
    <div class="mb-3"><label>Điện thoại</label><input type="text" name="phone" value="${branch.phone}" class="form-control"></div>
    <div class="form-check mb-3">
        <input type="checkbox" name="active" value="true" class="form-check-input" <c:if test="${branch.active}">checked</c:if>>
        <label class="form-check-label">Active</label>
    </div>
    <button class="btn btn-warning">Lưu</button>
</form>