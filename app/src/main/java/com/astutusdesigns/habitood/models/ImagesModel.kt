package com.astutusdesigns.habitood.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.datamodels.FSBusiness
import com.astutusdesigns.habitood.datamodels.FSVendor
import com.astutusdesigns.habitood.models.SharedPrefs.Companion.getSharedPreferenceLong
import com.astutusdesigns.habitood.models.SharedPrefs.Companion.setSharedPreferenceLong
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by TMiller on 1/30/2017.
 */
object ImagesModel {
    const val VENDOR_LOGO_REQ = 1999
    const val BUSINESS_LOGO_REQ = 2000
    const val CURRENT_USER_PHOTO_REQ = 2001
    const val OTHER_USER_PHOTO_REQ = 2002
    private const val TAG = "ImagesModel"
    private const val VENDOR_LOGO_DIR = "VendorLogo.png"
    private const val BUSINESS_LOGO_DIR = "CompanyLogo.png"
    private const val USER_PROFILE_PHOTO_DIR = "ProfilePhoto.jpg"
    private const val JPEG_IDENTIFIER = ".jpg"
    private const val BUSINESS_LOGO_CREATE_MILLI = "BusinessLogoCreateMillis"
    private const val VENDOR_LOGO_CREATE_MILLI = "VendorLogoCreateMillis"
    fun deleteUsersProfileImage(context: Context) {
        val profileImage = context.getFileStreamPath(USER_PROFILE_PHOTO_DIR)
        if (profileImage.exists()) {
            profileImage.delete()
            Log.v(TAG, "User profile photo deleted")
        }
    }

