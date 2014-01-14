// filename: OpenFileDemo.java
package org.openfiledialog;

import java.util.HashMap;
import java.util.Map;

import com.hanxi.luarun.R;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;

public class OpenFileDemo extends Activity {
	
	static private int openfileDialogId = 0;
	

	@Override
	protected Dialog onCreateDialog(int id) {
		if(id==openfileDialogId){
			Map<String, Integer> images = new HashMap<String, Integer>();
			// 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
			images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);	// 根目录图标
			images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);	//返回上一层的图标
			images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);	//文件夹图标
			images.put("wav", R.drawable.filedialog_wavfile);	//wav文件图标
			images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
			Dialog dialog = OpenFileDialog.createDialog(id, this, "打开文件", new CallbackBundle() {
				@Override
				public void callback(Bundle bundle) {
					String filepath = bundle.getString("path");
					setTitle(filepath); // 把文件路径显示在标题上
				}
			}, 
			".wav;",
			images,"/");
			return dialog;
		}
		return null;
	}
}
