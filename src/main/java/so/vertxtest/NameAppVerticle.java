/**
 * 
 */
package so.vertxtest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * @author j.benoit
 *
 */
public class NameAppVerticle extends AbstractVerticle implements IApp {

	/*-------------------------------------------------------*/
	/* IMPLEMENTATION DE AbstractVerticle
	/*-------------------------------------------------------*/
	@Override
	public void start( Promise<Void> promise ) {
		// Le démarrage au sein de Vert.x est asynchrone et repose sur une prommesse.
		startTheApp().setHandler(promise);
	}
	
	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		// L'arrêt au sein de Vert.x est asynchrone et repose sur une prommesse.
		stopTheApp().setHandler( stopFuture );
	}
	
	/*-------------------------------------------------------*/
	/* IMPLEMENTATION DE IApp
	/*-------------------------------------------------------*/
	@Override
	public Future<Void> startTheApp() {
		Promise<Void> promise = Promise.promise();
		promise.complete();
		return promise.future();
	}

	@Override
	public Future<Void> stopTheApp() {
		Promise<Void> promise = Promise.promise();
		promise.complete();
		return promise.future();
	}
}
