(function($){
	$.fn.newshortner = function(options) {

		var settings = $.extend({
			'lookinside': 'li',
			'makechanges': 'a.arrow-link'
		}, options);

		return this.find(settings['lookinside']).each(function(index) {
			var textIntroLength = 0;
			var shortText;
			var bigHeaderLength = 0;
			var minLength = 48;

			var bigHeader = $("p.news-caption").eq(index).text();
			var textIntro = $("a.arrow-link").eq(index).text();

			bigHeaderLength = bigHeader.length;

			if (bigHeaderLength >= 65) {
				//3 line min char is 65
				textIntroLength = minLength; //48
			}
			else if (bigHeaderLength >= 35 & bigHeaderLength <= 64) {
				//2 lines max char is 64
				textIntroLength = 90; //96 93 is good too
			}
			else if (bigHeaderLength <= 34) {
				//1 line max char is 34
				textIntroLength = 125; //135
			}

			if ((textIntro.length) >= minLength) {
				shortText = textIntro.substring(0, textIntroLength).split(" ").slice(0, -1).join(" ") +"...";

				$(settings['makechanges']).eq(index).html(shortText);

			}
		}); /* End of return this.find */
		
	}; //End fn.newshortner

})(jQuery);	