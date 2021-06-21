package com.ns.imagecompressorapp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.List;

import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity {
public static final int RESULT_IMAGE= 1;
    ImageView imgOriginal,imgCompressed;
    TextView txtOriginal,textCompressed,txtQuality;
    EditText textHeight,textWidth;
    SeekBar seekBar;
    Button btnPick,btnCompressed;
    File originalImage, compressImage;
    private static String filepath;
    File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/MyCompressor");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         imgOriginal=findViewById(R.id.imgOriginal);
         imgCompressed=findViewById(R.id.imgComp);
         txtOriginal=findViewById(R.id.textQuality);
         textCompressed = findViewById(R.id.textComp);
         textHeight = findViewById(R.id.editTextHeight);
         textWidth= findViewById(R.id.editTextWidth);
         btnPick=findViewById(R.id.btnPick);
         btnCompressed=findViewById(R.id.btnComp);
         seekBar=findViewById(R.id.seekQuality);
         filepath = path.getAbsolutePath();

         if(!path.exists()){
             path.mkdir();

         }

         seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 txtQuality.setText("Quality: " + progress);
                 seekBar.setMax(100);

             }

             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {

             }

             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {

             }
         });
            askPermission();

         btnPick.setOnClickListener(new View.OnClickListener() {
             @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
             @Override
             public void onClick(View v) {
                 openGallery();

             }
         });
         btnCompressed.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                int quality = seekBar.getProgress();
                int width = Integer.parseInt(textWidth.getText().toString());
                int height = Integer.parseInt(textHeight.getText().toString());
                 try {
                     compressImage = new Compressor(MainActivity.this)
                             .setMaxWidth(width)
                             .setMaxHeight(height)
                             .setQuality(quality)
                             .setCompressFormat(Bitmap.CompressFormat.JPEG)
                             .setDestinationDirectoryPath(filepath)
                             .compressToFile(originalImage);
                     File finalFile = new File(filepath,originalImage.getName());
                     Bitmap finalBitmap = BitmapFactory.decodeFile(finalFile.getAbsolutePath());
                     imgCompressed.setImageBitmap(finalBitmap);
                     textCompressed.setText("Size: " + Formatter.formatShortFileSize(MainActivity.this,finalFile.length()));
                     Toast.makeText(MainActivity.this, "Image Compressed & Saved", Toast.LENGTH_SHORT).show();
                 } catch (IOException e) {
                     e.printStackTrace();
                     Toast.makeText(MainActivity.this, "Error while Compressing!", Toast.LENGTH_SHORT).show();
                 }
             }
         });
    }

   @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void openGallery() {
        Intent gallery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, RESULT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            btnCompressed.setVisibility(View.VISIBLE);
            final Uri imageUri=data.getData();
            try {
                final InputStream imageStream=getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imgOriginal.setImageBitmap(selectedImage);
                originalImage = new File(imageUri.getPath().replace("raw/",""));
                txtOriginal.setText("Size: " + Formatter.formatShortFileSize(this,originalImage.length()));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void askPermission() {
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                      permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

}
