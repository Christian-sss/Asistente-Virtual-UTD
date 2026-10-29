<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Reporte de conformidad"/>
<c:set var="menu" value="revision"/>
<%@ include file="fragmentos/cabecera.jspf" %>

<div class="encabezado-pagina">
    <div class="container">
        <h1><i class="bi bi-clipboard-check"></i> Reporte de conformidad</h1>
        <p class="text-secondary mb-0"><c:out value="${revision.tramite}"/> &middot; <c:out value="${revision.nombreOriginal}"/>
            (<c:out value="${revision.tamanoTexto}"/><c:if test="${not empty revision.numeroPaginas}">, ${revision.numeroPaginas} página(s)</c:if>)</p>
    </div>
</div>

<div class="container mt-4" style="max-width: 960px">
    <c:set var="estado" value="${revision.estado}"/>
    <div class="estado-general estado-${estado} mb-4 d-flex align-items-center gap-3">
        <i class="bi ${estado == 'CUMPLE' ? 'bi-check-circle' : (estado == 'OBSERVADO' ? 'bi-exclamation-triangle' : 'bi-question-circle')} fs-1"></i>
        <div>
            <div class="fs-5 fw-bold">
                <c:choose>
                    <c:when test="${estado == 'CUMPLE'}">Tu solicitud cumple los requisitos mínimos formales revisados</c:when>
                    <c:when test="${estado == 'OBSERVADO'}">Tu solicitud tiene observaciones: corrígelas antes de presentarla</c:when>
                    <c:otherwise>Hay criterios que no se pudieron verificar automáticamente</c:otherwise>
                </c:choose>
            </div>
            <small>El documento y este reporte se eliminarán automáticamente el <c:out value="${revision.fechaExpiracionTexto}"/>.</small>
        </div>
    </div>

    <div class="card tarjeta mb-4">
        <div class="card-body">
            <h2 class="h5 titulo-seccion">Resultado por criterio</h2>
            <div class="table-responsive">
                <table class="table tabla-compacta mb-0">
                    <thead><tr><th>Criterio</th><th style="width:150px">Resultado</th><th>Detalle o recomendación</th></tr></thead>
                    <tbody>
                    <c:forEach var="r" items="${revision.resultados}">
                        <tr>
                            <td>
                                <c:out value="${r.criterio}"/>
                                <div><small class="text-secondary">
                                    <c:choose>
                                        <c:when test="${r.tipo == 'BASICO'}">Validación del archivo</c:when>
                                        <c:when test="${r.tipo == 'DATO_OBLIGATORIO'}">Dato obligatorio</c:when>
                                        <c:otherwise>Confirmación asistida</c:otherwise>
                                    </c:choose>
                                </small></div>
                            </td>
                            <td>
                                <span class="badge badge-${r.resultado.toLowerCase()}">
                                    <c:choose>
                                        <c:when test="${r.resultado == 'CUMPLE'}"><i class="bi bi-check-lg"></i> Cumple</c:when>
                                        <c:when test="${r.resultado == 'OBSERVADO'}"><i class="bi bi-x-lg"></i> Observado</c:when>
                                        <c:otherwise><i class="bi bi-question-lg"></i> No verificable</c:otherwise>
                                    </c:choose>
                                </span>
                            </td>
                            <td class="small"><c:out value="${r.detalle}"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <c:set var="hayAsistidos" value="false"/>
    <c:forEach var="r" items="${revision.resultados}">
        <c:if test="${r.tipo == 'CONFIRMACION_ASISTIDA'}"><c:set var="hayAsistidos" value="true"/></c:if>
    </c:forEach>
    <c:if test="${hayAsistidos}">
        <div class="card tarjeta mb-4 no-imprimir">
            <div class="card-body">
                <h2 class="h5 titulo-seccion"><i class="bi bi-hand-index"></i> Confirmación asistida</h2>
                <p class="small text-secondary">El sistema no verifica firmas, sellos ni anexos. Marque lo que ya tiene listo:</p>
                <form method="post" action="${ctx}/revision/confirmacion">
                    <input type="hidden" name="_csrf" value="${csrf}">
                    <input type="hidden" name="revision" value="${revision.id}">
                    <c:forEach var="r" items="${revision.resultados}">
                        <c:if test="${r.tipo == 'CONFIRMACION_ASISTIDA'}">
                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" name="confirmado" value="${r.id}" id="c${r.id}"
                                       ${r.resultado == 'CUMPLE' ? 'checked' : ''}>
                                <label class="form-check-label" for="c${r.id}"><c:out value="${r.criterio}"/></label>
                            </div>
                        </c:if>
                    </c:forEach>
                    <button class="btn btn-outline-primary mt-3"><i class="bi bi-check2-square"></i> Registrar confirmación</button>
                </form>
            </div>
        </div>
    </c:if>

    <div class="aviso-referencial mb-4"><i class="bi bi-info-circle"></i> <strong>Importante:</strong> <c:out value="${aviso}"/>
        El sistema no verifica la autenticidad de firmas, sellos, comprobantes de pago ni documentos de identidad.</div>

    <div class="d-flex flex-wrap gap-2 no-imprimir">
        <a class="btn btn-primary" href="${ctx}/revision?tramite=${revision.idTramite}"><i class="bi bi-arrow-repeat"></i> Revisar otra versión</a>
        <a class="btn btn-outline-secondary" href="${ctx}/hoja-ruta?id=${revision.idTramite}"><i class="bi bi-map"></i> Ver hoja de ruta del trámite</a>
        <button class="btn btn-outline-secondary" onclick="window.print()"><i class="bi bi-printer"></i> Imprimir reporte</button>
    </div>
</div>

<%@ include file="fragmentos/pie.jspf" %>
