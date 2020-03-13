package com.tqc.gdd03;


import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;


public class GDD03 extends Activity
{
  public static boolean bIfDebug = false;
  public static String TAG = "HIPPO_DEBUG";
  private TextView mTextView01;
  private EditText mEditText01,mEditText02,mEditText03;
  private Button mButton01, mButton02, mButton03;
  private LinearLayout mLinearLayout01;

  // http://data.taipei/
  private String strAddress = "";
  // The BroadcastReceiver that tracks network connectivity changes.
  private NetworkReceiver receiver = new NetworkReceiver();
  public static final String WIFI = "Wi-Fi";
  public static final String ANY = "Any";
  private static boolean wifiConnected = false;
  private static boolean mobileConnected = false;
  public static boolean refreshDisplay = true;
  public static String sPref = null;
  private static boolean bIfAllowDownload = false;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    init();
    if(checkPermission(GDD03.this))
    {
      bIfAllowDownload = true;
    }
    else
    {
      bIfAllowDownload = false;
    }
  }
  public static boolean checkPermission(final Activity activity)
  {
    if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
    {
      ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.EXTRA_WRITE_STORAGE);
      bIfAllowDownload = false;
      return false;
    }
    bIfAllowDownload = true;
    return true;
  }
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
  {
    if(grantResults[0]== PackageManager.PERMISSION_GRANTED)
    {
      Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
      bIfAllowDownload = true;
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  private void init()
  {
    mTextView01 = (TextView) findViewById(R.id.main_textView1);
    mEditText01 = (EditText) findViewById(R.id.main_editText1);
    mEditText02 = (EditText) findViewById(R.id.main_editText2);
    mEditText03 = (EditText) findViewById(R.id.main_editText3);
    mButton01 = (Button) findViewById(R.id.main_button1);
    mButton02 = (Button) findViewById(R.id.main_button2);
    mButton03 = (Button) findViewById(R.id.main_button3);
    mButton03.setEnabled(false);
    mLinearLayout01 = (LinearLayout)findViewById(R.id.main_linearLayout1);
    strAddress = getString(R.string.str_address);
    mEditText01.setText(strAddress);
    mButton01.setOnClickListener(new Button.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        if (refreshDisplay)
        {
          mTextView01.setText(getString(R.string.str_parsing));
          //  「地址查經緯度」按鈕事件，呼叫DoQueryLatLngTask背景向Google Geocoder 服務取得該地址的經緯度。
          // TO DO

        }
        else
        {
          mTextView01.setText(getString(R.string.connection_error));
        }
      }
    });

    mButton02.setOnClickListener(new Button.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        mTextView01.setText(getString(R.string.app_name));
        strAddress = getString(R.string.str_address);
        mEditText01.setText(strAddress);
        mLinearLayout01.setVisibility(View.GONE);
        mButton01.setEnabled(true);
        mButton03.setEnabled(false);
      }
    });

    mButton03.setOnClickListener(new Button.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        mTextView01.setText(getString(R.string.str_parsing));
        if(mEditText03.getText().toString().length()>1 && mEditText02.getText().toString().length()>1)
        {
          new DoQueryAddressTask().execute(new LatLng(Double.parseDouble(mEditText03.getText().toString()), Double.parseDouble(mEditText02.getText().toString())));
        }
        else
        {
          mTextView01.setText(getString(R.string.err_query_latlng_first));
        }
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings)
    {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public class NetworkReceiver extends BroadcastReceiver
  {
      @Override
      public void onReceive(Context context, Intent intent) {
          ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
          if (connMgr != null)
          {
              NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();
              if(activeNetwork != null && activeNetwork.isConnected())
              {
                  wifiConnected = activeNetwork.isConnected();
                  mobileConnected = activeNetwork.isConnected();
              }
              else
              {
                  wifiConnected = false;
                  mobileConnected = false;
              }
          }
          else
          {
              wifiConnected = false;
              mobileConnected = false;
          }

          // 關閉網路後 (當失去連線)。
          // TO DO
          //Toast.makeText(GDD03.this, String.valueOf(wifiConnected), Toast.LENGTH_SHORT).show();
          if(wifiConnected || mobileConnected)
          {
              Toast.makeText(GDD03.this, R.string.wifi_connected, Toast.LENGTH_LONG).show();
              refreshDisplay = true;
          }
          else
          {
              Toast.makeText(GDD03.this, R.string.lost_connection, Toast.LENGTH_LONG).show();
              refreshDisplay = true;
          }
      }
    }

  // Checks the network connection and sets the wifiConnected and mobileConnected
  // variables accordingly.
  private void updateConnectedFlags()
  {
      ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      if (connMgr != null)
      {
          NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();
          if(activeNetwork != null && activeNetwork.isConnected())
          {
              wifiConnected = activeNetwork.isConnected();
              mobileConnected = activeNetwork.isConnected();
          }
          else
          {
              wifiConnected = false;
              mobileConnected = false;
          }
      }
      else
      {
          wifiConnected = false;
          mobileConnected = false;
      }
  }

  private class DoQueryLatLngTask extends AsyncTask<String, Void, LatLng>
  {
    @Override
    protected LatLng doInBackground(String... urls)
    {
      return getLatLngByAddress(urls[0]);
    }

    @Override
    protected void onPostExecute(LatLng ll)
    {
      mTextView01.setText(getString(R.string.str_parsing_ok));
      mEditText02.setText(""+ll.longitude);
      mEditText03.setText(""+ll.latitude);
      mLinearLayout01.setVisibility(View.VISIBLE);
      mButton01.setEnabled(false);
      mButton03.setEnabled(true);
    }
  }

  private class DoQueryAddressTask extends AsyncTask<LatLng, Void, String>
  {
    @Override
    protected String doInBackground(LatLng... urls)
    {
      LatLng ll = new LatLng(urls[0].latitude,urls[0].longitude);
      return getAddressByGeoPoint(GDD03.this, ll);
    }

    @Override
    protected void onPostExecute(String result)
    {
      mTextView01.setText(getString(R.string.str_parsing_ok));
      mEditText01.setText(result);
      mLinearLayout01.setVisibility(View.VISIBLE);
      mButton03.setEnabled(false);
    }
  }

  private LatLng getLatLngByAddress(String strAddress)
  {
    LatLng ll = null;
    Geocoder geoCoder = new Geocoder(this);
    List<Address> address = null;

    //. 修改DoQueryLatLngTask裡的doInBackground()，呼叫getLatLngByAddress()，利用傳入的地址字串，傳回 LatLng 物件。
    // TO DO




    return ll;
  }

  private String getAddressByGeoPoint(Context context, LatLng ll)
  {
    String strAddress = "";
    try
    {
      if (ll != null)
      {
        double geoLatitude = ll.latitude;
        double geoLongitude = ll.longitude;

        //double dLat = location.getLatitude();
        //double dLng = location.getLongitude();

        Geocoder gc = new Geocoder(context, Locale.getDefault());
        try
        {
          // . 修改getAddressByGeoPoint() 方法，將傳入的LatLng轉成地址回傳 Geocoder 物件
          // TO DO

          List<Address> lstAddress = null;
          StringBuilder sb = new StringBuilder();

          /* 判斷地址是否為多行 */
          if (lstAddress.size() > 0)
          {
            //   自經緯度取得地址（可能有多行地址），取出第一筆，存放於StringBuilder sb當中
            // TO DO


          }
          strAddress = sb.toString();
        }
        catch (Exception e)
        {
          if(bIfDebug)
          {
            e.printStackTrace();
          }
        }
      }
    }
    catch(Exception e)
    {
      if(bIfDebug)
      {
        e.printStackTrace();
      }
    }
    return strAddress;
  }

  @Override
  protected void onStart()
  {
    super.onStart();
    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

    // Retrieves a string value for the preferences. The second parameter
    // is the default value to use if a preference value is not found.
    sPref = sharedPrefs.getString("listPref", "Wi-Fi");
    updateConnectedFlags();
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    IntentFilter filter = new IntentFilter();
    //android.net.ConnectivityManager.CONNECTIVITY_ACTION
    filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
    filter.addAction("android.net.wifi.STATE_CHANGE");

    //<action android:name="android.net.wifi.STATE_CHANGE" />
    receiver = new NetworkReceiver();
    this.registerReceiver(receiver, filter);
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    this.unregisterReceiver(receiver);
  }
}
