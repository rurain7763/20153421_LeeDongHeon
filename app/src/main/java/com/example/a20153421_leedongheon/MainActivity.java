package com.example.a20153421_leedongheon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> appNames;
    ArrayList<String> description;
    ArrayList<Drawable> images;
    Map<String,ArrayList<String>> usData;

    public void installedApps() throws PackageManager.NameNotFoundException {
        if(appNames == null) appNames = new ArrayList<String>();
        if(images == null) images = new ArrayList<Drawable>();
        if(description == null) description = new ArrayList<String>();

        List<PackageInfo> packageInfos = getPackageManager().getInstalledPackages(0);
        for(PackageInfo pi : packageInfos){
            if((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)==0){
                String appName = pi.applicationInfo.loadLabel(getPackageManager()).toString();
                Drawable image = pi.applicationInfo.loadIcon(getPackageManager());
                Date lastTime = new Date(pi.lastUpdateTime);
                SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String lastUpdateTime = dataFormat.format(lastTime);

                appNames.add(appName);
                images.add(image);
                description.add(lastUpdateTime);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean checkForPermission(){
         AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
         int mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(),getPackageName());


         return mode == AppOpsManager.MODE_ALLOWED;
    }

    public List<UsageStats> getAppUsageStats(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR,-1);

        UsageStatsManager usm = (UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);

        List<UsageStats> qus = usm.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY,cal.getTimeInMillis(),System.currentTimeMillis());

        return qus;
    }

    public Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("App List");

        if(!checkForPermission()){
            Toast.makeText(getApplicationContext(),"사용 권한 허용을 설정하지 않았습니다.",Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }

        try {
            installedApps();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        List<UsageStats> uss = getAppUsageStats();

        usData = new HashMap<String,ArrayList<String>>();

        final PackageManager pm = getPackageManager();

        for(UsageStats us : uss){
            ApplicationInfo ai = null;
            ArrayList<String> usArray = new ArrayList<String>();

            try {
                ai = pm.getApplicationInfo(us.getPackageName(),0);

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if(ai == null) continue;

            Date _lastTimeUsed = new Date(us.getLastTimeUsed());
            Date _totalTimeUsed = new Date(us.getTotalTimeInForeground());
            Date _lastTimeVisible = new Date(us.getLastTimeVisible());

            SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

            String lastTimeUsed = dataFormat.format(_lastTimeUsed);
            String totalTimeUsed = timeFormat.format(_totalTimeUsed);
            String lastTimeVisible = dataFormat.format(_lastTimeVisible);

            usArray.add(lastTimeUsed);
            usArray.add(totalTimeUsed);
            usArray.add(lastTimeVisible);
            usArray.add(us.getPackageName());

            String appName = pm.getApplicationLabel(ai).toString();
            usData.put(appName, usArray);
        }

        listView = findViewById(R.id.appListView);

        final MyAdator adapter = new MyAdator(this,appNames,description,images);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),appNames.get(position),Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(),detail.class);

                intent.putExtra("AppName",appNames.get(position));
                intent.putExtra("Description",description.get(position));
                intent.putExtra("Image",drawableToBitmap(images.get(position)));

                if(usData.containsKey(appNames.get(position))){
                    ArrayList<String> ud = usData.get(appNames.get(position));

                    intent.putExtra("LastTimeUsed",ud.get(0));
                    intent.putExtra("TotalTimeUsed",ud.get(1));
                    intent.putExtra("LastTimeVisible",ud.get(2));
                    intent.putExtra("PackageName",ud.get(3));
                }

                else{
                    intent.putExtra("LastTimeUsed","최근 한달간 사용한 기록이 없거나 앱을 찾을 수 없습니다.");
                    intent.putExtra("TotalTimeUsed","최근 한달간 사용한 기록이 없거나 앱을 찾을 수 없습니다.");
                    intent.putExtra("LastTimeVisible","최근 한달간 사용한 기록이 없거나 앱을 찾을 수 없습니다.");
                }

                startActivity(intent);
            }
        });

        listView.setAdapter(adapter);


    }

    class MyAdator extends ArrayAdapter<String>{
        Context context;
        String[] title;
        String[] description;
        Drawable[] images;

        MyAdator(Context c , String[] title,String[] description,Drawable[] images){
            super(c,R.layout.activity_row,R.id.textView1,title);
            this.context =c;
            this.title = title;
            this.description = description;
            this.images = images;
        }

        MyAdator(Context c , ArrayList<String> title,ArrayList<String> description, ArrayList<Drawable> images){
            super(c,R.layout.activity_row,R.id.textView1,title);
            this.context =c;

            String[] dummyTitle = new String[title.size()];
            String[] dummyDescription = new String[description.size()];
            Drawable[] dummyImage = new Drawable[images.size()];

            this.title = title.toArray(dummyTitle);
            this.images = images.toArray(dummyImage);
            this.description = description.toArray(dummyDescription);

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            LayoutInflater li = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = li.inflate(R.layout.activity_row,parent,false);

            ImageView image = row.findViewById(R.id.image);
            TextView tv1 = row.findViewById(R.id.textView1);
            TextView tv2 = row.findViewById(R.id.textView2);

            if(position < this.images.length) image.setImageDrawable(this.images[position]);
            if(position < this.title.length) tv1.setText(this.title[position]);
            if(position < this.description.length) tv2.setText("마지막 사용 시간: "+this.description[position]);


            return row;
        }
    }
}
