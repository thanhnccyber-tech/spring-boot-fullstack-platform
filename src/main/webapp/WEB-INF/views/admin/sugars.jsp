<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
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
</div>

<div class="card mb-3">
    <div class="card-header bg-white">
        <b>${form.id != null ? 'Cập nhật mức đường' : 'Thêm mức đường mới'}</b>
    </div>
    <div class="card-body">
        <form action="${pageContext.request.contextPath}/admin/sugars/save" method="post" class="row g-2">
            <c:if test="${form.id != null}">
                <input type="hidden" name="id" value="${fn:escapeXml(form.id)}">
            </c:if>
            <div class="col-md-4">
                <label class="form-label small fw-semibold mb-1">Tên mức đường *</label>
                <input name="name" class="form-control" required maxlength="30"
                       value="${fn:escapeXml(form.name)}" placeholder="VD: 50%">
            </div>
            <div class="col-md-3">
                <label class="form-label small fw-semibold mb-1">Phần trăm (%) *</label>
                <input name="percent" type="number" min="0" max="200" class="form-control" required
                       value="${fn:escapeXml(form.percent != null ? form.percent : 50)}">
            </div>
            <div class="col-md-3 d-flex align-items-end">
                <div class="form-check pb-2">
                    <input class="form-check-input" type="checkbox" name="active" value="true" id="sgAct"
                           <c:if test="${form.id == null || form.active}">checked</c:if>>
                    <label class="form-check-label" for="sgAct">Đang dùng</label>
                </div>
            </div>
            <div class="col-md-2 d-flex align-items-end gap-1">
                <button class="btn btn-warning flex-grow-1">
                    <i class="fas fa-save me-1"></i> ${form.id != null ? 'Cập nhật' : 'Thêm'}
                </button>
                <c:if test="${form.id != null}">
                    <a href="${pageContext.request.contextPath}/admin/sugars" class="btn btn-outline-secondary" title="Hủy sửa">
                        <i class="fas fa-times"></i>
                    </a>
                </c:if>
            </div>
        </form>
    </div>
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
                    <th class="text-end" style="width:200px">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${sugars}" var="s" varStatus="st">
                    <tr class="${form.id == s.id ? 'table-warning' : ''}">
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
                            <div class="d-inline-flex gap-1 align-items-center">
                                <form action="${pageContext.request.contextPath}/admin/sugars/move-up/${fn:escapeXml(s.id)}"
                                      method="post" class="m-0">
                                    <button class="btn btn-sm btn-outline-secondary" title="Di chuyển lên"
                                            ${st.first ? 'disabled' : ''}>
                                        <i class="fas fa-arrow-up"></i>
                                    </button>
                                </form>
                                <form action="${pageContext.request.contextPath}/admin/sugars/move-down/${fn:escapeXml(s.id)}"
                                      method="post" class="m-0">
                                    <button class="btn btn-sm btn-outline-secondary" title="Di chuyển xuống"
                                            ${st.last ? 'disabled' : ''}>
                                        <i class="fas fa-arrow-down"></i>
                                    </button>
                                </form>
                                <a href="${pageContext.request.contextPath}/admin/sugars?edit=${fn:escapeXml(s.id)}"
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