package org.ossmeter.platform.delta.bugtrackingsystem;

import java.io.Serializable;

import org.ossmeter.repository.model.BugTrackingSystem;

public class BugTrackingSystemAttachment implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected String attachmentId;
	protected String creator;
	protected String filename;
	protected String mimeType;
//	protected byte[] rawData;
	transient protected String bugId;
	transient protected BugTrackingSystem bugTrackingSystem;	
	
	public String getBugId() {
		return bugId;
	}
	
	public void setBugId(String bugId) {
		this.bugId = bugId;
	}
	
	public String getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(String attachmentId) {
		this.attachmentId = attachmentId;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

//	public byte[] getRawData() {
//		return rawData;
//	}

//	public void setText(byte[] rawData) {
//		this.rawData = rawData;
//	}

	public BugTrackingSystem getBugTrackingSystem() {
		return bugTrackingSystem;
	}

	public void setBugTrackingSystem(BugTrackingSystem bugTrackingSystem) {
		this.bugTrackingSystem = bugTrackingSystem;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BugTrackingSystemAttachment) {
			if ( this.bugId != ((BugTrackingSystemAttachment) obj).getBugId() ) {
				return false;
			} 
			if (!this.attachmentId.equals(((BugTrackingSystemAttachment) obj).getAttachmentId())) {
				return false;
			}
			return true;
		}
		
		return false;
	}
	
	
}
