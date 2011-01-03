/* MifareKeyCracker
 *
 * Copyright (C) 2011 by Radoslaw '[roscoe]' Matusiak <radoslaw.matusiak@google.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package pl.cyberdev.mifarekeycracker;

public class BasicCard implements ICard{
    private byte[] pollingResult = null;

    /*
     *  Constructor.
     */
    public BasicCard(byte[] pollingResult) {
        this.pollingResult = pollingResult;
    }

    public byte[] getUID() {
        int uidStartPos = 7;
        int uidLength = pollingResult[uidStartPos];

        byte[] uid = new byte[uidLength];
        System.arraycopy(pollingResult, uidStartPos+1, uid, 0, uidLength);

        return uid;
    }

    public TagType getTagType() {

        byte sel_res = (byte)pollingResult[6];

        switch(sel_res) {
            case (byte)0x00: return TagType.MIFARE_ULTRALIGHT;
            case (byte)0x08: return TagType.MIFARE_1K;
            case (byte)0x09: return TagType.MIFARE_MINI;
            case (byte)0x18: return TagType.MIFARE_4K;
            case (byte)0x20: return TagType.MIFARE_DESFIRE;
            case (byte)0x28: return TagType.JCOP30;
            case (byte)0x98: return TagType.GEMPLUS_MPCOS;

            default: return TagType.UNKNOWN;
        }
    }
}
