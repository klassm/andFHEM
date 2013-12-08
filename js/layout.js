$(document).ready(function() {    
    navbarWidth = ($("#navbar").css("width").replace("px", "") * 1);
    
    animationTime = 500;
    
    isNavbarPermanent = false;
    
    toggleNavbarVisibility = function() {
      if (isNavbarPermanent) return;
      
      var navbar = $("#navbar");
      
      var visible = isNavbarVisible();
      
      if (visible) {
        hideNavbar();
      } else {
        showNavbar(true);
      }
      
      return;
      
      var height = navbar.css("height");
      var targetHeight;
      if (height != navbarInvisibleHeight && !! height) {
        targetHeight = navbarInvisibleHeight;
      } else {
        targetHeight = $(document).height();
      }
      navbar.css("height", targetHeight);
    }
    
    hideNavbar = function() {
      if (isNavbarPermanent) return;
      
      if (isClickLinkTarget(event.target)) return;
      
      var navbar = $("#navbar");
      if (!! isNavbarVisible()) {
        var targetLeft = (navbarWidth * -1) + "px";
        navbar.animate({
          left: targetLeft
        }, animationTime);
      }
    }
    
    showNavbar = function(animate) {
      var time = animationTime;
      if (! animate) time = 0;
      
      var navbar = $("#navbar");
      navbar.css("height", $(document).height());
      if (! isNavbarVisible()) {
        navbar.animate({
          left: "0px"
        }, time);
      }
    }
    
    isNavbarVisible = function() {
      var left = $("#navbar").css("left");
      return ! (left && left != "0px");
    }
    
    function isClickLinkTarget(target) {
      if (! target) return false;
      if (target.nodeName == "A") return true;
      return isClickLinkTarget(target.parentElement);
    }
    
    function handlePermanentNavbar(animate) {
      var width = $(window).width();
      var height = $(window).height();
      
      var navbar = $("#navbar");
      var content = $("#body");
      
      navbar.css("height", $(document).height()); 
      
      isNavbarPermanent = (width > height && width > 800);
      
      var time = animationTime;
      if (! animate) time = 0;
      
      if (isNavbarPermanent) {
        showNavbar(animate);
        content.animate({
          "margin-left": navbarWidth + "px"
        }, time);
      } else {
        hideNavbar();
        
        content.animate({
          "margin-left": "0px"
        }, time);
      }
    }
    
    $(window).resize(function() {
      handlePermanentNavbar(true);
    });
    
    handlePermanentNavbar(false);
});
