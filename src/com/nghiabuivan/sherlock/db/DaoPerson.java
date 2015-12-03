package com.nghiabuivan.sherlock.db;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DaoPerson {
	
	@Expose
	@SerializedName("id")
	private int m_id;
	
	@Expose
	@SerializedName("name")
	private String m_fullName;
	
	@Expose
	@SerializedName("gender")
	private int m_gender;
	
	@Expose
	@SerializedName("height")
	private float m_height;
	
	@Expose
	@SerializedName("ageMin")
	private int m_ageMin;
	
	@Expose
	@SerializedName("ageMax")
	private int m_ageMax;
	
	@Expose
	@SerializedName("hairColor")
	private String m_hairColor;
	
	@Expose
	@SerializedName("comment")
	private String m_comment;
	
	@Expose
	@SerializedName("locations")
	private List<DaoLocation> m_locationList;
	
	public DaoPerson(int id, String fullName, int gender, float height, int ageMin, int ageMax, String hairColor, String comment) {
		m_id = id;
		m_fullName = fullName;
		m_gender = gender;
		m_height = height;
		m_ageMin = ageMin;
		m_ageMax = ageMax;
		m_hairColor = hairColor;
		m_comment = comment;
		
		m_locationList = new ArrayList<DaoLocation>();
	}
	
	public void update(String fullName, int gender, float height, int ageMin, int ageMax, String hairColor, String comment) {
		m_fullName = fullName;
		m_gender = gender;
		m_height = height;
		m_ageMin = ageMin;
		m_ageMax = ageMax;
		m_hairColor = hairColor;
		m_comment = comment;
	}
	
	public int getId() {
		return m_id;
	}
	
	public String getFullName() {
		return m_fullName;
	}
	
	public int getGender() {
		return m_gender;
	}
	
	public float getHeight() {
		return m_height;
	}
	
	public int getAgeMin() {
		return m_ageMin;
	}
	
	public int getAgeMax() {
		return m_ageMax;
	}
	
	public String getHairColor() {
		return m_hairColor;
	}
	
	public String getComment() {
		return m_comment;
	}
	
	public String toString() {
		return m_fullName;
	}
	
	public List<DaoLocation> getLocationList() {
		return m_locationList;
	}

}
