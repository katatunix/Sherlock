package com.nghiabuivan.sherlock;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class Utils {
	
	public static final String KEY_PERSON_ID = "person_id";
	public static final String KEY_PERSON_FULL_NAME = "person_full_name";
	public static final String KEY_LOCATION_ID = "location_id";
	public static final String KEY_PEOPLE_NUMBER = "people_number";
	
	private static final String TAG = "Sherlock";
	private static final String POS_SEP = "_";
	
	public static boolean s_needToRefreshPersonList = true;
	public static boolean s_needToRefreshPerson = true;
	
	public static boolean s_needToRefreshLocationList = true;
	public static boolean s_needToRefreshLocation = true;
	
	public static boolean s_hasCamera = true;
	
	private static SimpleDateFormat s_simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
	
	public static void log(String msg) {
		Log.d(TAG, msg);
	}
	
	public static void hideKeyboard(Context context, EditText et) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
	}
	
	public static long convertStringToMillis(String datetimeStr) {
		try {
			Date dateObj = s_simpleDateFormat.parse(datetimeStr);
			return dateObj.getTime();
		} catch (ParseException e) {
			return 0L;
		}
	}
	
	public static String convertMillisToString(long millis) {
		Date dateObj = new Date(millis);
		return s_simpleDateFormat.format(dateObj);
	}
	
	public static void showErrorMessage(Context ctx, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
	    builder
	    .setTitle(ctx.getString(R.string.error))
	    .setMessage(msg)
	    .setIcon(R.drawable.ic_action_error)
	    .setPositiveButton(ctx.getString(R.string.ok), new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	dialog.dismiss();
	        }
	    });
	    AlertDialog alert = builder.create();
	    alert.show();
	}
	
	public static String appendMsg(String fullMsg, String msg) {
		if (fullMsg.length() > 0) {
			fullMsg += "\n";
		}
		fullMsg += msg;
		return fullMsg;
	}
	
	public static String getLocationDesc(Context ctx, double lat, double lng) {
		Geocoder coder = new Geocoder(ctx);
		List<Address> list = null;
		try {
			list = coder.getFromLocation(lat, lng, 1);
		} catch (IOException e) {
			return null;
		}
		
		String locationString;
		if (list == null || list.size() == 0) {
			locationString = lat + ", " + lng;
		} else {
			Address address = list.get(0);
			locationString = "";
			int index = 0;
			while (true) {
				String line = address.getAddressLine(index);
				if (line == null) break;
				if (locationString.length() != 0) {
					locationString += ", ";
				}
				locationString += line;
				index++;
			}
			locationString = locationString.trim();
		}
		
		return locationString;
	}
	
	public static String makePositionString(double lat, double lng) {
		return lat + POS_SEP + lng;
	}
	
	public static double[] makePositionDoubles(String pos) {
		int i = pos.indexOf(POS_SEP);
		if (i == -1) return null;
		try {
			double[] ret = new double[] { 0.0, 0.0 };
			String sLat = pos.substring(0, i);
			String sLng = pos.substring(i + 1);
			ret[0] = Double.parseDouble(sLat);
			ret[1] = Double.parseDouble(sLng);
			return ret;
		} catch (Exception ex) {
			return null;
		}
	}
	
}
