define([], function() {

/********************************
 * Controller for the directive
 */
function StartButtonCtrl(soAppService, soCacheService, $log) {
	this.soAppService = soAppService;
	this.soCacheService = soCacheService;
	this.$log = $log;
}
StartButtonCtrl.$inject = ['soAppService', 'soCacheService', '$log'];

//------- Controller's methods
/** Starts the application. */
StartButtonCtrl.prototype.start = function() {
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
/** Button's disabled property. */
StartButtonCtrl.prototype.isDisabled = function() {
	return this.soCacheService.getAppStatus(this.app)!=="stopped" || false; 
}


/********************************
 * The StartButton directive.
 * Apply as element.
 * @require app
 */
function StartButtonDrtv() {
	this.restrict	= 'E';
	this.template	= `
		<div class="soStartButton">
			<button type="button" title="{{ctrl.title | htmlToPlainText}}" ng-click="ctrl.start()" ng-disabled="ctrl.isDisabled()"><div>{{ctrl.title}}</div></button>
		</div>
	`;
	this.transclude	= false;
	this.scope = {};
	this.bindToController	= {
		app:'@',	title:'@?'
	};
	this.controller	= StartButtonCtrl;
	this.controllerAs = 'ctrl';
	this.replace = true;
}

/**
 * Directive factory to be returned by requirejs.
 */
function directiveFactory() {
	return new StartButtonDrtv();
}

/*requirejs*/ return directiveFactory;});