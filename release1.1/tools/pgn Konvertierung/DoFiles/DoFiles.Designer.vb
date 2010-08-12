<Global.Microsoft.VisualBasic.CompilerServices.DesignerGenerated()> _
Partial Class DoFiles
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
        Me.FBDialog = New System.Windows.Forms.FolderBrowserDialog
        Me.Abbruch = New System.Windows.Forms.Button
        Me.OK = New System.Windows.Forms.Button
        Me.OpenFileDialog1 = New System.Windows.Forms.OpenFileDialog
        Me.SuspendLayout()
        '
        'Abbruch
        '
        Me.Abbruch.Anchor = CType((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.Abbruch.DialogResult = System.Windows.Forms.DialogResult.Abort
        Me.Abbruch.Location = New System.Drawing.Point(99, 57)
        Me.Abbruch.Name = "Abbruch"
        Me.Abbruch.Size = New System.Drawing.Size(75, 23)
        Me.Abbruch.TabIndex = 0
        Me.Abbruch.Text = "Ende"
        Me.Abbruch.UseVisualStyleBackColor = True
        '
        'OK
        '
        Me.OK.Anchor = CType((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.OK.DialogResult = System.Windows.Forms.DialogResult.OK
        Me.OK.Location = New System.Drawing.Point(10, 17)
        Me.OK.Name = "OK"
        Me.OK.Size = New System.Drawing.Size(164, 23)
        Me.OK.TabIndex = 1
        Me.OK.Text = "Verz. auswählen"
        Me.OK.UseVisualStyleBackColor = True
        '
        'OpenFileDialog1
        '
        Me.OpenFileDialog1.FileName = "OpenFileDialog1"
        '
        'DoFiles
        '
        Me.AutoScaleDimensions = New System.Drawing.SizeF(6.0!, 13.0!)
        Me.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font
        Me.ClientSize = New System.Drawing.Size(186, 95)
        Me.Controls.Add(Me.OK)
        Me.Controls.Add(Me.Abbruch)
        Me.Name = "DoFiles"
        Me.Text = "png Konvertierung"
        Me.ResumeLayout(False)

    End Sub
    Private WithEvents Abbruch As System.Windows.Forms.Button
    Private WithEvents OK As System.Windows.Forms.Button
    Private WithEvents FBDialog As System.Windows.Forms.FolderBrowserDialog
    Friend WithEvents OpenFileDialog1 As System.Windows.Forms.OpenFileDialog

End Class
