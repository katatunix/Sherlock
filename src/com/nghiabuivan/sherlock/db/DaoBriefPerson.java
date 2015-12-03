package com.nghiabuivan.sherlock.db;

public class DaoBriefPerson {
	
	private int m_id;
	private String m_fullName;
	
	public DaoBriefPerson(int id, String fullName) {
		m_id = id;
		m_fullName = fullName;
	}
	
	public int getId() {
		return m_id;
	}
	
	public String getFullName() {
		return m_fullName;
	}
	
	public String toString() {
		return m_fullName;
	}

}
