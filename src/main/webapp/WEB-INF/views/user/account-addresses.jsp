<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<title>Số địa chỉ</title>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="small mb-3"><a href="${ctx}/">Trang chủ</a> / <a href="${ctx}/profile">Tài khoản</a> / Số địa chỉ</nav>
<div class="row g-4">
    <div class="col-lg-4 col-xl-3">
        <jsp:include page="/WEB-INF/views/user/_account-menu.jsp"><jsp:param name="active" value="addresses"/></jsp:include>
    </div>
    <div class="col-lg-8 col-xl-9">

        <div class="checkout-box mb-3">
            <h4 class="mb-3" style="color:#8a6d00;font-weight:700">Số địa chỉ</h4>
            <c:forEach items="${addresses}" var="a">
                <div class="d-flex justify-content-between align-items-start gap-2 py-3 border-bottom">
                    <div>
                        <b>${fn:escapeXml(a.label)}</b>
                        <c:if test="${a.defaultAddress}"><span class="badge bg-success ms-1">Mặc định</span></c:if>
                        <div class="text-muted">${fn:escapeXml(a.address)}</div>
                    </div>
                    <div class="d-flex gap-1 flex-shrink-0">
                        <c:if test="${not a.defaultAddress}">
                            <form action="${ctx}/account/addresses/default" method="post"><input type="hidden" name="id" value="${fn:escapeXml(a.id)}"><button class="btn btn-sm btn-outline-dark">Đặt mặc định</button></form>
                        </c:if>
                        <form action="${ctx}/account/addresses/delete" method="post" onsubmit="return confirm('Xoá địa chỉ này?')"><input type="hidden" name="id" value="${fn:escapeXml(a.id)}"><button class="btn btn-sm btn-outline-danger"><i class="fas fa-trash"></i></button></form>
                    </div>
                </div>
            </c:forEach>
            <c:if test="${empty addresses}"><p class="text-center text-muted my-4">Không có dữ liệu</p></c:if>
        </div>
        <div class="checkout-box">
            <h5>Thêm địa chỉ mới</h5>
            <form action="${ctx}/account/addresses/add" method="post" class="row g-2">
                <div class="col-md-3"><input name="label" class="form-control" maxlength="50" required placeholder="Tên gợi nhớ (Nhà, Công ty...)"></div>
                <div class="col-md-9"><input name="address" class="form-control" maxlength="500" required placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành"></div>
                <div class="col-12 d-flex align-items-center gap-3">
                    <div class="form-check"><input class="form-check-input" type="checkbox" name="makeDefault" value="true" id="mk"><label class="form-check-label" for="mk">Đặt làm mặc định</label></div>
                    <button class="btn btn-warning fw-bold">Lưu địa chỉ</button>
                </div>
            </form>
        </div>
    </div>
</div>
