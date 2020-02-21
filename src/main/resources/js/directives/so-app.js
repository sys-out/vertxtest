define([], function() {

/********************************
 * Controller for the directive
 */
function AppStatusCtrl($log, soAppService, soCacheService) {
	this.soAppService = soAppService;
	this.soCacheService = soCacheService;
	this.$log = $log;
}
AppStatusCtrl.$inject = ['$log', 'soAppService', 'soCacheService'];

//------- Controller's methods
/** App status. */
AppStatusCtrl.prototype.getStatus = function() {
	return this.soCacheService.getAppStatus(this.app) || "unknown"; 
}

/** Starts the application. */
AppStatusCtrl.prototype.start = function() {
	this.soCacheService.updateAppStatus( this.app, "starting" );
	this.soAppService.start( this.app ).then( d => {	// Success handler
			if( d && d.data && d.data.status ) {
				this.soCacheService.updateAppStatus( this.app, d.data.status );
			}
		}, () => { // Error handler
			this.soCacheService.updateAppStatus( this.app, "unknown" );
			this.$log.error( 'Error while starting app "'+ this.app +'"' );
		}
	);
}

/** Stops the application. */
AppStatusCtrl.prototype.stop = function() {
	this.soCacheService.updateAppStatus( this.app, "stopping" );
	this.soAppService.stop( this.app ).then( d => {	// Success handler
			if( d && d.data && d.data.status ) {
				this.soCacheService.updateAppStatus( this.app, d.data.status );
			}
		}, () => { // Error handler
			this.soCacheService.updateAppStatus( this.app, "unknown" );
			this.$log.error( 'Error while stopping app "'+ this.app +'"' );
		}
	);
}

/** Button's disabled property. */
AppStatusCtrl.prototype.isStarted = function() {
	return this.soCacheService.getAppStatus(this.app)==="started" || false; 
}


/********************************
 * The AppStatus directive.
 * Apply as element.
 * @require app, title
 */
function AppStatusDrtv() {
	this.restrict	= 'E';
	this.template	= `
		<div class="app">
			<header>{{ctrl.title}}</header>
			<div class="status"><span>Status: </span><span>{{ctrl.getStatus()}}</span></div>
			<div class="cmds">
				<div class="soStartButton">
					<button type="button" title="Démarrer" ng-click="ctrl.start()" ng-disabled="ctrl.isStarted()">Démarrer</button>
				</div>
				<div class="soStopButton">
					<button type="button" title="Arrêter" ng-click="ctrl.stop()" ng-disabled="!ctrl.isStarted()">Arrêter</button>
				</div>
			</div>
		</div>	
	`;
	this.transclude	= false;
	this.scope = {};
	this.bindToController	= {
		app:'@', title:"@"
	};
	this.controller	= AppStatusCtrl;
	this.controllerAs = 'ctrl';
	this.replace = true;
}

/**
 * Directive factory to be returned by requirejs.
 */
function directiveFactory() {
	return new AppStatusDrtv();
}

/*requirejs*/ return directiveFactory;});