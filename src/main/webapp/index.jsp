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
                    <li class="station-card" onclick="openStationModal(${station.numero})">
                        <div class="country">${station.pays.nom}</div>
                        <div class="station">${station.nom}</div>

                        <c:if test="${not empty station.donneesMeteo}">
                            <c:set var="lastMeteo" value="${station.donneesMeteo[0]}" />
                            <div class="last-meteo">
                                Dernière mesure le ${lastMeteo.prettyDate}</div>
                            <div>
                                Température ${lastMeteo.temperature}°C - Humidité ${lastMeteo.humidite}%
                            </div>

                        </c:if>
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
        <!-- Cette zone va maintenant scroller toute seule -->
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

    function openStationModal(numero) {
        console.log("Ouverture modal pour station :", numero);

        fetch('station-json?numero=' + numero)
            .then(res => res.json())
            .then(data => {
                console.log("Données reçues :", data);

                // Remplissage des infos station (textContent est sûr)
                modalStationName.textContent =  data.nom +" - " + (data.pays ? data.pays.nom : "Inconnu");
                modalStationCoords.textContent = "Latitude : " + data.latitude + " - Longitude : " + data.longitude;

                // Remplissage de la liste météo
                modalMeteoList.innerHTML = "";


                if (data.donneesMeteo && data.donneesMeteo.length > 0) {
                    data.donneesMeteo.forEach(m => {
                        const p = document.createElement('p');
                        p.style.padding = "8px";
                        p.style.borderBottom = "1px solid #eee";
                        p.style.margin = "0";

                        // --- CORRECTION ICI : Utilisation de la concaténation (+) au lieu de $ {} ---
                        var html = '🕒 <strong>' + m.date + '</strong> : ';
                        html += '🌡️ ' + m.temp + '°C | ';
                        html += '💧 Humidité: ' + m.humi + '%';

                        p.innerHTML = html;
                        modalMeteoList.appendChild(p);
                    });
                } else {
                    modalMeteoList.innerHTML = "<p>Aucune donnée météo disponible.</p>";
                }

                // Gestion du bouton rafraîchir
                refreshBtn.onclick = function() {
                    refreshStation(data.numero);
                };

                modal.style.display = "flex";
            })
            .catch(err => {
                console.error("Erreur dans openStationModal:", err);
                showModalError("Erreur de chargement des données.");
            });
    }


    // Fonction utilitaire pour afficher un message d'erreur dans le modal
    function showModalError(message) {
        modalStationName.textContent = "Erreur";
        modalStationPays.textContent = "";
        modalStationCoords.textContent = "";
        modalMeteoList.textContent = message;
        modal.style.display = "flex";
    }


    function refreshStation(numero) {
        fetch('refresh-station?numero=' + numero, { method: 'POST' })
            .then(res => res.json())
            .then(data => openStationModal(numero)) // On ré-ouvre pour voir les nouvelles données
            .catch(err => console.error("Erreur refresh:", err));
    }

    closeModal.onclick = () => modal.style.display = "none";
    window.onclick = e => { if (e.target === modal) modal.style.display = "none"; }
</script>
</body>
</html>
