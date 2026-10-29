<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Bitácora"/>
<c:set var="menu" value="admin"/>
<c:set var="seccion" value="bitacora"/>
<%@ include file="../fragmentos/cabecera.jspf" %>
<%@ include file="../fragmentos/admin-menu.jspf" %>

<div class="container mt-4">
    <p class="text-secondary small">Toda operación administrativa queda registrada con usuario, fecha, entidad, acción, valor anterior y valor nuevo.</p>
    <div class="card tarjeta">
        <div class="table-responsive">
            <table class="table table-sm table-hover small mb-0">
                <thead class="table-light"><tr><th>Fecha</th><th>Usuario</th><th>Entidad</th><th>Acción</th><th>Valor anterior</th><th>Valor nuevo</th></tr></thead>
                <tbody>
                <c:forEach var="b" items="${bitacora}">
                    <tr>
                        <td class="text-nowrap">${b.fechaTexto}</td>
                        <td><c:out value="${b.usuario}"/></td>
                        <td><c:out value="${b.entidad}"/></td>
                        <td><span class="badge text-bg-light"><c:out value="${b.accion}"/></span></td>
                        <td class="text-secondary"><c:out value="${b.valorAnterior}"/></td>
                        <td><c:out value="${b.valorNuevo}"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="../fragmentos/pie.jspf" %>
