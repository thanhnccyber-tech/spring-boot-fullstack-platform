<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"
         isErrorPage="true"
         session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Có lỗi xảy ra</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
</head>
<body class="bg-light">
<div class="container text-center py-5">
    <h1 class="display-1 fw-bold text-warning">${fn:escapeXml(status)}</h1>
    <h4 class="mb-3">
        <c:choose>
            <c:when test="${status == 403}">Bạn không có quyền truy cập trang này.</c:when>
            <c:when test="${status == 404}">Không tìm thấy trang hoặc dữ liệu bạn yêu cầu.</c:when>
            <c:when test="${status == 400}">Yêu cầu không hợp lệ.</c:when>
            <c:when test="${status == 413}">Tệp tải lên quá lớn.</c:when>
            <c:otherwise>Hệ thống đang gặp sự cố, vui lòng thử lại sau.</c:otherwise>
        </c:choose>
    </h4>
    <c:if test="${not empty userMessage}">
        <p class="text-muted"><c:out value="${userMessage}"/></p>
    </c:if>
    <a href="${pageContext.request.contextPath}/" class="btn btn-warning mt-3">Về trang chủ</a>
</div>
</body>
</html>
