# vertxtest
Vert.x est un kit d’outils pour la JVM, permettant d’implémenter simplement les principes du [_reactive manifesto_](http://www.reactivemanifesto.org/).
Nous allons tester ces fonctionnalités principales.

## Identification des qualités de Vert.x 3

 - Utilisation des *Verticle*s pour isoler chaque composant technique en une unité réutilisable de traitement d’évènements.
- Utilisation d'un *bus d’évènements* pour la communication inter-*Verticle*s.
Le bus est aussi accessible depuis un navigateur, via une websocket par exemple.

Nous allons exploiter ces qualités dans ce projet de test.

## Pile technique

En complément de Vert.x, nous utiliserons les bibliothèques suivantes :

- Maven (pom.xml) : pour la gestion du projet et de ses dépendances.
Vous pouvez démarrer le projet à l'aide de la commande 
> mvn test exec:java

- Vert.x web : bibliothèque permettant de  gérer plus facilement les payloads HTTP et le contenu statique.

- HTML/CSS/AngularJS/requireJS/SockJS pour le front-end.
Le front-end a été écrit à partir d'une source AngularJS déjà existante pour réduire le temps de développement. Utiliser Angular (v2) pour concevoir un nouveau projet serait plus judicieux aujourd'hui.



## Description

On va concevoir un gestionnaire d’applications, qui permettra d'arrêter/démarrer d'autres applications dynamiquement.

Une application consistera en :

- 1 Verticle dédié (dossier *src* java),
- 1 serveur web démarré par le *Verticle*, sur un port paramétrable dédié à l'application,
- 1 dossier de ressources dédié (dans le dossier *resources*) contenant des pages HTML statiques.

Le dossier *resources* contiendra aussi un ensemble de javascripts réutilisables dans chaque application (dossier *js*)

### Gestionnaire d’application

Le gestionnaire d’application sera le *Verticle* principal du projet, son « point d’entrée ».

Il proposera des méthodes pour démarrer/arrêter chaque autre application : voir l’interface **IAppManager**.

Il proposera aussi un ensemble de services web dédiés à la même tâche, de manière à pouvoir piloter le démarrage et l’arrêt des autres applications. 
Ces services web feront appel aux méthodes de l’interface IAppManager pour ne pas dupliquer de code inutilement.

Il proposera aussi une interface graphique pour une utilisation manuelle (contenu statique à l'adresse [http://127.0.0.1 :10080/](http://127.0.0.1%20:10080/)).

### Application « name »
Cette première application permettra à l'utilisateur de saisir un nom.
Chaque changement de nom donnera lieu à un message dans le bus d'évènements.

### Application « photo »
Cette autre application permettra à l'utilisateur d'uploader une photo. On limitera leur poids à 2Mo.
Chaque changement de photo donnera lieu à un message dans le bus d'évènements.

### Application  « event »
Cette dernière application permettra à l'utilisateur de visualiser les messages du bus d'évènement.
Elle se contentera pour cela d'ouvrir une websocket (en réalité une SockJS) pour recevoir les messages du bus.

## Conclusion
Développer des applications réactives devient très simple avec Vert.x.
Sa compatibilité avec plusieurs langages le rend aussi très attractif.
A l'issu de ce test, on pourrait encore améliorer la modularité du système en empaquetant les Verticles dans des paquets OSGi, par exemple.
On pourrait dans ce cas ajouter de nouvelles applications dynamiquement via un jar séparé.

