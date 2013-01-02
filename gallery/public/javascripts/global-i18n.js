function loadI18NFile(url, lang) {
	$.i18n.properties({
		name: 'messages',
		path: url,
		mode: 'map',
		cache: true,
		language: lang
	});
}

function getI18NValue(key) {
	return $.i18n.prop(key)
}

function getI18NValue(key, param) {
	return $.i18n.prop(key, param)
}
