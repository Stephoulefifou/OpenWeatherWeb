<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="ch.hearc.heg.scl.business.StationMeteo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>Stations</title>
</head>
<body>
<a href="hello">Aller à la page HelloServlet</a>
<h1>Stations</h1>

<c:choose>
    <c:when test="${empty stations}">
        <p>No stations found. Make sure you requested <a href="${pageContext.request.contextPath}/stations">/stations</a>.</p>
    </c:when>
    <c:otherwise>
        <ul>
            <c:forEach var="station" items="${stations}">
                <li>
                        ${station.nom} — lat: ${station.latitude} lon: ${station.longitude}
                </li>
            </c:forEach>
        </ul>
    </c:otherwise>
</c:choose>
</body>
</html>

