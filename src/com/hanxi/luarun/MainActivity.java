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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System;
import java.util.HashMap;
import java.util.Map;

import org.openfiledialog.CallbackBundle;
import org.openfiledialog.FileDialog;

import com.hanxi.luarun.KeywordHighlight;

public class MainActivity extends Activity {
	public static final String PREFS_NAME = "MyPrefsFile";

	static private int openfileDialogId = 0;
	static private int savefileDialogId = 1;
	static private String TMP_FILE_NAME = "./tmp.lua";
	static private String mWriteablePath;

	private Button execute;
	private String mLastOpenFileName;
	private boolean mIsSave = true;

	// public so we can play with these from Lua
	private EditText source;

	Handler handler;

	public native String stringFromJNI();

	public native String luadostring(String str);

	public native void luaaddpath(String str);

	public native void luainit(String str);
	public native void luarestart();

	public native void luacleanoutput();

	static {
		System.loadLibrary("luajava");
	}

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
	protected Dialog onCreateDialog(int id, Bundle bundle) {
		if (id == openfileDialogId) {
			String curPath = bundle.getString("curPath");
			Map<String, Integer> images = new HashMap<String, Integer>();
			// 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
			images.put(FileDialog.sRoot, R.drawable.filedialog_root); // 根目录图标
			images.put(FileDialog.sParent, R.drawable.filedialog_folder_up); // 返回上一层的图标
			images.put(FileDialog.sFolder, R.drawable.filedialog_folder); // 文件夹图标
			images.put("lua", R.drawable.filedialog_luafile); // lua文件图标
			images.put(FileDialog.sEmpty, R.drawable.filedialog_root);
			Dialog dialog = FileDialog.createOpenDialog(id, this,
					this.getString(R.string.openfile), new CallbackBundle() {
						@Override
						public void callback(Bundle bundle) {
							String fileName = bundle.getString("path");
							openFile(fileName);
						}
					}, ".lua;", images, curPath);
			return dialog;
		} else if (id == savefileDialogId) {
			String curPath = bundle.getString("curPath");
			String content = bundle.getString("content");
			Map<String, Integer> images = new HashMap<String, Integer>();
			// 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
			images.put(FileDialog.sRoot, R.drawable.filedialog_root); // 根目录图标
			images.put(FileDialog.sParent, R.drawable.filedialog_folder_up); // 返回上一层的图标
			images.put(FileDialog.sFolder, R.drawable.filedialog_folder); // 文件夹图标
			images.put("lua", R.drawable.filedialog_luafile); // lua文件图标
			images.put(FileDialog.sEmpty, R.drawable.filedialog_root);
			Dialog dialog = FileDialog.createSaveDialog(id, this,
					this.getString(R.string.savefile), new CallbackBundle() {
						@Override
						public void callback(Bundle bundle) {
							final String fileName = bundle.getString("path");
							// 文件已存在是否覆盖
							AlertDialog.Builder builder = new AlertDialog.Builder(
									MainActivity.this);
							builder.setTitle(MainActivity.this
									.getString(R.string.fileexist));
							builder.setMessage(MainActivity.this
									.getString(R.string.fileexisttext));
							builder.setPositiveButton(
									MainActivity.this.getString(R.string.cover),
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int whitch) {
											mLastOpenFileName = fileName;
											saveFile();
											openFile(fileName);
										}
									});
							// 系统只提供三个对话框按钮,区别是默认的显示位置,Neutral在中间
							builder.setNegativeButton(MainActivity.this
									.getString(R.string.notcover), null);
							AlertDialog dialog = builder.create();
							dialog.show();// 记得加上show()方法
						}
					}, ".lua;", images, curPath, content, new CallbackBundle() {
						@Override
						public void callback(Bundle bundle) {
							String fileName = bundle.getString("fileName");
							openFile(fileName);
							mIsSave = true;
						}
					});
			return dialog;
		}
		return null;
	}

	private void openFile(String fileName) {
		StringBuffer strb = SdcardHelper.getFileToString(fileName);
		SpannableStringBuilder sp = new SpannableStringBuilder(strb);
		KeywordHighlight.clearHighlight();
		KeywordHighlight.setHighlight(sp, 0, sp.length(), 0);
		source.setText(sp);
		TextView title = (TextView) findViewById(R.id.titlebarText);
		title.setText(SdcardHelper.getFileNameFromPath(fileName));
		mLastOpenFileName = fileName;
		mIsSave = true;
		// System.out.println("misSave1=true");
	}

	@Override
	protected void onStop() {
		super.onStop();
		/* We need an Editor object to make preference changes. */
		/* All objects are from android.context.Context */
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("lastOpenFileName", mLastOpenFileName);
		/* Commit the edits! */
		editor.commit();
	}

	@SuppressWarnings("deprecation")
	private void saveFile() {
		Editable strb = source.getText();
		String content = strb.toString();
		if (mLastOpenFileName.equals(TMP_FILE_NAME)) {
			// 另存为
			Bundle bundle = new Bundle();
			bundle.putString("curPath",
					SdcardHelper.getFileDirPath(mLastOpenFileName));
			bundle.putString("content", content);
			showDialog(savefileDialogId, bundle);
		} else {
			if (SdcardHelper.writeStringToFile(mLastOpenFileName, content)) {
				Toast.makeText(MainActivity.this,
						MainActivity.this.getString(R.string.savesuccess),
						Toast.LENGTH_LONG).show();
				mIsSave = true;
			} else {
				Toast.makeText(MainActivity.this,
						MainActivity.this.getString(R.string.savefaild),
						Toast.LENGTH_LONG).show();
			}
		}
		// System.out.println(mLastOpenFileName);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SysApplication.getInstance().addActivity(this);

		mWriteablePath = this.getFilesDir().getAbsolutePath();

		RelativeLayout mBarView = (RelativeLayout) View.inflate(this,
				R.layout.titlebar, null);
		LinearLayout mLinearLayout = (LinearLayout) findViewById(R.id.titlebar);
		mLinearLayout.addView(mBarView);

		SdcardHelper.setDir(getPackageName().toString());
		TMP_FILE_NAME = SdcardHelper.getWriteDir() + "/tmp.lua";
		mIsSave = true;

		// 设置单击按钮时打开文件对话框
		findViewById(R.id.btnOpen).setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View arg0) {
				if (mIsSave) {
					Bundle bundle = new Bundle();
					bundle.putString("curPath",
							SdcardHelper.getFileDirPath(mLastOpenFileName));
					showDialog(openfileDialogId, bundle);
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							MainActivity.this);
					builder.setTitle(MainActivity.this
							.getString(R.string.nosavering));
					builder.setMessage(MainActivity.this
							.getString(R.string.nosavetext));
					builder.setPositiveButton(
							MainActivity.this.getString(R.string.nosave),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whitch) {
									Bundle bundle = new Bundle();
									bundle.putString("curPath", SdcardHelper
											.getFileDirPath(mLastOpenFileName));
									showDialog(openfileDialogId, bundle);
								}
							});
					// 系统只提供三个对话框按钮,区别是默认的显示位置,Neutral在中间
					builder.setNegativeButton(
							MainActivity.this.getString(R.string.save),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whitch) {
									saveFile();
								}
							});
					AlertDialog dialog = builder.create();
					dialog.show();// 记得加上show()方法
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
				if (!SdcardHelper.writeStringToFile(mLastOpenFileName, content)) {
					Toast.makeText(MainActivity.this,
							MainActivity.this.getString(R.string.savefaild),
							Toast.LENGTH_LONG).show();
				}
				openFile(TMP_FILE_NAME);
				source.setText("");
				mLastOpenFileName = TMP_FILE_NAME;
				// System.out.println(mLastOpenFileName);
			}
		});

		// 数据持久化
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		mLastOpenFileName = settings.getString("lastOpenFileName",
				TMP_FILE_NAME);
		TextView title = (TextView) findViewById(R.id.titlebarText);
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
		KeywordHighlight.setHighlight(sp, 0, sp.length(), 0);
		source.setText(sp);
		source.addTextChangedListener(new watcher());

		execute = (Button) findViewById(R.id.btnRun);
		// 绑定匿名的监听器，并执行您所要在点击按钮后执行的逻辑代码
		execute.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String src = source.getText().toString();
				String res = "";
				res = evalLua(src);
				Toast.makeText(MainActivity.this, R.string.run_finished,
						Toast.LENGTH_LONG).show();
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, ResultActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("result", res);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});

		handler = new Handler();

		luainit(mWriteablePath);
	}

	class watcher implements TextWatcher {
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			KeywordHighlight.setHighlight((SpannableStringBuilder) s, start,
					start + count, start + before);
			mIsSave = false;
			// System.out.println("misSave2=false");
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
		}
	}

	String evalLua(String src) {
		luacleanoutput();
        luarestart();
		luaaddpath(SdcardHelper.getFileDirPath(mLastOpenFileName));
		String res = luadostring(src);
		return res;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
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
