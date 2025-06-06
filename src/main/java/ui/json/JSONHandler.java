/* Licensed under MIT 2024-2025. */
package ui.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ui.ErrorHandler;
import ui.UserInterface;
import ui.MonthlySettingsBar;
import ui.TimesheetEntry;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class JSONHandler {

	private JSONHandler() {
		// Don't allow instances of this class
	}

	private static Global globalSettings;
	private static UISettings uiSettings;

	private static String configDir;

	/**
	 * Default file name format for, as requested by KASTEL @ KIT. This is the same
	 * as {@link #DEFAULT_PDF_NAME_FORMAT_ALGO} but it connects month and year with
	 * a space instead of an underscore.
	 * <p>
	 * Format Explained: - %LAST%: Last name of the user. - %FIRST_U%: First name of
	 * the user with underscores instead of spaces. - %MM%: Two-digit month (e.g.,
	 * 01 for January). - %YYYY%: Four-digit year (e.g., 2025).
	 * </p>
	 * <p>
	 * An example would be:<br/>
	 * Mustermann_Max_Tobias_04 2025.pdf<br/>
	 * </p>
	 */
	public static final String DEFAULT_PDF_NAME_FORMAT_PROG = "%LAST%_%FIRST_U%_%MM% %YYYY%";

	/**
	 * Default file name format for, as requested by ITI @ KIT. This is the same as
	 * {@link #DEFAULT_PDF_NAME_FORMAT_PROG} but it connects month and year with an
	 * underscore as well.
	 * <p>
	 * Format Explained: - %LAST%: Last name of the user. - %FIRST_U%: First name of
	 * the user with underscores instead of spaces. - %MM%: Two-digit month (e.g.,
	 * 01 for January). - %YYYY%: Four-digit year (e.g., 2025).
	 * </p>
	 * <p>
	 * An example would be:<br/>
	 * Mustermann_Max_Tobias_04_2025.pdf<br/>
	 * </p>
	 */
	public static final String DEFAULT_PDF_NAME_FORMAT_ALGO = "%LAST%_%FIRST_U%_%MM%_%YYYY%";

	private static final String CONFIG_FILE_NAME = "global.json";
	private static final String UI_SETTINGS_FILE_NAME = "settings.json";

	private static final String ERROR = "An unexpected error occurred:%s%s".formatted(System.lineSeparator(), "%s");

	public static void initialize() {
		final String homePropertyName = "user.home";
		String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

		if (os.contains("win")) {
			configDir = System.getenv("APPDATA");
		} else if (os.contains("mac")) {
			configDir = System.getProperty(homePropertyName) + "/Library/Application Support";
		} else if (os.contains("nux") || os.contains("nix")) {
			configDir = System.getProperty(homePropertyName) + "/.config";
		} else {
			// Default to user home directory
			configDir = System.getProperty(homePropertyName);
		}

		// Create a subdirectory for your application
		configDir += "/TimeSheetGenerator";

		createDefaultGlobalSettings();
		createDefaultOtherGlobalSettings();
		loadGlobal();
		loadUiSettings();

		cleanUp();
	}

	/**
	 * Gets a copy of the current global settings.
	 * 
	 * @return Copy of global settings
	 */
	public static Global getGlobalSettings() {
		return new Global(globalSettings);
	}

	/**
	 * Gets a copy of the current additional ui settings.
	 * 
	 * @return Copy of ui settings
	 */
	public static UISettings getUISettings() {
		return new UISettings(uiSettings);
	}

	private static void setGlobalSettings(Global globalSettings) {
		JSONHandler.globalSettings = globalSettings;
	}

	private static void setUISettings(UISettings otherSettings) {
		JSONHandler.uiSettings = otherSettings;
	}

	public static void loadGlobal() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			globalSettings = objectMapper.readValue(getConfigFile(), Global.class);
		} catch (IOException e) {
			ErrorHandler.showError("Error loading global settings file", ERROR.formatted(e.getMessage()));
		}
	}

	public static void saveGlobal(Global globalSettings) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			objectMapper.writeValue(getConfigFile(), globalSettings);
			setGlobalSettings(globalSettings);
		} catch (IOException e) {
			ErrorHandler.showError("Error saving global settings file", ERROR.formatted(e.getMessage()));
		}
	}

	private static void loadUiSettings() {
		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			uiSettings = objectMapper.readValue(getUiSettingsFile(), UISettings.class);
		} catch (IOException e) {
			ErrorHandler.showError("Error loading UI settings file", ERROR.formatted(e.getMessage()));
		}
	}

	public static void saveUISettings(UISettings uiSettings) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			objectMapper.writeValue(getUiSettingsFile(), uiSettings);
			setUISettings(uiSettings);
		} catch (IOException e) {
			ErrorHandler.showError("Error saving UI settings file", ERROR.formatted(e.getMessage()));
		}
	}

	public static void loadMonth(UserInterface parentUi, File monthFile) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			Month month = objectMapper.readValue(monthFile, Month.class);

			parentUi.importMonthBarSettings(month);

			for (Month.Entry entry : month.getEntries()) {
				parentUi.addEntry(new TimesheetEntry(entry));
			}
		} catch (IOException e) {
			ErrorHandler.showError("Error loading month file", ERROR.formatted(e.getMessage()));
		}
	}

	public static boolean isFileValidMonth(File monthFile) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			objectMapper.readValue(monthFile, Month.class);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static Month getMonth(MonthlySettingsBar settingsBar, DefaultListModel<TimesheetEntry> entries) {
		Month month = new Month();
		List<Month.Entry> monthEntries = new ArrayList<>();
		settingsBar.fillMonth(month);
		entries.elements().asIterator().forEachRemaining(entry -> monthEntries.add(entry.toMonthEntry()));
		month.setEntries(monthEntries);
		return month;
	}

	public static void saveMonth(File saveFile, MonthlySettingsBar settingsBar, DefaultListModel<TimesheetEntry> entries) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			Month month = getMonth(settingsBar, entries);
			objectMapper.writeValue(saveFile, month);
		} catch (IOException e) {
			ErrorHandler.showError("Error saving month file", ERROR.formatted(e.getMessage()));
		}
	}

	public static File generateTemporaryJSONFile(MonthlySettingsBar settingsBar, DefaultListModel<TimesheetEntry> entries) {
		File f;
		do {
			f = new File(configDir, "/temp-%s.json".formatted(UUID.randomUUID()));
		} while (f.exists());
		try {
			if (!f.createNewFile()) {
				// Show error in main and return using catch block
				throw new IOException("Failed to create temporary json file %s.".formatted(f.getAbsolutePath()));
			}
		} catch (IOException e) {
			ErrorHandler.showError("Error generating temp json file", ERROR.formatted(e.getMessage()));
			return null;
		}
		saveMonth(f, settingsBar, entries);
		return f;
	}

	public static File getConfigFile() {
		return new File(configDir, CONFIG_FILE_NAME);
	}

	private static boolean globalConfigExists() {
		return getConfigFile().exists();
	}

	private static void createDefaultGlobalSettings() {
		if (globalConfigExists())
			return;
		try {
			File folder = new File(configDir);
			if (!folder.exists() && !folder.mkdirs()) {
				// Show error in main and return using catch block
				throw new IOException("Failed to create folder %s for the global configuration file.".formatted(configDir));
			}
			File f = getConfigFile();
			if (!f.createNewFile()) {
				// Show error in main and return using catch block
				throw new IOException("Failed to create global configuration file %s.".formatted(f.getAbsolutePath()));
			}
		} catch (IOException e) {
			ErrorHandler.showError("Error creating global settings file", ERROR.formatted(e.getMessage()));
			return;
		}

		Global global = new Global();
		global.setName("Max Mustermann");
		global.setStaffId(1234567);
		global.setDepartment("Fakultät für Informatik");
		global.setWorkingTime("39:00");
		global.setWage(13.98);
		global.setWorkingArea("ub");
		saveGlobal(global);
	}

	public static File getUiSettingsFile() {
		return new File(configDir, UI_SETTINGS_FILE_NAME);
	}

	private static boolean otherSettingsFileExists() {
		return getUiSettingsFile().exists();
	}

	public static void createDefaultOtherGlobalSettings() {
		if (otherSettingsFileExists())
			return;
		try {
			File f = getUiSettingsFile();
			File folder = new File(configDir);
			if (!folder.exists() && !folder.mkdirs()) {
				// Show error in main and return using catch block
				throw new IOException("Failed to access folder %s for the global settings file.".formatted(configDir));
			}
			if (!f.createNewFile()) {
				// Show error in main and return using catch block
				throw new IOException("Failed to create global settings file %s.".formatted(f.getAbsolutePath()));
			}
		} catch (IOException e) {
			ErrorHandler.showError("Error creating UI settings file", ERROR.formatted(e.getMessage()));
			return;
		}

		UISettings settings = new UISettings();
		settings.setAddSignature(false);
		settings.setAddVacationEntry(false);
		settings.setUseYYYY(false);
		settings.setUseGermanMonths(false);
		settings.setWarnOnHoursMismatch(true);
		settings.setExportPdfNameFormat(DEFAULT_PDF_NAME_FORMAT_ALGO);
		saveUISettings(settings);
	}

	private static void cleanUp() {
		File[] files = new File(configDir).listFiles();
		if (files == null)
			return;
		for (File file : files) {
			if (file.getName().startsWith("temp") && !file.delete()) {
				file.deleteOnExit();
			}
		}
	}

}
