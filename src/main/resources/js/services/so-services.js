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

//-------------------------------------------------------------
.service('soNotifyService', ['$rootScope', function($rootScope) {
//-------------------------------------------------------------
	var subscribe = function(ev, _this, callback) {
		var closure = function(ev, data) {
			callback.call(_this, ev, data);
		}
        var fnUnsuscribe = $rootScope.$on(ev, closure);		// returns a deregistration function.
        _this.$scope.$on('$destroy', fnUnsuscribe);			// call it on scope destruction.
    };

    // NEW_MESSAGE notification
	var evNEW_MESSAGE = 'so:new_message'; // + string
	this.onNewMessage = function(_this, callback) {
		return subscribe(evNEW_MESSAGE, _this, callback);
    };
	this.notifyNewMessage = function(msg) {
        $rootScope.$emit(evNEW_MESSAGE, msg);
    };
    
}]) // Fin du soNotifyService

;

})();