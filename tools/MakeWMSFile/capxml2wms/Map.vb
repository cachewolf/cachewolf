Imports System.Net
Public Class Map
    Dim GetMapUrl As String
    Dim pngFileName As String
    Dim i As Image

    Private Sub Map_FormClosing(ByVal sender As Object, ByVal e As System.Windows.Forms.FormClosingEventArgs) Handles Me.FormClosing
        Try
            i.Dispose()
            IO.File.Delete(pngFileName)
        Catch ex As Exception

        End Try
    End Sub

    Private Sub Map_Load(ByVal sender As Object, ByVal e As System.EventArgs) Handles Me.Load
        Me.Text = GetMapUrl
        Try
            Dim wc As New WebClient()
            pngFileName = IO.Path.GetTempFileName
            IO.File.Delete(pngFileName)
            wc.DownloadFile(GetMapUrl, pngFileName)
            i = Image.FromFile(pngFileName)
            PictureBox.Image = i
        Catch ex As Exception
            If IO.File.Exists(pngFileName) Then
                Process.Start("notepad.exe", pngFileName)
            End If
            MsgBox("Tut mir leid, das mag der wms-Server wohl so nicht", MsgBoxStyle.Exclamation)
            Close()
        End Try
    End Sub

    Public Sub New(ByVal _GetMapUrl As String)

        ' Dieser Aufruf ist für den Windows Form-Designer erforderlich.
        InitializeComponent()

        ' Fügen Sie Initialisierungen nach dem InitializeComponent()-Aufruf hinzu.
        GetMapUrl = _GetMapUrl

    End Sub
End Class