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

    // IS_LOADING notification
	var evIS_LOADING = 'so:is_loading'; // + boolean
	this.onIsLoading = function(_this, callback) {
		return subscribe(evIS_LOADING, _this, callback);
    };
	this.notifyIsLoading = function(bool) {
        $rootScope.$emit(evIS_LOADING, bool);
    };
    
}]) // Fin du soNotifyService

;

})();