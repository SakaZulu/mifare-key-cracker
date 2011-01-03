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

public class BruteXForce {

    private static byte[] hashKey = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    private BruteXForce() {
    }

    public static byte[] getKey() {
        return hashKey;
    }

    public static void getNextKey() {
        long key = byteArrayToLong(hashKey);
        key++;
        longToByteArray(key);
    }

    public static boolean hasNextKey() {
        return (byteArrayToLong(hashKey) <= 0xffffffffffffL) ? true : false;
    }

    public static String getKeyAsString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < hashKey.length; i++) {
            if (hashKey[i] < 16 && hashKey[i] >= 0) {
                sb.append("0x0");
                sb.append(Integer.toHexString(hashKey[i]));
            } else if (hashKey[i] > 16) {
                sb.append("0x");
                sb.append(Integer.toHexString(hashKey[i]));
            } else {
                sb.append("0x");
                String s = Integer.toHexString(hashKey[i]);
                sb.append(s.substring(s.length()-2));
            }

            sb.append(" ");
        }

        return sb.toString();
    }

    private static long byteArrayToLong(byte[] array) {
        long l = 0;
        l = ((long)array[0]&0xff) << 40 | ((long)array[1]&0xff) << 32 | ((long)array[2]&0xff) << 24 | ((long)array[3]&0xff) << 16 | ((long)array[4]&0xff) << 8 | ((long)array[5]&0xff);
        return l;
    }

    private static void longToByteArray(long number) {
        hashKey[0] = (byte) (number & (byte) 0xff);
        hashKey[1] = (byte) (number & (byte) 0xff);
        hashKey[2] = (byte) (number & (byte) 0xff);
        hashKey[3] = (byte) (number & (byte) 0xff);
        hashKey[4] = (byte) (number & (byte) 0xff);
        hashKey[5] = (byte) (number & (byte) 0xff);
    }
}
