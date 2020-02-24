package so.vertxtest;

import io.vertx.core.Future;

/**
 * Cette interface décrit les fonctionnalités d'une Application, au sens de notre test :
 * - démarrage de l'appli,
 * - arrêt de l'appli.
 */
public interface IApp {

	/** Fonction de démarrage asynchrone d'une application. */
	public Future<Void> startTheApp();
	
	/** Fonction d'arrêt asynchrone d'une application. */
	public Future<Void> stopTheApp();
	
}
