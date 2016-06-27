package su.swage.recovery.utils;

import android.util.Base64;

public class CompressString {
    public static String encodeString(String str, String Key) {
        return new String(Base64.encode(str.getBytes(), Base64.DEFAULT));
    }

    public static String decodeString(String str, String Key) {
        return new String(Base64.decode(str, Base64.DEFAULT));
    }
    /*public static String encodeString(String str, String Key) {
        try {
            byte[] KeyData = Key.getBytes();
            SecretKeySpec key = new SecretKeySpec(KeyData, "Blowfish");

            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(str.getBytes());

            return bytesToHex(encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decodeString(String str, String Key){
        try {
            byte[] KeyData = Key.getBytes();
            SecretKeySpec key = new SecretKeySpec(KeyData, "Blowfish");

            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(hexToBytes(str));

            return new String(decrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }*/

    public static byte[] hexToBytes(String str) {
        if (str == null) {
            return null;
        } else if (str.length() < 2) {
            return null;
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
            }
            return buffer;
        }

    }

    public static String bytesToHex(byte[] data) {
        if (data == null) {
            return null;
        } else {
            int len = data.length;
            String str = "";
            for (byte aData : data) {
                if ((aData & 0xFF) < 16)
                    str = str + "0" + Integer.toHexString(aData & 0xFF);
                else
                    str = str + Integer.toHexString(aData & 0xFF);
            }
            return str.toUpperCase();
        }
    }
}