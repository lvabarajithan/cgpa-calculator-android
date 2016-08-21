/*
 * Copyright (C) 2016 Abarajithan Lv
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
package com.abara.calculator.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by abara on 15/11/15.
 *
 * Custom TextView class using "Roboto-Regular.ttf" as font
 */
public class MaterialTextViewRegular extends TextView {

    public MaterialTextViewRegular(Context context) {
        super(context);
        setTypeface(Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf"));
    }

    public MaterialTextViewRegular(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf"));
    }

    public MaterialTextViewRegular(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeface(Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf"));
    }

}
