package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.graphics.Bitmap;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Date;

public class QRcodeTest {

    @Test
    public void bitmapTest(){
        Event event = new Event(
                "test device",
                "QR code test",
                "event for QR code test",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                "testing environment",
                0);
        event.setEventId("sample id for QR Code testing");
        event.generateQrCode();

         Bitmap test = event.getQrCode();
         assertNotNull(test);
         assertEquals(400, test.getWidth());
         assertEquals(400, test.getHeight());
    }

}
