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

<!-- Modal -->
<div id="stationModal" class="modal">
    <div class="modal-content">
        <span id="closeModal" class="close">&times;</span>

        <h2 id="modalStationName"></h2>
        <p id="modalStationCoords"></p>

        <div class="section" id="meteoMainSection">
            <h3>🌡️ Température & Humidité</h3>
            <div id="meteoMain"></div>
        </div>

        <div class="section" id="meteoWindSection">
            <h3>💨 Vent</h3>
            <div id="meteoWind"></div>
        </div>

        <div class="section" id="meteoCloudsSection">
            <h3>☁️ Nuages / Pluie</h3>
            <div id="meteoClouds"></div>
        </div>

        <div class="section" id="meteoSunSection">
            <h3>🌅 Soleil</h3>
            <div id="meteoSun"></div>
        </div>

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
            .then(res => {
                if (!res.ok) { // Gérer les erreurs HTTP comme 404, 500
                    throw new Error(`Erreur HTTP: ${res.status} ${res.statusText}`);
                }
                return res.json();
            })
            .then(data => {
                console.log("Données reçues (StationMeteo) :", data);

                // 1. Infos de base de la station
                modalStationName.textContent = data.nom + " (" + (data.pays ? data.pays.nom : "Inconnu") + ")";
                modalStationCoords.textContent = "Lat: " + data.latitude + " | Lon: " + data.longitude;

                // On récupère la dernière mesure enregistrée
                if (data.donneesMeteo && data.donneesMeteo.length > 0) {
                    const m = data.donneesMeteo;
                    console.log("DÉBOGAGE - Première mesure :", m); // Pour voir les noms de champs exacts

                    // 2. Température & Humidité
                    const description = m.texte ? m.texte.join(', ') : "Pas de description";
                    meteoMain.innerHTML = `
                        <p>🌡️ Température : \${m.temperature || '--'}°C (ressenti \${m.ressenti || '--'}°C)</p>
                        <p>Min / Max : \${m.tempMin || '--'}°C / \${m.tempMax || '--'}°C</p>
                        <p>💧 Humidité : \${m.humidite || 0}%</p>
                        <p>🔽 Pression : \${m.pression || 0} hPa</p>
                        <p>☁️ Conditions : \${description}</p>
                    `;

                    // 3. Vent (CORRIGÉ ICI POUR INTELIJ)
                    let windHtmlContent = `
                        <p>💨 Vitesse : \${m.ventVitesse || 0} m/s</p>
                        <p>🧭 Direction : \${m.ventDirection || 0}°</p>
                    `;
                    // Ajout conditionnel des rafales
                    if (m.ventRafales) {
                        // Ici, on est dans un bloc JavaScript "normal", donc pas besoin d'échapper le $
                        windHtmlContent += `<p>🌪️ Rafales : ${m.ventRafales} m/s</p>`;
                    }
                    meteoWind.innerHTML = windHtmlContent;

                    // 4. Nuages / Pluie
                    meteoClouds.innerHTML = `
                        <p>🌧️ Précipitations : \${m.precipitation || 0} mm</p>
                        <p>👁️ Visibilité : \${m.visibilite || '--'} m</p>
                    `;

                    // 5. Soleil (Lever / Coucher)
                    meteoSun.innerHTML = `
                        <p>🌅 Lever : \${formatTime(m.leverSoleil)}</p>
                        <p>🌇 Coucher : \${formatTime(m.coucherSoleil)}</p>
                    `;

                } else {
                    meteoMain.innerHTML = "<p>Aucune donnée météo disponible. Cliquez sur Rafraîchir.</p>";
                    meteoWind.innerHTML = "";
                    meteoClouds.innerHTML = "";
                    meteoSun.innerHTML = "";
                }

                // Bouton rafraîchir
                refreshBtn.onclick = () => refreshStation(numero);

                // Afficher la modale
                modal.style.display = "flex";
            })
            .catch(err => {
                console.error("Erreur dans openStationModal:", err);
                alert("Erreur lors du chargement des données de la station.");
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
