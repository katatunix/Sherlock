package com.nghiabuivan.sherlock.db;

import android.util.Base64;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DaoLocation {
	
	@Expose
	@SerializedName("id")
	private int m_id;
	
	@Expose
	@SerializedName("personId")
	private int m_personId;
	
	@Expose
	@SerializedName("position")
	private String m_position;
	
	@Expose
	@SerializedName("description")
	private String m_description;
	
	@Expose
	@SerializedName("time")
	private long m_time;
	
	@Expose
	@SerializedName("note")
	private String m_note;
	
	@Expose
	@SerializedName("photo")
	private String m_photoBase64;
	
	private byte[] m_photoBytes;
	
	public DaoLocation(int id, int personId, String position, String description, long time, String note, byte[] photoBytes) {
		m_id = id;
		m_personId = personId;
		m_position = position;
		m_description = description;
		m_time = time;
		m_note = note;
		m_photoBytes = photoBytes;
		
		m_photoBase64 = m_photoBytes == null ? null :
			Base64.encodeToString(m_photoBytes, Base64.DEFAULT);
	}
	
	public int getId() {
		return m_id;
	}
	
	public int getPersonId() {
		return m_personId;
	}
	
	public String getPosition() {
		return m_position;
	}
	
	public String getDescription() {
		return m_description;
	}
	
	public long getTime() {
		return m_time;
	}
	
	public String getNote() {
		return m_note;
	}
	
	public byte[] getPhotoBytes() {
		return m_photoBytes;
	}

}
