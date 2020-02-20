package so.vertxtest;

import java.util.Arrays;
import java.util.List;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/** Gestionnaire d'applications */
public class MainVerticle extends AbstractVerticle implements IAppManager {
	
	/** Constante de configuration : port HTTP du gestionnaire d'applications. */
	public static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";
	
	private String nameAppID = null;
	private String photoAppID = null;
	

	/*-------------------------------------------------------*/
	/* IMPLEMENTATION DE AbstractVerticle
	/*-------------------------------------------------------*/
	@Override
	public void start( Promise<Void> promise ) {
		// Le démarrage au sein de Vert.x est asynchrone et repose sur une prommesse.
		startHttpServer().setHandler(promise);
	}

	/** Démarrage asynchrone du serveur HTTP. */ 
	private Future<Void> startHttpServer() {
		Promise<Void> promise = Promise.promise();
		HttpServer server = vertx.createHttpServer();

		int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 10080);
		
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
	
	/** Création du routeur pour le dispatch des requêtes HTTP reçues vers le Handler adéquat. */
	private Router generateRouter() {
		// Création d'un routeur pour traiter les requêtes HTTP reçues par le serveur.
		Router router = Router.router(vertx);
		// Affichage du menu des applications : /
		router.get("/").handler( this::menuHandler );

		// Interface d'administration (web services) 
		router.post().handler( BodyHandler.create() ); 
		router.post("/admin/:app/:cmd").handler( this::adminHandler );

/*
		router.post("/save").handler( this::pageUpdateHandler );
		router.post("/create").handler( this::pageCreateHandler );
		router.post("/delete").handler( this::pageDeletionHandler );
*/
		
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
	private void adminHandler( RoutingContext context ) {
		final List<String> validApps = Arrays.asList("name", "photo");
		final List<String> validCmds = Arrays.asList("start", "stop");
		
		// Application et verbe demandés via l'URL.
		String app = context.request().getParam("app");
		String cmd = context.request().getParam("cmd");
		
		// L'application demandée est-elle valide ?
		if( app==null || !validApps.contains(app) ) {
			context.response().setStatusMessage("Application invalide.").setStatusCode(400).end();
			return;
		}
		
		// Le verbe utilisé est-il valide ?
		if( cmd==null || !validCmds.contains(cmd) ) {
			context.response().setStatusMessage("Verbe invalide.").setStatusCode(400).end();
			return;
		}
		
		Future<String> async = null;
		
		switch(app) {
		case "name":
			switch(cmd) {
			case "start":	async = startNameApp();
				break;
			case "stop":	async = stopNameApp();
				break;
			default: 
				context.response().setStatusMessage("Ce verbe n'est pas géré pour l'instant.").setStatusCode(400).end();
				return;
			}
			break;
			
		case "photo":
			switch(cmd) {
			case "start":	async = startPhotoApp();
				break;
			case "stop":	async = stopPhotoApp();
				break;
			default: 
				context.response().setStatusMessage("Ce verbe n'est pas géré pour l'instant.").setStatusCode(400).end();
				return;
			}
			break;
		
		default:
			context.response().setStatusMessage("Cette application n'est pas gérée pour l'instant.").setStatusCode(400).end();
			return;
		}
		
		
		if( async!=null ) {
			context.response().putHeader("Content-Type", "application/json").end( async.result() );
		} else {
			context.response().putHeader("Content-Type", "application/json").end( "{\"status\":\"undefined\"}" );
		}
	}
	
	
	/** Définit le statut de l'application "name" */ 
	private Future<String> nameAppDeployed( String id ) {
		nameAppID = id;
		System.out.println( "nameApp deployed with id=" + nameAppID );
		
		Promise<String> promise = Promise.promise();
		promise.complete("{\"app\":\"name\",\"status\":\"started\",\"id\":\""+nameAppID+"\"}");
		return promise.future();
	}
	
	/** Définite le statut de l'application "name" */ 
	private Future<String> nameAppNotDeployed( Throwable t ) {
		nameAppID = null;
		System.out.println( "nameApp deployment unsuccessful" );
		t.printStackTrace();
		
		Promise<String> promise = Promise.promise();
		promise.complete("{\"app\":\"name\",\"status\":\"start failed\"}");
		return promise.future();
	}
	

	/*-------------------------------------------------------*/
	/* IMPLEMENTATION DE IAppManager
	/*-------------------------------------------------------*/
	@Override
	public Future<String> startNameApp() {
		if( nameAppID==null ) {
			Promise<String> nameAppVerticleDeployment = Promise.promise();
			vertx.deployVerticle(new NameAppVerticle(), nameAppVerticleDeployment);
			return nameAppVerticleDeployment.future().compose( this::nameAppDeployed, this::nameAppNotDeployed );
		} else {
			return nameAppDeployed( nameAppID );
		}
	}

	@Override
	public Future<String> stopNameApp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<String> startPhotoApp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<String> stopPhotoApp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<String> startEventViewerApp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<String> stopEventViewerApp() {
		// TODO Auto-generated method stub
		return null;
	}

}
