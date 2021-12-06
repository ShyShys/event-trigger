package gg.xp.xivdata.jobs;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatusEffectIcon implements HasIconURL {

	private static final Logger log = LoggerFactory.getLogger(StatusEffectIcon.class);

	private final URL url;

	private static boolean loaded;
	private static final Map<Long, StatusEffectIcon> cache = new HashMap<>();
	private static final Map<Long, Long> csvValues = new HashMap<>();

	private static void readCsv() {
		List<String[]> arrays;
		try (CSVReader csvReader = new CSVReader(new InputStreamReader(StatusEffectIcon.class.getResourceAsStream("/xiv/statuseffect/Status.csv")))) {
			arrays = csvReader.readAll();
		}
		catch (IOException | CsvException e) {
			log.error("Could not load icons!", e);
			return;
		}
		finally {
			loaded = true;
		}
		arrays.forEach(row -> {
			long id;
			try {
				id = Long.parseLong(row[0]);
			}
			catch (NumberFormatException nfe) {
				// Ignore the bad value at the top
				return;
			}
			String rawImg = row[3];
			if (rawImg.isEmpty()) {
				return;
			}
			long imageId;
			try {
				imageId = Long.parseLong(rawImg);
			}
			catch (NumberFormatException nfe) {
				Matcher matcher = texFilePattern.matcher(rawImg);
				if (matcher.find()) {
					imageId = Long.parseLong(matcher.group(1));
				}
				else {
					throw new RuntimeException("Invalid image specifier: " + rawImg);
					// Ignore non-numeric
//					return;
				}
			}
			if (imageId != 0) {
				csvValues.put(id, imageId);
			}
		});

		// If we fail, it's always going to fail, so continue without icons.
	}

	private static final Pattern texFilePattern = Pattern.compile("(\\d+)\\.tex");

	public static void main(String[] args) {
		readCsv();
		csvValues.values().stream().distinct().sorted().map(s -> String.format("%06d", s)).forEach(System.out::println);
	}

	// Special value to indicate no icon
	private static final StatusEffectIcon NULL_MARKER;

	static {
		try {
			NULL_MARKER = new StatusEffectIcon(new URL("http://bar/"));
		}
		catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public static StatusEffectIcon forId(long id) {
		if (!loaded) {
			readCsv();
		}
		StatusEffectIcon result = cache.computeIfAbsent(id, missingId -> {
			URL resource = StatusEffectIcon.class.getResource(String.format("/xiv/statuseffect/icons/%06d_hr1.png", csvValues.get(missingId)));
			if (resource == null) {
				return NULL_MARKER;
			}
			return new StatusEffectIcon(resource);
		});
		if (result == NULL_MARKER) {
			return null;
		}
		return result;
	}

	private StatusEffectIcon(URL url) {
		this.url = url;
	}

	@Override
	public URL getIcon() {
		return url;
	}
}
