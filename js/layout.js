$(document).ready(function() {
    isNavbarPermanent = false;
    navbarVisibleHeight = "100%";
    navbarInvisibleHeight = "0px";
         
    hideNavbar = function() {
      if (isNavbarPermanent) return;
      
      if (isClickLinkTarget(event.target)) return;
      
      var navbar = document.getElementById("navbar");
      var height = navbar.style.height;
      if (height != "0px") {
        navbar.style.height = "0px";
      }
    }
    
    toggleNavbarVisibility = function() {
      if (isNavbarPermanent) return;
      
      var navbar = $("#navbar");
      var height = navbar.css("height");
      var targetHeight;
      if (height != navbarInvisibleHeight && !! height) {
        targetHeight = navbarInvisibleHeight;
      } else {
        targetHeight = $(document).height();
      }
      navbar.css("height", targetHeight);
    }
    
    function isClickLinkTarget(target) {
      if (! target) return false;
      if (target.nodeName == "A") return true;
      return isClickLinkTarget(target.parentElement);
    }
    
    function handlePermanentNavbar() {
      var width = $(window).width();
      var height = $(window).height();
      
      var navbar = $("#navbar");
      var content = $("#content");
      
      isNavbarPermanent = (width > height && width > 800);
      
      if (isNavbarPermanent) {
        navbar.css("height", $(document).height());              
        content.css("margin-left", "240px");
      } else {
        navbar.css("height", navbarInvisibleHeight); 
        content.css("margin-left", "0px");
      }
    }
    
    $(window).resize(function() {
      handlePermanentNavbar();
    });
    
    handlePermanentNavbar();
    
});
