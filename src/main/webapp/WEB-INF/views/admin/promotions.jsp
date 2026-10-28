<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Tin tức & Khuyến mãi</title>

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
        <i class="fas fa-bullhorn me-1"></i>
        Tổng cộng <b class="text-dark">${fn:length(promotions)}</b> bài viết
    </div>
    <c:if test="${isAdmin and form.id != null}">
        <a href="${pageContext.request.contextPath}/admin/promotions" class="btn btn-outline-secondary">
            <i class="fas fa-plus me-1"></i> Đăng bài mới
        </a>
    </c:if>
</div>

<c:if test="${isAdmin}">
    <div class="card mb-3">
        <div class="card-header bg-white">
            <b>${form.id != null ? 'Cập nhật bài viết' : 'Đăng bài viết mới'}</b>
        </div>
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/admin/promotions/save" method="post"
                  enctype="multipart/form-data" class="row g-2">
                <c:if test="${form.id != null}">
                    <input type="hidden" name="id" value="${fn:escapeXml(form.id)}">
                </c:if>
                <div class="col-md-8">
                    <label class="form-label small fw-semibold mb-1">Tiêu đề *</label>
                    <input name="title" class="form-control" required maxlength="200" value="${fn:escapeXml(form.title)}">
                </div>
                <div class="col-md-4">
                    <label class="form-label small fw-semibold mb-1">Mã giảm giá đính kèm (tuỳ chọn)</label>
                    <input name="voucherCode" class="form-control text-uppercase" maxlength="30"
                           value="${fn:escapeXml(form.voucherCode)}" placeholder="VD: WELCOME10">
                </div>
                <div class="col-12">
                    <label class="form-label small fw-semibold mb-1">Tóm tắt (hiện ở danh sách)</label>
                    <input name="summary" class="form-control" maxlength="500" value="${fn:escapeXml(form.summary)}">
                </div>
                <div class="col-12">
                    <label class="form-label small fw-semibold mb-1">Nội dung</label>
                    <textarea name="content" rows="4" class="form-control">${fn:escapeXml(form.content)}</textarea>
                </div>
                <div class="col-12">
                    <label class="form-label small fw-semibold mb-1">Tải ảnh lên</label>
                    <input type="file" name="imageFile" accept="image/*" class="form-control">
                    <small class="text-muted d-block mt-1">JPG, PNG, GIF, WEBP. Tối đa 5 MB.</small>
                    <c:if test="${form.id != null and not empty form.imageUrl}">
                        <small class="text-muted d-block">
                            Đang có ảnh — chọn tệp mới để thay thế, hoặc để trống để giữ nguyên ảnh hiện tại.
                        </small>
                    </c:if>
                </div>
                <div class="col-12 d-flex align-items-center gap-3 pt-2 border-top">
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" name="active" value="true" id="pact"
                               <c:if test="${form.id == null || form.active}">checked</c:if>>
                        <label class="form-check-label" for="pact">Hiển thị</label>
                    </div>
                    <button class="btn btn-warning">
                        <i class="fas fa-save me-1"></i> ${form.id != null ? 'Cập nhật' : 'Đăng bài'}
                    </button>
                    <c:if test="${form.id != null}">
                        <a href="${pageContext.request.contextPath}/admin/promotions" class="btn btn-outline-secondary">Hủy</a>
                    </c:if>
                </div>
            </form>
        </div>
    </div>
</c:if>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-dark">
                <tr>
                    <th style="width:90px">Ảnh</th>
                    <th>Tiêu đề</th>
                    <th class="text-center" style="width:120px">Lượt xem</th>
                    <th class="text-center" style="width:140px">Trạng thái</th>
                    <th class="text-end" style="width:150px">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${promotions}" var="p">
                    <tr>
                        <td>
                            <c:choose>
                                <c:when test="${not empty p.imageUrl}">
                                    <img src="${fn:escapeXml(p.imageUrl)}" class="rounded"
                                         style="width:70px;height:50px;object-fit:cover" alt="">
                                </c:when>
                                <c:otherwise>
                                    <div class="rounded bg-light d-flex align-items-center justify-content-center"
                                         style="width:70px;height:50px">
                                        <i class="fas fa-image text-muted"></i>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <div class="fw-semibold">${fn:escapeXml(p.title)}</div>
                            <div class="small text-muted">${fn:escapeXml(p.createdAtFormatted)}</div>
                        </td>
                        <td class="text-center">
                            <span class="badge bg-light text-dark border"><i class="far fa-eye me-1"></i>${fn:escapeXml(p.views)}</span>
                        </td>
                        <td class="text-center">
                            <c:choose>
                                <c:when test="${isAdmin}">
                                    <form action="${pageContext.request.contextPath}/admin/promotions/toggle" method="post">
                                        <input type="hidden" name="id" value="${fn:escapeXml(p.id)}">
                                        <button class="badge border-0 px-3 py-2 ${p.active ? 'bg-success' : 'bg-secondary'}"
                                                title="${p.active ? 'Bấm để ẩn' : 'Bấm để hiện'}">
                                            <i class="fas ${p.active ? 'fa-eye' : 'fa-eye-slash'} me-1"></i>${p.active ? 'Đang hiện' : 'Đã ẩn'}
                                        </button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge px-3 py-2 ${p.active ? 'bg-success' : 'bg-secondary'}">
                                        <i class="fas ${p.active ? 'fa-eye' : 'fa-eye-slash'} me-1"></i>${p.active ? 'Đang hiện' : 'Đã ẩn'}
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-end">
                            <c:if test="${isAdmin}">
                                <div class="d-inline-flex gap-1">
                                    <a href="${pageContext.request.contextPath}/admin/promotions?edit=${fn:escapeXml(p.id)}"
                                       class="btn btn-sm btn-outline-primary" title="Sửa bài viết">
                                        <i class="fas fa-pen"></i>
                                    </a>
                                    <form action="${pageContext.request.contextPath}/admin/promotions/delete" method="post"
                                          onsubmit="return confirm('Xoá bài viết này?')">
                                        <input type="hidden" name="id" value="${fn:escapeXml(p.id)}">
                                        <button class="btn btn-sm btn-outline-danger" title="Xoá bài viết">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    </form>
                                </div>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty promotions}">
                    <tr>
                        <td colspan="5" class="text-center text-muted py-5">
                            <i class="fas fa-bullhorn fa-2x d-block mb-2 opacity-50"></i>
                            Chưa có bài viết nào
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>