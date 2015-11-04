package com.drey.test.imagedownloader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView _taskList;
    private TaskAdapter _adapter;
    private RecyclerView.LayoutManager _layoutManager;
    private AutoCompleteTextView _targetUrl;
    private String DATA_FRAG = "dataFragment";
    private EternalFragment _ef;
    private Messenger _service;
    private final Messenger _callback = new Messenger(new ServiceCallback());


    private String[] TEST_URLS;
    private boolean _isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Intent intent = new Intent(this, TaskService.class);
            startService(intent);
        }

        TEST_URLS = getApplicationContext().getResources().getStringArray(R.array.urls);

        _ef = (EternalFragment) getSupportFragmentManager().findFragmentByTag(DATA_FRAG);
        if (_ef == null) {
            _ef = new EternalFragment();
            getSupportFragmentManager().beginTransaction().add(_ef, DATA_FRAG).commit();
        }


        _taskList = (RecyclerView) findViewById(R.id.tasklist);
        _layoutManager = new LinearLayoutManager(this);
        _taskList.setLayoutManager(_layoutManager);

        _adapter = new TaskAdapter(_ef.getData());
        _taskList.setAdapter(_adapter);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.two_line_list_item, android.R.id.text1, TEST_URLS);
        _targetUrl = (AutoCompleteTextView)
                findViewById(R.id.target_url);
        _targetUrl.setAdapter(adapter);

        View download = findViewById(R.id.download);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String t = _targetUrl.getText().toString();
                if (!t.equals("")) {
                    Task tsk = new Task(t);
                    _adapter.addTask(tsk);
                    _targetUrl.setText("");
                    _targetUrl.clearFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    if (_service != null) {
                        Message msg = Message.obtain(null, TaskService.MSG_START_TASK, tsk);
                        msg.replyTo = _callback;
                        try {
                            _service.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        doBind();
    }

    @Override
    protected void onPause() {
        super.onPause();
        doUnbind();
    }


    private void doUnbind() {
        if (_isBound) {
            unbindService(_conn);
            _isBound = false;
        }
    }

    private void doBind() {
        if (!_isBound) {
            bindService(new Intent(this,
                    TaskService.class), _conn, Context.BIND_AUTO_CREATE);
            _isBound = true;
        }
    }

    private ServiceConnection _conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            _service = new Messenger(service);
            try {
                _service.send(Message.obtain(null, TaskService.MSG_SET_CALLBACK, _callback));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _service = null;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_stop:
                if (_isBound) {
                    if (_service != null) {
                        try {
                            _service.send(Message.obtain(null, TaskService.MSG_STOP_ALL));
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    doUnbind();
                    stopService(new Intent(getApplicationContext(), TaskService.class));
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    class ServiceCallback extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TaskService.MSG_UPATE_UI:
                    _adapter.notifyDataSetChanged();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
