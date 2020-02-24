/**
 * 
 */
package so.vertxtest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

/**
 *
 */
public class EventAppVerticle extends AbstractVerticle implements IApp {

	/** Constante de configuration : port HTTP de l'application "event". */
	public static final String CONFIG_HTTP_SERVER_PORT = "app.event.http.server.port";
	/** Constante de configuration : nom du bus d'évènements des applications. */
	public static final String CONFIG_APP_EVENTS_QUEUE = "app.events.queue";

	/** fonction utilitaire. */
	private void notifyAll( String eventMsg ) {
		final String appEventsQ = config().getString( CONFIG_APP_EVENTS_QUEUE, CONFIG_APP_EVENTS_QUEUE );
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

		// Lecture du numéro de port paramétré pour cette application.
		int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 10083);	// port 10083 par défaut.
		
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

		// Gestion de la racine : /
		router.get("/").handler(  this::rootHandler );

		// Affichage de l'application : /event/*
		// Cette URL est servie par du contenu statique, voir dossier projet src/main/java/resources/event 
		router.route("/event/*").handler( StaticHandler.create().setWebRoot("event").setIndexPage("index.html") );
		router.route("/js/*").handler( StaticHandler.create().setWebRoot("js") );

		// Interface d'administration (web services)
		// Consiste en une websocket pour pousser les messages du bus vers le client web.
		SockJSHandler sockJSHandler = SockJSHandler.create( vertx );
		BridgeOptions bridgeOptions = new BridgeOptions().addOutboundPermitted(
			new PermittedOptions().setAddress( config().getString(CONFIG_APP_EVENTS_QUEUE, CONFIG_APP_EVENTS_QUEUE) )
		);
		router.mountSubRouter("/ws/event", sockJSHandler.bridge(bridgeOptions) );
		
		return router;
	}
	
	/*-------------------------------------------------------*/
	/* HTTP Handlers
	/*-------------------------------------------------------*/
	/** root : redirigé vers la page event, servie par du contenu statique. */
	private void rootHandler( RoutingContext context ) {
		context.response().setStatusCode(301);
		context.response().putHeader("Location", "/event/");
		context.response().end();
	}
	
	
	/*-------------------------------------------------------*/
	/* IMPLEMENTATION DE IApp
	/*-------------------------------------------------------*/
	@Override
	public Future<Void> startTheApp() {
		notifyAll( "L'application \"event\" démarre !" );
		return startHttpServer();
	}
	
	@Override
	public Future<Void> stopTheApp() {
		notifyAll( "L'application \"event\" s'arrête." );
		Promise<Void> promise = Promise.promise();
		promise.complete();
		return promise.future();
	}
	
}
