<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Reportes"/>
<c:set var="menu" value="admin"/>
<c:set var="seccion" value="reportes"/>
<%@ include file="../fragmentos/cabecera.jspf" %>
<%@ include file="../fragmentos/admin-menu.jspf" %>

<div class="container mt-4">
    <form class="card tarjeta mb-3" method="get" action="${ctx}/admin/reportes">
        <div class="card-body row g-2 align-items-end">
            <div class="col-md-4"><label class="form-label small" for="tipo">Reporte</label>
                <select class="form-select" id="tipo" name="tipo">
                    <option value="consultas" ${tipo == 'consultas' ? 'selected' : ''}>Trámites más consultados</option>
                    <option value="servicios" ${tipo == 'servicios' ? 'selected' : ''}>Consultas por servicio</option>
                    <option value="observaciones" ${tipo == 'observaciones' ? 'selected' : ''}>Observaciones formales más frecuentes</option>
                    <option value="retroalimentacion" ${tipo == 'retroalimentacion' ? 'selected' : ''}>Retroalimentación recibida</option>
                    <option value="bitacora" ${tipo == 'bitacora' ? 'selected' : ''}>Bitácora de cambios</option>
                </select></div>
            <div class="col-6 col-md-3"><label class="form-label small" for="desde">Desde</label>
                <input class="form-control" type="date" id="desde" name="desde" value="${desde}" required></div>
            <div class="col-6 col-md-3"><label class="form-label small" for="hasta">Hasta</label>
                <input class="form-control" type="date" id="hasta" name="hasta" value="${hasta}" required></div>
            <div class="col-md-2 d-grid"><button class="btn btn-primary"><i class="bi bi-funnel"></i> Generar</button></div>
        </div>
    </form>

    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-2">
        <h2 class="h6 titulo-seccion mb-0"><c:out value="${reporte.titulo()}"/> &middot; <span class="text-secondary fw-normal">${desde} al ${hasta} &middot; ${reporte.filas().size()} fila(s)</span></h2>
        <a class="btn btn-sm btn-success" href="${ctx}/admin/reportes/csv?tipo=${tipo}&amp;desde=${desde}&amp;hasta=${hasta}"><i class="bi bi-filetype-csv"></i> Exportar CSV</a>
    </div>

    <c:choose>
        <c:when test="${tipo == 'retroalimentacion'}">
            <div class="card tarjeta">
                <div class="table-responsive">
                    <table class="table tabla-compacta small mb-0">
                        <thead class="table-light"><tr><th>Fecha</th><th>Trámite</th><th>Tipo</th><th>Comentario</th><th>Estado</th></tr></thead>
                        <tbody>
                        <c:forEach var="r" items="${retroalimentacion}">
                            <tr class="${r.atendida ? 'text-secondary' : ''}">
                                <td class="text-nowrap">${r.fechaTexto}</td>
                                <td><c:out value="${empty r.tramite ? '(General)' : r.tramite}"/></td>
                                <td><c:out value="${r.tipoTexto}"/></td>
                                <td><c:out value="${r.comentario}"/></td>
                                <td class="text-nowrap">
                                    <form method="post" action="${ctx}/admin/reportes" class="d-inline">
                                        <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${r.id}">
                                        <input type="hidden" name="desde" value="${desde}"><input type="hidden" name="hasta" value="${hasta}">
                                        <c:if test="${not r.atendida}"><input type="hidden" name="atendida" value="1"></c:if>
                                        <button class="btn btn-sm ${r.atendida ? 'btn-outline-secondary' : 'btn-outline-success'}">
                                            <i class="bi ${r.atendida ? 'bi-arrow-counterclockwise' : 'bi-check2'}"></i> ${r.atendida ? 'Reabrir' : 'Marcar atendida'}</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty retroalimentacion}"><tr><td colspan="5" class="text-secondary">No se recibieron reportes en el periodo.</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="card tarjeta">
                <div class="table-responsive">
                    <table class="table table-hover tabla-compacta small mb-0">
                        <thead class="table-light"><tr><c:forEach var="col" items="${reporte.columnas()}"><th><c:out value="${col}"/></th></c:forEach></tr></thead>
                        <tbody>
                        <c:forEach var="f" items="${reporte.filas()}">
                            <tr><c:forEach var="v" items="${f}"><td><c:out value="${v}"/></td></c:forEach></tr>
                        </c:forEach>
                        <c:if test="${empty reporte.filas()}"><tr><td colspan="${reporte.columnas().size()}" class="text-secondary">No hay datos en el periodo seleccionado.</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
    <p class="small text-secondary mt-2"><i class="bi bi-shield-check"></i> Las estadísticas se generan solo con datos anónimos: no se registran
        datos personales ni el contenido de los documentos. El CSV usa punto y coma como separador para abrirse directamente en Excel.</p>
</div>

<%@ include file="../fragmentos/pie.jspf" %>
