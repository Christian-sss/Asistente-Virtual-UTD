<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Ubicaciones y canales"/>
<c:set var="menu" value="admin"/>
<c:set var="seccion" value="ubicaciones"/>
<%@ include file="../fragmentos/cabecera.jspf" %>
<%@ include file="../fragmentos/admin-menu.jspf" %>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.css">

<div class="container mt-4">
    <div class="row g-4">
        <div class="col-lg-4">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <h2 class="h6 titulo-seccion mb-0">Dependencias</h2>
                <a class="btn btn-sm btn-primary" href="${ctx}/admin/ubicaciones?dependencia=0"><i class="bi bi-plus-lg"></i> Nueva</a>
            </div>
            <div class="list-group tarjeta">
                <c:forEach var="d" items="${dependencias}">
                    <a class="list-group-item list-group-item-action ${dependenciaSel == d.id ? 'active' : ''}" href="${ctx}/admin/ubicaciones?dependencia=${d.id}">
                        <div class="d-flex justify-content-between"><strong class="small"><c:out value="${d.nombre}"/></strong>
                            <span class="badge text-bg-light">${d.puntos.size()} punto(s) &middot; ${d.canales.size()} canal(es)</span></div>
                    </a>
                </c:forEach>
            </div>
            <p class="small text-secondary mt-2">Cada punto y canal guarda la fecha de su última verificación; si supera un semestre se marca para revisión.</p>
        </div>

        <div class="col-lg-8">
            <c:set var="dep" value="${null}"/>
            <c:forEach var="d" items="${dependencias}"><c:if test="${d.id == dependenciaSel}"><c:set var="dep" value="${d}"/></c:if></c:forEach>
            <c:choose>
                <c:when test="${empty dependenciaSel}">
                    <div class="alert alert-info">Seleccione una dependencia para mantener sus puntos de atención y canales digitales, o registre una nueva.</div>
                </c:when>
                <c:otherwise>
                    <!-- Dependencia -->
                    <div class="card tarjeta mb-3">
                        <div class="card-body">
                            <h2 class="h6 titulo-seccion">${empty dep ? 'Nueva dependencia' : 'Dependencia'}</h2>
                            <form method="post" action="${ctx}/admin/ubicaciones" class="row g-2">
                                <input type="hidden" name="_csrf" value="${csrf}">
                                <c:if test="${not empty dep}"><input type="hidden" name="id" value="${dep.id}"><input type="hidden" name="idDependencia" value="${dep.id}"></c:if>
                                <div class="col-md-9"><input class="form-control form-control-sm" name="nombre" value="<c:out value='${dep.nombre}'/>" placeholder="Nombre" required maxlength="150"></div>
                                <div class="col-md-3"><input class="form-control form-control-sm" name="sigla" value="<c:out value='${dep.sigla}'/>" placeholder="Sigla" maxlength="20"></div>
                                <div class="col-12"><input class="form-control form-control-sm" name="funciones" value="<c:out value='${dep.funciones}'/>" placeholder="Funciones o temas que atiende (se muestra al derivar)" maxlength="400"></div>
                                <div class="col-12 d-flex gap-2">
                                    <button class="btn btn-sm btn-primary" name="accion" value="dependencia"><i class="bi bi-save"></i> Guardar</button>
                                    <c:if test="${not empty dep}"><button class="btn btn-sm btn-outline-danger" name="accion" value="eliminarDependencia"
                                        onclick="return confirm('¿Eliminar la dependencia? Solo es posible si no tiene puntos de atención.')"><i class="bi bi-trash"></i> Eliminar</button></c:if>
                                </div>
                            </form>
                        </div>
                    </div>

                    <c:if test="${not empty dep}">
                        <!-- Puntos de atención -->
                        <div class="card tarjeta mb-3">
                            <div class="card-body">
                                <h2 class="h6 titulo-seccion">Puntos de atención</h2>
                                <p class="small text-secondary">Haga clic en el formulario del punto que quiere ubicar (queda resaltado) y luego
                                    haga clic en el mapa sobre su edificio: se llenan la latitud y la longitud. Después pulse <i class="bi bi-save"></i> para guardar.
                                    Use el botón de capas para cambiar a la vista satelital y reconocer los edificios.</p>
                                <div id="planoAdmin" class="plano-campus mb-3" style="height: 420px"></div>
                                <c:forEach var="p" items="${dep.puntos}">
                                    <form method="post" action="${ctx}/admin/ubicaciones" class="row g-2 border rounded p-2 mb-2 form-punto">
                                        <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${p.id}"><input type="hidden" name="idDependencia" value="${dep.id}">
                                        <div class="col-md-6"><input class="form-control form-control-sm" name="nombre" value="<c:out value='${p.nombre}'/>" required maxlength="150" aria-label="Nombre"></div>
                                        <div class="col-md-6"><input class="form-control form-control-sm" name="referencia" value="<c:out value='${p.referencia}'/>" placeholder="Pabellón, piso o ventanilla" maxlength="200"></div>
                                        <div class="col-md-4"><input class="form-control form-control-sm" name="sede" value="<c:out value='${p.sede}'/>" required maxlength="80" aria-label="Sede"></div>
                                        <div class="col-md-8"><input class="form-control form-control-sm" name="direccion" value="<c:out value='${p.direccion}'/>" required maxlength="150" aria-label="Dirección"></div>
                                        <div class="col-md-6"><input class="form-control form-control-sm" name="horario" value="<c:out value='${p.horario}'/>" required maxlength="120" aria-label="Horario"></div>
                                        <div class="col-md-6"><input class="form-control form-control-sm" name="telefono" value="<c:out value='${p.telefono}'/>" placeholder="Teléfono" maxlength="200"></div>
                                        <div class="col-12"><input class="form-control form-control-sm" name="correo" value="<c:out value='${p.correo}'/>" placeholder="Correo" maxlength="400"></div>
                                        <div class="col-6 col-md-3"><input class="form-control form-control-sm coord-lat" name="latitud" value="${p.latitud}" placeholder="Latitud" type="number" step="0.000001" min="-90" max="90" aria-label="Latitud" title="Latitud"></div>
                                        <div class="col-6 col-md-3"><input class="form-control form-control-sm coord-lon" name="longitud" value="${p.longitud}" placeholder="Longitud" type="number" step="0.000001" min="-180" max="180" aria-label="Longitud" title="Longitud"></div>
                                        <div class="col-md-6 d-flex gap-2 align-items-center justify-content-end">
                                            <small class="${p.revisionPendiente ? 'text-danger' : 'text-secondary'}">Verificado: ${p.fechaUltimaVerificacion}</small>
                                            <button class="btn btn-sm btn-primary" name="accion" value="punto" title="Guardar y marcar como verificado"><i class="bi bi-save"></i></button>
                                            <button class="btn btn-sm btn-outline-danger" name="accion" value="eliminarPunto" onclick="return confirm('¿Eliminar este punto de atención?')"><i class="bi bi-trash"></i></button>
                                        </div>
                                    </form>
                                </c:forEach>
                                <form method="post" action="${ctx}/admin/ubicaciones" class="row g-2 border border-primary rounded p-2 form-punto">
                                    <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="idDependencia" value="${dep.id}">
                                    <div class="col-12 small fw-semibold">Nuevo punto de atención</div>
                                    <div class="col-md-6"><input class="form-control form-control-sm" name="nombre" placeholder="Nombre" required maxlength="150"></div>
                                    <div class="col-md-6"><input class="form-control form-control-sm" name="referencia" placeholder="Pabellón, piso o ventanilla" maxlength="200"></div>
                                    <div class="col-md-4"><input class="form-control form-control-sm" name="sede" value="Sede Los Granados" required maxlength="80"></div>
                                    <div class="col-md-8"><input class="form-control form-control-sm" name="direccion" value="Av. Miraflores y Av. Cuzco S/N, Tacna" required maxlength="150"></div>
                                    <div class="col-md-6"><input class="form-control form-control-sm" name="horario" value="Lunes a viernes de 08:30 a 15:00" required maxlength="120"></div>
                                    <div class="col-md-6"><input class="form-control form-control-sm" name="telefono" placeholder="Teléfono" maxlength="200"></div>
                                    <div class="col-12"><input class="form-control form-control-sm" name="correo" placeholder="Correo" maxlength="400"></div>
                                    <div class="col-6 col-md-3"><input class="form-control form-control-sm coord-lat" name="latitud" placeholder="Latitud" type="number" step="0.000001" min="-90" max="90" aria-label="Latitud" title="Latitud"></div>
                                    <div class="col-6 col-md-3"><input class="form-control form-control-sm coord-lon" name="longitud" placeholder="Longitud" type="number" step="0.000001" min="-180" max="180" aria-label="Longitud" title="Longitud"></div>
                                    <div class="col-md-6 text-end"><button class="btn btn-sm btn-outline-primary" name="accion" value="punto"><i class="bi bi-plus"></i> Agregar punto</button></div>
                                </form>
                            </div>
                        </div>

                        <!-- Canales digitales -->
                        <div class="card tarjeta">
                            <div class="card-body">
                                <h2 class="h6 titulo-seccion">Canales digitales</h2>
                                <c:forEach var="ca" items="${dep.canales}">
                                    <form method="post" action="${ctx}/admin/ubicaciones" class="row g-2 border rounded p-2 mb-2">
                                        <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${ca.id}"><input type="hidden" name="idDependencia" value="${dep.id}">
                                        <div class="col-md-4"><select class="form-select form-select-sm" name="tipo" aria-label="Tipo">
                                            <c:forTokens var="tp" items="PORTAL,MESA_PARTES_VIRTUAL,CORREO,PLATAFORMA,TELEFONO,RED_SOCIAL" delims=",">
                                                <option value="${tp}" ${ca.tipo == tp ? 'selected' : ''}>${tp}</option></c:forTokens></select></div>
                                        <div class="col-md-8"><input class="form-control form-control-sm" name="nombre" value="<c:out value='${ca.nombre}'/>" required maxlength="150" aria-label="Nombre"></div>
                                        <div class="col-12"><input class="form-control form-control-sm" name="valor" value="<c:out value='${ca.valor}'/>" required maxlength="300" aria-label="Valor"></div>
                                        <div class="col-md-8"><input class="form-control form-control-sm" name="descripcion" value="<c:out value='${ca.descripcion}'/>" placeholder="Descripción" maxlength="300"></div>
                                        <div class="col-md-4 d-flex gap-2 align-items-center justify-content-end">
                                            <small class="${ca.revisionPendiente ? 'text-danger' : 'text-secondary'}">${ca.fechaUltimaVerificacion}</small>
                                            <button class="btn btn-sm btn-primary" name="accion" value="canal"><i class="bi bi-save"></i></button>
                                            <button class="btn btn-sm btn-outline-danger" name="accion" value="eliminarCanal" onclick="return confirm('¿Eliminar este canal?')"><i class="bi bi-trash"></i></button>
                                        </div>
                                    </form>
                                </c:forEach>
                                <form method="post" action="${ctx}/admin/ubicaciones" class="row g-2 border border-primary rounded p-2">
                                    <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="idDependencia" value="${dep.id}">
                                    <div class="col-12 small fw-semibold">Nuevo canal digital</div>
                                    <div class="col-md-4"><select class="form-select form-select-sm" name="tipo" required>
                                        <option value="PORTAL">Portal web</option><option value="MESA_PARTES_VIRTUAL">Mesa de partes virtual</option>
                                        <option value="CORREO">Correo electrónico</option><option value="PLATAFORMA">Plataforma</option>
                                        <option value="TELEFONO">Teléfono</option><option value="RED_SOCIAL">Red social</option></select></div>
                                    <div class="col-md-8"><input class="form-control form-control-sm" name="nombre" placeholder="Nombre" required maxlength="150"></div>
                                    <div class="col-12"><input class="form-control form-control-sm" name="valor" placeholder="https://..., correo@unjbg.edu.pe o número" required maxlength="300"></div>
                                    <div class="col-md-8"><input class="form-control form-control-sm" name="descripcion" placeholder="Descripción (opcional)" maxlength="300"></div>
                                    <div class="col-md-4 text-end"><button class="btn btn-sm btn-outline-primary" name="accion" value="canal"><i class="bi bi-plus"></i> Agregar canal</button></div>
                                </form>
                            </div>
                        </div>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<c:if test="${not empty dep}">
