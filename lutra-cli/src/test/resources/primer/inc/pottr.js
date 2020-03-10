

$(function() {
    
    var classHide = 'hideBtn';
    var classShow = 'showBtn';

    var toggleBefore = function (element) {
	if (element.hasClass(classHide)) {
	    element.removeClass(classHide)
	    element.addClass(classShow)
	} else {
	    element.removeClass(classShow)
	    element.addClass(classHide)
	}
    }

    /*
    // hide all h2's except first
    $('div.outline-text-2').slice(1).hide();
    $('div.outline-3').slice(1).hide();
    $('h2').slice(1).addClass('hideBtn');
    
    $('h2').css('cursor', 'pointer');
    $('h2').click(function(){
	$(this).siblings("div.outline-text-2").toggle();
	$(this).siblings("div.outline-3").toggle();
	toggleBefore($(this));
    });
    */

    $('div.outline-text-4').hide();
    $('h4').addClass(classHide);
    $('h4').css('cursor', 'pointer');
    $('h4').click(function(){
	$(this).siblings("div.outline-text-4").toggle();
	toggleBefore($(this));
    });
});


$(function() {
    
    $("div#toc > ul > ul").hide();

    $("div#text-prefixes > pre.example").hide();

});
