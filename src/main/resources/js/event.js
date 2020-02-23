/*Bootstrapping angularjs */
require([
		 '../js/services/so-services'
		,'../js/app/so-event-app']
	, function() {
	 angular.bootstrap(document, ['soEventApp']);
});