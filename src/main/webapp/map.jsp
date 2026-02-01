<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>Carte des stations météo</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@300;600;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/styles/style.css">
</head>
<body>
<jsp:include page="/menu.jsp" />

<div class="container">
    <h1>Carte des stations météo</h1>
    <div id="map" style="height: 600px; border-radius: 15px; box-shadow: 0 4px 15px rgba(0,0,0,0.1);"></div>
</div>

<!-- ETAPE 1 : AJOUT DE LA MODALE HTML (indispensable pour l'affichage) -->
<div id="stationModal" class="modal">
    <div class="modal-content">
        <span id="closeModal" class="close">&times;</span>
        <h2 id="modalStationName"></h2>
        <p id="modalStationPays"></p>
        <p id="modalStationCoords"></p>
        <div id="modalMeteoList"></div>
        <button id="refreshStationBtn">🔄 Rafraîchir cette station</button>
    </div>
</div>

<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>

<script>
    // ETAPE 2 : INITIALISATION DES VARIABLES POUR LA MODALE
    const modal = document.getElementById('stationModal');
    const closeModal = document.getElementById('closeModal');
    const modalStationName = document.getElementById('modalStationName');
    const modalStationCoords = document.getElementById('modalStationCoords');
    const modalMeteoList = document.getElementById('modalMeteoList');
    const refreshBtn = document.getElementById('refreshStationBtn');

    // Transfert des stations depuis JSTL
    const stations = [
        <c:forEach var="station" items="${stations}" varStatus="status">
        {
            id: ${station.numero},
            nom: "${station.nom.replace('"', '\"')}",
            lat: ${station.latitude},
            lon: ${station.longitude},
            pays: "${station.pays.nom}"
        }<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];

    // Initialisation de la carte
    var map = L.map('map').setView([46.8, 8.33], 7);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
    }).addTo(map);

    // ETAPE 3 : AJOUT DES MARKERS AVEC APPEL DE LA MODALE
    var markers = [];
    stations.forEach(function(station) {
        if(!station.lat || !station.lon) return;

        var marker = L.marker([station.lat, station.lon]).addTo(map);

        // Au clic sur le marker, on ouvre la modale
        marker.on('click', function() {
            openStationModal(station.id);
        });

        // Optionnel : un petit tooltip au survol pour voir le nom
        marker.bindTooltip(station.nom);

        markers.push(marker);
    });

    if(markers.length > 0){
        var group = new L.featureGroup(markers);
        map.fitBounds(group.getBounds(), { padding: [40, 40] });
    }

    // --- FONCTION OPEN MODAL (LA MEME QUE SUR INDEX.JSP) ---
    function openStationModal(numero) {
        fetch('station-json?numero=' + numero)
            .then(res => res.json())
            .then(data => {
                modalStationName.textContent =  data.nom +" - " + (data.pays ? data.pays.nom : "Inconnu");
                modalStationCoords.textContent = "Latitude : " + data.latitude + " - Longitude : " + data.longitude;
                modalMeteoList.innerHTML = "";

                if (data.donneesMeteo && data.donneesMeteo.length > 0) {
                    data.donneesMeteo.forEach(m => {
                        const p = document.createElement('p');
                        p.className = "meteo-item"; // Tu peux ajouter cette classe dans ton CSS

                        let pluieAffichee = (m.precipitation && m.precipitation > 0) ? m.precipitation + ' mm' : 'Pas de pluie';

                        let html = '<strong>' + m.date + '</strong> : ';
                        html += '🌡️ ' + m.temp + '°C (ressenti ' + m.ressenti + '°C) | ';
                        html += 'Min/Max: ' + m.tempMin + '/' + m.tempMax + '°C | ';
                        html += '💧 Humidité: ' + m.humi + '% | ';
                        html += '💨 Vent: ' + m.ventVitesse + ' km/h, rafales ' + m.ventRafales + ' km/h, dir ' + m.ventDirection + '° | ';
                        html += '🌦️ Pluie: ' + pluieAffichee  + ' | ';
                        html += '🌤️ Pression: ' + m.pression + ' hPa | ';
                        html += '🌫️ Visibilité: ' + m.visibilite + ' m | ';
                        html += '☀️ Lever: ' + m.leverSoleil + ' / Coucher: ' + m.coucherSoleil + ' | ';
                        if (m.texte && m.texte.length > 0) {
                            html += 'Descriptions: ' + m.texte.join(", ");
                        }
                        p.innerHTML = html;
                        modalMeteoList.appendChild(p);
                    });
                } else {
                    modalMeteoList.innerHTML = "<p>Aucune donnée météo disponible.</p>";
                }

                refreshBtn.onclick = function() { refreshStation(data.numero); };
                modal.style.display = "flex";
            });
    }

    // Fonction Refresh (indispensable car appelée par la modale)
    function refreshStation(numero){
        refreshBtn.textContent = "⌛ Chargement...";
        refreshBtn.disabled = true;
        fetch('refresh-station?numero=' + numero, {method:'POST'})
            .then(res => {
                refreshBtn.textContent = "🔄 Rafraîchir cette station";
                refreshBtn.disabled = false;
                openStationModal(numero);
            })
            .catch(err => alert("Erreur refresh"));
    }

    // Fermeture de la modale
    closeModal.onclick = () => modal.style.display = "none";
    window.onclick = e => { if(e.target === modal) modal.style.display = "none"; }
</script>
</body>
</html>