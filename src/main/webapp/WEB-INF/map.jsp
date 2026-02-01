<%--
  Created by IntelliJ IDEA.
  User: Nathan
  Date: 13.01.2026
  Time: 11:27
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <title>Carte des stations météo</title>

    <!-- Leaflet CSS -->
    <link rel="stylesheet"
          href="https://unpkg.com/leaflet/dist/leaflet.css"/>

    <link rel="stylesheet"
            href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<h1>Carte des stations météo</h1>

<!-- CONTENEUR DE LA CARTE -->
<div id="map" style="height: 500PX;"></div>

<!-- Leaflet JS -->
<script src="https://unpkg.com/leaflet/dist/leaflet.js"></script>

<!-- Ton JS -->
<script src="${pageContext.request.contextPath}/js/map.js"></script>

</body>
</html>