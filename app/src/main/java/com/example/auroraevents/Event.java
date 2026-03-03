package com.example.auroraevents;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class Event {

    private String  qrCodeData;
    private Bitmap qR;

    public Event(String qrCodeData){
        this.qrCodeData = qrCodeData;
    }


    /**
     * takes a string of data and converts to a bitmap QR code
     * The string data is defined in the constructor and using this produces a bitmap
     * that returns the value specified inside of the variable
     * @author Sean Ross
     */
    private void generateQrCode(){
        MultiFormatWriter writer = new MultiFormatWriter(); //bitmap writer
        try{
            int width = 400; //these values change the width and height of the qr code
            int height = 400;

            //convert data to bit matrix
            BitMatrix matrix = writer.encode(this.qrCodeData, BarcodeFormat.QR_CODE, width, height);

            //convert matrix to bitmap, can be used in image view
            BarcodeEncoder encoder = new BarcodeEncoder();
            qR = encoder.createBitmap(matrix);
        }
        catch (WriterException e){
            Log.e("EVENT","Error encoding QR code", e);
        }

    }

    private Bitmap getQrCode(){
        return this.qR;
    }
}
