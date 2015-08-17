package com.ducnd.myitem;

import android.graphics.Bitmap;
public class ItemListMessage {
    private String text;
    private Bitmap image;

    public ItemListMessage( String text ) {
        this.text = text;
        createImage();
    }
    public ItemListMessage( String text, Bitmap image ) {
        this.text = text;
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    private void createImage() {
        image = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
    }
}
