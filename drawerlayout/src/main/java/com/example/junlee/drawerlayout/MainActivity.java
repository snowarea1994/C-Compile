package com.example.junlee.drawerlayout;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private EditText editText;
    private ListView lvLeftMenu;
    private String[] lvs;//文件名数组
    private ArrayAdapter arrayAdapter;
    private String fileName;
    private boolean isSaved = false;
    String uploadUrl = "http://snowarea1994.oicp.net/testlaravel/public/compile";

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                新建文件之前保存未关闭文件，以免丢失文件内容
                if (!isSaved) {
                    Snackbar.make(view, "文件已自动保存", Snackbar.LENGTH_SHORT).show();
                    saveFile(fileName);
                }
                final TableLayout addTable = (TableLayout) getLayoutInflater().inflate(R.layout.add, null);
                new AlertDialog.Builder(MainActivity.this).setIcon(R.drawable.ic_insert_drive_file_white_24dp).setTitle("新建一个文件").setView(addTable).setPositiveButton("添加", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        如果一个activity有多个vie获取时要加上view的名称
//                        获取用户输入的文件名，更新标题，更新编辑窗口，设置状态为未保存
                        EditText et = (EditText) addTable.findViewById(R.id.filename);
                        fileName = et.getText().toString() + ".c";
                        toolbar.setTitle(fileName);
                        editText.setText("");
                        isSaved = false;
//                        Log.i("file:", fileName);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
            }
        });

        //创建返回键，并实现打开关/闭监听
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close) {
            //            打开左侧菜单时更新文件列表，保存未保存的文件你
            @Override
            public void onDrawerOpened(View drawerView) {
                if (!isSaved) {
                    Snackbar.make(drawerView, "文件已自动保存", Snackbar.LENGTH_SHORT).show();
                    saveFile(fileName);
                }
                refresh();
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        refresh();

    }

    //刷新文件列表
    private void refresh() {
        lvs = fileList();
//        Log.i("fileList:",""+lvs.length);

        //设置菜单列表
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, lvs);
        lvLeftMenu.setAdapter(arrayAdapter);
        lvLeftMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerLayout.closeDrawers();
                saveFile(fileName);
                fileName = lvs[position];
                toolbar.setTitle(fileName);
                readFile(fileName);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.save:
                saveFile(fileName);
                break;
            case R.id.compil:
                compile(fileName);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void compile(String fileName) {
        saveFile(fileName);
        new Thread(networkTask).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //初始化操作
    private void init() {
        editText = (EditText) findViewById(R.id.edit_text);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        lvLeftMenu = (ListView) findViewById(R.id.file_list);
        lvs = fileList();
        toolbar.setTitle("readme");//设置Toolbar标题
        toolbar.setTitleTextColor(Color.parseColor("#ffffff")); //设置标题颜色
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        editText.setText("左上角为菜单列表，右上角第一个为保存按钮，第二个为编译按钮，右下角为新建文件");
    }

    //读文件
    private void readFile(String fileName) {
        try {
            FileInputStream fis = openFileInput(fileName);
            byte[] buff = new byte[1024];
            int hasRead = 0;
            StringBuffer sb = new StringBuffer("");
            while ((hasRead = fis.read(buff)) > 0) {
                sb.append(new String(buff, 0, hasRead));
            }
            fis.close();
            editText.setText(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //写文件
    private void saveFile(String fileName) {
        isSaved = true;
        FileOutputStream outputStream;
        try {
            FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
            PrintStream ps = new PrintStream(fos);
            ps.println(editText.getText().toString());
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.i("mylog", "请求结果为-->" + val);
        }
    };

    Runnable networkTask = new Runnable() {
        @Override
        public void run() {

            Intent result = new Intent(MainActivity.this, ShowResultActivity.class);
            Message msg = new Message();
            Bundle data = new Bundle();
//          定义http报文边界
            String end = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            try {
//              打开一个httpURLConnection
                URL url = new URL(uploadUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

//              设置连接属性
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setUseCaches(true);
                urlConnection.setRequestProperty("Connection", "Keep-Alive");
                urlConnection.setRequestProperty("Charset", "UTF-8");
                urlConnection.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);

//              打开网络输出流，向网络地址写数据
                DataOutputStream ds = new DataOutputStream(urlConnection.getOutputStream());
                ds.writeBytes(twoHyphens + boundary + end);
                ds.writeBytes("Content-Disposition: multipart/form-data; "
                        + "name=\"uploadfile\";filename=\"" + fileName + "\"" + end);
                ds.writeBytes(end);

//              打开本地输出流，向网络输入流写数据
                FileInputStream fis = openFileInput(fileName);
                byte[] buffer = new byte[1024];
                int length = -1;
                while ((length = fis.read(buffer)) != -1) {
                    ds.write(buffer, 0, length);
                }
                ds.writeBytes(end);
                ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
                fis.close();
                ds.flush();

//              获取返回结果
                InputStream is = urlConnection.getInputStream();
                int ch;
                StringBuffer b = new StringBuffer();
                while ((ch = is.read()) != -1) {
                    b.append((char) ch);
                }
                ds.close();
                result.putExtra("result", b.toString().trim());
                data.putString("value", "上传成功" + b.toString().trim());
            } catch (Exception e) {
                e.printStackTrace();
                data.putString("value", "上传失败");
            }

            startActivity(result);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

}
