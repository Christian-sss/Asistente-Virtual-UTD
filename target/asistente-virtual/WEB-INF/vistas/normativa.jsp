<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Normativa"/>
<c:set var="menu" value="normativa"/>
<%@ include file="fragmentos/cabecera.jspf" %>

<div class="encabezado-pagina">
    <div class="container">
        <h1><i class="bi bi-journal-bookmark"></i> Información normativa</h1>
        <p class="text-secondary mb-0">Normas, artículos y vigencia que sustentan los requisitos de cada trámite.</p>
    </div>
</div>

<div class="container mt-4">
    <form class="row g-2 mb-4" method="get" action="${ctx}/normativa">
        <div class="col-md-5">
            <input class="form-control" type="search" name="q" value="<c:out value='${q}'/>" placeholder="Palabra clave (ej.: bachiller, información, consentimiento)">
        </div>
        <div class="col-md-5">
            <select class="form-select" name="tramite">
                <option value="">Todos los trámites</option>
                <c:forEach var="t" items="${tramites}">
                    <option value="${t.id}" ${tramiteSel == t.id ? 'selected' : ''}><c:out value="${t.nombre}"/></option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-2 d-grid"><button class="btn btn-primary"><i class="bi bi-search"></i> Buscar</button></div>
    </form>

    <c:if test="${empty normativas}">
        <div class="alert alert-info">No se encontraron normas con esos criterios.</div>
    </c:if>
    <div class="row g-3">
        <c:forEach var="n" items="${normativas}">
            <div class="col-lg-6">
                <div class="card tarjeta h-100">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start mb-2">
                            <span class="badge ${n.vigente ? 'text-bg-success' : 'text-bg-danger'}">${n.vigente ? 'Vigente' : 'No vigente'}</span>
                            <c:if test="${n.revisionPendiente}"><span class="badge text-bg-warning">Pendiente de verificación</span></c:if>
                        </div>
                        <h2 class="h6 mb-1"><c:out value="${n.tipo}"/> <c:out value="${n.numero}"/></h2>
                        <p class="mb-1"><c:out value="${n.titulo}"/></p>
                        <p class="small text-secondary mb-2">Emisor: <c:out value="${n.organoEmisor}"/>
                            <c:if test="${not empty n.fechaAprobacion}"> &middot; Aprobada: ${n.fechaAprobacion}</c:if>
                            &middot; Verificada: ${n.fechaUltimaVerificacion}</p>
                        <c:if test="${not empty n.articulos}">
                            <ul class="small mb-2">
                                <c:forEach var="a" items="${n.articulos}" end="3">
                                    <li><strong><c:out value="${a.numero}"/>:</strong> <c:out value="${a.contenido}"/></li>
                                </c:forEach>
                            </ul>
                        </c:if>
                        <a class="btn btn-sm btn-outline-primary" href="${ctx}/normativa/detalle?id=${n.id}">Ver artículos y requisitos que sustenta</a>
                        <c:if test="${not empty n.fuenteOficial}">
                            <a class="btn btn-sm btn-link" href="<c:out value='${n.fuenteOficial}'/>" target="_blank" rel="noopener">Fuente oficial <i class="bi bi-box-arrow-up-right"></i></a>
                        </c:if>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
</div>

<%@ include file="fragmentos/pie.jspf" %>
