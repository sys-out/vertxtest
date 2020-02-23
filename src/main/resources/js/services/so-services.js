(function() {

angular.module('soServices', [])

.value( 'app', {
	wsBase:"/ws/"
})

//-------------------------------------------------------------
.service('soAppService', ['$http', 'app', function($http, app) {
//-------------------------------------------------------------
	this.start = function( appName ) {
		return $http.post( 
			app.wsBase+appName+'/start'
		);
	}

	this.stop = function( appName ) {
		return $http.post(
			app.wsBase+appName+'/stop'
		);
	}
	
	this.changeName = function( newName ) {
		return $http.post(
			  app.wsBase+'name'
			, {"value": newName}
			, {
				headers: {
					'Content-Type':'application/json'
				}
			}
			
		);
	}
	
	this.changePhoto = function( photoData ) {
		return $http.post(
			  app.wsBase+'photo'
			, photoData
			, {
				headers: {
					'Content-Type':undefined
				}
			}
		);
	}
	
}]) // Fin du soAppService

;

})();