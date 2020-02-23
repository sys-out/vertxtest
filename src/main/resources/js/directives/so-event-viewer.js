define([], function() {

/********************************
 * Controller for the directive
 * $scope is required by the NotifyService.
 */
function EventViewerCtrl($log, $scope) {
	this.$log = $log;
	this.$scope = $scope;
	
	this.messages = [];
	
	var eb = new EventBus(window.location.protocol + "//" + window.location.host + "/ws/event");
	eb.onopen = () => {
		eb.registerHandler("app.events.queue", (error, message) => {
			if( message.body ) {
				$scope.$apply( () => {
					this.messages.push( message.body );
				});
			}
		});
	}
}
EventViewerCtrl.$inject = ['$log', '$scope'];


/********************************
 * The EventViewer directive.
 * Apply as element.
 * @require app
 */
function EventViewerDrtv() {
	this.restrict	= 'E';
	this.template	= `
		<div class="soEventViewer">
			<p>Flux des évènements</p>
			<ul>
				<li ng-repeat="msg in ctrl.messages track by $index">{{msg}}</li>
			</ul>
		</div>
	`;
	this.transclude	= false;
	this.scope = {};
	this.bindToController	= {
		//néant
	};
	this.controller	= EventViewerCtrl;
	this.controllerAs = 'ctrl';
	this.replace = true;
}


/**
 * Directive factory to be returned by requirejs.
 */
function directiveFactory() {
	return new EventViewerDrtv();
}

/*requirejs*/ return directiveFactory;});