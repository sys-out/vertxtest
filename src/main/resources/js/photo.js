/*Bootstrapping angularjs */
require([
		 '../js/services/so-services'
		,'../js/app/so-photo-app']
	, function() {
	 angular.bootstrap(document, ['soPhotoApp']);
});