/**
 * 
 */
package so.vertxtest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

/** Application "name" */
public class NameAppVerticle extends AbstractVerticle implements IApp {

	/** Constante de configuration : port HTTP de l'application "name". */
	public static final String CONFIG_HTTP_SERVER_PORT = "app.name.http.server.port";
	/** Constante de configuration : nom du bus d'évènements des applications. */
	public static final String CONFIG_APP_EVENTS_QUEUE = "app.events.queue";

	/** fonction utilitaire. */
	private void notifyAll( String eventMsg ) {
		final String appEventsQ = config().getString( CONFIG_APP_EVENTS_QUEUE, CONFIG_APP_EVENTS_QUEUE );
		vertx.eventBus().publish( appEventsQ, eventMsg );
	}

	/** Nom de l'utilisateur. */
	private String value = "Guest";

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

		int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 10081);	// port 10081 par défaut.

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

		// Gestion du payload
		router.route().handler(BodyHandler.create());
		
		// Gestion de la racine : /
		router.get("/").handler(  this::rootHandler );

		// Affichage de l'application : /name/*
		// Cette URL est servie par du contenu statique, voir dossier projet src/main/java/resources/name 
		router.route("/name/*").handler( StaticHandler.create().setWebRoot("name").setIndexPage("index.html") );
		router.route("/js/*").handler( StaticHandler.create().setWebRoot("js") );

		// Interface d'administration (web services)
		router.post("/ws/name").handler( this::wsHandler );

		return router;
	}

	/*-------------------------------------------------------*/
	/* HTTP Handlers
	/*-------------------------------------------------------*/
	/** root : redirigé vers la page name, servie par du contenu statique. */
	private void rootHandler( RoutingContext context ) {
		context.response().setStatusCode(301);
		context.response().putHeader("Location", "/name/");
		context.response().end();
	}

	/** Accès au backend de l'application. */
	private void wsHandler( RoutingContext context ) {
		JsonObject payload = context.getBodyAsJson();
		value = payload.getString( "value" );
		
		// On signale ce changement dans le bus d'évènements
		notifyAll( "La nom a changé pour la valeur \""+ value +"\"" );
		context.response().end();
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
