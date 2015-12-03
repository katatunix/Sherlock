package com.nghiabuivan.sherlock;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nghiabuivan.sherlock.db.DaoLocation;
import com.nghiabuivan.sherlock.db.SherlockDbHelper;
import com.nghiabuivan.sherlock.location.LocationHelper;
import com.nghiabuivan.sherlock.location.LocationObserver;

public class NewLocationActivity extends Activity {
	
	private int m_personId;
	private String m_personFullName;
	private int m_locationId;
	private boolean m_isEdit;
	
	private DaoLocation m_location;
	
	private TextView m_textViewLocationFor;
	private EditText m_editTextDescription;
	private Button m_buttonDetectLocation;
	private EditText m_editTextNote;
	private EditText m_editTextDatetime;
	private Button m_buttonTakePhoto;
	private ImageView m_imageViewLocationPhoto;
	private ProgressBar m_progressBarDetectLocation;
	
	private Bitmap m_bitmap;
	
	private boolean m_isDetectingLocation = false;
	
	private Intent m_intentTakePicture;
	
	private Menu m_menu = null;
	
	//--------------------------------
	private GoogleMap m_map;
	private Marker m_curMarker = null;
	//--------------------------------

	private LocationObserver m_locationObserver = new LocationObserver() {
		public void onLocationDetected(Location location) {
			handleLocationDetected(location);
		}
	};
	
	private String getLocDesc(double lat, double lng) {
		return Utils.getLocationDesc(this, lat, lng);
	}
	
