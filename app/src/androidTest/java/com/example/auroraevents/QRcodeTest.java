package com.example.auroraevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.graphics.Bitmap;

import com.example.auroraevents.model.Event;

import org.junit.Test;

import java.util.Date;

public class QRcodeTest {

    @Test
    public void bitmapTest(){
        Event event = new Event(
                "test device",
                "QR code test",
                "event for QR code test",
                new Date(),
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
