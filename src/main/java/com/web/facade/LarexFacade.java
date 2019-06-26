package com.web.facade;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.opencv.core.Size;
import org.primaresearch.ident.IdRegister.InvalidIdException;
import org.primaresearch.io.UnsupportedFormatVersionException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.web.communication.SegmentationStatus;
import com.web.controller.FileManager;
import com.web.io.PageXMLReader;
import com.web.io.PageXMLWriter;
import com.web.io.SettingsReader;
import com.web.io.SettingsWriter;
import com.web.model.Book;
import com.web.model.Page;
import com.web.model.PageAnnotations;
import com.web.model.Region;
import com.web.model.database.FileDatabase;

import larex.data.MemoryCleaner;
import larex.geometry.regions.RegionSegment;
import larex.segmentation.SegmentationResult;
import larex.segmentation.Segmenter;
import larex.segmentation.parameters.Parameters;

/**
 * Segmenter using the Larex project/algorithm
 * 
 */
public class LarexFacade {

	public static PageAnnotations segmentPage(BookSettings settings, int pageNr, boolean allowLocalResults,
			FileManager fileManager, FileDatabase database) {
		Book book = getBook(settings.getBookID(), database);

		Page page = book.getPage(pageNr);
		String xmlPath = fileManager.getLocalBooksPath() + File.separator + book.getName() + File.separator + page.getName()
				+ ".xml";

		if (allowLocalResults && new File(xmlPath).exists()) {
			PageAnnotations segmentation = PageXMLReader.loadSegmentationResultFromDisc(xmlPath);;
			segmentation.setStatus(SegmentationStatus.LOADED);
			return segmentation;
		} else {
			PageAnnotations segmentation = segment(settings, page, fileManager);
			return segmentation;
		}
	}

	public static PageAnnotations emptySegmentPage(int bookid, int pageNr, FileDatabase database) {
		Book book = getBook(bookid, database);

		Page page = book.getPage(pageNr);

		ArrayList<RegionSegment> regions = new ArrayList<RegionSegment>();

		PageAnnotations segmentation =  new PageAnnotations(page.getFileName(), page.getWidth(), page.getHeight(), regions,
				page.getId());
		segmentation.setStatus(SegmentationStatus.EMPTY);
		return segmentation;
	}

	public static BookSettings getDefaultSettings(Book book) {
		return new BookSettings(new Parameters(), book);
	}

	public static Document getPageXML(PageAnnotations segmentation, String version) {
		try {
			return PageXMLWriter.getPageXML(segmentation, segmentation.getFileName(), segmentation.getWidth(),
					segmentation.getHeight(), version);
		} catch (UnsupportedFormatVersionException e) {
			System.out.println(e.toString());
			e.printStackTrace();
			return null;
		} catch (InvalidIdException e) {
			System.out.println(e.toString());
			e.printStackTrace();
			return null;
		}
	}

	public static void savePageXMLLocal(String saveDir, String filename, Document document) {
		PageXMLWriter.saveDocument(document, filename, saveDir);
	}

	public static Document getSettingsXML(BookSettings settings) {
		Parameters parameters = settings.toParameters(new Size());
		return SettingsWriter.getSettingsXML(parameters);
	}

	private static PageAnnotations segment(BookSettings settings, Page page, FileManager fileManager) {
		PageAnnotations segmentation = null;
		larex.data.Page currentLarexPage = segmentLarex(settings, page, fileManager);

		if (currentLarexPage != null) {
			SegmentationResult segmentationResult = currentLarexPage.getSegmentationResult();
			currentLarexPage.setSegmentationResult(segmentationResult);

			ArrayList<RegionSegment> regions = segmentationResult.getRegions();

			segmentation = new PageAnnotations(page.getFileName(), page.getWidth(), page.getHeight(), regions,
					page.getId());
		} else {
			segmentation = new PageAnnotations(page.getFileName(), page.getWidth(), page.getHeight(), page.getId(),
					new HashMap<String, Region>(), SegmentationStatus.MISSINGFILE, new ArrayList<String>());
		}
		return segmentation;
	}

	private static larex.data.Page segmentLarex(BookSettings settings, Page page, FileManager fileManager) {
		String imagePath = fileManager.getLocalBooksPath() + File.separator + page.getImage();

		if (new File(imagePath).exists()) {
			larex.data.Page currentLarexPage = new larex.data.Page(imagePath);
			currentLarexPage.initPage();

			Size pagesize = currentLarexPage.getOriginal().size();

			Parameters parameters = settings.toParameters(pagesize, page.getId());

			Segmenter segmenter = new Segmenter(parameters);
			SegmentationResult segmentationResult = segmenter.segment(currentLarexPage.getOriginal());
			currentLarexPage.setSegmentationResult(segmentationResult);
			MemoryCleaner.clean(currentLarexPage);

			return currentLarexPage;
		} else {
			System.err.println(
					"Warning: Image file could not be found. Segmentation result will be empty. File: " + imagePath);
			return null;
		}
	}

	public static larex.data.Page getLarexPage(Page page, FileManager fileManager) {
		String imagePath = fileManager.getLocalBooksPath() + File.separator + page.getImage();

		if (new File(imagePath).exists()) {
			return new larex.data.Page(imagePath);
		}
		return null;
	}

	public static BookSettings readSettings(byte[] settingsFile, int bookID, FileManager fileManager, FileDatabase database) {
		BookSettings settings = null;

		try(ByteArrayInputStream stream = new ByteArrayInputStream(settingsFile)){
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document document = dBuilder.parse(stream);

			Book book = getBook(bookID, database);
			Page page = book.getPage(0);
			String imagePath = fileManager.getLocalBooksPath() + File.separator + page.getImage();
			larex.data.Page currentLarexPage = new larex.data.Page(imagePath);
			currentLarexPage.initPage();

			Parameters parameters = SettingsReader.loadSettings(document, currentLarexPage.getBinary().size());
			MemoryCleaner.clean(currentLarexPage);

			settings = new BookSettings(parameters, book);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return settings;
	}

	public static PageAnnotations readPageXML(byte[] pageXML, int pageNr, int bookID, FileDatabase database) {
		try (ByteArrayInputStream stream = new ByteArrayInputStream(pageXML)){
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			return PageXMLReader.getSegmentationResult(dBuilder.parse(stream));
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Book getBook(int bookID, FileDatabase database) {
		return database.getBook(bookID);
	}
}
