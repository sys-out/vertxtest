/*requirejs*/ define(
	[ '../directives/so-name-changer'
	],
function(
	soNameChanger
) {
	
	angular.module('soNameApp', [
		,'soServices'
//		,'infinite-scroll'
	])
	
	.directive('soNameChanger', soNameChanger)
	
	.value('soData', {
		 "value": "Guest"
	})
	
/*requirejs*/ });