	private void setMapToPos(double lat, double lng) {
		LatLng pos = new LatLng(lat, lng);
		if (m_curMarker == null) {
			m_curMarker = m_map.addMarker(new MarkerOptions().position(pos));
		} else {
			m_curMarker.setPosition(pos);
		}
		
	    // Move the camera instantly to hamburg with a zoom of 20
	    m_map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 20));
	    // Zoom in, animating the camera.
	    m_map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
		// 
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_location);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//
		m_map = ((MapFragment) (getFragmentManager().findFragmentById(R.id.locationMapFrag))).getMap();
		
		//
		m_textViewLocationFor = (TextView) findViewById(R.id.textViewLocation_For);
		m_editTextDescription = (EditText) findViewById(R.id.editText_Location_Description);
		m_buttonDetectLocation = (Button) findViewById(R.id.button_DetectLocation);
		m_editTextNote = (EditText) findViewById(R.id.editText_Location_Note);
		m_editTextDatetime = (EditText) findViewById(R.id.editText_Location_Time);
		m_buttonTakePhoto = (Button) findViewById(R.id.buttonTakePhoto);
		m_imageViewLocationPhoto = (ImageView) findViewById(R.id.imageView_NewLocation_Photo);
		m_progressBarDetectLocation = (ProgressBar) findViewById(R.id.progressBarDetectLocation);
		
		if (!Utils.s_hasCamera) {
			m_buttonTakePhoto.setVisibility(View.GONE);
		}
		
		m_bitmap = null;
		
		m_intentTakePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		m_buttonDetectLocation.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				handleButtonDetectLocation();
			}
		});
		
		m_buttonTakePhoto.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (m_bitmap == null) {
				    startActivityForResult(m_intentTakePicture, 1);
				} else {
					m_bitmap = null;
					m_imageViewLocationPhoto.setImageBitmap(null);
					m_buttonTakePhoto.setText(R.string.take_a_photo);
				}
			}
		});
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			m_isEdit = extras.containsKey(Utils.KEY_LOCATION_ID);
			
			if (m_isEdit) {
				m_locationId = extras.getInt(Utils.KEY_LOCATION_ID);
				SherlockDbHelper.getInstance().openReadDb();
				m_location = SherlockDbHelper.getInstance().getLocationById(m_locationId);
				SherlockDbHelper.getInstance().closeDb();
				
				m_editTextDescription.setText(m_location.getDescription());
				m_editTextNote.setText(m_location.getNote());
				m_editTextDatetime.setText(Utils.convertMillisToString(m_location.getTime()));
				
				byte[] photoBytes = m_location.getPhotoBytes();
				if (photoBytes != null) {
					m_bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
					m_buttonTakePhoto.setText(R.string.remove_photo);
				}
				
				m_imageViewLocationPhoto.setImageBitmap(m_bitmap);
				
				//
				// Map
				String position = m_location.getPosition();
				if (position != null) {
					double[] t = Utils.makePositionDoubles(position);
					if (t != null && t.length == 2) {
						setMapToPos(t[0], t[1]);
					}
				}
				
				
				setTitle(getString(R.string.title_activity_edit_location));
			} else {
				m_personId = extras.getInt(Utils.KEY_PERSON_ID);
			}
			
			m_personFullName = extras.getString(Utils.KEY_PERSON_FULL_NAME);
			
			m_textViewLocationFor.setText(getString(R.string.for_sth, m_personFullName));
			
			
		} else {
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == Activity.RESULT_CANCELED) return;
		Bundle extras = intent.getExtras();
	    m_bitmap = (Bitmap) extras.get("data");
	    if (m_bitmap != null) {
		    m_imageViewLocationPhoto.setImageBitmap(m_bitmap);
		    m_buttonTakePhoto.setText(R.string.remove_photo);
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_new_location_actions, menu);
		m_menu = menu;
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_new_location_cancel:
			case android.R.id.home:
				handleButtonCancel();
				return true;
				
			case R.id.action_new_location_done:
				handleButtonDone();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void handleButtonDone() {
		String messageError = "";
		
		String description = m_editTextDescription.getText().toString().trim();
		if (description.length() == 0) {
			if (messageError.length() > 0) {
				messageError += "\n";
			}
			messageError += getString(R.string.name_desc_not_blank);
		}
		
		String note = m_editTextNote.getText().toString().trim();
		if (note.length() == 0) {
			if (messageError.length() > 0) {
				messageError += "\n";
			}
			messageError += getString(R.string.note_not_blank);
		}
		
		String datetimeStr = m_editTextDatetime.getText().toString().trim();
		long datetimeMillis = 0L;
		if (datetimeStr.length() == 0) {
			datetimeMillis = System.currentTimeMillis();
		} else {
			datetimeMillis = Utils.convertStringToMillis(datetimeStr);
			if (datetimeMillis == 0L) {
				if (messageError.length() > 0) {
					messageError += "\n";
				}
				messageError += getString(R.string.datetime_in_valid_format);
			}
		}
		
		byte[] photoBytes = null;
		if (m_bitmap != null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			m_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			photoBytes = stream.toByteArray();
		}
		
		if (messageError.length() > 0) {
			Utils.showErrorMessage(this, messageError);
		} else {
			String position = null;
			if (m_curMarker != null) {
				LatLng p = m_curMarker.getPosition();
				if (p != null) {
					position = Utils.makePositionString(p.latitude, p.longitude);
				}
			}
			
			SherlockDbHelper.getInstance().openWriteDb();
			if (m_isEdit) {
				SherlockDbHelper.getInstance().editLocation(m_locationId, position, description, datetimeMillis, note, photoBytes);
				Utils.s_needToRefreshLocation = true;
			} else {
				SherlockDbHelper.getInstance().addLocation(m_personId, position, description, datetimeMillis, note, photoBytes);
			}
			
			SherlockDbHelper.getInstance().closeDb();
			
			Utils.s_needToRefreshLocationList = true; // always fresh location list
			
			handleButtonCancel();
		}
	}

	private void handleButtonCancel() {
		if (m_isDetectingLocation) {
			LocationHelper.getInstance().stopDetectLocation();
		}
		Utils.hideKeyboard(this, m_editTextDescription);
		
		finish();
	}
	
	private void handleButtonDetectLocation() {
		if (m_isDetectingLocation) {
			// Stop
			m_progressBarDetectLocation.setVisibility(View.GONE);
			m_editTextDescription.setVisibility(View.VISIBLE);
			m_buttonDetectLocation.setText(R.string.detect_current_location);
			m_menu.findItem(R.id.action_new_location_done).setVisible(true);
			
			LocationHelper.getInstance().stopDetectLocation();
			
			m_isDetectingLocation = false;
		} else {
			// Start
			if (LocationHelper.getInstance().startDetectLocation(this, m_locationObserver)) {
				m_progressBarDetectLocation.setVisibility(View.VISIBLE);
				m_editTextDescription.setVisibility(View.GONE);
				m_buttonDetectLocation.setText(R.string.cancel_detect);
				m_menu.findItem(R.id.action_new_location_done).setVisible(false);
				
				m_isDetectingLocation = true;
			} else {
				Utils.showErrorMessage(this, getString(R.string.cannot_detect_current_location));
			}
		}
	}
	
	private void handleLocationDetected(Location location) {
		m_progressBarDetectLocation.setVisibility(View.GONE);
		m_editTextDescription.setVisibility(View.VISIBLE);
		m_buttonDetectLocation.setText(R.string.detect_current_location);
		m_menu.findItem(R.id.action_new_location_done).setVisible(true);
		
		m_isDetectingLocation = false;
		
		if (location == null) {
			Utils.showErrorMessage(this, getString(R.string.cannot_detect_current_location));
		} else {
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			String locationString = getLocDesc(lat, lng);
			
			m_editTextDescription.setText(locationString);
			
			setMapToPos(lat, lng); 
		}
	}

}
