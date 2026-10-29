<%@ page pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Aviso"/>
<c:set var="codigo" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<%@ include file="fragmentos/cabecera.jspf" %>

<div class="container my-5 text-center" style="max-width: 620px">
    <span class="icono-circulo mb-3"><i class="bi ${codigo == 403 ? 'bi-shield-x' : (codigo == 404 ? 'bi-signpost' : 'bi-exclamation-octagon')}"></i></span>
    <h1 class="h3 titulo-seccion">
        <c:choose>
            <c:when test="${codigo == 403}">Acceso no permitido</c:when>
            <c:when test="${codigo == 404}">No encontramos lo que busca</c:when>
            <c:otherwise>Ocurrió un problema</c:otherwise>
        </c:choose>
    </h1>
    <p class="text-secondary">
        <c:choose>
            <c:when test="${codigo == 403 or codigo == 404}"><c:out value="${requestScope['jakarta.servlet.error.message']}"/></c:when>
            <c:otherwise>No pudimos completar la operación. Intente nuevamente en unos minutos.</c:otherwise>
        </c:choose>
    </p>
    <a class="btn btn-primary" href="${ctx}/"><i class="bi bi-house"></i> Volver al inicio</a>
</div>

<%@ include file="fragmentos/pie.jspf" %>
