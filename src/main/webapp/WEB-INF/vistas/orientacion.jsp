<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Orientación guiada"/>
<c:set var="menu" value="orientacion"/>
<%@ include file="fragmentos/cabecera.jspf" %>

<div class="encabezado-pagina">
    <div class="container">
        <h1><i class="bi bi-signpost-split"></i> Orientación guiada</h1>
        <p class="text-secondary mb-0">Responde las preguntas y te indicaremos el trámite que corresponde a tu caso.</p>
    </div>
</div>

<div class="container mt-4" style="max-width: 760px">
    <c:if test="${not empty sessionScope.rutaOrientacion and not pregunta.inicial}">
        <nav aria-label="Respuestas anteriores" class="mb-3">
            <ol class="breadcrumb small">
                <li class="breadcrumb-item"><a href="${ctx}/orientacion">Inicio</a></li>
                <c:forEach var="r" items="${sessionScope.rutaOrientacion}">
                    <li class="breadcrumb-item"><c:out value="${r}"/></li>
                </c:forEach>
            </ol>
        </nav>
    </c:if>

    <div class="card tarjeta">
        <div class="card-body p-4">
            <h2 class="h4 mb-4"><c:out value="${pregunta.texto}"/></h2>
            <form method="post" action="${ctx}/orientacion">
                <input type="hidden" name="_csrf" value="${csrf}">
                <input type="hidden" name="pregunta" value="${pregunta.id}">
                <div class="d-grid gap-2">
                    <c:forEach var="o" items="${pregunta.opciones}">
                        <button class="opcion-guia" type="submit" name="opcion" value="${o.id}">
                            <i class="bi bi-chevron-right text-primary me-2"></i><c:out value="${o.texto}"/>
                        </button>
                    </c:forEach>
                </div>
            </form>
        </div>
    </div>
    <div class="d-flex justify-content-between mt-3 small">
        <a href="${ctx}/orientacion"><i class="bi bi-arrow-counterclockwise"></i> Empezar de nuevo</a>
        <a href="${ctx}/tramites">Prefiero ver el catálogo completo <i class="bi bi-arrow-right"></i></a>
    </div>
</div>

<%@ include file="fragmentos/pie.jspf" %>
