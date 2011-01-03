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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

public class Main {

    //Handler to card terminal
    private static CardTerminal terminal = null;
    private static CardChannel channel = null;
    private static String fileName = "keys.txt";
    private static String blocks = null;
    private static boolean keyBAuthentication = false;
    private static String customKey = null;
    private static boolean bruteforce = false;
    private static boolean bruteXforce = false;
    //APDU used to poll MIFARE 1K/4K, Ultralight, ISO 14443-4 Type A and Type B Tags, 106 kbps.
    //Great command to check contactless communication.
    //E.g Response if tag found: 0xD5 0x4B 0x01 0x01 0x00 0x02 0x18 0x04 0xF6 0x8E 0x2A 0x99 0x90 0x00
    //Response if no tag found: 0x61 0x05
    public static final byte[] POLL_THE_TAG = {(byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xD4, (byte) 0x4A, (byte) 0x01, (byte) 0x00};

    /**
     * @param args the command line arguments
     */
    public static void main(String[] arguments) throws IOException {

        if (cmdLineSearch(arguments, "-h") >= 0) {


            System.out.println("Usage: java -jar MifareKeyCracker.jar [-h] [-keyb] [-f path] [-b 1,7...] [-k key]\n");
            System.out.printf("\t%-10s %s", "-h", "Display this help message\n");
            System.out.printf("\t%-10s %s", "-keyb", "Use KEY B authentication instead of KEY A\n");
            System.out.printf("\t%-10s %s", "-f path", "Use specified file with keys\n");
            System.out.printf("\t%-10s %s", "-b 1,7...", "Try to authenticate only given blocks (comma separated)\n");
            System.out.printf("\t%-10s %s", "-k key", "Use given key instead of keys file. I.e. aabbccddeeff\n");
            System.out.printf("\t%-10s %s", "-bf", "Brute force key\n");
            System.out.printf("\t%-10s %s", "-bx", "Brute force key (each key byte is equal)\n");
            System.exit(0);
        }

        if (cmdLineSearch(arguments, "-keyb") >= 0) {
            keyBAuthentication = true;
            System.out.println("[INFO] Using KEY B authentication.");
        }

        int index = -1;
        if ((index = cmdLineSearch(arguments, "-k")) >= 0) {
            if (arguments.length > index + 1) {
                customKey = arguments[index + 1];
                System.out.println("[INFO] Custom key: " + customKey);
            } else {
                System.out.println("[ERROR] Custom key was not specified!");
                System.exit(-1);
            }
        }

        index = -1;
        if ((index = cmdLineSearch(arguments, "-f")) >= 0 && customKey == null) { //This option is valid only when no custom key
            if (arguments.length > index + 1) {                                  //was specified
                fileName = arguments[index + 1];
                System.out.println("[INFO] Using keys file: " + fileName);
            } else {
                System.out.println("[ERROR] Keys file was not specified!");
                System.exit(-1);
            }
        }

        index = -1;
        if ((index = cmdLineSearch(arguments, "-b")) >= 0) {
            blocks = arguments[index + 1];
            System.out.println("[INFO] Blocks: " + blocks);
        }

        index = -1;
        if ((index = cmdLineSearch(arguments, "-bf")) >= 0) {
            bruteforce = true;
            System.out.println("[INFO] Bruteforce key.");
        }

        index = -1;
        if ((index = cmdLineSearch(arguments, "-bx")) >= 0) {
            bruteXforce = true;
            System.out.println("[INFO] Bruteforce key.");
        }

        initReader();
        assert terminal != null : "[ASSERT] Could not find any terminal instance!";
        waitForCard();
    }

    /**
     * Enumerate all card readers in the system and
     * choose one which will be used.
     *
     * @throws IOException
     */
    private static void initReader() throws IOException {
        try {
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminalsList = factory.terminals().list();

            System.out.println("[INFO] Avaliable card readers:");
            for (int i = 0; i < terminalsList.size(); i++) {
                System.out.println("\t" + i + "..." + terminalsList.get(i));
            }

            //Ask user for card terminal to use
            System.out.print("\n\tUse terminal #: ");
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(isr);
            String selection = br.readLine();

            try {
                int selectionNumber = Integer.parseInt(selection);
                if (selectionNumber < terminalsList.size()) {
                    System.out.println("[INFO] Using: " + terminalsList.get(selectionNumber));
                    terminal = terminalsList.get(selectionNumber);
                } else {
                    System.out.println("\n[ERROR] Your number is to big? Any complex?");
                    System.exit(-1);
                }

            } catch (NumberFormatException nfex) {
                System.out.println("\n[ERROR] Is it too hard to enter the number?!?!");
                System.exit(-1);
            }


        } catch (CardException ex) {
            System.out.println("[ERROR] Check if any terminal is connected!");
            System.exit(-1);
        }
    }

    /**
     *
     */
    private static void waitForCard() {
        try {
            Card card = terminal.connect("*");
            channel = card.getBasicChannel();

            //Reset the card
            card.getATR();

            System.out.println("[INFO] Waiting for card...");
            CommandAPDU cmd = new CommandAPDU(POLL_THE_TAG);
            ResponseAPDU transmit = channel.transmit(cmd);

            ICard icard = CardFactory.getInstance().getCard(transmit.getBytes());
            String suid = Conversions.byte2String(icard.getUID());
            System.out.println("[INFO] Card UID: " + suid);
            System.out.println("[INFO] Card type: " + icard.getTagType());

            //If card is not MIFARE_1K or MIFARE_4K - exit
            TagType type = icard.getTagType();
            if (type != TagType.MIFARE_1K && type != TagType.MIFARE_4K) System.exit(0);

            if (bruteforce) {
                while(BruteForce.hasNextKey())
                {
                    byte[] key = BruteForce.getKey();
                    System.out.print("[INFO] Key "+BruteForce.getKeyAsString());
                    crackKeys(icard, key);
                    System.out.println();
                    BruteForce.getNextKey();
                }
            } else if(bruteXforce)
            {
                while(BruteXForce.hasNextKey())
                {
                    byte[] key = BruteXForce.getKey();
                    System.out.print("[INFO] Key "+BruteXForce.getKeyAsString());
                    crackKeys(icard, key);
                    System.out.println();
                    BruteXForce.getNextKey();
                }
            } else if (customKey == null) {
                try {
                    File keysFile = new File(fileName);
                    BufferedReader br = new BufferedReader(new FileReader(keysFile));

                    String line = "";
                    while ((line = br.readLine()) != null) {

                        System.out.print("[INFO] Key " + line.trim() + " ");
                        String[] bytes = line.split(" ");
                        byte[] key = new byte[bytes.length];

                        for (int i = 0; i < key.length; i++) {
                            key[i] = (byte) (int) Integer.decode(bytes[i]);
                        }

                        crackKeys(icard, key);
                        System.out.println();
                    }

                } catch (IOException ex) {
                    System.out.println("[ERROR] Sth wrong with keys file!");
                }
            } else if (customKey != null) {
                if (customKey.length() == 12) {

                    byte[] key = new byte[6];
                    System.out.print("[INFO] Key ");

                    for (int i = 0; i < 6; i++) {
                        String sub = customKey.substring(i * 2, i * 2 + 2);
                        System.out.print("0x" + sub + " ");
                        key[i] = (byte) (int) Integer.decode("0x" + sub);
                    }

                    crackKeys(icard, key);

                } else {
                    System.out.println("[ERROR] Key '" + customKey + "' is invalid!");
                    System.exit(-1);
                }
            }

        } catch (CardException ex) {
            System.out.println("[ERROR] Hmm...exception occured when connecting to the card.");
            System.exit(-1);
        }
    }

    private static void crackKeys(ICard card, byte[] key) throws CardException {
        assert terminal != null : "[ASSERT] Could not find any terminal instance!";
        assert channel  != null : "[ASSERT] Channel is null!";

        byte[] auth_apdu = {(byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0F, (byte) 0xD4, (byte) 0x40,
            (byte) 0x01, //Card #
            (byte) 0x60, //KEY A
            (byte) 0x00, //Block # index:09
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, //Block's key index:10
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF //UID index:16
        };


        byte[] uid = card.getUID();

        System.arraycopy(uid, 0, auth_apdu, 16, uid.length);
        System.arraycopy(key, 0, auth_apdu, 10, key.length);

        //Set KEY B authentication
        if (keyBAuthentication) {
            auth_apdu[8] = (byte) 0x61;
        }

        int loopCount = 0;
        switch (card.getTagType()) {
            case MIFARE_1K:
                loopCount = 16;
                break;
            case MIFARE_4K:
                loopCount = 40;
                break;
            default:
                loopCount = 0;
        }

        System.out.print(" [");

        if (blocks == null) {
            for (int i = 0; i < loopCount; i++) {
                auth_apdu[9] = (byte) (i*4+3);
                sendAuthApdu(auth_apdu);

                byte[] baReadUID = new byte[]{(byte)0xFF, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x04, (byte)0xD4, (byte)0x4A, (byte)0x01, (byte)0x00};
                CommandAPDU c = new CommandAPDU(baReadUID);
                channel.transmit(c);
            }
        } else {
            String[] blocksArray = blocks.split(",");
            for (String blockNumber : blocksArray) {
                try {
                    int no = Integer.parseInt(blockNumber);
                    if (no < loopCount*4) {
                        auth_apdu[9] = (byte) (no&0xff);
                        //System.out.println(Conversions.byte2String(auth_apdu));
                        sendAuthApdu(auth_apdu);

                        byte[] baReadUID = new byte[]{(byte)0xFF, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x04, (byte)0xD4, (byte)0x4A, (byte)0x01, (byte)0x00};
                        CommandAPDU c = new CommandAPDU(baReadUID);
                        channel.transmit(c);
                    }
                } catch (NumberFormatException nfex) {
                    System.out.println("\n[ERROR] Naughty user...naughty! '" + blockNumber + "' is not a valid number!");
                    System.exit(-1);
                }
            }
        }

        System.out.print("]");
    }

    private static void sendAuthApdu(byte[] auth_apdu) throws CardException {
        CommandAPDU cmd = new CommandAPDU(auth_apdu);
        ResponseAPDU transmit = channel.transmit(cmd);

        byte[] resp = transmit.getBytes();

        if (resp[2] == (byte) 20) { //Error code # 14 - Authentication failed
            System.out.print("x");
        } else if (resp[2] == (byte) 00){
            System.out.print("o");
        } else {
            System.out.print("?");
        }
    }

    private static int cmdLineSearch(String[] array, String key) {
        int result = -1;

        int index = 0;
        for (String element : array) {
            if (element.equals(key)) {
                result = index;
                break;
            }
            index++;
        }

        return result;
    }
}
