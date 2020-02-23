define([], function() {

/********************************
 * Controller for the directive
 */
function PhotoChangerCtrl($log, soAppService) {
	this.$log = $log;
	this.soAppService = soAppService;
	this.photoData = "data:image/jpeg;base64,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";
}
PhotoChangerCtrl.$inject = ['$log', 'soAppService'];

//------- Controller's methods
/** Gestion du changement de photo */
PhotoChangerCtrl.prototype.changePhoto = function() {
	var element = document.getElementById('photo');
    var formData = new FormData();
    formData.append('file', element.files[0]);
    this.soAppService.changePhoto( formData ).then( d => {	// Success handler
    	if( d && d.data ) {
    		this.photoData = d.data;
    		this.$log.error( 'Photo changed successfully.' );
    	}
	}, () => { // Error handler
		this.$log.error( 'Error while changing photo.' );
	});
}


/********************************
 * The PhotoChanger directive.
 * Apply as element.
 * @require app
 */
function PhotoChangerDrtv() {
	this.restrict	= 'E';
	this.template	= `
		<div class="soPhotoChanger">
			<img ng-src="{{ctrl.photoData}}"/>
			<p>Vous pouvez charger une photo ci-dessous :</p>
			
			<form id="options">
				<label>Choisir une photo : <input id="photo" type="file" name="photo" /></label>
				<button ng-click="ctrl.changePhoto()">Cliquer pour valider le changement de photo</button>
			</form>

		</div>
	`;
	this.transclude	= false;
	this.scope = {};
	this.bindToController	= {
		//néant
	};
	this.controller	= PhotoChangerCtrl;
	this.controllerAs = 'ctrl';
	this.replace = true;
}


/**
 * Directive factory to be returned by requirejs.
 */
function directiveFactory() {
	return new PhotoChangerDrtv();
}

/*requirejs*/ return directiveFactory;});