package com.hanxi.luarun;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.Menu;
import android.view.MenuItem;
import android.content.res.AssetManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.System;
import java.net.ServerSocket;
import java.net.Socket;

import org.keplerproject.luajava.*;

import com.hanxi.luarun.KeywordHighlight;

public class MainActivity extends Activity {

    private WebView mWebView;
    private Js mJs;
	private final static int LISTEN_PORT = 3333;

	Button execute;
	
	// public so we can play with these from Lua
	public EditText source;
	public TextView status;
	public LuaState L;
	
	final StringBuilder output = new StringBuilder();

	Handler handler;
	ServerThread serverThread;
	
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

        mJs = new Js();
        /*
        mWebView = (WebView)findViewById(R.id.webView);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.requestFocus();
        mWebView.loadUrl("file:///android_asset/code_editor.html");
        mWebView.addJavascriptInterface(mJs, "Java");
*/
		execute = (Button)findViewById(R.id.btnRun);
        //绑定匿名的监听器，并执行您所要在点击按钮后执行的逻辑代码
        execute.setOnClickListener(new View.OnClickListener() {
        	 @Override
        	 public void onClick(View arg0) {
        			String src = source.getText().toString();
        			status.setText("");
        			try {
        				String res = evalLua(src);
        				status.append(res);
        				status.append("Finished succesfully");
        			} catch(LuaException e) {			
        				Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();			
        			}
        	 }
         });

		source = (EditText) findViewById(R.id.editText);
		boolean canHighlight = KeywordHighlight.loadHighlight(this, "t.lua");
	    if (canHighlight) {
	    	System.out.println("can hightlight it");
	    }
		String str = "local t=1\nlocal a=t";
		SpannableStringBuilder sp = new SpannableStringBuilder(str);
		KeywordHighlight.setHighlight(sp,0,sp.length(),0);
		source.setText(sp);
		source.addTextChangedListener(new watcher());

		status = (TextView) findViewById(R.id.statusText);
		//status.setMovementMethod(ScrollingMovementMethod.getInstance());

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
			status.setText("Cannot override print");
		}
    }
	class watcher implements TextWatcher{
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			KeywordHighlight.setHighlight((SpannableStringBuilder)s,start,start+count,start+before);
			System.out.print(start);
			System.out.print(",");
			System.out.print(start+before);
			System.out.print(",");
			System.out.print(start+count);
			System.out.print("\n");
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
					status.setText(s);
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
		serverThread = new ServerThread();
		serverThread.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		serverThread.stopped = true;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    final class Js {
        Js() {
        }
        public String getCodeString() {
            mWebView.loadUrl("javascript:getCodeString()");
            System.out.println("hello js");
            return "";
        }
    }
}