package kr.ac.ajouino.intercom;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


public class MainActivity extends Activity {
	ImageView image;
	Button call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        image = (ImageView) findViewById(R.id.image);
        call = (Button) findViewById(R.id.call);
        
        call.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this , CameraActivity.class);
				startActivity(intent);
				
			}
		});
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences("intercom", MODE_PRIVATE);

        if(sharedPreferences.getString("remote-addr", null) == null) {
            Toast.makeText(this, "Not registered yet", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, StandbyActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent servIntent = new Intent(this, HelloServerService.class);
            startService(servIntent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent servIntent = new Intent(this, HelloServerService.class);
        stopService(servIntent);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 0){
			Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
			
			// 방문기록 남기는 팝업 액티비티 띄워야함
            // 제거
//			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setTitle("Guest History");
//			builder.setMessage("방문기록을 남기시겠습니까?");
//			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					// TODO Auto-generated method stub
//					dialog.dismiss();
//
//					// 방문기록 남기는 액티비티로 전환
//					Intent i = new Intent(MainActivity.this, CameraActivity.class);
//					startActivityForResult(i, 1);
//
//				}
//			});
//			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					// TODO Auto-generated method stub
//					dialog.dismiss();
//				}
//			})
//			.show();
			
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_standby, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reset) {
            SharedPreferences sharedPreferences = getSharedPreferences("intercom", MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();
            Toast.makeText(this, "Not registered yet", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, StandbyActivity.class);
            startActivity(intent);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
