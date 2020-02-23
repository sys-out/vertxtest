/*requirejs*/ define(
	[ '../directives/so-photo-changer'
	],
function(
	soPhotoChanger
) {
	
	angular.module('soPhotoApp', [
		,'soServices'
	])
	
	.directive('soPhotoChanger', soPhotoChanger)
	
/*requirejs*/ });
