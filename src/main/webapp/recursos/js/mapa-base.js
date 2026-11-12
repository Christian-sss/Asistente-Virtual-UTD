/*
 * Mapa real del campus con Leaflet.js: capa de calles de OpenStreetMap, capa satelital (Esri)
 * y el contorno de la Ciudad Universitaria tomado de OpenStreetMap. Lo usan la página pública
 * de ubicación (plano.js) y el panel «Ubicaciones y canales».
 */
var MapaCampus = (function () {
    'use strict';

    var COLOR = {borde: '#072a4f', relleno: '#e0a526', ruta: '#c0392b'};

    function crear(contenedor, opciones) {
        var mapa = L.map(contenedor, {zoomSnap: 0.5, scrollWheelZoom: opciones.ruedaZoom !== false});
        // La página usa Referrer-Policy: same-origin, pero los servidores de OpenStreetMap rechazan (403)
        // las imágenes pedidas sin Referer. Solo estas imágenes envían el origen del sitio (sin ruta).
        var calles = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            referrerPolicy: 'strict-origin-when-cross-origin',
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noopener">colaboradores de OpenStreetMap</a>'
        });
        // En Tacna la imagen satelital llega hasta el zoom 17; más cerca se amplía esa misma imagen.
        var satelite = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
            maxZoom: 19,
            maxNativeZoom: 17,
            referrerPolicy: 'strict-origin-when-cross-origin',
            attribution: 'Imágenes &copy; Esri, Maxar, Earthstar Geographics'
        });
        (opciones.satelite ? satelite : calles).addTo(mapa);
        L.control.layers({'Mapa': calles, 'Satélite': satelite}, null, {position: 'topright'}).addTo(mapa);
        L.control.scale({imperial: false}).addTo(mapa);
        mapa.setView([opciones.lat, opciones.lon], opciones.zoom);

        if (opciones.campus) {
            fetch(opciones.campus)
                .then(function (r) { return r.json(); })
                .then(function (geo) {
                    var capa = L.geoJSON(geo, {
                        interactive: false,
                        style: {color: COLOR.borde, weight: 2, dashArray: '6 4', fillColor: COLOR.relleno, fillOpacity: 0.06}
                    }).addTo(mapa);
                    capa.bringToBack();
                })
                .catch(function () { /* el contorno es solo una ayuda visual */ });
        }
        return mapa;
    }

    function marcador(latlng) {
        return L.circleMarker(latlng, {radius: 9, color: COLOR.borde, weight: 2, fillColor: COLOR.relleno, fillOpacity: 0.95});
    }

    function sinLeaflet(contenedor, texto) {
        contenedor.innerHTML = '';
        var d = document.createElement('div');
        d.className = 'alert alert-warning m-3';
        d.textContent = texto;
        contenedor.appendChild(d);
    }

    return {crear: crear, marcador: marcador, sinLeaflet: sinLeaflet, COLOR: COLOR};
})();