<script src="https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.js"></script>
<script src="${ctx}/recursos/js/mapa-base.js?v=2.3"></script>
<script>
    (function () {
        var el = document.getElementById('planoAdmin');
        if (!el) { return; }
        if (typeof L === 'undefined' || typeof MapaCampus === 'undefined') {
            el.textContent = 'No se pudo cargar el mapa (sin conexión a internet). Puede escribir la latitud y la longitud a mano.';
            return;
        }
        var mapa = MapaCampus.crear(el, {lat: ${mapaLat}, lon: ${mapaLon}, zoom: ${mapaZoom},
            campus: '${ctx}/recursos/datos/campus-unjbg.geojson', satelite: true});
        var formularios = document.querySelectorAll('.form-punto');
        var formActivo = formularios[formularios.length - 1];
        var ubicados = [];
        formularios.forEach(function (f) {
            function activar() {
                formularios.forEach(function (o) { o.classList.remove('bg-light'); });
                f.classList.add('bg-light');
                formActivo = f;
            }
            f.addEventListener('focusin', activar);
            f.addEventListener('pointerdown', activar);
            var lat = f.querySelector('.coord-lat').value, lon = f.querySelector('.coord-lon').value;
            if (lat !== '' && lon !== '') {
                var ll = [Number(lat), Number(lon)];
                ubicados.push(ll);
                MapaCampus.marcador(ll).addTo(mapa).bindTooltip(f.querySelector('[name=nombre]').value);
            }
        });
        if (ubicados.length > 0) {
            mapa.fitBounds(L.latLngBounds(ubicados).pad(0.5), {maxZoom: 18});
        }
        var nuevo = null;
        mapa.on('click', function (e) {
            if (!formActivo) { return; }
            formActivo.querySelector('.coord-lat').value = e.latlng.lat.toFixed(6);
            formActivo.querySelector('.coord-lon').value = e.latlng.lng.toFixed(6);
            if (nuevo) { mapa.removeLayer(nuevo); }
            nuevo = L.circleMarker(e.latlng, {radius: 9, color: MapaCampus.COLOR.ruta, fillColor: MapaCampus.COLOR.ruta, fillOpacity: .6}).addTo(mapa)
                .bindTooltip('Nueva posición: guarde el formulario resaltado').openTooltip();
        });
    })();
</script>
</c:if>
<%@ include file="../fragmentos/pie.jspf" %>
