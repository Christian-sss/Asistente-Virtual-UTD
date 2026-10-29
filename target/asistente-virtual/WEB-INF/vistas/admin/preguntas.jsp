<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Preguntas guiadas"/>
<c:set var="menu" value="admin"/>
<c:set var="seccion" value="preguntas"/>
<%@ include file="../fragmentos/cabecera.jspf" %>
<%@ include file="../fragmentos/admin-menu.jspf" %>

<div class="container mt-4">
    <p class="text-secondary small">Árbol de decisión de la orientación guiada: cada opción conduce a otra pregunta o a un trámite.</p>
    <c:forEach var="p" items="${preguntas}">
        <div class="card tarjeta mb-3" id="p${p.id}">
            <div class="card-body">
                <form method="post" action="${ctx}/admin/preguntas" class="d-flex gap-2 mb-2">
                    <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="idPregunta" value="${p.id}">
                    <c:if test="${p.inicial}"><span class="badge text-bg-warning align-self-center">Inicial</span></c:if>
                    <input class="form-control fw-semibold" name="texto" value="<c:out value='${p.texto}'/>" required>
                    <button class="btn btn-outline-primary" name="accion" value="pregunta" title="Guardar"><i class="bi bi-save"></i></button>
                    <c:if test="${not p.inicial}">
                        <button class="btn btn-outline-danger" name="accion" value="eliminarPregunta" title="Eliminar"
                                onclick="return confirm('¿Eliminar la pregunta y sus opciones?')"><i class="bi bi-trash"></i></button>
                    </c:if>
                </form>
                <ul class="list-group list-group-flush small">
                    <c:forEach var="o" items="${p.opciones}">
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <span><i class="bi bi-arrow-return-right text-primary"></i> <c:out value="${o.texto}"/>
                                <span class="text-secondary">&rarr; <c:out value="${o.destino}"/></span></span>
                            <form method="post" action="${ctx}/admin/preguntas">
                                <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="idPregunta" value="${p.id}">
                                <input type="hidden" name="idOpcion" value="${o.id}">
                                <button class="btn btn-sm btn-outline-danger" name="accion" value="eliminarOpcion"><i class="bi bi-x"></i></button>
                            </form>
                        </li>
                    </c:forEach>
                </ul>
                <form method="post" action="${ctx}/admin/preguntas" class="row g-2 mt-2">
                    <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="idPregunta" value="${p.id}">
                    <div class="col-md-5"><input class="form-control form-control-sm" name="texto" placeholder="Nueva opción" required></div>
                    <div class="col-md-5"><select class="form-select form-select-sm" name="destino" required>
                        <option value="">Conduce a...</option>
                        <optgroup label="Otra pregunta">
                            <c:forEach var="q" items="${preguntas}"><c:if test="${q.id != p.id}"><option value="P${q.id}"><c:out value="${q.texto}"/></option></c:if></c:forEach>
                        </optgroup>
                        <optgroup label="Trámite">
                            <c:forEach var="t" items="${tramites}"><option value="T${t.id}"><c:out value="${t.nombre}"/></option></c:forEach>
                        </optgroup>
                    </select></div>
                    <div class="col-md-2 d-grid"><button class="btn btn-sm btn-outline-primary" name="accion" value="opcion"><i class="bi bi-plus"></i> Opción</button></div>
                </form>
            </div>
        </div>
    </c:forEach>

    <div class="card tarjeta">
        <div class="card-body">
            <h2 class="h6 titulo-seccion">Nueva pregunta</h2>
            <form method="post" action="${ctx}/admin/preguntas" class="d-flex gap-2">
                <input type="hidden" name="_csrf" value="${csrf}">
                <input class="form-control" name="texto" placeholder="Texto de la pregunta" required>
                <button class="btn btn-primary" name="accion" value="pregunta"><i class="bi bi-plus-lg"></i> Agregar</button>
            </form>
        </div>
    </div>
</div>

<%@ include file="../fragmentos/pie.jspf" %>