    fun cacheOtherUsersProfilePhoto(context: Context, userId: String?, bitmap: Bitmap) {
        try {
            val os = context.openFileOutput(
                String.format("%s%s", userId, JPEG_IDENTIFIER),
                Context.MODE_PRIVATE
            )
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os)
        } catch (fnfe: FileNotFoundException) {
            Log.e(TAG, fnfe.toString())
        }
    }
    /*
    public static Drawable getCachedProfilePhoto(final Context context, final User user ) {

        DataManager dataManager = DataManager.getInstance( context );
        UserAndImageUrl userAndImageUrl = new UserAndImageUrl();
        userAndImageUrl.setUserId( user.getKey() );
        userAndImageUrl.setImageUrl( user.getProfilePhotoUrl() );

        if( !dataManager.isThisProfilePhotoDownloaded( userAndImageUrl ) ) {
            try {
                File file = context.getFileStreamPath(String.format("%s%s", user.getKey(), JPEG_IDENTIFIER));
                file.delete();
                Log.v(tag, "Cached photo deleted for User: " + userAndImageUrl.getUserId());
            } catch( Exception ex ) {
                Log.e( tag, "exception thrown in getCachedProfilePhoto: " + ex.toString() );
            }
            return null;
        }

        Bitmap userProfilePhoto = null;
        FileInputStream is = null;
        File filePath = null;
        try {
            filePath = context.getFileStreamPath( String.format( "%s%s", user.getKey(), JPEG_IDENTIFIER ) );
            is = new FileInputStream( filePath );
            userProfilePhoto = BitmapFactory.decodeStream( is );
            Log.v( tag, "Cached photo was found. Size (in Bytes): " + userProfilePhoto.getByteCount() );
        } catch( FileNotFoundException fnfe ) {
            Log.v( tag, "No cached photo was found for User: " + user.getKey() );
        } catch( OutOfMemoryError oome ) {
            Log.e( tag, "Out of memory error detected while downloading and caching photos! Error: " + oome.getMessage() );
        }

        if( userProfilePhoto == null ) {
            return null;
        }

        Drawable profileDrawable = getRoundedBitmap( context, userProfilePhoto );
        return profileDrawable;
    }
*/
    /*
    public static Target getTarget(final Context context, final User user, final ImageView receivingImageView ) {
        Target target = new Target(){
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                receivingImageView.setImageDrawable( getRoundedBitmap( context, bitmap ) );
                receivingImageView.setColorFilter( null );
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        cacheOtherUsersProfilePhoto( context, user.getKey(), bitmap );
                        UserAndImageUrl uiu = new UserAndImageUrl();
                        uiu.setUserId( user.getKey() );
                        uiu.setImageUrl( user.getProfilePhotoUrl() );
                        uiu.setDownloaded( true );

                        DataManager.getInstance( context ).markPhotoUrlAsDownloaded( uiu );
                    }
                }).start();
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) { }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };
        return target;
    }
*/
    /**
     * This method will be used to save any of 3 different images to disk. 1- Vendor logo
     * 2-Business logo. 3-Current end User's photo.
     * @param imageRequest int Image Request defined in this class.
     * @param bitmap Bitmap to be saved.
     */
    fun saveImageToDisk(imageRequest: Int, bitmap: Bitmap) {
        val context = HabitoodApp.instance.applicationContext
        var os: FileOutputStream? = null
        try {
            when (imageRequest) {
                VENDOR_LOGO_REQ -> {
                    os = context.openFileOutput(VENDOR_LOGO_DIR, Context.MODE_PRIVATE)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                }

                BUSINESS_LOGO_REQ -> {
                    os = context.openFileOutput(BUSINESS_LOGO_DIR, Context.MODE_PRIVATE)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                }

                CURRENT_USER_PHOTO_REQ -> {
                    os = context.openFileOutput(USER_PROFILE_PHOTO_DIR, Context.MODE_PRIVATE)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 40, os)
                }
            }
            os?.close()
        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
        }
    }

    fun getImageFromDisk(imageRequest: Int): Bitmap? {
        val context = HabitoodApp.instance.applicationContext
        var bitmapImage: Bitmap? = null
        var `is`: FileInputStream? = null
        val filePath: File
        try {
            when (imageRequest) {
                VENDOR_LOGO_REQ -> {
                    filePath = context.getFileStreamPath(VENDOR_LOGO_DIR)
                    `is` = FileInputStream(filePath)
                    bitmapImage = BitmapFactory.decodeStream(`is`)
                    Log.v(TAG, "Vendor logo found and retrieved.")
                }

                BUSINESS_LOGO_REQ -> {
                    filePath = context.getFileStreamPath(BUSINESS_LOGO_DIR)
                    `is` = FileInputStream(filePath)
                    bitmapImage = BitmapFactory.decodeStream(`is`)
                    Log.v(TAG, "Company logo found and retrieved.")
                }

                CURRENT_USER_PHOTO_REQ -> {
                    filePath = context.getFileStreamPath(USER_PROFILE_PHOTO_DIR)
                    `is` = FileInputStream(filePath)
                    bitmapImage = BitmapFactory.decodeStream(`is`)
                    Log.v(TAG, "User profile found and retrieved.")
                }
            }
        } catch (fnfe: FileNotFoundException) {
            Log.e(TAG, fnfe.toString())
        } catch (oome: OutOfMemoryError) {
            Log.e(TAG, oome.toString())
        } finally {
            try {
                `is`?.close()
            } catch (ioe: IOException) {
                Log.e(TAG, "ioe exception thrown: $ioe")
            }
        }
        return bitmapImage
    }

    fun checkForNewBusinessLogo(bid: String) {
        Log.d(TAG, "Checking for new business logo.")

        // get the stored create time millis.
        val createMillis = getSharedPreferenceLong(HabitoodApp.instance, BUSINESS_LOGO_CREATE_MILLI)
        if (createMillis == -1000L) {
            Log.d(TAG, "No create time stored. Returning.")
            return
        }

        // get reference to business logo.
        val sRef: StorageReference = FirebaseStorage
            .getInstance()
            .getReferenceFromUrl(HabitoodApp.STORAGE_URL)
            .child("businesses/$bid/logo.png")

        // get the create time metadata in millis and store for comparison.
        sRef.metadata.addOnSuccessListener{ storageMetadata: StorageMetadata ->
            val createTime: Long = storageMetadata.creationTimeMillis
            if (createMillis != createTime) {
                Log.d(
                    TAG,
                    "Remote logo create date is different. Deleting local business logo to replace."
                )
                removeCompanyLogo()
            }
        }
    }

    fun checkForNewVendorLogo(vid: String) {
        Log.d(TAG, "Checking for new vendor logo.")

        // get the stored create time millis.
        val createMillis = getSharedPreferenceLong(HabitoodApp.instance, VENDOR_LOGO_CREATE_MILLI)
        if (createMillis == -1000L) {
            Log.d(TAG, "No create time stored. Returning.")
            return
        }

        // get reference to business logo.
        val sRef: StorageReference = FirebaseStorage
            .getInstance()
            .getReferenceFromUrl(HabitoodApp.STORAGE_URL)
            .child("vendors/$vid/logo.png")

        // get the create time metadata in millis and store for comparison.
        sRef.metadata.addOnSuccessListener { storageMetadata ->
            val createTime: Long = storageMetadata.creationTimeMillis
            if (createMillis != createTime) {
                Log.d(
                    TAG,
                    "Remote logo create date is different. Deleting local vendor logo to replace."
                )
                removeVendorLogo()
            }
        }
    }

    fun getBusinessLogo(b: FSBusiness, c: ImageDownloadCallback) {
        var bitmap: Bitmap? = null

        // get the logo from disk if possible.
        try {
            bitmap = getImageFromDisk(BUSINESS_LOGO_REQ)
        } catch (ex: Exception) {
            Log.e("ImagesModel", "Business photo not found.")
        }

        // return image if it is on disk.
        if (bitmap != null) {
            c.imageDownloaded(bitmap)
            return
        }

        // get reference to business logo.
        val sRef: StorageReference = FirebaseStorage
            .getInstance()
            .getReferenceFromUrl(HabitoodApp.STORAGE_URL)
            .child("businesses/${b.businessId}/logo.png")

        // get the create time metadata in millis and store for comparison.
        sRef.metadata.addOnSuccessListener { storageMetadata ->
            val createTime: Long = storageMetadata.creationTimeMillis
            setSharedPreferenceLong(HabitoodApp.instance, BUSINESS_LOGO_CREATE_MILLI, createTime)
        }

        // begin the download process.
        downloadImage(sRef, BUSINESS_LOGO_DIR, c)
    }

    fun getVendorLogo(v: FSVendor, c: ImageDownloadCallback) {
        var bitmap: Bitmap? = null
        try {
            bitmap = getImageFromDisk(VENDOR_LOGO_REQ)
        } catch (ex: Exception) {
            Log.e("ImagesModel", "Vendor photo not found.")
        }
        if (bitmap != null) {
            c.imageDownloaded(bitmap)
            return
        }
        val sRef: StorageReference = FirebaseStorage
            .getInstance()
            .getReferenceFromUrl(HabitoodApp.STORAGE_URL)
            .child("vendors/${v.id}/logo.png")

        // get the create time metadata in millis and store for comparison.
        sRef.metadata.addOnSuccessListener { storageMetadata ->
            val createTime: Long = storageMetadata.creationTimeMillis
            setSharedPreferenceLong(HabitoodApp.instance, VENDOR_LOGO_CREATE_MILLI, createTime)
        }
        downloadImage(sRef, VENDOR_LOGO_DIR, c)
    }

    private fun downloadImage(sRef: StorageReference, imageDir: String, c: ImageDownloadCallback?) {
        val imageFile = File(HabitoodApp.instance.applicationContext.filesDir, imageDir)
        sRef.getFile(imageFile)
            .addOnSuccessListener(OnSuccessListener<Any?> {
                if (c != null) {
                    val b = BitmapFactory.decodeFile(imageFile.path)
                    c.imageDownloaded(b)
                }
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.e(
                    "ImagesModel",
                    "An error occurred attempting to download an image file. See exception: $e"
                )
            })
    }

    fun removeCompanyLogo() {
        val filePath = HabitoodApp.instance.getFileStreamPath(BUSINESS_LOGO_DIR)
        filePath.delete()
        setSharedPreferenceLong(HabitoodApp.instance, BUSINESS_LOGO_CREATE_MILLI, -1000)
    }

    fun removeVendorLogo() {
        val filePath = HabitoodApp.instance.getFileStreamPath(VENDOR_LOGO_DIR)
        filePath.delete()
        setSharedPreferenceLong(HabitoodApp.instance, VENDOR_LOGO_CREATE_MILLI, -1000)
    }

    fun getRoundedBitmap(context: Context, bitmap: Bitmap?): Drawable {
        val rbd = RoundedBitmapDrawableFactory.create(context.resources, bitmap)
        rbd.isCircular = true
        return rbd
    }

    interface ImageDownloadCallback {
        fun imageDownloaded(bitmap: Bitmap?)
    }
}