cordova.define("cordova-plugin-volume.volume", function(require, exports, module) {
//
//	VolumeSlider.js
//	Volume Slider Cordova Plugin
//
//	Created by Tommy-Carlos Williams on 20/07/11. 
//  Updated by Samuel Michelot (orbitaloop) 05/11/2013
//	Copyright 2011 Tommy-Carlos Williams. All rights reserved.
//      MIT Licensed
//

var exec = require('cordova/exec');


module.exports = {

    /**
	 * Create a volume slider.
	 */
	createVolume : function(originx,originy,width,height, color) {
		exec(null, null, "VolumeSlider","createVolumeSlider", [originx, originy, width, height, color]);
	},
	
	/**
	 * Show the volume slider
	 */
	showVolume : function(enableRoute, enableSlider) {
		exec(null, null, "VolumeSlider","showVolumeSlider", [enableRoute, enableSlider]);
	},
	/**
	 * Hide the volume slider
	 */
	hideVolume : function() {
		exec(null, null, "VolumeSlider","hideVolumeSlider", []);
	}
};

});
