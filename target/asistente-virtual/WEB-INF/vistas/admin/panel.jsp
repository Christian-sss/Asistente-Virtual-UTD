<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Panel"/>
<c:set var="menu" value="admin"/>
<c:set var="seccion" value="panel"/>
<%@ include file="../fragmentos/cabecera.jspf" %>
<%@ include file="../fragmentos/admin-menu.jspf" %>

<div class="container mt-4">
    <div class="row g-3">
        <div class="col-6 col-lg-3"><div class="card tarjeta"><div class="card-body">
            <div class="cifra">${estadisticas[0]}</div><div class="text-secondary small">Trámites registrados</div></div></div></div>
        <div class="col-6 col-lg-3"><div class="card tarjeta"><div class="card-body">
            <div class="cifra">${estadisticas[1]}</div><div class="text-secondary small">Trámites publicados</div></div></div></div>
        <div class="col-6 col-lg-3"><div class="card tarjeta"><div class="card-body">
            <div class="cifra">${estadisticas[2]}</div><div class="text-secondary small">Normativas</div></div></div></div>
        <div class="col-6 col-lg-3"><div class="card tarjeta"><div class="card-body">
            <div class="cifra">${estadisticas[3]}</div><div class="text-secondary small">Preguntas guiadas</div></div></div></div>
    </div>

    <div class="row g-3 mt-1">
        <div class="col-lg-5">
            <div class="card tarjeta h-100">
                <div class="card-body">
                    <h2 class="h6 titulo-seccion"><i class="bi bi-trash3"></i> Eliminación automática de documentos (RF-17)</h2>
                    <p class="small mb-1">Documentos temporales en el servidor: <strong>${temporales}</strong></p>
                    <p class="small mb-1">Retención máxima: <strong>24 horas</strong>; la tarea se ejecuta <strong>cada hora</strong>.</p>
                    <p class="small text-secondary">Última ejecución: <c:out value="${empty ultimaLimpieza ? 'aún no se ha ejecutado' : ultimaLimpieza}"/></p>
                    <c:if test="${usr.rol == 'ADMIN_CONTENIDOS'}">
                        <form method="post" action="${ctx}/admin/limpieza">
                            <input type="hidden" name="_csrf" value="${csrf}">
                            <button class="btn btn-sm btn-outline-danger"><i class="bi bi-play-circle"></i> Ejecutar limpieza ahora</button>
                        </form>
                    </c:if>
                </div>
            </div>
        </div>
        <div class="col-lg-7">
            <div class="card tarjeta h-100">
                <div class="card-body">
                    <div class="d-flex justify-content-between">
                        <h2 class="h6 titulo-seccion"><i class="bi bi-clock-history"></i> Actividad reciente</h2>
                        <a class="small" href="${ctx}/admin/bitacora">Ver bitácora completa</a>
                    </div>
                    <table class="table table-sm small mb-0">
                        <tbody>
                        <c:forEach var="b" items="${bitacora}">
                            <tr><td class="text-nowrap">${b.fechaTexto}</td><td><c:out value="${b.usuario}"/></td>
                                <td><span class="badge text-bg-light"><c:out value="${b.accion}"/></span> <c:out value="${b.entidad}"/></td></tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <c:if test="${usr.rol == 'ADMIN_CONTENIDOS'}">
        <div class="d-flex flex-wrap gap-2 mt-4">
            <a class="btn btn-primary" href="${ctx}/admin/tramite"><i class="bi bi-plus-lg"></i> Nuevo trámite</a>
            <a class="btn btn-outline-primary" href="${ctx}/admin/preguntas"><i class="bi bi-diagram-3"></i> Editar preguntas guiadas</a>
            <a class="btn btn-outline-primary" href="${ctx}/admin/normativas"><i class="bi bi-journal-plus"></i> Gestionar normativas</a>
        </div>
    </c:if>
</div>

<%@ include file="../fragmentos/pie.jspf" %>
