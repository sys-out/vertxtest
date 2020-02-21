package so.vertxtest;

import io.vertx.core.Future;

/**
 * Cette interface décrit les fonctionnalités du gestionnaire d'Applications.
 * Elle permet de démarrer (startApp) ou arrêter (stopApp) une application connue.
 */
public interface IAppManager {

	/** 
	 * Démarrage d'une application.
	 * @param appName nom de l'application à démarrer
	 * @return Une chaine de caractères au format JSON. 
	 */
	public Future<String> startApp( final String appName );
	
	/** Arrêt d'une application.
	 * @param appName nom de l'application à arrêter.
	 * @return Une chaine de caractères au format JSON. 
	 */
	public Future<String> stopApp( final String appName );

}
