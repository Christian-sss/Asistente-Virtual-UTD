<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Trámites"/>
<c:set var="menu" value="admin"/>
<c:set var="seccion" value="tramites"/>
<%@ include file="../fragmentos/cabecera.jspf" %>
<%@ include file="../fragmentos/admin-menu.jspf" %>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h2 class="h5 titulo-seccion mb-0">Trámites de la base de conocimiento</h2>
        <c:if test="${usr.rol == 'ADMIN_CONTENIDOS'}">
            <a class="btn btn-primary btn-sm" href="${ctx}/admin/tramite"><i class="bi bi-plus-lg"></i> Nuevo trámite</a>
        </c:if>
    </div>
    <div class="card tarjeta">
        <div class="table-responsive">
            <table class="table table-hover tabla-compacta mb-0">
                <thead class="table-light"><tr><th>Código</th><th>Trámite</th><th>Categoría</th><th>Derecho</th><th>Plazo</th><th>Estado</th><th></th></tr></thead>
                <tbody>
                <c:forEach var="t" items="${tramites}">
                    <tr>
                        <td class="small"><c:out value="${t.codigo}"/><br><span class="text-secondary"><c:out value="${t.codigoTupa}"/></span></td>
                        <td><c:out value="${t.nombre}"/></td>
                        <td class="small"><c:out value="${t.categoria}"/></td>
                        <td class="small"><c:out value="${t.pagoTexto}"/></td>
                        <td class="small"><c:out value="${t.plazoReferencial}"/></td>
                        <td><span class="badge ${t.publicado ? 'text-bg-success' : 'text-bg-secondary'}">${t.publicado ? 'Publicado' : 'Borrador'}</span></td>
                        <td class="text-end text-nowrap">
                            <c:if test="${t.publicado}"><a class="btn btn-sm btn-outline-secondary" href="${ctx}/hoja-ruta?id=${t.id}" title="Ver como usuario"><i class="bi bi-eye"></i></a></c:if>
                            <c:if test="${usr.rol == 'ADMIN_CONTENIDOS'}"><a class="btn btn-sm btn-primary" href="${ctx}/admin/tramite?id=${t.id}"><i class="bi bi-pencil"></i> Editar</a></c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="../fragmentos/pie.jspf" %>
