package com.jigwtf.heybill.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.jigwtf.heybill.Config;
import com.jigwtf.heybill.activities.OneContentLinkActivity;
import com.jigwtf.heybill.activities.OneContentProductActivity;
import com.jigwtf.heybill.activities.OneContentTextActivity;
import com.jigwtf.heybill.activities.OneContentVideoActivity;
import com.jigwtf.heybill.activities.ShowWebViewContentActivity;
import com.jigwtf.heybill.adapters.CategoryHorizentalAdapter;
import com.jigwtf.heybill.adapters.ContentHorizentalAdapter;
import com.jigwtf.heybill.models.CategoryModel;
import com.jigwtf.heybill.models.ContentModel;
import com.jigwtf.heybill.utils.AppController;
import com.jigwtf.heybill.utils.Tools;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.jigwtf.heybill.R;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;

public class MainFragment extends Fragment {
    CoordinatorLayout mainCoordinatorLayout;
    SliderLayout topSliderLayout;
    String sliderId;
    String sliderTitle;
    String sliderContentId;
    String sliderContentTypeId;
    String sliderImage;
    private Context context;
    ImageButton btn_f_main_featured_show_all, btn_f_main_latest_show_all, btn_f_main_all_statistics, btn_f_main_country_statistics;
    CardView cvStatistics;
    TextView txt_cases, txt_deaths, txt_recovered, txt_cases_country, txt_deaths_country, txt_recovered_country, txt_total_updated_country, textViewCountry;

    //Category Horizental Variable for volley
    RecyclerView horizentalCategoryRecyclerView;
    RecyclerView.Adapter horCatAdapter;
    RecyclerView.LayoutManager horCatLayoutManager;
    List<CategoryModel> horCatItemModelsList;
    RequestQueue rqHorCatItem;

    //Featured Content Variable for volley
    RecyclerView featuredContentRecyclerView;
    RecyclerView.Adapter featuredAdapter;
    RecyclerView.LayoutManager featuredLayoutManager;
    List<ContentModel> featuredContentModelsList;
    RequestQueue rqFeaturedContent;

    //Latest Content Variable for volley
    RecyclerView latestContentRecyclerView;
    RecyclerView.Adapter latestAdapter;
    RecyclerView.LayoutManager latestLayoutManager;
    List<ContentModel> latestContentModelsList;
    RequestQueue rqLatestContent;

    private ProgressWheel progressWheelInterpolated;

    public MainFragment() { }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        //Set ActionBar Title
        getActivity().setTitle(R.string.app_name);

        mainCoordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.mainCoordinatorLayout);
        //Check internet connection start
        if (!Tools.isNetworkAvailable(getActivity())) {
            Snackbar snackbar = Snackbar.make(mainCoordinatorLayout, R.string.txt_no_internet, Snackbar.LENGTH_LONG)
                    .setAction(R.string.txt_retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Go to last fragment/activity
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    });
            snackbar.setActionTextColor(getResources().getColor(R.color.colorYellow));
            snackbar.show();
        }

        //Material ProgressWheel
        progressWheelInterpolated = (ProgressWheel) view.findViewById(R.id.main_progress_wheel);

        //Total Statistics
        txt_cases = (TextView) view.findViewById(R.id.txt_cases);
        txt_deaths = (TextView) view.findViewById(R.id.txt_deaths);
        txt_recovered = (TextView) view.findViewById(R.id.txt_recovered);

        txt_cases_country = (TextView) view.findViewById(R.id.txt_cases_country);
        txt_deaths_country = (TextView) view.findViewById(R.id.txt_deaths_country);
        txt_recovered_country = (TextView) view.findViewById(R.id.txt_recovered_country);

        textViewCountry = (TextView) view.findViewById(R.id.textViewCountry);
        textViewCountry.setText(((AppController) getActivity().getApplication()).getSetting_default_country());

        sendTotalStatistics();
        sendCountryStatistics();

        //Featured Content List
        //Featured Content List
        btn_f_main_featured_show_all = (ImageButton) view.findViewById(R.id.btn_f_main_featured_show_all);
        btn_f_main_featured_show_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Pass data from Fragment to Fragment
                Bundle bundle = new Bundle();
                bundle.putString("showWhichContent","FeaturedContent");
                bundle.putString("showTitle",getActivity().getString(R.string.txt_featured_title));
                SearchFragment searchFragment = new SearchFragment();
                searchFragment.setArguments(bundle);
                getFragmentManager()
                        .beginTransaction()
                        //.setCustomAnimations(R.anim.enter, R.anim.exit) //Start Animation
                        .replace(R.id.mainCoordinatorLayout, searchFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });


        //Latest Content List
        btn_f_main_latest_show_all = (ImageButton) view.findViewById(R.id.btn_f_main_latest_show_all);
        btn_f_main_latest_show_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Pass data from Fragment to Fragment
                Bundle bundle = new Bundle();
                bundle.putString("showWhichContent","LatestContent");
                bundle.putString("showTitle", getActivity().getString(R.string.txt_latest_title));
                SearchFragment searchFragment = new SearchFragment();
                searchFragment.setArguments(bundle);
                getFragmentManager()
                        .beginTransaction()
                        //.setCustomAnimations(R.anim.enter, R.anim.exit) //Start Animation
                        .replace(R.id.mainCoordinatorLayout, searchFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });


        //Top slider start
        topSliderLayout = (SliderLayout) view.findViewById(R.id.topSlider);
        final Map<String, String> urlImageMap = new TreeMap<>();
        //Start get slider information from server via Volley
        progressWheelInterpolated.setVisibility(View.VISIBLE);

        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response)
            {
                try
                {
                    for (int i = 0; i < response.length(); i++)
                    {
                        JSONObject object = response.getJSONObject(i);
                        sliderId = object.getString("slider_id");
                        sliderTitle = object.getString("slider_title");
                        sliderContentId = object.getString("slider_content_id");
                        sliderContentTypeId = object.getString("slider_content_type_id");
                        sliderImage = object.getString("slider_image");
                        sliderImage = Config.GET_SLIDER_IMG_URL+sliderImage;

                        //Add sliderTitle and sliderImage to SliderLayout
                        TextSliderView textSliderView = new TextSliderView(getActivity());
                        textSliderView
                                .description(sliderTitle)
                                //.empty(R.drawable.slider_pre_loadin)
                                //.error(R.drawable.slider_pre_loadin)
                                .errorDisappear(true)
                                .image(sliderImage)
                                .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                                //.descriptionVisibility(BaseSliderView.Visibility.VISIBLE)
                                .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                    @Override
                                    public void onSliderClick(BaseSliderView baseSliderView) {
                                        String click = baseSliderView.getBundle().getString("click");
                                        String[] arrOfClick = click.split("|", 5);
                                        //Toast.makeText(getActivity(), click, Toast.LENGTH_LONG).show();
                                        //Open One Content Activity and pass variable: sliderContentId & sliderContentTypeId
                                        //arrOfClick[0] = sliderContentId
                                        //arrOfClick[1] = |
                                        //arrOfClick[2] = sliderContentTypeId
                                        if(arrOfClick[2].equals("1")) { //Video
                                            Intent intent = new Intent(getActivity(), OneContentVideoActivity.class);
                                            intent.putExtra("contentId", arrOfClick[0]); //click has content_id value
                                            intent.putExtra("buttonText", getString(R.string.txt_button_play_video));
                                            startActivity(intent);
                                            //Toast.makeText(getActivity(), click, Toast.LENGTH_LONG).show();

                                        }else if(arrOfClick[2].equals("3")) { //Game
                                            Intent intent = new Intent(getActivity(), OneContentLinkActivity.class);
                                            intent.putExtra("contentId", arrOfClick[0]); //click has content_id value
                                            intent.putExtra("buttonText", getString(R.string.txt_button_play_game));
                                            startActivity(intent);

                                        }else if(arrOfClick[2].equals("4")) { //Text
                                            Intent intent = new Intent(getActivity(), OneContentTextActivity.class);
                                            intent.putExtra("contentId", arrOfClick[0]); //click has content_id value
                                            intent.putExtra("buttonText", getString(R.string.txt_button_open));
                                            startActivity(intent);

                                        }else if(arrOfClick[2].equals("7")) { //Location
                                            Intent intent = new Intent(getActivity(), OneContentProductActivity.class);
                                            intent.putExtra("contentId", arrOfClick[0]); //click has content_id value
                                            intent.putExtra("buttonText", getString(R.string.txt_button_show));
                                            startActivity(intent);
                                        }
                                    }
                                });
                                //.descriptionLayoutColor(Color.parseColor("#99999999"));

                        textSliderView.bundle(new Bundle());
                        //textSliderView.getBundle().putString("click", sliderContentId);
                        textSliderView.getBundle().putString("click", sliderContentId+"|"+sliderContentTypeId);

                        topSliderLayout.addSlider(textSliderView);
                        topSliderLayout.setPresetTransformer(SliderLayout.Transformer.Default);
                        topSliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                        topSliderLayout.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Visible);
                        topSliderLayout.setDuration(4000);
                    }
                }
                catch (Exception e)
                {

                }
                progressWheelInterpolated.setVisibility(View.GONE);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                progressWheelInterpolated.setVisibility(View.GONE);
                Toast.makeText(getActivity(), R.string.txt_no_slider_found, Toast.LENGTH_LONG).show();
            }
        };

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, Config.GET_SLIDER_URL + "?api_key=" + Config.API_KEY, null, listener, errorListener);
        AppController.getInstance().addToRequestQueue(request);
        //.End get slider information from server via Volley
        //.Top slider end

        //Category Horizental RecyclerView start
        /*rqHorCatItem = Volley.newRequestQueue(getActivity());
        horizentalCategoryRecyclerView = (RecyclerView) view.findViewById(R.id.rv_horizental_category);
        horizentalCategoryRecyclerView.setHasFixedSize(true);
        horCatLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        horizentalCategoryRecyclerView.setLayoutManager(horCatLayoutManager);
        horCatItemModelsList = new ArrayList<>();
        sendCategoryHorizentalRequest();*/
        //Category Horizental RecyclerView start
        rqHorCatItem = Volley.newRequestQueue(getActivity());
        horizentalCategoryRecyclerView = (RecyclerView) view.findViewById(R.id.rv_horizental_category);
        horizentalCategoryRecyclerView.setHasFixedSize(true);
        //horCatLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        horCatLayoutManager = new GridLayoutManager(getActivity(), 2); // 2 Grid
        horizentalCategoryRecyclerView.setLayoutManager(horCatLayoutManager);
        horCatItemModelsList = new ArrayList<>();
        sendCategoryHorizentalRequest();

        //Featured contentRecyclerView start
        //Horizental list gamebox for Featured
        rqFeaturedContent = Volley.newRequestQueue(getActivity());
        featuredContentRecyclerView = (RecyclerView) view.findViewById(R.id.rv_f_main_featured_content);
        featuredContentRecyclerView.setHasFixedSize(true);
        featuredLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        featuredContentRecyclerView.setLayoutManager(featuredLayoutManager);
        featuredContentModelsList = new ArrayList<>();
        sendFeaturedContentRequest();


        //Latest contentRecyclerView start
        //Horizental list items by Latest
        rqLatestContent = Volley.newRequestQueue(getActivity());
        latestContentRecyclerView = (RecyclerView) view.findViewById(R.id.rv_f_main_latest_content);
        latestContentRecyclerView.setHasFixedSize(true);
        latestLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        latestContentRecyclerView.setLayoutManager(latestLayoutManager);
        latestContentModelsList = new ArrayList<>();
        sendLatestContentRequest();

        /*cvStatistics = (CardView) view.findViewById(R.id.cvStatistics);
        cvStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send contentURL to ShowWebViewContentActivity
                Intent intentStat = new Intent(getActivity(), ShowWebViewContentActivity.class);
                intentStat.putExtra("contentTitle", getString(R.string.txt_real_time_statistics));
                intentStat.putExtra("contentUrl", getString(R.string.txt_real_time_statistics_url));
                intentStat.putExtra("contentCached", "1");
                intentStat.putExtra("contentOrientation", "1");
                intentStat.putExtra("contentActionBar", "1");
                startActivity(intentStat);
            }
        });*/


        //All Statistics
        btn_f_main_all_statistics = (ImageButton) view.findViewById(R.id.btn_f_main_all_statistics);
        btn_f_main_all_statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentStat = new Intent(getActivity(), ShowWebViewContentActivity.class);
                intentStat.putExtra("contentTitle", getString(R.string.txt_real_time_statistics));
                intentStat.putExtra("contentUrl", Config.CORONA_VIRUS_ALL_COUNTRIES_WEB);
                intentStat.putExtra("contentCached", "1");
                intentStat.putExtra("contentOrientation", "1");
                intentStat.putExtra("contentActionBar", "1");
                startActivity(intentStat);
            }
        });


        //Country Statistics
        btn_f_main_country_statistics = (ImageButton) view.findViewById(R.id.btn_f_main_country_statistics);
        btn_f_main_country_statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentStat = new Intent(getActivity(), ShowWebViewContentActivity.class);
                intentStat.putExtra("contentTitle", getString(R.string.txt_real_time_statistics));
                intentStat.putExtra("contentUrl", Config.CORONA_VIRUS_ALL_ONE_COUNTRY_WEB+((AppController) getActivity().getApplication()).getSetting_default_country());
                intentStat.putExtra("contentCached", "0");
                intentStat.putExtra("contentOrientation", "1");
                intentStat.putExtra("contentActionBar", "1");
                startActivity(intentStat);
            }
        });


        return view;
    }


    //==========================================================================//
    public void sendCategoryHorizentalRequest() {
        progressWheelInterpolated.setVisibility(View.VISIBLE);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Config.GET_MAIN_CATEGORY_URL + "?api_key=" + Config.API_KEY, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() == 0)
                {
                    //No result found!
                    Toast.makeText(getActivity(), R.string.txt_no_result, Toast.LENGTH_SHORT).show();
                }
                for(int i = 0; i < response.length(); i++){
                    CategoryModel categoryModel = new CategoryModel();

                    try {
                        JSONObject jsonObject = response.getJSONObject(i);

                        categoryModel.setCategoryId(jsonObject.getString("category_id"));
                        categoryModel.setCategoryImage(jsonObject.getString("category_image"));
                        categoryModel.setCategoryTitle(jsonObject.getString("category_title"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    horCatItemModelsList.add(categoryModel);

                }

                horCatAdapter = new CategoryHorizentalAdapter(getActivity(), horCatItemModelsList);
                horizentalCategoryRecyclerView.setAdapter(horCatAdapter);
                progressWheelInterpolated.setVisibility(View.GONE);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressWheelInterpolated.setVisibility(View.GONE);
                Log.i("BlueDev Volley Error: ", error+"");
                //Toast.makeText(getActivity(), R.string.txt_no_result, Toast.LENGTH_SHORT).show();
            }
        });

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(20000,2,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rqHorCatItem.add(jsonArrayRequest);
    }


    //==========================================================================//
    public void sendFeaturedContentRequest() {
        progressWheelInterpolated.setVisibility(View.VISIBLE);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Config.GET_FEATURED_CONTENT_URL+"?limit=15&last_id=0&api_key=" + Config.API_KEY, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() == 0)
                {
                    //No result found!
                    //Toast.makeText(getActivity(), R.string.txt_no_result, Toast.LENGTH_SHORT).show();
                }
                for(int i = 0; i < response.length(); i++){
                    ContentModel contentModel = new ContentModel();

                    try {
                        JSONObject jsonObject = response.getJSONObject(i);

                        contentModel.setContent_id(jsonObject.getString("content_id"));
                        contentModel.setContent_title(jsonObject.getString("content_title"));
                        contentModel.setContent_image(jsonObject.getString("content_image"));
                        contentModel.setContent_publish_date(jsonObject.getString("content_publish_date"));
                        contentModel.setCategory_title(jsonObject.getString("category_title"));
                        contentModel.setContent_duration(jsonObject.getString("content_duration"));
                        contentModel.setContent_viewed(jsonObject.getString("content_viewed"));
                        contentModel.setContent_url(jsonObject.getString("content_url"));
                        contentModel.setContent_type_title(jsonObject.getString("content_type_title"));
                        contentModel.setContent_type_id(jsonObject.getString("content_type_id"));
                        contentModel.setContent_user_role_id(jsonObject.getString("content_user_role_id"));
                        contentModel.setContent_orientation(jsonObject.getString("content_orientation"));
                        contentModel.setUser_role_title(jsonObject.getString("user_role_title"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    featuredContentModelsList.add(contentModel);
                }

                featuredAdapter = new ContentHorizentalAdapter(getActivity(), featuredContentModelsList);
                featuredContentRecyclerView.setAdapter(featuredAdapter);
                progressWheelInterpolated.setVisibility(View.GONE);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressWheelInterpolated.setVisibility(View.GONE);
                Log.i("BlueDev Volley Error: ", error+"");
                //Toast.makeText(getActivity(), R.string.txt_no_result, Toast.LENGTH_SHORT).show();
            }
        });

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(25000,2,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rqFeaturedContent.add(jsonArrayRequest);
    }


    //==========================================================================//
    public void sendLatestContentRequest() {
        progressWheelInterpolated.setVisibility(View.VISIBLE);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Config.GET_LATEST_CONTENT_URL+"?limit=15&last_id=0&api_key=" + Config.API_KEY, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() == 0)
                {
                    //No result found!
                    //Toast.makeText(getActivity(), R.string.txt_no_result, Toast.LENGTH_SHORT).show();
                }
                for(int i = 0; i < response.length(); i++){
                    ContentModel contentModel = new ContentModel();

                    try {
                        JSONObject jsonObject = response.getJSONObject(i);

                        contentModel.setContent_id(jsonObject.getString("content_id"));
                        contentModel.setContent_title(jsonObject.getString("content_title"));
                        contentModel.setContent_image(jsonObject.getString("content_image"));
                        contentModel.setContent_publish_date(jsonObject.getString("content_publish_date"));
                        contentModel.setCategory_title(jsonObject.getString("category_title"));
                        contentModel.setContent_duration(jsonObject.getString("content_duration"));
                        contentModel.setContent_viewed(jsonObject.getString("content_viewed"));
                        contentModel.setContent_url(jsonObject.getString("content_url"));
                        contentModel.setContent_type_title(jsonObject.getString("content_type_title"));
                        contentModel.setContent_type_id(jsonObject.getString("content_type_id"));
                        contentModel.setContent_user_role_id(jsonObject.getString("content_user_role_id"));
                        contentModel.setContent_orientation(jsonObject.getString("content_orientation"));
                        contentModel.setUser_role_title(jsonObject.getString("user_role_title"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    latestContentModelsList.add(contentModel);

                }

                latestAdapter = new ContentHorizentalAdapter(getActivity(), latestContentModelsList);
                latestContentRecyclerView.setAdapter(latestAdapter);
                progressWheelInterpolated.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressWheelInterpolated.setVisibility(View.GONE);
                Log.i("BlueDev Volley Error: ", error+"");
                //Toast.makeText(getActivity(), R.string.txt_error, Toast.LENGTH_SHORT).show();
            }
        });

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(25000,2,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rqLatestContent.add(jsonArrayRequest);
    }


    //==========================================================================//
    public void sendTotalStatistics() {
        progressWheelInterpolated.setVisibility(View.VISIBLE);
        DecimalFormat formatter = new DecimalFormat("#,###,###");

        JsonObjectRequest jsonObjectRequestAll = new JsonObjectRequest(Request.Method.GET, Config.CORONA_VIRUS_ALL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            txt_cases.setText(formatter.format(Integer.parseInt(response.getString("cases"))));
                            txt_deaths.setText(formatter.format(Integer.parseInt(response.getString("deaths"))));
                            txt_recovered.setText(formatter.format(Integer.parseInt(response.getString("recovered"))));

                            progressWheelInterpolated.setVisibility(View.GONE);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        progressWheelInterpolated.setVisibility(View.GONE);
                    }
                });

        jsonObjectRequestAll.setRetryPolicy(new DefaultRetryPolicy(10000,2,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(jsonObjectRequestAll);
    }


    //==========================================================================//
    public void sendCountryStatistics() {
        progressWheelInterpolated.setVisibility(View.VISIBLE);
        DecimalFormat formatter = new DecimalFormat("#,###,###");

        JsonObjectRequest jsonObjectRequestCountry = new JsonObjectRequest(Request.Method.GET, Config.CORONA_VIRUS_ONE_COUNTRY+((AppController) getActivity().getApplication()).getSetting_default_country(), null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    txt_cases_country.setText(formatter.format(Integer.parseInt(response.getString("cases"))));
                    txt_deaths_country.setText(formatter.format(Integer.parseInt(response.getString("deaths"))));
                    txt_recovered_country.setText(formatter.format(Integer.parseInt(response.getString("recovered"))));
                    txt_total_updated_country.setText(Config.UnixToHuman(response.getString("updated")));

                    progressWheelInterpolated.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                progressWheelInterpolated.setVisibility(View.GONE);
            }
        });

        jsonObjectRequestCountry.setRetryPolicy(new DefaultRetryPolicy(10000,2,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(jsonObjectRequestCountry);
    }

}