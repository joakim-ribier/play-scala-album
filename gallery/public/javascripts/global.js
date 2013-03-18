function changeDisableState(checkbox, elementToEnableOrDisable) {
	if (document.getElementById(checkbox).checked == true) {
		document.getElementById(elementToEnableOrDisable).disabled = "";
		document.getElementById(elementToEnableOrDisable).style.backgroundColor = '#FFFFFF';
	} else {
		document.getElementById(elementToEnableOrDisable).disabled = "disabled";
		document.getElementById(elementToEnableOrDisable).value = "";
		document.getElementById(elementToEnableOrDisable).style.backgroundColor = '#DCDCDC';
	}
}

function ifCheckIENavigator() {
	if (navigator.appName == "Microsoft Internet Explorer") {
		$('#app-message').fadeIn();
		$('#app-message').html(getI18NValue('page.main.update.site.ie.redirection.html'));
		setTimeout(function() {
		  $('#app-message').fadeOut();
		}, 15000);
	}
}

function redirectTo(url) {
	window.location.replace(url);
}

$(document).ready(function() {
	
	$.post('/album/get/post/value/from/key',
		{'configuration-key': 'app.default.lang'},
		function(data) {
			switch (data['status']) {
				case 'success':
					loadI18NFile('/album/i18n/lang/', data['value']);
					ifCheckIENavigator();
					break;
				case 'nothing' :
				case 'failed' :
				default :
					// no value for app.default.lang key
			}
	});
		
	$.post('/album/get/post/value/from/key',
		{'configuration-key': 'app.google.analytics'},
		function(data) {
			switch (data['status']) {
				case 'success':
					$('head').append('<script type="text/javascript">' + data['value'] + '</script>');
					break;
				case 'nothing' :
				case 'failed' :
				default :
					// no value for app.google.analytic key
			}
	});
	
	$("#app-message").live("click", function() {
		$(this).fadeOut();
	});
	
	$(".index-application-photo-title-view-post").live("mouseover", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/iconmonstr-details-large-view-icon-R250G199B19.x20.png");
	});
	
	$(".index-application-photo-title-view-post").live("mouseout", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/iconmonstr-details-large-view-icon.x20.png");
	});
	
	$(".index-application-photo-title-view-full-screen").live("mouseover", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/iconmonstr-eye-3-icon-R250G199B19.x20.png");
	});
	
	$(".index-application-photo-title-view-full-screen").live("mouseout", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/iconmonstr-eye-3-icon.x20.png");
	});
	
	$(".index-application-photo-title-view-full-screen-x32").live("mouseover", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/iconmonstr-eye-3-icon-R250G199B19.x32.png");
	});
	
	$(".index-application-photo-title-view-full-screen-x32").live("mouseout", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/iconmonstr-eye-3-icon.x32.png");
	});
	
	$(".index-application-grid-small-view-img").live("mouseover", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/grid-small-view-icon-R250G199B19.x20.png");
	});
	
	$(".index-application-grid-small-view-img").live("mouseout", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/grid-small-view-icon-white.x20.png");
	});
	
	$(".index-application-content-comment-delete-x20").live("mouseover", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/x-mark-5-icon-RG250G199B19.x20.png");
	});
	
	$(".index-application-content-comment-delete-x20").live("mouseout", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/x-mark-5-icon-white.x20.png");
	});
	
	$(".index-application-content-comment-update-x20").live("mouseover", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/pencil-6-icon-R250G199B19.x20.png");
	});
	
	$(".index-application-content-comment-update-x20").live("mouseout", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/pencil-6-icon-white.x20.png");
	});
	
	$(".index-application-content-comment-update-ok-x20").live("mouseover", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/check-mark-10-icon-R250G199B19.20x.png");
	});
	
	$(".index-application-content-comment-update-ok-x20").live("mouseout", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/check-mark-10-icon-white.x20.png");
	});
	
	$(".index-application-content-comment-undo-x20").live("mouseover", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/undo-4-icon-r250g199B19.x20.png");
	});
	
	$(".index-application-content-comment-undo-x20").live("mouseout", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/undo-4-icon-white.x20.png");
	});
	
	$(".index-application-page-comment-view-img").live("mouseover", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/iconmonstr-details-large-view-icon-R250G199B19.x20.png");
	});
	
	$(".index-application-page-comment-view-img").live("mouseout", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/details-large-view-icon-white.x20.png");
	});
	
	$(".index-application-page-refresh-view").live("mouseover", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/refresh-5-icon-R250G199B19.x20.png");
	});
	
	$(".index-application-page-refresh-view").live("mouseout", function() {
		 $(this).attr("src", "/assets/images/iconmonstr/refresh-5-icon-white.x20.png");
	});
});