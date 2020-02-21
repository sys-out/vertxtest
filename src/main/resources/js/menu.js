/*Bootstrapping angularjs */
require([
		 '../js/libs/ng-infinite-scroll.min.js'
		,'../js/services/so-services'
		,'../js/app/so-menu-app']
	, function() {
	 angular.bootstrap(document, ['soMenuApp']);
	 angular.module('infinite-scroll').value('THROTTLE_MILLISECONDS', 250);
});