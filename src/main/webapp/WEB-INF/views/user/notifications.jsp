<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<title>Thông báo</title>

<h3>🔔 Thông báo</h3>
<ul class="list-group">
    <c:forEach items="${notifications}" var="n">
        <li class="list-group-item">
            <div class="d-flex justify-content-between">
                <strong>${n.title}</strong>
                <small class="text-muted"><fmt:formatDate value="${n.createdAt}" pattern="dd/MM/yyyy HH:mm"/></small>
            </div>
            <p class="mb-0">${n.content}</p>
        </li>
    </c:forEach>
    <c:if test="${empty notifications}">
        <li class="list-group-item text-muted">Chưa có thông báo nào.</li>
    </c:if>
</ul>