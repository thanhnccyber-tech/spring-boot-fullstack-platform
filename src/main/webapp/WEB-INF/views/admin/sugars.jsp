<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Quản lý Mức Đường</title>

<c:if test="${success != null}">
    <div class="alert alert-success alert-dismissible fade show">
        ${fn:escapeXml(success)}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>
<c:if test="${error != null}">
    <div class="alert alert-danger alert-dismissible fade show">
        ${fn:escapeXml(error)}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
    <div class="text-muted">
        <i class="fas fa-cubes-stacked me-1"></i>
        Tổng cộng <b class="text-dark">${fn:length(sugars)}</b> mức đường
    </div>
    <a href="${pageContext.request.contextPath}/admin/sugars/new" class="btn btn-warning">
        <i class="fas fa-plus me-1"></i> Thêm mức đường
    </a>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-dark">
                <tr>
                    <th style="width:80px">ID</th>
                    <th>Tên</th>
                    <th class="text-center" style="width:150px">Phần trăm</th>
                    <th class="text-center" style="width:100px">Thứ tự</th>
                    <th class="text-center" style="width:140px">Trạng thái</th>
                    <th class="text-end" style="width:140px">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${sugars}" var="s">
                    <tr>
                        <td class="text-muted small">#${fn:escapeXml(s.id)}</td>
                        <td class="fw-semibold">${fn:escapeXml(s.name)}</td>
                        <td class="text-center">
                            <span class="badge bg-info text-dark">${fn:escapeXml(s.percent)}%</span>
                        </td>
                        <td class="text-center">${fn:escapeXml(s.sortOrder)}</td>
                        <td class="text-center">
                            <span class="badge px-3 py-2 ${s.active ? 'bg-success' : 'bg-secondary'}">
                                ${s.active ? 'Đang dùng' : 'Đã khóa'}
                            </span>
                        </td>
                        <td class="text-end">
                            <div class="d-inline-flex gap-1">
                                <a href="${pageContext.request.contextPath}/admin/sugars/edit/${fn:escapeXml(s.id)}"
                                   class="btn btn-sm btn-outline-primary" title="Sửa mức đường">
                                    <i class="fas fa-pen"></i>
                                </a>
                                <c:choose>
                                    <c:when test="${s.active}">
                                        <a href="${pageContext.request.contextPath}/admin/sugars/delete/${fn:escapeXml(s.id)}"
                                           class="btn btn-sm btn-outline-danger" title="Khóa"
                                           onclick="return confirm('Khóa mức đường này?')">
                                            <i class="fas fa-lock"></i>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="${pageContext.request.contextPath}/admin/sugars/restore/${fn:escapeXml(s.id)}"
                                           class="btn btn-sm btn-outline-success" title="Mở khóa">
                                            <i class="fas fa-unlock"></i>
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty sugars}">
                    <tr>
                        <td colspan="6" class="text-center text-muted py-5">
                            <i class="fas fa-cubes-stacked fa-2x d-block mb-2 opacity-50"></i>
                            Chưa có mức đường nào
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>