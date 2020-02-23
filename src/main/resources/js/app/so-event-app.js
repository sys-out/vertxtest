/*requirejs*/ define(
	[ '../directives/so-event-viewer'
	],
function(
	soEventViewer
) {
	
	angular.module('soEventApp', [
		,'soServices'
	])
	
	.directive('soEventViewer', soEventViewer)
	
/*requirejs*/ });
