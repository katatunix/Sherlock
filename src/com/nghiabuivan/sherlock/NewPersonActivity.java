package com.nghiabuivan.sherlock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.nghiabuivan.sherlock.db.DaoPerson;
import com.nghiabuivan.sherlock.db.SherlockDbHelper;

public class NewPersonActivity extends Activity {
	
	private EditText m_editTextFullName;
	
	private RadioGroup m_radioGroupGender;
	private RadioButton m_radioButtonMale;
	private RadioButton m_radioButtonFemale;
	
	private EditText m_editTextHeight;
	private EditText m_editTextAgeMin;
	private EditText m_editTextAgeMax;
	private EditText m_editTextHairColor;
	private EditText m_editTextComment;
	
	private Intent m_intentPerson;
	
	private boolean m_isEdit = false;
	private int m_personId = 0;
	private DaoPerson m_person = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_person);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//
		m_editTextFullName = (EditText) findViewById(R.id.editTextFullName);
		
		m_radioGroupGender = (RadioGroup) findViewById(R.id.radioGroupGender);
		m_radioButtonMale = (RadioButton) findViewById(R.id.radioMale);
		m_radioButtonFemale = (RadioButton) findViewById(R.id.radioFemale);
		
		m_editTextHeight = (EditText) findViewById(R.id.editTextHeight);
		m_editTextAgeMin = (EditText) findViewById(R.id.editTextAgeMin);
		m_editTextAgeMax = (EditText) findViewById(R.id.editTextAgeMax);
		m_editTextHairColor = (EditText) findViewById(R.id.editTextHairColor);
		m_editTextComment = (EditText) findViewById(R.id.editTextComment);
		
		m_intentPerson = new Intent(this, PersonActivity.class);
		
		//
		m_isEdit = false;
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey(Utils.KEY_PERSON_ID)) {
			m_isEdit = true;
			m_personId = extras.getInt(Utils.KEY_PERSON_ID);
			SherlockDbHelper.getInstance().openReadDb();
			m_person = SherlockDbHelper.getInstance().getPersonById(m_personId);
			SherlockDbHelper.getInstance().closeDb();
			
			setTitle(getString(R.string.edit_person));
			
			m_editTextFullName.setText(m_person.getFullName());
			
			m_radioButtonMale.setChecked(m_person.getGender() == 0);
			m_radioButtonFemale.setChecked(m_person.getGender() == 1);
			
			m_editTextHeight.setText(m_person.getHeight() + "");
			m_editTextAgeMin.setText(m_person.getAgeMin() + "");
			m_editTextAgeMax.setText(m_person.getAgeMax() + "");
			if (m_person.getHairColor() != null) {
				m_editTextHairColor.setText(m_person.getHairColor());
			}
			if (m_person.getComment() != null) {
				m_editTextComment.setText(m_person.getComment());
			}
		}
	}

	private void handleButtonDone() {
		String messageError = "";
		
		String fullName = m_editTextFullName.getText().toString().trim();
		if (fullName.length() == 0) {
			messageError = Utils.appendMsg(messageError, getString(R.string.fullname_not_blank));
		}
		
		int radioId = m_radioGroupGender.getCheckedRadioButtonId();
		int gender = radioId == R.id.radioMale ? 0 : 1;
		
		float height = 0.0f;
		{
			String heightString = m_editTextHeight.getText().toString().trim();
			if (heightString.length() == 0) {
				messageError = Utils.appendMsg(messageError, getString(R.string.height_not_blank));
			} else {
				try {
					height = Float.parseFloat(heightString);
					if (height <= 0.0f) {
						messageError = Utils.appendMsg(messageError, getString(R.string.height_float_number));
					}
				} catch (NumberFormatException ex) {
					messageError = Utils.appendMsg(messageError, getString(R.string.height_float_number));
				}
			}
		}
		
		int ageMin = 0;
		{
			String ageMinString = m_editTextAgeMin.getText().toString().trim();
			if (ageMinString.length() == 0) {
				messageError = Utils.appendMsg(messageError, getString(R.string.age_min_not_blank));
			} else {
				try {
					ageMin = Integer.parseInt(ageMinString);
					if (ageMin <= 0) {
						messageError = Utils.appendMsg(messageError, getString(R.string.age_min_integer_number));
					}
				} catch (NumberFormatException ex) {
					ageMin = 0;
					messageError = Utils.appendMsg(messageError, getString(R.string.age_min_integer_number));	
				}
			}
		}
		
		int ageMax = 0;
		{
			String ageMaxString = m_editTextAgeMax.getText().toString().trim();
			if (ageMaxString.length() == 0) {
				messageError = Utils.appendMsg(messageError, getString(R.string.age_max_not_blank));
			} else {
				try {
					ageMax = Integer.parseInt(ageMaxString);
					if (ageMax <= 0) {
						messageError = Utils.appendMsg(messageError, getString(R.string.age_max_integer_number));
					}
				} catch (NumberFormatException ex) {
					ageMax = 0;
					messageError = Utils.appendMsg(messageError, getString(R.string.age_max_integer_number));	
				}
			}
		}
		
		if (ageMin > 0 && ageMax > 0 && ageMin > ageMax) {
			messageError = Utils.appendMsg(messageError, getString(R.string.age_min_max));
		}
		
		String hairColor = m_editTextHairColor.getText().toString().trim();
		if (hairColor.length() == 0) hairColor = null;
		String comment = m_editTextComment.getText().toString().trim();
		if (comment.length() == 0) comment = null;
		
		if (messageError.length() > 0) {
			Utils.showErrorMessage(this, messageError);
		} else {
			SherlockDbHelper.getInstance().openWriteDb();
			if (m_isEdit) {
				SherlockDbHelper.getInstance().editPerson(
						m_personId, fullName, gender, height, ageMin, ageMax, hairColor, comment);
				SherlockDbHelper.getInstance().closeDb();
				
				Utils.s_needToRefreshPerson = true;
				Utils.s_needToRefreshPersonList = true; // always fresh person list
				
				handleButtonCancel();
			} else {
				long newPersonId = SherlockDbHelper.getInstance().addPerson(
						fullName, gender, height, ageMin, ageMax, hairColor, comment);
				SherlockDbHelper.getInstance().closeDb();
				
				Utils.s_needToRefreshPersonList = true;
				
				handleButtonCancel();
				
				m_intentPerson.putExtra(Utils.KEY_PERSON_ID, (int)newPersonId);
				startActivity(m_intentPerson);
			}
		}
	}
	
	private void handleButtonCancel() {
		Utils.hideKeyboard(this, m_editTextFullName);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_new_person_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_new_person_cancel:
			case android.R.id.home:
				handleButtonCancel();
				return true;
				
			case R.id.action_new_person_done:
				handleButtonDone();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}

}
