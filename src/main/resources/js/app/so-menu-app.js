/*requirejs*/ define(
	[ '../directives/so-app'
	],
function(
	soApp
) {

	angular.module('soMenuApp', [
		,'soServices'
		,'infinite-scroll'
	])
	
	.directive('soApp', soApp)
	
	.service( 'soCacheService', ['soData', function(soData) {
		this.updateAppStatus = function( appName, newStatus ) {
			soData[appName].status = newStatus;
		}
		this.getAppStatus = function( appName ) {
			return soData[appName].status;
		}
	}])
	
	.value('soData', {
		 "name": { status: 'stopped', id:'unknown', url:'' }
		,"photo": { status: 'stopped', id:'unknown', url:'' }
		,"eventFlow": { status: 'stopped', id:'unknown', url:'' }
	})

	.filter('htmlToPlainText', function() {
		return function(text) {
			return angular.element('<div>'+text+'</div>').text();
		}
	})
	
/*requirejs*/ });
