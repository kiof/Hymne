package com.kiof.hymne;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.location.GpsStatus;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class Hymne extends Activity implements LocationListener {
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer = null;
    private LocationManager mLocationManager;
    private NmeaListener mNmeaListener;
    private ViewSwitcher mViewSwitcher;

    private static final String MY_COUNTRY = "mycountry";
    private static final String CHECK_VOLUME = "checkvolume";
    private static final String VOLUME_MAX = "volumemax";
    private static final String VOLUME_RESTORE = "volumerestore";
    private static final String KEEP_MY_COUNTRY = "keepmycountry";
    private static final String AUTO_PLAY = "autoplay";
    private static final String SYNCHRO = "synchro";
    private static final String TAG = "HymneActivity";
    private static final String NTP_SERVER = "pool.ntp.org";
    //	private static final String NTP_SERVER = "fr.ntp.org";
//	private static final String NTP_SERVER = "canon.inria.fr";
    private static final int NTP_NB_TRY = 5;
    private static final int NTP_SLEEP_TIME = 1000;
    private static final int TIME_WAIT = 3;
    private static final int RETURN_SETTING = 1;
    private static final int NETWORK_TIMEOUT = 10000;
    private String[] countries;
    private TypedArray flags;
    private TypedArray sounds;
    private int myCountry;
    private int initVolume;
    private long gpsTime = 0, gpsDelta = 0, ntpTime = 0, ntpDelta = 0, newDelta = 0, sysTime = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this.getApplicationContext();
        Resources mResources = this.getResources();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Register NMEA listener
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        createNmeaListener();
//		mLocationManager.addNmeaListener(mNmeaListener);

        // Launch Sntp request
        createSntp();

        setContentView(R.layout.main);

        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//                .addTestDevice("53356E870D99B80A68F8E2DBBFCD28FB")
                .build();
        adView.loadAd(adRequest);

        mViewSwitcher = (ViewSwitcher) findViewById(R.id.profileSwitcher);

        // Get Preferences
        PreferenceManager.setDefaultValues(this, R.xml.setting, false);
        myCountry = mSharedPreferences.getInt(MY_COUNTRY, -1);

        countries = mResources.getStringArray(R.array.countries);
        flags = mResources.obtainTypedArray(R.array.flags);
        sounds = mResources.obtainTypedArray(R.array.sounds);

        Gallery gallery = (Gallery) findViewById(R.id.gallery);
        gallery.setAdapter(new ImageAdapter(this));

        gallery.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                setMyFlag(position);
            }
        });

        if (myCountry >= 0)
            setMyFlag(myCountry);

        // Display change log if new version
        ChangeLog cl = new ChangeLog(this);
        if (cl.firstRun())
            new HtmlAlertDialog(this, R.raw.about, getString(R.string.about_title), android.R.drawable.ic_menu_info_details).show();

        // Audio management for initVolume control
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // Propose to set initVolume to max if it is not loud enough
        initVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (mSharedPreferences.getBoolean(CHECK_VOLUME, false)) {
            if ((2 * initVolume / maxVolume) < 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.volume_title);
                builder.setIcon(android.R.drawable.ic_menu_preferences);
                builder.setMessage(R.string.volume_question);
                builder.setNegativeButton(R.string.volume_no, null);
                builder.setPositiveButton(R.string.volume_yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                        mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_SHOW_UI);
                            }
                        }
                );
                builder.create();
                builder.show();
            }
        } else {
            if (mSharedPreferences.getBoolean(VOLUME_MAX, false)) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_SHOW_UI);
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.setting:
                startActivityForResult(new Intent(Hymne.this, Setting.class), RETURN_SETTING);
                return true;
            case R.id.share:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.share_title));
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title));
                sharingIntent.putExtra(Intent.EXTRA_TEMPLATE, Html.fromHtml(getString(R.string.share_link)));
                sharingIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(getString(R.string.share_link)));
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_with)));
                return true;
            case R.id.about:
                new HtmlAlertDialog(this, R.raw.about, getString(R.string.about_title),
                        android.R.drawable.ic_menu_info_details).show();
                return true;
            case R.id.other:
                Intent otherIntent = new Intent(Intent.ACTION_VIEW);
                otherIntent.setData(Uri.parse(getString(R.string.other_link)));
                startActivity(otherIntent);
                return true;
            case R.id.quit:
                // Create out AlterDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.quit_title);
                builder.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
                builder.setMessage(R.string.quit_message);
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }
                );
                builder.setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(mContext, R.string.goingon, Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaPlayer != null) mMediaPlayer.release();
        mLocationManager.removeNmeaListener(mNmeaListener);
        mLocationManager.removeUpdates(this);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (!mSharedPreferences.getBoolean(CHECK_VOLUME, false)
                && mSharedPreferences.getBoolean(VOLUME_MAX, false)
                && mSharedPreferences.getBoolean(VOLUME_RESTORE, false)) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    initVolume, AudioManager.FLAG_SHOW_UI);
        }
        if (!mSharedPreferences.getBoolean(KEEP_MY_COUNTRY, false)) {
            myCountry = -1;
            // setSetting();
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt(MY_COUNTRY, myCountry);
            editor.apply();
        }
    }

    public void stopHymne(View view) {
        mViewSwitcher.showPrevious();
        if (mMediaPlayer != null) mMediaPlayer.release();
    }

    public void playHymne(View view) {
        long absTime = 0, duration = 0, msec = 0;
        if (myCountry > 0) {
            mViewSwitcher.showNext();

            if (mSharedPreferences.getBoolean(AUTO_PLAY, false)) {
                mMediaPlayer = MediaPlayer.create(mContext, sounds.getResourceId(myCountry, 0));
                if (mMediaPlayer != null) {
                    mMediaPlayer.setLooping(true);
                    duration = mMediaPlayer.getDuration();
                    if (mSharedPreferences.getBoolean(SYNCHRO, false)) {
                        sysTime = System.currentTimeMillis();
                        if (ntpDelta != 0) absTime = sysTime + ntpDelta;
                        else if (gpsDelta != 0) absTime = sysTime + gpsDelta;
                        else absTime = sysTime;
                        msec = absTime % duration;
                        mMediaPlayer.seekTo((int) msec);
                    }
                    mMediaPlayer.start();
                }
                Toast.makeText(mContext, R.string.message_flag, Toast.LENGTH_SHORT).show();
            }

            StringBuilder stat = new StringBuilder();
            stat.append(sysTime + "|" + gpsTime + "|" + gpsDelta + "|" + ntpTime
                    + "|" + ntpDelta + "|" + absTime);
            stat.append("|" + myCountry + "|" + duration + "|" + msec);
            stat.append("|" + System.getProperty("os.name") + "|"
                    + System.getProperty("os.version") + "|"
                    + System.getProperty("os.arch") + "|"
                    + System.getProperty("user.region") + "|"
                    + System.getProperty("http.agent"));
            stat.append("|" + Build.BOARD + "|" + Build.BOOTLOADER + "|"
                    + Build.BRAND + "|" + Build.CPU_ABI + "|" + Build.CPU_ABI2
                    + "|" + Build.DEVICE + "|" + Build.DISPLAY + "|"
                    + Build.FINGERPRINT + "|" + Build.HARDWARE + "|" + Build.HOST
                    + "|" + Build.ID + "|" + Build.MANUFACTURER + "|" + Build.MODEL
                    + "|" + Build.PRODUCT + "|" + Build.RADIO + "|" + Build.SERIAL
                    + "|" + Build.TAGS + "|" + Build.TIME + "|" + Build.TYPE + "|"
                    + Build.UNKNOWN + "|" + Build.USER);
            try {
                stat.append("|" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
                stat.append("|NA");
            }

            Log.d(TAG, "stat : " + stat);
            // postStat("http://kiof.free.fr/stats.php?", stat.toString());
        }
    }

    void postStat(final String url, final String stat) {
        Thread thread = new Thread() {
            public void run() {
                Looper.prepare(); // For Preparing Message Pool for the child
                // Thread
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                HttpResponse httpResponse = null;
                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
//					for (String key : params) { }
                    nameValuePairs.add(new BasicNameValuePair("stat", stat));
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//					Log.d(TAG, "httpPost : " + httpPost.getAllHeaders());

                    // Set TCP_NODELAY to true to reduce request latency (counterpart is increasing bandwidth usage)
//                    HttpParams httpParams = null;
//                    httpParams = httpParams.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true);
//                    httpClient.setParams(httpParams);

                    httpResponse = httpClient.execute(httpPost);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
//					Toast.makeText(mContext, "UnsupportedEncodingException", Toast.LENGTH_SHORT).show();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
//					Toast.makeText(mContext, "ClientProtocolException", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (httpResponse != null) {
//					Toast.makeText(mContext, httpResponse.getStatusLine().toString(), Toast.LENGTH_SHORT).show();
                    InputStream inputStream = null;
                    try {
                        inputStream = httpResponse.getEntity().getContent();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
//						Toast.makeText(mContext, "IllegalStateException", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//					Toast.makeText(mContext, inputStream.toString(), Toast.LENGTH_SHORT).show();
                } else {
//					Toast.makeText(mContext, "httpResponse null", Toast.LENGTH_SHORT).show();
                }
                Looper.loop(); // Loop in the message queue
            }
        };
        thread.start();
    }

    private void createSntp() {
        Thread thread = new Thread() {
            public void run() {
//				Looper.prepare(); // For Preparing Message Pool for the child
                // Thread
                SntpClient client = new SntpClient();
                long ntpMin1 = 0, ntpMin2 = 0, ntpMin3 = 0;

                for (int i = 0; i < NTP_NB_TRY; i++) {
                    ntpTime = 0;
                    newDelta = 0;
                    if (client.requestTime(NTP_SERVER, NETWORK_TIMEOUT)) {
                        ntpTime = client.getNtpTime() + SystemClock.elapsedRealtime() - client.getNtpTimeReference();
                        newDelta = ntpTime - System.currentTimeMillis();
                        Log.d(TAG, "newDelta : " + Long.toString(newDelta));
                    } else {
                        Log.d(TAG, "SntpRequest failed");
                    }

                    if (ntpDelta == 0 || (newDelta != 0 && newDelta < ntpDelta)) {
                        ntpDelta = newDelta;
                    }
                    ;
                    if (ntpMin1 == 0 || (newDelta != 0 && newDelta < ntpMin1)) {
                        ntpMin3 = ntpMin2;
                        ntpMin2 = ntpMin1;
                        ntpMin1 = newDelta;
                    } else {
                        if (ntpMin2 == 0 || (newDelta != 0 && newDelta < ntpMin2)) {
                            ntpMin3 = ntpMin2;
                            ntpMin2 = newDelta;
                        } else {
                            if (ntpMin3 == 0 || (newDelta != 0 && newDelta < ntpMin3)) {
                                ntpMin3 = newDelta;
                            }
                        }
                    }

                    try {
                        Thread.sleep(NTP_SLEEP_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//					Looper.loop(); // Loop in the message queue
                }
                ntpDelta = ntpMin3;
                Log.d(TAG, "ntpMin1 : " + Long.toString(ntpMin1) + ", ntpMin2 : " + Long.toString(ntpMin2) + ", ntpMin3 : " + Long.toString(ntpMin3) + ", ntpDelta : " + Long.toString(ntpDelta));
            }
        };
        thread.start();
    }

    private void createNmeaListener() {
        mNmeaListener = new GpsStatus.NmeaListener() {
            private static final String NMEA = "$GPRMC";

            public void onNmeaReceived(long timestamp, String nmea) {
//				long startNmea = SystemClock.elapsedRealtime();
                // check that this is an RMC string
                if (!nmea.startsWith(NMEA))
                    return;

                Log.d(TAG, "Timestamp : " + timestamp + " NMEA : " + nmea);

                // extract time, date
                String[] tokens = nmea.split(",");
                String utcTime = tokens[1];
                String utcDate = tokens[9];
//				Log.d(TAG, "utcDate : " + utcDate + "utcTime : " + utcTime);
                // parse

                if (!utcTime.equals("") && !utcDate.equals("")) {
                    SimpleDateFormat df = new SimpleDateFormat("HHmmss.S ddMMyy Z");
                    String dateStr = utcTime + " " + utcDate + " +0000";

                    try {
                        Date date = df.parse(dateStr);
                        // do something with date here ...
                        Log.d(TAG, "Date : " + date.toString());
                        gpsTime = date.getTime();
                        gpsDelta = gpsTime - System.currentTimeMillis();
//						gpsDelta = gpsTime - System.currentTimeMillis() - (SystemClock.elapsedRealtime() - startNmea);
                        Log.d(TAG, "gpsTime : " + gpsTime + "(" + gpsDelta + ")");
                        //					Log.d(TAG, "Delta clock : " + (SystemClock.elapsedRealtime() - startNmea));
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                }

                if (gpsTime != 0) {
                    mLocationManager.removeNmeaListener(mNmeaListener);
                    mLocationManager.removeUpdates(Hymne.this);
                    Log.d(TAG, "removeNmeaListener");
                }
            }
        };
    }

    private void setMyFlag(int position) {
        // Save country to preferences
        this.myCountry = position;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(MY_COUNTRY, myCountry);
        editor.apply();

        ImageView imageView = (ImageView) findViewById(R.id.myflag);
        imageView.setImageResource(flags.getResourceId(position, 0));

        ImageView imageView2 = (ImageView) findViewById(R.id.flag);

//		mView.setImageResource(flags.getResourceId(myCountry, 0));
//		mView.setRotation(90);
        // Bypass because setRotation in not available before API level 11
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), flags.getResourceId(myCountry, 0));
        // Getting width & height of the given image.
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        // Setting post rotate to 90
        Matrix mtx = new Matrix();
        mtx.postRotate(90);
        // Rotating Bitmap
        Bitmap rotatedBMP = Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);
        BitmapDrawable bmd = new BitmapDrawable(rotatedBMP);
        imageView2.setImageDrawable(bmd);

        TextView mTextView = (TextView) findViewById(R.id.mycountry);
        mTextView.setText(countries[position]);

        TextView mTextView1 = (TextView) findViewById(R.id.comment);
        mTextView1.setText(R.string.flag_set);
    }

    public class ImageAdapter extends BaseAdapter {
        final int mGalleryItemBackground;
        private final Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
            TypedArray attr = mContext
                    .obtainStyledAttributes(R.styleable.Hymne);
            mGalleryItemBackground = attr.getResourceId(
                    R.styleable.Hymne_android_galleryItemBackground, 0);
            attr.recycle();
        }

        public int getCount() {
            return flags.length();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public int getItemRessourceId(int position) {
            return flags.getResourceId(position, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(mContext);

            imageView.setImageResource(flags.getResourceId(position, 0));
            imageView.setLayoutParams(new Gallery.LayoutParams(300, 200));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setBackgroundResource(mGalleryItemBackground);
//			imageView.setRotation(90);			

            return imageView;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}