package com.zhihu.matisse.engine.impl

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.zhihu.matisse.engine.ImageEngine

/**
 * Description
 * <p>
 *     {@link ImageEngine} implementation using Picasso.
 *
 * @author peyo
 * @date 9/4/2019
 */
class PicassoEngine : ImageEngine {

    override fun loadThumbnail(context: Context, resize: Int, placeholder: Drawable?, imageView: ImageView, uri: Uri) {
        Picasso.with(context)
                .load(uri)
                .placeholder(placeholder)
                .resize(resize, resize)
                .centerCrop()
                .into(imageView)
    }

    override fun loadGifThumbnail(context: Context, resize: Int, placeholder: Drawable?, imageView: ImageView, uri: Uri) {
        loadThumbnail(context, resize, placeholder, imageView, uri)
    }

    override fun loadImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri) {
        Picasso.with(context)
                .load(uri)
                .resize(resizeX, resizeY)
                .priority(Picasso.Priority.HIGH)
                .centerInside()
                .into(imageView)
    }

    override fun loadGifImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri) {
        loadImage(context, resizeX, resizeY, imageView, uri)
    }

    override fun supportAnimatedGif(): Boolean = false
}