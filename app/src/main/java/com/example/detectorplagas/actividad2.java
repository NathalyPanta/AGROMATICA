package com.example.detectorplagas;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

public class actividad2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad2);


        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.loadUrl("https://pabloyaguaribarra.github.io/AgromaticaProject/");

    }
}