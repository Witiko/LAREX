package de.uniwue.web.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwue.algorithm.geometry.regions.RegionSegment;
import de.uniwue.web.communication.SegmentationStatus;

/**
 * Segmentation result of a specific page. Contains a pageNr and the resulting
 * segment polygons.
 */
public class PageAnnotations {
	/**
	 * Name of the page (does not include image extensions)
	 * Does not include sub extensions if imageSubFilter is active
	 */
	@JsonProperty("name")
	private final String name;
	@JsonProperty("width")
	private final int width;
	@JsonProperty("height")
	private final int height;
	@JsonProperty("segments")
	private final Map<String, Region> segments;
	@JsonProperty("readingOrder")
	private final List<String> readingOrder;
	@JsonProperty("status")
	private final SegmentationStatus status;
	@JsonProperty("metadata")
	private final Map<String, String> metadata;

	@JsonCreator
	public PageAnnotations(@JsonProperty("name") String name, @JsonProperty("width") int width,
			@JsonProperty("height") int height,
			@JsonProperty("metadata") Map<String, String> metadata,
			@JsonProperty("segments") Map<String, Region> segments, @JsonProperty("status") SegmentationStatus status,
			@JsonProperty("readingOrder") List<String> readingOrder) {
		this.segments = segments;
		this.status = status;
		this.readingOrder = readingOrder;
		this.name = name;
		this.width = width;
		this.height = height;
		this.metadata = metadata;
		checkNameValidity(name);
	}

	public PageAnnotations(String name, int width, int height, int pageNr, 
			Collection<RegionSegment> regions,  SegmentationStatus status) {
		Map<String, Region> segments = new HashMap<String, Region>();
		Map<String, String> metadata = new HashMap<>();

		for (RegionSegment region : regions) {
			LinkedList<Point> points = new LinkedList<Point>();
			for (org.opencv.core.Point regionPoint : region.getPoints().toList()) {
				points.add(new Point(regionPoint.x, regionPoint.y));
			}

			Region segment = new Region(points, region.getId(), region.getType().toString());
			segments.put(segment.getId(), segment);
		}

		this.segments = segments;
		this.status = status;
		this.readingOrder = new ArrayList<String>();
		this.name = name;
		this.width = width;
		this.height = height;
		this.metadata = metadata;
		checkNameValidity(name);
	}

	public PageAnnotations(String name, int width, int height, int pageNr) {
		this(name, width, height, pageNr, new ArrayList<RegionSegment>(), SegmentationStatus.EMPTY);
	}
	
	public Map<String, Region> getSegments() {
		return new HashMap<String, Region>(segments);
	}

	public SegmentationStatus getStatus() {
		return status;
	}

	public List<String> getReadingOrder() {
		return new ArrayList<String>(readingOrder);
	}

	public String getName() {
		return name;
	}

	public Map<String, String> getMetadata() {return metadata;}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}
	
	private static void checkNameValidity(String name) {
		final List<String> imageExtensions = Arrays.asList(".png", ".jpg", ".jpeg", ".tif", ".tiff");
		for (String ext : imageExtensions) {
			if(name.toLowerCase().endsWith(ext))
				System.err.println("[Warning] Page name '"+name+"' ends with an image extension ('"+ext+"').\n"+
								   "\tThis should not happen unless '"+ext+"' is part of the page name.\n"+
								   "\te.g. '"+name+".png'");
		}
		
	}
}
