<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="${tramite.nombre}"/>
<c:set var="menu" value="tramites"/>
<%@ include file="fragmentos/cabecera.jspf" %>

<div class="encabezado-pagina">
    <div class="container">
        <c:if test="${identificado}">
            <div class="alert alert-success py-2 mb-3 no-imprimir"><i class="bi bi-check-circle"></i>
                Según tus respuestas, este es el trámite que te corresponde.</div>
        </c:if>
        <div class="d-flex flex-wrap justify-content-between align-items-start gap-3">
            <div>
                <small class="text-secondary"><c:out value="${tramite.categoria}"/> &middot; Código TUPA <c:out value="${tramite.codigoTupa}"/></small>
                <h1 class="mt-1"><c:out value="${tramite.nombre}"/></h1>
                <p class="text-secondary mb-0"><c:out value="${tramite.descripcion}"/></p>
            </div>
            <div class="d-flex gap-2 no-imprimir">
                <a class="btn btn-primary" href="${ctx}/hoja-ruta/pdf?id=${tramite.id}"><i class="bi bi-file-earmark-pdf"></i> Descargar PDF</a>
                <button class="btn btn-outline-secondary" onclick="window.print()"><i class="bi bi-printer"></i> Imprimir</button>
            </div>
        </div>
    </div>
</div>

<div class="container mt-4">
    <div class="row g-4">
        <div class="col-lg-8">
            <div class="card tarjeta mb-4" id="pasos">
                <div class="card-body">
                    <h2 class="h5 titulo-seccion"><i class="bi bi-map"></i> Hoja de ruta</h2>
                    <c:forEach var="p" items="${tramite.pasos}">
                        <div class="paso">
                            <div class="paso-numero">${p.orden}</div>
                            <div>
                                <c:out value="${p.descripcion}"/>
                                <c:if test="${p.pagoPrevio}"><span class="badge text-bg-warning ms-1">Pago previo</span></c:if>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>

            <div class="card tarjeta mb-4">
                <div class="card-body">
                    <h2 class="h5 titulo-seccion"><i class="bi bi-list-check"></i> Requisitos</h2>
                    <ol class="mb-0">
                        <c:forEach var="r" items="${tramite.requisitos}">
                            <li class="mb-2">
                                <c:out value="${r.descripcion}"/>
                                <c:if test="${not r.obligatorio}"><span class="badge text-bg-light">Según corresponda</span></c:if>
                                <c:forEach var="a" items="${r.articulos}">
                                    <div class="sustento"><i class="bi bi-bookmark"></i> Sustento:
                                        <a href="${ctx}/normativa/detalle?id=${a.idNormativa}"><c:out value="${a.normativa}"/></a>,
                                        <c:out value="${a.numero}"/>
                                        <c:if test="${not a.vigente}"><span class="badge text-bg-danger">No vigente</span></c:if>
                                    </div>
                                </c:forEach>
                            </li>
                        </c:forEach>
                    </ol>
                </div>
            </div>

            <div class="aviso-referencial"><i class="bi bi-info-circle"></i> <strong>Importante:</strong> <c:out value="${aviso}"/></div>
        </div>

        <div class="col-lg-4">
            <div class="card tarjeta mb-3">
                <div class="card-body">
                    <h2 class="h6 titulo-seccion">Datos del trámite</h2>
                    <dl class="mb-0 small">
                        <dt>Solicitud dirigida a</dt><dd><c:out value="${tramite.dirigidoA}"/></dd>
                        <dt>Derecho de trámite</dt><dd><c:out value="${tramite.pagoTexto}"/></dd>
                        <dt>Plazo referencial</dt><dd><c:out value="${tramite.plazoReferencial}"/></dd>
                        <dt>Calificación</dt><dd class="mb-0"><c:out value="${tramite.calificacion}"/></dd>
                    </dl>
                </div>
            </div>
            <c:if test="${not empty tramite.punto}">
                <div class="card tarjeta mb-3">
                    <div class="card-body">
                        <h2 class="h6 titulo-seccion"><i class="bi bi-geo-alt"></i> Dónde presentar</h2>
                        <p class="mb-1 fw-semibold"><c:out value="${tramite.punto.nombre}"/></p>
                        <p class="small mb-1"><c:out value="${tramite.punto.sede}"/> &middot; <c:out value="${tramite.punto.direccion}"/></p>
                        <p class="small mb-1"><i class="bi bi-clock"></i> <c:out value="${tramite.punto.horario}"/></p>
                        <c:if test="${not empty tramite.punto.telefono}"><p class="small mb-1"><i class="bi bi-telephone"></i> <c:out value="${tramite.punto.telefono}"/></p></c:if>
                        <c:if test="${not empty tramite.punto.correo}"><p class="small mb-0 text-break"><i class="bi bi-envelope"></i> <c:out value="${tramite.punto.correo}"/></p></c:if>
                    </div>
                </div>
            </c:if>
            <div class="card tarjeta no-imprimir">
                <div class="card-body">
                    <h2 class="h6 titulo-seccion">Antes de ir a ventanilla</h2>
                    <p class="small text-secondary">Sube tu solicitud y verifica que tenga DNI, correo, fecha, firma y pago.</p>
                    <a class="btn btn-warning w-100" href="${ctx}/revision?tramite=${tramite.id}"><i class="bi bi-file-earmark-check"></i> Revisar mi solicitud</a>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="fragmentos/pie.jspf" %>
