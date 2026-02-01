<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>Trouver la station la plus proche</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/styles/style.css">
</head>
<body>
<jsp:include page="/menu.jsp" />

<div class="container">
    <div class="sun">📍</div>
    <h1>Station la plus proche</h1>

    <!-- Formulaire de recherche stylé -->
    <div class="search-container">
        <form method="post" action="${pageContext.request.contextPath}/findStation">
            <input type="text" name="latitude" class="input-field" placeholder="Latitude (ex: 46.99)" required>
            <input type="text" name="longitude" class="input-field" placeholder="Longitude (ex: 6.93)" required>
            <button type="submit" class="btn">🔍 Chercher</button>
        </form>
    </div>

    <!-- Alerte d'erreur -->
    <c:if test="${not empty error}">
        <div id="errorAlert" class="error-alert">
            ⚠️ ${error}
        </div>
    </c:if>

    <!-- Résultat de la recherche -->
    <c:if test="${not empty station}">
        <c:set var="s" value="${station.stationMeteo}" />
        <div class="result-card">
            <div class="result-header">
                <h2 style="margin:0; color:#2c3e50;">${s.nom}</h2>
                <span style="color:#3498db; font-weight:700;">${station.pays.nom}</span>
                <div style="font-size: 0.8rem; color:#999; margin-top:5px;">
                    🌍 lat: ${s.latitude} | lon: ${s.longitude}
                </div>
            </div>

            <c:choose>
                <%-- On vérifie si la liste de météo n'est pas vide --%>
                <c:when test="${not empty s.donneesMeteo}">
                    <%-- On prend la première (la plus récente si tu as mis @OrderBy DESC) --%>
                    <c:set var="m" value="${s.donneesMeteo[0]}" />

                    <div style="font-size: 1.1rem; margin-bottom: 15px; color: #555;">
                        <strong>Dernière mesure :</strong> ${m.prettyDate}
                    </div>

                    <div class="weather-grid">
                        <div class="weather-item">🌡️ <b>Temp:</b> ${m.temperature}°C</div>
                        <div class="weather-item">☁️ <b>Ressenti:</b> ${m.ressenti}°C</div>
                        <div class="weather-item">💧 <b>Humidité:</b> ${m.humidite}%</div>
                        <div class="weather-item">🌤️ <b>Pression:</b> ${m.pression} hPa</div>
                        <div class="weather-item">💨 <b>Vent:</b> ${m.ventVitesse} km/h</div>
                        <div class="weather-item">
                            🌦️ <b>Pluie:</b> ${(not empty m.precipitation and m.precipitation > 0) ? m.precipitation : 'Pas de'} ${(not empty m.precipitation and m.precipitation > 0) ? 'mm' : 'pluie'}
                        </div>
                            <%-- On affiche juste l'heure du lever/coucher si possible --%>
                        <div class="weather-item">☀️ <b>Soleil:</b>
                                ${not empty m.leverSoleil ? m.leverSoleil.toString().substring(11,16) : '--:--'} /
                                ${not empty m.coucherSoleil ? m.coucherSoleil.toString().substring(11,16) : '--:--'}
                        </div>
                        <div class="weather-item">🌫️ <b>Visibilité:</b> ${m.visibilite} m</div>
                    </div>

                    <div style="margin-top:20px; font-style: italic; color:#7f8c8d; border-top: 1px solid #eee; pt: 10px;">
                        📝 Descriptions :
                        <c:forEach var="t" items="${m.texte}" varStatus="loop">
                            ${t}${!loop.last ? ', ' : ''}
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="text-align:center; padding:20px; color:#999;">
                        <p>Station trouvée, mais aucune donnée météo n'est enregistrée en base pour le moment.</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </c:if>
</div>

<script>
    // Script pour faire disparaître l'alerte d'erreur au bout de 4 secondes
    window.onload = function() {
        const alert = document.getElementById('errorAlert');
        if (alert) {
            setTimeout(function() {
                alert.style.transition = "opacity 0.8s";
                alert.style.opacity = "0";
                setTimeout(() => alert.remove(), 800);
            }, 4000);
        }
    };
</script>

</body>
</html>