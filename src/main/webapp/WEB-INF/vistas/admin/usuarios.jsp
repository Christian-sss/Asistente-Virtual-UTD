<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Usuarios"/>
<c:set var="menu" value="admin"/>
<c:set var="seccion" value="usuarios"/>
<%@ include file="../fragmentos/cabecera.jspf" %>
<%@ include file="../fragmentos/admin-menu.jspf" %>

<div class="container mt-4">
    <div class="row g-4">
        <div class="col-lg-7">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <h2 class="h6 titulo-seccion mb-0">Usuarios administrativos</h2>
                <a class="btn btn-sm btn-primary" href="${ctx}/admin/usuarios?nuevo=1"><i class="bi bi-person-plus"></i> Nuevo usuario</a>
            </div>
            <div class="card tarjeta">
                <div class="table-responsive">
                    <table class="table table-hover tabla-compacta mb-0 small">
                        <thead class="table-light"><tr><th>Usuario</th><th>Nombre</th><th>Rol</th><th>Estado</th><th></th></tr></thead>
                        <tbody>
                        <c:forEach var="u" items="${usuarios}">
                            <tr class="${u.activo ? '' : 'text-secondary'}">
                                <td><c:out value="${u.usuario}"/><c:if test="${u.id == usr.id}"> <span class="badge text-bg-light">usted</span></c:if></td>
                                <td><c:out value="${u.nombreCompleto}"/></td>
                                <td><span class="badge ${u.rol == 'ADMIN_CONTENIDOS' ? 'text-bg-primary' : 'text-bg-info'}"><c:out value="${u.rol}"/></span></td>
                                <td><span class="badge ${u.activo ? 'text-bg-success' : 'text-bg-secondary'}">${u.activo ? 'Activo' : 'Desactivado'}</span></td>
                                <td class="text-end text-nowrap">
                                    <a class="btn btn-sm btn-outline-primary" href="${ctx}/admin/usuarios?id=${u.id}"><i class="bi bi-pencil"></i></a>
                                    <c:if test="${u.id != usr.id}">
                                        <form method="post" action="${ctx}/admin/usuarios" class="d-inline">
                                            <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${u.id}">
                                            <button class="btn btn-sm ${u.activo ? 'btn-outline-danger' : 'btn-outline-success'}" name="accion" value="${u.activo ? 'desactivar' : 'activar'}"
                                                    title="${u.activo ? 'Desactivar' : 'Activar'}"><i class="bi ${u.activo ? 'bi-person-dash' : 'bi-person-check'}"></i></button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
            <p class="small text-secondary mt-2"><i class="bi bi-info-circle"></i> Los usuarios no se eliminan: se desactivan para conservar la trazabilidad de la bitácora.
                ADMIN_CONTENIDOS administra los contenidos; PERSONAL_UTD valida y publica contenidos y consulta reportes.</p>
        </div>
        <div class="col-lg-5">
            <c:choose>
                <c:when test="${empty editar}">
                    <div class="alert alert-info">Seleccione un usuario para editarlo o registre uno nuevo.</div>
                </c:when>
                <c:otherwise>
                    <div class="card tarjeta mb-3">
                        <div class="card-body">
                            <h2 class="h6 titulo-seccion">${editar.id == 0 ? 'Nuevo usuario' : 'Editar usuario'}</h2>
                            <form method="post" action="${ctx}/admin/usuarios" class="row g-2">
                                <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="accion" value="guardar">
                                <c:if test="${editar.id != 0}"><input type="hidden" name="id" value="${editar.id}"></c:if>
                                <div class="col-12"><label class="form-label small" for="usuario">Usuario</label>
                                    <input class="form-control form-control-sm" id="usuario" name="usuario" value="<c:out value='${editar.usuario}'/>"
                                           ${editar.id != 0 ? 'readonly' : 'required'} pattern="[a-zA-Z0-9._\-]{3,60}" maxlength="60"></div>
                                <div class="col-12"><label class="form-label small" for="nombre">Nombre completo</label>
                                    <input class="form-control form-control-sm" id="nombre" name="nombreCompleto" value="<c:out value='${editar.nombreCompleto}'/>" required maxlength="150"></div>
                                <div class="col-12"><label class="form-label small" for="rol">Rol</label>
                                    <select class="form-select form-select-sm" id="rol" name="rol" ${editar.id == usr.id ? 'disabled' : ''}>
                                        <option value="ADMIN_CONTENIDOS" ${editar.rol == 'ADMIN_CONTENIDOS' ? 'selected' : ''}>ADMIN_CONTENIDOS - Administrador de contenidos</option>
                                        <option value="PERSONAL_UTD" ${editar.rol == 'PERSONAL_UTD' ? 'selected' : ''}>PERSONAL_UTD - Personal de la UTD</option>
                                    </select>
                                    <c:if test="${editar.id == usr.id}"><input type="hidden" name="rol" value="${editar.rol}"></c:if></div>
                                <c:if test="${editar.id == 0}">
                                    <div class="col-12"><label class="form-label small" for="clave">Contraseña temporal</label>
                                        <input class="form-control form-control-sm" id="clave" type="password" name="contrasena" required minlength="8" autocomplete="new-password">
                                        <div class="form-text">Al menos 8 caracteres, con letras y números.</div></div>
                                </c:if>
                                <div class="col-12"><button class="btn btn-sm btn-primary"><i class="bi bi-save"></i> Guardar</button></div>
                            </form>
                        </div>
                    </div>
                    <c:if test="${editar.id != 0}">
                        <div class="card tarjeta">
                            <div class="card-body">
                                <h2 class="h6 titulo-seccion">Asignar contraseña temporal</h2>
                                <form method="post" action="${ctx}/admin/usuarios" class="d-flex gap-2">
                                    <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${editar.id}">
                                    <input class="form-control form-control-sm" type="password" name="contrasena" required minlength="8" placeholder="Nueva contraseña" autocomplete="new-password">
                                    <button class="btn btn-sm btn-outline-warning text-nowrap" name="accion" value="contrasena"><i class="bi bi-key"></i> Asignar</button>
                                </form>
                            </div>
                        </div>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%@ include file="../fragmentos/pie.jspf" %>
