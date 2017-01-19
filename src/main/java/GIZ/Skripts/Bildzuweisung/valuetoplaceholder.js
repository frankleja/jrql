function valuetoplaceholder(placeholderGUID, newValue) {
	$.ajax({
		url: '/CMS/plugins/valuetoplaceholder/valuetoplaceholder.asp?placeholder=' + placeholderGUID + '&value=' + newValue,
		dataType: 'script',
		async: false,
		success: function(){}
	});
	
}