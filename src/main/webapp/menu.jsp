<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<nav class="navbar">
    <a href="${pageContext.request.contextPath}/findStation" class="nav-brand">
        OpenWeather<span>M-App</span>
    </a>

    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/findStation" class="nav-item">➕ Ajouter</a>
        <a href="${pageContext.request.contextPath}/stations" class="nav-item">📋 Stations</a>
        <a href="${pageContext.request.contextPath}/map" class="nav-item">🌍 Carte</a>
    </div>

    <div class="nav-spacer"></div>
</nav>
