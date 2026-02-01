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
    <h1>Stations météo</h1>

    <div style="display:flex; justify-content:flex-end; margin-bottom:20px;">
        <form method="post" action="stations">
            <input type="hidden" name="action" value="refreshAll"/>
            <button type="submit">🔄 Rafraîchir toutes les stations</button>
        </form>
    </div>

    <c:choose>
        <c:when test="${empty stations}">
            <div>Aucune station trouvée.</div>
        </c:when>
        <c:otherwise>
            <ul>
                <c:forEach var="station" items="${stations}">
                    <li class="station-card" onclick="openStationModal(${station.numero})">
                        <div><strong>${station.nom}</strong> (${station.pays.nom})</div>
                        <c:if test="${not empty station.donneesMeteo}">
                            <c:set var="lastMeteo" value="${station.donneesMeteo[0]}" />
                            <div>
                                Dernière mesure : ${lastMeteo.prettyDate} |
                                Temp: ${lastMeteo.temperature}°C, Humidité: ${lastMeteo.humidite}%
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

<script>
    // Récupération des éléments principaux
    const modal = document.getElementById('stationModal');
    const closeModal = document.getElementById('closeModal');
    const modalStationName = document.getElementById('modalStationName');
    const modalStationCoords = document.getElementById('modalStationCoords');
    const refreshBtn = document.getElementById('refreshStationBtn');

    // Récupération des sections de données
    const meteoMain = document.getElementById('meteoMain');
    const meteoWind = document.getElementById('meteoWind');
    const meteoClouds = document.getElementById('meteoClouds');
    const meteoSun = document.getElementById('meteoSun');

    /**
     * Formate une date venant de Java (LocalDateTime) en HH:mm
     * Gère aussi bien les strings ISO que les objets Date
     */
    function formatTime(dateSource) {
        if (!dateSource) return "--:--";
        try {
            const date = new Date(dateSource);
            if (isNaN(date.getTime())) { // Si la date est invalide après parsing
                console.warn("Date invalide pour formatTime:", dateSource);
                return "--:--";
            }
            return date.toLocaleTimeString("fr-CH", {
                hour: "2-digit",
                minute: "2-digit"
            });
        } catch (e) {
            console.error("Erreur de formatage de date:", dateSource, e);
            return "--:--";
        }
    }

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

    function refreshStation(numero){
        // Change le texte du bouton pendant le chargement
        refreshBtn.textContent = "⌛ Chargement...";
        refreshBtn.disabled = true;

        fetch('refresh-station?numero=' + numero, {method:'POST'})
            .then(res => {
                refreshBtn.textContent = "🔄 Rafraîchir cette station"; // Réinitialise le texte
                refreshBtn.disabled = false;
                if (!res.ok) throw new Error('Erreur refresh côté serveur');
                return res.json();
            })
            .then(() => openStationModal(numero)) // Recharge la modale avec les nouvelles données
            .catch(err => {
                console.error("Erreur refresh:", err);
                alert("Erreur lors du rafraîchissement de la station.");
                refreshBtn.textContent = "🔄 Rafraîchir cette station";
                refreshBtn.disabled = false;
            });
    }

    // Fermeture de la modale
    closeModal.onclick = () => modal.style.display = "none";
    window.onclick = e => { if(e.target === modal) modal.style.display = "none"; }
</script>
</body>
</html>
