Imports System.Net
Public Class Map
    Dim GetMapUrl As String
    Dim pngFileName As String
    Dim i As Image
    Dim scale_ As Double
    Public exitcode As Integer
    Public clickedAt As Point


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

    Public Sub New(ByVal _GetMapUrl As String, ByVal scale As Double)

        ' Dieser Aufruf ist für den Windows Form-Designer erforderlich.
        InitializeComponent()

        ' Fügen Sie Initialisierungen nach dem InitializeComponent()-Aufruf hinzu.
        GetMapUrl = _GetMapUrl
        scale_ = scale
        LabelScaleVertical.Text = "Vertical scale m/pixel: " & scale
    End Sub

    Private Sub ButtonScaleUp_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles ButtonScaleUp.Click
        exitcode = +1
        Close()
    End Sub

    Private Sub ButtonScaleDown_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles ButtonScaleDown.Click
        exitcode = -1
        Close()
    End Sub

    Private Sub PictureBox_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles PictureBox.Click
        Dim mev As Windows.Forms.MouseEventArgs
        mev = e
        Dim im As RectangleF
        im = PictureBox.Image.GetBounds(System.Drawing.GraphicsUnit.Pixel)
        clickedAt = New Point
        clickedAt.X = mev.X - (Width - im.Width) / 2 - im.Width / 2
        clickedAt.Y = mev.Y - (Height - im.Height) / 2 - im.Height / 2
        exitcode = 5
        Close()
    End Sub
End Class