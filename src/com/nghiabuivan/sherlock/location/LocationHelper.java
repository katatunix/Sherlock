package com.nghiabuivan.sherlock.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationHelper {
	
	private static LocationHelper s_instance = null;
	
	public static LocationHelper getInstance() {
		if (s_instance == null) {
			s_instance = new LocationHelper();
		}
		return s_instance;
	}
	
	//=========================================================================
	private boolean m_running = false;
	
	private LocationManager m_locationManager = null;
	private LocationObserver m_observer = null;
	
	private boolean m_isGpsEnabled = false;
	private boolean m_isNetEnabled = false;
	
	private LocationHelper() {
		
	}
	
	public boolean startDetectLocation(Context context, LocationObserver observer) {
		if (m_running) return false;
		
		m_observer = observer;
		
		if (m_locationManager == null) {
			m_locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		}
		
		try {
			m_isGpsEnabled = m_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
			m_isGpsEnabled = false;
		}
		
		try {
			m_isNetEnabled = m_locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
			m_isNetEnabled = false;
		}
		
		if (!m_isGpsEnabled && !m_isNetEnabled) return false;
		
		if (m_isGpsEnabled) {
			m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, m_locationListenerGps);
		}
		if (m_isNetEnabled) {
			m_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, m_locationListenerNet);
		}
		
		m_running = true;
		
		return true;
	}
	
	public void stopDetectLocation() {
		if (!m_running) return;
		
		removeUpdatesAll();
		
		m_running = false;
	}
	
	private void removeUpdatesAll() {
		m_locationManager.removeUpdates(m_locationListenerGps);
		m_locationManager.removeUpdates(m_locationListenerNet);
	}
	
	//======================================================================================
	private LocationListener m_locationListenerGps = new LocationListener() {
		
		public void onLocationChanged(Location location) {
			m_observer.onLocationDetected(location);
			
			removeUpdatesAll();
			m_running = false;
		}
		
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		public void onProviderEnabled(String provider) {}
		public void onProviderDisabled(String provider) {}
	};
	
	private LocationListener m_locationListenerNet = new LocationListener() {
		
		public void onLocationChanged(Location location) {
			m_observer.onLocationDetected(location);
			
			removeUpdatesAll();
			m_running = false;
		}
		
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		public void onProviderEnabled(String provider) {}
		public void onProviderDisabled(String provider) {}
	};
	
}
