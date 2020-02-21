define([], function() {

/********************************
 * Controller for the directive
 */
function StopButtonCtrl(soAppService, soCacheService, $log) {
	this.soAppService = soAppService;
	this.soCacheService = soCacheService;
	this.$log = $log;
}
StopButtonCtrl.$inject = ['soAppService', 'soCacheService', '$log'];

//------- Controller's methods
/** Stops the application. */
StopButtonCtrl.prototype.stop = function() {
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
StopButtonCtrl.prototype.isDisabled = function() {
	return this.soCacheService.getAppStatus(this.app)==="stopped" || true; 
}


/********************************
 * The StopButton directive.
 * Apply as element.
 * @require app
 */
function StopButtonDrtv() {
	this.restrict	= 'E';
	this.template	= `
		<div class="soStopButton">
			<button type="button" title="{{ctrl.title | htmlToPlainText}}" ng-click="ctrl.stop()" ng-disabled="ctrl.isDisabled()"><div>{{ctrl.title}}</div></button>
		</div>
	`;
	this.transclude	= false;
	this.scope = {};
	this.bindToController	= {
		app:'@',	title:'@?'
	};
	this.controller	= StopButtonCtrl;
	this.controllerAs = 'ctrl';
	this.replace = true;
}

/**
 * Directive factory to be returned by requirejs.
 */
function directiveFactory() {
	return new StopButtonDrtv();
}

/*requirejs*/ return directiveFactory;});