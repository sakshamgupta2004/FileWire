package com.sugarsnooper.filetransfer;

import android.util.Log;

public class QRCodeFormatter {
    private QRCodeFormatter(){
    }
    public static String getSSIDfromQRCodeResult(String result) throws QRCodeFormatException {
        if (result.startsWith("WIFI:")) {
            try {
                return result.substring(result.indexOf("S:") + 2, result.indexOf(";", result.indexOf("S:")));
            }
            catch (Exception e) {
                throw new QRCodeFormatException();
            }
        }
        else {
            throw new QRCodeFormatException();
        }
    }

    public static String getPassfromQRCodeResult(String result) throws QRCodeFormatException {
        if (result.startsWith("WIFI:")) {
            try {
                return result.substring(result.indexOf("P:") + 2, result.indexOf(";", result.indexOf("P:")));
            }
            catch (Exception e) {
                throw new QRCodeFormatException();
            }
        }
        else {
            throw new QRCodeFormatException();
        }
    }

    public static String formatSSIDAndPass(String ssid, String pass) {
        return ("WIFI:") + ("S:" + ssid + ";") + ("P:" + pass + ";");
    }

    public static void continueIfItIsPcFormat(String result) throws Exception {
        boolean isPcFormat = true;
        Log.e("Checking Pc Format", "Start");
        if (!result.endsWith("\n")) {
            isPcFormat = false;
            Log.e("Checking Pc Format", "Does not end with \\n");
        }
        else {
            Log.e("Checking Pc Format", "Ends with \\n");
            if (!result.startsWith("http://")) {
                isPcFormat = false;
                Log.e("Checking Pc Format", "Does not Start with \"http://\"");
            }
            else {
                Log.e("Checking Pc Format", "Starts with \"http://\"");
                String[] addresses = result.split("\n");
                for (String address : addresses) {
                    if (!address.startsWith("http://")) {
                        isPcFormat = false;
                        break;
                    }
                    if (!address.endsWith("/")) {
                        isPcFormat = false;
                        break;
                    }
                    if (!(Integer.parseInt(address.split(":")[address.split(":").length - 1].substring(0, 4)) >= 1234 && Integer.parseInt(address.split(":")[address.split(":").length - 1].substring(0, 4)) <= 1300)) {
                        isPcFormat = false;
                        break;
                    }
                }
            }
        }
        if (!isPcFormat) {
            throw new QRCodeFormatException();
        }
    }

    public static class QRCodeFormatException extends Exception {
    }
}
