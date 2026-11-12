/*
 * Mapa del campus con Leaflet.js (RF-09): dibuja los puntos de atención sobre el mapa real
 * (OpenStreetMap) y, si se indica un trámite, la secuencia de visita numerada. Los datos llegan
 * en JSON desde /ubicacion/puntos. El texto se inserta con textContent para evitar inyección de HTML.
 */
(function () {
    'use strict';
    var contenedor = document.getElementById('plano');
    if (!contenedor) {
        return;
    }
    if (typeof L === 'undefined' || typeof MapaCampus === 'undefined') {
        if (typeof MapaCampus !== 'undefined') {
            MapaCampus.sinLeaflet(contenedor, 'No se pudo cargar el mapa (sin conexión a internet).');
        } else {
            contenedor.textContent = 'No se pudo cargar el mapa (sin conexión a internet).';
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
        var a = elemento('a', 'small d-inline-block mt-1', 'Cómo llegar (Google Maps)');
        a.href = 'https://www.google.com/maps/dir/?api=1&destination=' + p.lat + ',' + p.lon;
        a.target = '_blank';
        a.rel = 'noopener';
        d.appendChild(a);
        return d;
    }

    function ubicado(p) {
        return p && p.lat !== null && p.lon !== null;
    }

    fetch(urlDatos, {headers: {'Accept': 'application/json'}})
        .then(function (r) { return r.json(); })
        .then(function (datos) {
            var mapa = MapaCampus.crear(contenedor, {
                lat: datos.mapa.lat, lon: datos.mapa.lon, zoom: datos.mapa.zoom, campus: datos.mapa.campus, ruedaZoom: false
            });

            var porId = {};
            var todos = [];
            datos.puntos.forEach(function (p) {
                porId[p.id] = p;
                if (!ubicado(p)) { return; }
                var ll = [Number(p.lat), Number(p.lon)];
                todos.push(ll);
                marcadores[p.id] = MapaCampus.marcador(ll).addTo(mapa)
                    .bindPopup(popup(p)).bindTooltip(p.nombre, {direction: 'top'});
            });

            var ruta = [];
            datos.secuencia.forEach(function (s) {
                var p = porId[s.idPunto];
                if (!ubicado(p)) { return; }
                var ll = [Number(p.lat), Number(p.lon)];
                ruta.push(ll);
                var icono = L.divIcon({className: 'marcador-secuencia', html: '<span>' + Number(s.orden) + '</span>', iconSize: [30, 30], iconAnchor: [15, 15]});
                L.marker(ll, {icon: icono, zIndexOffset: 1000}).addTo(mapa).bindPopup(popup(p));
            });
            if (ruta.length > 1) {
                L.polyline(ruta, {color: MapaCampus.COLOR.ruta, weight: 4, dashArray: '8 8'}).addTo(mapa);
            }
            var encuadre = ruta.length > 0 ? ruta : todos;
            if (encuadre.length > 0) {
                mapa.fitBounds(L.latLngBounds(encuadre).pad(0.5), {maxZoom: 18});
            }

            document.querySelectorAll('[data-ver-punto]').forEach(function (b) {
                b.addEventListener('click', function () {
                    var m = marcadores[b.getAttribute('data-ver-punto')];
                    if (m) {
                        contenedor.scrollIntoView({behavior: 'smooth', block: 'center'});
                        mapa.setView(m.getLatLng(), 19);
                        m.openPopup();
                    }
                });
            });
        })
        .catch(function () {
            MapaCampus.sinLeaflet(contenedor, 'No se pudieron cargar los puntos de atención.');
        });
})();
