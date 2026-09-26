<%@ page contentType="text/html;charset=UTF-8" %>
<title>${category.id != null ? 'Sửa' : 'Thêm'} danh mục</title>
<form action="${pageContext.request.contextPath}/admin/categories/save" method="post" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${category.id}">
    <div class="mb-3"><label>Tên</label><input type="text" name="name" value="${category.name}" class="form-control" required></div>
    <div class="mb-3"><label>Mô tả</label><textarea name="description" class="form-control">${category.description}</textarea></div>
    <div class="mb-3"><label>Ảnh</label><input type="file" name="imageFile" class="form-control" accept="image/*"></div>
    <div class="form-check mb-3">
        <input type="checkbox" name="active" value="true" class="form-check-input" <c:if test="${category.active}">checked</c:if>>
        <label class="form-check-label">Active</label>
    </div>
    <button class="btn btn-warning">Lưu</button>
</form>