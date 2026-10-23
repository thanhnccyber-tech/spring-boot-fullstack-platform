<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<title>Chi nhánh</title>

<c:if test="${success != null}">
    <div class="alert alert-success alert-dismissible fade show">
        ${success}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>
<c:if test="${error != null}">
    <div class="alert alert-danger alert-dismissible fade show">
        ${error}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<a href="${pageContext.request.contextPath}/admin/branches/new"
   class="btn btn-warning mb-3">
    <i class="fas fa-plus"></i> Thêm chi nhánh
</a>

<table class="table bg-white align-middle">
    <thead class="table-dark">
        <tr>
            <th>ID</th>
            <th>Tên</th>
            <th>Địa chỉ</th>
            <th>Điện thoại</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${branches}" var="b">
            <tr>
                <td>${b.id}</td>
                <td>${b.name}</td>
                <td>${b.address}</td>
                <td>${b.phone}</td>
                <td>
                    <c:choose>
                        <c:when test="${b.active}">
                            <span class="badge bg-success">Đang hoạt động</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge bg-secondary">Đã khóa</span>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/branches/edit/${b.id}"
                       class="btn btn-sm btn-primary">
                        <i class="fas fa-edit"></i> Sửa
                    </a>
                    <c:choose>
                        <c:when test="${b.active}">
                            <a href="${pageContext.request.contextPath}/admin/branches/delete/${b.id}"
                               class="btn btn-sm btn-danger"
                               onclick="return confirm('Khóa chi nhánh này?')">
                                <i class="fas fa-lock"></i> Khóa
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/admin/branches/restore/${b.id}"
                               class="btn btn-sm btn-success"
                               onclick="return confirm('Mở khóa chi nhánh này?')">
                                <i class="fas fa-unlock"></i> Mở khóa
                            </a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty branches}">
            <tr>
                <td colspan="6" class="text-center text-muted">
                    Chưa có chi nhánh nào
                </td>
            </tr>
        </c:if>
    </tbody>
</table>