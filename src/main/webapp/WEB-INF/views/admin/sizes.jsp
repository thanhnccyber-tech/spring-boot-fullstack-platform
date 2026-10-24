<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Quản lý Size</title>

<c:if test="${success != null}"><div class="alert alert-success">${fn:escapeXml(success)}</div></c:if>
<c:if test="${error != null}"><div class="alert alert-danger">${fn:escapeXml(error)}</div></c:if>

<a href="${pageContext.request.contextPath}/admin/sizes/new" class="btn btn-warning mb-3">
    <i class="fas fa-plus"></i> Thêm Size
</a>

<table class="table bg-white align-middle">
    <thead class="table-dark">
        <tr><th>ID</th><th>Tên</th><th>Phụ thu</th><th>Thứ tự</th><th>Trạng thái</th><th></th></tr>
    </thead>
    <tbody>
        <c:forEach items="${sizes}" var="s">
            <tr>
                <td>${fn:escapeXml(s.id)}</td>
                <td>${fn:escapeXml(s.name)}</td>
                <td><fmt:formatNumber value="${s.extraPrice}" pattern="#,##0"/> ₫</td>
                <td>${fn:escapeXml(s.sortOrder)}</td>
                <td>
                    <c:choose>
                        <c:when test="${s.active}"><span class="badge bg-success">Đang dùng</span></c:when>
                        <c:otherwise><span class="badge bg-secondary">Đã khóa</span></c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/sizes/edit/${fn:escapeXml(s.id)}" class="btn btn-sm btn-primary">Sửa</a>
                    <c:choose>
                        <c:when test="${s.active}">
                            <a href="${pageContext.request.contextPath}/admin/sizes/delete/${fn:escapeXml(s.id)}" class="btn btn-sm btn-danger"
                               onclick="return confirm('Khóa size này?')">Khóa</a>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/admin/sizes/restore/${fn:escapeXml(s.id)}" class="btn btn-sm btn-success">Mở</a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>