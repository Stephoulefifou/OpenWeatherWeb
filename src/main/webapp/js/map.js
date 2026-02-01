// Initialisation de la carte avec Leaflet.js

var DEFAULT_LAT = 46.8;     // Suisse
var DEFAULT_LON = 8.33;
var DEFAULT_ZOOM = 7;

// Création de la carte
var map = L.map('map').setView(
    [DEFAULT_LAT, DEFAULT_LON],
    DEFAULT_ZOOM
);

// Fond de carte OpenStreetMap
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors'
}).addTo(map);

//2. Vérification des données
if (typeof stations === 'undefined' || stations.length === 0) {
    console.warn("Aucune station à afficher sur la carte.");
}

//3. Ajout des markers pour chaque station
var markers = [];

stations.forEach(function (station) {

    // Sécurité minimale
    if (!station.lat || !station.lon) {
        console.warn("Station ignorée (coordonnées manquantes):", station);
        return;
    }

    var marker = L.marker([station.lat, station.lon])
        .addTo(map)
        .bindPopup(
            "<b>" + station.nom + "</b><br>" +
            "<a href='station?id=" + station.id + "'>Voir détails</a>"
        );

    markers.push(marker);
});

//4. Ajuster la vue à toutes les stations
if (markers.length > 0) {
    var group = new L.featureGroup(markers);
    map.fitBounds(group.getBounds(), { padding: [40, 40] });
}
