function updateImage(EndDate) {
    var valuetoplaceholderObj = new valuetoplaceholder("<%!! Context:CurrentPage.GetElementByName(std_EndDate).Id.ToString(N).ToUpper() !!%>",EndDate);
    console.log("value to placeholder fired");
}

function validate_image(){
var ShortUrlNew = document.getElementById('shortUrl').value
var LongUrlNew = document.getElementById('longUrl').value

console.log(imageL);
//updateImage(imageXS);
}
