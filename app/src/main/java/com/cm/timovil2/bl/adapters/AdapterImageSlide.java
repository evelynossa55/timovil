package com.cm.timovil2.bl.adapters;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cm.timovil2.front.ActivityBase;

import java.util.ArrayList;

import com.cm.timovil2.R;
/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA 03/12/2015.
 */
public class AdapterImageSlide extends PagerAdapter {

    private final ActivityBase context;
    private final ArrayList<Bitmap> bitmaps;

    public AdapterImageSlide(ActivityBase context, ArrayList<Bitmap> bitmaps) {
        this.context = context;
        this.bitmaps = bitmaps;
    }

    @Override
    public int getCount() {
        return bitmaps.size();
    }

    @Override
    public @NonNull View instantiateItem(@NonNull ViewGroup container, final int position) {
        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.vp_image, container, false);
        Bitmap bitmap = bitmaps.get(position);

        ImageView mImageView = view.findViewById(R.id.image_display);
        mImageView.setImageBitmap(bitmap);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }


    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}