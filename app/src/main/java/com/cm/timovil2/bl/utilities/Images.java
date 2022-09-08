package com.cm.timovil2.bl.utilities;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 24/07/18.
 */

public class Images {

    private static final String JPG = ".jpg";

    private static Bitmap resizeBitmap(String photoPath, int targetW, int targetH) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true; //Deprecated API 21

        return BitmapFactory.decodeFile(photoPath, bmOptions);
    }

    //SCALE IMAGES RESOLUTION AND SIZE
    private static Bitmap defaultScaledBitmap(Bitmap bm) throws Exception{
        //Convert your photo to a bitmap
        Bitmap photoBm = bm;
        //get its orginal dimensions
        int bmOriginalWidth = photoBm.getWidth();
        int bmOriginalHeight = photoBm.getHeight();
        double originalWidthToHeightRatio =  1.0 * bmOriginalWidth / bmOriginalHeight;
        double originalHeightToWidthRatio =  1.0 * bmOriginalHeight / bmOriginalWidth;
        //choose a maximum height
        int maxHeight = 500;
        //choose a max width
        int maxWidth = 500;
        //call the method to get the scaled bitmap
        photoBm = getScaledBitmap(photoBm,
                bmOriginalWidth,
                bmOriginalHeight,
                originalWidthToHeightRatio,
                originalHeightToWidthRatio,
                maxHeight, maxWidth);

        return photoBm;
    }

    /**
     *
     * @param bitmap Original image bitmap
     * @param originalPath path of original image file
     * @return Resized bitmap 600 X 400 | 400 X 600
     */
    private static Bitmap getResizedBitmap(Bitmap bitmap, String originalPath){

        int bmOriginalWidth = bitmap.getWidth();
        int bmOriginalHeight = bitmap.getHeight();

        if(bmOriginalWidth > bmOriginalHeight) {
            bitmap = resizeBitmap(originalPath, 600, 400);
        } else if (bmOriginalHeight > bmOriginalWidth){
            bitmap = resizeBitmap(originalPath, 400, 600);
        }

        return bitmap;
    }

    private static Bitmap getScaledBitmap(Bitmap bm, int bmOriginalWidth,
                                         int bmOriginalHeight,
                                         double originalWidthToHeightRatio,
                                         double originalHeightToWidthRatio,
                                         int maxHeight, int maxWidth) {

        if(bmOriginalWidth > maxWidth || bmOriginalHeight > maxHeight) {

            if(bmOriginalWidth > bmOriginalHeight) {
                bm = scaleDeminsFromWidth(bm, maxWidth, bmOriginalWidth, originalHeightToWidthRatio);
            } else if (bmOriginalHeight > bmOriginalWidth){
                bm = scaleDeminsFromHeight(bm, maxHeight, bmOriginalHeight, originalWidthToHeightRatio);
            }

        }
        return bm;
    }

    private static Bitmap scaleDeminsFromHeight(Bitmap bm, int maxHeight, int bmOriginalHeight,
                                                double originalWidthToHeightRatio) {

        int newHeight = (int) Math.max(maxHeight, bmOriginalHeight * .55);
        int newWidth = (int) (newHeight * originalWidthToHeightRatio);
        bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
        return bm;
    }

    private static Bitmap scaleDeminsFromWidth(Bitmap bm, int maxWidth, int bmOriginalWidth,
                                               double originalHeightToWidthRatio) {
        //scale the width
        int newWidth = (int) Math.max(maxWidth, bmOriginalWidth * .75);
        int newHeight = (int) (newWidth * originalHeightToWidthRatio);
        bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
        return bm;
    }

    private static Bitmap bitmapFromFilePath(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(path, options);
    }

    public static void saveImageFile(Bitmap photoBm) throws Exception{

        //create a byte array output stream to hold the photo's bytes
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        //compress the photo's bytes into the byte array output stream
        photoBm.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        //construct a File object to save the scaled file to
        File f = new File(Environment.getExternalStorageDirectory()
                + File.separator + createImageName() + JPG);
        //create the file
        if(f.createNewFile()){
            //create an FileOutputStream on the created file
            FileOutputStream fo = new FileOutputStream(f);
            //write the photo's bytes to the file
            fo.write(bytes.toByteArray());

            //finish by closing the FileOutputStream
            fo.close();
        }
    }

    public static File createTemporaryFile(Context context) throws Exception {

        String imageFileName = createImageName();
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                JPG,         /* suffix */
                storageDir      /* directory */
        );
    }

    private static String createImageName(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        return "JPEG_" + timeStamp + "_";
    }

    public String getRealPathFromURI(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    /**
     * Gets the last image path from the media store
     * @return path of the last taken image
     */
    private String getLastImagePath(Activity context) {

        final String[] imageColumns = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
        final String imageOrderBy = MediaStore.Images.Media._ID + " DESC";

        Cursor imageCursor = context.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageColumns,
                null, null, imageOrderBy);

        if (imageCursor.moveToFirst()) {
            int id = imageCursor.getInt(imageCursor
                    .getColumnIndex(MediaStore.Images.Media._ID));
            String fullPath = imageCursor.getString(imageCursor
                    .getColumnIndex(MediaStore.Images.Media.DATA));
            Log.d(getClass().getSimpleName(), "getLastImageId::id " + id);
            Log.d(getClass().getSimpleName(), "getLastImageId::path "
                    + fullPath);
            //imageCursor.close();
            return fullPath;
        } else {
            return null;
        }
    }

    /**
     *
     * @param path Image File path
     * @param quality Hint to the compressor, 0-100. 0 meaning compress for small size, 100 meaning compress for max quality
     * @return Base 64  String
     */
    public static String imageFileToBase64String(String path, int quality) throws Exception{
        Bitmap bm = bitmapFromFilePath(path);
        bm = getResizedBitmap(bm, path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, quality , baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }
}
