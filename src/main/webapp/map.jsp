<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="ch.hearc.heg.scl.business.StationMeteo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>Carte des stations météo</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
          integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
          crossorigin=""/>
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@300;600;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/styles/style.css">
</head>
<body>
<jsp:include page="/menu.jsp" />
<h1>Carte des stations météo</h1>
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
        integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo="
        crossorigin=""></script>
<div id="map"></div>
<script>
    // -------------------------
    // 1. Transfert des stations côté JS
    // -------------------------
    const stations = [
        <c:forEach var="station" items="${stations}" varStatus="status">
        {
            id: ${station.numero},
            nom: "${station.nom.replace('"', '\"')}", // Sécurité pour les guillemets
            lat: ${station.latitude},
            lon: ${station.longitude}
        }<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];

    // -------------------------
    // 2. Initialisation de la carte
    // -------------------------
    var map = L.map('map').setView([46.8, 8.33], 7);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
    }).addTo(map);

    // -------------------------
    // 3. Ajout des markers
    // -------------------------
    var markers = [];

    stations.forEach(function(station) {
        if(!station.lat || !station.lon) return;

        var marker = L.marker([station.lat, station.lon])
            .addTo(map)
            .bindPopup("<b>" + station.nom + "</b><br>" +
                "<a href='station?id=" + station.id + "'>Voir détails</a>");
        markers.push(marker);
    });

    // -------------------------
    // 4. Ajuster la vue
    // -------------------------
    if(markers.length > 0){
        var group = new L.featureGroup(markers);
        map.fitBounds(group.getBounds(), { padding: [40, 40] });
    }
</script>
</body>
</html>
