<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Ubicación y canales"/>
<c:set var="menu" value="ubicacion"/>
<%@ include file="fragmentos/cabecera.jspf" %>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.css">

<div class="encabezado-pagina">
    <div class="container">
        <h1><i class="bi bi-geo-alt"></i> Ubicación de puntos de trámite y canales digitales</h1>
        <p class="text-secondary mb-0">Oficinas y ventanillas en el mapa del campus, y los portales, correos y teléfonos de cada dependencia.</p>
    </div>
</div>

<div class="container mt-4">
    <form class="row g-2 mb-3" method="get" action="${ctx}/ubicacion">
        <div class="col-md-9">
            <select class="form-select" name="tramite" aria-label="Trámite">
                <option value="">Ver todos los puntos de atención</option>
                <c:forEach var="t" items="${tramites}">
                    <option value="${t.id}" ${tramite.id == t.id ? 'selected' : ''}>Secuencia de visita: <c:out value="${t.nombre}"/></option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-3 d-grid"><button class="btn btn-primary"><i class="bi bi-signpost"></i> Mostrar en el mapa</button></div>
    </form>

    <c:if test="${not empty tramite}">
        <div class="card tarjeta mb-3">
            <div class="card-body">
                <h2 class="h6 titulo-seccion mb-2">Secuencia de visita: <c:out value="${tramite.nombre}"/></h2>
                <ol class="mb-0 small">
                    <c:forEach var="p" items="${tramite.secuenciaVisita}">
                        <li><strong><c:out value="${p.nombre}"/></strong>
                            <c:if test="${not empty p.referencia}"> &middot; <c:out value="${p.referencia}"/></c:if>
                            &middot; <c:out value="${p.horario}"/></li>
                    </c:forEach>
                </ol>
                <a class="small" href="${ctx}/hoja-ruta?id=${tramite.id}"><i class="bi bi-arrow-left"></i> Volver a la hoja de ruta</a>
            </div>
        </div>
    </c:if>

    <div class="card tarjeta mb-4">
        <div id="plano" class="plano-campus" data-url="${ctx}/ubicacion/puntos${empty tramite ? '' : '?tramite='+=tramite.id}"
             role="application" aria-label="Mapa del campus"></div>
    </div>
    <p class="small text-secondary"><i class="bi bi-info-circle"></i> Mapa de la Ciudad Universitaria (Av. Miraflores y Av. Cuzco, Tacna).
        Use el botón de capas para ver la vista satelital. Cada punto indica la fecha de su última verificación; si supera un
        semestre se marca como pendiente de verificación.</p>

    <h2 class="h5 titulo-seccion mt-4 mb-3">Dependencias, puntos de atención y canales digitales</h2>
    <div class="row g-3">
        <c:forEach var="d" items="${dependencias}">
            <div class="col-lg-6" id="dep${d.id}">
                <div class="card tarjeta h-100 ${dependenciaSel == d.id ? 'border border-primary' : ''}">
                    <div class="card-body">
                        <h3 class="h6 mb-1"><c:out value="${d.nombre}"/> <c:if test="${not empty d.sigla}"><span class="badge text-bg-light"><c:out value="${d.sigla}"/></span></c:if></h3>
                        <c:if test="${not empty d.funciones}"><p class="small text-secondary mb-2"><c:out value="${d.funciones}"/></p></c:if>
                        <c:forEach var="p" items="${d.puntos}">
                            <div class="border-start border-3 ps-2 mb-2 small">
                                <strong><c:out value="${p.nombre}"/></strong>
                                <c:if test="${p.revisionPendiente}"><span class="badge text-bg-warning">Pendiente de verificación</span></c:if><br>
                                <c:if test="${not empty p.referencia}"><c:out value="${p.referencia}"/> &middot; </c:if><c:out value="${p.sede}"/>, <c:out value="${p.direccion}"/><br>
                                <i class="bi bi-clock"></i> <c:out value="${p.horario}"/>
                                <c:if test="${not empty p.telefono}"> &middot; <i class="bi bi-telephone"></i> <c:out value="${p.telefono}"/></c:if>
                                <c:if test="${p.ubicado}"><br><button type="button" class="btn btn-link btn-sm p-0" data-ver-punto="${p.id}"><i class="bi bi-pin-map"></i> Ver en el mapa</button></c:if>
                            </div>
                        </c:forEach>
                        <c:if test="${not empty d.canales}">
                            <p class="small fw-semibold mb-1 mt-2">Canales digitales</p>
                            <ul class="list-unstyled small mb-0">
                                <c:forEach var="ca" items="${d.canales}">
                                    <li class="canal-item mb-1"><i class="bi ${ca.icono} text-primary"></i> <span class="text-secondary"><c:out value="${ca.tipoTexto}"/>:</span>
                                        <c:choose>
                                            <c:when test="${ca.enlace}"><a href="<c:out value='${ca.valor}'/>" target="_blank" rel="noopener"><c:out value="${ca.nombre}"/> <i class="bi bi-box-arrow-up-right"></i></a></c:when>
                                            <c:otherwise><strong><c:out value="${ca.nombre}"/>:</strong> <c:out value="${ca.valor}"/></c:otherwise>
                                        </c:choose>
                                        <c:if test="${ca.revisionPendiente}"><span class="badge text-bg-warning">Pendiente de verificación</span></c:if>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:if>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.js"></script>
<script src="${ctx}/recursos/js/mapa-base.js?v=2.3"></script>
<script src="${ctx}/recursos/js/plano.js?v=2.1"></script>
<%@ include file="fragmentos/pie.jspf" %>
