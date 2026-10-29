<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Inicio"/>
<%@ include file="fragmentos/cabecera.jspf" %>

<section class="portada">
    <div class="container">
        <div class="row align-items-center g-4">
            <div class="col-lg-7">
                <span class="badge bg-warning text-dark mb-3">Unidad de Trámite Documentario - UNJBG</span>
                <h1 class="display-6 fw-bold">¿Qué trámite necesitas hacer?</h1>
                <p class="lead opacity-75">Encuentra tu trámite, conoce sus requisitos, pagos previos y dónde presentarlo,
                    y revisa tu solicitud antes de ir a ventanilla.</p>
                <form action="${ctx}/tramites" method="get" class="d-flex gap-2 mt-4" role="search">
                    <input class="form-control form-control-lg" type="search" name="q"
                           placeholder="Ej.: certificado de estudios, bachiller, reserva de matrícula" aria-label="Buscar trámite">
                    <button class="btn btn-warning btn-lg px-4"><i class="bi bi-search"></i></button>
                </form>
            </div>
            <div class="col-lg-5 d-none d-lg-block text-center">
                <i class="bi bi-file-earmark-richtext" style="font-size: 9rem; opacity: .25"></i>
            </div>
        </div>
    </div>
</section>

<section class="container accesos">
    <div class="row g-3">
        <div class="col-md-6 col-lg-3">
            <a href="${ctx}/orientacion" class="card tarjeta-acceso text-decoration-none text-reset">
                <div class="card-body">
                    <span class="icono-circulo mb-3"><i class="bi bi-signpost-split"></i></span>
                    <h5 class="card-title">No sé qué trámite es</h5>
                    <p class="card-text small text-secondary">Responde unas preguntas y te decimos qué trámite te corresponde.</p>
                </div>
            </a>
        </div>
        <div class="col-md-6 col-lg-3">
            <a href="${ctx}/tramites" class="card tarjeta-acceso text-decoration-none text-reset">
                <div class="card-body">
                    <span class="icono-circulo mb-3"><i class="bi bi-list-check"></i></span>
                    <h5 class="card-title">Catálogo de trámites</h5>
                    <p class="card-text small text-secondary">Requisitos, derecho de pago, plazos y oficina de cada trámite.</p>
                </div>
            </a>
        </div>
        <div class="col-md-6 col-lg-3">
            <a href="${ctx}/revision" class="card tarjeta-acceso text-decoration-none text-reset">
                <div class="card-body">
                    <span class="icono-circulo mb-3"><i class="bi bi-file-earmark-check"></i></span>
                    <h5 class="card-title">Revisar mi solicitud</h5>
                    <p class="card-text small text-secondary">Sube tu PDF y verifica DNI, correo, firma y pago antes de presentarla.</p>
                </div>
            </a>
        </div>
        <div class="col-md-6 col-lg-3">
            <a href="${ctx}/normativa" class="card tarjeta-acceso text-decoration-none text-reset">
                <div class="card-body">
                    <span class="icono-circulo mb-3"><i class="bi bi-journal-bookmark"></i></span>
                    <h5 class="card-title">Normativa</h5>
                    <p class="card-text small text-secondary">La norma vigente que sustenta cada requisito, con su fuente oficial.</p>
                </div>
            </a>
        </div>
    </div>
</section>

<section class="container mt-5">
    <h2 class="h4 titulo-seccion mb-3">Trámites por categoría</h2>
    <div class="row g-3">
        <c:forEach var="cat" items="${categorias}">
            <div class="col-md-6 col-lg-3">
                <a class="card tarjeta h-100 text-decoration-none text-reset" href="${ctx}/tramites?categoria=${cat.id}">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <i class="bi ${cat.icono} fs-3 text-primary"></i>
                            <span class="badge rounded-pill text-bg-light">${cat.totalTramites} trámites</span>
                        </div>
                        <h6 class="mt-2 mb-1"><c:out value="${cat.nombre}"/></h6>
                        <small class="text-secondary"><c:out value="${cat.descripcion}"/></small>
                    </div>
                </a>
            </div>
        </c:forEach>
    </div>

    <h2 class="h4 titulo-seccion mt-5 mb-3">Trámites más solicitados</h2>
    <div class="list-group tarjeta">
        <c:forEach var="t" items="${tramites}" end="5">
            <a class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3"
               href="${ctx}/hoja-ruta?id=${t.id}">
                <span><i class="bi bi-file-text text-primary me-2"></i><c:out value="${t.nombre}"/>
                    <small class="text-secondary d-block ms-4"><c:out value="${t.categoria}"/></small></span>
                <span class="text-end"><span class="badge text-bg-primary"><c:out value="${t.pagoTexto}"/></span>
                    <small class="d-block text-secondary"><c:out value="${t.plazoReferencial}"/></small></span>
            </a>
        </c:forEach>
    </div>
</section>

<%@ include file="fragmentos/pie.jspf" %>
