package com.hanxi.luarun;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.System;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.keplerproject.luajava.*;
import org.openfiledialog.CallbackBundle;
import org.openfiledialog.FileDialog;

import com.hanxi.luarun.KeywordHighlight;

public class MainActivity extends Activity {
    public static final String PREFS_NAME = "MyPrefsFile";

    private final static int LISTEN_PORT = 3333;
    static private int openfileDialogId = 0;
    static private int savefileDialogId = 1;
    private static String TMP_FILE_NAME =  "./tmp.lua";

    Button execute;
    private String mLastOpenFileName;
    private boolean mIsSave = true;


    // public so we can play with these from Lua
    public EditText source;
    public LuaState L;

    final StringBuilder output = new StringBuilder();

    Handler handler;
//  ServerThread serverThread;

    private static byte[] readAll(InputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    @Override
    protected Dialog onCreateDialog(int id,Bundle bundle) {
        if(id==openfileDialogId){
            String curPath = bundle.getString("curPath");
            Map<String, Integer> images = new HashMap<String, Integer>();
            // 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
            images.put(FileDialog.sRoot, R.drawable.filedialog_root);   // 根目录图标
            images.put(FileDialog.sParent, R.drawable.filedialog_folder_up);    //返回上一层的图标
            images.put(FileDialog.sFolder, R.drawable.filedialog_folder);   //文件夹图标
            images.put("lua", R.drawable.filedialog_luafile);   //lua文件图标
            images.put(FileDialog.sEmpty, R.drawable.filedialog_root);
            Dialog dialog = FileDialog.createOpenDialog(id, this, this.getString(R.string.openfile), new CallbackBundle() {
                @Override
                public void callback(Bundle bundle) {
                    String fileName = bundle.getString("path");
                    openFile(fileName);
                }
            },
            ".lua;",
            images,
            curPath);
            return dialog;
        }
        else if (id==savefileDialogId) {
            String curPath = bundle.getString("curPath");
            String content = bundle.getString("content");
            Map<String, Integer> images = new HashMap<String, Integer>();
            // 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
            images.put(FileDialog.sRoot, R.drawable.filedialog_root);   // 根目录图标
            images.put(FileDialog.sParent, R.drawable.filedialog_folder_up);    //返回上一层的图标
            images.put(FileDialog.sFolder, R.drawable.filedialog_folder);   //文件夹图标
            images.put("lua", R.drawable.filedialog_luafile);   //lua文件图标
            images.put(FileDialog.sEmpty, R.drawable.filedialog_root);
            Dialog dialog = FileDialog.createSaveDialog(id, this, this.getString(R.string.savefile), new CallbackBundle() {
                @Override
                public void callback(Bundle bundle) {
                    final String fileName = bundle.getString("path");
                    // 文件已存在是否覆盖
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(MainActivity.this.getString(R.string.fileexist));
                    builder.setMessage(MainActivity.this.getString(R.string.fileexisttext));
                    builder.setPositiveButton(MainActivity.this.getString(R.string.cover), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whitch) {
                            mLastOpenFileName = fileName;
                            saveFile();
                            openFile(fileName);
                        }
                    });
                    // 系统只提供三个对话框按钮,区别是默认的显示位置,Neutral在中间
                    builder.setNegativeButton(MainActivity.this.getString(R.string.notcover), null);
                    AlertDialog dialog = builder.create();
                    dialog.show();//记得加上show()方法
                }
            },
            ".lua;",
            images,
            curPath,
            content, new CallbackBundle() {
                @Override
                public void callback(Bundle bundle) {
                    String fileName = bundle.getString("fileName");
                    openFile(fileName);
                    mIsSave = true;
                }
            }
            );
            return dialog;
        }
        return null;
    }

    private void openFile(String fileName) {
        StringBuffer strb = SdcardHelper.getFileToString(fileName);
        SpannableStringBuilder sp = new SpannableStringBuilder(strb);
        KeywordHighlight.clearHighlight();
        KeywordHighlight.setHighlight(sp,0,sp.length(),0);
        source.setText(sp);
        TextView title = (TextView)findViewById(R.id.titlebarText);
        title.setText(SdcardHelper.getFileNameFromPath(fileName));
        mLastOpenFileName = fileName;
        mIsSave = true;
        //System.out.println("misSave1=true");
    }
    @Override
    protected void onStop(){
       super.onStop();
      /* We need an Editor object to make preference changes.*/
      /* All objects are from android.context.Context*/
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putString("lastOpenFileName", mLastOpenFileName);
      /* Commit the edits!*/
      editor.commit();
    }

    private void saveFile() {
        Editable strb = source.getText();
        String content = strb.toString();
        if (mLastOpenFileName.equals(TMP_FILE_NAME)) {
            // 另存为
            Bundle bundle = new Bundle();
            bundle.putString("curPath", SdcardHelper.getFileDirPath(mLastOpenFileName));
            bundle.putString("content", content);
            showDialog(savefileDialogId,bundle);
        }
        else {
            if (SdcardHelper.writeStringToFile(mLastOpenFileName,content)) {
                Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.savesuccess), Toast.LENGTH_LONG).show();
                mIsSave = true;
            }
            else {
                Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.savefaild), Toast.LENGTH_LONG).show();
            }
        }
        //System.out.println(mLastOpenFileName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SysApplication.getInstance().addActivity(this);

        RelativeLayout mBarView = (RelativeLayout)View.inflate(this, R.layout.titlebar, null);
        LinearLayout mLinearLayout = (LinearLayout)findViewById(R.id.titlebar);
        mLinearLayout.addView(mBarView);

        SdcardHelper.setDir(getPackageName().toString());
        TMP_FILE_NAME =  SdcardHelper.getWriteDir()+"/tmp.lua";
        mIsSave = true;

        // 设置单击按钮时打开文件对话框
        findViewById(R.id.btnOpen).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mIsSave) {
                    Bundle bundle = new Bundle();
                    bundle.putString("curPath", SdcardHelper.getFileDirPath(mLastOpenFileName));
                    showDialog(openfileDialogId,bundle);
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(MainActivity.this.getString(R.string.nosavering));
                    builder.setMessage(MainActivity.this.getString(R.string.nosavetext));
                    builder.setPositiveButton(MainActivity.this.getString(R.string.nosave), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whitch) {
                            Bundle bundle = new Bundle();
                            bundle.putString("curPath", SdcardHelper.getFileDirPath(mLastOpenFileName));
                            showDialog(openfileDialogId,bundle);
                        }
                    });
                    // 系统只提供三个对话框按钮,区别是默认的显示位置,Neutral在中间
                    builder.setNegativeButton(MainActivity.this.getString(R.string.save), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whitch) {
                            saveFile();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();//记得加上show()方法
                }
            }
        });

        // 设置保存按钮事件
        findViewById(R.id.btnSave).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                saveFile();
            }
        });

        // 设置新建按钮事件
        findViewById(R.id.btnNew).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                                Editable strb = source.getText();
                                String content = strb.toString();
                                if (!SdcardHelper.writeStringToFile(mLastOpenFileName,content)) {
                                        Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.savefaild), Toast.LENGTH_LONG).show();
                                }
                                openFile(TMP_FILE_NAME);
                                source.setText("");
                                mLastOpenFileName = TMP_FILE_NAME;
                                //System.out.println(mLastOpenFileName);
                        }
                });

        // 数据持久化
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mLastOpenFileName = settings.getString("lastOpenFileName",TMP_FILE_NAME);
        TextView title = (TextView)findViewById(R.id.titlebarText);
        title.setText(SdcardHelper.getFileNameFromPath(mLastOpenFileName));
        File file = new File(mLastOpenFileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);
                out.write("print('hello lua');".getBytes("utf-8"));
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        source = (EditText) findViewById(R.id.editText);
        boolean canHighlight = KeywordHighlight.loadHighlight(this, "t.lua");
        if (!canHighlight) {
            System.out.println("can't hightlight it");
        }
        StringBuffer strb = SdcardHelper.getFileToString(mLastOpenFileName);
        SpannableStringBuilder sp = new SpannableStringBuilder(strb);
        KeywordHighlight.clearHighlight();
        KeywordHighlight.setHighlight(sp,0,sp.length(),0);
        source.setText(sp);
        source.addTextChangedListener(new watcher());

        execute = (Button)findViewById(R.id.btnRun);
        //绑定匿名的监听器，并执行您所要在点击按钮后执行的逻辑代码
        execute.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View arg0) {
                    String src = source.getText().toString();
                    String res = "";
                    try {
                        res = evalLua(src);
                        Toast.makeText(MainActivity.this, R.string.run_finished, Toast.LENGTH_LONG).show();
                    } catch(LuaException e) {
                        res = e.getMessage();
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this,ResultActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("result", res);
                    intent.putExtras(bundle);
                    startActivity(intent);
             }
         });

        handler = new Handler();

        L = LuaStateFactory.newLuaState();
        L.openLibs();

        try {
            L.pushJavaObject(this);
            L.setGlobal("activity");

            JavaFunction print = new JavaFunction(L) {
                @Override
                public int execute() throws LuaException {
                    for (int i = 2; i <= L.getTop(); i++) {
                        int type = L.type(i);
                        String stype = L.typeName(type);
                        String val = null;
                        if (stype.equals("userdata")) {
                            Object obj = L.toJavaObject(i);
                            if (obj != null)
                                val = obj.toString();
                        } else if (stype.equals("boolean")) {
                            val = L.toBoolean(i) ? "true" : "false";
                        } else {
                            val = L.toString(i);
                        }
                        if (val == null)
                            val = stype;
                        output.append(val);
                        output.append("\t");
                    }
                    output.append("\n");
                    return 0;
                }
            };
            print.register("print");

            JavaFunction assetLoader = new JavaFunction(L) {
                @Override
                public int execute() throws LuaException {
                    String name = L.toString(-1);

                    AssetManager am = getAssets();
                    try {
                        InputStream is = am.open(name + ".lua");
                        byte[] bytes = readAll(is);
                        L.LloadBuffer(bytes, name);
                        return 1;
                    } catch (Exception e) {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        e.printStackTrace(new PrintStream(os));
                        L.pushString("Cannot load module "+name+":\n"+os.toString());
                        return 1;
                    }
                }
            };

            L.getGlobal("package");            // package
            L.getField(-1, "loaders");         // package loaders
            int nLoaders = L.objLen(-1);       // package loaders

            L.pushJavaFunction(assetLoader);   // package loaders loader
            L.rawSetI(-2, nLoaders + 1);       // package loaders
            L.pop(1);                          // package

            L.getField(-1, "path");            // package path
            String customPath = getFilesDir() + "/?.lua";
            L.pushString(";" + customPath);    // package path custom
            L.concat(2);                       // package pathCustom
            L.setField(-2, "path");            // package
            L.pop(1);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Cannot override print", Toast.LENGTH_LONG).show();
        }
    }
    class watcher implements TextWatcher{
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            KeywordHighlight.setHighlight((SpannableStringBuilder)s,start,start+count,start+before);
            mIsSave = false;
            //System.out.println("misSave2=false");
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void afterTextChanged(Editable arg0) {
            // TODO Auto-generated method stub

        }
    }

    private class ServerThread extends Thread {
        public boolean stopped;

        @Override
        public void run() {
            stopped = false;
            try {
                ServerSocket server = new ServerSocket(LISTEN_PORT);
                show("Server started on port " + LISTEN_PORT);
                while (!stopped) {
                    Socket client = server.accept();
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(client.getInputStream()));
                    final PrintWriter out = new PrintWriter(client.getOutputStream());
                    String line = null;
                    while (!stopped && (line = in.readLine()) != null) {
                        final String s = line.replace('\001', '\n');
                        if (s.startsWith("--mod:")) {
                            int i1 = s.indexOf(':'), i2 = s.indexOf('\n');
                            String mod = s.substring(i1+1,i2);
                            String file = getFilesDir()+"/"+mod.replace('.', '/')+".lua";
                            FileWriter fw = new FileWriter(file);
                            fw.write(s);
                            fw.close();
                            // package.loaded[mod] = nil
                            L.getGlobal("package");
                            L.getField(-1, "loaded");
                            L.pushNil();
                            L.setField(-2, mod);
                            out.println("wrote " + file + "\n");
                            out.flush();
                        } else {
                            handler.post(new Runnable() {
                                public void run() {
                                    String res = safeEvalLua(s);
                                    res = res.replace('\n', '\001');
                                    out.println(res);
                                    out.flush();
                                }
                            });
                        }
                    }
                }
                server.close();
            } catch (Exception e) {
                show(e.toString());
            }
        }

        private void show(final String s) {
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    String safeEvalLua(String src) {
        String res = null;
        try {
            res = evalLua(src);
        } catch(LuaException e) {
            res = e.getMessage()+"\n";
        }
        return res;
    }

    String evalLua(String src) throws LuaException {
        L.setTop(0);
        int ok = L.LloadString(src);
        if (ok == 0) {
            L.getGlobal("debug");
            L.getField(-1, "traceback");
            L.remove(-2);
            L.insert(-2);
            ok = L.pcall(0, 0, -2);
            if (ok == 0) {
                String res = output.toString();
                output.setLength(0);
                return res;
            }
        }
        throw new LuaException(errorReason(ok) + ": " + L.toString(-1));
        //return null;

    }

    @Override
    protected void onResume() {
        super.onResume();
        //serverThread = new ServerThread();
        //serverThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //serverThread.stopped = true;
    }
    private String errorReason(int error) {
        switch (error) {
        case 4:
            return "Out of memory";
        case 3:
            return "Syntax error";
        case 2:
            return "Runtime error";
        case 1:
            return "Yield error";
        }
        return "Unknown error " + error;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            pressAgainExit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void pressAgainExit() {
        if (Exit.isExit()) {
            SysApplication.getInstance().exit();
        } else {
            Toast.makeText(getApplicationContext(), R.string.exit_again,
                    Integer.valueOf(R.string.exit_time)).show();
            Exit.doExitInOneSecond();
        }
    }

}
