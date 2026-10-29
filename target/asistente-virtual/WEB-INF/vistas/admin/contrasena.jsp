<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Cambiar contraseña"/>
<c:set var="menu" value="admin"/>
<%@ include file="../fragmentos/cabecera.jspf" %>
<%@ include file="../fragmentos/admin-menu.jspf" %>

<div class="container mt-4" style="max-width: 480px">
    <div class="card tarjeta">
        <div class="card-body">
            <h2 class="h6 titulo-seccion">Cambiar contraseña</h2>
            <form method="post" action="${ctx}/admin/contrasena">
                <input type="hidden" name="_csrf" value="${csrf}">
                <div class="mb-2"><label class="form-label small" for="actual">Contraseña actual</label>
                    <input class="form-control" type="password" id="actual" name="actual" required></div>
                <div class="mb-2"><label class="form-label small" for="nueva">Nueva contraseña</label>
                    <input class="form-control" type="password" id="nueva" name="nueva" minlength="8" required>
                    <div class="form-text">Mínimo 8 caracteres, con letras y números.</div></div>
                <div class="mb-3"><label class="form-label small" for="confirmacion">Confirmar nueva contraseña</label>
                    <input class="form-control" type="password" id="confirmacion" name="confirmacion" minlength="8" required></div>
                <button class="btn btn-primary"><i class="bi bi-key"></i> Actualizar</button>
            </form>
        </div>
    </div>
</div>

<%@ include file="../fragmentos/pie.jspf" %>
