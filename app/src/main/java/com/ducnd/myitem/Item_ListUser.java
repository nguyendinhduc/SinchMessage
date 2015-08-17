package com.ducnd.myitem;

public class Item_ListUser {
	private String username, date, idParse;
	private int idIcon;
	private byte[] bIcon;
	private boolean isByteIcon = false;

	public Item_ListUser(String username, String date, int idIcon) {
		this.username = username;
		this.date = date;
		this.idIcon = idIcon;
	}

	public Item_ListUser(String username, String date, byte[] bIcon) {
		this.username = username;
		this.date = date;
		this.bIcon = bIcon;
		this.isByteIcon = true;
	}
	
	public Item_ListUser(String idParse, String username, String date, int idIcon) {
		this.idParse = idParse;
		this.username = username;
		this.date = date;
		this.bIcon = bIcon;
		this.isByteIcon = true;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getIdIcon() {
		return idIcon;
	}

	public void setIdIcon(int idIcon) {
		this.idIcon = idIcon;
	}

	public byte[] getbIcon() {
		return bIcon;
	}

	public void setbIcon(byte[] bIcon) {
		this.bIcon = bIcon;
	}

	public boolean isByteIcon() {
		return isByteIcon;
	}

	public void setByteIcon(boolean isByteIcon) {
		this.isByteIcon = isByteIcon;
	}

	public String getIdParse() {
		return idParse;
	}

	public void setIdParse(String idParse) {
		this.idParse = idParse;
	}
	

}
