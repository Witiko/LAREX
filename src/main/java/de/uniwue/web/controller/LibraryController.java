package de.uniwue.web.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import de.uniwue.web.config.LarexConfiguration;
import de.uniwue.web.io.FileDatabase;
import de.uniwue.web.io.FilePathManager;
import de.uniwue.web.model.Library;

/**
 * Communication Controller to handle simple requests about the book library.
 * 
 */
@Controller
@Scope("request")
public class LibraryController {
	@Autowired
	private ServletContext servletContext;
	@Autowired
	private FilePathManager fileManager;
	@Autowired
	private LarexConfiguration config;

	/**
	 * Display a list of all books present in the book path. Clicking on a book will
	 * open it in the larex view.
	 * 
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/")
	public String home(Model model) throws IOException {
		// Reset config
		fileManager.init(servletContext);
		config.read(new File(fileManager.getConfigurationFile()));
		String bookFolder = config.getSetting("bookpath");
    String saveDir = config.getSetting("savedir");
		if (!bookFolder.equals("")) {
			fileManager.setLocalBooksPath(bookFolder);
		}
		if (saveDir != null && !saveDir.equals("")) {
			fileManager.setSaveDir(saveDir);
		}
		File bookPath = new File(fileManager.getLocalBooksPath());
		bookPath.isDirectory();
		FileDatabase database = new FileDatabase(bookPath, config.getListSetting("imagefilter"));
		Library lib = new Library(database);

		model.addAttribute("library", lib);
		return "lib";
	}
}
