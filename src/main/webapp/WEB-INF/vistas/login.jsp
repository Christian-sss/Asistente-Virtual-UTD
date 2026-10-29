<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Ingreso del personal"/>
<%@ include file="fragmentos/cabecera.jspf" %>

<div class="container my-5" style="max-width: 440px">
    <div class="card tarjeta">
        <div class="card-body p-4">
            <div class="text-center mb-4">
                <span class="icono-circulo"><i class="bi bi-shield-lock"></i></span>
                <h1 class="h4 mt-2 titulo-seccion">Panel de administración</h1>
                <p class="small text-secondary mb-0">Acceso exclusivo para el personal de la UTD</p>
            </div>
            <c:if test="${param.salida == '1'}"><div class="alert alert-success small">Sesión cerrada correctamente.</div></c:if>
            <c:if test="${not empty error}"><div class="alert alert-danger small"><c:out value="${error}"/></div></c:if>
            <form method="post" action="${ctx}/login" autocomplete="off">
                <input type="hidden" name="_csrf" value="${csrf}">
                <div class="mb-3">
                    <label class="form-label" for="usuario">Usuario</label>
                    <input class="form-control" id="usuario" name="usuario" value="<c:out value='${usuarioIngresado}'/>" required autofocus>
                </div>
                <div class="mb-4">
                    <label class="form-label" for="contrasena">Contraseña</label>
                    <input class="form-control" type="password" id="contrasena" name="contrasena" required>
                </div>
                <button class="btn btn-primary w-100"><i class="bi bi-box-arrow-in-right"></i> Ingresar</button>
            </form>
        </div>
    </div>
</div>

<%@ include file="fragmentos/pie.jspf" %>
