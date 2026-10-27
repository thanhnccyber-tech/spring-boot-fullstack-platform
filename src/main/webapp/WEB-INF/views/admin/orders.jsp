<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Đơn hàng</title>

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

<%-- Quick filter theo trạng thái (dạng pill) --%>
<c:set var="baseQ" value="kw=${fn:escapeXml(kw)}&fromDate=${fn:escapeXml(fromDate)}&toDate=${fn:escapeXml(toDate)}"/>
<div class="d-flex flex-wrap gap-2 mb-3">
    <a href="${pageContext.request.contextPath}/admin/orders?${baseQ}"
       class="btn btn-sm ${empty statusFilter ? 'btn-dark' : 'btn-outline-dark'}">
        <i class="fas fa-list me-1"></i> Tất cả <span class="badge bg-light text-dark ms-1">${fn:escapeXml(statusCount['ALL'] != null ? statusCount['ALL'] : 0)}</span>
    </a>
    <a href="${pageContext.request.contextPath}/admin/orders?status=PENDING&${baseQ}"
       class="btn btn-sm ${statusFilter == 'PENDING' ? 'btn-warning' : 'btn-outline-warning'}">
        <i class="fas fa-hourglass-half me-1"></i> Chờ xác nhận <span class="badge bg-light text-dark ms-1">${fn:escapeXml(statusCount['PENDING'] != null ? statusCount['PENDING'] : 0)}</span>
    </a>
    <a href="${pageContext.request.contextPath}/admin/orders?status=PREPARING&${baseQ}"
       class="btn btn-sm ${statusFilter == 'PREPARING' ? 'btn-info' : 'btn-outline-info'}">
        <i class="fas fa-mug-hot me-1"></i> Pha chế <span class="badge bg-light text-dark ms-1">${fn:escapeXml(statusCount['PREPARING'] != null ? statusCount['PREPARING'] : 0)}</span>
    </a>
    <a href="${pageContext.request.contextPath}/admin/orders?status=READY&${baseQ}"
       class="btn btn-sm ${statusFilter == 'READY' ? 'btn-warning' : 'btn-outline-warning'}">
        <i class="fas fa-rotate me-1"></i> Chờ giao lại <span class="badge bg-light text-dark ms-1">${fn:escapeXml(statusCount['READY'] != null ? statusCount['READY'] : 0)}</span>
    </a>
    <a href="${pageContext.request.contextPath}/admin/orders?status=DELIVERING&${baseQ}"
       class="btn btn-sm ${statusFilter == 'DELIVERING' ? 'btn-primary' : 'btn-outline-primary'}">
        <i class="fas fa-motorcycle me-1"></i> Đang giao <span class="badge bg-light text-dark ms-1">${fn:escapeXml(statusCount['DELIVERING'] != null ? statusCount['DELIVERING'] : 0)}</span>
    </a>
    <a href="${pageContext.request.contextPath}/admin/orders?status=COMPLETED&${baseQ}"
       class="btn btn-sm ${statusFilter == 'COMPLETED' ? 'btn-success' : 'btn-outline-success'}">
        <i class="fas fa-circle-check me-1"></i> Hoàn thành <span class="badge bg-light text-dark ms-1">${fn:escapeXml(statusCount['COMPLETED'] != null ? statusCount['COMPLETED'] : 0)}</span>
    </a>
    <a href="${pageContext.request.contextPath}/admin/orders?status=FAILED&${baseQ}"
       class="btn btn-sm ${statusFilter == 'FAILED' ? 'btn-secondary' : 'btn-outline-secondary'}">
        <i class="fas fa-circle-xmark me-1"></i> Thất bại <span class="badge bg-light text-dark ms-1">${fn:escapeXml(statusCount['FAILED'] != null ? statusCount['FAILED'] : 0)}</span>
    </a>
    <a href="${pageContext.request.contextPath}/admin/orders?status=CANCELLED&${baseQ}"
       class="btn btn-sm ${statusFilter == 'CANCELLED' ? 'btn-danger' : 'btn-outline-danger'}">
        <i class="fas fa-ban me-1"></i> Đã hủy <span class="badge bg-light text-dark ms-1">${fn:escapeXml(statusCount['CANCELLED'] != null ? statusCount['CANCELLED'] : 0)}</span>
    </a>
