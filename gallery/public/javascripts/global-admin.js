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

function deletePhotoToUploadDirectory(filename) {
	var resultat = confirm(getI18NValue('js.global.delete.photo')); 
	if (resultat) {
		$.post('/album/admin/delete/photo/to/upload/directory',
			{'filename-post': filename},
			function(data) {
				switch (data['status']) {
						case 'success':
							window.location.replace("/album/admin/list/photo");
							break;
						case 'nothing' :
						case 'failed' :
						default :
							$("#app-message").html(data['error-message']);
							$('#app-message').fadeIn();
							setTimeout(function() {
			  					$('#app-message').fadeOut();
							}, 10000);
				}
		});
	}
}

function deleteVideoToUploadDirectory(filename) {
	var resultat = confirm(getI18NValue('js.global.delete.video')); 
	if (resultat) {
		$.post('/album/admin/delete/video/to/upload/directory',
			{'filename-post': filename},
			function(data) {
				switch (data['status']) {
						case 'success':
							window.location.replace("/album/admin/list/photo");
							break;
						case 'nothing' :
						case 'failed' :
						default :
							$("#app-message").html(data['error-message']);
							$('#app-message').fadeIn();
							setTimeout(function() {
			  					$('#app-message').fadeOut();
							}, 10000);
				}
		});
	}
}

function deleteMediaToAlbum(mediaId, mediaTitle) {
	var resultat = confirm(getI18NValue('js.global.delete.media') + " ( " + mediaTitle + " )"); 
	if (resultat) {
		$.post('/album/admin/delete/media/to/album',
			{'mediaid-post': mediaId},
			function(data) {
				switch (data['status']) {
						case 'success':
							var url = '/album/admin/display/all/media/message/key/' + data['message-key'];
							window.location.replace(url);
							break;
						case 'nothing' :
						case 'failed' :
						default :
							$("#app-message").html(data['error-message']);
							$('#app-message').fadeIn();
							setTimeout(function() {
			  					$('#app-message').fadeOut();
							}, 10000);
				}
		});
	}
}

$(document).ready(function() {
	
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
		
	$("#admin-photo-list").live("click", function() {
		window.location.replace("/album/admin/list/photo");
	});
	
	$("#admin-album-medias").live("click", function() {
		window.location.replace("/album/admin/display/all/media");
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
	
	$("#create-photo-description").keyup(function(event) {
		var html = $("#create-photo-description").val();
		if (html == null || html == '') {
			html = getI18NValue('page.adminAddPhoto.form.field.description');
		}
		$("#administrator-content-add-new-photo-form-description-preview").html('<span>' + html + '</span>');	
	});
});