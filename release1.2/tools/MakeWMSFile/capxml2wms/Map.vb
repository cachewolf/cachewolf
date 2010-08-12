Imports System.Net
Public Class Map
    Dim GetMapUrl As String
    Dim pngFileName As String
    Dim i As Image
    Dim sf As Double
    Dim center As capxml2wms.point
    Public exitcode As Integer
    Public clickedAt As Point
    Dim scaleLowerLimit, scaleUpperLimit As Double


    Private Sub Map_FormClosing(ByVal sender As Object, ByVal e As System.Windows.Forms.FormClosingEventArgs) Handles Me.FormClosing
        Try
            i.Dispose()
            IO.File.Delete(pngFileName)
        Catch ex As Exception

        End Try
    End Sub

    Private Sub Map_Load(ByVal sender As Object, ByVal e As System.EventArgs) Handles Me.Load
        _MapLoad()
    End Sub

    Friend Sub New(ByVal _GetMapUrl As String, ByVal _scale As Double, ByVal _sfmin As Double, ByVal _sfmax As Double, ByVal _center As capxml2wms.point)

        ' Dieser Aufruf ist f�r den Windows Form-Designer erforderlich.
        InitializeComponent()

        ' F�gen Sie Initialisierungen nach dem InitializeComponent()-Aufruf hinzu.
        GetMapUrl = _GetMapUrl
        sf = _scale
        scaleLowerLimit = _sfmin
        scaleUpperLimit = _sfmax
        center = _center
        LabelScaleVertical.Text = "Vertical scale m/pixel: " & sf
        LabelScaleDiagonal.Text = "Diagonal scale m/pixel: " & sf * Math.Sqrt(2)
        ButtonUseAsMaxScale.Enabled = True
        ButtonUseAsMinScale.Enabled = False
        ButtonAddToRecommendedScale.Enabled = False
    End Sub

    Private Sub _MapLoad()
        sf = (scaleLowerLimit + scaleUpperLimit) / 2
        Dim BBoxString As String = capxml2wms.ToBBox(center, capxml2wms._srs, capxml2wms._pixelwidth * sf)
        GetMapUrl = capxml2wms.makeGetMapUrlFromBBox(BBoxString)
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
        LabelScaleVertical.Text = "Vertical scale m/pixel: " & sf
        LabelScaleDiagonal.Text = "Diagonal scale m/pixel: " & sf * Math.Sqrt(2)
    End Sub

    Private Sub ButtonScaleUp_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles ButtonScaleUp.Click
        scaleLowerLimit = sf
        _MapLoad()
    End Sub

    Private Sub ButtonScaleDown_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles ButtonScaleDown.Click
        scaleUpperLimit = sf
        _MapLoad()
    End Sub

    Private Sub PictureBox_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles PictureBox.Click
        Dim mev As Windows.Forms.MouseEventArgs
        mev = e
        Dim im As RectangleF
        im = PictureBox.Image.GetBounds(System.Drawing.GraphicsUnit.Pixel)
        clickedAt = New Point
        clickedAt.X = mev.X - (Width - im.Width) / 2 - im.Width / 2
        clickedAt.Y = mev.Y - (Height - im.Height) / 2 - im.Height / 2
        ' shift center to the clicked pos
        center.x = center.x + clickedAt.X * sf * capxml2wms.metersToBBoxHorizontal(capxml2wms._srs, center)
        center.y = center.y - clickedAt.Y * sf * capxml2wms.metersToBBoxVertical(capxml2wms._srs)
        _MapLoad()
    End Sub

    Private Sub ButtonUseAsMaxScale_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles ButtonUseAsMaxScale.Click
        capxml2wms.maxScale = sf * Math.Sqrt(2)
        scaleUpperLimit = sf
        scaleLowerLimit = capxml2wms.minScale / Math.Sqrt(2)
        _MapLoad()
        ButtonUseAsMaxScale.Enabled = False
        ButtonUseAsMinScale.Enabled = True
        AddToRecommendedScale()
    End Sub

    Private Sub ButtonAddToRecommendedScale_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles ButtonAddToRecommendedScale.Click
        AddToRecommendedScale()
    End Sub

    Private Sub AddToRecommendedScale()
        If capxml2wms.recScale Is Nothing Then
            capxml2wms.recScale = New ArrayList
        End If
        capxml2wms.recScale.Add(sf)
        scaleLowerLimit = capxml2wms.minScale / Math.Sqrt(2)
        scaleUpperLimit = capxml2wms.maxScale / Math.Sqrt(2)
        _MapLoad()
    End Sub

    Private Sub ButtonUseAsMinScale_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles ButtonUseAsMinScale.Click
        capxml2wms.minScale = sf * Math.Sqrt(2)
        scaleLowerLimit = sf
        scaleUpperLimit = capxml2wms.maxScale / Math.Sqrt(2)
        _MapLoad()
        ButtonUseAsMinScale.Enabled = False
        ButtonAddToRecommendedScale.Enabled = True
        AddToRecommendedScale()
    End Sub

    Private Sub Button1_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles ButtonHelp.Click
        MsgBox("Diese Funktion dient dazu, eine Aufl�sung zu finden, die bestimmten Kriterien entspricht. Welche Kriterien dies sind " & _
        "legen sie selbst fest, in dem Sie die angezeigt Karte beurteilen und dem Progamm durch klich auf 'scale up' oder " & _
        "'scale down' mitteilen, ob die gesuchte Karte weniger Meter pro Pixel (scale down) oder mehr Meter pro " & _
        "Pixel hat als die angezeigt Karte. " & _
        Chr(13) & _
        "Nach der Aufl�sung wird in der Spanne zwischen 0 und 1000m/Pixel nach der gew�nschten Aufl�sung gesucht. " & _
        "Dazu wird die Spanne jeweils in 2 gleich gro�e Teile geteilt und Sie beurteilen anhand der gezeigten Karte" & _
        ", ob die gesuchte Aufl�sung " & _
        "in der unteren oder der oberen H�lfte zu finden ist. Damit Sie diese Aussage machen k�nnen, wird Ihnen die " & _
        "Karte in der Aufl�ung genau in der Mitte der jeweiligen Spanne gezeigt (zu Beginn also mit 500m/Pixel) " & _
        "und Sie teilen den Computer mit, ob die gesuchte Karte eine gr��ere oder eine kleinere Aufl�sung hat. " & _
        "Danach wird die verbliebene Spanne wieder in der Mitte geteilt und die entsprechende Karte pr�sentiert. " & _
        " Auf diese Weise kann die gew�nschte Aufl�sung mit nur wenigen Klicks sehr genau bestimmt werden. " & Chr(13) & _
        "Begonnen wird mit der Spanne 0 bis 1000m/Pixel. Die erste Abfrage lautet daher, ob die gesuchte Karte " & _
        "eine Aufl�sung gr��er oder kleiner 500m/Pixel hat. Wenn sie beispielseweise kleiner ist, dann dr�cken Sie " & _
        "auf 'scale down'. Damit liegt die gesuchte Aufl�sung zwischen 0 und 500m/Pixel. Das Programm wird Ihnen " & _
        "daher eine Karte mit 250m/Pixel zeigen und fragen, ob die gesuchte Aufl�sung kleiner oder gr��er ist." & _
        " Wenn sie gr��er ist, dann ist liegt die gesuchte Aufl�sung zwischen 250 und 500 m/Pixel. Der Computer " & _
        "wird Ihnen daher eine Karte mit (250+500)/2 = 375m/Pixel pr�sentieren." & _
        "Konkret ist diese Methode dazu gedacht, a) die Maximale b) die minimale und c) evtl. mehrere empfohlene " & _
        "Aufl�sungen zu ermitteln. Es wird empfohlen, zuerst die maximal, dann die minimale und schlie�lich ggf. " & _
        "mehrere Aufl�sungen zu empfehlen. " & Chr(13) & _
        "Nehmen wir dann, dass bei 1000m/Pixel die maximale Aufl�sung �berschritten ist. Vermutlich erhalten Sie " & _
        "dann eine wei�e, also leere Karte. Dr�cken Sie nun so oft auf 'scale down' bis eine Karte angezeigt wird. " & _
        " Wenn eine Karte angezeigt wird, dr�cken sie so oft auf 'scale up' bis wieder eine leere Karte erscheint. " & _
        "Nun dr�cken Sie wieder auf 'scale down' bis wieder eine Karte erscheint. Beenden Sie die Suche nach der " & _
        " maximalen Aufl�sung, wenn sich die Aufl�sung um weniger als 0.5 m/Pixel �ndert. Dr�cken Sie zum Abschluss " & _
        "auf 'Use as MaxScale'. Dadurch wird die aktuelle Aufl�sung als MaxScale in die .wms �bernommen." & _
        "Au�erdem wird eine neue Karte geladen und als Startspanne zur Suche der Minscale 0 bis MaxScale verwendet." & _
        Chr(13) & _
        "MinScale, MaxScale und RecommendeScales werden zur�ckgesetzt, wenn man im Hauptfenster auf 'M�glichkeiten abfragen' " & _
        " dr�ckt" & _
        Chr(13) & _
        "Damit Sie die Karte besser beurteilen k�nnen, k�nnen Sie sie verschieben. Klicken Sie dazu an die Stelle " & _
        "in der Karte, die in die Mitte des Bildes verschoben werden soll. Ein Klick in die Karte bewirkt, dass " & _
        "die Karte so verschoben wird, dass die Stelle, an die man geklickt hat, " & _
        "in der Mitte des Bildes erscheint.")
    End Sub
End Class
