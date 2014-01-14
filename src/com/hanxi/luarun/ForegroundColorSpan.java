/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hanxi.luarun;

import java.util.ArrayList;

import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

public class ForegroundColorSpan extends CharacterStyle
        implements UpdateAppearance, ParcelableSpan {

    private int mColor;
    private int mStart;
    private int mEnd;

    static private ArrayList<ForegroundColorSpan> sPool = new ArrayList<ForegroundColorSpan>();


	public ForegroundColorSpan(int color) {
		mColor = color;
	}

	public int getStart() {
		return mStart;
	}
	public int getEnd() {
		return mEnd;
	}

	public void setArea(int start, int end) {
		mStart = start;
		mEnd = end;
	}

    public ForegroundColorSpan(Parcel src) {
        mColor = src.readInt();
    }

    public int getSpanTypeId() {
    	return 2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mColor);
    }

	public int getForegroundColor() {
		return mColor;
	} 

    @Override
	public void updateDrawState(TextPaint ds) {
		ds.setColor(mColor);
	}

    public boolean isLapped(int s, int e)
    {
    	System.out.println("span:"+mStart+","+mEnd+","+s+","+e);
        return  ( mStart < e && s < mEnd );
    }

    static public ForegroundColorSpan obtain(int color,int start,int end)
    {
        if ( sPool.size() == 0 ){
        	ForegroundColorSpan fcs = new ForegroundColorSpan(color);
        	fcs.setArea(start, end);
            return fcs;
        }else{
            ForegroundColorSpan fcs = sPool.get(0);
            sPool.remove(fcs);
            fcs.mColor = color;
            fcs.mStart = start;
            fcs.mEnd = end;
            return fcs;
        }
    }

    public void recycle()
    {
        sPool.add(this);
    }
}
