<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="ch.hearc.heg.scl.business.StationMeteo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>Stations Météo</title>
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@300;600;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/styles/style.css">
</head>
<body>

<jsp:include page="/menu.jsp" />

<div class="container">

    <div class="sun">☀️</div>
    <a href="findStation">Aller à la page de recherche de station</a>

    <h1>Stations météo</h1>

    <!-- Bouton Rafraîchir toutes les stations -->
    <div style="display: flex; justify-content: flex-end; margin-bottom: 20px;">
        <form method="post" action="stations">
            <input type="hidden" name="action" value="refreshAll"/>
            <button type="submit">🔄 Rafraîchir toutes les stations</button>
        </form>
    </div>

    <!-- Liste des stations -->
    <c:choose>
        <c:when test="${empty stations}">
            <div class="empty">
                Aucune station trouvée. Assurez-vous d'avoir demandé
                <a href="${pageContext.request.contextPath}/stations">/stations</a>.
            </div>
        </c:when>
        <c:otherwise>
            <ul>
                <c:forEach var="station" items="${stations}">
                    <li class="station-card" onclick="openStationModal('${station.numero}')">
                        <div class="country">${station.pays.nom}</div>
                        <div class="station">${station.nom}</div>
                        <div class="coords">
                            🌍 lat: ${station.latitude} | lon: ${station.longitude}
                        </div>
                    </li>
                </c:forEach>
            </ul>
        </c:otherwise>
    </c:choose>

</div>

<!-- Modal pour une station -->
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

<!-- JS pour modal et fetch -->
<script>
    const modal = document.getElementById('stationModal');
    const closeModal = document.getElementById('closeModal');
    const modalStationName = document.getElementById('modalStationName');
    const modalStationPays = document.getElementById('modalStationPays');
    const modalStationCoords = document.getElementById('modalStationCoords');
    const modalMeteoList = document.getElementById('modalMeteoList');
    const refreshBtn = document.getElementById('refreshStationBtn');

    // Ouvre le modal avec les données d'une station
    function openStationModal(numero) {
        fetch(`station-json?numero=${numero}`)
            .then(res => res.json())
            .then(data => {
                modalStationName.textContent = data.nom;
                modalStationPays.textContent = "Pays : " + data.pays.nom;
                modalStationCoords.textContent = "Lat: " + data.latitude + " | Lon: " + data.longitude;

                modalMeteoList.innerHTML = "";
                if (data.donneesMeteo && data.donneesMeteo.length > 0) {
                    data.donneesMeteo.forEach(m => {
                        const p = document.createElement('p');
                        p.textContent = `${m.dateMesure} : ${m.temperature}°C | Humidité: ${m.humidite}%`;
                        modalMeteoList.appendChild(p);
                    });
                } else {
                    modalMeteoList.textContent = "Aucune donnée météo disponible.";
                }

                modal.style.display = "flex";

                // Bouton pour rafraîchir la station
                refreshBtn.onclick = () => refreshStation(data.numero);
            })
            .catch(err => console.error("Erreur fetch station:", err));
    }

    // Rafraîchit une station individuelle
    function refreshStation(numero) {
        fetch(`refresh-station?numero=${numero}`, { method: 'POST' })
            .then(res => res.json())
            .then(data => {
                console.log("Station rafraîchie", data);
                openStationModal(numero); // recharger modal avec nouvelles données
            })
            .catch(err => console.error("Erreur refresh station:", err));
    }

    // Fermer le modal
    closeModal.onclick = () => modal.style.display = "none";
    window.onclick = e => { if (e.target === modal) modal.style.display = "none"; }
</script>

</body>
</html>
