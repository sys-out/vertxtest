/*Bootstrapping angularjs */
require([
		 '../js/services/so-services'
		,'../js/app/so-menu-app']
	, function() {
	 angular.bootstrap(document, ['soMenuApp']);
});