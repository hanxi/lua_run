package com.hanxi.luarun;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

public class KeywordHighlight {

    private static final String PATH     = Environment.getExternalStorageDirectory() + "/.jota/keyword/";
    private static final String USERPATH = Environment.getExternalStorageDirectory() + "/.jota/keyword/user/";
    private static final String EXT      = "conf";
    private static final String ASSET_PATH     = "keyword";
    private static final String COLOR_PATH     = "colorsetting."+EXT;

    public Pattern pattern;
    public int color;

    static ArrayList<KeywordHighlight> sList = new ArrayList<KeywordHighlight>();
//    static ArrayList<ForegroundColorSpan> sFcsList = new ArrayList<ForegroundColorSpan>();
    static HashMap<String,Integer> sColorMap = new HashMap<String,Integer>();
    static int mLastStart = 0;
    static int mLastEnd = 0;
    
    static TreeMap<Area,ForegroundColorSpan> sTmFcs = new TreeMap<Area,ForegroundColorSpan>(new Comparator<Area>(){
    	public int compare(Area a, Area b) {
    		return a.start-b.start;
    	}
    });

    private KeywordHighlight( String regexp , int _color )
    {
        pattern = Pattern.compile(regexp,Pattern.DOTALL);
        color = _color;
    }

    static private void addKeyword( String regexp , int color )
    {
        if ( color != 0 && !TextUtils.isEmpty(regexp) ){
            try{
                sList.add( new KeywordHighlight(regexp , color|0xFF000000) );
            }
            catch( PatternSyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    static private void clearKeyword( )
    {
        sList.clear();
    }

    static public boolean needHighlight()
    {
        return sList.size()!=0;
    }

    static public void refresh()
    {
        mLastStart = mLastEnd = -1;
    }

    // TODO: Unify with TextView.getWordForDictionary()
    private static int findWordStart(CharSequence text, int start) {
        if ( text.length() <= start ){
            return start;
        }

        UnicodeBlock c0 = UnicodeBlock.of(text.charAt(start));

        for (; start > 0; start--) {
            char c = text.charAt(start - 1);
            UnicodeBlock cb = UnicodeBlock.of(c);
            if ( c0 == UnicodeBlock.BASIC_LATIN ){
                int type = Character.getType(c);

                if (c != '\'' &&
                    type != Character.UPPERCASE_LETTER &&
                    type != Character.LOWERCASE_LETTER &&
                    type != Character.TITLECASE_LETTER &&
                    type != Character.MODIFIER_LETTER &&
                    type != Character.DECIMAL_DIGIT_NUMBER) {
                    break;
                }
            }else if ( c0 != cb ){
                break;
            }
        }

        return start;
    }

    // TODO: Unify with TextView.getWordForDictionary()
    private static int findWordEnd(CharSequence text, int end) {
        int len = text.length();

        if ( len <= end ){
            return end;
        }

        UnicodeBlock c0 = UnicodeBlock.of(text.charAt(end));

        for (; end < len; end++) {
            char c = text.charAt(end);
            UnicodeBlock cb = UnicodeBlock.of(c);
            if ( c0 == UnicodeBlock.BASIC_LATIN ){
                int type = Character.getType(c);

                if (c != '\'' &&
                    type != Character.UPPERCASE_LETTER &&
                    type != Character.LOWERCASE_LETTER &&
                    type != Character.TITLECASE_LETTER &&
                    type != Character.MODIFIER_LETTER &&
                    type != Character.DECIMAL_DIGIT_NUMBER) {
                    break;
                }
            }else if ( c0 != cb ){
                break;
            }
        }

        return end;
    }
    public static int findBlockStart(CharSequence text, int start) {
        if ( text.length() <= start ){
            return start;
        }

        for (; start > 0; start--) {
            char c = text.charAt(start - 1);
            if ( c == '\n' ){
                break;
            }
        }
        return start;
    }

    // TODO: Unify with TextView.getWordForDictionary()
    public  static int findBlockEnd(CharSequence text, int end) {
        int len = text.length();

        if ( len <= end ){
            return end;
        }

        for (; end < len; end++) {
            char c = text.charAt(end);
            if ( c == '\n' ){
                break;
            }
        }

        return end;
    }
    
    static public void setHighlight( SpannableStringBuilder buf, int start , int end, int bend)
    {
    	if (buf.length()>end && bend!=end) {
            Iterator<Area> iter = sTmFcs.keySet().iterator();
            int count = end-bend;
         	while (iter.hasNext()) {
    			//it.next()得到的是key，tm.get(key)得到obj
    			Area area = iter.next();
    			if (area.end>bend) {
    				ForegroundColorSpan fgspan = sTmFcs.get(area);
    				fgspan.setArea(area.start+count, bend+count);
    				area.start += count;
    				area.end += count;
    			}
    		}
    	}
    	System.out.print(mLastStart);
    	System.out.print(",");
    	System.out.print(mLastEnd);
        if ( mLastStart == start && mLastEnd == end ){
            return;
        }
        mLastStart = start;
        mLastEnd = end;


        start = findBlockStart(buf, start);
        end = findBlockEnd(buf, end);
        if ( end+1 < buf.length() ){
            end++;
        }
/*
        for(int i=0; i<sFcsList.size(); i++)
        {
        	ForegroundColorSpan fcs = sFcsList.get(i);
        	if (fcs.isLapped(start,end)) {
        		buf.removeSpan(fcs);
	            fcs.recycle();  
	            System.out.println("removeSpan");
	            sFcsList.remove(fcs);
        	}
        }*/
        //sFcsList.clear();
        Iterator<Area> iter = sTmFcs.keySet().iterator();
        ArrayList<Area> removeList = new ArrayList<Area>();
		while (iter.hasNext()) {
			//it.next()得到的是key，tm.get(key)得到obj
			Area area = iter.next();
			ForegroundColorSpan fgspan = sTmFcs.get(area);
        	if (fgspan.isLapped(start,end)) {
        		buf.removeSpan(fgspan);
        		fgspan.recycle();  
	            System.out.println("removeSpan");
	            removeList.add(area);
        	}
		}
		for (int i=0; i<removeList.size(); i++) {
			Area area = removeList.get(i);
            sTmFcs.remove(area);
		}

        System.out.println("can hightlight it2");

        CharSequence target = buf.subSequence(start, end);
        System.out.print(target);
        for( KeywordHighlight syn : sList ){
            try{
                System.out.println("can hightlight it3");
                Matcher m= syn.pattern.matcher(target);

                while (m.find()) {
                    int matchstart = start+m.start();
                    int matchend = start+m.end();
                    System.out.println("can hightlight it4");
                    if ( matchstart!=matchend ){
                        boolean found = false;
                        //for( ForegroundColorSpan fcs : sFcsList ){
                          //  if ( fcs.isLapped(matchstart, matchend)){
                            //    found = true;
                              //  break;
                            //}
                        //}
                        Iterator<Area> it = sTmFcs.keySet().iterator();
                		while (it.hasNext()) {
                			//it.next()得到的是key，tm.get(key)得到obj
                			Area area = it.next();
                			ForegroundColorSpan fgspan = sTmFcs.get(area);
                			if (fgspan.isLapped(matchstart, matchend)) {
                				found = true;
                				break;
                			}
                		}
                        if ( !found ){
                            ForegroundColorSpan fgspan = ForegroundColorSpan.obtain(syn.color,matchstart,matchend);
                            buf.setSpan(fgspan, matchstart, matchend, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            System.out.print("\n");System.out.print(matchstart);System.out.print(",");
                            System.out.print(matchend);
                            sTmFcs.put(new Area(matchstart,matchend),fgspan);
                        }
                    }
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    static private void loadColorSettings()
    {
        sColorMap.clear();
        String path = USERPATH + COLOR_PATH;

        File f = new File(path);
        if (!f.exists() ){
            path = PATH + COLOR_PATH;
            f = new File(path);
            if (!f.exists() ){
                return;
            }
        }

        // parse ini file
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line;
            while( (line = br.readLine()) != null ){
                line = line.replaceAll( "^#.*$" , "" );
                line = line.replaceAll( "^//.*$" , "" );
                line = line.replaceAll( "[ \\t]+$", "" );

                int separator = line.indexOf('=');
                if ( separator!=-1 ){
                    String head = line.substring(0, separator);
                    String body = line.substring(separator+1);

                    try{
                        int color = Integer.parseInt(body, 16);
                        sColorMap.put(head , color );
                    }
                    catch(Exception e){}
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if ( br != null ){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static File getKeywordFile(String path,String ext)
    {
        File dir = new File(path);
        File[] files = dir.listFiles();
        if ( files != null ){
            for( File f : files ){
                if ( f.isFile() ){
                    String name = f.getName();
                    String exts[] = name.split("\\.");
                    int len = exts.length-1;
                    if ( len>0 && EXT.equals(exts[len])){
                        for( int i=0;i<len;i++){
                            if ( ext.equalsIgnoreCase(exts[i])){
                                return f;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


    static public boolean loadHighlight(Context context, String filename )
    {
        clearKeyword();
        if (filename == null){
            return false;
        }
        int point = filename.lastIndexOf(".");
        if (point == -1) {
            return false;
        }
        String ext = filename.substring(point + 1);

        // create direcotry
        new File(USERPATH).mkdirs();
        if ( !new File(PATH + COLOR_PATH).exists() ){
            extractFromAssets( context );
        }

        File f = getKeywordFile(USERPATH,ext);
        if ( f==null ){
            f = getKeywordFile(PATH,ext);
            if ( f==null ){
                return false;
            }
        }
        loadColorSettings();

        System.out.println("load keyworld");

        // parse ini file
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line;
            while( (line = br.readLine()) != null ){
                line = line.replaceAll( "^//.*$" , "" );
                line = line.replaceAll( "[ \\t]+$", "" );

                int separator = line.indexOf('=');
                if ( separator!=-1 ){
                    String head = line.substring(0, separator);
                    String body = line.substring(separator+1);

                    Integer color = sColorMap.get(head);
                    System.out.println(head);
                    System.out.println(body);
                    if ( color!=null ){
                        System.out.println("yyyyyyyyyyyyyyyyyyy");
                        addKeyword( body , color );
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if ( br != null ){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        sColorMap.clear();
        System.out.println("xxxxxxxxxxxxxxxxxxx");
        System.out.println(sList.size());
        System.out.println("xxxxxxxxxxxxxxxxxxx");
        return true;
    }

    static public void extractFromAssets( Context context)
    {
        AssetManager am = context.getAssets();
        byte[] buf = new byte[4096];

        try {
            // create direcotry
            new File(USERPATH).mkdirs();

            // remove all files except directory..
            File dir = new File(PATH);
            File[] files = dir.listFiles();
            if ( files != null ){
                for( File f : files ){
                    if ( f.isFile() ){
                        f.delete();
                    }
                }
            }
            // extarct files from assets.
            String[] list = am.list(ASSET_PATH);
            for( String filename : list ){
                File ofile = new File(PATH  + filename);
                InputStream in = am.open(ASSET_PATH + "/"+ filename);
                OutputStream out = new FileOutputStream(ofile);
                try{
                    int len;
                    while( (len = in.read(buf))>0 ){
                        out.write(buf, 0, len);
                    }
                }
                catch(Exception e){}
                in.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

class Area {
	public Area(int matchstart, int matchend) {
		// TODO Auto-generated constructor stub
		start=matchstart;
		end = matchend;
	}
	public int start;
	public int end;
}


