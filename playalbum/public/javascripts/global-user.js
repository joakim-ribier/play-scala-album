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
	$('#index-application-content-comment-update-x20-' + commentId).attr("class", "icon-pencil cursorpointer");
	$('#index-application-content-comment-update-x20-' + commentId).attr("src", "/assets/images/iconmonstr/pencil-6-icon-white.x20.png");
	$('#index-application-content-comment-update-x20-' + commentId).attr("onclick", editOnClick);
}

$(document).ready(function() {
	
	$(".index-tag-already-exists").live("click", function() {
		$(this).addClass("index-tag-already-exists-select");
		$(this).removeClass("index-tag-already-exists");
		
		var divList = $(".index-tag-already-exists-select");
		
		var search = '';
		jQuery.each(divList, function(index, value) {
			search = search + '.' + $(value).html();
 		});
		search = search.substring(1);
		var url = '/album/page/1/tags/' + search;
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
		var url = '/album/page/1/tags/' + search;
		$('#index-tag-search-title-href').attr("href", url);
	});
	
	$("#index-application-content-configuration-new-mail-button").live("click", function() {
		var waitText = getI18NValue('js.global.send.user.email.wait');
		$("#index-application-content-configuration-info-email-callback").html(waitText);
		
		var errorText = getI18NValue('js.global.send.user.email.failed');
		
		var value = $("#index-application-content-configuration-new-mail-input").val();
		if (value != null && value != "") {
			$.post('/album/user/new/address/mail',
				{'address-post': value},
				function(data) {
				switch (data['status']) {
					case 'success':
						var okText1 = getI18NValue('js.global.send.user.email.success.1', data['return']);
						var okText2 = getI18NValue('js.global.send.user.email.success.2');
						$("#index-application-content-configuration-info-email-callback").html(okText1 + okText2);
						break;
					case 'nothing' :
					case 'failed' :
					default :
						$("#index-application-content-configuration-info-email-callback").html(errorText);
				}
			});
		} else {
			$("#index-application-content-configuration-info-email-callback").html(errorText);
		}
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