/*Bootstrapping angularjs */
require([
		 '../js/services/so-services'
		,'../js/app/so-name-app']
	, function() {
	 angular.bootstrap(document, ['soNameApp']);
});