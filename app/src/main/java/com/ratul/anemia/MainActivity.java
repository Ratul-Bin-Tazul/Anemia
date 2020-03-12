package com.ratul.anemia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.demo);

        Bitmap bitmap = largeIcon;
        bitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, true);

        Mat mat = new Mat();

        //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);

        //THRESH_BINARY
        Utils.bitmapToMat(bitmap, mat);
        //Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(mat, mat, 5, 255, Imgproc.THRESH_BINARY);

        bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);

        mat.reshape(64);

        Utils.matToBitmap(mat, bitmap);


        int batchNum = 0;
        float[][][][] input = new float[1][64][64][1];
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                int pixel = bitmap.getPixel(x, y);
                // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                // model. For example, some models might require values to be normalized
                // to the range [0.0, 1.0] instead.
                input[batchNum][x][y][0] = ((float) pixel / 255.0f);
                Log.e("x", "" + input[batchNum][x][y][0]);
            }
        }

        //GREY SCALE
        //Utils.loadResource(MainActivity.this, d, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

        final FirebaseCustomRemoteModel remoteModel =
                new FirebaseCustomRemoteModel.Builder("anemia").build();

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Success.
                    }
                });

        FirebaseModelManager.getInstance().isModelDownloaded(remoteModel)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isDownloaded) {
                        FirebaseModelInterpreterOptions options;
                        if (isDownloaded) {
                            options = new FirebaseModelInterpreterOptions.Builder(remoteModel).build();
                            try {
                                FirebaseModelInterpreter interpreter = FirebaseModelInterpreter.getInstance(options);
                            } catch (FirebaseMLException e) {
                                e.printStackTrace();
                            }


                        } else {
                            //options = new FirebaseModelInterpreterOptions.Builder(localModel).build();
                        }
                        // ...
                    }
                });


//        int batchNum = 0;
//        float[][][][] input = new float[1][64][64][1];
//        for (int x = 0; x < 224; x++) {
//            for (int y = 0; y < 224; y++) {
//                int pixel = bitmap.getPixel(x, y);
//                // Normalize channel values to [-1.0, 1.0]. This requirement varies by
//                // model. For example, some models might require values to be normalized
//                // to the range [0.0, 1.0] instead.
//                input[batchNum][x][y][0] = (Color.red(pixel) - 127) / 128.0f;
//                input[batchNum][x][y][1] = (Color.green(pixel) - 127) / 128.0f;
//                input[batchNum][x][y][2] = (Color.blue(pixel) - 127) / 128.0f;
//            }
//        }
//    }

        FirebaseModelInputOutputOptions inputOutputOptions;

        {
            try {
                inputOutputOptions = new FirebaseModelInputOutputOptions.Builder()
                        .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 64, 64, 1})
                        .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 1})
                        .build();
            } catch (FirebaseMLException e) {
                e.printStackTrace();
            }
        }

    }
}
