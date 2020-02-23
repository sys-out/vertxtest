/*requirejs*/ define(
	[ '../directives/so-app-starter'
	],
function(
	soAppStarter
) {

	angular.module('soMenuApp', [
		,'soServices'
	])
	
	.directive('soAppStarter', soAppStarter)
	
	.service( 'soCacheService', ['soData', function(soData) {
		this.updateAppStatus = function( appName, newStatus ) {
			soData[appName].status = newStatus;
		}
		this.getAppStatus = function( appName ) {
			return soData[appName].status;
		}
		this.getAppUrl = function( appName ) {
			return soData[appName].url;
		}
		this.updateAppPort = function( appName, newPort ) {
			if( Number. isInteger(newPort) )
				soData[appName].url = "http://127.0.0.1:"+newPort;
			else 
				soData[appName].url = "";
		}
	}])
	
	.value('soData', {
		 "name": { status: 'stopped', id:'unknown', url:'' }
		,"photo": { status: 'stopped', id:'unknown', url:'' }
		,"eventFlow": { status: 'stopped', id:'unknown', url:'' }
	})

/*requirejs*/ });
