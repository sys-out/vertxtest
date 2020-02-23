define([], function() {

/********************************
 * Controller for the directive
 */
function NameChangerCtrl($log, soAppService, soData) {
	this.$log = $log;
	this.soAppService = soAppService;
	this.soData = soData;
	
	this.isChanging = false;
	this.newName = '';
}
NameChangerCtrl.$inject = ['$log', 'soAppService', 'soData'];

//------- Controller's methods
/** Changes the name for a new value. */
NameChangerCtrl.prototype.changeName = function() {
	if( this.isNameValid() ) {
		this.isChanging = true;
		this.soAppService.changeName( this.newName ).then( d => {	// Success handler
				this.soData.value = this.newName;
				this.newName = '';
				this.isChanging = false;
			}, () => { // Error handler
				this.$log.error( 'Error while changing name to "'+ this.newName +'"' );
				this.newName = '';
				this.isChanging = false;
			}
		);
	}
}
/** Button's disabled property. */
NameChangerCtrl.prototype.isNameValid = function() {
	return !this.isChanging && this.newName!=='' ;
}


/********************************
 * The NameChanger directive.
 * Apply as element.
 * @require app
 */
function NameChangerDrtv() {
	this.restrict	= 'E';
	this.template	= `
		<div class="soNameChanger">
			<p class="hello">Bonjour, {{ctrl.soData.value}}</p>
			<p>Vous pouvez changer de nom ci-dessous :</p>
			
			<div id="options">
				<label>Saisir un nouveau nom : <input ng-model="ctrl.newName" placeholder="{{ctrl.soData.value}}"/></label>
				<button ng-click="ctrl.changeName()" ng-enabled="ctrl.isNameValid()">Cliquer pour valider le changement de nom</button>
			</div>

		</div>
	`;
	this.transclude	= false;
	this.scope = {};
	this.bindToController	= {
		//néant
	};
	this.controller	= NameChangerCtrl;
	this.controllerAs = 'ctrl';
	this.replace = true;
}

/**
 * Directive factory to be returned by requirejs.
 */
function directiveFactory() {
	return new NameChangerDrtv();
}

/*requirejs*/ return directiveFactory;});