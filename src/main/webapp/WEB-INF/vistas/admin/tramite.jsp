<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="${tramite.id == 0 ? 'Nuevo trámite' : tramite.nombre}"/>
<c:set var="menu" value="admin"/>
<c:set var="seccion" value="tramites"/>
<%@ include file="../fragmentos/cabecera.jspf" %>
<%@ include file="../fragmentos/admin-menu.jspf" %>

<div class="container mt-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <div>
            <a class="small" href="${ctx}/admin/tramites"><i class="bi bi-arrow-left"></i> Trámites</a>
            <h2 class="h4 titulo-seccion mb-0 mt-1"><c:out value="${tramite.id == 0 ? 'Nuevo trámite' : tramite.nombre}"/></h2>
        </div>
        <c:if test="${tramite.id != 0}">
            <form method="post" action="${ctx}/admin/tramite" class="d-flex align-items-center gap-2">
                <input type="hidden" name="_csrf" value="${csrf}">
                <input type="hidden" name="id" value="${tramite.id}">
                <span class="badge ${tramite.publicado ? 'text-bg-success' : 'text-bg-secondary'} fs-6">${tramite.publicado ? 'Publicado' : 'Borrador'}</span>
                <c:choose>
                    <c:when test="${tramite.publicado}"><button class="btn btn-outline-secondary btn-sm" name="accion" value="despublicar"><i class="bi bi-eye-slash"></i> Despublicar</button></c:when>
                    <c:otherwise><button class="btn btn-success btn-sm" name="accion" value="publicar"><i class="bi bi-upload"></i> Publicar</button></c:otherwise>
                </c:choose>
            </form>
        </c:if>
    </div>

    <!-- Datos del trámite -->
    <div class="card tarjeta mb-4">
        <div class="card-body">
            <h3 class="h6 titulo-seccion">Datos generales</h3>
            <form method="post" action="${ctx}/admin/tramite" class="row g-3">
                <input type="hidden" name="_csrf" value="${csrf}">
                <input type="hidden" name="accion" value="guardar">
                <c:if test="${tramite.id != 0}"><input type="hidden" name="id" value="${tramite.id}"></c:if>
                <div class="col-md-2"><label class="form-label small">Código</label>
                    <input class="form-control" name="codigo" value="<c:out value='${tramite.codigo}'/>" required maxlength="20" placeholder="TR-016"></div>
                <div class="col-md-2"><label class="form-label small">Código TUPA</label>
                    <input class="form-control" name="codigoTupa" value="<c:out value='${tramite.codigoTupa}'/>" maxlength="20"></div>
                <div class="col-md-8"><label class="form-label small">Nombre</label>
                    <input class="form-control" name="nombre" value="<c:out value='${tramite.nombre}'/>" required maxlength="200"></div>
                <div class="col-12"><label class="form-label small">Descripción en lenguaje claro</label>
                    <textarea class="form-control" name="descripcion" rows="2"><c:out value="${tramite.descripcion}"/></textarea></div>
                <div class="col-md-4"><label class="form-label small">Categoría</label>
                    <select class="form-select" name="idCategoria" required>
                        <c:forEach var="cat" items="${categorias}">
                            <option value="${cat.id}" ${tramite.idCategoria == cat.id ? 'selected' : ''}><c:out value="${cat.nombre}"/></option>
                        </c:forEach>
                    </select></div>
                <div class="col-md-8"><label class="form-label small">Punto de atención</label>
                    <select class="form-select" name="idPunto">
                        <option value="">(Sin asignar)</option>
                        <c:forEach var="p" items="${puntos}">
                            <option value="${p.id}" ${tramite.idPunto == p.id ? 'selected' : ''}><c:out value="${p.nombre}"/> - <c:out value="${p.sede}"/></option>
                        </c:forEach>
                    </select></div>
                <div class="col-md-4"><label class="form-label small">Solicitud dirigida a</label>
                    <input class="form-control" name="dirigidoA" value="<c:out value='${tramite.dirigidoA}'/>"></div>
                <div class="col-md-2"><label class="form-label small">Derecho (S/)</label>
                    <input class="form-control" name="derechoPago" type="number" step="0.01" min="0" value="${tramite.derechoPago}"></div>
                <div class="col-md-6"><label class="form-label small">Detalle del pago (opcional)</label>
                    <input class="form-control" name="detallePago" value="<c:out value='${tramite.detallePago}'/>"></div>
                <div class="col-md-4"><label class="form-label small">Plazo referencial</label>
                    <input class="form-control" name="plazoReferencial" value="<c:out value='${tramite.plazoReferencial}'/>"></div>
                <div class="col-md-8"><label class="form-label small">Calificación del procedimiento</label>
                    <input class="form-control" name="calificacion" value="<c:out value='${tramite.calificacion}'/>"></div>
                <div class="col-12"><button class="btn btn-primary"><i class="bi bi-save"></i> Guardar datos</button>
                    <c:if test="${tramite.id != 0}"><small class="text-secondary ms-2">Última verificación: ${tramite.fechaUltimaVerificacion}</small></c:if></div>
            </form>
        </div>
    </div>

    <c:if test="${tramite.id != 0}">
        <!-- Pasos -->
        <div class="card tarjeta mb-4" id="pasos">
            <div class="card-body">
                <h3 class="h6 titulo-seccion">Pasos de la hoja de ruta</h3>
                <c:forEach var="p" items="${tramite.pasos}">
                    <div class="paso align-items-center">
                        <div class="paso-numero">${p.orden}</div>
                        <div class="flex-grow-1 small"><c:out value="${p.descripcion}"/>
                            <c:if test="${p.pagoPrevio}"><span class="badge text-bg-warning">Pago previo</span></c:if></div>
                        <form method="post" action="${ctx}/admin/tramite" onsubmit="return confirm('¿Eliminar este paso?')">
                            <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${tramite.id}">
                            <input type="hidden" name="idPaso" value="${p.id}">
                            <button class="btn btn-sm btn-outline-danger" name="accion" value="eliminarPaso"><i class="bi bi-trash"></i></button>
                        </form>
                    </div>
                </c:forEach>
                <form method="post" action="${ctx}/admin/tramite" class="row g-2 mt-2">
                    <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${tramite.id}">
                    <div class="col-md-8"><input class="form-control form-control-sm" name="descripcion" placeholder="Nuevo paso" required></div>
                    <div class="col-md-2 d-flex align-items-center"><div class="form-check">
                        <input class="form-check-input" type="checkbox" name="pagoPrevio" id="pp"><label class="form-check-label small" for="pp">Pago previo</label></div></div>
                    <div class="col-md-2 d-grid"><button class="btn btn-sm btn-outline-primary" name="accion" value="paso"><i class="bi bi-plus"></i> Agregar</button></div>
                </form>
            </div>
        </div>

        <!-- Requisitos con sustento normativo -->
        <div class="card tarjeta mb-4" id="requisitos">
            <div class="card-body">
                <h3 class="h6 titulo-seccion">Requisitos y sustento normativo</h3>
                <c:forEach var="r" items="${tramite.requisitos}">
                    <div class="border rounded p-2 mb-2">
                        <div class="d-flex justify-content-between gap-2">
                            <div class="small"><strong>${r.orden}.</strong> <c:out value="${r.descripcion}"/>
                                <span class="badge ${r.obligatorio ? 'text-bg-primary' : 'text-bg-light'}">${r.obligatorio ? 'Obligatorio' : 'Según corresponda'}</span></div>
                            <form method="post" action="${ctx}/admin/tramite" onsubmit="return confirm('¿Eliminar este requisito?')">
                                <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${tramite.id}">
                                <input type="hidden" name="idRequisito" value="${r.id}">
                                <button class="btn btn-sm btn-outline-danger" name="accion" value="eliminarRequisito"><i class="bi bi-trash"></i></button>
                            </form>
                        </div>
                        <div class="d-flex flex-wrap gap-1 mt-1">
                            <c:forEach var="a" items="${r.articulos}">
                                <form method="post" action="${ctx}/admin/tramite" class="d-inline">
                                    <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${tramite.id}">
                                    <input type="hidden" name="idRequisito" value="${r.id}"><input type="hidden" name="idArticulo" value="${a.id}">
                                    <span class="badge text-bg-light border"><i class="bi bi-bookmark"></i> <c:out value="${a.numero}"/>
                                        <button class="btn btn-link btn-sm p-0 ms-1 text-danger" name="accion" value="desvincular" title="Quitar vínculo"><i class="bi bi-x"></i></button></span>
                                </form>
                            </c:forEach>
                            <form method="post" action="${ctx}/admin/tramite" class="d-inline-flex gap-1">
                                <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${tramite.id}">
                                <input type="hidden" name="idRequisito" value="${r.id}">
                                <select class="form-select form-select-sm" name="idArticulo" style="max-width: 340px" required>
                                    <option value="">Vincular artículo...</option>
                                    <c:forEach var="a" items="${articulos}"><option value="${a.id}"><c:out value="${a.normativa}"/> - <c:out value="${a.numero}"/></option></c:forEach>
                                </select>
                                <button class="btn btn-sm btn-outline-secondary" name="accion" value="vincular"><i class="bi bi-link-45deg"></i></button>
                            </form>
                        </div>
                    </div>
                </c:forEach>
                <form method="post" action="${ctx}/admin/tramite" class="row g-2 mt-2">
                    <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${tramite.id}">
                    <div class="col-md-5"><input class="form-control form-control-sm" name="descripcion" placeholder="Nuevo requisito" required></div>
                    <div class="col-md-4"><select class="form-select form-select-sm" name="idArticulo">
                        <option value="">Artículo que lo sustenta...</option>
                        <c:forEach var="a" items="${articulos}"><option value="${a.id}"><c:out value="${a.normativa}"/> - <c:out value="${a.numero}"/></option></c:forEach>
                    </select></div>
                    <div class="col-md-1 d-flex align-items-center"><div class="form-check">
                        <input class="form-check-input" type="checkbox" name="obligatorio" id="ob" checked><label class="form-check-label small" for="ob">Oblig.</label></div></div>
                    <div class="col-md-2 d-grid"><button class="btn btn-sm btn-outline-primary" name="accion" value="requisito"><i class="bi bi-plus"></i> Agregar</button></div>
                </form>
            </div>
        </div>

        <!-- Reglas de revisión -->
        <div class="card tarjeta mb-4" id="reglas">
            <div class="card-body">
                <h3 class="h6 titulo-seccion">Reglas de revisión formal</h3>
                <p class="small text-secondary">Se aplican en la siguiente revisión, sin recompilar. «Dato obligatorio» busca la expresión regular en el texto; «Confirmación asistida» la confirma el usuario.</p>
                <div class="table-responsive">
                    <table class="table table-sm tabla-compacta small">
                        <thead><tr><th>Criterio</th><th>Tipo</th><th>Expresión</th><th>Mensaje de corrección</th><th></th></tr></thead>
                        <tbody>
                        <c:forEach var="r" items="${reglas}">
                            <tr class="${r.activa ? '' : 'text-secondary'}">
                                <td><c:out value="${r.criterio}"/></td>
                                <td><span class="badge text-bg-light">${r.tipo == 'DATO_OBLIGATORIO' ? 'Dato obligatorio' : 'Confirmación'}</span></td>
                                <td><code class="regex"><c:out value="${r.expresion}"/></code></td>
                                <td><c:out value="${r.mensajeCorreccion}"/></td>
                                <td class="text-nowrap">
                                    <form method="post" action="${ctx}/admin/tramite" class="d-inline">
                                        <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${tramite.id}">
                                        <input type="hidden" name="idRegla" value="${r.id}">
                                        <button class="btn btn-sm btn-outline-secondary" name="accion" value="${r.activa ? 'desactivarRegla' : 'activarRegla'}"
                                                title="${r.activa ? 'Desactivar' : 'Activar'}"><i class="bi ${r.activa ? 'bi-toggle-on' : 'bi-toggle-off'}"></i></button>
                                        <button class="btn btn-sm btn-outline-danger" name="accion" value="eliminarRegla" onclick="return confirm('¿Eliminar esta regla?')"><i class="bi bi-trash"></i></button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
                <form method="post" action="${ctx}/admin/tramite" class="row g-2">
                    <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${tramite.id}">
                    <div class="col-md-3"><input class="form-control form-control-sm" name="criterio" placeholder="Criterio" required></div>
                    <div class="col-md-2"><select class="form-select form-select-sm" name="tipo">
                        <option value="DATO_OBLIGATORIO">Dato obligatorio</option><option value="CONFIRMACION_ASISTIDA">Confirmación asistida</option></select></div>
                    <div class="col-md-2"><input class="form-control form-control-sm" name="expresion" placeholder="Expresión regular"></div>
                    <div class="col-md-3"><input class="form-control form-control-sm" name="mensajeCorreccion" placeholder="Mensaje de corrección" required></div>
                    <div class="col-md-2 d-grid"><button class="btn btn-sm btn-outline-primary" name="accion" value="regla"><i class="bi bi-plus"></i> Agregar regla</button></div>
                </form>
            </div>
        </div>
    </c:if>
</div>

<%@ include file="../fragmentos/pie.jspf" %>
