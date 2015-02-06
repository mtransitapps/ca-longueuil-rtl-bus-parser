package org.mtransit.parser.ca_longueuil_rtl_bus;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MSpec;
import org.mtransit.parser.mt.data.MTrip;

// http://www.rtl-longueuil.qc.ca/en-CA/open-data/
// http://www.rtl-longueuil.qc.ca/en-CA/open-data/gtfs-files/
// http://www.rtl-longueuil.qc.ca/transit/latestfeed/RTL.zip
public class LongueuilRTLBusAgencyTools extends DefaultAgencyTools {

	public static final String ROUTE_TYPE_FILTER = "3"; // bus only

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-longueuil-rtl-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new LongueuilRTLBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("Generating RTL bus data...\n");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("Generating RTL bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		if (ROUTE_TYPE_FILTER != null && !gRoute.route_type.equals(ROUTE_TYPE_FILTER)) {
			return true;
		}
		return super.excludeRoute(gRoute);
	}

	private static final String AGENCY_COLOR = "A32638";

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final Pattern CLEAN_TAXI = Pattern.compile("(taxi)[\\s]*\\-[\\s]*", Pattern.CASE_INSENSITIVE);
	private static final String CLEAN_TAXI_REPLACEMENT = "Taxi ";

	@Override
	public String getRouteLongName(GRoute gRoute) {
		return cleanRouteLongName(gRoute.route_long_name);
	}

	private String cleanRouteLongName(String routeLongName) {
		routeLongName = CLEAN_TAXI.matcher(routeLongName).replaceAll(CLEAN_TAXI_REPLACEMENT);
		return MSpec.cleanLabelFR(routeLongName);
	}

	@Override
	public String getStopCode(GStop gStop) {
		return null; // no stop code
	}
	
	@Override
	public void setTripHeadsign(MRoute route, MTrip mTrip, GTrip gTrip) {
		int directionId = Integer.valueOf(gTrip.direction_id);
		String stationName = cleanTripHeadsign(gTrip.trip_headsign);
		mTrip.setHeadsignString(stationName, directionId);
	}

	private static final Pattern PLACE_CHAR_TERMINUS = Pattern.compile("(terminus )", Pattern.CASE_INSENSITIVE);
	private static final Pattern PLACE_CHAR_SECTEUR = Pattern.compile("(secteur )", Pattern.CASE_INSENSITIVE);
	private static final Pattern PLACE_CHAR_SECTEURS = Pattern.compile("(secteurs )", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanTripHeadsign(String result) {
		result = MSpec.CLEAN_SLASHES.matcher(result).replaceAll(MSpec.CLEAN_SLASHES_REPLACEMENT);
		result = Utils.replaceAll(result, MSpec.SPACE_CHARS, MSpec.SPACE);
		result = PLACE_CHAR_TERMINUS.matcher(result).replaceAll(MSpec.SPACE);
		result = PLACE_CHAR_SECTEUR.matcher(result).replaceAll(MSpec.SPACE);
		result = PLACE_CHAR_SECTEURS.matcher(result).replaceAll(MSpec.SPACE);
		result = Utils.replaceAll(result, MSpec.SPACE_ST, MSpec.SPACE);
		return MSpec.cleanLabelFR(result);
	}

	public static final Pattern RTL_LONG = Pattern.compile("(du reseau de transport de longueuil)", Pattern.CASE_INSENSITIVE);
	public static final String RTL_SHORT = "R.T.L.";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = gStopName.toLowerCase(Locale.ENGLISH); // SOURCE FILE ALL CAPS !!!
		gStopName = MSpec.CONVERT_ET_TO_SLASHES.matcher(gStopName).replaceAll(MSpec.CONVERT_ET_TO_SLASHES_REPLACEMENT);
		gStopName = RTL_LONG.matcher(gStopName).replaceAll(RTL_SHORT);
		return super.cleanStopNameFR(gStopName);
	}
}
