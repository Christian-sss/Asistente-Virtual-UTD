/*
 * Plano del campus con Leaflet.js (RF-09): dibuja los puntos de atención sobre la imagen del plano
 * y, si se indica un trámite, la secuencia de visita numerada. Los datos llegan en JSON desde
 * /ubicacion/puntos. El texto se inserta con textContent para evitar inyección de HTML.
 */
(function () {
    'use strict';
    var contenedor = document.getElementById('plano');
    if (!contenedor || typeof L === 'undefined') {
        if (contenedor) {
            contenedor.innerHTML = '<div class="alert alert-warning m-3">No se pudo cargar el plano (sin conexión a la biblioteca Leaflet).</div>';
        }
        return;
    }
    var urlDatos = contenedor.getAttribute('data-url');
    var marcadores = {};

    function elemento(tag, clase, texto) {
        var e = document.createElement(tag);
        if (clase) { e.className = clase; }
        if (texto !== undefined && texto !== null) { e.textContent = texto; }
        return e;
    }

    function popup(p) {
        var d = elemento('div', 'popup-punto');
        d.appendChild(elemento('strong', null, p.nombre));
        d.appendChild(elemento('div', 'small text-secondary', p.dependencia));
        if (p.referencia) { d.appendChild(elemento('div', 'small', p.referencia)); }
        d.appendChild(elemento('div', 'small', p.sede + ' · ' + p.direccion));
        d.appendChild(elemento('div', 'small', 'Horario: ' + p.horario));
        if (p.telefono) { d.appendChild(elemento('div', 'small', 'Teléfono: ' + p.telefono)); }
        if (p.correo) { d.appendChild(elemento('div', 'small text-break', 'Correo: ' + p.correo)); }
        var v = elemento('div', 'small mt-1 ' + (p.revisar ? 'text-danger' : 'text-secondary'),
            'Verificado: ' + (p.verificado || '-') + (p.revisar ? ' (pendiente de verificación)' : ''));
        d.appendChild(v);
        return d;
    }

    fetch(urlDatos, {headers: {'Accept': 'application/json'}})
        .then(function (r) { return r.json(); })
        .then(function (datos) {
            var alto = datos.plano.alto, ancho = datos.plano.ancho;
            var limites = [[0, 0], [alto, ancho]];
            var mapa = L.map(contenedor, {crs: L.CRS.Simple, minZoom: -1, maxZoom: 2, zoomSnap: 0.25, attributionControl: false});
            L.imageOverlay(datos.plano.imagen, limites).addTo(mapa);
            mapa.fitBounds(limites);
            mapa.setMaxBounds([[-100, -100], [alto + 100, ancho + 100]]);

            var porId = {};
            datos.puntos.forEach(function (p) {
                porId[p.id] = p;
                if (p.x === null || p.y === null) { return; }
                var m = L.circleMarker([alto - p.y, p.x], {radius: 9, color: '#072a4f', weight: 2, fillColor: '#e0a526', fillOpacity: 0.95})
                    .addTo(mapa).bindPopup(popup(p)).bindTooltip(p.nombre, {direction: 'top'});
                marcadores[p.id] = m;
            });

            var ruta = [];
            datos.secuencia.forEach(function (s) {
                var p = porId[s.idPunto];
                if (!p || p.x === null || p.y === null) { return; }
                var ll = [alto - p.y, p.x];
                ruta.push(ll);
                var icono = L.divIcon({className: 'marcador-secuencia', html: '<span>' + Number(s.orden) + '</span>', iconSize: [30, 30], iconAnchor: [15, 15]});
                L.marker(ll, {icon: icono, zIndexOffset: 1000}).addTo(mapa).bindPopup(popup(p));
            });
            if (ruta.length > 1) {
                L.polyline(ruta, {color: '#c0392b', weight: 4, dashArray: '8 8'}).addTo(mapa);
            }
            if (ruta.length > 0) {
                mapa.fitBounds(L.latLngBounds(ruta).pad(0.6), {maxZoom: 0.5});
            }

            document.querySelectorAll('[data-ver-punto]').forEach(function (b) {
                b.addEventListener('click', function () {
                    var m = marcadores[b.getAttribute('data-ver-punto')];
                    if (m) {
                        contenedor.scrollIntoView({behavior: 'smooth', block: 'center'});
                        mapa.setView(m.getLatLng(), 1);
                        m.openPopup();
                    }
                });
            });
        })
        .catch(function () {
            contenedor.innerHTML = '<div class="alert alert-warning m-3">No se pudieron cargar los puntos de atención.</div>';
        });
})();
