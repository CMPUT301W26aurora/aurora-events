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

    private String  eventId;
    private Bitmap qR;

    public Event(String eventId){
        this.eventId = eventId;
    }


    /**
     * takes a string of data and converts to a bitmap QR code
     * The string data is defined in the constructor and using this produces a bitmap
     * that returns the value specified inside of the variable
     * @author Sean Ross
     */
    public void generateQrCode(){
        MultiFormatWriter writer = new MultiFormatWriter(); //bitmap writer
        try{
            // ideas taken from Hilal Ahmed in medium at https://ihilalahmadd.medium.com/how-to-generate-qr-code-in-android-5a2a7edf11c

            int width = 400; //these values change the width and height of the qr code
            int height = 400;

            //convert data to bit matrix
            BitMatrix matrix = writer.encode(this.eventId, BarcodeFormat.QR_CODE, width, height);

            //convert matrix to bitmap, can be used in image view
            BarcodeEncoder encoder = new BarcodeEncoder();
            qR = encoder.createBitmap(matrix);
        }
        catch (WriterException e){
            Log.e("EVENT","Error encoding QR code", e);
        }

    }

    public Bitmap getQrCode(){
        return this.qR;
    }
}
