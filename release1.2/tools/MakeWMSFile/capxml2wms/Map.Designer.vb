<Global.Microsoft.VisualBasic.CompilerServices.DesignerGenerated()> _
Partial Class Map
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
        Me.PictureBox = New System.Windows.Forms.PictureBox
        Me.LabelScaleVertical = New System.Windows.Forms.Label
        Me.ButtonScaleDown = New System.Windows.Forms.Button
        Me.ButtonScaleUp = New System.Windows.Forms.Button
        Me.LabelScaleDiagonal = New System.Windows.Forms.Label
        Me.ButtonUseAsMaxScale = New System.Windows.Forms.Button
        Me.ButtonAddToRecommendedScale = New System.Windows.Forms.Button
        Me.ButtonUseAsMinScale = New System.Windows.Forms.Button
        Me.ButtonHelp = New System.Windows.Forms.Button
        CType(Me.PictureBox, System.ComponentModel.ISupportInitialize).BeginInit()
        Me.SuspendLayout()
        '
        'PictureBox
        '
        Me.PictureBox.Dock = System.Windows.Forms.DockStyle.Fill
        Me.PictureBox.Location = New System.Drawing.Point(0, 0)
        Me.PictureBox.Name = "PictureBox"
        Me.PictureBox.Size = New System.Drawing.Size(674, 521)
        Me.PictureBox.SizeMode = System.Windows.Forms.PictureBoxSizeMode.Zoom
        Me.PictureBox.TabIndex = 0
        Me.PictureBox.TabStop = False
        '
        'LabelScaleVertical
        '
        Me.LabelScaleVertical.AutoSize = True
        Me.LabelScaleVertical.Location = New System.Drawing.Point(179, 470)
        Me.LabelScaleVertical.Name = "LabelScaleVertical"
        Me.LabelScaleVertical.Size = New System.Drawing.Size(98, 13)
        Me.LabelScaleVertical.TabIndex = 1
        Me.LabelScaleVertical.Text = "LabelScale Vertikal"
        '
        'ButtonScaleDown
        '
        Me.ButtonScaleDown.Location = New System.Drawing.Point(361, 470)
        Me.ButtonScaleDown.Name = "ButtonScaleDown"
        Me.ButtonScaleDown.Size = New System.Drawing.Size(85, 23)
        Me.ButtonScaleDown.TabIndex = 2
        Me.ButtonScaleDown.Text = "Scale down"
        Me.ButtonScaleDown.UseVisualStyleBackColor = True
        '
        'ButtonScaleUp
        '
        Me.ButtonScaleUp.Location = New System.Drawing.Point(73, 470)
        Me.ButtonScaleUp.Name = "ButtonScaleUp"
        Me.ButtonScaleUp.Size = New System.Drawing.Size(85, 23)
        Me.ButtonScaleUp.TabIndex = 3
        Me.ButtonScaleUp.Text = "Scale up"
        Me.ButtonScaleUp.UseVisualStyleBackColor = True
        '
        'LabelScaleDiagonal
        '
        Me.LabelScaleDiagonal.AutoSize = True
        Me.LabelScaleDiagonal.Location = New System.Drawing.Point(179, 444)
        Me.LabelScaleDiagonal.Name = "LabelScaleDiagonal"
        Me.LabelScaleDiagonal.Size = New System.Drawing.Size(102, 13)
        Me.LabelScaleDiagonal.TabIndex = 4
        Me.LabelScaleDiagonal.Text = "LabelScaleDiagonal"
        '
        'ButtonUseAsMaxScale
        '
        Me.ButtonUseAsMaxScale.Location = New System.Drawing.Point(452, 385)
        Me.ButtonUseAsMaxScale.Name = "ButtonUseAsMaxScale"
        Me.ButtonUseAsMaxScale.Size = New System.Drawing.Size(195, 33)
        Me.ButtonUseAsMaxScale.TabIndex = 5
        Me.ButtonUseAsMaxScale.Text = "Use as MaxScale"
        Me.ButtonUseAsMaxScale.UseVisualStyleBackColor = True
        '
        'ButtonAddToRecommendedScale
        '
        Me.ButtonAddToRecommendedScale.Location = New System.Drawing.Point(452, 427)
        Me.ButtonAddToRecommendedScale.Name = "ButtonAddToRecommendedScale"
        Me.ButtonAddToRecommendedScale.Size = New System.Drawing.Size(194, 30)
        Me.ButtonAddToRecommendedScale.TabIndex = 6
        Me.ButtonAddToRecommendedScale.Text = "Add to recommended scale"
        Me.ButtonAddToRecommendedScale.UseVisualStyleBackColor = True
        '
        'ButtonUseAsMinScale
        '
        Me.ButtonUseAsMinScale.Location = New System.Drawing.Point(452, 463)
        Me.ButtonUseAsMinScale.Name = "ButtonUseAsMinScale"
        Me.ButtonUseAsMinScale.Size = New System.Drawing.Size(195, 30)
        Me.ButtonUseAsMinScale.TabIndex = 7
        Me.ButtonUseAsMinScale.Text = "Use as MinScale"
        Me.ButtonUseAsMinScale.UseVisualStyleBackColor = True
        '
        'ButtonHelp
        '
        Me.ButtonHelp.Location = New System.Drawing.Point(529, 346)
        Me.ButtonHelp.Name = "ButtonHelp"
        Me.ButtonHelp.Size = New System.Drawing.Size(41, 22)
        Me.ButtonHelp.TabIndex = 8
        Me.ButtonHelp.Text = "Help"
        Me.ButtonHelp.UseVisualStyleBackColor = True
        '
        'Map
        '
        Me.AutoScaleDimensions = New System.Drawing.SizeF(6.0!, 13.0!)
        Me.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font
        Me.ClientSize = New System.Drawing.Size(674, 521)
        Me.Controls.Add(Me.ButtonHelp)
        Me.Controls.Add(Me.ButtonUseAsMinScale)
        Me.Controls.Add(Me.ButtonAddToRecommendedScale)
        Me.Controls.Add(Me.ButtonUseAsMaxScale)
        Me.Controls.Add(Me.LabelScaleDiagonal)
        Me.Controls.Add(Me.ButtonScaleUp)
        Me.Controls.Add(Me.ButtonScaleDown)
        Me.Controls.Add(Me.LabelScaleVertical)
        Me.Controls.Add(Me.PictureBox)
        Me.Name = "Map"
        Me.ShowIcon = False
        Me.StartPosition = System.Windows.Forms.FormStartPosition.Manual
        Me.Text = "Map"
        CType(Me.PictureBox, System.ComponentModel.ISupportInitialize).EndInit()
        Me.ResumeLayout(False)
        Me.PerformLayout()

    End Sub
    Private WithEvents PictureBox As System.Windows.Forms.PictureBox
    Friend WithEvents LabelScaleVertical As System.Windows.Forms.Label
    Friend WithEvents ButtonScaleDown As System.Windows.Forms.Button
    Friend WithEvents LabelScaleDiagonal As System.Windows.Forms.Label
    Friend WithEvents ButtonUseAsMaxScale As System.Windows.Forms.Button
    Friend WithEvents ButtonAddToRecommendedScale As System.Windows.Forms.Button
    Friend WithEvents ButtonUseAsMinScale As System.Windows.Forms.Button
    Friend WithEvents ButtonHelp As System.Windows.Forms.Button
    Friend WithEvents ButtonScaleUp As System.Windows.Forms.Button
End Class
