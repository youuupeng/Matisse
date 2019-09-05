/*
 * Copyright 2017 Zhihu Inc.
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
package com.zhihu.matisse.sample;

import android.content.Context;
import android.graphics.Point;

import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.IncapableCause;
import com.zhihu.matisse.internal.entity.Item;

import java.util.HashSet;
import java.util.Set;

import static com.zhihu.matisse.internal.utils.PhotoMetadataUtilsKt.getBitmapBound;
import static com.zhihu.matisse.internal.utils.PhotoMetadataUtilsKt.getSizeInMB;

class GifSizeFilter extends Filter {

    private int mMinWidth;
    private int mMinHeight;
    private int mMaxSize;

    GifSizeFilter(int minWidth, int minHeight, int maxSizeInBytes) {
        mMinWidth = minWidth;
        mMinHeight = minHeight;
        mMaxSize = maxSizeInBytes;
    }

    @Override
    public Set<MimeType> constraintTypes() {
        return new HashSet<MimeType>() {{
            add(MimeType.GIF);
        }};
    }

    @Override
    public IncapableCause filter(Context context, Item item) {
        if (!needFiltering(context, item))
            return null;

        Point size = getBitmapBound(context.getContentResolver(), item.getUri());
        if (size.x < mMinWidth || size.y < mMinHeight || item.getSize() > mMaxSize) {
            return new IncapableCause(IncapableCause.Form.DIALOG, context.getString(R.string.error_gif, mMinWidth,
                    String.valueOf(getSizeInMB(mMaxSize))));
        }
        return null;
    }

}
