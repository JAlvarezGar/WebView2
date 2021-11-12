package com.example.webview2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    InputStream is;
    OutputStream os;
    HttpURLConnection connection;
    WebView webView;
    FileInputStream fis = null;
    BufferedInputStream bis;

    int byte_entrada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // LOCALIZACION DEL index.html
        String folderPath = "file:///android_asset/";
        String fileName = "index.html";
        String file = folderPath + fileName;

        webView = findViewById(R.id.webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setSupportZoom(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setWebContentsDebuggingEnabled(true);
        }
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.setBackgroundColor(0x00000000); //<-- Color to transparent
        webView.setBackgroundResource(R.drawable.fondo);

        // Esta clase le permite escuchar llamadas de JavaScript
        // Usando WebChromeClient podemos manejar eventos JS.
        webView.setWebChromeClient(new MyWebChromeClient());

        // Se llama a WebViewClient cuando se representa el contenido de la página.
        // WebViewClient le permite escuchar los eventos de la página web, por ejemplo,
        // cuando comienza a cargarse o cuando finaliza la carga cuando se produce un error
        // relacionado con la carga de la página, el envío de formularios, los enlaces y otros eventos.
        webView.setWebViewClient(new MyWebViewClient());
        webView.setVerticalScrollBarEnabled(false);


        // carga la página web en el WebView
        webView.loadUrl(file);

        // crea la interfaz para comunicarse con la WebView
        // en este caso al pulsar el botón Di Hola!!!, saluda en un toast
        webView.addJavascriptInterface(new WebAppInterface(MainActivity.this), "Android");


    }

    // interfaz para la comunicación con el WebView
    public class WebAppInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /**
         * Show a toast from the web page
         */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
        }

        @JavascriptInterface
        public String getText() {
            return "Texto enviado desde la parte Android";
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @JavascriptInterface
        public void getUrl(final String url, final String destino) {

            Toast.makeText(mContext, url + "\n" + "destino: " + destino, Toast.LENGTH_SHORT).show();


            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {

                    downloadFile(url, destino);
                }
            });


        }

        @JavascriptInterface
        public void lecturaArchivo(final String archivo ){


            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        readFile(archivo);
                    } catch (FileNotFoundException e) {
                        Log.e("ERROR DE I/O", "fichero no encontrado");
                        //Log.e("Error de i/o","ruta:"+)
                    }
                }
            });
        }



    }

    private void readFile(String archivo) throws FileNotFoundException {

        // devuelve a la interfazDescarga.js una respuesta true
        webView.post(new Runnable() {
            @Override
            public void run() {
                String dirFile= getFilesDir()+"/"+archivo;
                String urlFile="file://"+dirFile;
                //webView.loadUrl("javascript:const img=document.createElement('img'); img.id='imagen'; img.src='"+urlFile+"'");
                webView.loadUrl("javascript:image(\""+dirFile+"\")");
//                try {
//                    fis = new FileInputStream(dirFile);
//                    boolean final_fichero= false;
//                    while (!final_fichero) {
//                         byte_entrada= fis.read();
//                        if(byte_entrada==-1) final_fichero=true;
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }finally {
//                    if(fis!=null){
//                        try {
//                            fis.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//                Bitmap imagen = BitmapFactory.decodeStream(fis);
//                Log.d("BITMAP:", imagen.toString());
//                webView.loadUrl("javascript:document.getElementById('imagen').innerHTML="+imagen);
//                Log.d("webview", "Informando de la descarga de " + dirFile);
            }
        });


    }

    private void downloadFile(String url, String destino) {
        File internalFile = getFilesDir();
        File guardar = new File(internalFile, destino);
        int longitud = (int) internalFile.length();


        try {
            URL u = new URL(url);
            Log.i("URL...", u.toString());
            connection = (HttpURLConnection) u.openConnection();
            int contentLength = connection.getContentLength();
            connection.connect();

            final String msg;
            if (connection.getResponseCode() == 200) {
                msg = "Conexión realizada: " + connection.getResponseMessage();
            } else {
                msg = "Conexión fallida: " + connection.getResponseMessage();
            }
            webView.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });


            is = connection.getInputStream();
            os = new FileOutputStream(guardar);

            byte[] buffer = new byte[contentLength];
            Log.i("GUARDAR....", guardar.toString() +
                    "\n" + "longitud fichero: " + longitud +
                    "\n" + "ContentLength: " + contentLength);
            int count;
            int total = 0;
            while ((count = is.read(buffer)) != -1) {

                total += count;

                if (contentLength > 0) // solo si se conoce el tamaño del fichero

                    os.write(buffer, 0, count);
            }

        } catch (FileNotFoundException e) {
            Log.e("FILE_ERROR", "FICHERO NO ENCONTRADO 404");
        } catch (IOException e) {
            Log.e("FILE_ERROR", "ERROR DE LECTURA/ESCRITURA 404");
        } finally {
            try {
                if (os != null)
                    os.close();
                if (is != null)
                    is.close();
                if (guardar.exists()) {
                    Log.i("FICHERO_ENCONTRADO", guardar + " existe en el directorio");

                    // devuelve a la interfazDescarga.js una respuesta true
                    webView.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("webview", "Informando de la descarga de " + url);
                            webView.loadUrl("javascript:Interfaz.onRespuesta('" + url + "', true)");
                        }
                    });

                }
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
    }

    // Se llama a WebViewClient cuando se representa el contenido de la página.
    // WebViewClient le permite escuchar los eventos de la página web, por ejemplo,
    // cuando comienza a cargarse o cuando finaliza la carga cuando se produce un error
    // relacionado con la carga de la página, el envío de formularios, los enlaces y otros eventos.
    private class MyWebViewClient extends WebViewClient {


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            findViewById(R.id.webView).setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    // Esta clase le permite escuchar llamadas de JavaScript
    // Usando WebChromeClient podemos manejar eventos JS.
    private class MyWebChromeClient extends WebChromeClient {


    }
    //        webView.postDelayed(new Runnable() {
//            public void run() {
//
//                webView.loadUrl("javascript:cambiarTexto();");
//            }
//        }, 5000);
//

}