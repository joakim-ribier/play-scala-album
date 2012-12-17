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

function fadeInPhoto(filename, title, description) {
	$('#display-photo-standard').fadeIn();
	var img = '<img src="/album/get/800x600/photo/' + filename + '" onclick="fadeOutPhoto();" />';
	$('#display-photo-standard-content-img').html(img);
	$('#display-photo-standard-content-title').html(title);
	$('#display-photo-standard-content-description').html(description);
}

function fadeOutPhoto() {
	$('#display-photo-standard').fadeOut();
}

function accesstoNewPhoto(name) {
	window.location.replace("/album/admin/add/new/photo/" + name);
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
		
		$(function() {
			if (navigator.appName == "Microsoft Internet Explorer") {
				$('#footer-title-ie').fadeIn();
				setTimeout(function() {
  				window.location.replace("http://www.google.com/chrome");
				}, 4000);
			}
		});
		
		$("#admin-photo-list").live("click", function() {
			window.location.replace("/album/admin/list/photo");
		});
});