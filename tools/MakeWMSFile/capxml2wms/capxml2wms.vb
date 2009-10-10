Imports System.Xml
Imports System.Net
Public Class capxml2wms

    Dim DPunkt As New Globalization.CultureInfo("") 'en-US 

    Dim SRSCWAll() As String = {"31466", "31467", "31468", "31469", "3003", "3004", "4326"}

    Dim xmlFileName As String

    Dim GotCapabilities As Boolean = False

    Dim wmsUrl As String = "" ' Input
    Dim capsUrl As String = "" ' direkt aus Input herstellen
    Dim mapUrl As String = "" ' Output

    Dim Version As String = "" '1.
    Dim OnlineResource As String = "" '2.nicht wichtig
    Dim land As String = "" '3.
    Dim area As String = "" '4.
    Dim SRSCW As String = "" '5. so wie es CW in der wms haben will
    Dim SRS As String = "" '6. Auflistung der EPSG-Codes des wms-Servers
    Dim minx As String = "" '7. Die BoundingBox Werte 
    Dim miny As String = "" '8.
    Dim maxx As String = "" '9.
    Dim maxy As String = "" '10.

    Dim mittex As String = ""
    Dim mittey As String = ""

    Dim rminx As Double
    Dim rminy As Double
    Dim rmaxx As Double
    Dim rmaxy As Double

    Dim rxmitte As Double
    Dim rymitte As Double

    '11. ComboBoxFormat Bild Speicherformat (png,jpg ...)
    '12. CheckedListBoxLayers Layers mit Name, Title und Scalehint 
    '13. ComboBoxBBox LatlonBoundingBox

    Dim WGS84 As Ellipsoid
    Dim OBBoxes As Dictionary(Of String, XmlElement)
    Friend minScale As Double = 0
    Friend maxScale As Double = 1000
    Friend recScale As ArrayList


    Private Sub capxml2wms_FormClosing(ByVal sender As Object, ByVal e As System.Windows.Forms.FormClosingEventArgs) Handles Me.FormClosing
        Try
            IO.File.Delete(xmlFileName)
        Catch ex As Exception
        End Try
    End Sub

    Private Sub capxml2wms_Load(ByVal sender As Object, ByVal e As System.EventArgs) Handles Me.Load
        WGS84.a = 6378137
        WGS84.b = 298.257223563

        xmlFileName = IO.Path.GetTempFileName
        IO.File.Delete(xmlFileName)

        xmlFileName = IO.Path.ChangeExtension(xmlFileName, ".xml")

        Me.TextBoxUrl.Text = "http://"

        Me.ComboBoxVersion.SelectedIndex = 2

    End Sub

    Private Sub GetLayers(ByVal n As XmlNode)
        Dim Layername As String = ""
        If n.Name = "Layer" Then
            For Each sn As XmlNode In n.ChildNodes
                If sn.Name = "Name" Then
                    Layername = sn.ChildNodes.ItemOf(0).Value
                End If
                If sn.Name = "Title" Then
                End If
                If sn.Name = "ScaleHint" Then
                End If
            Next

        End If
        CheckedListBoxLayers.Items.Add(Layername, False)
    End Sub

    Private Sub ButtonGetCapabilities_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles ButtonGetCapabilities.Click
        GotCapabilities = GetCapabilities()
        maxScale = 1000
        minScale = 0
        If Not recScale Is Nothing Then
            recScale.Clear()
        End If
    End Sub

    Private Sub ButtonShowMap_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles ButtonShowMap.Click
        Dim MeineSkalierung As Double
        MeineSkalierung = 2.0 * Me.HScrollBar1.Value
        mapUrl = makeGetMapUrl(MeineSkalierung)
        Dim center As point
        center.x = 0
        center.y = 0
        Dim SM As New Map(mapUrl, 0, 0, 0, center)
        'MsgBox("Noch nicht vorhanden!", MsgBoxStyle.Information, "Hinweis!")
        SM.ButtonAddToRecommendedScale.Visible = False
        SM.ButtonHelp.Visible = False
        SM.ButtonScaleDown.Visible = False
        SM.ButtonScaleUp.Visible = False
        SM.ButtonUseAsMaxScale.Visible = False
        SM.ButtonUseAsMinScale.Visible = False
        SM.LabelScaleDiagonal.Visible = False
        SM.LabelScaleVertical.Visible = False
        SM.ShowDialog()
    End Sub
    Friend _srs As Integer
    Friend _pixelwidth As Integer
    Private Sub ButtonFindScale_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles ButtonFindScale.Click
        Dim BBoxString As String

        _srs = Me.ComboBoxEPSG.SelectedItem

        Dim center As point
        ' find center
        Dim bb As XmlElement
        If (Me.OBBoxes.ContainsKey("EPSG:" & Me.ComboBoxEPSG.SelectedItem)) Then
            bb = Me.OBBoxes.Item("EPSG:" & Me.ComboBoxEPSG.SelectedItem)
            minx = bb.GetAttribute("minx")
            rminx = Double.Parse(minx, DPunkt.NumberFormat)
            miny = bb.GetAttribute("miny")
            rminy = Double.Parse(miny, DPunkt.NumberFormat)
            maxx = bb.GetAttribute("maxx")
            rmaxx = Double.Parse(maxx, DPunkt.NumberFormat)
            maxy = bb.GetAttribute("maxy")
            rmaxy = Double.Parse(maxy, DPunkt.NumberFormat)
        Else
            MsgBox("In dem ausgewähltem Raumbezugssystem (EPSG-code) ist die BoundingBox leider nicht angegeben." & _
                   "Daher kann kein Zentrum automatisch bestimmt werden.")
            Return
        End If

        rxmitte = (rminx + rmaxx) / 2
        rymitte = (rminy + rmaxy) / 2
        center.x = rxmitte
        center.y = rymitte

        Dim scale_ll, scale_ul, scale_m As Double
        _pixelwidth = 500

        scale_ll = minScale
        scale_ul = maxScale

        Dim SM As Map
        Dim mapUrl As String
        Do
            scale_m = (scale_ll + scale_ul) / 2
            BBoxString = ToBBox(center, _srs, _pixelwidth * scale_m)
            mapUrl = makeGetMapUrlFromBBox(BBoxString)
            SM = New Map(mapUrl, scale_m, minScale, maxScale, center)
            SM.ShowDialog()
        Loop Until SM.exitcode = 0
    End Sub
    Friend Function makeGetMapUrlFromBBox(ByVal bb As String) As String
        'http:// www.lv-bw.de/dv/service/getrds2.asp?login=dv&pw=anonymous
        '&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap
        '&SRS=EPSG:31467
        '&BBOX=3500394.51 , 5371405.09 , 3520394.51 , 5391405.09 
        '&WIDTH=1000
        '&HEIGHT=1000
        '&LAYERS=DVTK50K
        '&STYLES=
        '&FORMAT=image/png


        mapUrl = wmsUrl & "SERVICE=WMS&VERSION=" & Version & "&REQUEST=GetMap"

        mapUrl += "&SRS=EPSG:" & Me.ComboBoxEPSG.SelectedItem

        Dim BBox As String = "&BBox=" & bb

        mapUrl += BBox
        mapUrl += "&WIDTH=500"
        mapUrl += "&HEIGHT=500"
        ' und die gewählten Layer durch Komma getrennt
        Dim SelectedLayers As String = ""
        For Each s As String In CheckedListBoxLayers.CheckedItems
            SelectedLayers += "," & s.Split("|")(0)
        Next
        If SelectedLayers.Length = 0 Then
            MsgBox("Mindestens 1 Layer muss ausgewählt werden")
            Return ""
        End If

        If SelectedLayers <> "" Then
            mapUrl += "&LAYERS=" & SelectedLayers.Substring(1).Replace(" ", "%20")
        End If
        mapUrl += "&STYLES="
        mapUrl += "&FORMAT=" & Me.ComboBoxFormat.SelectedItem.ToString
        makeGetMapUrlFromBBox = mapUrl
    End Function
    Private Function makeGetMapUrl(ByVal scale As Double) As String
        'http:// www.lv-bw.de/dv/service/getrds2.asp?login=dv&pw=anonymous
        '&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap
        '&SRS=EPSG:31467
        '&BBOX=3500394.51 , 5371405.09 , 3520394.51 , 5391405.09 
        '&WIDTH=1000
        '&HEIGHT=1000
        '&LAYERS=DVTK50K
        '&STYLES=
        '&FORMAT=image/png


        mapUrl = wmsUrl & "SERVICE=WMS&VERSION=" & Version & "&REQUEST=GetMap"

        mapUrl += "&SRS=EPSG:" & Me.ComboBoxEPSG.SelectedItem

        Dim rxmin As Double
        Dim rxmax As Double
        Dim rymin As Double
        Dim rymax As Double

        If Me.ComboBoxEPSG.SelectedItem = "4326" Then
            Me.ComboBoxBBoxI.SelectedIndex = Me.ComboBoxBBox.SelectedIndex
            GetBBoxValues(Me.ComboBoxBBoxI.SelectedItem)
        Else
            Try
                GetBBoxValues(Me.OBBoxes.Item("EPSG:" & Me.ComboBoxEPSG.SelectedItem))
            Catch ex As Exception
                'todo: und jetzt umrechnen !
                'das spar ich mir wohl, oder !
                Me.ComboBoxBBoxI.SelectedIndex = Me.ComboBoxBBox.SelectedIndex
                GetBBoxValues(Me.ComboBoxBBoxI.SelectedItem)
                ' jetzt haben wir die BBox zum WGS84
                MsgBox("Kartenansicht für diese Auswahl nicht implementiert", MsgBoxStyle.Exclamation)
                Exit Function
            End Try
        End If

        Dim HalbeKantenlaengex As Double = (rmaxx - rminx) / scale
        Dim HalbeKantenlaengey As Double = (rmaxy - rminy) / scale
        Dim HalbeKantenlaenge As Double = HalbeKantenlaengex
        If HalbeKantenlaengex > HalbeKantenlaengey Then
            HalbeKantenlaenge = HalbeKantenlaengey
        End If
        ' is in x und y die gleiche Einheit?
        rxmin = rxmitte - HalbeKantenlaenge
        rxmax = rxmitte + HalbeKantenlaenge
        rymin = rymitte - HalbeKantenlaenge
        rymax = rymitte + HalbeKantenlaenge

        Dim BBox As String = "&BBox=" & rxmin.ToString(DPunkt) & "," & rymin.ToString(DPunkt) & ","
        BBox += rxmax.ToString(DPunkt) & "," & rymax.ToString(DPunkt)

        mapUrl += BBox
        mapUrl += "&WIDTH=500"
        mapUrl += "&HEIGHT=500"
        ' und die gewählten Layer durch Komma getrennt
        Dim SelectedLayers As String = ""
        For Each s As String In CheckedListBoxLayers.CheckedItems
            SelectedLayers += "," & s.Split("|")(0)
        Next
        If SelectedLayers <> "" Then
            mapUrl += "&LAYERS=" & SelectedLayers.Substring(1).Replace(" ", "%20")
        End If
        mapUrl += "&STYLES="
        mapUrl += "&FORMAT=" & Me.ComboBoxFormat.SelectedItem.ToString
        makeGetMapUrl = mapUrl
    End Function

    Friend Function metersToBBoxVertical(ByVal epsg As Integer) As Double
        If epsg = 4326 Then
            metersToBBoxVertical = 360 / (WGS84.a * Math.PI * 2)
        Else : metersToBBoxVertical = 1
        End If
    End Function
    Friend Function metersToBBoxHorizontal(ByVal epsg As Integer, ByRef center As capxml2wms.point) As Double
        If epsg = 4326 Then
            metersToBBoxHorizontal = (360 / (WGS84.a * Math.PI * 2)) / Math.Cos(center.Y / 180 * Math.PI)
        Else : metersToBBoxHorizontal = 1
        End If
    End Function

    Friend Function ToBBox(ByRef center As point, ByVal srs As Integer, ByVal meter As Double) As String
        Dim tl As point
        Dim br As point
        Dim diff_v, diff_h As Double
        diff_h = metersToBBoxHorizontal(srs, center) * meter
        diff_v = metersToBBoxVertical(srs) * meter
        tl.x = center.x - diff_h / 2
        tl.y = center.y + diff_v / 2
        br.x = center.x + diff_h / 2
        br.y = center.y - diff_v / 2
        Dim BBox As String
        BBox = tl.x.ToString(DPunkt) + "," + br.y.ToString(DPunkt) + "," + br.x.ToString(DPunkt) + "," + tl.y.ToString(DPunkt)
        ToBBox = BBox
    End Function
    Private Sub ButtonErstellen_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles ButtonErstellen.Click

        Dim wmsFileName As String = IO.Path.ChangeExtension(xmlFileName, ".wms")
        IO.File.Delete(wmsFileName) 'vorsichtshalber
        Dim encoding As System.Text.Encoding = New System.Text.UTF8Encoding(False)
        Dim F As New System.IO.StreamWriter(wmsFileName, False, encoding)

        If Not GotCapabilities Then
            GotCapabilities = GetCapabilities()
        End If

        Me.ComboBoxBBoxI.SelectedIndex = Me.ComboBoxBBox.SelectedIndex
        GetBBoxValues(Me.ComboBoxBBoxI.SelectedItem)


        F.WriteLine("TakenFromUrl:       " & OnlineResource)
        F.WriteLine("GetCapabilitiesUrl: " & capsUrl)
        F.WriteLine("Name:               " & land & "." & area & " topo")
        F.WriteLine("MapType:                        topo")
        F.WriteLine("MainUrl:            " & wmsUrl)
        F.WriteLine("ServiceTypeUrlPart: SERVICE=WMS")
        F.WriteLine("VersionUrlPart:     VERSION=" & Version)
        F.WriteLine("CoordinateReferenceSystemCacheWolf: " & SRSCW)
        F.WriteLine("CoordinateReferenceSystemUrlPart: " & SRS)
        F.WriteLine("RequestUrlPart:     REQUEST=GetMap")
        ' und noch die möglichen Layer , Title, ScaleHint
        For Each s As String In CheckedListBoxLayers.Items
            F.WriteLine("#LayersUrlPart:     LAYERS=" & s)
        Next
        ' und die gewählten Layer durch Komma getrennt
        Dim SelectedLayers As String = ""
        For Each s As String In CheckedListBoxLayers.CheckedItems
            SelectedLayers += "," & s.Split("|")(0)
        Next
        If SelectedLayers <> "" Then
            F.WriteLine("LayersUrlPart:     LAYERS=" & SelectedLayers.Substring(1).Replace(" ", "%20"))
        Else
            F.WriteLine("LayersUrlPart:     LAYERS=")
        End If
        F.WriteLine("StylesUrlPart:     STYLES=")
        F.WriteLine("ImageFormatUrlPart:FORMAT=" & Me.ComboBoxFormat.SelectedItem.ToString)
        F.WriteLine("BoundingBoxTopLeftWGS84: " & maxy & " " & minx)
        F.WriteLine("BoundingBoxButtomRightWGS84: " & miny & " " & maxx)
        F.WriteLine("#BBox_Mitte: " & mittey & " " & mittex)
        F.WriteLine("MinScale:   " & (Math.Ceiling(minScale * 10000) / 10000).ToString(DPunkt))
        F.WriteLine("MaxScale:   " & (Math.Floor(maxScale * 10000) / 10000).ToString(DPunkt))
        Dim recsscales As String = ""
        If Not recScale Is Nothing Then
            For Each d As Double In recScale
                recsscales = recsscales & " " & (Math.Floor(d * 10000) / 10000).ToString(DPunkt)
            Next
            recsscales.Trim()
        Else
            recsscales = "5"
        End If
        F.WriteLine("RecommendedScale:   " & recsscales)
        'Me.ComboBoxFormat.SelectedItem.ToString.Split("/")(1)
        Dim rk As Microsoft.Win32.RegistryKey = My.Computer.Registry.ClassesRoot.OpenSubKey("Mime\Database\Content Type\" & Me.ComboBoxFormat.SelectedItem.ToString)
        Dim stmp As String = rk.GetValue("Extension")
        F.WriteLine("ImageFileExtension: " & stmp)

        F.Close()
        ' noch ne gpx mit waypoints der BBox

        Dim gpxFileName As String = IO.Path.ChangeExtension(xmlFileName, ".gpx")
        IO.File.Delete(gpxFileName) 'vorsichtshalber
        'Dim encoding As System.Text.Encoding = New System.Text.UTF8Encoding(False)
        Dim G As New System.IO.StreamWriter(gpxFileName, False, encoding)
        G.WriteLine("<?xml version=""1.0"" encoding=""ISO-8859-1"" standalone=""yes""?>")
        G.WriteLine("<gpx xmlns=""http://www.topografix.com/GPX/1/0"" ")
        G.WriteLine("xmlns:xsi=""http://www.w3.org/2001/XMLSchema-instance"" ")
        G.WriteLine("xsi:schemaLocation=""http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd"" ")
        G.WriteLine("version=""1.0"" creator=""ich"" > ")
        Dim dx As Double = (rmaxx - rminx) / 10.0
        Dim dy As Double = (rmaxy - rminy) / 10.0
        Dim rx As Double = rminx
        Dim ry As Double = rminy
        Dim k As Integer = 0
        For i As Integer = 1 To 9
            rx += dx
            ry = rminy
            For j As Integer = 1 To 9
                k += 1
                ry += dy
                G.Write("<wpt lat=""" & ry.ToString(DPunkt) & """ lon=""" & rx.ToString(DPunkt) & """> ")
                G.Write("<name>CW" & k.ToString("0000") & "</name> ")
                G.Write("<desc>" & i.ToString & ":" & j.ToString & "</desc> ")
                G.Write("<sym>Circle, Red</sym> ")
                G.WriteLine("</wpt>")
            Next
        Next
        k += 1
        G.Write("<wpt lat=""" & rminy.ToString(DPunkt) & """ lon=""" & rminx.ToString(DPunkt) & """> ")
        G.Write("<name>CW" & k.ToString("0000") & "</name> ")
        G.Write("<desc>" & "Unten Links" & "</desc> ")
        G.Write("<sym>Circle, Green</sym> ")
        G.WriteLine("</wpt>")
        k += 1
        G.Write("<wpt lat=""" & rmaxy.ToString(DPunkt) & """ lon=""" & rminx.ToString(DPunkt) & """> ")
        G.Write("<name>CW" & k.ToString("0000") & "</name> ")
        G.Write("<desc>" & "Oben Links" & "</desc> ")
        G.Write("<sym>Circle, Green</sym> ")
        G.WriteLine("</wpt>")
        k += 1
        G.Write("<wpt lat=""" & rmaxy.ToString(DPunkt) & """ lon=""" & rmaxx.ToString(DPunkt) & """> ")
        G.Write("<name>CW" & k.ToString("0000") & "</name> ")
        G.Write("<desc>" & "Oben Rechts" & "</desc> ")
        G.Write("<sym>Circle, Green</sym> ")
        G.WriteLine("</wpt>")
        k += 1
        G.Write("<wpt lat=""" & rminy.ToString(DPunkt) & """ lon=""" & rmaxx.ToString(DPunkt) & """> ")
        G.Write("<name>CW" & k.ToString("0000") & "</name> ")
        G.Write("<desc>" & "Unten Rechts" & "</desc> ")
        G.Write("<sym>Circle, Green</sym> ")
        G.WriteLine("</wpt>")
        G.WriteLine("</gpx>")
        G.Close()

        Process.Start("notepad.exe", wmsFileName)
        Process.Start("notepad.exe", gpxFileName)

        MsgBox("Bitte die Datei im Verzeichnis webmapservices des Cachewolf speichern", MsgBoxStyle.Information, "WMS-Datei")

        IO.File.Delete(wmsFileName)
        IO.File.Delete(gpxFileName)

    End Sub

    Private Sub TextBoxUrl_TextChanged(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles TextBoxUrl.TextChanged
        GotCapabilities = False
        Me.ButtonShowMap.Enabled = False
        wmsUrl = Me.TextBoxUrl.Text
        capsUrl = Me.TextBoxUrl.Text
    End Sub

    Private Sub ButtonCheckAll_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles ButtonCheckAll.Click
        For i As Integer = 0 To Me.CheckedListBoxLayers.Items.Count - 1
            If Not Me.CheckedListBoxLayers.GetItemCheckState(i) = CheckState.Checked Then
                Me.CheckedListBoxLayers.SetItemCheckState(i, CheckState.Checked)
            End If
        Next
    End Sub

    Private Sub ButtonCheckNone_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles ButtonCheckNone.Click
        For i As Integer = 0 To Me.CheckedListBoxLayers.Items.Count - 1
            If Not Me.CheckedListBoxLayers.GetItemCheckState(i) = CheckState.Unchecked Then
                Me.CheckedListBoxLayers.SetItemCheckState(i, CheckState.Unchecked)
            End If
        Next
    End Sub

    Private Function GetCapabilities() As Boolean

        If wmsUrl.IndexOf("?") = -1 Then
            wmsUrl += "?"
        Else
            If Not wmsUrl.EndsWith("?") Then
                If Not wmsUrl.EndsWith("&") Then
                    wmsUrl += "&"
                End If
            End If
        End If

        ' Version kann vermutlich entfallen, denn wer weiss das schon vorher
        capsUrl = wmsUrl & "SERVICE=WMS&VERSION=" & Me.ComboBoxVersion.SelectedItem & "&REQUEST=GetCapabilities"

        Dim wc As New WebClient()
        Try
            IO.File.Delete(xmlFileName)
        Catch ex As Exception
        End Try
        Try
            wc.DownloadFile(capsUrl, xmlFileName)
        Catch ex As Exception
            MsgBox(ex.Message, MsgBoxStyle.Critical)
            If IO.File.Exists(xmlFileName) Then
                IO.File.Delete(xmlFileName)
            End If
            Exit Function
        End Try

        Dim xd As New XmlDocument
        xd.PreserveWhitespace = False
        Dim xtr As New XmlTextReader(xmlFileName)
        Try
            xd.Load(xtr)
        Catch ex As Exception
            Process.Start("notepad.exe", xmlFileName)
            MsgBox("Oh, ein nicht Standard wms-Server! Da geb ich auf!!!", MsgBoxStyle.Exclamation)
            Try
                IO.File.Delete(xmlFileName)
            Catch e As Exception
            End Try
            Exit Function
        End Try
        xtr.Close()
        IO.File.Delete(xmlFileName)

        Me.TextBoxXML.Clear()
        Dim ms As New IO.MemoryStream
        xd.Save(ms)
        Dim encoding As System.Text.Encoding = New System.Text.UTF8Encoding '.ASCIIEncoding
        Me.TextBoxXML.Text = System.Text.Encoding.UTF8.GetString(ms.GetBuffer())
        ms.Close()

        Me.ButtonShowMap.Enabled = True

        '1. Version aus WMT_MS_Capabilities
        For Each nl As XmlNode In xd.GetElementsByTagName("WMT_MS_Capabilities")
            If nl.NodeType = XmlNodeType.Element Then
                Dim ne As XmlElement = nl
                Version = ne.GetAttribute("version")
                Exit For
            End If
        Next

        Dim Caps As XmlNodeList = Nothing
        Dim Service As XmlNodeList = Nothing
        For Each N As XmlNode In xd.DocumentElement.ChildNodes
            If N.Name = "Capability" Then
                Caps = N.ChildNodes
            End If
            If N.Name = "Service" Then
                Service = N.ChildNodes
            End If
        Next

        '2. OnlineResource  aus Service
        For Each nl As XmlNode In Service
            If nl.NodeType = XmlNodeType.Element Then
                If nl.Name = "OnlineResource" Then
                    Dim ne As XmlElement = nl
                    OnlineResource = ne.GetAttribute("xlink:href")
                    Exit For
                End If
            End If
        Next

        Dim Request As XmlElement = Nothing
        Dim Exception As XmlElement = Nothing
        Dim VendorSpecificCapabilities As XmlElement = Nothing
        Dim UserDefinedSymbolization As XmlElement = Nothing
        Dim Layer As XmlElement = Nothing
        For Each N As XmlNode In Caps
            If N.NodeType = XmlNodeType.Element Then
                If N.Name = "Request" Then
                    Request = N
                End If
                If N.Name = "Exception" Then
                    Exception = N
                End If
                If N.Name = "VendorSpecificCapabilities" Then
                    VendorSpecificCapabilities = N
                End If
                If N.Name = "UserDefinedSymbolization" Then
                    UserDefinedSymbolization = N
                End If
                If N.Name = "Layer" Then
                    Layer = N
                End If
            End If
        Next
        If Layer Is Nothing Then
            Exit Function
        End If

        Dim ltmp() As String
        ltmp = wmsUrl.Split("/")(2).Split(".")
        '3.
        land = ltmp(ltmp.Length - 1)
        '4.
        area = ltmp(ltmp.Length - 2)

        '5. EPSG und 6. EPSG
        ' SRS = Spatial referencing systems  
        ' EPSG = European Petroleum Survey Group
        ComboBoxEPSG.Items.Clear()
        SRS = ""
        SRSCW = ""
        Dim SRSNodes As XmlNodeList = Layer.GetElementsByTagName("SRS")
        Dim SRSD As New Dictionary(Of String, String)
        For Each N As XmlNode In SRSNodes
            Dim SRStmp As String = N.ChildNodes.ItemOf(0).Value
            For Each s As String In SRSCWAll
                If SRStmp.IndexOf(s) > 0 Then
                    If Not SRSD.ContainsKey("SRS=EPSG:" & s) Then
                        SRSD.Add("SRS=EPSG:" & s, " ")
                        SRS = SRS & "SRS=EPSG:" & s & " "
                        SRSCW = SRSCW & " " & s
                        ComboBoxEPSG.Items.Add(s)
                    End If
                End If
            Next
        Next
        Me.ComboBoxEPSG.SelectedIndex = -1
        For i As Integer = 0 To Me.ComboBoxEPSG.Items.Count - 1
            If Me.ComboBoxEPSG.Items.Item(i) = "4326" Then
                Me.ComboBoxEPSG.SelectedIndex = i
                Exit For
            Else
                'falls überhaupt was drin steht
                Me.ComboBoxEPSG.SelectedIndex = 0
            End If
        Next

        '11.
        Me.ComboBoxFormat.Items.Clear()
        For Each n As XmlNode In Request.GetElementsByTagName("GetMap")
            ' gibt eigentlich nur einen
            For Each fn As XmlNode In n.ChildNodes
                If fn.NodeType = XmlNodeType.Element Then
                    If fn.Name = "Format" Then
                        Me.ComboBoxFormat.Items.Add(fn.ChildNodes.ItemOf(0).Value)
                    End If
                End If
            Next
        Next
        Me.ComboBoxFormat.SelectedIndex = 0
        For i As Integer = 0 To Me.ComboBoxFormat.Items.Count - 1
            If Me.ComboBoxFormat.Items.Item(i).ToString.EndsWith("png") Then
                Me.ComboBoxFormat.SelectedIndex = i
                Exit For
            End If
        Next

        '12. CheckedListBoxLayers
        CheckedListBoxLayers.Items.Clear()
        ' nur die Nodes mit "Name" , zu "Layer" gehören
        Dim NodesToDo As New Dictionary(Of XmlNode, String)
        For Each n As XmlNode In Layer.GetElementsByTagName("Name")
            If n.ParentNode.Name = "Layer" Then
                If Not NodesToDo.ContainsKey(n.ParentNode) Then
                    NodesToDo.Add(n.ParentNode, "")
                    Dim tmp_name As String = ""
                    Dim tmp_titel As String = ""
                    Dim tmp_skalierung_min As String = ""
                    Dim tmp_skalierung_max As String = ""
                    If n.ParentNode.Name = "Layer" Then
                        For Each sn As XmlNode In n.ParentNode.ChildNodes
                            If sn.Name = "Name" Then
                                tmp_name = sn.ChildNodes.ItemOf(0).Value
                            End If
                            If sn.Name = "Title" Then
                                tmp_titel = sn.ChildNodes.ItemOf(0).Value
                            End If
                            If sn.Name = "ScaleHint" Then
                                tmp_skalierung_min = sn.Attributes.ItemOf(0).Value
                                tmp_skalierung_max = sn.Attributes.ItemOf(1).Value
                            End If
                        Next
                    End If
                    CheckedListBoxLayers.Items.Add(tmp_name & "|" & tmp_titel & "|" & tmp_skalierung_min & "|" & tmp_skalierung_max, False)
                End If
            End If
        Next

        '13. ComboBoxBBox LatlonBoundingBox
        Me.ComboBoxBBox.Items.Clear()
        Me.ComboBoxBBoxI.Items.Clear()
        Dim BBoxes As New Dictionary(Of String, String)
        For Each n As XmlNode In Layer.GetElementsByTagName("LatLonBoundingBox")
            If n.NodeType = XmlNodeType.Element Then
                GetBBoxValues(n)
                Dim stmp As String = "ol=" & maxy & " " & minx & " , ur=" & miny & " " & maxx
                If Not BBoxes.ContainsKey(stmp) Then
                    Me.ComboBoxBBox.Items.Add(stmp)
                    Me.ComboBoxBBoxI.Items.Add(n)
                    BBoxes.Add(stmp, "")
                End If
            End If
        Next
        Me.ComboBoxBBox.SelectedIndex = 0
        'Me.ComboBoxBBoxI.SelectedIndex = 0

        '.z.B.
        '<BoundingBox SRS="EPSG:31467" minx="3340000" miny="5230000" maxx="3615000" maxy="5550000" />
        OBBoxes = New Dictionary(Of String, XmlElement)
        For Each n As XmlNode In Layer.GetElementsByTagName("BoundingBox")
            If n.NodeType = XmlNodeType.Element Then
                Dim e As XmlElement = n
                Dim stmp As String = e.GetAttribute("SRS")
                If Not OBBoxes.ContainsKey(stmp) Then
                    OBBoxes.Add(stmp, e)
                End If
            End If
        Next
        If Not OBBoxes.ContainsKey("EPSG:4326") Then
            For Each n As XmlNode In Layer.GetElementsByTagName("LatLonBoundingBox")
                If n.NodeType = XmlNodeType.Element Then
                    Dim e As XmlElement = n
                    Dim stmp As String = e.GetAttribute("SRS")
                    If Not stmp = "" And Not OBBoxes.ContainsKey(stmp) Then
                        OBBoxes.Add("EPSG:4326", e)
                    End If
                End If
            Next
        End If

        ButtonCheckAll_Click(Nothing, Nothing)
        Return True

    End Function

    Private Sub GetBBoxValues(ByVal e As XmlElement)
        'in die folgenden globalen Variablen
        'Dim minx As String = "" '7. Die BoundingBox Werte 
        'Dim miny As String = "" '8.
        'Dim maxx As String = "" '9.
        'Dim maxy As String = "" '10.

        'Dim rminx As Double
        'Dim rminy As Double
        'Dim rmaxx As Double
        'Dim rmaxy As Double
        minx = e.GetAttribute("minx")
        rminx = Double.Parse(minx, DPunkt.NumberFormat)
        minx = Format(rminx, "0.0000").Replace(",", ".")
        If minx.StartsWith("-") Then
            minx = minx.Substring(1)
            minx = "W " & minx
        Else
            minx = "E " & minx
        End If

        miny = e.GetAttribute("miny")
        rminy = Double.Parse(miny, DPunkt.NumberFormat)
        miny = Format(rminy, "0.0000").Replace(",", ".")
        If miny.StartsWith("-") Then
            miny = miny.Substring(1)
            miny = "S " & miny
        Else
            miny = "N " & miny
        End If

        maxx = e.GetAttribute("maxx")
        rmaxx = Double.Parse(maxx, DPunkt.NumberFormat)
        maxx = Format(rmaxx, "0.0000").Replace(",", ".")
        If maxx.StartsWith("-") Then
            maxx = maxx.Substring(1)
            maxx = "W " & maxx
        Else
            maxx = "E " & maxx
        End If

        maxy = e.GetAttribute("maxy")
        rmaxy = Double.Parse(maxy, DPunkt.NumberFormat)
        maxy = Format(rmaxy, "0.0000").Replace(",", ".")
        If maxy.StartsWith("-") Then
            maxy = maxy.Substring(1)
            maxy = "S " & maxy
        Else
            maxy = "N " & maxy
        End If

        rxmitte = (rminx + rmaxx) / 2
        If rxmitte < 0 Then
            mittex = "W " & Math.Abs(rxmitte).ToString(DPunkt.NumberFormat)
        Else
            mittex = "E " & rxmitte.ToString(DPunkt.NumberFormat)
        End If
        rymitte = (rminy + rmaxy) / 2
        If rymitte < 0 Then
            mittey = "S " & Math.Abs(rymitte).ToString(DPunkt.NumberFormat)
        Else
            mittey = "N " & rymitte.ToString(DPunkt.NumberFormat)
        End If

    End Sub

    Friend Structure point
        Dim x As Double
        Dim y As Double
    End Structure
    Private Structure xyzKoordinate
        Dim x, y, z As Double
    End Structure
    Private Structure Ellipsoid
        Dim a As Double ' Semi-Major Axis
        Dim b As Double ' Inverse Flattening
    End Structure

    Private Function fromEPSG4326(ByVal pt As point, ByVal EPSGxxxx As String) As point
        ' "31466", "31467", "31468", "31469", "3003", "3004", "4326"}
        Dim WGS84 As Ellipsoid
        WGS84.a = 6378137
        WGS84.b = 298.257223563

        Select Case EPSGxxxx
            Case "31466"
            Case "31467"
            Case "31468"
            Case "31469"
            Case "3003"
            Case "3004"
            Case Else
        End Select
    End Function

    Private Function EPSG4326toxyz(ByVal pt As point) As xyzKoordinate


    End Function

End Class
