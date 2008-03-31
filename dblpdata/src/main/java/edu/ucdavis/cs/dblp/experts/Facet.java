package edu.ucdavis.cs.dblp.experts;

public class Facet {
	private String fieldName;
	private String fieldValue;
	private int count;
	
	public Facet(String fieldName, String fieldValue, int count) {
		super();
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
		this.count = count;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getFieldValue() {
		return fieldValue;
	}
	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
}
