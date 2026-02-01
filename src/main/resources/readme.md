Projet 2 OpenWeatherMap  – README
--------------------------------
Membres du groupe :

Nathan Altermatt  
Stéphane Thiébaud

--------------------------------
Description :

Projet client/serveur pour récupérer et afficher des données météo via l’API OpenWeather et les stocker dans une base Oracle.

--------------------------------
Fonctionnalités :

- Rechercher la météo par coordonnées (ajoute la station si absente).
- Lister les stations météo de la base.
- Afficher toutes les infos d’une station (toutes les mesures météo).
- Rafraîchir les données des stations existantes.

--------------------------------
Configuration base de données et mise en place :

Si vous lancez ce projet du zip, il suffit de charger Maven si nécéssaire et tout doit marcher.
Si vous copiez depuis git (https://github.com/Stephoulefifou/OpenWeather.git), il est nécéssaire d'ajouter le hibernate.cfg.xml dans les ressources du serveur (le fichier est dans le zip)

Le client est configuré pour aller sur le localhost.  
Si vous souhaitez changer ceci pour tester depuis un autre PC, il suffit de changer les lignes dans le MainClient et le MainServer. Dans les deux cas : commentez la ligne du localhost, décommentez l'autre, renseignez l'IP du serveur à l'endroit indiqué.

NOTE : Les schémas "normaux" ne permettent plus d'accès. nous avons ainsi mis nos schéma AGL, qui ont une théorique durée restante de 7 jours. Dans le cas ou cela ne fonctionnerait pas au démarrage, il faudrait remplacer les informations dans hibernate.cfg.xml par vos propres schémas Oracle. Hibernate se chargera de la création des tables.
