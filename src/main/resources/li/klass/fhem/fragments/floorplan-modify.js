var idsToHide = [ "floorplans", "fpmenu", "menu", "logo" ];

document.body.setAttribute("style", "background-image: none");

var hideId = function(id) {
    var element = document.getElementById(id);
    if (element) {
        element.style.display = "none";
    }
}

for (var i = 0; i < idsToHide.length; i++) {
    try {
        hideId(idsToHide[i]);
    } catch (e) {
        console.log("cannot hide " + idsToHide[i] + ": " + e);
    }
}

// shift the background image to left, compute the left offset
var backImg = document.getElementById("backimg");
var backImgOffset = 0;
if (!! backImg) {
  backImgOffset = window.getComputedStyle(backImg, null).getPropertyValue("left").replace("px", "").replace("auto", "0");
  document.getElementById("backimg").style.left="0";
}

// move each child element to left by using the computed background image offset
var elements = document.getElementById("floorplan").getElementsByTagName("div");
if (!! elements) {
  for (var i = 0; i < elements.length; i++) {
    var element = elements[i];
    if (element.parentNode && element.parentNode.id == "floorplan") {
      var left = element.style.left.replace("px", "");
      element.style.left = (left - backImgOffset) + "px";
    }
  }
};

// override the implemented FW_cmd function to allow page
// reloading when the XMLHttpRequest is finished
function FW_cmd(arg) {
  var req = new XMLHttpRequest();
  req.onreadystatechange=function() {
  if (req.readyState == 4 && req.status == 200) {
    window.location.reload();
  }
};

req.open("GET", arg, true);
req.send(null);};