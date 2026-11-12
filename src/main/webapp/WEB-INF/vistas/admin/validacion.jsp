<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Validación de contenidos"/>
<c:set var="menu" value="admin"/>
<c:set var="seccion" value="validacion"/>
<%@ include file="../fragmentos/cabecera.jspf" %>
<%@ include file="../fragmentos/admin-menu.jspf" %>

<div class="container mt-4">
    <div class="row g-4">
        <div class="col-lg-4">
            <h2 class="h6 titulo-seccion">Pendientes de validación (${pendientes.size()})</h2>
            <div class="list-group tarjeta mb-4">
                <c:forEach var="t" items="${pendientes}">
                    <a class="list-group-item list-group-item-action ${comparacion.propuesta().id == t.id ? 'active' : ''}" href="${ctx}/admin/validacion?id=${t.id}">
                        <strong class="small"><c:out value="${t.codigo}"/></strong> <small><c:out value="${t.nombre}"/></small><br>
                        <span class="badge ${t.publicado ? 'text-bg-info' : 'text-bg-secondary'}">${t.publicado ? 'Cambios a un trámite publicado' : 'Trámite nuevo o retirado'}</span>
                    </a>
                </c:forEach>
                <c:if test="${empty pendientes}"><div class="list-group-item small text-secondary">No hay contenidos pendientes.</div></c:if>
            </div>
            <h2 class="h6 titulo-seccion" id="devueltos">Devueltos con observaciones (${devueltos.size()})</h2>
            <div class="list-group tarjeta">
                <c:forEach var="t" items="${devueltos}">
                    <a class="list-group-item list-group-item-action small" href="${ctx}/admin/validacion?id=${t.id}">
                        <strong><c:out value="${t.codigo}"/></strong> <c:out value="${t.nombre}"/><br>
                        <span class="text-secondary"><c:out value="${t.observacionValidacion}"/></span></a>
                </c:forEach>
                <c:if test="${empty devueltos}"><div class="list-group-item small text-secondary">Ninguno.</div></c:if>
            </div>
        </div>

        <div class="col-lg-8">
            <c:choose>
                <c:when test="${empty comparacion}">
                    <div class="alert alert-info">Seleccione un trámite pendiente para revisarlo. Al aprobarlo se publica la versión propuesta;
                        al devolverlo, el administrador de contenidos recibe sus observaciones y el público sigue viendo la versión anterior.</div>
                </c:when>
                <c:otherwise>
                    <c:set var="t" value="${comparacion.propuesta()}"/>
                    <c:set var="pub" value="${comparacion.publicada()}"/>
                    <div class="card tarjeta mb-3">
                        <div class="card-body">
                            <div class="d-flex justify-content-between flex-wrap gap-2">
                                <div><small class="text-secondary"><c:out value="${t.codigo}"/> &middot; <c:out value="${t.categoria}"/></small>
                                    <h2 class="h5 mb-0"><c:out value="${t.nombre}"/></h2></div>
                                <span class="badge ${t.pendiente ? 'text-bg-warning' : t.devuelto ? 'text-bg-danger' : 'text-bg-success'} align-self-start"><c:out value="${t.estadoTexto}"/></span>
                            </div>
                            <c:if test="${not empty comparacion.incumplimientos()}">
                                <div class="alert alert-danger small mt-3 mb-0"><strong>No cumple las condiciones para publicarse:</strong>
                                    <ul class="mb-0"><c:forEach var="f" items="${comparacion.incumplimientos()}"><li><c:out value="${f}"/></li></c:forEach></ul></div>
                            </c:if>
                        </div>
                    </div>

                    <div class="row g-3 mb-3">
                        <div class="col-md-6">
                            <div class="card tarjeta h-100 border border-warning">
                                <div class="card-body small">
                                    <h3 class="h6 titulo-seccion">Versión propuesta</h3>
                                    <dl class="mb-2">
                                        <dt>Descripción</dt><dd><c:out value="${t.descripcion}"/></dd>
                                        <dt>Derecho de trámite</dt><dd><c:out value="${t.pagoTexto}"/></dd>
                                        <dt>Plazo</dt><dd><c:out value="${t.plazoReferencial}"/></dd>
                                        <dt>Oficina</dt><dd><c:out value="${t.punto.nombre}"/></dd>
                                    </dl>
                                    <strong>Pasos (${t.pasos.size()})</strong>
                                    <ol class="ps-3"><c:forEach var="p" items="${t.pasos}"><li><c:out value="${p.descripcion}"/><c:if test="${not empty p.punto}"> <span class="text-secondary">[<c:out value="${p.punto.nombre}"/>]</span></c:if></li></c:forEach></ol>
                                    <strong>Requisitos (${t.requisitos.size()})</strong>
                                    <ul class="ps-3 mb-0"><c:forEach var="r" items="${t.requisitos}"><li><c:out value="${r.descripcion}"/>
                                        <c:forEach var="a" items="${r.articulos}"><br><span class="text-secondary"><i class="bi bi-bookmark"></i> <c:out value="${a.normativa}"/>, <c:out value="${a.numero}"/></span></c:forEach>
                                        <c:if test="${r.obligatorio and empty r.articulos}"><span class="badge text-bg-danger">Sin sustento</span></c:if></li></c:forEach></ul>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="card tarjeta h-100">
                                <div class="card-body small">
                                    <h3 class="h6 titulo-seccion">Versión publicada actualmente</h3>
                                    <c:choose>
                                        <c:when test="${empty pub}"><p class="text-secondary">El trámite no está publicado: si lo aprueba, se publicará por primera vez.</p></c:when>
                                        <c:otherwise>
                                            <dl class="mb-2">
                                                <dt>Descripción</dt><dd class="${pub.descripcion != t.descripcion ? 'bg-warning-subtle' : ''}"><c:out value="${pub.descripcion}"/></dd>
                                                <dt>Derecho de trámite</dt><dd class="${pub.pagoTexto != t.pagoTexto ? 'bg-warning-subtle' : ''}"><c:out value="${pub.pagoTexto}"/></dd>
                                                <dt>Plazo</dt><dd class="${pub.plazoReferencial != t.plazoReferencial ? 'bg-warning-subtle' : ''}"><c:out value="${pub.plazoReferencial}"/></dd>
                                                <dt>Oficina</dt><dd><c:out value="${pub.punto.nombre}"/></dd>
                                            </dl>
                                            <strong>Pasos (${pub.pasos.size()})</strong>
                                            <ol class="ps-3"><c:forEach var="p" items="${pub.pasos}"><li><c:out value="${p.descripcion}"/></li></c:forEach></ol>
                                            <strong>Requisitos (${pub.requisitos.size()})</strong>
                                            <ul class="ps-3 mb-0"><c:forEach var="r" items="${pub.requisitos}"><li><c:out value="${r.descripcion}"/></li></c:forEach></ul>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </div>

                    <c:if test="${usr.rol == 'PERSONAL_UTD' and t.pendiente}">
                        <div class="card tarjeta">
                            <div class="card-body">
                                <h3 class="h6 titulo-seccion">Decisión</h3>
                                <form method="post" action="${ctx}/admin/validacion" class="mb-3">
                                    <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${t.id}">
                                    <button class="btn btn-success" name="accion" value="aprobar" ${empty comparacion.incumplimientos() ? '' : 'disabled'}
                                            onclick="return confirm('¿Aprobar y publicar esta versión?')"><i class="bi bi-patch-check"></i> Aprobar y publicar</button>
                                </form>
                                <form method="post" action="${ctx}/admin/validacion">
                                    <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${t.id}">
                                    <label class="form-label small" for="obs">Observaciones para el administrador de contenidos</label>
                                    <textarea class="form-control mb-2" id="obs" name="observacion" rows="3" maxlength="600" required></textarea>
                                    <button class="btn btn-outline-danger" name="accion" value="devolver"><i class="bi bi-arrow-return-left"></i> Devolver con observaciones</button>
                                </form>
                            </div>
                        </div>
                    </c:if>
                    <c:if test="${usr.rol == 'ADMIN_CONTENIDOS'}">
                        <div class="alert alert-secondary small">Solo el personal de la UTD (rol PERSONAL_UTD) puede aprobar o devolver contenidos.</div>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%@ include file="../fragmentos/pie.jspf" %>
