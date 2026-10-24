<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<title>Đơn hàng</title>

<c:if test="${success != null}"><div class="alert alert-success">${success}</div></c:if>
<c:if test="${error != null}"><div class="alert alert-danger">${error}</div></c:if>

<%-- ========== THỐNG KÊ NHANH THEO TRẠNG THÁI ========== --%>
<c:set var="returnUrlBase" value="/admin/orders?kw=${kw}&amp;fromDate=${fromDate}&amp;toDate=${toDate}"/>

<div class="d-flex flex-wrap gap-2 mb-3">
    <a href="${pageContext.request.contextPath}/admin/orders?kw=${kw}&fromDate=${fromDate}&toDate=${toDate}"
       class="btn btn-sm ${empty statusFilter ? 'btn-dark' : 'btn-outline-dark'}">
        Tất cả (${statusCount['ALL'] != null ? statusCount['ALL'] : 0})
    </a>
    <a href="${pageContext.request.contextPath}/admin/orders?status=PENDING&kw=${kw}&fromDate=${fromDate}&toDate=${toDate}"
       class="btn btn-sm ${statusFilter == 'PENDING' ? 'btn-warning' : 'btn-outline-warning'}">
        Chờ xác nhận (${statusCount['PENDING'] != null ? statusCount['PENDING'] : 0})
    </a>
    <a href="${pageContext.request.contextPath}/admin/orders?status=PREPARING&kw=${kw}&fromDate=${fromDate}&toDate=${toDate}"
       class="btn btn-sm ${statusFilter == 'PREPARING' ? 'btn-info' : 'btn-outline-info'}">
        Đang pha chế (${statusCount['PREPARING'] != null ? statusCount['PREPARING'] : 0})
    </a>
    <a href="${pageContext.request.contextPath}/admin/orders?status=DELIVERING&kw=${kw}&fromDate=${fromDate}&toDate=${toDate}"
       class="btn btn-sm ${statusFilter == 'DELIVERING' ? 'btn-primary' : 'btn-outline-primary'}">
        Đang giao (${statusCount['DELIVERING'] != null ? statusCount['DELIVERING'] : 0})
    </a>
    <a href="${pageContext.request.contextPath}/admin/orders?status=COMPLETED&kw=${kw}&fromDate=${fromDate}&toDate=${toDate}"
       class="btn btn-sm ${statusFilter == 'COMPLETED' ? 'btn-success' : 'btn-outline-success'}">
        Hoàn thành (${statusCount['COMPLETED'] != null ? statusCount['COMPLETED'] : 0})
    </a>
    <a href="${pageContext.request.contextPath}/admin/orders?status=FAILED&kw=${kw}&fromDate=${fromDate}&toDate=${toDate}"
       class="btn btn-sm ${statusFilter == 'FAILED' ? 'btn-secondary' : 'btn-outline-secondary'}">
        Thất bại (${statusCount['FAILED'] != null ? statusCount['FAILED'] : 0})
    </a>
    <a href="${pageContext.request.contextPath}/admin/orders?status=CANCELLED&kw=${kw}&fromDate=${fromDate}&toDate=${toDate}"
       class="btn btn-sm ${statusFilter == 'CANCELLED' ? 'btn-danger' : 'btn-outline-danger'}">
        Đã hủy (${statusCount['CANCELLED'] != null ? statusCount['CANCELLED'] : 0})
    </a>
</div>

<%-- ========== FORM TÌM KIẾM ========== --%>
<form class="row g-2 mb-3 bg-white p-3 rounded shadow-sm" method="get">

    <input type="hidden" name="status" value="${statusFilter}">

    <div class="col-md-4">
        <input type="text" name="kw" value="${kw}" class="form-control"
               placeholder="Mã đơn / Tên khách / SĐT / Email...">
    </div>

    <div class="col-md-2">
        <input type="date" name="fromDate" value="${fromDate}" class="form-control">
    </div>
    <div class="col-md-2">
        <input type="date" name="toDate" value="${toDate}" class="form-control">
    </div>

    <div class="col-md-2">
        <button class="btn btn-warning w-100"><i class="fas fa-search"></i> Tìm</button>
    </div>
    <div class="col-md-2">
        <a href="${pageContext.request.contextPath}/admin/orders"
           class="btn btn-outline-secondary w-100"><i class="fas fa-redo"></i> Xóa lọc</a>
    </div>
