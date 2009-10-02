<Global.Microsoft.VisualBasic.CompilerServices.DesignerGenerated()> _
Partial Class capxml2wms
    Inherits System.Windows.Forms.Form

    'Das Formular überschreibt den Löschvorgang, um die Komponentenliste zu bereinigen.
    <System.Diagnostics.DebuggerNonUserCode()> _
    Protected Overrides Sub Dispose(ByVal disposing As Boolean)
        Try
            If disposing AndAlso components IsNot Nothing Then
                components.Dispose()
            End If
        Finally
            MyBase.Dispose(disposing)
        End Try
    End Sub

    'Wird vom Windows Form-Designer benötigt.
    Private components As System.ComponentModel.IContainer

    'Hinweis: Die folgende Prozedur ist für den Windows Form-Designer erforderlich.
    'Das Bearbeiten ist mit dem Windows Form-Designer möglich.  
    'Das Bearbeiten mit dem Code-Editor ist nicht möglich.
    <System.Diagnostics.DebuggerStepThrough()> _
    Private Sub InitializeComponent()
        Dim resources As System.ComponentModel.ComponentResourceManager = New System.ComponentModel.ComponentResourceManager(GetType(capxml2wms))
        Me.ButtonErstellen = New System.Windows.Forms.Button
        Me.TextBoxUrl = New System.Windows.Forms.TextBox
        Me.Label1 = New System.Windows.Forms.Label
        Me.TextBoxXML = New System.Windows.Forms.TextBox
        Me.Label2 = New System.Windows.Forms.Label
        Me.ComboBoxVersion = New System.Windows.Forms.ComboBox
        Me.ButtonGetCapabilities = New System.Windows.Forms.Button
        Me.ButtonShowMap = New System.Windows.Forms.Button
        Me.CheckedListBoxLayers = New System.Windows.Forms.CheckedListBox
        Me.Label3 = New System.Windows.Forms.Label
        Me.ButtonCheckNone = New System.Windows.Forms.Button
        Me.ButtonCheckAll = New System.Windows.Forms.Button
        Me.ComboBoxEPSG = New System.Windows.Forms.ComboBox
        Me.Label4 = New System.Windows.Forms.Label
        Me.HScrollBar1 = New System.Windows.Forms.HScrollBar
        Me.Label5 = New System.Windows.Forms.Label
        Me.ComboBoxFormat = New System.Windows.Forms.ComboBox
        Me.ComboBoxBBox = New System.Windows.Forms.ComboBox
        Me.Label6 = New System.Windows.Forms.Label
        Me.ComboBoxBBoxI = New System.Windows.Forms.ComboBox
        Me.ButtonFindScale = New System.Windows.Forms.Button
        Me.SuspendLayout()
        '
        'ButtonErstellen
        '
        Me.ButtonErstellen.Anchor = CType((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.ButtonErstellen.Location = New System.Drawing.Point(540, 552)
        Me.ButtonErstellen.Name = "ButtonErstellen"
        Me.ButtonErstellen.Size = New System.Drawing.Size(123, 36)
        Me.ButtonErstellen.TabIndex = 1
        Me.ButtonErstellen.Text = "WMS-Datei erstellen"
        Me.ButtonErstellen.UseVisualStyleBackColor = True
        '
        'TextBoxUrl
        '
        Me.TextBoxUrl.Anchor = CType(((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Left) _
                    Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.TextBoxUrl.Location = New System.Drawing.Point(20, 57)
        Me.TextBoxUrl.Name = "TextBoxUrl"
        Me.TextBoxUrl.Size = New System.Drawing.Size(496, 20)
        Me.TextBoxUrl.TabIndex = 0
        '
        'Label1
        '
        Me.Label1.Anchor = CType(((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Left) _
                    Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.Label1.AutoSize = True
        Me.Label1.Location = New System.Drawing.Point(21, 27)
        Me.Label1.Name = "Label1"
        Me.Label1.Size = New System.Drawing.Size(205, 13)
        Me.Label1.TabIndex = 2
        Me.Label1.Text = "http Adresse des WMS-Servers eingeben:"
        '
        'TextBoxXML
        '
        Me.TextBoxXML.Anchor = CType((((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Bottom) _
                    Or System.Windows.Forms.AnchorStyles.Left) _
                    Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.TextBoxXML.Location = New System.Drawing.Point(20, 83)
        Me.TextBoxXML.Multiline = True
        Me.TextBoxXML.Name = "TextBoxXML"
        Me.TextBoxXML.ReadOnly = True
        Me.TextBoxXML.ScrollBars = System.Windows.Forms.ScrollBars.Both
        Me.TextBoxXML.Size = New System.Drawing.Size(643, 288)
        Me.TextBoxXML.TabIndex = 4
        Me.TextBoxXML.WordWrap = False
        '
        'Label2
        '
        Me.Label2.Anchor = CType((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.Label2.AutoSize = True
        Me.Label2.Location = New System.Drawing.Point(511, 27)
        Me.Label2.Name = "Label2"
        Me.Label2.Size = New System.Drawing.Size(75, 13)
        Me.Label2.TabIndex = 5
        Me.Label2.Text = "WMS-Version:"
        Me.Label2.Visible = False
        '
        'ComboBoxVersion
        '
        Me.ComboBoxVersion.Anchor = CType((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.ComboBoxVersion.FormattingEnabled = True
        Me.ComboBoxVersion.Items.AddRange(New Object() {"1.0.0", "1.1.0", "1.1.1"})
        Me.ComboBoxVersion.Location = New System.Drawing.Point(588, 24)
        Me.ComboBoxVersion.MaxDropDownItems = 3
        Me.ComboBoxVersion.Name = "ComboBoxVersion"
        Me.ComboBoxVersion.Size = New System.Drawing.Size(75, 21)
        Me.ComboBoxVersion.TabIndex = 6
        '
        'ButtonGetCapabilities
        '
        Me.ButtonGetCapabilities.Anchor = CType((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.ButtonGetCapabilities.Location = New System.Drawing.Point(540, 43)
        Me.ButtonGetCapabilities.Name = "ButtonGetCapabilities"
        Me.ButtonGetCapabilities.Size = New System.Drawing.Size(123, 36)
        Me.ButtonGetCapabilities.TabIndex = 7
        Me.ButtonGetCapabilities.Text = "Möglichkeiten abfragen"
        Me.ButtonGetCapabilities.UseVisualStyleBackColor = True
        '
        'ButtonShowMap
        '
        Me.ButtonShowMap.Anchor = CType((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.ButtonShowMap.Location = New System.Drawing.Point(540, 397)
        Me.ButtonShowMap.Name = "ButtonShowMap"
        Me.ButtonShowMap.Size = New System.Drawing.Size(123, 36)
        Me.ButtonShowMap.TabIndex = 8
        Me.ButtonShowMap.Text = "Test Karte anzeigen"
        Me.ButtonShowMap.UseVisualStyleBackColor = True
        '
        'CheckedListBoxLayers
        '
        Me.CheckedListBoxLayers.Anchor = CType(((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Left) _
                    Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.CheckedListBoxLayers.CheckOnClick = True
        Me.CheckedListBoxLayers.FormattingEnabled = True
        Me.CheckedListBoxLayers.HorizontalScrollbar = True
        Me.CheckedListBoxLayers.Location = New System.Drawing.Point(20, 397)
        Me.CheckedListBoxLayers.Name = "CheckedListBoxLayers"
        Me.CheckedListBoxLayers.Size = New System.Drawing.Size(496, 109)
        Me.CheckedListBoxLayers.TabIndex = 9
        '
        'Label3
        '
        Me.Label3.Anchor = CType((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Left), System.Windows.Forms.AnchorStyles)
        Me.Label3.AutoSize = True
        Me.Label3.Location = New System.Drawing.Point(21, 381)
        Me.Label3.Name = "Label3"
        Me.Label3.Size = New System.Drawing.Size(36, 13)
        Me.Label3.TabIndex = 10
        Me.Label3.Text = "Layer:"
        '
        'ButtonCheckNone
        '
        Me.ButtonCheckNone.Anchor = CType((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Left), System.Windows.Forms.AnchorStyles)
        Me.ButtonCheckNone.Location = New System.Drawing.Point(212, 377)
        Me.ButtonCheckNone.Name = "ButtonCheckNone"
        Me.ButtonCheckNone.Size = New System.Drawing.Size(60, 20)
        Me.ButtonCheckNone.TabIndex = 13
        Me.ButtonCheckNone.Text = "kein x"
        Me.ButtonCheckNone.UseVisualStyleBackColor = True
        '
        'ButtonCheckAll
        '
        Me.ButtonCheckAll.Anchor = CType((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Left), System.Windows.Forms.AnchorStyles)
        Me.ButtonCheckAll.Location = New System.Drawing.Point(149, 377)
        Me.ButtonCheckAll.Name = "ButtonCheckAll"
        Me.ButtonCheckAll.Size = New System.Drawing.Size(60, 20)
        Me.ButtonCheckAll.TabIndex = 14
        Me.ButtonCheckAll.Text = "alle  x"
        Me.ButtonCheckAll.UseVisualStyleBackColor = True
        '
        'ComboBoxEPSG
        '
        Me.ComboBoxEPSG.Anchor = CType((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Left), System.Windows.Forms.AnchorStyles)
        Me.ComboBoxEPSG.FormattingEnabled = True
        Me.ComboBoxEPSG.Location = New System.Drawing.Point(20, 526)
        Me.ComboBoxEPSG.Name = "ComboBoxEPSG"
        Me.ComboBoxEPSG.Size = New System.Drawing.Size(121, 21)
        Me.ComboBoxEPSG.TabIndex = 15
        '
        'Label4
        '
        Me.Label4.Anchor = CType((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Left), System.Windows.Forms.AnchorStyles)
        Me.Label4.AutoSize = True
        Me.Label4.Location = New System.Drawing.Point(22, 509)
        Me.Label4.Name = "Label4"
        Me.Label4.Size = New System.Drawing.Size(39, 13)
        Me.Label4.TabIndex = 16
        Me.Label4.Text = "EPSG:"
        '
        'HScrollBar1
        '
        Me.HScrollBar1.Anchor = CType((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.HScrollBar1.Location = New System.Drawing.Point(540, 377)
        Me.HScrollBar1.Minimum = 1
        Me.HScrollBar1.Name = "HScrollBar1"
        Me.HScrollBar1.Size = New System.Drawing.Size(123, 17)
        Me.HScrollBar1.TabIndex = 17
        Me.HScrollBar1.Value = 1
        '
        'Label5
        '
        Me.Label5.Anchor = CType((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Left), System.Windows.Forms.AnchorStyles)
        Me.Label5.AutoSize = True
        Me.Label5.Location = New System.Drawing.Point(148, 509)
        Me.Label5.Name = "Label5"
        Me.Label5.Size = New System.Drawing.Size(42, 13)
        Me.Label5.TabIndex = 18
        Me.Label5.Text = "Format:"
        '
        'ComboBoxFormat
        '
        Me.ComboBoxFormat.Anchor = CType((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Left), System.Windows.Forms.AnchorStyles)
        Me.ComboBoxFormat.FormattingEnabled = True
        Me.ComboBoxFormat.Location = New System.Drawing.Point(151, 526)
        Me.ComboBoxFormat.Name = "ComboBoxFormat"
        Me.ComboBoxFormat.Size = New System.Drawing.Size(121, 21)
        Me.ComboBoxFormat.TabIndex = 19
        '
        'ComboBoxBBox
        '
        Me.ComboBoxBBox.Anchor = CType(((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Left) _
                    Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.ComboBoxBBox.FormattingEnabled = True
        Me.ComboBoxBBox.Location = New System.Drawing.Point(20, 567)
        Me.ComboBoxBBox.Name = "ComboBoxBBox"
        Me.ComboBoxBBox.Size = New System.Drawing.Size(496, 21)
        Me.ComboBoxBBox.TabIndex = 20
        '
        'Label6
        '
        Me.Label6.Anchor = CType((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Left), System.Windows.Forms.AnchorStyles)
        Me.Label6.AutoSize = True
        Me.Label6.Location = New System.Drawing.Point(22, 550)
        Me.Label6.Name = "Label6"
        Me.Label6.Size = New System.Drawing.Size(35, 13)
        Me.Label6.TabIndex = 21
        Me.Label6.Text = "BBox:"
        '
        'ComboBoxBBoxI
        '
        Me.ComboBoxBBoxI.Anchor = CType((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Left), System.Windows.Forms.AnchorStyles)
        Me.ComboBoxBBoxI.FormattingEnabled = True
        Me.ComboBoxBBoxI.Location = New System.Drawing.Point(20, 580)
        Me.ComboBoxBBoxI.Name = "ComboBoxBBoxI"
        Me.ComboBoxBBoxI.Size = New System.Drawing.Size(252, 21)
        Me.ComboBoxBBoxI.TabIndex = 22
        Me.ComboBoxBBoxI.Visible = False
        '
        'ButtonFindScale
        '
        Me.ButtonFindScale.Anchor = CType((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.ButtonFindScale.Location = New System.Drawing.Point(540, 439)
        Me.ButtonFindScale.Name = "ButtonFindScale"
        Me.ButtonFindScale.Size = New System.Drawing.Size(123, 36)
        Me.ButtonFindScale.TabIndex = 23
        Me.ButtonFindScale.Text = "Optimale Scale finden"
        Me.ButtonFindScale.UseVisualStyleBackColor = True
        '
        'capxml2wms
        '
        Me.AutoScaleDimensions = New System.Drawing.SizeF(6.0!, 13.0!)
        Me.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font
        Me.ClientSize = New System.Drawing.Size(675, 601)
        Me.Controls.Add(Me.ButtonFindScale)
        Me.Controls.Add(Me.ComboBoxBBoxI)
        Me.Controls.Add(Me.Label6)
        Me.Controls.Add(Me.ComboBoxBBox)
        Me.Controls.Add(Me.ComboBoxFormat)
        Me.Controls.Add(Me.Label5)
        Me.Controls.Add(Me.HScrollBar1)
        Me.Controls.Add(Me.Label4)
        Me.Controls.Add(Me.ComboBoxEPSG)
        Me.Controls.Add(Me.ButtonCheckAll)
        Me.Controls.Add(Me.ButtonCheckNone)
        Me.Controls.Add(Me.Label3)
        Me.Controls.Add(Me.CheckedListBoxLayers)
        Me.Controls.Add(Me.ButtonShowMap)
        Me.Controls.Add(Me.ButtonGetCapabilities)
        Me.Controls.Add(Me.ComboBoxVersion)
        Me.Controls.Add(Me.Label2)
        Me.Controls.Add(Me.TextBoxXML)
        Me.Controls.Add(Me.Label1)
        Me.Controls.Add(Me.TextBoxUrl)
        Me.Controls.Add(Me.ButtonErstellen)
        Me.Icon = CType(resources.GetObject("$this.Icon"), System.Drawing.Icon)
        Me.Name = "capxml2wms"
        Me.Text = "capxml2wms"
        Me.ResumeLayout(False)
        Me.PerformLayout()

    End Sub
    Private WithEvents ButtonErstellen As System.Windows.Forms.Button
    Private WithEvents TextBoxUrl As System.Windows.Forms.TextBox
    Private WithEvents Label1 As System.Windows.Forms.Label
    Private WithEvents TextBoxXML As System.Windows.Forms.TextBox
    Private WithEvents ComboBoxVersion As System.Windows.Forms.ComboBox
    Private WithEvents Label2 As System.Windows.Forms.Label
    Private WithEvents ButtonGetCapabilities As System.Windows.Forms.Button
    Private WithEvents ButtonShowMap As System.Windows.Forms.Button
    Private WithEvents CheckedListBoxLayers As System.Windows.Forms.CheckedListBox
    Private WithEvents Label3 As System.Windows.Forms.Label
    Private WithEvents ButtonCheckNone As System.Windows.Forms.Button
    Private WithEvents ButtonCheckAll As System.Windows.Forms.Button
    Private WithEvents Label4 As System.Windows.Forms.Label
    Private WithEvents ComboBoxEPSG As System.Windows.Forms.ComboBox
    Private WithEvents HScrollBar1 As System.Windows.Forms.HScrollBar
    Private WithEvents Label5 As System.Windows.Forms.Label
    Private WithEvents ComboBoxFormat As System.Windows.Forms.ComboBox
    Private WithEvents ComboBoxBBox As System.Windows.Forms.ComboBox
    Private WithEvents Label6 As System.Windows.Forms.Label
    Private WithEvents ComboBoxBBoxI As System.Windows.Forms.ComboBox
    Private WithEvents ButtonFindScale As System.Windows.Forms.Button

End Class
