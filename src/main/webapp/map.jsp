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
          integrity="sha256-sA+4Al+N7Etk+75qFvV3Tc3MwRZiUJjeCjcJTg5wPqM=" crossorigin=""/>
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@300;600;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/styles/style.css">
</head>
<body>
<jsp:include page="/menu.jsp" />
<h1>Carte des stations météo</h1>

<div id="map"></div>

<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
        integrity="sha256-V4D8lEyfQJXzFapxCqix2A+vL0oKekmGkV+ZfqL8kKw=" crossorigin=""></script>

<script>
    // -------------------------
    // 1. Transfert des stations côté JS
    // -------------------------
    const stations = [
        <c:forEach var="station" items="${stations}" varStatus="status">
        {
            id: ${station.numero},
            nom: "${station.nom}",
            lat: ${station.latitude},
            lon: ${station.longitude}
        }<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];

    // -------------------------
    // 2. Initialisation de la carte
    // -------------------------
    var DEFAULT_LAT = 46.8;   // centre Suisse
    var DEFAULT_LON = 8.33;
    var DEFAULT_ZOOM = 7;

    var map = L.map('map').setView([DEFAULT_LAT, DEFAULT_LON], DEFAULT_ZOOM);

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
    // 4. Ajuster la vue pour tout voir
    // -------------------------
    if(markers.length > 0){
        var group = new L.featureGroup(markers);
        map.fitBounds(group.getBounds(), { padding: [40, 40] });
    }

</script>
</body>
</html>
