package so.vertxtest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	/** Constante de configuration : nom du bus de statut des applications. */
	public static final String CONFIG_APP_STATUS_QUEUE = "app.status.queue";
	/** Constante de configuration : nom du bus d'évènements des applications. */
	public static final String CONFIG_APP_EVENTS_QUEUE = "app.events.queue";
	
	
	/** Cache des ID de déploiement des applications. */
	private Map<String, String> appDeploymentID = new HashMap<String, String>();
	

	/*-------------------------------------------------------*/
	/* IMPLEMENTATION DE AbstractVerticle
	/*-------------------------------------------------------*/
	@Override
	public void start( Promise<Void> promise ) {
		// Le démarrage au sein de Vert.x est asynchrone et repose sur une prommesse.
		startHttpServer().setHandler(promise);
	}

	
	/*-------------------------------------------------------*/
	/* HTTP Server
	/*-------------------------------------------------------*/
	/** Démarrage asynchrone du serveur HTTP. */ 
	private Future<Void> startHttpServer() {
		Promise<Void> promise = Promise.promise();
		HttpServer server = vertx.createHttpServer();

		int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 10080);	// port 10080 par défaut.
		
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
		router.post().handler( BodyHandler.create() ); 
		router.get("/admin/:app/:cmd").handler( this::adminHandler );
		
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
		final List<String> validApps = Arrays.asList("name", "photo", "eventflow");
		final List<String> validCmds = Arrays.asList("start", "stop");
		
		// Application et verbe demandés via l'URL.
		String appName = context.request().getParam("app");
		String cmd = context.request().getParam("cmd");
		
		// L'application demandée est-elle valide ?
		if( appName==null || !validApps.contains(appName) ) {
			context.response().setStatusMessage("Application invalide.").setStatusCode(400).end();
			return;
		}
		
		// Le verbe utilisé est-il valide ?
		if( cmd==null || !validCmds.contains(cmd) ) {
			context.response().setStatusMessage("Verbe invalide.").setStatusCode(400).end();
			return;
		}
		
		Future<String> future = null;
		
		switch(cmd) {
		case "start":	future = startApp( appName );
			break;
		case "stop":	future = stopApp( appName );
			break;
		default: 
			context.response().setStatusMessage("Ce verbe n'est pas géré pour l'instant.").setStatusCode(400).end();
			return;
		}
		
		future.setHandler( ar -> {
			if( ar.succeeded() ) 
				context.response().putHeader("Content-Type", "application/json").end( ar.result() );
			else
				context.response().putHeader("Content-Type", "application/json").end( "{\"status\":\"undefined\"}" );
		});
	}
	
	/** Définit le statut "started" de l'application dont le nom est paramétré. */ 
	private Future<String> onAppDeployed( String appName ) {
		String appID = appDeploymentID.get( appName );
		System.out.println( appName +" application was deployed with id="+ appID );
		
		Promise<String> promise = Promise.promise();
		// La lecture du statut de l'application pourrait être dynamique.
		promise.complete("{\"app\":\""+appName+"\",\"status\":\"started\",\"id\":\""+appID+"\"}");
		return promise.future();
	}
	
	
	/** Définit le statut "stopped" de l'application dont le nom est paramétré */ 
	private Future<String> onAppUndeployed( String appName, String oldAppID ) {
		System.out.println( appName +" with id="+ oldAppID +" was undeployed successfully" );
		
		Promise<String> promise = Promise.promise();
		promise.complete("{\"app\":\""+appName+"\",\"status\":\"stopped\",\"id\":\""+oldAppID+"\"}");
		return promise.future();
	}
	

	/*-------------------------------------------------------*/
	/* IMPLEMENTATION DE IAppManager
	/*-------------------------------------------------------*/
	@Override
	public Future<String> startApp( final String appName ) {
		String appID = appDeploymentID.get( appName );
		if( appID==null ) {
			Promise<String> nameAppVerticleDeploymentResult = Promise.promise();

			Promise<String> nameAppVerticleDeployment = Promise.promise();
			vertx.deployVerticle( new NameAppVerticle(), nameAppVerticleDeployment );
			nameAppVerticleDeployment.future().setHandler( deploy -> {
				if( deploy.succeeded() ) {
					appDeploymentID.put(appName, deploy.result() );
					onAppDeployed(appName).setHandler( status -> {
						nameAppVerticleDeploymentResult.handle( status );
					});
				} else {
					nameAppVerticleDeploymentResult.fail( deploy.cause() );
				}
			});
			return nameAppVerticleDeployment.future();
		} else {
			// App déjà démarrée.
			return onAppDeployed( appID );
		}
	}

	@Override
	public Future<String> stopApp( final String appName ) {
		String appID = appDeploymentID.remove( appName );
		if( appID!=null ) {
			Promise<String> nameAppVerticleUndeploymentResult = Promise.promise();
			
			// Arrêt de l'application
			Promise<Void> nameAppVerticleUndeployment = Promise.promise();
			vertx.undeploy(appID, nameAppVerticleUndeployment);
			nameAppVerticleUndeployment.future().setHandler( undeploy -> {
				if( undeploy.succeeded() ) {
					onAppUndeployed(appName, appID).setHandler( status -> {
						nameAppVerticleUndeploymentResult.handle( status );
					});
				} else {
					nameAppVerticleUndeploymentResult.fail( undeploy.cause() );
				}
			});
			
			return nameAppVerticleUndeploymentResult.future();
		} else {
			// App déjà arrêtée.
			return onAppUndeployed( appName, "unknown" );
		}
	}

}
