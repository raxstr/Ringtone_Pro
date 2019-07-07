package com.ristana.ringtone_app.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ristana.ringtone_app.R;
import com.ristana.ringtone_app.adapter.CategorySelectAdapter;
import com.ristana.ringtone_app.adapter.SelectableViewHolder;
import com.ristana.ringtone_app.api.ProgressRequestBody;
import com.ristana.ringtone_app.api.apiClient;
import com.ristana.ringtone_app.api.apiRest;
import com.ristana.ringtone_app.entity.ApiResponse;
import com.ristana.ringtone_app.entity.Category;
import com.ristana.ringtone_app.manager.PrefManager;
import com.squareup.picasso.Picasso;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.AudioPickActivity;
import com.vincent.filepicker.filter.entity.AudioFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UploadActivity extends AppCompatActivity implements ProgressRequestBody.UploadCallbacks,SelectableViewHolder.OnItemSelectedListener  {

    private RelativeLayout relative_layout_upload;




    private int PICK_AUDIO = 1002;
    private Bitmap bitmap_wallpaper;
    private ProgressDialog register_progress;
    private FloatingActionButton fab_upload;
    private FloatingActionButton button_save_upload;
    private EditText edit_text_upload_title;
    private static final int CAMERA_REQUEST_IMAGE_1 = 3001;
    private String          imageurl;
    private ArrayList<Category> categoriesListObj = new ArrayList<Category>();
    private CircleImageView circle_image_view_upload_user;
    private LinearLayoutManager gridLayoutManagerCategorySelect;
    private RecyclerView recycle_view_selected_category;
    private CategorySelectAdapter categorySelectAdapter;
    private ProgressBar progress_bar_progress_wallpaper_upload;
    private TextView text_view_progress_progress_wallpaper_upload;
    private RelativeLayout relative_layout_progress_wallpaper_upload
            ;
    private File file_final;
    private long file_duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        initView();
        initAction();
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //  SelectWallpaper();
        getSupportActionBar().setTitle(getResources().getString(R.string.upload_ringtone));
    }

    private void initAction() {
        fab_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectWallpaper();
            }
        });
        button_save_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_text_upload_title.getText().toString().trim().length()<3){
                    Toasty.error(UploadActivity.this, getResources().getString(R.string.edit_text_upload_title_error), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (file_final==null){
                    Toasty.error(UploadActivity.this, getResources().getString(R.string.image_upload_error), Toast.LENGTH_SHORT).show();
                    return;
                }
                upload(CAMERA_REQUEST_IMAGE_1);
            }
        });
    }

    private void SelectWallpaper() {
        if (ContextCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UploadActivity.this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }else{

            Intent intent3 = new Intent(this, AudioPickActivity.class);
            intent3.putExtra(AudioPickActivity.IS_NEED_RECORDER, false);
            intent3.putExtra(Constant.MAX_NUMBER, 1);
            startActivityForResult(intent3, Constant.REQUEST_CODE_PICK_AUDIO);
        }
    }

    private void initView() {

        this.circle_image_view_upload_user=(CircleImageView) findViewById(R.id.circle_image_view_upload_user);
        this.edit_text_upload_title=(EditText) findViewById(R.id.edit_text_upload_title);
        this.button_save_upload =(FloatingActionButton) findViewById(R.id.button_save_upload);
        button_save_upload.hide();

        this.fab_upload=(FloatingActionButton) findViewById(R.id.fab_upload);
        this.relative_layout_upload=(RelativeLayout) findViewById(R.id.relative_layout_upload);
        getCategory();



        PrefManager prf= new PrefManager(getApplicationContext());


        Picasso.with(getApplicationContext()).load(prf.getString("IMAGE_USER").toString()).placeholder(R.drawable.profile).error(R.drawable.profile).resize(200,200).centerCrop().into(circle_image_view_upload_user);


        this.progress_bar_progress_wallpaper_upload=(ProgressBar) findViewById(R.id.progress_bar_progress_wallpaper_upload);
        this.text_view_progress_progress_wallpaper_upload=(TextView) findViewById(R.id.text_view_progress_progress_wallpaper_upload);
        this.relative_layout_progress_wallpaper_upload=(RelativeLayout) findViewById(R.id.relative_layout_progress_wallpaper_upload);

        gridLayoutManagerCategorySelect = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);

        recycle_view_selected_category= (RecyclerView) findViewById(R.id.recycle_view_selected_category);

        hideProgress();
    }
    private void getCategory() {
        register_progress= ProgressDialog.show(this, null,getResources().getString(R.string.operation_progress), true);

        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<List<Category>> call = service.categoryAll();
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if(response.isSuccessful()){
                    categoriesListObj.clear();
                    for (int i = 0;i<response.body().size();i++){
                        categoriesListObj.add(response.body().get(i));
                    }
                    categorySelectAdapter = new CategorySelectAdapter(UploadActivity.this, categoriesListObj, true, UploadActivity.this);
                    recycle_view_selected_category.setHasFixedSize(true);
                    recycle_view_selected_category.setAdapter(categorySelectAdapter);
                    recycle_view_selected_category.setLayoutManager(gridLayoutManagerCategorySelect);
                    register_progress.dismiss();

                }else {
                    register_progress.dismiss();
                    Snackbar snackbar = Snackbar
                            .make(relative_layout_upload, getResources().getString(R.string.no_connexion), Snackbar.LENGTH_INDEFINITE)
                            .setAction(getResources().getString(R.string.retry), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    getCategory();
                                }
                            });
                    snackbar.setActionTextColor(android.graphics.Color.RED);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(android.graphics.Color.YELLOW);
                    snackbar.show();
                }

            }
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                register_progress.dismiss();
                Snackbar snackbar = Snackbar
                        .make(relative_layout_upload, getResources().getString(R.string.no_connexion), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getResources().getString(R.string.retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                getCategory();
                            }
                        });
                snackbar.setActionTextColor(android.graphics.Color.RED);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(android.graphics.Color.YELLOW);
                snackbar.show();
            }
        });
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ArrayList<AudioFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_AUDIO);
            for (int i = 0; i < list.size(); i++) {
                Log.v("v",list.get(i).getPath()+"");
                this.file_final = new File(list.get(i).getPath());
                edit_text_upload_title.setText(file_final.getName());
                this.file_duration = list.get(i).getDuration();
            }
        }

