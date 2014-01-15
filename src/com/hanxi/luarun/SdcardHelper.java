package com.hanxi.luarun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;


public class SdcardHelper {
	private static String  SDCARD_DIR;
	private static String  NOSDCARD_DIR;
	
	static public void setDir(String packageName) {
		SDCARD_DIR = Environment.getExternalStorageDirectory().toString()+"/.luaRun";
		NOSDCARD_DIR = Environment.getDataDirectory().toString()+packageName+"/.luaRun";		
		if (SdcardHelper.isHasSdcard()) {
			 File destDir = new File(SDCARD_DIR);
		     if (!destDir.exists()) {
		       destDir.mkdirs();
	         }
		}
		else {
			File destDir = new File(NOSDCARD_DIR);
		     if (!destDir.exists()) {
		       destDir.mkdirs();
	         }
		}
	}
	
	static public boolean isHasSdcard() {
		String status = Environment.getExternalStorageState();
		  if (status.equals(Environment.MEDIA_MOUNTED)) {
		   return true;
		  } else {
		   return false;
		  }
	}
	
	static public String getWriteDir() {
		if (SdcardHelper.isHasSdcard()) {
			   return SDCARD_DIR;
		  } else {
			   return NOSDCARD_DIR;
		  }
	}

	static public String getFileDirPath(String path) {
		int last = path.lastIndexOf('/');
		String fileName = path.substring(0,last);
		return fileName;
	}

	static public String getFileNameFromPath(String path) {
		int last = path.lastIndexOf('/');
		String fileName = path.substring(last+1,path.length());
		return fileName;
	}
	
	static public boolean writeStringToFile(String fileName, String str) {
		try {
	        File file = new File(fileName);
			FileOutputStream out;
			out = new FileOutputStream(file);
			out.write(str.getBytes());
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	static public StringBuffer getFileToString(String path) {
		StringBuffer strb = new StringBuffer();
        FileInputStream in = null;
        File file = new File(path);
		try {
			in = new FileInputStream(file);
            int len;
            byte[] buf = new byte[4096];
            while( (len = in.read(buf))>0 ){
                strb.append(new String(buf, 0, len));
            }
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return strb;
	}
}
