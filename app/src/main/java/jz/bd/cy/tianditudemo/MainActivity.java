package jz.bd.cy.tianditudemo;

import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.tianditu.android.maps.GeoPoint;
import com.tianditu.android.maps.MapController;
import com.tianditu.android.maps.MapView;
import com.tianditu.android.maps.overlay.MarkerOverlay;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Handler handler = new Handler();

    private Timer timer = new Timer();

    private Runnable randomMarkerRun, mapRefreshRun;

    private MapView mapView;

    private SharedPreferences pref;

    private double dLat, dLon, dSpan;

    private Button stopRefreshBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();

        useHandler();

//        useTimer();

    }

    private void initData() {
        stopRefreshBtn = (Button) findViewById(R.id.stopRefresh);
        Button continueRefreshBtn = (Button) findViewById(R.id.continueRefresh);
        stopRefreshBtn.setOnClickListener(this);
        continueRefreshBtn.setOnClickListener(this);
        mapView = (MapView) findViewById(R.id.mapView_main_activity);
        mapView.setBuiltInZoomControls(true);
        MapController controller = mapView.getController();
        GeoPoint point = new GeoPoint((int) (39.892616 * 1E6), (int) (116.359291 * 1E6));
        controller.setCenter(point);
        controller.setZoom(10);

        dLon = 116.359291;
        dLat = 39.892616;
        dSpan = 0.025062;

        pref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void useHandler() {
        randomMarkerRun = new Runnable() {
            @Override
            public void run() {
                randomMarkerDetail();
                handler.postDelayed(this, 2000);
            }
        };
        handler.postDelayed(randomMarkerRun, 1000);

        mapRefreshRun = new Runnable() {
            @Override
            public void run() {
                //更新地图上的数据
                refreshMapViewDetailOriginal();
                //将本runnable继续插入主线程中，再次执行。
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(mapRefreshRun, 1);
    }

    private void useTimer() {
        timer.schedule(new MarkerTask(), 1000, 2000);
        timer.schedule(new MapTask(), 1000, 3000);
    }

    private void refreshMapViewDetailOriginal() {
        int numMarker = pref.getInt("num", 0);
        Log.d("MainActivity", "获取到的缓存数量为:" + numMarker);
        mapView.removeAllOverlay();
        MarkerOverlay[] marker = new MarkerOverlay[numMarker];
        for (int i = 0; i < numMarker; i++) {
            GeoPoint geoPoint;
            if ( i%4 == 0)
                geoPoint = new GeoPoint((int) (dLat * 1E6), (int) ((dLon + dSpan * i) * 1E6));
            else if ( i%4 == 1)
                geoPoint = new GeoPoint((int) ((dLat + dSpan * i) * 1E6), (int) (dLon * 1E6));
            else if ( i%4 == 2)
                geoPoint = new GeoPoint((int) ((dLat - dSpan * i) * 1E6), (int) (dLon * 1E6));
            else
                geoPoint = new GeoPoint((int) (dLat * 1E6), (int) ((dLon - dSpan * i) * 1E6));
            marker[i] = new MarkerOverlay();
            marker[i].setPosition(geoPoint);
            marker[i].setIcon(getResources().getDrawable(R.drawable.poiresult));
            mapView.addOverlay(marker[i]);
        }
        stopRefreshBtn.setText("我改变了" + numMarker);
        mapView.refreshDrawableState();
    }

    private void refreshMapViewDetail() {
        int numMarker = pref.getInt("num", 0);
        Log.d("MainActivity", "获取到的缓存数量为:" + numMarker);
        mapView.removeAllOverlay();
        MarkerOverlay[] marker = new MarkerOverlay[numMarker];
        for (int i = 0; i < numMarker; i++) {
            GeoPoint geoPoint;
            if ( i%4 == 0)
                geoPoint = new GeoPoint((int) (dLat * 1E6), (int) ((dLon + dSpan * i) * 1E6));
            else if ( i%4 == 1)
                geoPoint = new GeoPoint((int) ((dLat + dSpan * i) * 1E6), (int) (dLon * 1E6));
            else if ( i%4 == 2)
                geoPoint = new GeoPoint((int) ((dLat - dSpan * i) * 1E6), (int) (dLon * 1E6));
            else
                geoPoint = new GeoPoint((int) (dLat * 1E6), (int) ((dLon - dSpan * i) * 1E6));
            marker[i] = new MarkerOverlay();
            marker[i].setPosition(geoPoint);
            marker[i].setIcon(getResources().getDrawable(R.drawable.poiresult));
            mapView.addOverlay(marker[i]);
        }
        mapView.refreshDrawableState();
    }

    private void randomMarkerDetail() {
        Random numRandom = new Random();
        int numMarker = numRandom.nextInt(20);
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(MainActivity.this).edit();
        editor.putInt("num", numMarker);
        editor.apply();
        Log.d("MainActivity", "随机的marker的数量为:" + numMarker);
    }

    private void randomMarker() {
        randomMarkerRun = new Runnable() {
            @Override
            public void run() {
                randomMarkerDetail();
                handler.postDelayed(this, 2000);
            }
        };
    }

    private void refreshMap() {
        mapRefreshRun = new Runnable() {
            @Override
            public void run() {
                refreshMapViewDetail();
                handler.postDelayed(this, 3000);
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stopRefresh:
                Log.d("MainActivity", "onClick: stopRefresh");
                timer.cancel();
                handler.removeCallbacks(mapRefreshRun);
                break;
            case R.id.continueRefresh:
                Log.d("MainActivity", "onClick: continueRefresh");
                Timer timer1 = new Timer();
                timer1.schedule(new MarkerTask(), 1000, 2000);
                timer1.schedule(new MapTask(), 1, 3000);
                handler.postDelayed(mapRefreshRun, 1);
                break;
            default:
                break;
        }
    }

    private class MapTask extends TimerTask {

        @Override
        public void run() {
            refreshMapViewDetail();
        }
    }

    private class MarkerTask extends TimerTask {

        @Override
        public void run() {
            randomMarkerDetail();
        }
    }
}
