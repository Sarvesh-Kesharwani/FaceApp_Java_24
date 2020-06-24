package com.example.faceapp_java_24;

import java.lang.Integer;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    //////////////////////////////////////////////////
    public String HOST = "192.168.43.205";
    public int Port = 1998;
    public String message;
    public String name;
    public Drawable photo;
    public int SELECT_PHOTO = 1;
    public Uri uri;
    public ImageView photoImage;
    public Bitmap photoBitmap;
    public String PhotoPath;
    //////////////////////////////////////////////////
    private AppBarConfiguration mAppBarConfiguration;
    public FileOutputStream ImageStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

///////////////////////////////////////////////
        setContentView(R.layout.fragment_home);
        final EditText nameText = findViewById(R.id.nameText);
        final Button sendButton = findViewById(R.id.sendButton);
        photoImage = findViewById(R.id.photoImage);
        sendButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                send sendcode = new send();
                name = nameText.getText().toString();
                sendcode.execute();
            }
        });

        final Button uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        uploadPhotoButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent (Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,SELECT_PHOTO);
            }
        });
///////////////////////////////////////////////

    }
///////////////////////////////////////////////


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data.getData() != null){
            uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                photoImage.setImageBitmap(bitmap);
                photoBitmap = bitmap;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ////////////////////////////////
            try (FileOutputStream fos = new FileOutputStream("")) {
                photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                ImageStream = fos;
            } catch (IOException e) {
                e.printStackTrace();
            }
            ////////////////////////////////////
        }
    }

    ///////////////////////////////////////////////
//////////////////////////////////////////////////////////////////
    class send extends AsyncTask<Void, Void, Void> {
        Socket s23;
        PrintWriter pw23;
        Socket s1;
        PrintWriter pw1;
        Socket s2;

        @Override
        protected Void doInBackground(Void... params) {
            //send name
            try {
                sendName();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //send photo
            sendFile();
            return null;
        }

        void sendName() throws IOException {
            //prepration
            try {
                s23 = new Socket(HOST, Port);
                pw23 = new PrintWriter(s23.getOutputStream());
            } catch (IOException e) {
                System.out.println("Fail");
                e.printStackTrace();
            }
            //preparing to send name_length
            byte[] nameBytes = name.getBytes();
            int nameBytesLength = nameBytes.length;//no of charaters in the name
            String nameBytesLengthString = Integer.toString(nameBytesLength);

            //send name_length

                if (nameBytesLength < 100) {
                    if (nameBytesLength <= 9)
                        pw23.write('0' + nameBytesLengthString);
                    else
                        pw23.write(nameBytesLengthString);
                }
                pw23.write(name);
                pw23.flush();
                pw23.close();
                s23.close();
        }


        byte [] EncodeToUTF8(String string)
        {
            //encoding delimeter string to utf-8 encoding
            ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(string);
            byte [] buff = new byte [byteBuffer.remaining()];
            byteBuffer.get(buff, 0, buff.length);
            return buff;
        }
        void sendFile()
        {
            try {
                s1 = new Socket(HOST, Port);
                pw1 = new PrintWriter(s1.getOutputStream());
            } catch (IOException e) {
                System.out.println("Fail");
                e.printStackTrace();
            }
            try
            {
                //preparing bytearray of photo
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100,stream);
                byte[] byteArray = stream.toByteArray();
                InputStream inn = new ByteArrayInputStream(byteArray);

                //sending bytearray_length or image_length
                if(pw1 != null)
                {
                    pw1.write(String.valueOf(byteArray.length)+'$');
                    Log.d("finally", String.valueOf(byteArray.length));
                    pw1.flush();
                    pw1.close();
                    s1.close();
                }

                //making 3rd connection with pyjnius for sending photo
                try {
                    s2 = new Socket(HOST, Port);
                } catch (IOException e) {
                    System.out.println("Fail");
                    e.printStackTrace();
                }
                if(s2 != null)
                {
                    DataOutputStream dos = new DataOutputStream(s2.getOutputStream());

                    //sending photo delimeter
                    dos.write(EncodeToUTF8("?start\n"), 0, "?start\n".length());

                   //send photo size with new way
                    String strSize = (byteArray.length) + "\n";
                    dos.write(EncodeToUTF8(strSize));

                   /* int len = 0 ;
                    int bytesRead = 0;
                    byte [] buffer = new byte [1024];
                    while (len<byteArray.length)
                    {
                        bytesRead = inn.read(buffer, 0, Math.min(buffer.length, byteArray.length-len));
                        len = len + bytesRead;
                        dos.write(buffer, 0, bytesRead);
                    }*/
                    dos.write(byteArray, 0,byteArray.length);
                    dos.write(EncodeToUTF8("\n"));
                    Log.d("photo","photo was wrote in dos");

                    dos.flush();
                    stream.close();
                    inn.close();
                    dos.close();
                    s2.close();
                }

            }
            catch (IOException ioe)
            {
                Log.d("Exception Caught", ioe.getMessage());
            }

        }
          /*  void sendPhotoEncoding () throws UnknownHostException, IOException {
                try {
                    s1 = new Socket(HOST, Port);
                    pw1 = new PrintWriter(s1.getOutputStream());
                } catch (UnknownHostException e) {
                    System.out.println("Fail");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Fail");
                    e.printStackTrace();
                }
                /*
                System.out.println("Executing python3 script:");
                Process p = Runtime.getRuntime().exec("python3"+);
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String ret = in.readLine();
                Log.d("tyyo","value is :"+ret);
                s.close();
            }*/

        public void receiveFileFromServer () throws UnknownHostException, IOException {
            Socket sock = new Socket("192.168.1.10", 5555);
            byte[] mybytearray = new byte[1024];
            InputStream is = sock.getInputStream();
            FileOutputStream fos = new FileOutputStream("/home/files/file.jpeg");
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            int bytesRead = is.read(mybytearray, 0, mybytearray.length);
            bos.write(mybytearray, 0, bytesRead);
            bos.close();
            sock.close();
        }
    }
/////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}