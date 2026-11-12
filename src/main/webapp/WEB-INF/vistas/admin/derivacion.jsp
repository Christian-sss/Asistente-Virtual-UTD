<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Reglas de derivación"/>
<c:set var="menu" value="admin"/>
<c:set var="seccion" value="derivacion"/>
<%@ include file="../fragmentos/cabecera.jspf" %>
<%@ include file="../fragmentos/admin-menu.jspf" %>

<div class="container mt-4">
    <h2 class="h6 titulo-seccion">Reglas de derivación a la dependencia competente</h2>
    <p class="small text-secondary">Cuando un usuario describe su consulta en «Expediente», el sistema busca estas palabras clave (sin distinguir tildes
        ni mayúsculas) y sugiere la dependencia con más coincidencias. Las frases de varias palabras pesan más.</p>

    <c:forEach var="r" items="${reglas}">
        <form method="post" action="${ctx}/admin/derivacion" class="card tarjeta mb-2">
            <div class="card-body row g-2 ${r.activa ? '' : 'opacity-75'}">
                <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${r.id}">
                <div class="col-md-4"><label class="form-label small mb-0">Tema</label>
                    <input class="form-control form-control-sm" name="tema" value="<c:out value='${r.tema}'/>" required maxlength="150"></div>
                <div class="col-md-5"><label class="form-label small mb-0">Dependencia competente</label>
                    <select class="form-select form-select-sm" name="idDependencia" required>
                        <c:forEach var="d" items="${dependencias}"><option value="${d.id}" ${r.idDependencia == d.id ? 'selected' : ''}><c:out value="${d.nombre}"/></option></c:forEach>
                    </select></div>
                <div class="col-md-3 d-flex align-items-end gap-2">
                    <div class="form-check mb-1"><input class="form-check-input" type="checkbox" name="activa" id="a${r.id}" ${r.activa ? 'checked' : ''}>
                        <label class="form-check-label small" for="a${r.id}">Activa</label></div>
                </div>
                <div class="col-12"><label class="form-label small mb-0">Palabras clave (separadas por comas)</label>
                    <input class="form-control form-control-sm" name="palabrasClave" value="<c:out value='${r.palabrasClave}'/>" required maxlength="400"></div>
                <div class="col-md-9"><label class="form-label small mb-0">Indicación al usuario</label>
                    <input class="form-control form-control-sm" name="indicacion" value="<c:out value='${r.indicacion}'/>" maxlength="400"></div>
                <div class="col-md-3 d-flex align-items-end justify-content-end gap-2">
                    <small class="${r.revisionPendiente ? 'text-danger' : 'text-secondary'}">${r.fechaUltimaVerificacion}</small>
                    <button class="btn btn-sm btn-primary" name="accion" value="regla"><i class="bi bi-save"></i></button>
                    <button class="btn btn-sm btn-outline-danger" name="accion" value="eliminarRegla" onclick="return confirm('¿Eliminar esta regla?')"><i class="bi bi-trash"></i></button>
                </div>
            </div>
        </form>
    </c:forEach>

    <form method="post" action="${ctx}/admin/derivacion" class="card tarjeta border border-primary mt-3">
        <div class="card-body row g-2">
            <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="activa" value="1">
            <div class="col-12 small fw-semibold">Nueva regla</div>
            <div class="col-md-4"><input class="form-control form-control-sm" name="tema" placeholder="Tema" required maxlength="150"></div>
            <div class="col-md-8"><select class="form-select form-select-sm" name="idDependencia" required>
                <option value="">Dependencia competente...</option>
                <c:forEach var="d" items="${dependencias}"><option value="${d.id}"><c:out value="${d.nombre}"/></option></c:forEach>
            </select></div>
            <div class="col-12"><input class="form-control form-control-sm" name="palabrasClave" placeholder="Palabras clave separadas por comas" required maxlength="400"></div>
            <div class="col-md-10"><input class="form-control form-control-sm" name="indicacion" placeholder="Indicación al usuario" maxlength="400"></div>
            <div class="col-md-2 d-grid"><button class="btn btn-sm btn-outline-primary" name="accion" value="regla"><i class="bi bi-plus"></i> Agregar</button></div>
        </div>
    </form>
</div>

<%@ include file="../fragmentos/pie.jspf" %>
