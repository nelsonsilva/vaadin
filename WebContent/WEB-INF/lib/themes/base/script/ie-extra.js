/** ** THIS FILE CONTAINS IE ONLY FIXES ** **/

/*
 * Add indexOf funtion for IE's Arrays
 */
if(!Array.prototype.indexOf) {
	Array.prototype.indexOf = function(value) {
 		for(var i = 0; i < this.length; i++) {
 			if(this[i] == value) {
 				return i;
 			}
 		}
 		return -1;
 	}
}
