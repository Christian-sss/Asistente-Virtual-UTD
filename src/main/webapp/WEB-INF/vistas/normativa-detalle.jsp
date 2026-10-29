<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="${normativa.nombreCompleto}"/>
<c:set var="menu" value="normativa"/>
<%@ include file="fragmentos/cabecera.jspf" %>

<div class="encabezado-pagina">
    <div class="container">
        <a href="${ctx}/normativa" class="small"><i class="bi bi-arrow-left"></i> Volver a normativa</a>
        <h1 class="mt-2"><c:out value="${normativa.nombreCompleto}"/></h1>
        <p class="mb-0"><c:out value="${normativa.titulo}"/></p>
    </div>
</div>

<div class="container mt-4">
    <div class="row g-4">
        <div class="col-lg-4">
            <div class="card tarjeta">
                <div class="card-body small">
                    <dl class="mb-0">
                        <dt>Estado</dt><dd><span class="badge ${normativa.vigente ? 'text-bg-success' : 'text-bg-danger'}">${normativa.vigente ? 'Vigente' : 'No vigente'}</span></dd>
                        <dt>Órgano emisor</dt><dd><c:out value="${normativa.organoEmisor}"/></dd>
                        <dt>Fecha de aprobación</dt><dd>${empty normativa.fechaAprobacion ? '-' : normativa.fechaAprobacion}</dd>
                        <dt>Última verificación</dt><dd>${normativa.fechaUltimaVerificacion}
                            <c:if test="${normativa.revisionPendiente}"><span class="badge text-bg-warning">Revisar</span></c:if></dd>
                        <c:if test="${not empty normativa.fuenteOficial}">
                            <dt>Fuente oficial</dt><dd class="mb-0 text-break"><a href="<c:out value='${normativa.fuenteOficial}'/>" target="_blank" rel="noopener"><c:out value="${normativa.fuenteOficial}"/></a></dd>
                        </c:if>
                    </dl>
                </div>
            </div>
        </div>
        <div class="col-lg-8">
            <c:forEach var="a" items="${normativa.articulos}">
                <div class="card tarjeta mb-3">
                    <div class="card-body">
                        <h2 class="h6 titulo-seccion"><c:out value="${a.numero}"/></h2>
                        <p class="mb-2"><c:out value="${a.contenido}"/></p>
                        <c:if test="${not empty a.requisitosSustentados}">
                            <p class="small fw-semibold mb-1">Requisitos que sustenta:</p>
                            <ul class="small text-secondary mb-0">
                                <c:forEach var="r" items="${a.requisitosSustentados}"><li><c:out value="${r}"/></li></c:forEach>
                            </ul>
                        </c:if>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</div>

<%@ include file="fragmentos/pie.jspf" %>
