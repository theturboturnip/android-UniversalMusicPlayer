package com.turboturnip.turnipmusic.frontend.roots;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.turboturnip.turnipmusic.R;
import com.turboturnip.turnipmusic.frontend.roots.library.MusicBrowserActivity;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionsActivity extends Activity {
	protected static Map<String, String> permissions = new ConcurrentHashMap<>(); // Map of permission : explanation
	private static final int GET_PERMISSIONS_CODE = 0;

	private Button permissionButton = null;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		permissions.put(Manifest.permission.READ_EXTERNAL_STORAGE, "This app needs to read external storage to see your music!");

		if (!hasPermissions()){
			getPermissions();
		}else
			onReceivePermissions();
	}

	private boolean hasPermissions(){
		for (String permission : permissions.keySet()) {
			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
				return false;
		}
		return true;
	}
	private void getPermissions(){
		ArrayList<String> shouldShow = new ArrayList<>();
		for (String permission : permissions.keySet()) {
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
				shouldShow.add(permissions.get(permission));
		}
		if (shouldShow.size() > 0){
			setContentView(R.layout.permissions_layout);
			for (String explanation : shouldShow)
				((TextView)findViewById(R.id.permissions_text)).setText(explanation);
			permissionButton = findViewById(R.id.permission_accept_button);
			permissionButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					requestPermissions();
				}
			});
		}else{
			requestPermissions();
		}
	}
	private void requestPermissions(){
		ActivityCompat.requestPermissions(this, permissions.keySet().toArray(new String[permissions.size()]), GET_PERMISSIONS_CODE);
	}
	@Override
	public void onRequestPermissionsResult(int requestCode,
	                                       @NonNull String permissions[], @NonNull int[] grantResults) {
		switch (requestCode) {
			case GET_PERMISSIONS_CODE: {
				for (int result : grantResults){
					if (result == PackageManager.PERMISSION_DENIED){
						getPermissions();
						return;
					}
				}
				// We have the permissions we need!
				if (permissionButton != null)
					permissionButton.setOnClickListener(null);
				onReceivePermissions();
			}
		}
	}


	void onReceivePermissions(){
		Intent intent = new Intent();
		intent.setClass(this, MusicBrowserActivity.class);
		startActivity(intent);
		finish();
	}
}
