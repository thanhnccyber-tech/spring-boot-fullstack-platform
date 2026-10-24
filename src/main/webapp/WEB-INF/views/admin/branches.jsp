<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Chi nhánh</title>

<c:if test="${success != null}">
    <div class="alert alert-success alert-dismissible fade show">
        ${fn:escapeXml(success)}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>
<c:if test="${error != null}">
    <div class="alert alert-danger alert-dismissible fade show">
        ${fn:escapeXml(error)}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">🏪 Quản lý chi nhánh</h5>
    <a href="${pageContext.request.contextPath}/admin/branches/new"
       class="btn btn-warning">
        <i class="fas fa-plus"></i> Thêm chi nhánh
    </a>
</div>

<form class="row g-2 mb-3 bg-white p-3 rounded shadow-sm" method="get">
    <div class="col-md-5">
        <input type="text" name="kw" value="${fn:escapeXml(kw)}" class="form-control"
               placeholder="Tìm theo tên, địa chỉ hoặc SĐT...">
    </div>
    <div class="col-md-3">
        <select name="active" class="form-select">
            <option value="">-- Trạng thái --</option>
            <option value="true"  <c:if test="${activeFilter == 'true'}">selected</c:if>>Đang hoạt động</option>
            <option value="false" <c:if test="${activeFilter == 'false'}">selected</c:if>>Đã khóa</option>
        </select>
    </div>
    <div class="col-md-2">
        <button class="btn btn-warning w-100"><i class="fas fa-search"></i> Tìm</button>
    </div>
    <div class="col-md-2">
        <a href="${pageContext.request.contextPath}/admin/branches"
           class="btn btn-outline-secondary w-100">
            <i class="fas fa-redo"></i> Xóa lọc
        </a>
    </div>
</form>

<table class="table table-bordered bg-white align-middle">
    <thead class="table-dark">
        <tr>
            <th style="width:60px">ID</th>
            <th>Tên</th>
            <th>Địa chỉ</th>
            <th style="width:140px">Điện thoại</th>
            <th class="text-center" style="width:140px">Trạng thái</th>
            <th style="width:180px">Hành động</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${pageData.content}" var="b">
            <tr>
                <td>${fn:escapeXml(b.id)}</td>
                <td><b>${fn:escapeXml(b.name)}</b></td>
                <td>${fn:escapeXml(b.address)}</td>
                <td>${fn:escapeXml(b.phone)}</td>
                <td class="text-center">
                    <form action="${pageContext.request.contextPath}/admin/branches/toggle-active"
                          method="post" style="display:inline">
                        <input type="hidden" name="id" value="${fn:escapeXml(b.id)}">
                        <input type="hidden" name="returnUrl"
                               value="/admin/branches?page=${fn:escapeXml(pageData.number)}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}">
                        <button class="badge ${fn:escapeXml(b.active ? 'bg-success' : 'bg-secondary')} border-0"
                                title="${fn:escapeXml(b.active ? 'Bấm để khóa' : 'Bấm để mở khóa')}">
                            <i class="fas ${fn:escapeXml(b.active ? 'fa-check-circle' : 'fa-lock')}"></i>
                            ${fn:escapeXml(b.active ? 'Đang hoạt động' : 'Đã khóa')}
                        </button>
                    </form>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/branches/edit/${fn:escapeXml(b.id)}"
                       class="btn btn-sm btn-primary">
                        <i class="fas fa-edit"></i> Sửa
                    </a>
                    <c:choose>
                        <c:when test="${b.active}">
                            <form action="${pageContext.request.contextPath}/admin/branches/delete"
                                  method="post" style="display:inline"
                                  onsubmit="return confirm('Khóa chi nhánh này?')">
                                <input type="hidden" name="id" value="${fn:escapeXml(b.id)}">
                                <button class="btn btn-sm btn-danger">
                                    <i class="fas fa-lock"></i> Khóa
                                </button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <form action="${pageContext.request.contextPath}/admin/branches/restore"
                                  method="post" style="display:inline"
                                  onsubmit="return confirm('Mở khóa chi nhánh này?')">
                                <input type="hidden" name="id" value="${fn:escapeXml(b.id)}">
                                <button class="btn btn-sm btn-success">
                                    <i class="fas fa-unlock"></i> Mở khóa
                                </button>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty pageData.content}">
            <tr><td colspan="6" class="text-center text-muted py-4">
                Không có chi nhánh nào khớp điều kiện
            </td></tr>
        </c:if>
    </tbody>
</table>

<c:if test="${pageData.totalPages > 1}">
    <nav>
        <ul class="pagination justify-content-center">
            <li class="page-item ${fn:escapeXml(pageData.first ? 'disabled' : '')}">
                <a class="page-link" href="?page=${fn:escapeXml(pageData.number - 1)}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}">«</a>
            </li>
            <c:set var="startPage" value="${pageData.number - 2}"/>
            <c:if test="${startPage < 0}"><c:set var="startPage" value="0"/></c:if>
            <c:set var="endPage" value="${startPage + 4}"/>
            <c:if test="${endPage >= pageData.totalPages}">
                <c:set var="endPage" value="${pageData.totalPages - 1}"/>
                <c:set var="startPage" value="${endPage - 4}"/>
                <c:if test="${startPage < 0}"><c:set var="startPage" value="0"/></c:if>
            </c:if>
            <c:forEach begin="${startPage}" end="${endPage}" var="i">
                <li class="page-item ${fn:escapeXml(i == pageData.number ? 'active' : '')}">
                    <a class="page-link" href="?page=${fn:escapeXml(i)}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}">${fn:escapeXml(i + 1)}</a>
                </li>
            </c:forEach>
            <li class="page-item ${fn:escapeXml(pageData.last ? 'disabled' : '')}">
                <a class="page-link" href="?page=${fn:escapeXml(pageData.number + 1)}&kw=${fn:escapeXml(kw)}&active=${fn:escapeXml(activeFilter)}">»</a>
            </li>
        </ul>
    </nav>
</c:if>