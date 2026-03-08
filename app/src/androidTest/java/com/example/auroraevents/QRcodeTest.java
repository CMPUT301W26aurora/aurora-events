package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.graphics.Bitmap;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import org.junit.Test;

public class QRcodeTest {

    @Test
    public void bitmapTest(){
         Event event = new Event(); //was previous new Event("hello") in the argument, this was removed as it was causing issues with test cases, feel free to add it back in
         event.generateQrCode();

         Bitmap test = event.getQrCode();
         assertNotNull(test);
         assertEquals(400, test.getWidth());
         assertEquals(400, test.getHeight());
    }

}
