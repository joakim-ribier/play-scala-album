function fileSelected() {
	var files = document.getElementById('photos').files;
	if (files) {
		
		var count = files.length;
	  var fileSize = 0;
	  for (var i = 0; i < files.length; ++i) {
	  	fileSize += files[i].size;
	  }
	  
	  var fileSizeToString = "";
	  if (fileSize > 1024 * 1024) {
	  	fileSizeToString = (Math.round(fileSize * 100 / (1024 * 1024)) / 100).toString() + 'MB';
		} else {
	  	fileSizeToString = (Math.round(fileSize * 100 / 1024) / 100).toString() + 'KB';
	  }
	  
	 	document.getElementById('admin-upload-form-count-photo-value').innerHTML = count;
	 	document.getElementById('admin-upload-form-size-photo-value').innerHTML = fileSizeToString;
		
		document.getElementById('admin-upload-form-photo-count').style.display = 'block';
		document.getElementById('admin-upload-form-photo-size').style.display = 'block';
	}
}

function uploadFile() {
  var fd = new FormData();
  var files = document.getElementById('photos').files;
  if (files) {
  	for (var i = 0; i < files.length; ++i) {
	  	fd.append("photos", files[i]);
	  }
	  
	  var xhr = new XMLHttpRequest();
	  xhr.upload.addEventListener("progress", uploadProgress, false);
	  xhr.addEventListener("load", uploadComplete, false);
	  xhr.addEventListener("error", uploadFailed, false);
	  xhr.addEventListener("abort", uploadCanceled, false);
	  xhr.open("POST", "/album/upload");
	  
	  xhr.send(fd);
	  
	  document.getElementById('admin-upload-form-photo-details-upload').style.display = 'block';
  }
}

function uploadProgress(evt) {
  if (evt.lengthComputable) {
    var percentComplete = Math.round(evt.loaded * 100 / evt.total);
    document.getElementById('admin-upload-form-photo-progress-number').innerHTML = percentComplete.toString() + '%';
  }
}

function uploadComplete(evt) {
  document.getElementById('admin-upload-form-photo-progress-number').innerHTML = '100%';
  window.location.replace("/album/admin/list/photo");
}

function uploadFailed(evt) {
 	document.getElementById('admin-upload-form-photo-details-upload-error').style.display = 'block';
}

function uploadCanceled(evt) {
  alert("The upload has been canceled by the user or the browser dropped the connection.");
}
      
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

function updateTimeDisplay(e) {
  alert('loadstart');
}

function fadeInPhoto(id, filename, mediaType, title, description, tags, firstPhotoId, lastPhotoId) {
	if (mediaType == "photo") {
		var photo = '<img src="/album/get/800x600/photo/' + filename + '" onclick="fadeOutPhoto(\'display-photo-standard\');"/>';
		$('#display-photo-standard-content-img').html(photo);
	} else {
		var videoDefaultDescriptionLabel = getI18NValue('app.global.video.description.defaut');
		var video = '<video width="800" height="600" controls="controls" src="/album/get/media/video/standard/' + filename + '">' + videoDefaultDescriptionLabel + '</video>';
		$('#display-photo-standard-content-img').html(video);
	}

	$('#display-photo-standard-content-title').html(title);
	$('#display-photo-standard-content-description').html(description);

	var next = '<div id="display-photo-standard-content-header-next" onclick="nextPhoto(\'' + id + '\', \'' + tags + '\')"></div>';
	var previous = '<div id="display-photo-standard-content-header-prev" onclick="previousPhoto(\'' + id + '\', \'' + tags + '\')"></div>';
	if (id == firstPhotoId) {
		next = '<div id="display-photo-standard-content-header-next-nothing"></div>';
	}
	if (id == lastPhotoId) {
		previous = '<div id="display-photo-standard-content-header-prev-nothing"></div>';
	}

	var close = '<div id="display-photo-standard-content-header-close" onclick="fadeOutDisplayMediaStandard(\'display-photo-standard\');"></div>';
 	$('#display-photo-standard-content-header').html(next + previous + close);
	
	$('#display-photo-standard').fadeIn();
}

function previousPhoto(id, tags) {	
	$.getJSON('/album/photo/previous/' + id + '/tags/' + tags,
    function(data) {
			refreshDatas(data, tags, 'previous');
	});
}

function nextPhoto(id, tags) {
	$.getJSON('/album/photo/next/' + id + '/tags/' + tags,
    function(data) {
			refreshDatas(data, tags, 'next');
	});
}
 
