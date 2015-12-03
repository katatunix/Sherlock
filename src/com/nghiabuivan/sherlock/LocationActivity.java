package com.nghiabuivan.sherlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nghiabuivan.sherlock.db.DaoLocation;
import com.nghiabuivan.sherlock.db.SherlockDbHelper;

public class LocationActivity extends Activity {
	
	private int m_locationId;
	private DaoLocation m_location;
	private String m_personFullname;
	
	private TextView m_textViewDatetime;
	private TextView m_textViewDesciption;
	private TextView m_textViewNote;
	private ImageView m_imageViewPhoto;
	
	private Intent m_intentEditLocation;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//
		m_textViewDatetime = (TextView) findViewById(R.id.textView_Location_Datetime);
		m_textViewDesciption = (TextView) findViewById(R.id.textView_Location_Desciption);
		m_textViewNote = (TextView) findViewById(R.id.textView_Location_Note);
		m_imageViewPhoto = (ImageView) findViewById(R.id.imageView_Location_Photo);
		
		//
		Utils.s_needToRefreshLocation = true;
		
		//
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			m_locationId = extras.getInt(Utils.KEY_LOCATION_ID);
			m_personFullname = extras.getString(Utils.KEY_PERSON_FULL_NAME);
		} else {
		}
		
		//
		m_intentEditLocation = new Intent(this, NewLocationActivity.class);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (Utils.s_needToRefreshLocation) {
			SherlockDbHelper.getInstance().openReadDb();
			m_location = SherlockDbHelper.getInstance().getLocationById(m_locationId);
			SherlockDbHelper.getInstance().closeDb();
			
			setTitle(m_location.getDescription());
			m_textViewDatetime.setText(Utils.convertMillisToString(m_location.getTime()));
			m_textViewDesciption.setText(m_location.getDescription());
			m_textViewNote.setText(m_location.getNote());
			byte[] photoBytes = m_location.getPhotoBytes();
			if (photoBytes == null) {
				m_imageViewPhoto.setVisibility(View.GONE);
			} else {
				m_imageViewPhoto.setVisibility(View.VISIBLE);
				Bitmap bm = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
				m_imageViewPhoto.setImageBitmap(bm);
			}
			// Map
			String position = m_location.getPosition();
			if (position != null) {
				double[] t = Utils.makePositionDoubles(position);
				if (t != null && t.length == 2) {
					LatLng pos = new LatLng(t[0], t[1]);
					
					GoogleMap map = ((MapFragment) (getFragmentManager().findFragmentById(R.id.locationDetailMapFrag))).getMap();
					
					map.addMarker(new MarkerOptions().position(pos));
					
				    // Move the camera instantly to hamburg with a zoom of 20
				    map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 20));
				    // Zoom in, animating the camera.
				    map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
				}
			}
			
			Utils.s_needToRefreshLocation = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_location_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
				
			case R.id.action_location_edit:
				m_intentEditLocation.putExtra(Utils.KEY_LOCATION_ID, m_location.getId());
				m_intentEditLocation.putExtra(Utils.KEY_PERSON_FULL_NAME, m_personFullname);
				startActivity(m_intentEditLocation);
				return true;
				
			case R.id.action_location_delete:
				handleButtonDelete();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void handleButtonDelete() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
		.setTitle(getString(R.string.confirm))
		.setMessage(getString(R.string.are_you_sure_to_delete_this_location))
		.setIcon(R.drawable.ic_action_warning)
		.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				SherlockDbHelper.getInstance().openWriteDb();
				SherlockDbHelper.getInstance().deleteLocationById(m_location.getId());
				SherlockDbHelper.getInstance().closeDb();
				
				dialog.dismiss();
				Utils.s_needToRefreshLocationList = true;
				
				finish();
				
			}
		})
		.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

}
