package de.uniwue.web.communication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwue.web.model.PageAnnotations;

import java.util.Map;

/**
 * Communication object for the gui to request a export of a segmentation
 * 
 */
public class ExportRequest {

	@JsonProperty("bookid")
	private Integer bookid;
	@JsonProperty("segmentation")
	private PageAnnotations segmentation;
	@JsonProperty("version")
	private String version;
	@JsonProperty("metadata")
	private Map<String, String> metadata;

	@JsonCreator
	public ExportRequest(@JsonProperty("bookid") Integer bookid,
			@JsonProperty("segmentation") PageAnnotations segmentation, @JsonProperty("version") String version,
						 @JsonProperty("metadata") Map<String, String> metadata) {
		this.bookid = bookid;
		this.segmentation = segmentation;
		this.version = version;
		this.metadata = metadata;
	}

	public Integer getBookid() {
		return bookid;
	}

	public PageAnnotations getSegmentation() {
		return segmentation;
	}

	public String getVersion() {
		return version;
	}

	public Map<String, String> getMetadata() { return metadata; }
}