function refreshDatas(data, tags, previousOrNext) {
	switch (data['status']) {
		case 'success':
			var id = data['id'];
			var filename = data['filename'];
			var title = data['title'];
			var description = data['desc'];
			var mediaType = data['mediaType'];
			
			if (mediaType == "photo") {
			
				var imgDiv = $('#display-photo-standard-content-img').find('img');
				if (imgDiv[0] == null) {
					var photo = '<img src="/album/get/800x600/photo/' + filename + '" onclick="fadeOutPhoto(\'display-photo-standard\');"/>';
					$('#display-photo-standard-content-img').html(photo);
				} else {
					var url = '/album/get/800x600/photo/' + filename;
        	$('#display-photo-standard-content-img > img').attr("src", url);
				}

			} else {
				var videoDefaultDescriptionLabel = getI18NValue('app.global.video.description.defaut');
				var video = '<video width="800" height="600" controls="controls" src="/album/get/media/video/standard/' + filename + '">' + videoDefaultDescriptionLabel + '</video>';
				$('#display-photo-standard-content-img').html(video);
			}
	
			$('#display-photo-standard-content-title').html(title);
			$('#display-photo-standard-content-description').html(description);
		
			var next = '<div id="display-photo-standard-content-header-next" onclick="nextPhoto(\'' + id + '\', \'' + tags + '\')"></div>';
			var previous = '<div id="display-photo-standard-content-header-prev" onclick="previousPhoto(\'' + id + '\', \'' + tags + '\')"></div>';
			if (previousOrNext == 'next') {
				if (data['is'] != 'true') {
					next = '<div id="display-photo-standard-content-header-next-nothing"></div>';
				}
			} else {
				if (data['is'] != 'true') {
					previous = '<div id="display-photo-standard-content-header-prev-nothing"></div>';
				}
			}
			
			var close = '<div id="display-photo-standard-content-header-close" onclick="fadeOutDisplayMediaStandard(\'display-photo-standard\');"></div>';
			$('#display-photo-standard-content-header').html(next + previous + close);		

			break;
		case 'nothing' :
		case 'failed' :
		default :
			// nothing
	}
}

function fadeOutPhoto(div) {
	$('#' + div).fadeOut();
	var video = $('#display-photo-standard-content-img > video')[0];
	if (video != null) {
		$('#display-photo-standard-content-img > video')[0].pause();
	}
}

function fadeOutDisplayMediaStandard(div) {
	var video = $('#display-photo-standard-content-img > video')[0];
	if (video != null) {
		$('#display-photo-standard-content-img > video')[0].pause();
	}
	fadeOutPhoto(div);
}

function accesstoNewPhoto(name) {
	window.location.replace("/album/admin/add/new/photo/" + name);
}

function redirectionToAddVideo(video) {
	window.location.replace("/album/admin/media/redirect/add/video/" + video);
}

function deleteNotificationMessage(messageId) {
	$.post('/album/admin/notification/message/delete',
		{'messageid-post': messageId},
		function(data) {
			switch (data['status']) {
					case 'success':
						window.location.replace("/album/admin/notification");
						break;
					case 'nothing' :
					case 'failed' :
					default :
						$("#notification-message-error-label").html(data['error-message']);
			}
	});
}

function deleteNotification(notificationId) {
	$.post('/album/admin/notification/delete',
		{'notificationid-post': notificationId},
		function(data) {
			switch (data['status']) {
					case 'success':
						window.location.replace("/album/admin/notification");
						break;
					case 'nothing' :
					case 'failed' :
					default :
						$("#notification-message-error-label").html(data['error-message']);
			}
	});
}

