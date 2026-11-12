<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Cargo, expediente y derivación"/>
<c:set var="menu" value="derivacion"/>
<%@ include file="fragmentos/cabecera.jspf" %>

<div class="encabezado-pagina">
    <div class="container">
        <h1><i class="bi bi-inboxes"></i> Cargo, expediente y seguimiento</h1>
        <p class="text-secondary mb-0">Qué conservar al presentar tu trámite, cómo se identifica tu expediente y a qué oficina acudir si tu consulta no es de la UTD.</p>
    </div>
</div>

<div class="container mt-4">
    <div class="row g-4">
        <div class="col-lg-7">
            <div class="card tarjeta mb-4">
                <div class="card-body">
                    <h2 class="h5 titulo-seccion"><i class="bi bi-receipt"></i> El cargo de recepción</h2>
                    <p class="mb-2">Al presentar tu expediente en mesa de partes recibirás el <strong>cargo</strong>: una copia de tu solicitud
                        con el sello, la fecha y la hora de recepción, y el número de expediente.</p>
                    <ul class="small mb-0">
                        <li>Es la <strong>prueba de que presentaste</strong> tu trámite y desde cuándo corre el plazo de atención.</li>
                        <li>Consérvalo hasta que el trámite concluya; lo necesitarás para cualquier consulta o reclamo.</li>
                        <li>Revisa que el sello, la fecha y el número de folios sean correctos antes de retirarte de la ventanilla.</li>
                    </ul>
                </div>
            </div>

            <div class="card tarjeta mb-4">
                <div class="card-body">
                    <h2 class="h5 titulo-seccion"><i class="bi bi-123"></i> El número de expediente</h2>
                    <p class="mb-2">La UTD asigna a cada trámite un <strong>número de expediente de siete dígitos</strong> (por ejemplo, 0123456).
                        Con ese número se registra y se hace el seguimiento de tu trámite en las oficinas que intervienen.</p>
                    <form class="row g-2 align-items-end" method="get" action="${ctx}/derivacion">
                        <div class="col-sm-7">
                            <label class="form-label small" for="expediente">Comprueba el formato de tu número</label>
                            <input class="form-control" id="expediente" name="expediente" inputmode="numeric" maxlength="12"
                                   value="<c:out value='${expediente}'/>" placeholder="7 dígitos">
                        </div>
                        <div class="col-sm-5 d-grid"><button class="btn btn-outline-primary"><i class="bi bi-check2-circle"></i> Comprobar</button></div>
                    </form>
                    <c:if test="${not empty expediente}">
                        <div class="alert ${expedienteValido ? 'alert-success' : 'alert-warning'} small mt-3 mb-0">
                            <c:choose>
                                <c:when test="${expedienteValido}">El número tiene el formato correcto de siete dígitos. Para conocer su estado consulte en la
                                    ventanilla de la UTD o por los canales de la dependencia; este asistente no consulta el sistema institucional.</c:when>
                                <c:otherwise>El número de expediente debe tener exactamente siete dígitos, sin letras ni guiones. Revise su cargo.</c:otherwise>
                            </c:choose>
                        </div>
                    </c:if>
                </div>
            </div>

            <div class="card tarjeta mb-4">
                <div class="card-body">
                    <h2 class="h5 titulo-seccion"><i class="bi bi-signpost-2"></i> Estados de seguimiento</h2>
                    <div class="table-responsive">
                        <table class="table table-sm small mb-0">
                            <tbody>
                            <tr><td><span class="badge text-bg-secondary">Recibido</span></td><td>La UTD registró tu expediente y te entregó el cargo con su número.</td></tr>
                            <tr><td><span class="badge text-bg-info">Derivado</span></td><td>El expediente fue enviado a la oficina responsable de atenderlo.</td></tr>
                            <tr><td><span class="badge text-bg-primary">En trámite</span></td><td>La oficina responsable está evaluando tu solicitud dentro del plazo del TUPA.</td></tr>
                            <tr><td><span class="badge text-bg-warning">Observado</span></td><td>Falta un requisito o hay un error: debes subsanarlo en el plazo indicado.</td></tr>
                            <tr><td><span class="badge text-bg-success">Atendido</span></td><td>El trámite concluyó; puedes recoger el documento o la respuesta.</td></tr>
                            <tr><td><span class="badge text-bg-dark">Archivado</span></td><td>El expediente concluido se guarda en el archivo de la universidad.</td></tr>
                            </tbody>
                        </table>
                    </div>
                    <p class="small text-secondary mt-2 mb-0">Información referencial: el asistente no procesa pagos, no asigna números de expediente ni consulta su estado.</p>
                </div>
            </div>
        </div>

        <div class="col-lg-5">
            <div class="card tarjeta mb-4" id="derivar">
                <div class="card-body">
                    <h2 class="h5 titulo-seccion"><i class="bi bi-compass"></i> ¿A qué oficina debo ir?</h2>
                    <p class="small text-secondary">Describe tu consulta con tus palabras y te indicamos la dependencia competente.</p>
                    <form method="get" action="${ctx}/derivacion#derivar">
                        <textarea class="form-control mb-2" name="consulta" rows="3" maxlength="300" required
                                  placeholder="Ej.: quiero saber mis notas del semestre pasado"><c:out value="${consulta}"/></textarea>
                        <button class="btn btn-primary w-100"><i class="bi bi-search"></i> Buscar dependencia</button>
                    </form>
                    <c:if test="${not empty consulta}">
                        <c:choose>
                            <c:when test="${empty resultados}">
                                <div class="alert alert-info small mt-3 mb-0">No encontramos una dependencia para esa consulta. Prueba con otras palabras,
                                    usa la <a href="${ctx}/orientacion">orientación guiada</a> o acércate a la UTD (mesa de partes), que te orientará.</div>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="r" items="${resultados}" varStatus="st">
                                    <div class="border rounded p-3 mt-3 ${st.first ? 'border-primary' : ''}">
                                        <c:if test="${st.first}"><span class="badge text-bg-primary mb-1">Dependencia sugerida</span></c:if>
                                        <c:if test="${r.dependencia().utd}"><span class="badge text-bg-success mb-1">Corresponde a la UTD</span></c:if>
                                        <h3 class="h6 mb-1"><c:out value="${r.dependencia().nombre}"/></h3>
                                        <p class="small mb-1"><c:out value="${r.regla().indicacion}"/></p>
                                        <p class="small text-secondary mb-2">Tema: <c:out value="${r.regla().tema}"/></p>
                                        <c:forEach var="p" items="${r.dependencia().puntos}">
                                            <p class="small mb-1"><i class="bi bi-geo-alt"></i> <strong><c:out value="${p.nombre}"/></strong>
                                                <c:if test="${not empty p.referencia}">, <c:out value="${p.referencia}"/></c:if><br>
                                                <c:out value="${p.sede}"/> &middot; <c:out value="${p.horario}"/></p>
                                        </c:forEach>
                                        <c:forEach var="ca" items="${r.dependencia().canales}">
                                            <p class="small mb-1 canal-item"><i class="bi ${ca.icono}"></i>
                                                <c:choose>
                                                    <c:when test="${ca.enlace}"><a href="<c:out value='${ca.valor}'/>" target="_blank" rel="noopener"><c:out value="${ca.nombre}"/></a></c:when>
                                                    <c:otherwise><c:out value="${ca.nombre}"/>: <c:out value="${ca.valor}"/></c:otherwise>
                                                </c:choose></p>
                                        </c:forEach>
                                        <a class="btn btn-sm btn-outline-primary mt-1" href="${ctx}/ubicacion?dependencia=${r.dependencia().id}#dep${r.dependencia().id}"><i class="bi bi-map"></i> Ver en el plano</a>
                                    </div>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                </div>
            </div>

            <div class="card tarjeta" id="retroalimentacion">
                <div class="card-body">
                    <h2 class="h5 titulo-seccion"><i class="bi bi-megaphone"></i> ¿Encontraste información inexacta?</h2>
                    <%@ include file="fragmentos/retroalimentacion.jspf" %>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="fragmentos/pie.jspf" %>
