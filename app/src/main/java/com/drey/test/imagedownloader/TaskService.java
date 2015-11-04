package com.drey.test.imagedownloader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;


/**
 * Created by drey on 01.11.2015.
 */
public class TaskService extends Service {

    static final int MSG_START_TASK = 1;
    static final int MSG_UPATE_UI = 2;
    static final int MSG_SET_CALLBACK = 3;
    static final int MSG_STOP_ALL=4;

    private boolean _isStopped=false;
    private HashMap<Long, Task> _tasks = new HashMap<Long, Task>();
    private Messenger _msgr;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MSG_START_TASK:
                    if (msg.obj instanceof Task) {
                        Task t = (Task) msg.obj;
                        final Long id = t.getCreated();
                        _tasks.put(t.getCreated(), t);

                        Thread th = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                downloadImage(_tasks.get(id));
                            }
                        });

                        th.start();
                    }
                    break;
                case MSG_SET_CALLBACK:
                    if (msg.obj instanceof Messenger) {
                        _msgr = (Messenger) msg.obj;
                    }
                    break;
                case MSG_STOP_ALL:
                    _isStopped = true;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger _messenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent) {
        return _messenger.getBinder();
    }

    public void downloadImage(Task task) {
        int width = 320;
        int height = 480;
        int previewWidth = 100;
        int previewHeight = 100;
        int count;
        try {
            URL url = new URL(task.getUrl());
            URLConnection conection = url.openConnection();
            conection.connect();

            int fileLength = conection.getContentLength();
            InputStream input = new BufferedInputStream(url.openStream(), 8096);
            String dir = TaskService.this.getApplicationContext().getFilesDir().getPath();
            String file = URLUtil.guessFileName(url.toString(), null, null);
            String fName = dir + file;
            OutputStream output = new FileOutputStream(fName);

            byte data[] = new byte[1024];

            long total = 0;

            task.setState(Task.State.Downloading);
            _msgr.send(Message.obtain(null, MSG_UPATE_UI));

            while ((count = input.read(data)) != -1) {
                if (_isStopped){
                    output.close();
                    input.close();
                    return;
                }
                total += count;
                task.setProgress((int) ((total * 100) / fileLength));
                _msgr.send(Message.obtain(null, MSG_UPATE_UI));
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();

            task.setState(Task.State.Processing);
            _msgr.send(Message.obtain(null, MSG_UPATE_UI));

            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fName, opt);
            opt.inSampleSize = Util.calculateInSampleSize(opt, width, height);
            opt.inJustDecodeBounds = false;

            Bitmap b = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(fName, opt), width, height, false);
            task.setImg(Bitmap.createScaledBitmap(b, previewWidth, previewHeight, false));

            File f = new File(fName);
            f.delete();

            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                File  externalDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "TestPictures");
                if (externalDir.exists() || externalDir.mkdirs()){
                    String extFile = externalDir.getPath() + "/"+ file;
                    FileOutputStream out =  new FileOutputStream(extFile);
                    b.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();
                    Util.addImageToGallery(extFile, getApplicationContext());
                }
            }


            task.setState(Task.State.Complete);
            _msgr.send(Message.obtain(null, MSG_UPATE_UI));
        } catch (Exception e) {
            task.setState(Task.State.Error);
            task.setMsg(e.getMessage());
            try {
                _msgr.send(Message.obtain(null, MSG_UPATE_UI));
            } catch (RemoteException e1) {
            }
        }
    }


}