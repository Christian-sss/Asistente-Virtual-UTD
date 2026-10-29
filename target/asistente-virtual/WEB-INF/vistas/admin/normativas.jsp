<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Normativas"/>
<c:set var="menu" value="admin"/>
<c:set var="seccion" value="normativas"/>
<%@ include file="../fragmentos/cabecera.jspf" %>
<%@ include file="../fragmentos/admin-menu.jspf" %>

<div class="container mt-4">
    <div class="row g-4">
        <div class="col-lg-5">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <h2 class="h6 titulo-seccion mb-0">Normativas registradas</h2>
                <a class="btn btn-sm btn-primary" href="${ctx}/admin/normativas?nueva=1"><i class="bi bi-plus-lg"></i> Nueva</a>
            </div>
            <div class="list-group tarjeta">
                <c:forEach var="n" items="${normativas}">
                    <a class="list-group-item list-group-item-action ${editar.id == n.id ? 'active' : ''}" href="${ctx}/admin/normativas?id=${n.id}">
                        <div class="d-flex justify-content-between"><strong class="small"><c:out value="${n.tipo}"/> <c:out value="${n.numero}"/></strong>
                            <span class="badge ${n.vigente ? 'text-bg-success' : 'text-bg-danger'}">${n.vigente ? 'Vigente' : 'No vigente'}</span></div>
                        <small><c:out value="${n.titulo}"/></small>
                    </a>
                </c:forEach>
            </div>
        </div>
        <div class="col-lg-7">
            <c:choose>
                <c:when test="${empty editar}">
                    <div class="alert alert-info">Seleccione una normativa para editarla o registre una nueva.</div>
                </c:when>
                <c:otherwise>
                    <div class="card tarjeta mb-3">
                        <div class="card-body">
                            <h2 class="h6 titulo-seccion">${editar.id == 0 ? 'Nueva normativa' : 'Editar normativa'}</h2>
                            <form method="post" action="${ctx}/admin/normativas" class="row g-2">
                                <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="accion" value="guardar">
                                <c:if test="${editar.id != 0}"><input type="hidden" name="id" value="${editar.id}"></c:if>
                                <div class="col-md-5"><label class="form-label small">Tipo</label><input class="form-control form-control-sm" name="tipo" value="<c:out value='${editar.tipo}'/>" required placeholder="Ley, Resolución Rectoral..."></div>
                                <div class="col-md-7"><label class="form-label small">Número</label><input class="form-control form-control-sm" name="numero" value="<c:out value='${editar.numero}'/>" required></div>
                                <div class="col-12"><label class="form-label small">Título</label><input class="form-control form-control-sm" name="titulo" value="<c:out value='${editar.titulo}'/>" required></div>
                                <div class="col-md-7"><label class="form-label small">Órgano emisor</label><input class="form-control form-control-sm" name="organoEmisor" value="<c:out value='${editar.organoEmisor}'/>" required></div>
                                <div class="col-md-5"><label class="form-label small">Fecha de aprobación</label><input class="form-control form-control-sm" type="date" name="fechaAprobacion" value="${editar.fechaAprobacion}"></div>
                                <div class="col-md-9"><label class="form-label small">Fuente oficial (URL)</label><input class="form-control form-control-sm" name="fuenteOficial" value="<c:out value='${editar.fuenteOficial}'/>"></div>
                                <div class="col-md-3 d-flex align-items-end"><div class="form-check">
                                    <input class="form-check-input" type="checkbox" name="vigente" id="vig" ${editar.id == 0 or editar.vigente ? 'checked' : ''}>
                                    <label class="form-check-label small" for="vig">Vigente</label></div></div>
                                <div class="col-12"><button class="btn btn-sm btn-primary"><i class="bi bi-save"></i> Guardar</button>
                                    <c:if test="${editar.id != 0}"><small class="text-secondary ms-2">Última verificación: ${editar.fechaUltimaVerificacion}</small></c:if></div>
                            </form>
                        </div>
                    </div>
                    <c:if test="${editar.id != 0}">
                        <div class="card tarjeta">
                            <div class="card-body">
                                <h2 class="h6 titulo-seccion">Artículos</h2>
                                <c:forEach var="a" items="${editar.articulos}">
                                    <div class="border-bottom py-2 d-flex justify-content-between gap-2">
                                        <div class="small"><strong><c:out value="${a.numero}"/>:</strong> <c:out value="${a.contenido}"/>
                                            <c:if test="${not empty a.requisitosSustentados}"><br><span class="text-secondary">Sustenta ${a.requisitosSustentados.size()} requisito(s)</span></c:if></div>
                                        <form method="post" action="${ctx}/admin/normativas" onsubmit="return confirm('¿Eliminar el artículo? Se quitarán sus vínculos con requisitos.')">
                                            <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${editar.id}">
                                            <input type="hidden" name="idArticulo" value="${a.id}">
                                            <button class="btn btn-sm btn-outline-danger" name="accion" value="eliminarArticulo"><i class="bi bi-trash"></i></button>
                                        </form>
                                    </div>
                                </c:forEach>
                                <form method="post" action="${ctx}/admin/normativas" class="row g-2 mt-2">
                                    <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${editar.id}">
                                    <div class="col-md-3"><input class="form-control form-control-sm" name="numeroArticulo" placeholder="Art. 10" required></div>
                                    <div class="col-md-7"><input class="form-control form-control-sm" name="contenido" placeholder="Contenido del artículo" required></div>
                                    <div class="col-md-2 d-grid"><button class="btn btn-sm btn-outline-primary" name="accion" value="articulo"><i class="bi bi-plus"></i></button></div>
                                </form>
                            </div>
                        </div>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%@ include file="../fragmentos/pie.jspf" %>
