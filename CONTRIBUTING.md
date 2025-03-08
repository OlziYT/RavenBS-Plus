# Guide de contribution à Raven bS++

Merci de votre intérêt pour contribuer au projet Raven bS++! Ce document fournit des lignes directrices pour contribuer au projet.

## Processus de contribution

1. Forkez le projet
2. Créez une branche pour votre fonctionnalité (`git checkout -b feature/amazing-feature`)
3. Committez vos changements (`git commit -m 'Add some amazing feature'`)
4. Poussez vers la branche (`git push origin feature/amazing-feature`)
5. Ouvrez une Pull Request

## Versionnement

Nous utilisons le [versionnement sémantique](https://semver.org/) avec le format MAJOR.MINOR.PATCH:

- **MAJOR**: changements incompatibles avec les versions précédentes
- **MINOR**: ajout de fonctionnalités rétrocompatibles
- **PATCH**: corrections de bugs rétrocompatibles

La version actuelle du mod est **1.5.3**.

### Mise à jour de la version

Lors de la mise à jour de la version, veuillez modifier les fichiers suivants:

1. `gradle.properties`: Mettez à jour la propriété `version`
2. `README.md`: Mettez à jour la version mentionnée dans la section Description
3. `CONTRIBUTING.md`: Mettez à jour la version actuelle mentionnée dans ce fichier

## Style de code

- Utilisez des noms de variables et de méthodes descriptifs
- Commentez votre code lorsque nécessaire
- Suivez les conventions de nommage Java standard
- Testez vos modifications avant de soumettre une PR

## Processus de build

Pour compiler le projet localement:

```bash
./gradlew build --no-daemon
```

Les fichiers JAR compilés seront disponibles dans le dossier `build/libs/`.

## Création d'une release

Les releases sont automatiquement créées lorsqu'un tag avec le format `vX.Y.Z` est poussé vers le dépôt. Par exemple:

```bash
git tag v1.5.3
git push origin v1.5.3
```

Cela déclenchera le workflow GitHub Actions qui compilera le projet et créera une release avec le fichier JAR.