/*
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Audio.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Log.v("v",getMimeType(filePathColumn[0])+"");



        } else {

            Log.i("SonaSys", "resultCode: " + resultCode);
            switch (resultCode) {
                case 0:
                    Log.i("SonaSys", "User cancelled");
                    break;
                case -1:
                    break;
            }
        }
     /*   if (requestCode == PICK_AUDIO && resultCode == RESULT_OK
                && null != data) {

                Uri uploadfileuri = data.getData();
                File file = new File(uploadfileuri.getPath());


                if (file.exists()){
                    Log.i("exists", "YES");

                }

            /*Log.i("SonaSys", "i'm here not cancelled");
            AudioWaveView wave=findViewById(R.id.wave);

            Uri uri = data.getData();
            String src = uri.getPath();

            File source = new File(src);
            String filename = uri.getLastPathSegment();
            File destination = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CustomFolder/" + filename);

            Log.v("exists",src);*/



            /*
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            File file = new File(picturePath);
            if (file.exists()){
                Log.v("exists","YES");
            }else {
                Log.v("exists","NO");
            }
            byte[] soundBytes = null;
            try {
                InputStream inputStream = new FileInputStream(picturePath);
                soundBytes = new byte[inputStream.available()];
                soundBytes = Util.toByteArray(inputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            wave.setWaveColor(Color.parseColor("#007fff"));
            wave.setScaledData(soundBytes);
            wave.setRawData(soundBytes, new OnSamplingListener() {
                @Override
                public void onComplete() {
                }
            });



            if (bitmap_wallpaper != null) {

                ImageView rotate = (ImageView) findViewById(R.id.image_view_wallpaper_image);
                rotate.setImageBitmap(bitmap_wallpaper);
                imageurl = picturePath  ;
            }
            */
       /* } else {

            Log.i("SonaSys", "resultCode: " + resultCode +":"+ RESULT_OK);
            switch (resultCode) {
                case 0:
                    Log.i("SonaSys", "User cancelled");
                    break;
                case -1:
                    break;
            }
        }*/
    }
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    public void upload(final int CODE){
        showProgress();
        PrefManager prf = new PrefManager(getApplicationContext());

        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);

        //File creating from selected URL




        ProgressRequestBody requestFile = new ProgressRequestBody(file_final, UploadActivity.this);

        // create RequestBody instance from file
        // RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =MultipartBody.Part.createFormData("uploaded_file",file_final.getName(), requestFile);
        String id_ser=  prf.getString("ID_USER");
        String key_ser=  prf.getString("TOKEN_USER");

        Call<ApiResponse> request = service.uploadRingtone(body,file_duration,id_ser,key_ser,edit_text_upload_title.getText().toString().trim(),getSelectedCategories());
        request.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.isSuccessful()){
                    Toasty.success(getApplication(),getResources().getString(R.string.ringtone_upload_success),Toast.LENGTH_LONG).show();
                    finish();
                }else{
                    Toasty.error(getApplication(),getResources().getString(R.string.no_connexion),Toast.LENGTH_LONG).show();

                }
                // file.delete();
                // getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                hideProgress();
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toasty.error(getApplication(),getResources().getString(R.string.no_connexion),Toast.LENGTH_LONG).show();
                hideProgress();
            }
        });


    }

    @Override
    public void onProgressUpdate(int percentage) {
        ProgressValue(percentage);
    }

    @Override
    public void onError() {
        hideProgress();
    }

    @Override
    public void onFinish() {
        hideProgress();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
        return;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Category item) {

    }



    public String getSelectedCategories(){
        String categories = "";
        for (int i = 0; i < categorySelectAdapter.getSelectedItems().size(); i++) {
            categories+="_"+categorySelectAdapter.getSelectedItems().get(i).getId();
        }
        Log.v("categories",categories);

        return categories;
    }

    public void ProgressValue(Integer progress){
        progress_bar_progress_wallpaper_upload.setProgress(progress);
        text_view_progress_progress_wallpaper_upload.setText("Loading : " + progress + "%");
    }
    public void hideProgress() {

            Animation c= AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.slide_down);
            c.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    relative_layout_progress_wallpaper_upload.setVisibility(View.GONE);
                    button_save_upload.show();

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }
                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.relative_layout_progress_wallpaper_upload.startAnimation(c);

    }
    public void showProgress(){
        button_save_upload.hide();
        Animation c= AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);
        c.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                relative_layout_progress_wallpaper_upload.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        this.relative_layout_progress_wallpaper_upload.startAnimation(c);
    }

}


