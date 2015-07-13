package org.mtransit.parser.ca_longueuil_rtl_bus;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.mt.data.MTrip;

// http://www.rtl-longueuil.qc.ca/en-CA/open-data/
// http://www.rtl-longueuil.qc.ca/en-CA/open-data/gtfs-files/
// http://www.rtl-longueuil.qc.ca/transit/latestfeed/RTL.zip
public class LongueuilRTLBusAgencyTools extends DefaultAgencyTools {

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
		System.out.printf("\nGenerating RTL bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating RTL bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
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
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
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
		return CleanUtils.cleanLabelFR(routeLongName);
	}

	@Override
	public String getStopCode(GStop gStop) {
		return null; // no stop code
	}
	
	private static final String ROUTE_25_TRIP_0_NAME = "Parcs Industriels";
	private static final String ROUTE_30_TRIP_0_NAME = "P-V Brossard";
	private static final String ROUTE_37_TRIP_0_NAME = "Simard";
	private static final String ROUTE_59_TRIP_0_NAME = "Gareau";
	private static final String ROUTE_82_TRIP_0_NAME = "Marie-Victorin";
	private static final String ROUTE_106_TRIP_0_NAME = "B Brossard";
	private static final String ROUTE_142_TRIP_0_NAME = "Pacific";
	private static final String ROUTE_142_TRIP_1_NAME = "Centre-Ville";
	private static final String ROUTE_822_TRIP_1_NAME = "Parc Industriel G-Leclerc";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		String stationName = cleanTripHeadsign(gTrip.trip_headsign);
		if (mRoute.id == 25L) {
			if (gTrip.direction_id == 0) {
				stationName = ROUTE_25_TRIP_0_NAME;
			}
		} else if (mRoute.id == 30L) {
			if (gTrip.direction_id == 0) {
				stationName = ROUTE_30_TRIP_0_NAME;
			}
		} else if (mRoute.id == 37L) {
			if (gTrip.direction_id == 0) {
				stationName = ROUTE_37_TRIP_0_NAME;
			}
		} else if (mRoute.id == 59L) {
			if (gTrip.direction_id == 0) {
				stationName = ROUTE_59_TRIP_0_NAME;
			}
		} else if (mRoute.id == 82L) {
			if (gTrip.direction_id == 0) {
				stationName = ROUTE_82_TRIP_0_NAME;
			}
		} else if (mRoute.id == 106L) {
			if (gTrip.direction_id == 0) {
				stationName = ROUTE_106_TRIP_0_NAME;
			}
		} else if (mRoute.id == 142L) {
			if (gTrip.direction_id == 0) {
				stationName = ROUTE_142_TRIP_0_NAME;
			} else if (gTrip.direction_id == 1) {
				stationName = ROUTE_142_TRIP_1_NAME;
			}
		} else if (mRoute.id == 822L) {
			if (gTrip.direction_id == 1) {
				stationName = ROUTE_822_TRIP_1_NAME;
			}
		}
		mTrip.setHeadsignString(stationName, gTrip.direction_id);
	}

	private static final Pattern PLACE_CHAR_TERMINUS = Pattern.compile("(terminus )", Pattern.CASE_INSENSITIVE);
	private static final Pattern PLACE_CHAR_SECTEUR = Pattern.compile("(secteur )", Pattern.CASE_INSENSITIVE);
	private static final Pattern PLACE_CHAR_SECTEURS = Pattern.compile("(secteurs )", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanTripHeadsign(String result) {
		result = CleanUtils.CLEAN_SLASHES.matcher(result).replaceAll(CleanUtils.CLEAN_SLASHES_REPLACEMENT);
		result = Utils.replaceAll(result, CleanUtils.SPACE_CHARS, CleanUtils.SPACE);
		result = PLACE_CHAR_TERMINUS.matcher(result).replaceAll(CleanUtils.SPACE);
		result = PLACE_CHAR_SECTEUR.matcher(result).replaceAll(CleanUtils.SPACE);
		result = PLACE_CHAR_SECTEURS.matcher(result).replaceAll(CleanUtils.SPACE);
		result = Utils.replaceAll(result, CleanUtils.SPACE_ST, CleanUtils.SPACE);
		return CleanUtils.cleanLabelFR(result);
	}

	public static final Pattern RTL_LONG = Pattern.compile("(du reseau de transport de longueuil)", Pattern.CASE_INSENSITIVE);
	public static final String RTL_SHORT = "R.T.L.";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = gStopName.toLowerCase(Locale.ENGLISH); // SOURCE FILE ALL CAPS !!!
		gStopName = CleanUtils.CONVERT_ET_TO_SLASHES.matcher(gStopName).replaceAll(CleanUtils.CONVERT_ET_TO_SLASHES_REPLACEMENT);
		gStopName = RTL_LONG.matcher(gStopName).replaceAll(RTL_SHORT);
		gStopName = CleanUtils.cleanStreetTypesFRCA(gStopName);
		return CleanUtils.cleanLabelFR(gStopName);
	}
}
