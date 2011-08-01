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
        Me.ButtonErstellen = New System.Windows.Forms.Button()
        Me.Panel1 = New System.Windows.Forms.Panel()
        Me.ComboBoxBBoxI = New System.Windows.Forms.ComboBox()
        Me.Label6 = New System.Windows.Forms.Label()
        Me.ComboBoxBBox = New System.Windows.Forms.ComboBox()
        Me.ComboBoxFormat = New System.Windows.Forms.ComboBox()
        Me.Label5 = New System.Windows.Forms.Label()
        Me.Label4 = New System.Windows.Forms.Label()
        Me.ComboBoxEPSG = New System.Windows.Forms.ComboBox()
        Me.ButtonFindScale = New System.Windows.Forms.Button()
        Me.ButtonCheckAll = New System.Windows.Forms.Button()
        Me.ButtonCheckNone = New System.Windows.Forms.Button()
        Me.Label3 = New System.Windows.Forms.Label()
        Me.CheckedListBoxLayers = New System.Windows.Forms.CheckedListBox()
        Me.Panel2 = New System.Windows.Forms.Panel()
        Me.TextBoxXML = New System.Windows.Forms.TextBox()
        Me.ButtonGetCapabilities = New System.Windows.Forms.Button()
        Me.ComboBoxVersion = New System.Windows.Forms.ComboBox()
        Me.Label2 = New System.Windows.Forms.Label()
        Me.Label1 = New System.Windows.Forms.Label()
        Me.TextBoxUrl = New System.Windows.Forms.TextBox()
        Me.Panel1.SuspendLayout()
        Me.Panel2.SuspendLayout()
        Me.SuspendLayout()
        '
        'ButtonErstellen
        '
        Me.ButtonErstellen.Anchor = CType((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.ButtonErstellen.Location = New System.Drawing.Point(537, 553)
        Me.ButtonErstellen.Name = "ButtonErstellen"
        Me.ButtonErstellen.Size = New System.Drawing.Size(123, 36)
        Me.ButtonErstellen.TabIndex = 1
        Me.ButtonErstellen.Text = "3. WMS-Datei erstellen"
        Me.ButtonErstellen.UseVisualStyleBackColor = True
        '
        'Panel1
        '
        Me.Panel1.Anchor = CType((((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Bottom) _
                    Or System.Windows.Forms.AnchorStyles.Left) _
                    Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.Panel1.Controls.Add(Me.ComboBoxBBoxI)
        Me.Panel1.Controls.Add(Me.Label6)
        Me.Panel1.Controls.Add(Me.ComboBoxBBox)
        Me.Panel1.Controls.Add(Me.ComboBoxFormat)
        Me.Panel1.Controls.Add(Me.Label5)
        Me.Panel1.Controls.Add(Me.Label4)
        Me.Panel1.Controls.Add(Me.ComboBoxEPSG)
        Me.Panel1.Controls.Add(Me.ButtonFindScale)
        Me.Panel1.Controls.Add(Me.ButtonCheckAll)
        Me.Panel1.Controls.Add(Me.ButtonCheckNone)
        Me.Panel1.Controls.Add(Me.Label3)
        Me.Panel1.Controls.Add(Me.CheckedListBoxLayers)
        Me.Panel1.Location = New System.Drawing.Point(20, 240)
        Me.Panel1.Name = "Panel1"
        Me.Panel1.Size = New System.Drawing.Size(643, 306)
        Me.Panel1.TabIndex = 24
        '
        'ComboBoxBBoxI
        '
        Me.ComboBoxBBoxI.FormattingEnabled = True
        Me.ComboBoxBBoxI.Location = New System.Drawing.Point(190, 36)
        Me.ComboBoxBBoxI.Name = "ComboBoxBBoxI"
        Me.ComboBoxBBoxI.Size = New System.Drawing.Size(252, 21)
        Me.ComboBoxBBoxI.TabIndex = 35
        Me.ComboBoxBBoxI.Visible = False
        '
        'Label6
        '
        Me.Label6.AutoSize = True
        Me.Label6.Location = New System.Drawing.Point(141, 6)
        Me.Label6.Name = "Label6"
        Me.Label6.Size = New System.Drawing.Size(35, 13)
        Me.Label6.TabIndex = 34
        Me.Label6.Text = "BBox:"
        '
        'ComboBoxBBox
        '
        Me.ComboBoxBBox.Anchor = CType(((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Left) _
                    Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.ComboBoxBBox.FormattingEnabled = True
        Me.ComboBoxBBox.Location = New System.Drawing.Point(144, 23)
        Me.ComboBoxBBox.Name = "ComboBoxBBox"
        Me.ComboBoxBBox.Size = New System.Drawing.Size(489, 21)
        Me.ComboBoxBBox.TabIndex = 33
        '
        'ComboBoxFormat
        '
        Me.ComboBoxFormat.Anchor = CType((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.ComboBoxFormat.FormattingEnabled = True
        Me.ComboBoxFormat.Location = New System.Drawing.Point(512, 104)
        Me.ComboBoxFormat.Name = "ComboBoxFormat"
        Me.ComboBoxFormat.Size = New System.Drawing.Size(121, 21)
        Me.ComboBoxFormat.TabIndex = 32
        '
        'Label5
        '
        Me.Label5.Anchor = CType((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.Label5.AutoSize = True
        Me.Label5.Location = New System.Drawing.Point(514, 88)
        Me.Label5.Name = "Label5"
        Me.Label5.Size = New System.Drawing.Size(42, 13)
        Me.Label5.TabIndex = 31
        Me.Label5.Text = "Format:"
        '
        'Label4
        '
        Me.Label4.Anchor = CType((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.Label4.AutoSize = True
        Me.Label4.Location = New System.Drawing.Point(514, 47)
        Me.Label4.Name = "Label4"
        Me.Label4.Size = New System.Drawing.Size(39, 13)
        Me.Label4.TabIndex = 30
        Me.Label4.Text = "EPSG:"
        '
        'ComboBoxEPSG
        '
        Me.ComboBoxEPSG.Anchor = CType((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.ComboBoxEPSG.FormattingEnabled = True
        Me.ComboBoxEPSG.Location = New System.Drawing.Point(512, 64)
        Me.ComboBoxEPSG.Name = "ComboBoxEPSG"
        Me.ComboBoxEPSG.Size = New System.Drawing.Size(121, 21)
        Me.ComboBoxEPSG.TabIndex = 29
        '
        'ButtonFindScale
        '
        Me.ButtonFindScale.Anchor = CType((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.ButtonFindScale.Location = New System.Drawing.Point(517, 265)
        Me.ButtonFindScale.Name = "ButtonFindScale"
        Me.ButtonFindScale.Size = New System.Drawing.Size(123, 36)
        Me.ButtonFindScale.TabIndex = 28
        Me.ButtonFindScale.Text = "2. Optimale Scale finden"
        Me.ButtonFindScale.UseVisualStyleBackColor = True
        '
        'ButtonCheckAll
        '
        Me.ButtonCheckAll.Location = New System.Drawing.Point(6, 24)
        Me.ButtonCheckAll.Name = "ButtonCheckAll"
        Me.ButtonCheckAll.Size = New System.Drawing.Size(60, 20)
        Me.ButtonCheckAll.TabIndex = 27
        Me.ButtonCheckAll.Text = "alle  x"
        Me.ButtonCheckAll.UseVisualStyleBackColor = True
        '
        'ButtonCheckNone
        '
        Me.ButtonCheckNone.Location = New System.Drawing.Point(72, 23)
        Me.ButtonCheckNone.Name = "ButtonCheckNone"
        Me.ButtonCheckNone.Size = New System.Drawing.Size(60, 20)
        Me.ButtonCheckNone.TabIndex = 26
        Me.ButtonCheckNone.Text = "kein x"
        Me.ButtonCheckNone.UseVisualStyleBackColor = True
        '
        'Label3
        '
        Me.Label3.AutoSize = True
        Me.Label3.Location = New System.Drawing.Point(3, 6)
        Me.Label3.Name = "Label3"
        Me.Label3.Size = New System.Drawing.Size(36, 13)
        Me.Label3.TabIndex = 25
        Me.Label3.Text = "Layer:"
        '
        'CheckedListBoxLayers
        '
        Me.CheckedListBoxLayers.Anchor = CType((((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Bottom) _
                    Or System.Windows.Forms.AnchorStyles.Left) _
                    Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.CheckedListBoxLayers.CheckOnClick = True
        Me.CheckedListBoxLayers.FormattingEnabled = True
        Me.CheckedListBoxLayers.HorizontalScrollbar = True
        Me.CheckedListBoxLayers.Location = New System.Drawing.Point(6, 72)
        Me.CheckedListBoxLayers.Name = "CheckedListBoxLayers"
        Me.CheckedListBoxLayers.Size = New System.Drawing.Size(496, 229)
        Me.CheckedListBoxLayers.TabIndex = 24
        '
        'Panel2
        '
        Me.Panel2.Anchor = CType(((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Left) _
                    Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.Panel2.Controls.Add(Me.TextBoxXML)
        Me.Panel2.Controls.Add(Me.ButtonGetCapabilities)
        Me.Panel2.Controls.Add(Me.ComboBoxVersion)
        Me.Panel2.Controls.Add(Me.Label2)
        Me.Panel2.Controls.Add(Me.Label1)
        Me.Panel2.Controls.Add(Me.TextBoxUrl)
        Me.Panel2.Location = New System.Drawing.Point(20, 12)
        Me.Panel2.Name = "Panel2"
        Me.Panel2.Size = New System.Drawing.Size(643, 222)
        Me.Panel2.TabIndex = 25
        '
        'TextBoxXML
        '
        Me.TextBoxXML.Anchor = CType((((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Bottom) _
                    Or System.Windows.Forms.AnchorStyles.Left) _
                    Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.TextBoxXML.Location = New System.Drawing.Point(6, 52)
        Me.TextBoxXML.Multiline = True
        Me.TextBoxXML.Name = "TextBoxXML"
        Me.TextBoxXML.ReadOnly = True
        Me.TextBoxXML.ScrollBars = System.Windows.Forms.ScrollBars.Both
        Me.TextBoxXML.Size = New System.Drawing.Size(627, 167)
        Me.TextBoxXML.TabIndex = 13
        Me.TextBoxXML.WordWrap = False
        '
        'ButtonGetCapabilities
        '
        Me.ButtonGetCapabilities.Anchor = CType((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.ButtonGetCapabilities.Location = New System.Drawing.Point(517, 9)
        Me.ButtonGetCapabilities.Name = "ButtonGetCapabilities"
        Me.ButtonGetCapabilities.Size = New System.Drawing.Size(123, 36)
        Me.ButtonGetCapabilities.TabIndex = 12
        Me.ButtonGetCapabilities.Text = "1. Möglichkeiten abfragen"
        Me.ButtonGetCapabilities.UseVisualStyleBackColor = True
        '
        'ComboBoxVersion
        '
        Me.ComboBoxVersion.Anchor = CType((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.ComboBoxVersion.FormattingEnabled = True
        Me.ComboBoxVersion.Items.AddRange(New Object() {"1.0.0", "1.1.0", "1.1.1", "1.3.0"})
        Me.ComboBoxVersion.Location = New System.Drawing.Point(412, 25)
        Me.ComboBoxVersion.MaxDropDownItems = 3
        Me.ComboBoxVersion.Name = "ComboBoxVersion"
        Me.ComboBoxVersion.Size = New System.Drawing.Size(84, 21)
        Me.ComboBoxVersion.TabIndex = 11
        '
        'Label2
        '
        Me.Label2.Anchor = CType((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.Label2.AutoSize = True
        Me.Label2.Location = New System.Drawing.Point(409, 9)
        Me.Label2.Name = "Label2"
        Me.Label2.Size = New System.Drawing.Size(75, 13)
        Me.Label2.TabIndex = 10
        Me.Label2.Text = "WMS-Version:"
        '
        'Label1
        '
        Me.Label1.Anchor = CType(((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Left) _
                    Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.Label1.AutoSize = True
        Me.Label1.Location = New System.Drawing.Point(3, 9)
        Me.Label1.Name = "Label1"
        Me.Label1.Size = New System.Drawing.Size(205, 13)
        Me.Label1.TabIndex = 9
        Me.Label1.Text = "http Adresse des WMS-Servers eingeben:"
        '
        'TextBoxUrl
        '
        Me.TextBoxUrl.Anchor = CType(((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Left) _
                    Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.TextBoxUrl.Location = New System.Drawing.Point(6, 25)
        Me.TextBoxUrl.Name = "TextBoxUrl"
        Me.TextBoxUrl.Size = New System.Drawing.Size(400, 20)
        Me.TextBoxUrl.TabIndex = 8
        '
        'capxml2wms
        '
        Me.AutoScaleDimensions = New System.Drawing.SizeF(6.0!, 13.0!)
        Me.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font
        Me.ClientSize = New System.Drawing.Size(675, 601)
        Me.Controls.Add(Me.Panel2)
        Me.Controls.Add(Me.Panel1)
        Me.Controls.Add(Me.ButtonErstellen)
        Me.Icon = CType(resources.GetObject("$this.Icon"), System.Drawing.Icon)
        Me.Name = "capxml2wms"
        Me.Text = "capxml2wms"
        Me.Panel1.ResumeLayout(False)
        Me.Panel1.PerformLayout()
        Me.Panel2.ResumeLayout(False)
        Me.Panel2.PerformLayout()
        Me.ResumeLayout(False)

    End Sub
    Private WithEvents ButtonErstellen As System.Windows.Forms.Button
    Friend WithEvents Panel1 As System.Windows.Forms.Panel
    Private WithEvents ButtonFindScale As System.Windows.Forms.Button
    Private WithEvents ButtonCheckAll As System.Windows.Forms.Button
    Private WithEvents ButtonCheckNone As System.Windows.Forms.Button
    Private WithEvents Label3 As System.Windows.Forms.Label
    Private WithEvents CheckedListBoxLayers As System.Windows.Forms.CheckedListBox
    Private WithEvents ComboBoxBBoxI As System.Windows.Forms.ComboBox
    Private WithEvents Label6 As System.Windows.Forms.Label
    Private WithEvents ComboBoxBBox As System.Windows.Forms.ComboBox
    Private WithEvents ComboBoxFormat As System.Windows.Forms.ComboBox
    Private WithEvents Label5 As System.Windows.Forms.Label
    Private WithEvents Label4 As System.Windows.Forms.Label
    Private WithEvents ComboBoxEPSG As System.Windows.Forms.ComboBox
    Friend WithEvents Panel2 As System.Windows.Forms.Panel
    Private WithEvents TextBoxXML As System.Windows.Forms.TextBox
    Private WithEvents ButtonGetCapabilities As System.Windows.Forms.Button
    Private WithEvents ComboBoxVersion As System.Windows.Forms.ComboBox
    Private WithEvents Label2 As System.Windows.Forms.Label
    Private WithEvents Label1 As System.Windows.Forms.Label
    Private WithEvents TextBoxUrl As System.Windows.Forms.TextBox

End Class
