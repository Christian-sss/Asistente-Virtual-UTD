<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Catálogo de trámites"/>
<c:set var="menu" value="tramites"/>
<%@ include file="fragmentos/cabecera.jspf" %>

<div class="encabezado-pagina">
    <div class="container">
        <h1><i class="bi bi-grid"></i> Catálogo de trámites</h1>
        <p class="text-secondary mb-0">Trámites del TUPA de la UNJBG publicados y validados por la UTD.</p>
    </div>
</div>

<div class="container mt-4">
    <form class="row g-2 mb-4" method="get" action="${ctx}/tramites">
        <div class="col-md-6">
            <input class="form-control" type="search" name="q" value="<c:out value='${q}'/>"
                   placeholder="Buscar por nombre, palabra clave o código TUPA">
        </div>
        <div class="col-md-4">
            <select class="form-select" name="categoria">
                <option value="">Todas las categorías</option>
                <c:forEach var="cat" items="${categorias}">
                    <option value="${cat.id}" ${categoriaSel == cat.id ? 'selected' : ''}><c:out value="${cat.nombre}"/></option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-2 d-grid">
            <button class="btn btn-primary"><i class="bi bi-search"></i> Buscar</button>
        </div>
    </form>

    <c:choose>
        <c:when test="${empty tramites}">
            <div class="alert alert-info">
                No se encontraron trámites con esos criterios.
                <a href="${ctx}/orientacion" class="alert-link">Prueba la orientación guiada</a> para identificar tu trámite.
            </div>
        </c:when>
        <c:otherwise>
            <p class="text-secondary small">${tramites.size()} trámite(s) encontrado(s).</p>
            <div class="row g-3">
                <c:forEach var="t" items="${tramites}">
                    <div class="col-md-6 col-xl-4">
                        <div class="card tarjeta h-100">
                            <div class="card-body d-flex flex-column">
                                <div class="d-flex justify-content-between mb-2">
                                    <span class="badge text-bg-light"><c:out value="${t.categoria}"/></span>
                                    <small class="text-secondary">TUPA <c:out value="${t.codigoTupa}"/></small>
                                </div>
                                <h5 class="card-title h6"><c:out value="${t.nombre}"/></h5>
                                <p class="small text-secondary flex-grow-1"><c:out value="${t.descripcion}"/></p>
                                <div class="d-flex justify-content-between small mb-3">
                                    <span><i class="bi bi-cash-coin text-success"></i> <c:out value="${t.pagoTexto}"/></span>
                                    <span><i class="bi bi-clock text-primary"></i> <c:out value="${t.plazoReferencial}"/></span>
                                </div>
                                <div class="d-flex gap-2">
                                    <a class="btn btn-primary btn-sm flex-grow-1" href="${ctx}/hoja-ruta?id=${t.id}"><i class="bi bi-map"></i> Ver hoja de ruta</a>
                                    <a class="btn btn-outline-secondary btn-sm" href="${ctx}/revision?tramite=${t.id}" title="Revisar mi solicitud"><i class="bi bi-file-earmark-check"></i></a>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="fragmentos/pie.jspf" %>
