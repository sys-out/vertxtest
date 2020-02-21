/**
 * 
 */
package so.vertxtest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * @author j.benoit
 *
 */
public class PhotoAppVerticle extends AbstractVerticle implements IApp {

	/** Constante de configuration : port HTTP de l'application "photo". */
	public static final String CONFIG_HTTP_SERVER_PORT = "app.photo.http.server.port";
	/** Constante de configuration : nom du bus d'évènements des applications. */
	public static final String CONFIG_APP_EVENTS_QUEUE = "app.events.queue";

	/** fonction utilitaire. */
	private void notifyAll( String eventMsg ) {
		final String appEventsQ = config().getString(CONFIG_APP_EVENTS_QUEUE, CONFIG_APP_EVENTS_QUEUE);
		vertx.eventBus().publish( appEventsQ, eventMsg );
	}
	
	
	/*-------------------------------------------------------*/
	/* IMPLEMENTATION DE AbstractVerticle
	/*-------------------------------------------------------*/
	@Override
	public void start( Promise<Void> promise ) {
		// Le démarrage au sein de Vert.x est asynchrone et repose sur une promesse.
		startTheApp().setHandler(promise);
	}
	
	@Override
	public void stop(Future<Void> promise) throws Exception {
		// L'arrêt au sein de Vert.x est asynchrone et repose sur une promesse.
		stopTheApp().setHandler( promise );
	}
	
	/*-------------------------------------------------------*/
	/* HTTP Server
	/*-------------------------------------------------------*/
	/** Démarrage asynchrone du serveur HTTP. */ 
	private Future<Void> startHttpServer() {
		Promise<Void> promise = Promise.promise();
		HttpServer server = vertx.createHttpServer();

		int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 10082);	// port 10082 par défaut.
		
		server
		.requestHandler( generateRouter() )
		.listen(portNumber, ar -> {
			if (ar.succeeded()) {
				System.out.println("HTTP server running on port "+ portNumber);
				promise.complete();
			} else {
				System.out.println("HTTP server not started.");
				ar.cause().printStackTrace();
				promise.fail( ar.cause() );
			}
		});

		return promise.future();
	}
	
	/** Création du dispatcher des requêtes HTTP reçues vers le Handler adéquat. */
	private Router generateRouter() {
		// Création du routeur pour traiter les requêtes HTTP reçues par le serveur.
		Router router = Router.router(vertx);
		// Affichage du menu des applications : /
		router.get("/").handler( this::menuHandler );

		// Interface d'administration (web services)
		// TODO
		//router.post().handler( BodyHandler.create() ); 
//		router.get("/ws/:app/:cmd").handler( this::apiHandler );
		
		return router;
	}
	
	/*-------------------------------------------------------*/
	/* HTTP Handlers
	/*-------------------------------------------------------*/
	/** Génération du menu. */
	private void menuHandler( RoutingContext context ) {
		HttpServerResponse response = context.response();
		response.putHeader("Content-Type", "text/html").sendFile("templates/menu.html");
	}
	
	/** Interface d'administration des applications (start, stop...) */
	private void wsHandler( RoutingContext context ) {
		//TODO
	}

	
	/*-------------------------------------------------------*/
	/* IMPLEMENTATION DE IApp
	/*-------------------------------------------------------*/
	@Override
	public Future<Void> startTheApp() {
		notifyAll( "L'application \"name\" démarre !" );
		return startHttpServer();
	}
	
	@Override
	public Future<Void> stopTheApp() {
		notifyAll( "L'application \"name\" s'arrête." );
		Promise<Void> promise = Promise.promise();
		promise.complete();
		return promise.future();
	}
	
}