function closeNotification(notificationId) {
	var checked = $('#index-application-notification-header-close-check').is(':checked');
	if (checked == false) {
		$.post('/album/user/popup/notify/close',
			{'notificationid-post': notificationId},
			function(data) {
				$('#index-application-notification-display').fadeOut();
		});
	} else {
		$('#index-application-notification-display').fadeOut();
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

function deleteCommentWithConfirmationWindow(mediaId, page, byTags, commentId) {
	var resultat = confirm(getI18NValue('js.global.delete.comment.answer')); 
	if (resultat) {
		$.post('/album/media/post/remove/comment',
			{'commentid-post': commentId},
			function(data) {
				switch (data['status']) {
					case 'success':
					case 'nothing' :
					case 'failed' :
					default :
						var url = '/album/get/media/' + mediaId + '/post/page/' + page + '/tags/' + byTags + '/message/key/' + data['key'];
						window.location.replace(url);
					
				}
		});
	}
}

function updateComment(mediaId, page, byTags, commentId) {
	var id = "#index-application-content-comments-one-to-one-textarea-" + commentId;
	var value = $(id).val();
	var commentTex = null;
	if (value != null && value != '') {
		var commentTex = value;
	}
	$.post('/album/media/post/update/comment',
		{'commentid-post': commentId, 'commenttext-post': commentTex},
		function(data) {
			switch (data['status']) {
				case 'success':
				case 'nothing' :
				case 'failed' :
				default :
					var url = '/album/get/media/' + mediaId + '/post/page/' + page + '/tags/' + byTags + '/message/key/' + data['key'];
					window.location.replace(url);
			}
	});
}

function editComment(mediaId, page, byTags, commentId) {
	var id = "#index-application-content-comments-one-to-one-p-" + commentId;
	var commentTex = $(id + ' > p').html();
	$('#index-application-content-comments-one-to-one-p-undo-' + commentId).html(commentTex);
	var placeholder = getI18NValue('js.global.edit.textarea.placeholder');
	var textarea = '<textarea id="index-application-content-comments-one-to-one-textarea-' + commentId + '" rows="5" placeholder="' + placeholder + '">' + commentTex + '</textarea>';
	$(id + ' > p').html(textarea);
	
	var undoOnClick = "undoComment('" + mediaId + "', '" + page + "', '" + byTags + "', '" + commentId + "');";
	$('#index-application-content-comment-undo-x20-' + commentId).attr("title", getI18NValue('js.global.edit.undo.img.title'));
	$('#index-application-content-comment-undo-x20-' + commentId).attr("class", "index-application-content-comment-undo-x20 cursorpointer");
	$('#index-application-content-comment-undo-x20-' + commentId).attr("src", "/assets/images/iconmonstr/undo-4-icon-white.x20.png");
	$('#index-application-content-comment-undo-x20-' + commentId).attr("onclick", undoOnClick);
	
	var updateOnClick = "updateComment('" + mediaId + "', '" + page + "', '" + byTags + "', '" + commentId + "');";
	$('#index-application-content-comment-update-x20-' + commentId).attr("title", getI18NValue('js.global.edit.save.img.title'));
	$('#index-application-content-comment-update-x20-' + commentId).attr("class", "index-application-content-comment-update-ok-x20 cursorpointer");
	$('#index-application-content-comment-update-x20-' + commentId).attr("src", "/assets/images/iconmonstr/check-mark-10-icon-white.x20.png");
	$('#index-application-content-comment-update-x20-' + commentId).attr("onclick", updateOnClick);
}

function undoComment(mediaId, page, byTags, commentId) {
	var commentText = $('#index-application-content-comments-one-to-one-p-undo-' + commentId).html();
	var id = "#index-application-content-comments-one-to-one-p-" + commentId;
	$(id + ' > p').html(commentText);
	
	$('#index-application-content-comment-undo-x20-' + commentId).removeAttr("title");
	$('#index-application-content-comment-undo-x20-' + commentId).removeAttr("class");
	$('#index-application-content-comment-undo-x20-' + commentId).attr("src", "/assets/images/iconmonstr/iconmonstr-undo-4-icon.x20.png");
	$('#index-application-content-comment-undo-x20-' + commentId).removeAttr("onclick");
	
	var editOnClick = "editComment('" + mediaId + "', '" + page + "', '" + byTags + "', '" + commentId + "');";
	$('#index-application-content-comment-update-x20-' + commentId).attr("title", getI18NValue('page.post.update.commment.img.title'));
	$('#index-application-content-comment-update-x20-' + commentId).attr("class", "index-application-content-comment-update-x20 cursorpointer");
	$('#index-application-content-comment-update-x20-' + commentId).attr("src", "/assets/images/iconmonstr/pencil-6-icon-white.x20.png");
	$('#index-application-content-comment-update-x20-' + commentId).attr("onclick", editOnClick);
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
	
	$("#add-new-tag-input").keypress(function(event) {
		if (event.which == 32) {
	 		
	 		var tagValue = $.trim($("#add-new-tag-input").val()).replace(/[/]/g, "_");
	 		if (tagValue != "") {
		 		var contentInputTags = $("#administrator-content-add-new-photo-form-input-tags").html();
		   	contentInputTags = contentInputTags + '<div class="add-new-tag">' +  tagValue + '</div>';
		   	$('#administrator-content-add-new-photo-form-input-tags').html(contentInputTags);
		   	$("#add-new-tag-input").val("");
				
				var length = $('#administrator-content-add-new-photo-form-input-tags-hidden > *').length;
				var contentInputHiddenTags = $('#administrator-content-add-new-photo-form-input-tags-hidden').html();
				contentInputHiddenTags = contentInputHiddenTags + '<input type="hidden" name="tags[' + length + ']" value="' + tagValue + '"/>';
				$('#administrator-content-add-new-photo-form-input-tags-hidden').html(contentInputHiddenTags);
	 		}
		}
	});
		
	$(".add-new-tag").live("click", function() {
		var content = $(this).html();
		$(this).fadeOut();
		var inputTab = $("#administrator-content-add-new-photo-form-input-tags-hidden :input");
		
		var contentInputHiddenTags = "";
		var id = -1;
		jQuery.each(inputTab, function(index, value) {
				if (content != value.value) {
					id +=1;
					contentInputHiddenTags = contentInputHiddenTags + '<input type="hidden" name="tags[' + id + ']" value="' + value.value + '"/>';
				}
 		});
		$('#administrator-content-add-new-photo-form-input-tags-hidden').html(contentInputHiddenTags);
	});
	
	$(".admin-tag-already-exists").live("click", function() {
		var tagValue = $(this).html();
 		var contentInputTags = $("#administrator-content-add-new-photo-form-input-tags").html();
   		contentInputTags = contentInputTags + '<div class="add-new-tag">' +  tagValue + '</div>';
   		$('#administrator-content-add-new-photo-form-input-tags').html(contentInputTags);
		
		var length = $('#administrator-content-add-new-photo-form-input-tags-hidden > *').length;
		var contentInputHiddenTags = $('#administrator-content-add-new-photo-form-input-tags-hidden').html();
		contentInputHiddenTags = contentInputHiddenTags + '<input type="hidden" name="tags[' + length + ']" value="' + tagValue + '"/>';
		$('#administrator-content-add-new-photo-form-input-tags-hidden').html(contentInputHiddenTags);
	});
		
	$(".index-tag-already-exists").live("click", function() {
		$(this).addClass("index-tag-already-exists-select");
		$(this).removeClass("index-tag-already-exists");
		
		var divList = $(".index-tag-already-exists-select");
		
		var search = '';
		jQuery.each(divList, function(index, value) {
			search = search + '.' + $(value).html();
 		});
		search = search.substring(1);
		
		var numberPage = $('#index-application-page-number-value').html();
		var url = '/album/page/' + numberPage + '/tags/' + search;
		$('#index-tag-search-title-href').attr("href", url);
	});
	
	$(".index-tag-already-exists-select").live("click", function() {
		$(this).addClass("index-tag-already-exists");
		$(this).removeClass("index-tag-already-exists-select");

		var divList = $(".index-tag-already-exists-select");
		
		var search = 'all';
		if (divList.size() > 0) {
			search = ''
			jQuery.each(divList, function(index, value) {
				search = search + '.' + $(value).html();
 			});
 			search = search.substring(1);
		}
		
		var numberPage = $('#index-application-page-number-value').html();
		var url = '/album/page/' + numberPage + '/tags/' + search;
		$('#index-tag-search-title-href').attr("href", url);
	});
	
	$("#admin-photo-list").live("click", function() {
		window.location.replace("/album/admin/list/photo");
	});
	
	$("#admin-notification-title").live("click", function() {
		window.location.replace("/album/admin/notification");
	});
	
	$("#footer-information-email-ckx").live("change", function() {
		if ($(this).is(':checked')) {
			$('#footer-information-new-email').fadeIn();
		} else {
			$('#footer-information-new-email').fadeOut();
		}
	});
	
	$("#footer-information-email-button").live("click", function() {
		var waitText = 'Envoi en cours ...';
		$("#footer-information-new-email-return").html(waitText);
		
		var errorText = getI18NValue('js.global.send.user.email.failed');
		
		var value = $("#footer-information-email-input").val();
		if (value != null && value != "") {
			$.post('/album/user/new/address/mail',
				{'address-post': value},
				function(data) {
				switch (data['status']) {
					case 'success':
						var okText1 = getI18NValue('js.global.send.user.email.success.1', data['return']);
						var okText2 = getI18NValue('js.global.send.user.email.success.2');
						$("#footer-information-new-email-return").html(okText1 + okText2);
						break;
					case 'nothing' :
					case 'failed' :
					default :
						$("#footer-information-new-email-return").html(errorText);
				}
			});
		} else {
			$("#footer-information-new-email-return").html(errorText);
		}
	});
	
	$("#textarea-notification").keyup(function(event) {
		var html = $("#textarea-notification").val();
		if (html == null || html == '') {
			html = getI18NValue('page.adminNotification.form.field.notification.html');
		}
		$("#administrator-content-main-notification-create-preview").html('<span>' + html + '</span>');	
	});
	
	$(".notification-list-checkbox").live("change", function(event) {
		var divList = $("#notification-create-alarm-form input[type=checkbox]");
	  var nbr = -1;
	  for (var i = 0; i < divList.length; ++i) {
	  	if (divList[i].checked) {
	  		nbr++;
	  		divList[i].name = 'messageIds[' + nbr + ']';
	  	} else {
				divList[i].name = 'nothing';
	  	}
	  }
	});
	
	$("#app-message").live("click", function() {
		$(this).fadeOut();
	});
	
	$("#create-photo-description").keyup(function(event) {
		var html = $("#create-photo-description").val();
		if (html == null || html == '') {
			html = getI18NValue('page.adminAddPhoto.form.field.description');
		}
		$("#administrator-content-add-new-photo-form-description-preview").html('<span>' + html + '</span>');	
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