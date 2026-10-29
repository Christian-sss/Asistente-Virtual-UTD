<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Revisión preliminar"/>
<c:set var="menu" value="revision"/>
<%@ include file="fragmentos/cabecera.jspf" %>

<div class="encabezado-pagina">
    <div class="container">
        <h1><i class="bi bi-file-earmark-check"></i> Revisión preliminar de documentos</h1>
        <p class="text-secondary mb-0">Verifica que tu solicitud cumpla los requisitos mínimos formales antes de presentarla.</p>
    </div>
</div>

<div class="container mt-4" style="max-width: 860px">
    <div class="card tarjeta">
        <div class="card-body p-4">
            <form method="post" action="${ctx}/revision" enctype="multipart/form-data" id="formRevision">
                <input type="hidden" name="_csrf" value="${csrf}">
                <div class="mb-3">
                    <label class="form-label fw-semibold" for="tramite">1. ¿Para qué trámite es tu solicitud?</label>
                    <select class="form-select" id="tramite" name="tramite" required>
                        <option value="">Seleccione el trámite</option>
                        <c:forEach var="t" items="${tramites}">
                            <option value="${t.id}" ${tramiteSel == t.id ? 'selected' : ''}><c:out value="${t.nombre}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="mb-3">
                    <label class="form-label fw-semibold" for="archivo">2. Selecciona tu solicitud</label>
                    <input class="form-control" type="file" id="archivo" name="archivo" accept=".pdf,.jpg,.jpeg,.png" required>
                    <div class="form-text">PDF, JPG o PNG, máximo 10 MB y 20 páginas. Para revisar el texto se recomienda un PDF digital.</div>
                </div>
                <div class="mb-3">
                    <label class="form-label fw-semibold">3. Tratamiento de datos personales</label>
                    <div class="border rounded p-3 bg-light small" style="max-height: 170px; overflow: auto">
                        Conforme a la <strong>Ley N.° 29733, Ley de Protección de Datos Personales</strong>, autorizo el
                        tratamiento de los datos contenidos en el documento que cargo con la <strong>única finalidad</strong>
                        de realizar su revisión preliminar. El archivo se almacena de forma temporal fuera del acceso público,
                        con un nombre aleatorio; el reporte solo es visible en mi sesión, y el documento y su reporte
                        <strong>se eliminan automáticamente a las 24 horas</strong>, sin conservar copias. La revisión es
                        referencial y no constituye la admisión del trámite.
                    </div>
                    <div class="form-check mt-2">
                        <input class="form-check-input" type="checkbox" id="acepto" name="acepto" value="1"
                               ${clausulaAceptada ? 'checked' : ''} required>
                        <label class="form-check-label" for="acepto">He leído y acepto la cláusula de tratamiento de datos.</label>
                    </div>
                </div>
                <button class="btn btn-primary btn-lg w-100" id="btnRevisar">
                    <i class="bi bi-search"></i> Revisar documento
                </button>
            </form>
        </div>
    </div>

    <div class="row g-3 mt-2 small text-secondary">
        <div class="col-md-4"><i class="bi bi-shield-lock text-primary"></i> Se valida el tipo real del archivo, su tamaño y número de páginas.</div>
        <div class="col-md-4"><i class="bi bi-ui-checks text-primary"></i> Se buscan DNI, correo, fecha, código de estudiante, destinatario y pago.</div>
        <div class="col-md-4"><i class="bi bi-trash3 text-primary"></i> El documento se elimina automáticamente a las 24 horas.</div>
    </div>
</div>
<script>
    document.getElementById('formRevision').addEventListener('submit', function () {
        var b = document.getElementById('btnRevisar');
        b.disabled = true;
        b.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Revisando...';
    });
</script>

<%@ include file="fragmentos/pie.jspf" %>