</div>

<%-- Bộ lọc chi tiết --%>
<div class="card mb-3">
    <div class="card-body py-3">
        <form method="get" class="row g-2 align-items-end">
            <input type="hidden" name="status" value="${fn:escapeXml(statusFilter)}">
            <div class="col-md-5">
                <label class="form-label small fw-semibold text-muted mb-1">Tìm kiếm</label>
                <input type="text" name="kw" value="${fn:escapeXml(kw)}" class="form-control"
                       placeholder="Mã đơn / tên khách / SĐT / email...">
            </div>
            <div class="col-md-3">
                <label class="form-label small fw-semibold text-muted mb-1">Từ ngày</label>
                <input type="date" name="fromDate" value="${fn:escapeXml(fromDate)}" class="form-control">
            </div>
            <div class="col-md-3">
                <label class="form-label small fw-semibold text-muted mb-1">Đến ngày</label>
                <input type="date" name="toDate" value="${fn:escapeXml(toDate)}" class="form-control">
            </div>
            <div class="col-md-1">
                <button class="btn btn-warning w-100" title="Tìm"><i class="fas fa-search"></i></button>
            </div>
        </form>
        <c:if test="${not empty kw or not empty fromDate or not empty toDate or not empty statusFilter}">
            <a href="${pageContext.request.contextPath}/admin/orders" class="small text-decoration-none d-inline-block mt-2">
                <i class="fas fa-times"></i> Xóa bộ lọc
            </a>
        </c:if>
    </div>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-dark">
                <tr>
                    <th style="width:140px">Mã đơn</th>
                    <th>Khách hàng</th>
                    <th style="width:140px">Chi nhánh</th>
                    <th style="width:120px">Shipper</th>
                    <th class="text-end" style="width:130px">Tổng tiền</th>
                    <th class="text-center" style="width:150px">Trạng thái</th>
                    <th style="width:110px">Ngày đặt</th>
                    <th class="text-end" style="width:150px">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${pageData.content}" var="o">
                    <tr>
                        <%-- 1. Mã đơn hàng trở thành Link để xem chi tiết --%>
                        <td>
                            <a href="${pageContext.request.contextPath}/admin/orders/detail/${fn:escapeXml(o.id)}"
                               class="fw-bold text-decoration-none" title="Xem chi tiết đơn hàng">
                                #${fn:escapeXml(o.orderCode)}
                            </a>
                        </td>
                        <td>
                            <div class="fw-semibold">${fn:escapeXml(o.user.fullName)}</div>
                            <div class="small text-muted">${fn:escapeXml(o.user.phone)}</div>
                        </td>
                        <td class="small">${fn:escapeXml(o.branch.name)}</td>
                        <td class="small">
                            <c:choose>
                                <c:when test="${o.shipper != null}">${fn:escapeXml(o.shipper.fullName)}</c:when>
                                <c:otherwise><span class="text-muted">—</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-end fw-semibold text-success">
                            <fmt:formatNumber value="${o.totalAmount}" pattern="#,##0"/> ₫
                        </td>
                        <td class="text-center">
                            <c:choose>
                                <c:when test="${o.status == 'PENDING'}"><span class="badge bg-warning text-dark px-2 py-1">Chờ xác nhận</span></c:when>
                                <c:when test="${o.status == 'PREPARING'}"><span class="badge bg-info text-dark px-2 py-1">Đang pha chế</span></c:when>
                                <c:when test="${o.status == 'READY'}"><span class="badge bg-warning text-dark px-2 py-1">Chờ giao lại</span></c:when>
                                <c:when test="${o.status == 'DELIVERING'}"><span class="badge bg-primary px-2 py-1">Đang giao</span></c:when>
                                <c:when test="${o.status == 'COMPLETED'}"><span class="badge bg-success px-2 py-1">Hoàn thành</span></c:when>
                                <c:when test="${o.status == 'FAILED'}"><span class="badge bg-secondary px-2 py-1">Giao thất bại</span></c:when>
                                <c:when test="${o.status == 'CANCELLED'}"><span class="badge bg-danger px-2 py-1">Đã hủy</span></c:when>
                                <c:otherwise><span class="badge bg-light text-dark">${fn:escapeXml(o.status)}</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td class="small text-muted">${fn:escapeXml(o.createdAtShort)}</td>
                        
                        <%-- 2. Cột Thao tác: 1 Nút chính + 1 Menu Dropdown --%>
                        <td class="text-end">
                            <div class="d-flex justify-content-end align-items-center gap-1">
                                
                                <%-- Nút Hành động chính (Primary Action) --%>
                                <c:choose>
                                    <c:when test="${o.status == 'PENDING'}">
                                        <form action="${pageContext.request.contextPath}/admin/orders/quick-status" method="post" class="m-0">
                                            <input type="hidden" name="id" value="${fn:escapeXml(o.id)}">
                                            <input type="hidden" name="status" value="PREPARING">
                                            <input type="hidden" name="returnUrl" value="/admin/orders?kw=${fn:escapeXml(kw)}&status=${fn:escapeXml(statusFilter)}&fromDate=${fn:escapeXml(fromDate)}&toDate=${fn:escapeXml(toDate)}&page=${fn:escapeXml(pageData.number)}">
                                            <button class="btn btn-sm btn-outline-info fw-semibold" title="Bắt đầu pha chế">
                                                <i class="fas fa-mug-hot me-1"></i> Pha chế
                                            </button>
                                        </form>
                                    </c:when>
                                    <c:when test="${o.status == 'PREPARING'}">
                                        <form action="${pageContext.request.contextPath}/admin/orders/quick-status" method="post" class="m-0">
                                            <input type="hidden" name="id" value="${fn:escapeXml(o.id)}">
                                            <input type="hidden" name="status" value="DELIVERING">
                                            <input type="hidden" name="returnUrl" value="/admin/orders?kw=${fn:escapeXml(kw)}&status=${fn:escapeXml(statusFilter)}&fromDate=${fn:escapeXml(fromDate)}&toDate=${fn:escapeXml(toDate)}&page=${fn:escapeXml(pageData.number)}">
                                            <button class="btn btn-sm btn-outline-primary fw-semibold" title="Bắt đầu giao">
                                                <i class="fas fa-motorcycle me-1"></i> Giao
                                            </button>
                                        </form>
                                    </c:when>
                                    <c:when test="${o.status == 'DELIVERING'}">
                                        <form action="${pageContext.request.contextPath}/admin/orders/quick-status" method="post" class="m-0">
                                            <input type="hidden" name="id" value="${fn:escapeXml(o.id)}">
                                            <input type="hidden" name="status" value="COMPLETED">
                                            <input type="hidden" name="returnUrl" value="/admin/orders?kw=${fn:escapeXml(kw)}&status=${fn:escapeXml(statusFilter)}&fromDate=${fn:escapeXml(fromDate)}&toDate=${fn:escapeXml(toDate)}&page=${fn:escapeXml(pageData.number)}">
                                            <button class="btn btn-sm btn-outline-success fw-semibold" title="Xác nhận đã giao">
                                                <i class="fas fa-check me-1"></i> Xong
                                            </button>
                                        </form>
                                    </c:when>
                                    <c:when test="${o.status == 'FAILED'}">
                                        <form action="${pageContext.request.contextPath}/admin/orders/quick-status" method="post" class="m-0">
                                            <input type="hidden" name="id" value="${fn:escapeXml(o.id)}">
                                            <input type="hidden" name="status" value="READY">
                                            <input type="hidden" name="returnUrl" value="/admin/orders?kw=${fn:escapeXml(kw)}&status=${fn:escapeXml(statusFilter)}&fromDate=${fn:escapeXml(fromDate)}&toDate=${fn:escapeXml(toDate)}&page=${fn:escapeXml(pageData.number)}">
                                            <button class="btn btn-sm btn-outline-warning fw-semibold" title="Giao lại đơn">
                                                <i class="fas fa-redo me-1"></i> Giao lại
                                            </button>
                                        </form>
                                    </c:when>
                                </c:choose>

                                <%-- Menu Dropdown cho các hành động phụ / nguy hiểm --%>
                                <div class="dropdown">
                                    <button class="btn btn-sm btn-light border dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false" title="Thêm thao tác">
                                        <i class="fas fa-ellipsis-v"></i>
                                    </button>
                                    <ul class="dropdown-menu dropdown-menu-end shadow-sm" style="font-size: 0.9rem;">
                                        <li>
                                            <a class="dropdown-item" href="${pageContext.request.contextPath}/admin/orders/detail/${fn:escapeXml(o.id)}">
                                                <i class="fas fa-eye text-muted me-2"></i> Xem chi tiết
                                            </a>
                                        </li>
                                        
                                        <%-- Hủy đơn: Chỉ hiện khi Pending --%>
                                        <c:if test="${o.status == 'PENDING'}">
                                            <li><hr class="dropdown-divider"></li>
                                            <li>
                                                <form action="${pageContext.request.contextPath}/admin/orders/quick-status" method="post"
                                                      onsubmit="return confirm('CẢNH BÁO: Bạn có chắc chắn muốn HỦY đơn hàng này?')">
                                                    <input type="hidden" name="id" value="${fn:escapeXml(o.id)}">
                                                    <input type="hidden" name="status" value="CANCELLED">
                                                    <input type="hidden" name="returnUrl" value="/admin/orders?kw=${fn:escapeXml(kw)}&status=${fn:escapeXml(statusFilter)}&fromDate=${fn:escapeXml(fromDate)}&toDate=${fn:escapeXml(toDate)}&page=${fn:escapeXml(pageData.number)}">
                                                    <button type="submit" class="dropdown-item text-danger fw-semibold">
                                                        <i class="fas fa-ban me-2"></i> Hủy đơn hàng
                                                    </button>
                                                </form>
                                            </li>
                                        </c:if>

                                        <%-- Báo giao thất bại: Chỉ hiện khi Đang giao --%>
                                        <c:if test="${o.status == 'DELIVERING'}">
                                            <li><hr class="dropdown-divider"></li>
                                            <li>
                                                <form action="${pageContext.request.contextPath}/admin/orders/quick-status" method="post"
                                                      onsubmit="return confirm('Đánh dấu đơn hàng giao THẤT BẠI?')">
                                                    <input type="hidden" name="id" value="${fn:escapeXml(o.id)}">
                                                    <input type="hidden" name="status" value="FAILED">
                                                    <input type="hidden" name="returnUrl" value="/admin/orders?kw=${fn:escapeXml(kw)}&status=${fn:escapeXml(statusFilter)}&fromDate=${fn:escapeXml(fromDate)}&toDate=${fn:escapeXml(toDate)}&page=${fn:escapeXml(pageData.number)}">
                                                    <button type="submit" class="dropdown-item text-danger fw-semibold">
                                                        <i class="fas fa-times-circle me-2"></i> Báo giao thất bại
                                                    </button>
                                                </form>
                                            </li>
                                        </c:if>
                                    </ul>
                                </div>
                                
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty pageData.content}">
                    <tr>
                        <td colspan="8" class="text-center text-muted py-5">
                            <i class="fas fa-receipt fa-2x d-block mb-2 opacity-50"></i>
                            Không có đơn hàng nào khớp điều kiện
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<%-- Phân trang --%>
<c:if test="${pageData.totalPages > 1}">
    <nav class="mt-3">
        <ul class="pagination justify-content-center mb-0">
            <li class="page-item ${pageData.first ? 'disabled' : ''}">
                <a class="page-link" href="?page=${pageData.number - 1}&kw=${fn:escapeXml(kw)}&status=${fn:escapeXml(statusFilter)}&fromDate=${fn:escapeXml(fromDate)}&toDate=${fn:escapeXml(toDate)}">
                    <i class="fas fa-chevron-left"></i>
                </a>
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
                <li class="page-item ${i == pageData.number ? 'active' : ''}">
                    <a class="page-link" href="?page=${i}&kw=${fn:escapeXml(kw)}&status=${fn:escapeXml(statusFilter)}&fromDate=${fn:escapeXml(fromDate)}&toDate=${fn:escapeXml(toDate)}">${i + 1}</a>
                </li>
            </c:forEach>
            <li class="page-item ${pageData.last ? 'disabled' : ''}">
                <a class="page-link" href="?page=${pageData.number + 1}&kw=${fn:escapeXml(kw)}&status=${fn:escapeXml(statusFilter)}&fromDate=${fn:escapeXml(fromDate)}&toDate=${fn:escapeXml(toDate)}">
                    <i class="fas fa-chevron-right"></i>
                </a>
            </li>
        </ul>
    </nav>
</c:if>