package com.example.detectorplagas;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.detectorplagas.ml.Lastv1;
import com.example.detectorplagas.ml.PlantDiseaseModel;
import com.example.detectorplagas.ml.PlantVFinal;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {
    TextView result, demoTxt, classified, clickHere;
    ImageView imageView, arrowImage;
    Button picture;

    int imageSize = 224; // default image  size

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);
        picture = findViewById(R.id.button);

        demoTxt = findViewById(R.id.demoText);
        clickHere = findViewById(R.id.click_here);
        arrowImage = findViewById(R.id.demoArrow);
        classified = findViewById(R.id.classified);

        demoTxt.setVisibility(View.VISIBLE);
        clickHere.setVisibility(View.GONE);
        arrowImage.setVisibility(View.VISIBLE);
        classified.setVisibility(View.GONE);
        result.setVisibility(View.GONE);

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Activar camara (con permiso)
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent,1);
                }else {
                    //solicitar el permiso de la camara
                    requestPermissions(new String[]{Manifest.permission.CAMERA},100);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 1 && resultCode == RESULT_OK){
            Bitmap image = (Bitmap) data.getExtras().get("data");
            int dimension = Math.min(image.getWidth(),image.getHeight());
            image = ThumbnailUtils.extractThumbnail(image,dimension,dimension);
            imageView.setImageBitmap(image);

            demoTxt.setVisibility(View.GONE);
            clickHere.setVisibility(View.VISIBLE);
            arrowImage.setVisibility(View.GONE);
            classified.setVisibility(View.VISIBLE);
            result.setVisibility(View.VISIBLE);

            image = Bitmap.createScaledBitmap(image,imageSize,imageSize,false);
            classifyImage(image);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void classifyImage(Bitmap image) {
        try {
            PlantVFinal model = PlantVFinal.newInstance(getApplicationContext());
//            Lastv1 model = Lastv1.newInstance(getApplicationContext());
//            PlantDiseaseModel model = PlantDiseaseModel.newInstance(getApplicationContext());
            //
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1,224,224,3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            //
            int[] intValue = new int[imageSize*imageSize];
            image.getPixels(intValue,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());

            //iteracion en los pixeles
            int pixel = 0;
            for (int i = 0; i< imageSize; i++){
                for(int j = 0; j<imageSize; j++){
                    int val = intValue[pixel++]; //RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val  & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            //correr la interfaz del modelo y obtener resultado
            PlantVFinal.Outputs outputs = model.process(inputFeature0);
//            Lastv1.Outputs outputs = model.process(inputFeature0);
//            PlantDiseaseModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidence = outputFeature0.getFloatArray();


            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidence.length; i++){
                if(confidence[i] > maxConfidence){
                    maxConfidence = confidence[i];
                    maxPos = i;
                }
            }
            String[] classes = {"Manzana Hoja saludable","Roya del Manzano","Sarna en hoja del manzano",
                    "Maiz Hoja Saludable","Maiz Mancha foliar gris","Tomate Hoja Saludable","Tomate Mancha Foliar",
                    "Sin Hoja","Banano hoja saludable","Banano Sigatoka Negra","Manzana"};

//            String[] classes = {"manzana sarna","manzana podredumbre negra","manzana royuela del manzano",
//                    "manzana saludable","arándano saludable","cereza mildiu polvoriento",
//                    "cereza saludable","maíz manchas foliares de cercospora manchas foliares grises","maíz roya común",
//                    "maíz tizón foliar del norte","maíz saludable","uva podredumbre negra",
//                    "uva esca manchas negras","uva tizón foliar isariopsis manchas foliares", "uva saludable",
//                    "naranja Huanglongbing greening de los cítricos","melocotón manchas bacterianas","melocotón saludable",
//                    "pimiento manchas bacterianas","pimiento saludable","patata tizón temprano",
//                    "patata tizón tardío","patata saludable","frambuesa saludable",
//                    "soja saludable","calabaza mildiu polvoriento","fresa marchitez foliar",
//                    "fresa saludable","tomate manchas bacterianas","tomate tizón temprano",
//                    "tomate tizón tardío","tomate moho foliar","tomate septoria manchas foliares",
//                    "tomate ácaros araña de dos puntos ácaros araña manchados de dos puntos","tomate manchas foliares",
//                    "tomate rizado amarillo del tomate","tomate mosaico del tomate","tomate saludable"};


            for (int i = 0; i < confidence.length; i++) {
                Log.d("Clase " + i, classes[i] + ": " + confidence[i]);
            }

            result.setText(classes[maxPos]);
            result.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //buscar las enfermedades en internet
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/search?q="+result.getText())));
                }
            });
            model.close();

        }catch (IOException e){
            //TODO Handle the exception
        }
    }

    public void goToActividad2 (View view){
        Intent i = new Intent(this, actividad2.class);
        startActivity(i);
    }
}