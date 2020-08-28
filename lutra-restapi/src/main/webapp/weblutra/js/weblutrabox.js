(function($){
    $.fn.styleddropdown = function(){
	return this.each(function(){
	    var obj = $(this);

	    //onclick event, 'list' fadein
	    obj.find('span').click(function() {
		obj.find('.list').show();
		obj.find('.list').hover(
		    function(){ },
		    function(){ $(this).hide(); });
	    });

	    //onclick event, change field value with selected 'list' item and fadeout 'list'
	    obj.find('.list li').click(function() {
		obj.find('span').html($(this).html());
		obj.find('input').val($(this).html());
		obj.find('.list').hide();
	    });
	});
    };
})(jQuery);


$(function() {

    var globalPrefixes;

    if ($("div#text-prefixes > pre.example").length) {
	globalPrefixes = $("div#text-prefixes > pre.example").text();
    } else if ($("div#text-prefixes > div.org-src-container > pre.src").length) {
        globalPrefixes = $("div#text-prefixes > div.org-src-container > pre.src").text();
    }
    
    // helper functions

    var generateID = (function(){
	var i=0;
	return function() {
	    return i++;
	}
    })();


    var popup = function (label, formName, list) {
	var output = "";
	output += " <div class='popup " + formName + "'>[" + label + ": <span>" + list[0] + "</span>]";
	output += "<ul class='list'>";
	list.forEach(function(value) { output +=  "<li>" + value + "</li>"; });
	output += "</ul>";
	output += "<input type='hidden' value='" + list[0] + "' name='" + formName + "'/>";
	output += "</div> ";
	return output;
    };

    var htmlTextarea = function (className) {
	return "<div style='display:none;' class=\"" + className + "\"><p><textarea></textarea></p></div>"
    }

    var htmlToggleButton = function (idElement, textDescription) {
	return "<span class='toggle' data-toggle='" + idElement + "'>"
	    + "<span class='arrow'>"
	    + ($('#' + idElement).is(":visible") ? "&#9661;" : "&#9655;")
	    + "</span> "
	    + textDescription + "</span>";
    }

    var doToggle = function(button) {
	var element = $("#" + $(button).attr("data-toggle"));
	element.toggle();

	if(element.is(":visible")) {
	    $(button).children("span.arrow").html("&#9661;");
	} else {
	    $(button).children("span.arrow").html("&#9655;");
	}
    }

    // contants
    var conMain = "div.weblutra";
    var conInputCls = "input";
    var conLibraryCls = "library";
    var conOutputCls = "example";

    $(conMain).each(function(){

	var me = $(this);

	// note that these may not be present:
	var inputCon = $(me).children("div." + conInputCls);
	var libraryCon = $(me).children("div." + conLibraryCls);
	var outputCon = $(me).children("pre." + conOutputCls);

	$(me).wrap("<form enctype='multipart/form-data'></form>");

	/*
	// add input if it does not exist, place relative to library
	if(!$(inputCon).length) {
	    var html = htmlTextarea(conInputCls);
	    if ($(libraryCon).length) {
		$(libraryCon).before(html);
	    } else {
		$(me).append(html);
	    }
	}

	// add library if it does not exist, place relative to input
	if(!$(libraryCon).length) {
	    var html = htmlTextarea(conLibraryCls);
	    if ($(inputCon).length) {
		$(inputCon).after(html);
	    } else {
		$(me).append(html);
	    }
	}
	*/

	// add output pre
	if(!$(outputCon).length) {
	    $(me).append("<pre style='display:none;' class='" + conOutputCls + "'></pre>");
	}

	// hide all output pre-s:
	$(me).find("pre." + conOutputCls).hide();

	// Add ids for easy access to elements when show/hide-ing
	$(me).find("textarea").each(function(){
	    $(this).attr("id", "lutra-" + generateID());
	});
	$(me).find("pre.example").each(function(){
	    $(this).attr("id", "lutra-" + generateID());
	});

	// Add menu to input
	$(me).children("div." + conInputCls).find("textarea").each(
	    function(){
		$(this).attr("name", conInputCls)
		    .before("<div class='menu'>"
			    + htmlToggleButton($(this).attr("id"), "Input")
			    + popup("Format", "inputFormat", ["stottr", "wottr", "bottr"])
			    + "</div>"
			   )
	    }
	);

	// Add menu to library
	$(me).children("div." + conLibraryCls).find("textarea").each(
	    function(){
		$(this).attr("name", conLibraryCls)
		    .before("<div class='menu'>"
			    + htmlToggleButton($(this).attr("id"), "Library")
			    + popup("Format", "libraryFormat", ["stottr", "wottr"])
			    + "</div>"
			   )}
	);

	// Add menu to output
	$(me).find("pre." + conOutputCls).each(
	    function(){
		$(this).before(
		    "<div class='menu'>"
			+ htmlToggleButton($(this).attr("id"), "Output")
			+ popup("Format", "outputFormat", ["wottr", "stottr"])
			+ popup("Action", "mode", ["expand", "format", "lint" /*, "expandLibrary", "formatLibrary" */ ])
			//+ popup("Fetch templates", "fetchMissing", ["true", "false"])
		    //+ popup("Load tpl.ottr.xyz", "loadStdLib", ["true", "false"])
			+ "<span class='lutra-send'> Run &#9654; </span>"
			+ "</div>"
		)
	    }
	);

    });


    /// show hide for textareas
    $("span.toggle").click(function(){
	doToggle($(this));
    });

    $("span.lutra-send").click(function(){

	var button = $(this);

	// do nothing if button is disabled
	if (button.attr('disabled')) {
	    return;
	}

	// disable button for 3 sec
	button.attr('disabled', 'true');
	button.css("color", "#ccc");
	setTimeout(function() {
	    button.removeAttr('disabled');
	    button.css("color", "#000");
	}, 3000);

	var form = $(this).closest("form");
	var elmOutput = $(form).find("pre." + conOutputCls);

	// show output container
	button.siblings("span.toggle").children("span.arrow").html("&#9661;");
	$(elmOutput).show();
	$(elmOutput).text("Loading...");

	var formData = new FormData(form[0]);
	formData.append('prefixes', globalPrefixes);

	$.ajax({
	    data: formData,
	    type: "post",
	    url: "https://sws.ifi.uio.no/lutra/expand",
	    processData: false,
	    contentType: false,
	    success: function(response) {
		$(elmOutput).text(response).html();
	    },
	    error: function (xhr, ajaxOptions, thrownError) {
		$(elmOutput).text('Error: ' + xhr.status + '\n' + thrownError).html();
	    }
	});
    });

    $('.popup').styleddropdown();

    $('input.reset-value').each(function() {
	var elmToUpdate = $(this).attr('data-name');
	var valueToSet = $(this).attr('value');

	$(this)
	    .closest("form")
	    .find("div.popup." + elmToUpdate + " > .list > li:contains('" + valueToSet + "')")
	    .click();
    });

});
