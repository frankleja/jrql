<%
'V11 version
Response.CodePage = 65001
Response.CharSet = "UTF-8"

' get and convert request
HdrB = Request.BinaryRead(Request.TotalBytes)
Hdr = Stream_BinaryToString(HdrB, "UTF-8")

' call CMS
sReturn = sendXML(Hdr)

' return result
Response.Write "<?xml version=""1.0"" encoding=""UTF-8""?>" & chr(13) & chr(10) Response.Write sReturn

'*** functions
'call CMS COM XMLServer
function sendXML (XMLString)	
	'set objIO = server.CreateObject("RDCMSASP.RdPageData")
	'objIO.XmlServerClassName="RDCMSServer.XmlServer"
	
	set objIO = server.CreateObject("OTWSMS.AspLayer.PageData")
	
	sendXML = objIO.ServerExecuteXml(XMLString, sErrors)
	if sErrors <> "" then
		XMLString = replace(XMLString, "<", "(")
		XMLString = replace(XMLString, ">", ")")
		sendXML = "<ERROR>Error occured: " & sErrors & " Request: " & XMLString & "</ERROR>"
	end if
	objIO = NULL
end function	

'BinaryToString converts binary data (VT_UI1 | VT_ARRAY Or MultiByte string) 'to a string (BSTR) using MultiByte VBS functions Function BinaryToString(Binary)
  Dim I, S
  For I = 1 To LenB(Binary)
    S = S & Chr(AscB(MidB(Binary, I, 1)))
  Next
  BinaryToString = S
End Function

'Stream_BinaryToString Function
'2003 Antonin Foller, http://www.motobit.com 'Binary - VT_UI1 | VT_ARRAY data To convert To a string 'CharSet - charset of the source binary data - default is "us-ascii"
Function Stream_BinaryToString(Binary, CharSet)
  Const adTypeText = 2
  Const adTypeBinary = 1
  
  'Create Stream object
  Dim BinaryStream 'As New Stream
  Set BinaryStream = CreateObject("ADODB.Stream")
  
  'Specify stream type - we want To save text/string data.
  BinaryStream.Type = adTypeBinary
  
  'Open the stream And write text/string data To the object
  BinaryStream.Open
  BinaryStream.Write Binary
  
  
  'Change stream type To binary
  BinaryStream.Position = 0
  BinaryStream.Type = adTypeText
  
  'Specify charset For the source text (unicode) data.
  If Len(CharSet) > 0 Then
    BinaryStream.CharSet = CharSet
  Else
    BinaryStream.CharSet = "us-ascii"
  End If
  
  'Open the stream And get binary data from the object
  Stream_BinaryToString = BinaryStream.ReadText 
  End Function %>
