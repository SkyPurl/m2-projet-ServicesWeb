# Explication du projet

Auteur : **Alex Collin**

## Présentation et lancement du projet
L'API a été développée avec Java 25, Spring Boot 4, H2, Keycloak, S3 et Lucene.  
L'[accès au Swagger](http://localhost:8080/swagger-ui/index.html#/) nécessite de renseigner un token pour les routes où l'on doit être connecté.  
J'ai énormément commenté le projet, donc cela peut faire beaucoup, mais je me dis que comme ça, au moins, ce sera clair pour vous.

Quelques explications sont présentes ici, mais l'essentiel se trouve dans les commentaires du code.

## Routes
- Comme expliqué dans les consignes, la moyenne vaut bien `-1` lorsque le restaurant n'a pas encore d'évaluation.
- J'ai interprété le mot « éditer » de l'énoncé comme le fait de pouvoir modifier, ce qui m'a amené à ajouter une route de modification.
- Le service d'évaluation ne reçoit pas directement le token, mais un booléen et un `String`, pour pouvoir être testé simplement.

## Sécurité
- J'ai choisi de mettre toutes les routes `GET` en public, sans besoin d'être connecté, excepté la route qui permet de récupérer ses propres évaluations. Pour toutes les autres, il faut être connecté, et pour certaines avoir le rôle `ADMIN`.
- Les rôles des clients sont dans `resource_access.coursm2.roles`, et pas là où Spring va les chercher par défaut.
- Par défaut, Spring lit le claim `scope` et crée des autorités `SCOPE_...`, d'où la classe `KeycloakRoleConverter` que j'ai créée, qui est branchée dans la `SecurityFilterChain` via un `JwtAuthenticationConverter`.

## Gestion des erreurs
- Si une ressource est absente, on reçoit une 404, ou une liste vide s'il s'agit d'une liste.
- Les erreurs sont au format classique : `code` + `message`.

## S3
- Les variables d'environnement liées au S3 ne sont pas dans le projet, elles sont donc à renseigner : `S3_ACCESS_KEY`, `S3_SECRET_KEY` ainsi que `S3_PREFIXE` (optionnel) pour bien organiser le S3.
- Comme je l'ai dit, j'ai décidé d'utiliser un préfixe, géré dans le `S3Repository`, qui permet de ranger dans les buckets ce qui m'appartient, que ce soit les images des restaurants ou celles des évaluations.
- Pour ajouter l'image du restaurant, c'est un `PUT`, car on ajoute ou on remplace l'ancienne, alors que pour les photos des évaluations, c'est un `POST`, car on ajoute seulement les images à la liste.
- Pour l'image du restaurant, si on en publie une alors qu'il y en a déjà une, la nouvelle remplace l'ancienne. Une amélioration possible serait de supprimer également l'ancienne image du S3.

## Index Lucene
- La recherche avec Lucene retourne uniquement les id des évaluations concernées. Je trouve plus pertinent de n'enregistrer dans les index que les id et le strict nécessaire à leur bon fonctionnement : cela prend moins de place et évite d'avoir toutes les données recopiées à 100 % dans les index.

## Modèle de données

La plupart du code présent dans les entités est du code que nous avions déjà vu, excepté l'annotation `@ElementCollection`, sur laquelle j'ai dû me documenter. Je savais qu'en base de données relationnelle il n'est pas possible de mettre une liste dans un champ, et en cherchant, j'ai trouvé cette annotation. Elle sert pour la liste des photos d'une évaluation : JPA les range dans une table à part, `evaluation_image`, sans avoir besoin de créer une entité de type photo. Cela permet aussi, même si ce n'est pas important ici, d'avoir un chargement « lazy » (les photos ne sont chargées que lorsqu'on en a besoin).  
J'ai également pris le parti d'initialiser les listes avec `new ArrayList<>()` afin d'éviter les `null`.  
Je me suis servi du projet de démo que nous avions fait en cours pour tout le modèle de données, car c'est assez simple de le faire à partir de ça.

## Architecture

En termes d'architecture, j'ai aussi repris celle du projet réalisé en cours. J'ai décidé de tout nommer en français, excepté les suffixes techniques comme `DTO`, `Entity`...

## Autres

- Je tiens à préciser que l’entièreté du code de ce projet a été écrite à la main : aucune ligne n’a été générée par une IA. Pour avancer, je me suis surtout servi de ce qu'on avait fait en cours et de la documentation. Quand je bloquais sur quelque chose, ou quand je mettais du temps à comprendre avec seulement la documentation (celle de Spring est assez verbeuse), je me suis servi de l'IA comme d'une aide. Je ne lui demandais pas le code du projet, mais des explications et des exemples concrets, en dehors du projet, assez clairs pour que je comprenne le principe et que je sache ensuite comment l’adapter moi-même ici.
- Pour la javadoc, j'ai utilisé le raccourci d'intelliJ `/**` + <kbd>Entrer</kbd>, qui génère automatiquement la javadoc pour les classes et méthodes. J'ai ensuite complété les javadocs générées.
- Pour finir, je voulais vous remercier pour ce cours, qui était vraiment incroyable.