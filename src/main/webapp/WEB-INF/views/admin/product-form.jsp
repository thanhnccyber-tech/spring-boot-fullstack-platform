<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<title>${product.id != null ? 'Sửa' : 'Thêm'} sản phẩm</title>

<form action="${pageContext.request.contextPath}/admin/products/save" method="post" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${product.id}">

    <div class="mb-3">
        <label>Tên sản phẩm</label>
        <input type="text" name="name" value="${product.name}" class="form-control" required>
    </div>
    <div class="mb-3">
        <label>Giá cơ bản</label>
        <input type="number" name="basePrice" value="${product.basePrice}" class="form-control" required step="1000">
    </div>
    <div class="mb-3">
        <label>Mô tả</label>
        <textarea name="description" class="form-control" rows="3">${product.description}</textarea>
    </div>
    <div class="mb-3">
        <label>Danh mục</label>
        <select name="categoryId" class="form-select">
            <c:forEach items="${categories}" var="c">
                <option value="${c.id}" <c:if test="${product.category != null && product.category.id == c.id}">selected</c:if>>
                    ${c.name}
                </option>
            </c:forEach>
        </select>
    </div>
    <div class="mb-3">
        <label>Topping áp dụng</label><br/>
        <c:forEach items="${toppings}" var="t">
            <div class="form-check form-check-inline">
                <input type="checkbox" name="toppingIds" value="${t.id}" class="form-check-input"
                    <c:if test="${product.toppings != null && product.toppings.contains(t)}">checked</c:if>>
                <label class="form-check-label">${t.name}</label>
            </div>
        </c:forEach>
    </div>
    <div class="mb-3">
        <label>Ảnh (Cloudinary)</label>
        <input type="file" name="imageFile" class="form-control" accept="image/*">
        <c:if test="${product.imageUrl != null}">
            <img src="${product.imageUrl}" class="mt-2" width="120">
        </c:if>
    </div>
    <div class="form-check mb-2">
        <input type="checkbox" name="featured" value="true" class="form-check-input"
               <c:if test="${product.featured}">checked</c:if>>
        <label class="form-check-label">Sản phẩm nổi bật</label>
    </div>
    <div class="form-check mb-2">
        <input type="checkbox" name="isNew" value="true" class="form-check-input"
               <c:if test="${product.isNew}">checked</c:if>>
        <label class="form-check-label">Sản phẩm mới</label>
    </div>
    <div class="form-check mb-3">
        <input type="checkbox" name="active" value="true" class="form-check-input"
               <c:if test="${product.active}">checked</c:if>>
        <label class="form-check-label">Đang bán</label>
    </div>
    <button class="btn btn-warning">Lưu</button>
    <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-secondary">Hủy</a>
</form>