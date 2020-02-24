/**
 * 
 */
package so.vertxtest;

import java.io.File;
import java.util.Base64;
import java.util.Set;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

/** Application "photo" */
public class PhotoAppVerticle extends AbstractVerticle implements IApp {

	/** Constante de configuration : port HTTP de l'application "photo". */
	public static final String CONFIG_HTTP_SERVER_PORT = "app.photo.http.server.port";
	/** Constante de configuration : nom du bus d'évènements des applications. */
	public static final String CONFIG_APP_EVENTS_QUEUE = "app.events.queue";

	/** fonction utilitaire. */
	private void notifyAll( String eventMsg ) {
		final String appEventsQ = config().getString( CONFIG_APP_EVENTS_QUEUE, CONFIG_APP_EVENTS_QUEUE );
		vertx.eventBus().publish( appEventsQ, eventMsg );
	}

	/** Photo de l'utilisateur. */
	private String base64EncodedPhoto = "";

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

		// Gestion du payload
		router.route().handler( BodyHandler.create().setBodyLimit(2097152) ); // upload limité à 2Mo
		
		// Gestion de la racine : /
		router.get("/").handler(  this::rootHandler );

		// Affichage de l'application : /photo/*
		// Cette URL est servie par du contenu statique, voir dossier projet src/main/java/resources/photo 
		router.route("/photo/*").handler( StaticHandler.create().setWebRoot("photo").setIndexPage("index.html") );
		router.route("/js/*").handler( StaticHandler.create().setWebRoot("js") );

		// Interface d'administration (web services)
		router.post("/ws/photo").handler( this::wsHandler );

		return router;
	}

	/*-------------------------------------------------------*/
	/* HTTP Handlers
	/*-------------------------------------------------------*/
	/** root : redirigé vers la page photo, servie par du contenu statique. */
	private void rootHandler( RoutingContext context ) {
		context.response().setStatusCode(301);
		context.response().putHeader("Location", "/photo/");
		context.response().end();
	}

	/** Accès au backend de l'application. */
	private void wsHandler( RoutingContext context ) {
		Set<FileUpload> uploads = context.fileUploads();
		int count = 0;
		for( FileUpload f : uploads ) {
			// On traite la 1ère image uploadée.
			if( ++count==1 ) {
				String mimeType=f.contentType();
				String fileName=f.fileName();
				// Lecture de l'image : le volume de données à traiter est "petit" donc on se permet une lecture synchrone.
				// En pratique, il faudrait probablement rendre ce traitement asynchrone pour respecter la règle d'or de Vert.x.
				Buffer uploaded = vertx.fileSystem().readFileBlocking(f.uploadedFileName());
				byte[] payload = uploaded.getBytes();
				
				base64EncodedPhoto = Base64.getEncoder().encodeToString( payload );
				String dataScheme = "data:"+mimeType+";base64,"+base64EncodedPhoto;
				
				System.out.println(dataScheme);
				
				// On signale ce changement dans le bus d'évènements
				notifyAll( "La photo a changé : "+ fileName );
				context.response().putHeader("Content-Type", "text/plain").end( dataScheme );
			}			
			new File(f.uploadedFileName()).delete();
		}
	}


	/*-------------------------------------------------------*/
	/* IMPLEMENTATION DE IApp
	/*-------------------------------------------------------*/
	@Override
	public Future<Void> startTheApp() {
		notifyAll( "L'application \"photo\" démarre !" );
		return startHttpServer();
	}

	@Override
	public Future<Void> stopTheApp() {
		notifyAll( "L'application \"photo\" s'arrête." );
		Promise<Void> promise = Promise.promise();
		promise.complete();
		return promise.future();
	}

}
