package so.vertxtest;

import io.vertx.core.Future;

/**
 * Cette interface décrit les fonctionnalités du gestionnaire d'Applications.
 * - démarrage/arrêt de l'appli de changement de nom,
 * - démarrage/arrêt de l'appli de changement de photo,
 * - Démarrage/arrêt de l'appli de visualisation d'évènements.
 * 
 * Retourne une chaine de caractère contenant du JSON.
 * 
 * @author j.benoit
 */
public interface IAppManager {

	/** Démarrage de l'application de changement de nom. */
	public Future<String> startNameApp();
	/** Arrêt de l'application de changement de nom. */
	public Future<String> stopNameApp();

	/** Démarrage de l'application de changement de photo. */
	public Future<String> startPhotoApp();
	/** Arrêt de l'application de changement de photo. */
	public Future<String> stopPhotoApp();

	/** Démarrage de l'application de visualisation d'évènements. */
	public Future<String> startEventViewerApp();
	/** Arrêt de l'application de visualisation d'évènements. */
	public Future<String> stopEventViewerApp();

}
