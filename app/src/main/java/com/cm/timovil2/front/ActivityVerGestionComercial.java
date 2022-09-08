package com.cm.timovil2.front;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import androidx.viewpager.widget.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.adapters.AdapterImageSlide;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.CirclePageIndicator;
import com.cm.timovil2.bl.utilities.PageIndicator;
import com.cm.timovil2.data.ClienteDAL;
import com.cm.timovil2.data.GestionComercialDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.GestionComercialDTO;

import java.util.ArrayList;

/*
 * Created by JORGE ANDRES DAVID CARDONA on 14/10/2015.
 */
public class ActivityVerGestionComercial extends ActivityBase {

    private TextView textViewContacto;
    private TextView textViewTelefonoContacto;
    private TextView textViewComentario;
    private TextView textViewCliente;
    private TextView textViewTomarFoto;
    //---
    private static final long ANIM_VIEWPAGER_DELAY = 5000;
    private static final long ANIM_VIEWPAGER_DELAY_USER_VIEW = 10000;
    private ViewPager mViewPager;
    private PageIndicator mIndicator;
    private ArrayList<Bitmap> images;
    private Runnable animateViewPager;
    private Handler handler;
    private boolean stopSliding = false;

    private class PageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_gestion_comercial);
        setControls();
        App.actualActivity = this;
        try {
            obtenerUbicacion();
            Intent intent = getIntent();
            long idGestion = intent.getLongExtra("IdGestion", -1);

            if (idGestion > 0) {
                textViewTomarFoto.setVisibility(View.VISIBLE);
                mostrarDatosGestion(idGestion);
            }else if(App.vergestionComercialDTO != null){
                textViewTomarFoto.setVisibility(View.GONE);
                mostrarDatosGestion(App.vergestionComercialDTO);
            }

        }catch (Exception x){
            makeErrorDialog(x.getMessage(), this);
        }
    }

    @Override
    protected void setControls() {
        textViewComentario = findViewById(R.id.text_view_comentario);
        textViewTelefonoContacto = findViewById(R.id.text_view_tel_contacto);
        textViewContacto = findViewById(R.id.text_view_contacto);
        textViewCliente = findViewById(R.id.text_view_cliente);
        textViewTomarFoto = findViewById(R.id.text_view_tomar_foto);

        mViewPager = findViewById(R.id.view_pager);
        mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        mIndicator.setOnPageChangeListener(new PageChangeListener());
        mViewPager.setOnPageChangeListener(new PageChangeListener());
        mViewPager.setOnTouchListener(new android.view.View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction()) {

                    case MotionEvent.ACTION_CANCEL:
                        break;

                    case MotionEvent.ACTION_UP:
                        // calls when touch release on ViewPager
                        if (images != null && images.size() != 0) {
                            stopSliding = false;
                            runnable(images.size());
                            handler.postDelayed(animateViewPager, ANIM_VIEWPAGER_DELAY_USER_VIEW);
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // calls when ViewPager touch
                        if (handler != null && !stopSliding) {
                            stopSliding = true;
                            handler.removeCallbacks(animateViewPager);
                        }
                        break;
                }
                return false;
            }
        });
    }

    private void setPageAdapter() {
        ArrayList<Bitmap> bitmaps = new ArrayList<>(images);
        mostrarImagenes(bitmaps);
    }

    private void mostrarImagenes(ArrayList<Bitmap> imagenes) {
        if (images != null && images.size() > 0) {
            mViewPager.setAdapter(new AdapterImageSlide(this, imagenes));
            mIndicator.setViewPager(mViewPager);
            runnable(images.size());
            // Re-run callback
            handler.postDelayed(animateViewPager, ANIM_VIEWPAGER_DELAY);
        }
    }

    private void runnable(final int size) {
        handler = new Handler();
        animateViewPager = new Runnable() {
            public void run() {
                if (!stopSliding) {
                    if (mViewPager.getCurrentItem() == size - 1) {
                        mViewPager.setCurrentItem(0);
                    } else {
                        mViewPager.setCurrentItem(
                                mViewPager.getCurrentItem() + 1, true);
                    }
                    handler.postDelayed(animateViewPager, ANIM_VIEWPAGER_DELAY);
                }
            }
        };
    }

    private void mostrarDatosGestion(long idGestion){

        GestionComercialDAL gestionDAL = new GestionComercialDAL(this);
        GestionComercialDTO gestionDTO = gestionDAL.getGestionComercial(idGestion);
        mostrarDatosGestion(gestionDTO);

    }

    private void mostrarDatosGestion(GestionComercialDTO gestionDTO){

        ClienteDAL clienteDAL = new ClienteDAL(this);
        ClienteDTO clienteDTO = clienteDAL.ObtenerClientePorIdCliente(String.valueOf(gestionDTO.IdCliente));

        String titulo = clienteDTO.NombreComercial + " - " + clienteDTO.RazonSocial;

        textViewCliente.setText(titulo);
        textViewContacto.setText(gestionDTO.Contacto);
        textViewTelefonoContacto.setText(gestionDTO.TelContacto);
        textViewComentario.setText(gestionDTO.Comentario);

        images = new ArrayList<>();
        //Mostrar imagenes
        if(gestionDTO.EncodedImages != null && !gestionDTO.EncodedImages.isEmpty()) {
            String[] pathImages = gestionDTO.EncodedImages.split("\\|");
            for (String path : pathImages) {
                try {
                    Bitmap photobmp;
                    photobmp = BitmapFactory.decodeFile(path);
                    images.add(photobmp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        setPageAdapter();
    }

}
