package com.example.faceapp_java_24;

import java.io.OutputStream;
import java.lang.Integer;

import android.content.ContentProvider;
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
import android.widget.Toast;

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
import java.util.Arrays;

import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static javax.net.ssl.SSLEngineResult.Status.OK;

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
                if(photoBitmap != null)
                {
                    send sendcode = new send();
                    name = nameText.getText().toString();
                    sendcode.execute();
                }
                else
                    displayToast("Select Photo!");
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
                if(bitmap != null)
                {
                    photoImage.setImageBitmap(bitmap);
                    photoBitmap = bitmap;
                }
                else
                {
                    //int id = getResources().getIdentifier("com.example.faceapp_java_24/"+StringGenerated,null,null);
                    displayToast("Select Photo!");
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayToast(String ToastMessage)
    {
        Toast.makeText(MainActivity.this,ToastMessage,Toast.LENGTH_SHORT).show();
    }

    ///////////////////////////////////////////////
//////////////////////////////////////////////////////////////////
    class send extends AsyncTask<Void, Void, Void> {
        Socket s23;
        PrintWriter pw23;
        Socket s1;
        PrintWriter pw1;
        Socket s2;
        Socket s4;
        PrintWriter pw4;

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
            recieveFile();
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

                    /*//sending photo delimeter
                    dos.write(EncodeToUTF8("?start\n"), 0, "?start\n".length());

                   //send photo size with new way
                    String strSize = (byteArray.length) + "\n";
                    dos.write(EncodeToUTF8(strSize));*/

                   /* int len = 0 ;
                    int bytesRead = 0;
                    byte [] buffer = new byte [1024];
                    while (len<byteArray.length)
                    {
                        bytesRead = inn.read(buffer, 0, Math.min(buffer.length, byteArray.length-len));
                        len = len + bytesRead;
                        dos.write(buffer, 0, bytesRead);
                    }*/
                    //dos.write(EncodeToUTF8("$#\n"));
                    dos.write(byteArray, 0,byteArray.length);
                    //Log.d("image", Arrays.toString(byteArray));
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

        void recieveFile()
        {
            try
            {
                s4 = new Socket(HOST, Port);
            } catch (IOException e)
            {
                System.out.println("Fail");
                e.printStackTrace();
            }

            try {
                InputStream sin = s4.getInputStream();
                DataInputStream dis = new DataInputStream(sin);
                BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(s4.getInputStream()));

                OutputStream sout = s4.getOutputStream();

                Boolean mRun = true;
                if(s4 != null)
                {
                    while (mRun)
                    {
                        String mServerMessage = mBufferIn.readLine();
                        if (mServerMessage != null)
                        {
                            //receive name
                            if (mServerMessage.equals("?name"))
                            {
                                String name = String.valueOf(mBufferIn.readLine());
                                Log.d("recna", "Name is: " + name);
                            }
                        }
                        //Check if data is image and receive image
                        String mServerMessage1 = mBufferIn.readLine();
                        if (mServerMessage1.equals("?start")) {
                            // Get length of image byte array
                            int size = Integer.parseInt(mBufferIn.readLine());
                            Log.d("recna", "ImageSize is: " + size);

                            // Create buffers
                            byte[] msg_buff = new byte[1024];
                            byte[] img_buff = new byte[size];
                            int img_offset = 0;

                            while (true) {
                                int bytes_read = dis.read(msg_buff, 0, msg_buff.length);
                                if (bytes_read == -1) {
                                    break;
                                }
                                //copy bytes into img_buff
                                System.arraycopy(msg_buff, 0, img_buff, img_offset, bytes_read);
                                img_offset += bytes_read;
                                if (img_offset >= size) {
                                    break;
                                }
                            }
                            //save image to app's storage
                           /* ContextWrapper cw = new ContextWrapper(getApplicationContext());
                            String imageDir = "imageDir";
                            File directory = cw.getDir(imageDir, Context.MODE_PRIVATE);
                            String fileName = name+".png";
                            File mypath = new File(directory,fileName);*/

                            //create file_storage path
                            File myDir = new File(getFilesDir(),"FaceApp"+File.separator+"Images");
                            if(!myDir.exists())
                            {
                                myDir.mkdirs();
                            }

                            //save images
                            String fileName = name+".png";
                            File imageFile = new File(myDir, fileName);

                            Bitmap bitmap = BitmapFactory.decodeByteArray(img_buff, 0, img_buff.length);
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(imageFile);
                                //Use compress method on Bitmap object to write image to OutputStream
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                fos.close();

                                //Send OK byte[]
                                byte[] ok = new byte[]{0x4F, 0x4B};
                                sout.write(ok);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        mRun = false;
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
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