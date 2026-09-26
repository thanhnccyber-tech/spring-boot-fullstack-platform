<%@ page contentType="text/html;charset=UTF-8" %>
<title>Topping</title>
<form action="${pageContext.request.contextPath}/admin/toppings/save" method="post">
    <input type="hidden" name="id" value="${topping.id}">
    <div class="mb-3"><label>Tên</label><input type="text" name="name" value="${topping.name}" class="form-control" required></div>
    <div class="mb-3"><label>Giá</label><input type="number" name="price" value="${topping.price}" class="form-control" required></div>
    <div class="form-check mb-3">
        <input type="checkbox" name="active" value="true" class="form-check-input" <c:if test="${topping.active}">checked</c:if>>
        <label class="form-check-label">Active</label>
    </div>
    <button class="btn btn-warning">Lưu</button>
</form>