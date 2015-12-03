package com.nghiabuivan.sherlock;

import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nghiabuivan.sherlock.db.DaoLocation;
import com.nghiabuivan.sherlock.db.DaoPerson;
import com.nghiabuivan.sherlock.db.SherlockDbHelper;

public class PersonActivity extends FragmentActivity implements ActionBar.TabListener {
	
	private static String[] TAB_TITLES = new String[2];
	
	AppSectionsPagerAdapter m_appSectionsPagerAdapter;
	ViewPager m_viewPager;
	
	//
	private static DaoPerson s_person;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_person);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//
		TAB_TITLES[0] = getString(R.string.basic_information);
		TAB_TITLES[1] = getString(R.string.location_history);
		
		//
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			int personId = extras.getInt(Utils.KEY_PERSON_ID);
			
			SherlockDbHelper.getInstance().openReadDb();
			s_person = SherlockDbHelper.getInstance().getPersonById(personId);
			SherlockDbHelper.getInstance().closeDb();
			
			setTitle(s_person.getFullName());
		} else {
		}

		// Create the adapter that will return a fragment for each of the three primary sections
		// of the app.
		m_appSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();

		// Specify that we will be displaying tabs in the action bar.
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Set up the ViewPager, attaching the adapter and setting up a listener for when the
		// user swipes between sections.
		m_viewPager = (ViewPager) findViewById(R.id.pager);
		m_viewPager.setAdapter(m_appSectionsPagerAdapter);
		m_viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				// When swiping between different app sections, select the corresponding tab.
				// We can also use ActionBar.Tab#select() to do this if we have a reference to the
				// Tab.
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < m_appSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by the adapter.
			// Also specify this Activity object, which implements the TabListener interface, as the
			// listener for when this tab is selected.
			actionBar.addTab(
					actionBar.newTab()
							.setText(m_appSectionsPagerAdapter.getPageTitle(i))
							.setTabListener(this));
		}
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in the ViewPager.
		m_viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_person_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;

			case R.id.action_delete_person:
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
		.setMessage(getString(R.string.are_you_sure_to_delete_this_person))
		.setIcon(R.drawable.ic_action_warning)
		.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				SherlockDbHelper.getInstance().openWriteDb();
				SherlockDbHelper.getInstance().deletePersonById(s_person.getId());
				SherlockDbHelper.getInstance().closeDb();
				
				dialog.dismiss();
				Utils.s_needToRefreshPersonList = true;
				
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

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
	 * sections of the app.
	 */
	public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
		
		public AppSectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment;
			if (i == 0) {
				fragment = new BasicInfoFragment();
			} else {
				fragment = new LocationHistoryFragment();
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return TAB_TITLES[position];
		}
	}

	public static class BasicInfoFragment extends Fragment {
		private TextView m_textViewFullName;
		private TextView m_textViewGender;
		private TextView m_textViewHeight;
		private TextView m_textViewAgeRange;
		private TextView m_textViewHairColor;
		private TextView m_textViewComment;
		private ImageButton m_imageButtonEditPerson;
		
		private Intent m_intentEditPerson;
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_basic_info, container, false);
			
			m_textViewFullName = (TextView) rootView.findViewById(R.id.textViewFullName);
			m_textViewGender = (TextView) rootView.findViewById(R.id.textViewGender);
			m_textViewHeight = (TextView) rootView.findViewById(R.id.textViewHeight);
			m_textViewAgeRange = (TextView) rootView.findViewById(R.id.textViewAgeRange);
			m_textViewHairColor = (TextView) rootView.findViewById(R.id.textViewHairColor);
			m_textViewComment = (TextView) rootView.findViewById(R.id.textViewComment);
			m_imageButtonEditPerson = (ImageButton) rootView.findViewById(R.id.imageButtonEditPerson);
			
			m_intentEditPerson = new Intent(getActivity(), NewPersonActivity.class);
				
			//
			Utils.s_needToRefreshPerson = false;
			showBasicInfo();
			
			//
			m_imageButtonEditPerson.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					m_intentEditPerson.putExtra(Utils.KEY_PERSON_ID, s_person.getId());
					startActivity(m_intentEditPerson);
				}
			});
			
			return rootView;
		}
		
		public void onResume() {
			super.onResume();
			
			if (Utils.s_needToRefreshPerson) {
				SherlockDbHelper.getInstance().openReadDb();
				SherlockDbHelper.getInstance().fetchBasicInfoForPerson(s_person);
				SherlockDbHelper.getInstance().closeDb();
				
				showBasicInfo();
				
				Utils.s_needToRefreshPerson = false;
			}
		}
		
		private void showBasicInfo() {
			getActivity().setTitle(s_person.getFullName());
			
			m_textViewFullName.setText(s_person.getFullName());
			
			String gender = s_person.getGender() == 0 ? getString(R.string.male) : getString(R.string.female); 
			m_textViewGender.setText(gender);
			
			m_textViewHeight.setText(s_person.getHeight() + "");
			
			String ageRange = String.valueOf(s_person.getAgeMin() + "-" + s_person.getAgeMax());
			m_textViewAgeRange.setText(ageRange);
					
			String hairColor = s_person.getHairColor();
			if (hairColor == null) {
				hairColor = getString(R.string.unknown);
			}
			m_textViewHairColor.setText(hairColor);
			
			String comment = s_person.getComment();
			if (comment == null) {
				comment = getString(R.string.no_comment);
			}
			
			m_textViewComment.setText(comment);
		}
	}
	
	public static class LocationHistoryFragment extends Fragment {
		private ListView m_listViewLocationList;
		private TextView m_textViewCountLocation;
		private TextView m_textViewNoLocation;
		private ImageButton m_imageButtonAddLocation;
		
		private LocationAdapter m_adapter;
		private Intent m_intentLocation;
		private Intent m_intentNewLocation;
		private Intent m_intentEditLocation;
		
		private static final int CONTEXTMENU_OPTION_VIEW		= 1;
		private static final int CONTEXTMENU_OPTION_EDIT		= 2;
		private static final int CONTEXTMENU_OPTION_DELETE		= 3;
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_location_history, container, false);
			
			m_intentLocation = new Intent(getActivity(), LocationActivity.class);
			m_intentNewLocation = new Intent(getActivity(), NewLocationActivity.class);
			m_intentEditLocation = new Intent(getActivity(), NewLocationActivity.class);
			
			//
			m_listViewLocationList = (ListView) rootView.findViewById(R.id.listViewLocationList);
			m_textViewNoLocation = (TextView) rootView.findViewById(R.id.textViewNoLocation);
			m_textViewCountLocation = (TextView) rootView.findViewById(R.id.textViewCountLocation);
			m_imageButtonAddLocation = (ImageButton) rootView.findViewById(R.id.imageButtonAddLocation);
			
			m_imageButtonAddLocation.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					m_intentNewLocation.putExtra(Utils.KEY_PERSON_ID, s_person.getId());
					m_intentNewLocation.putExtra(Utils.KEY_PERSON_FULL_NAME, s_person.getFullName());
					startActivity(m_intentNewLocation);
				}
			});
			
			m_listViewLocationList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					DaoLocation location = m_adapter.getItem(position);
					m_intentLocation.putExtra(Utils.KEY_LOCATION_ID, location.getId());
					m_intentLocation.putExtra(Utils.KEY_PERSON_FULL_NAME, s_person.getFullName());
					startActivity(m_intentLocation);
				}
			});
			
			//
			registerForContextMenu(m_listViewLocationList);
			
			Utils.s_needToRefreshLocationList = false;
			showLocationList();

			return rootView;
		}
		
		@Override		
		public void onResume() {
			super.onResume();
			
			if (Utils.s_needToRefreshLocationList) {
				SherlockDbHelper.getInstance().openReadDb();
				SherlockDbHelper.getInstance().fetchLocationListForPerson(s_person);
				SherlockDbHelper.getInstance().closeDb();
				
				showLocationList();
				Utils.s_needToRefreshLocationList = false;
			}
		}
		
		private void showLocationList() {
			List<DaoLocation> locationList = s_person.getLocationList();
			
			if (locationList.size() == 0) {
				m_listViewLocationList.setVisibility(View.GONE);
				m_textViewCountLocation.setVisibility(View.GONE);
				m_textViewNoLocation.setVisibility(View.VISIBLE);
			} else {
				m_listViewLocationList.setVisibility(View.VISIBLE);
				m_textViewCountLocation.setVisibility(View.VISIBLE);
				m_textViewNoLocation.setVisibility(View.GONE);
				
				m_adapter = new LocationAdapter(getActivity(), locationList);
				m_listViewLocationList.setAdapter(m_adapter);
				
				m_textViewCountLocation.setText(getString(R.string.num_location, locationList.size()));
			}
		}
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
			
			menu.setHeaderTitle(R.string.title_menu_context);
			
			int i = 0;
			
			menu.add(Menu.NONE, CONTEXTMENU_OPTION_VIEW, i++, R.string.item_view);
			menu.add(Menu.NONE, CONTEXTMENU_OPTION_EDIT, i++, R.string.item_edit);
			menu.add(Menu.NONE, CONTEXTMENU_OPTION_DELETE, i++, R.string.item_delete);
		}
		
		@Override
		public boolean onContextItemSelected(MenuItem item) {
			AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
			
			final int position = menuInfo.position;
			final DaoLocation location = m_adapter.getItem(position);
			
			switch (item.getItemId()) {
			
			case CONTEXTMENU_OPTION_VIEW: {
				m_intentLocation.putExtra(Utils.KEY_LOCATION_ID, location.getId());
				m_intentLocation.putExtra(Utils.KEY_PERSON_FULL_NAME, s_person.getFullName());
				startActivity(m_intentLocation);
				return true;
			}
				
			case CONTEXTMENU_OPTION_EDIT: {
				m_intentEditLocation.putExtra(Utils.KEY_LOCATION_ID, location.getId());
				m_intentEditLocation.putExtra(Utils.KEY_PERSON_FULL_NAME, s_person.getFullName());
				startActivity(m_intentEditLocation);
				return true;
			}
			
			case CONTEXTMENU_OPTION_DELETE: {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder
				.setTitle(getString(R.string.confirm))
				.setMessage(getString(R.string.are_you_sure_to_delete_this_location))
				.setIcon(R.drawable.ic_action_warning)
				.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						SherlockDbHelper.getInstance().openWriteDb();
						SherlockDbHelper.getInstance().deleteLocationById(location.getId());
						SherlockDbHelper.getInstance().closeDb();
						
						dialog.dismiss();
						
						s_person.getLocationList().remove(position);
						m_adapter.notifyDataSetChanged();
						
						int count = s_person.getLocationList().size();
						
						if (count == 0) {
							m_listViewLocationList.setVisibility(View.GONE);
							m_textViewCountLocation.setVisibility(View.GONE);
							m_textViewNoLocation.setVisibility(View.VISIBLE);
						} else {
							m_listViewLocationList.setVisibility(View.VISIBLE);
							m_textViewCountLocation.setVisibility(View.VISIBLE);
							m_textViewNoLocation.setVisibility(View.GONE);
							m_textViewCountLocation.setText(getString(R.string.num_location, count));
						}
					}
				})
				.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
				
				return true;
			}
			
			}
			
			return true;
		}
	}
	
	public static class LocationAdapter extends ArrayAdapter<DaoLocation> {
		private Context m_context;
		private List<DaoLocation> m_locationList;

		public LocationAdapter(Context context, List<DaoLocation> objects) {
			super(context, R.layout.listview_location_list, objects);
			m_locationList = objects;
			m_context = context;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.listview_location_list, parent, false);
			
			TextView textView_Datetime = (TextView) rowView.findViewById(R.id.textView_LocItem_Time);
			TextView textView_Desc = (TextView) rowView.findViewById(R.id.textView_LocItem_Desc);
			TextView textView_Note = (TextView) rowView.findViewById(R.id.textView_LocItem_Note);
			ImageView imageView_Photo = (ImageView) rowView.findViewById(R.id.imageView_LocItem_Photo);
			
			DaoLocation location = m_locationList.get(position);
			
			textView_Datetime.setText(Utils.convertMillisToString(location.getTime()));
			textView_Desc.setText(location.getDescription());
			textView_Note.setText(location.getNote());
			
			byte[] photoBytes = location.getPhotoBytes();
			if (photoBytes == null) {
				imageView_Photo.setImageResource(R.drawable.ic_no_photo);
			} else {
				Bitmap bm = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
				imageView_Photo.setImageBitmap(bm);
			}
			
			return rowView;
		}
		
	}
}