</form>

<%-- ========== BẢNG ========== --%>
<table class="table table-bordered bg-white align-middle">
    <thead class="table-dark">
        <tr>
            <th>Mã</th>
            <th>Khách</th>
            <th>Chi nhánh</th>
            <th>Shipper</th>
            <th class="text-end">Tổng</th>
            <th class="text-center">Trạng thái</th>
            <th>Ngày</th>
            <th style="width:220px">Hành động</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${pageData.content}" var="o">
            <tr>
                <td><b>${o.orderCode}</b></td>
                <td>
                    <div>${o.user.fullName}</div>
                    <small class="text-muted">${o.user.phone}</small>
                </td>
                <td>${o.branch.name}</td>
                <td>
                    <c:choose>
                        <c:when test="${o.shipper != null}">${o.shipper.fullName}</c:when>
                        <c:otherwise><span class="text-muted small">Chưa giao</span></c:otherwise>
                    </c:choose>
                </td>
                <td class="text-end">
                    <fmt:formatNumber value="${o.totalAmount}" pattern="#,##0"/> ₫
                </td>
                <td class="text-center">
                    <c:choose>
                        <c:when test="${o.status == 'PENDING'}">
                            <span class="badge bg-warning text-dark">Chờ xác nhận</span>
                        </c:when>
                        <c:when test="${o.status == 'PREPARING'}">
                            <span class="badge bg-info text-dark">Đang pha chế</span>
                        </c:when>
                        <c:when test="${o.status == 'DELIVERING'}">
                            <span class="badge bg-primary">Đang giao</span>
                        </c:when>
                        <c:when test="${o.status == 'COMPLETED'}">
                            <span class="badge bg-success">Hoàn thành</span>
                        </c:when>
                        <c:when test="${o.status == 'FAILED'}">
                            <span class="badge bg-secondary">Thất bại</span>
                        </c:when>
                        <c:when test="${o.status == 'CANCELLED'}">
                            <span class="badge bg-danger">Đã hủy</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge bg-light text-dark">${o.status}</span>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td>${o.createdAtShort}</td>
                <td>
                    <div class="d-flex gap-1 flex-wrap">
                        <a href="${pageContext.request.contextPath}/admin/orders/detail/${o.id}"
                           class="btn btn-sm btn-primary">
                            <i class="fas fa-eye"></i>
                        </a>

                        <c:choose>
                            <c:when test="${o.status == 'PENDING'}">
                                <form action="${pageContext.request.contextPath}/admin/orders/quick-status"
                                      method="post" style="display:inline">
                                    <input type="hidden" name="id" value="${o.id}">
                                    <input type="hidden" name="status" value="PREPARING">
                                    <input type="hidden" name="returnUrl"
                                           value="/admin/orders?kw=${kw}&status=${statusFilter}&fromDate=${fromDate}&toDate=${toDate}&page=${pageData.number}">
                                    <button class="btn btn-sm btn-info text-dark" title="Bắt đầu pha chế">
                                        <i class="fas fa-mug-hot"></i> Pha chế
                                    </button>
                                </form>
                                <form action="${pageContext.request.contextPath}/admin/orders/quick-status"
                                      method="post" style="display:inline"
                                      onsubmit="return confirm('Hủy đơn này?')">
                                    <input type="hidden" name="id" value="${o.id}">
                                    <input type="hidden" name="status" value="CANCELLED">
                                    <input type="hidden" name="returnUrl"
                                           value="/admin/orders?kw=${kw}&status=${statusFilter}&fromDate=${fromDate}&toDate=${toDate}&page=${pageData.number}">
                                    <button class="btn btn-sm btn-outline-danger" title="Hủy đơn">
                                        <i class="fas fa-times"></i>
                                    </button>
                                </form>
                            </c:when>

                            <c:when test="${o.status == 'PREPARING'}">
                                <form action="${pageContext.request.contextPath}/admin/orders/quick-status"
                                      method="post" style="display:inline">
                                    <input type="hidden" name="id" value="${o.id}">
                                    <input type="hidden" name="status" value="DELIVERING">
                                    <input type="hidden" name="returnUrl"
                                           value="/admin/orders?kw=${kw}&status=${statusFilter}&fromDate=${fromDate}&toDate=${toDate}&page=${pageData.number}">
                                    <button class="btn btn-sm btn-primary" title="Bắt đầu giao">
                                        <i class="fas fa-motorcycle"></i> Giao
                                    </button>
                                </form>
                            </c:when>

                            <c:when test="${o.status == 'DELIVERING'}">
                                <form action="${pageContext.request.contextPath}/admin/orders/quick-status"
                                      method="post" style="display:inline">
                                    <input type="hidden" name="id" value="${o.id}">
                                    <input type="hidden" name="status" value="COMPLETED">
                                    <input type="hidden" name="returnUrl"
                                           value="/admin/orders?kw=${kw}&status=${statusFilter}&fromDate=${fromDate}&toDate=${toDate}&page=${pageData.number}">
                                    <button class="btn btn-sm btn-success" title="Giao thành công">
                                        <i class="fas fa-check"></i> Xong
                                    </button>
                                </form>
                                <form action="${pageContext.request.contextPath}/admin/orders/quick-status"
                                      method="post" style="display:inline"
                                      onsubmit="return confirm('Đánh dấu giao thất bại?')">
                                    <input type="hidden" name="id" value="${o.id}">
                                    <input type="hidden" name="status" value="FAILED">
                                    <input type="hidden" name="returnUrl"
                                           value="/admin/orders?kw=${kw}&status=${statusFilter}&fromDate=${fromDate}&toDate=${toDate}&page=${pageData.number}">
                                    <button class="btn btn-sm btn-outline-secondary" title="Giao thất bại">
                                        <i class="fas fa-times-circle"></i>
                                    </button>
                                </form>
                            </c:when>

                            <c:when test="${o.status == 'FAILED'}">
                                <form action="${pageContext.request.contextPath}/admin/orders/quick-status"
                                      method="post" style="display:inline">
                                    <input type="hidden" name="id" value="${o.id}">
                                    <input type="hidden" name="status" value="DELIVERING">
                                    <input type="hidden" name="returnUrl"
                                           value="/admin/orders?kw=${kw}&status=${statusFilter}&fromDate=${fromDate}&toDate=${toDate}&page=${pageData.number}">
                                    <button class="btn btn-sm btn-warning text-dark" title="Giao lại">
                                        <i class="fas fa-redo"></i> Giao lại
                                    </button>
                                </form>
                            </c:when>
                        </c:choose>
                    </div>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty pageData.content}">
            <tr><td colspan="8" class="text-center text-muted py-4">
                Không có đơn hàng nào khớp điều kiện
            </td></tr>
        </c:if>
    </tbody>
</table>

<%-- ========== PHÂN TRANG ========== --%>
<c:if test="${pageData.totalPages > 1}">
    <nav>
        <ul class="pagination justify-content-center">
            <li class="page-item ${pageData.first ? 'disabled' : ''}">
                <a class="page-link"
                   href="?page=${pageData.number - 1}&kw=${kw}&status=${statusFilter}&fromDate=${fromDate}&toDate=${toDate}">«</a>
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
                    <a class="page-link"
                       href="?page=${i}&kw=${kw}&status=${statusFilter}&fromDate=${fromDate}&toDate=${toDate}">${i + 1}</a>
                </li>
            </c:forEach>
            <li class="page-item ${pageData.last ? 'disabled' : ''}">
                <a class="page-link"
                   href="?page=${pageData.number + 1}&kw=${kw}&status=${statusFilter}&fromDate=${fromDate}&toDate=${toDate}">»</a>
            </li>
        </ul>
    </nav>
</c:if>