package au.org.nifpi.cerei;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dates in Energy Usage, Spot Price, Energy Used and Feed-in Tariff files have varying formats.  This class used to parse the various formats.
 * 
 * @author Copyright (c) 2023 University of Technology Sydney and Federation University under MIT License.
 */
public class DateParser {

	/** Date formats that can be parsed */
	List<DateTimeFormatter> knownPatterns; 

	/**
	 * Essentially the list of known date formats.  Additional formats can be added if they turn up.
	 */
	public DateParser() {
		knownPatterns = new ArrayList<DateTimeFormatter>();
		
		// Create a bunch of date formats.  Make sure to set the Locale to English as this has caused problems on some computers that had a different native Locale setting.
		// d-MM-uu
		knownPatterns.add(
				new DateTimeFormatterBuilder()
				.parseCaseInsensitive()
				.appendPattern("d-MM-uu")
				.optionalStart()
				.appendPattern(" H:mm")
				.optionalEnd()
				.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
				.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
				.toFormatter(Locale.ENGLISH));
		// d-MM-uuuu
		knownPatterns.add(
				new DateTimeFormatterBuilder()
				.parseCaseInsensitive()
				.appendPattern("d-MM-uuuu")
				.optionalStart()
				.appendPattern(" H:mm")
				.optionalEnd()
				.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
				.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
				.toFormatter(Locale.ENGLISH));
		// d-MMM-uu
		knownPatterns.add(
				new DateTimeFormatterBuilder()
				.parseCaseInsensitive()
				.appendPattern("d-MMM-uu")
				.optionalStart()
				.appendPattern(" H:mm")
				.optionalEnd()
				.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
				.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
				.toFormatter(Locale.ENGLISH));
		// d-MMM-uuuu
		knownPatterns.add(
				new DateTimeFormatterBuilder()
				.parseCaseInsensitive()
				.appendPattern("d-MMM-uuuu")
				.optionalStart()
				.appendPattern(" H:mm")
				.optionalEnd()
				.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
				.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
				.toFormatter(Locale.ENGLISH));
		// d/MM/uu
		knownPatterns.add(
				new DateTimeFormatterBuilder()
				.parseCaseInsensitive()
				.appendPattern("d/MM/uu")
				.optionalStart()
				.appendPattern(" H:mm")
				.optionalEnd()
				.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
				.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
				.toFormatter(Locale.ENGLISH));
		// d/MM/uuuu
		knownPatterns.add(
				new DateTimeFormatterBuilder()
				.parseCaseInsensitive()
				.appendPattern("d/MM/uuuu")
				.optionalStart()
				.appendPattern(" H:mm")
				.optionalEnd()
				.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
				.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
				.toFormatter(Locale.ENGLISH));
		// d/MMM/uu
		knownPatterns.add(
				new DateTimeFormatterBuilder()
				.parseCaseInsensitive()
				.appendPattern("d/MMM/uu")
				.optionalStart()
				.appendPattern(" H:mm")
				.optionalEnd()
				.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
				.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
				.toFormatter(Locale.ENGLISH));
		// d/MMM/uuuu
		knownPatterns.add(
				new DateTimeFormatterBuilder()
				.parseCaseInsensitive()
				.appendPattern("d/MMM/uuuu")
				.optionalStart()
				.appendPattern(" H:mm")
				.optionalEnd()
				.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
				.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
				.toFormatter(Locale.ENGLISH));
	}
	
	/**
	 * Parse a date time string against a number of formats.
	 * 
	 * @param dateTimeString The string to be parsed
	 * @param file The name of the file that contains the string - so user can be alerted to a specific line in a specific file.
	 * @param lineNumber The line number of the dateTimeString in the file  - so user can be alerted to a specific line in a specific file.
	 * 
	 * @throws Exception If the dateTimeString cannot be parsed against a known format.
	 * 
	 * @return parsed dateTime in a form that is easy to process.
	 */
	public LocalDateTime parseDateTime(String dateTimeString, String file, int lineNumber) throws Exception {
		for (DateTimeFormatter pattern : knownPatterns) {
		    try {
		        // Take a try
		        return LocalDateTime.parse(dateTimeString, pattern);

		    } catch (Exception pe) {
		        // Loop on
		    }
		}
		throw new Exception("Unknown date format in "+file+". Line number: "+lineNumber);
	}
	
}
