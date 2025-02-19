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

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.SelectionCreator;
import com.zhihu.matisse.engine.impl.PicassoEngine;
import com.zhihu.matisse.filter.FilterKt;
import com.zhihu.matisse.internal.entity.CaptureStrategy;
import com.zhihu.matisse.listener.OnCheckedListener;
import com.zhihu.matisse.listener.OnSelectedListener;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static com.zhihu.matisse.MimeTypeKt.ofImageMimeType;
import static com.zhihu.matisse.MimeTypeKt.ofMimeType;

public class SampleActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "SampleActivity";
    private static final int REQUEST_CODE_CHOOSE = 23;

    private UriAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: it's a start");
        findViewById(R.id.zhihu).setOnClickListener(this);
        findViewById(R.id.dracula).setOnClickListener(this);
        findViewById(R.id.only_gif).setOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter = new UriAdapter());
    }

    @Override
    public void onClick(final View v) {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            switch (v.getId()) {
                                case R.id.zhihu:
                                    Matisse.Companion.from(SampleActivity.this)
                                            .choose(ofImageMimeType(), false)
                                            .countable(true)
                                            .capture(true)
                                            .captureStrategy(
                                                    new CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider", "test"))
                                            .maxSelectable(9)
                                            .addFilter(new GifSizeFilter(320, 320, 5 * FilterKt.K * FilterKt.K))
                                            .gridExpectedSize(
                                                    getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                                            .restrictOrientation(SelectionCreator.ScreenOrientation.SCREEN_ORIENTATION_PORTRAIT)
                                            .thumbnailScale(0.85f)
//                                            .imageEngine(new GlideEngine())  // for glide-V3
                                            .imageEngine(new Glide4Engine())    // for glide-V4
                                            .setOnSelectedListener(new OnSelectedListener() {
                                                @Override
                                                public void onSelected(
                                                        @NonNull List<? extends Uri> uriList, @NonNull List<String> pathList) {
                                                    // DO SOMETHING IMMEDIATELY HERE
                                                    Log.e("onSelected", "onSelected: pathList=" + pathList);

                                                }
                                            })
                                            .showSingleMediaType(true)
                                            .originalEnable(true)
                                            .maxOriginalSize(10)
                                            .autoHideToolbarOnSingleTap(true)
                                            .setOnCheckedListener(new OnCheckedListener() {
                                                @Override
                                                public void onCheck(boolean isChecked) {
                                                    // DO SOMETHING IMMEDIATELY HERE
                                                    Log.e("isChecked", "onCheck: isChecked=" + isChecked);
                                                }
                                            })
                                            .forResult(REQUEST_CODE_CHOOSE);
                                    break;
                                case R.id.dracula:
                                    Matisse.Companion.from(SampleActivity.this)
                                            .choose(ofImageMimeType())
                                            .theme(R.style.Matisse_Dracula)
                                            .countable(false)
                                            .addFilter(new GifSizeFilter(320, 320, 5 * FilterKt.K * FilterKt.K))
                                            .maxSelectable(9)
                                            .originalEnable(true)
                                            .maxOriginalSize(10)
                                            .imageEngine(new PicassoEngine())
                                            .forResult(REQUEST_CODE_CHOOSE);
                                    break;
                                case R.id.only_gif:
                                    Matisse.Companion.from(SampleActivity.this)
                                            .choose(ofMimeType(MimeType.GIF), false)
                                            .countable(true)
                                            .capture(true)
                                            .captureStrategy(
                                                    new CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider", "test"))
                                            .maxSelectable(9)
                                            .addFilter(new GifSizeFilter(320, 320, 5 * FilterKt.K * FilterKt.K))
                                            .gridExpectedSize(
                                                    getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                                            .restrictOrientation(SelectionCreator.ScreenOrientation.SCREEN_ORIENTATION_PORTRAIT)
                                            .thumbnailScale(0.85f)
//                                            .imageEngine(new GlideEngine())  // for glide-V3
                                            .imageEngine(new Glide4Engine())    // for glide-V4
                                            .setOnSelectedListener(new OnSelectedListener() {
                                                @Override
                                                public void onSelected(List<? extends Uri> uriList, List<String> pathList) {
                                                    // DO SOMETHING IMMEDIATELY HERE
                                                    Log.e("onSelected", "onSelected: pathList=" + pathList);
                                                }
                                            })
                                            .showSingleMediaType(true)
                                            .originalEnable(true)
                                            .maxOriginalSize(10)
                                            .autoHideToolbarOnSingleTap(true)
                                            .setOnCheckedListener(new OnCheckedListener() {
                                                @Override
                                                public void onCheck(boolean isChecked) {
                                                    // DO SOMETHING IMMEDIATELY HERE
                                                    Log.e("isChecked", "onCheck: isChecked=" + isChecked);
                                                }
                                            })
                                            .forResult(REQUEST_CODE_CHOOSE);
                                    break;
                                default:
                                    break;
                            }
                            mAdapter.setData(null, null);
                        } else {
                            Toast.makeText(SampleActivity.this, R.string.permission_request_denied, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mAdapter.setData(Matisse.Companion.obtainResult(data), Matisse.Companion.obtainPathResult(data));
            Log.e("OnActivityResult ", String.valueOf(Matisse.Companion.obtainOriginalState(data)));
        }
    }

    private static class UriAdapter extends RecyclerView.Adapter<UriAdapter.UriViewHolder> {

        private List<Uri> mUris;
        private List<String> mPaths;

        void setData(List<Uri> uris, List<String> paths) {
            mUris = uris;
            mPaths = paths;
            notifyDataSetChanged();
        }

        @Override
        public UriViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new UriViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.uri_item, parent, false));
        }

        @Override
        public void onBindViewHolder(UriViewHolder holder, int position) {
            holder.mUri.setText(mUris.get(position).toString());
            holder.mPath.setText(mPaths.get(position));

            holder.mUri.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
            holder.mPath.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
        }

        @Override
        public int getItemCount() {
            return mUris == null ? 0 : mUris.size();
        }

        static class UriViewHolder extends RecyclerView.ViewHolder {

            private TextView mUri;
            private TextView mPath;

            UriViewHolder(View contentView) {
                super(contentView);
                mUri = (TextView) contentView.findViewById(R.id.uri);
                mPath = (TextView) contentView.findViewById(R.id.path);
            }
        }
    }

}
