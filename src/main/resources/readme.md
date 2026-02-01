Projet 3 OpenWeatherMap  – README
--------------------------------
**Membres du groupe :**

Nathan Altermatt  
Stéphane Thiébaud

--------------------------------
**Description :**

Projet web qui récupère des coordonnées GPS (latitude et longitude) et affiche les données de la station météo la plus proche et son emplacement sur une carte.

--------------------------------
**Fonctionnalités :**

-  Saisie d’une nouvelle station météo
-  Afficher la liste des stations météos de la DB
-  Affichage des détails d’une station météo et de ses mesures
-  Rafraichir les données météo d’une station particulière 
-  Rafraichir les données météo de toutes les stations 
-  Afficher une carte avec les stations

--------------------------------
**Configuration base de données et mise en place :**

Le projet utilise une base de données Oracle et les tables sont créées automatiquement par Hibernate.
1. Si le projet est lancé sur le zip fourni, il suffit d'importer le projet Maven :

   Toutes les dépendances sont automatiquement téléchargées.


2. Si le projet est cloné depuis GitHub, il faut vérifier le fichier hibernate.cfg.xml

   Si le fichier n'existe pas, prendre la template, supprimer le _.templete_ et renseigner les propriétés de configuration.

Pour lancer l'application, on démarre le serveur Tomcat, on déploie l'application web et on accède à l'URL suivant : <ins>http://localhost:8080</ins>.


_NOTE_ : Les schémas "normaux" ne permettent plus d'accès. Nous avons ainsi mis nos schémas AGL, qui ont une théorique durée restante de 7 jours. Dans le cas ou cela ne fonctionnerait pas au démarrage, il faudrait remplacer les informations dans hibernate.cfg.xml par vos propres schémas Oracle. Hibernate se chargera de la création des tables.
