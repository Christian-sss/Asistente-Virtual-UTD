<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Categorías"/>
<c:set var="menu" value="admin"/>
<c:set var="seccion" value="categorias"/>
<%@ include file="../fragmentos/cabecera.jspf" %>
<%@ include file="../fragmentos/admin-menu.jspf" %>

<div class="container mt-4">
    <h2 class="h6 titulo-seccion">Categorías del catálogo de trámites</h2>
    <c:forEach var="cat" items="${categorias}">
        <form method="post" action="${ctx}/admin/categorias" id="f${cat.id}">
            <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${cat.id}">
        </form>
    </c:forEach>
    <div class="card tarjeta mb-4">
        <div class="table-responsive">
            <table class="table tabla-compacta mb-0 small align-middle">
                <thead class="table-light"><tr><th style="width:70px">Orden</th><th>Nombre</th><th>Descripción</th><th style="width:150px">Ícono</th><th>Trámites</th><th></th></tr></thead>
                <tbody>
                <c:forEach var="cat" items="${categorias}">
                    <tr>
                        <td><input class="form-control form-control-sm" form="f${cat.id}" type="number" name="orden" value="${cat.orden}" min="0" max="99"></td>
                        <td><input class="form-control form-control-sm" form="f${cat.id}" name="nombre" value="<c:out value='${cat.nombre}'/>" required maxlength="120"></td>
                        <td><input class="form-control form-control-sm" form="f${cat.id}" name="descripcion" value="<c:out value='${cat.descripcion}'/>" maxlength="300"></td>
                        <td><div class="input-group input-group-sm"><span class="input-group-text"><i class="bi ${cat.icono}"></i></span>
                            <input class="form-control" form="f${cat.id}" name="icono" value="<c:out value='${cat.icono}'/>" maxlength="40"></div></td>
                        <td class="text-center">${cat.totalTramites}</td>
                        <td class="text-end text-nowrap">
                            <button class="btn btn-sm btn-primary" form="f${cat.id}" name="accion" value="guardar" title="Guardar"><i class="bi bi-save"></i></button>
                            <button class="btn btn-sm btn-outline-danger" form="f${cat.id}" name="accion" value="eliminar" title="Eliminar"
                                    onclick="return confirm('¿Eliminar la categoría? Solo es posible si no tiene trámites.')"><i class="bi bi-trash"></i></button>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
    <p class="small text-secondary">La columna «Trámites» cuenta los trámites publicados. Una categoría solo se elimina si no tiene trámites.</p>

    <div class="card tarjeta">
        <div class="card-body">
            <h2 class="h6 titulo-seccion">Nueva categoría</h2>
            <form method="post" action="${ctx}/admin/categorias" class="row g-2">
                <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="accion" value="guardar">
                <div class="col-md-1"><input class="form-control form-control-sm" type="number" name="orden" value="${categorias.size() + 1}" min="0" max="99" aria-label="Orden"></div>
                <div class="col-md-3"><input class="form-control form-control-sm" name="nombre" placeholder="Nombre" required maxlength="120"></div>
                <div class="col-md-5"><input class="form-control form-control-sm" name="descripcion" placeholder="Descripción" maxlength="300"></div>
                <div class="col-md-2"><input class="form-control form-control-sm" name="icono" value="bi-folder" maxlength="40" aria-label="Ícono"></div>
                <div class="col-md-1 d-grid"><button class="btn btn-sm btn-outline-primary"><i class="bi bi-plus"></i></button></div>
            </form>
            <p class="form-text mb-0">El ícono es un nombre de <a href="https://icons.getbootstrap.com/" target="_blank" rel="noopener">Bootstrap Icons</a>, por ejemplo bi-mortarboard.</p>
        </div>
    </div>
</div>

<%@ include file="../fragmentos/pie.jspf" %>
