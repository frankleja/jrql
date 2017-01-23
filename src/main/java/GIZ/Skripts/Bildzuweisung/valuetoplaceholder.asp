<%
' ------------------------------------------------------------------------------------------
'puts Nvalue into placeholderG 

Option Explicit
Server.ScriptTimeout = 500 
Response.Buffer = true
On Error Resume Next

Dim placeholderG 
Dim Nvalue
Dim xmlString
Dim xmlResult

placeholderG = (request.querystring("placeholder"))
Nvalue = (request.querystring("value"))

xmlString = "<IODATA loginguid=""" &Session("LoginGuid")& """ sessionkey=""" &Session("SessionKey")& """>"&_
"<ELEMENTS translationmode=""0"" action=""save"" reddotcacheguid=""""><ELT guid=""" &placeholderG& """ extendedinfo="""" type=""1"" value=""" &Nvalue& """>"&_
"</ELT></ELEMENTS></IODATA>"

xmlResult = sendXML(xmlString)
'response.write(xmlString)
function sendXML(xmlString) 
	Dim objData
	Dim sErrors
	Dim xmlResult
	set objData = Server.CreateObject("OTWSMS.AspLayer.PageData") 
	xmlResult = objData.ServerExecuteXML(xmlString, sErrors)
	if sErrors <> "" then
		errorFound = true
		errorMessage = sErrors
	end if
	sendXML = xmlResult

end function

%>