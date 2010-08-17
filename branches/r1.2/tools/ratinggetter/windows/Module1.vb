Imports System.Xml
Imports System.Net
Imports System.IO
Module Module1
    Declare Sub ExitProcess Lib "kernel32" (ByVal uExitCode As Long)
    Sub Main()
        Dim E As String = ""
        Dim url As String = "http://gcvote.de/getVotes.php?waypoints=" + Command$()
        Dim reader As XmlReader = XmlReader.Create(url)
        With reader
            Do While .Read
                Select Case .NodeType
                    Case Xml.XmlNodeType.Element
                        If .AttributeCount > 0 Then
                            While .MoveToNextAttribute
                                If .Name = "voteAvg" Then E = .Value : Exit Do
                            End While
                        End If
                End Select
            Loop
            .Close()
        End With
        If E = "" Then E = "00"
        ExitProcess(Left(E.Replace(".", ""), 2))
    End Sub
End Module
