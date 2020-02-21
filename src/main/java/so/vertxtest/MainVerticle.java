package so.vertxtest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

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
		router.get("/").handler(  this::rootHandler );

		// Affichage du menu des applications : /menu/*
		// Ces URLs sont servies par du contenu statique, voir dossier projet src/main/java/resources 
		router.route("/menu/*").handler( StaticHandler.create().setWebRoot("menu").setIndexPage("index.html") );
		router.route("/js/*").handler( StaticHandler.create().setWebRoot("js") );

		// Interface d'administration (web services)
		router.post("/ws/:app/:cmd").handler( this::wsHandler );

		return router;
	}
	
	/*-------------------------------------------------------*/
	/* HTTP Handlers
	/*-------------------------------------------------------*/
	/** root page : redirigée vers la page menu, servie par du contenu statique. */
	private void rootHandler( RoutingContext context ) {
		context.response().setStatusCode(301);
		context.response().putHeader("Location", "/menu/");
		context.response().end();
	}

	/** Services web d'administration des applications (start, stop...) */
	private void wsHandler( RoutingContext context ) {
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
	
	/** 
	 * Définit le statut "started" de l'application dont le nom est paramétré.
	 * L'info générée ici est basique, mais on pourrait imaginer la récupérer 
	 * à travers un bus de communication dédié aux statuts des applications.
	 */ 
	private Future<String> onAppDeployed( String appName ) {
		String appID = appDeploymentID.get( appName );
		System.out.println( appName +" application was deployed with id="+ appID );
		
		Promise<String> promise = Promise.promise();
		// La lecture du statut de l'application pourrait être dynamique.
		promise.complete("{\"app\":\""+appName+"\",\"status\":\"started\",\"id\":\""+appID+"\"}");
		return promise.future();
	}
	
	/** 
	 * Définit le statut "stopped" de l'application dont le nom est paramétré.
	 * L'info générée ici est basique, mais on pourrait imaginer la récupérer 
	 * à travers un bus de communication dédié aux statuts des applications.
	 */ 
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
		System.out.println( "Trying to start "+ appName );
		String appID = appDeploymentID.get( appName );
		if( appID==null ) {
			System.out.println( "Deploying verticle for "+ appName );
			Promise<String> verticleDeploymentResult = Promise.promise();

			Promise<String> verticleDeployment = Promise.promise();
			switch( appName ) {
				case "name": vertx.deployVerticle( new NameAppVerticle(), verticleDeployment ); break;
				case "photo": vertx.deployVerticle( new PhotoAppVerticle(), verticleDeployment ); break;
				default: vertx.deployVerticle( new EventFlowAppVerticle(), verticleDeployment ); break;
			}
			verticleDeployment.future().setHandler( deploy -> {
				if( deploy.succeeded() ) {
					System.out.println( "Verticle deployed with id="+ deploy.result());
					appDeploymentID.put(appName, deploy.result() );
					onAppDeployed(appName).setHandler( status -> {
						verticleDeploymentResult.handle( status );
					});
				} else {
					verticleDeploymentResult.fail( deploy.cause() );
				}
			});
			
			return verticleDeploymentResult.future();
		} else {
			// App déjà démarrée.
			System.out.println( appName +" is already started.");
			return onAppDeployed( appName );
		}
	}

	@Override
	public Future<String> stopApp( final String appName ) {
		System.out.println( "Trying to stop "+ appName );
		String appID = appDeploymentID.remove( appName );
		if( appID!=null ) {
			System.out.println( "Undeploying verticle for "+ appName );
			Promise<String> verticleUndeploymentResult = Promise.promise();
			
			// Arrêt de l'application
			Promise<Void> verticleUndeployment = Promise.promise();
			vertx.undeploy(appID, verticleUndeployment);
			verticleUndeployment.future().setHandler( undeploy -> {
				if( undeploy.succeeded() ) {
					System.out.println( "Verticle undeployed successfully.");
					onAppUndeployed(appName, appID).setHandler( status -> {
						verticleUndeploymentResult.handle( status );
					});
				} else {
					System.out.println( "Undeployement failed.");
					verticleUndeploymentResult.fail( undeploy.cause() );
				}
			});
			
			return verticleUndeploymentResult.future();
		} else {
			// App déjà arrêtée.
			System.out.println( appName +" is already stopped.");
			return onAppUndeployed( appName, "unknown" );
		}
	}
}
