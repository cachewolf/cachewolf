Imports System.Drawing.Imaging
Imports System.Runtime.InteropServices
Public Class DoFiles
    Private Sub OpenSave(ByVal fn As String)
        Dim img As Bitmap = Bitmap.FromFile(fn)
        Dim rect As Rectangle = New Rectangle(0, 0, img.Width, img.Height)
        Dim bmpData As BitmapData = img.LockBits(rect, ImageLockMode.ReadWrite, img.PixelFormat)

        'Get the address of the first line of the bitmap.
        Dim ptr As IntPtr = bmpData.Scan0

        ' Declare an array to hold the bytes of the bitmap.
        Dim numBytes As Integer = bmpData.Stride * img.Height
        Dim rgbValues(numBytes) As Byte

        ' Copy the RGB values into the array.
        Marshal.Copy(ptr, rgbValues, 0, numBytes)

        ' Manipulate the bitmap
        For counter As Integer = 0 To rgbValues.Length - 3 Step 4
            If ((rgbValues(counter) = 0) And _
               (rgbValues(counter + 1) = 0) And _
               (rgbValues(counter + 2) = 0) And _
               (rgbValues(counter + 3) = 0)) Then
                rgbValues(counter) = 255
                rgbValues(counter + 1) = 255
                rgbValues(counter + 2) = 255
                rgbValues(counter + 3) = 255
            End If
        Next counter

        ' Copy the RGB values back to the bitmap
        Marshal.Copy(rgbValues, 0, ptr, numBytes)
        ' Unlock the bits.
        img.UnlockBits(bmpData)

        Dim ImageEncoder As ImageCodecInfo = Nothing
        Dim Encoders() As ImageCodecInfo = ImageCodecInfo.GetImageEncoders()
        For i As Integer = 0 To Encoders.Length - 1
            If Encoders(i).FilenameExtension.ToLower.EndsWith("png") Then
                ImageEncoder = Encoders(i)
                Exit For
            End If
        Next

        Dim EncoderParams As EncoderParameters = New EncoderParameters(1)
        'Dim Pars As New Encoder(ImageEncoder.Clsid)
        EncoderParams.Param(0) = New EncoderParameter(Encoder.ColorDepth, ColorDepth.Depth8Bit)
        'IO.File.Delete(fn)

        'img.Save("c:\test.png", ImageEncoder, EncoderParams)
        img.Save(IO.Path.ChangeExtension(fn, ".gif"), ImageFormat.Gif)

    End Sub

    Private Function HandleFiles(ByVal path As String) As Boolean
        Dim d As New System.IO.DirectoryInfo(path)
        Dim fs As System.IO.FileInfo()
        fs = d.GetFiles()
        For Each f As System.IO.FileInfo In fs
            Dim fn As String = f.FullName
            Dim fe As String = IO.Path.GetExtension(fn).ToLower
            If fe.ToLower = ".png" Then
                OpenSave(fn)
            End If
        Next
        'For Each g As System.IO.DirectoryInfo In d.GetDirectories
        '    HandleFiles(g.FullName)
        'Next
    End Function

    Private Sub Button2_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles OK.Click
        Me.FBDialog.RootFolder = Environment.SpecialFolder.MyComputer
        Me.FBDialog.SelectedPath = "C:"
        If Me.FBDialog.ShowDialog = Windows.Forms.DialogResult.OK Then
            HandleFiles(FBDialog.SelectedPath)
        End If
    End Sub

    Private Sub Abbruch_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles Abbruch.Click
        Me.Close()
    End Sub
End Class
