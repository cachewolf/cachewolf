package CacheWolf.utils;

import ewe.io.IOException;
import ewe.io.JavaUtf8Codec;
import ewe.io.TextCodec;
import ewe.sys.Vm;
import ewe.util.CharArray;

/*
 GNU General Public License
 CacheWolf is a software for PocketPC, Win and Linux that
 enables paperless caching.
 It supports the sites geocaching.com and opencaching.de

 Copyright (C) 2006  CacheWolf development team
 See http://www.cachewolf.de/ for more information.
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; version 2 of the License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

public class BetterUTF8Codec extends JavaUtf8Codec implements TextCodec {

    private byte[] readbackBuffer;

    private BetterUTF8Codec(byte[] newReadBackBuffer) {
        readbackBuffer = newReadBackBuffer;
    }

    public BetterUTF8Codec() {
        this(new byte[0]);
    }

    public StringBuffer decodeUTF8(byte[] paramArrayOfByte, int start, int length) throws IOException {
        if (readbackBuffer != null) {
            byte[] b = new byte[length + readbackBuffer.length];
            Vm.arraycopy(readbackBuffer, 0, b, 0, readbackBuffer.length);
            Vm.arraycopy(paramArrayOfByte, start, b, readbackBuffer.length, length);
            length += readbackBuffer.length;
            paramArrayOfByte = b;
            readbackBuffer = new byte[0];
        }
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < paramArrayOfByte.length; i++) {
            byte b1 = paramArrayOfByte[i];
            if (b1 >= 0) {
                result.append((char) b1);
            } else if (b1 < 0) {
                if ((b1 & 0xe0) == 0xc0) {
                    if (i < paramArrayOfByte.length - 1) {
                        char ch1 = (char) (((int) b1 & 0x3f) << 6 | ((int) paramArrayOfByte[++i] & 0x3f));
                        result.append(ch1);
                    } else {
                        readbackBuffer = new byte[1];
                        readbackBuffer[0] = b1;
                        break;
                    }
                } else if ((b1 & 0xf0) == 0xe0) {
                    if (i < paramArrayOfByte.length - 2) {
                        b1 &= 0x1f;
                        int b2 = paramArrayOfByte[++i] & 0x3f;
                        int b3 = paramArrayOfByte[++i] & 0x3f;
                        char ch1 = (char) (((b1 << 6 | b2) << 6) | b3);
                        result.append(ch1);
                    } else {
                        readbackBuffer = new byte[paramArrayOfByte.length - i];
                        Vm.arraycopy(paramArrayOfByte, i, readbackBuffer, 0, readbackBuffer.length);
                        break;
                    }
                } else if ((b1 & 0xf8) == 0xf0) {
                    if (i < paramArrayOfByte.length - 3) {
                        result.append(" ");
                        i += 3;
                    } else {
                        readbackBuffer = new byte[paramArrayOfByte.length - i];
                        Vm.arraycopy(paramArrayOfByte, i, readbackBuffer, 0, readbackBuffer.length);
                        break;
                    }
                } else {
                    throw new IOException("Bad format");
                }

            }
        }
        return result;
    }

    public CharArray decodeText(byte[] paramArrayOfByte, int start, int length, boolean paramBoolean, CharArray paramCharArray) throws IOException {
        StringBuffer utf8 = decodeUTF8(paramArrayOfByte, start, length);
        if (paramCharArray == null) {
            paramCharArray = new CharArray();
        }

        paramCharArray.length = 0;
        paramCharArray.append(utf8.toString());

        return paramCharArray;
    }

    public Object getCopy() {
        byte[] newReadBackBuffer = new byte[readbackBuffer.length];
        Vm.arraycopy(readbackBuffer, 0, newReadBackBuffer, 0, readbackBuffer.length);
        return new BetterUTF8Codec(newReadBackBuffer);
    }

}
