package com.nghiabuivan.sherlock;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.nghiabuivan.sherlock.db.DaoBriefPerson;
import com.nghiabuivan.sherlock.db.SherlockDbHelper;

public class MainActivity extends Activity  {
	
	private ListView m_listViewPersonList;
	private TextView m_textViewNoPerson;
	private EditText m_editTextSearch;
	
	private List<DaoBriefPerson> m_listPerson = null;
	private ArrayAdapter<DaoBriefPerson> m_adapter = null;
	
	private Intent m_intentNewPerson = null;
	private Intent m_intentEditPerson = null;
	private Intent m_intentPerson = null;
	private Intent m_intentUpload = null;
	private Intent m_intentNewLocation = null;
	
	private static final int CONTEXTMENU_OPTION_VIEW			= 1;
	private static final int CONTEXTMENU_OPTION_EDIT			= 2;
	private static final int CONTEXTMENU_OPTION_DELETE			= 3;
	private static final int CONTEXTMENU_OPTION_ADD_LOCATION	= 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		SherlockDbHelper.setContext(getApplicationContext());
		
		Utils.s_needToRefreshPersonList = true;
		Utils.s_hasCamera = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
		
		//
		m_listViewPersonList = (ListView) findViewById(R.id.listViewPerson);
		m_editTextSearch = (EditText) findViewById(R.id.editTextSearch);
		
		m_textViewNoPerson = (TextView) findViewById(R.id.textViewNoPerson);
		
		//
		m_listViewPersonList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				DaoBriefPerson person = m_adapter.getItem(position);
				m_intentPerson.putExtra(Utils.KEY_PERSON_ID, person.getId());
				startActivity(m_intentPerson);
			}
		});
		
		m_editTextSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (m_adapter != null) {
					m_adapter.getFilter().filter(s);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		
		//
		m_intentNewPerson = new Intent(this, NewPersonActivity.class);
		m_intentEditPerson = new Intent(this, NewPersonActivity.class);
		m_intentPerson = new Intent(this, PersonActivity.class);
		m_intentUpload = new Intent(this, UploadActivity.class);
		m_intentNewLocation = new Intent(this, NewLocationActivity.class);
		
		//
		registerForContextMenu(m_listViewPersonList);		
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		menu.setHeaderTitle(R.string.title_menu_context);
		
		int i = 0;
		
		menu.add(Menu.NONE, CONTEXTMENU_OPTION_VIEW, i++, R.string.item_view);
		menu.add(Menu.NONE, CONTEXTMENU_OPTION_EDIT, i++, R.string.item_edit);
		menu.add(Menu.NONE, CONTEXTMENU_OPTION_DELETE, i++, R.string.item_delete);
		menu.add(Menu.NONE, CONTEXTMENU_OPTION_ADD_LOCATION, i++, R.string.item_add_location);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		
		final int position = menuInfo.position;
		final DaoBriefPerson person = m_adapter.getItem(position);
		
		switch (item.getItemId()) {
		case CONTEXTMENU_OPTION_VIEW: {
			m_intentPerson.putExtra(Utils.KEY_PERSON_ID, person.getId());
			startActivity(m_intentPerson);
			return true;
		}
			
		case CONTEXTMENU_OPTION_EDIT: {
			m_intentEditPerson.putExtra(Utils.KEY_PERSON_ID, person.getId());
			startActivity(m_intentEditPerson);
			break;
		}
		
		case CONTEXTMENU_OPTION_DELETE: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder
			.setTitle(getString(R.string.confirm))
			.setMessage(getString(R.string.are_you_sure_to_delete_this_person))
			.setIcon(R.drawable.ic_action_warning)
			.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					SherlockDbHelper.getInstance().openWriteDb();
					SherlockDbHelper.getInstance().deletePersonById(person.getId());
					SherlockDbHelper.getInstance().closeDb();
					
					dialog.dismiss();
					
					m_listPerson.remove(position);
					m_adapter.notifyDataSetChanged();
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
			
		case CONTEXTMENU_OPTION_ADD_LOCATION:
			m_intentNewLocation.putExtra(Utils.KEY_PERSON_ID, person.getId());
			m_intentNewLocation.putExtra(Utils.KEY_PERSON_FULL_NAME, person.getFullName());
			startActivity(m_intentNewLocation);
			
			return true;
		}
		
		return true;
	}
	
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (Utils.s_needToRefreshPersonList) {
			SherlockDbHelper.getInstance().openReadDb();
			m_listPerson = SherlockDbHelper.getInstance().getDaoBriefPersonList();
			SherlockDbHelper.getInstance().closeDb();
			
			m_editTextSearch.getText().clear();
			
			if (m_listPerson.size() == 0) {
				m_listViewPersonList.setVisibility(View.GONE);
				m_editTextSearch.setVisibility(View.GONE);
				m_textViewNoPerson.setVisibility(View.VISIBLE);
			} else {
				m_listViewPersonList.setVisibility(View.VISIBLE);
				m_editTextSearch.setVisibility(View.VISIBLE);
				m_textViewNoPerson.setVisibility(View.GONE);
				
				m_adapter = new ArrayAdapter<DaoBriefPerson>(
					this, R.layout.listview_person_list, m_listPerson
				);
				m_listViewPersonList.setAdapter(m_adapter);
			}
			
			Utils.s_needToRefreshPersonList = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_new_person:
				startActivity(m_intentNewPerson);
				return true;
			case R.id.action_upload:
				m_intentUpload.putExtra(Utils.KEY_PEOPLE_NUMBER, m_listPerson.size());
				startActivity(m_intentUpload);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
}
