<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="ch.hearc.heg.scl.business.StationMeteo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>Station la plus proche</title>
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@300;600;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/styles/style.css">
</head>
<body>
<jsp:include page="/menu.jsp" />

<div class="container">
    <div class="sun">☀️</div>
    <h1>Station la plus proche</h1>

    <form method="post" action="${pageContext.request.contextPath}/findStation">
        <input type="text" name="latitude" placeholder="Latitude" required style="padding:10px; margin:5px;">
        <input type="text" name="longitude" placeholder="Longitude" required style="padding:10px; margin:5px;">
        <button type="submit" style="padding:10px 20px; margin:5px; border-radius:12px; background:#fff; color:#003049; font-weight:600;">Chercher</button>
    </form>

    <c:if test="${not empty error}">
        <div class="empty">${error}</div>
    </c:if>

    <c:if test="${not empty station}">
        <ul>
            <li>
                <div class="country">${station.pays.nom}</div>
                <div class="station">${station.stationMeteo.nom}</div>  <!-- ✅ -->
                <div class="coords">🌍 lat: ${station.stationMeteo.latitude} | lon: ${station.stationMeteo.longitude}</div>
            </li>
        </ul>
    </c:if>
</div>
</body>
</html>
