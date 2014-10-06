package org.mtransit.parser.ca_longueuil_rtl_bus;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MSpec;
import org.mtransit.parser.mt.data.MTrip;

// http://www.rtl-longueuil.qc.ca/en-CA/open-data/gtfs-files/
// http://www.rtl-longueuil.qc.ca/transit/latestfeed/RTL.zip
public class MontrealAMTBusAgencyTools extends DefaultAgencyTools {

	public static final String ROUTE_TYPE_FILTER = "3"; // bus only

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../ca-longueuil-rtl-bus/res/raw/";
			args[2] = ""; // files-prefix
		}
		new MontrealAMTBusAgencyTools().start(args);
	}

	@Override
	public void start(String[] args) {
		System.out.printf("Generating RTL bus data...\n");
		long start = System.currentTimeMillis();
		super.start(args);
		System.out.printf("Generating RTL bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		if (ROUTE_TYPE_FILTER != null && !gRoute.route_type.equals(ROUTE_TYPE_FILTER)) {
			return true;
		}
		if (gRoute.route_short_name.startsWith("T")) { // exclude Taxi for now
			return true;
		}
		return super.excludeRoute(gRoute);
	}

	@Override
	public String getRouteShortName(GRoute gRoute) {
		return StringUtils.leftPad(gRoute.route_short_name, 3); // route short name length = 3
	}
	
	@Override
	public String getStopCode(GStop gStop) {
		return null; // no stop code
	}
	
	@Override
	public void setTripHeadsign(MTrip mTrip, GTrip gTrip) {
		int directionId = Integer.valueOf(gTrip.direction_id);
		String stationName = cleanTripHeading(gTrip.trip_headsign);
		mTrip.setHeadsignString(stationName, directionId);
	}
	
	private static final String PLACE_CHAR_TERMINUS = "terminus ";
	private static final String PLACE_CHAR_SECTEUR = "secteur ";
	private static final String PLACE_CHAR_SECTEURS = "secteurs ";
	private static final String PLACE_CHAR_ARRONDISSEMENT = "arrondissement ";
	private static final String PLACE_CHAR_BOULEVARD = "boulevard";

	private String cleanTripHeading(String heading) {
		String result = heading.toLowerCase(Locale.ENGLISH);
		if (result.contains(PLACE_CHAR_TERMINUS)) {
			result = result.replace(PLACE_CHAR_TERMINUS, " ");
		}
		if (result.contains(PLACE_CHAR_SECTEUR)) {
			result = result.replace(PLACE_CHAR_SECTEUR, " ");
		}
		if (result.contains(PLACE_CHAR_SECTEURS)) {
			result = result.replace(PLACE_CHAR_SECTEURS, " ");
		}
		if (result.contains(PLACE_CHAR_ARRONDISSEMENT)) {
			result = result.replace(PLACE_CHAR_ARRONDISSEMENT, " ");
		}
		if (result.contains(PLACE_CHAR_BOULEVARD)) {
			result = result.replace(PLACE_CHAR_BOULEVARD, " ");
		}
		return cleanStopName(result);
	}
	
	private static final String PLACE_CHAR_ET = " et ";
	private static final String PLACE_CHAR_AV = "av. ";
	private static final String PLACE_CHAR_CH = "ch. ";
	private static final String PLACE_CHAR_BOUL = "boul. ";
	private static final String PLACE_CHAR_RTE = "rte ";
	private static final String PLACE_CHAR_TSSE = "tsse ";
	private static final String PLACE_CHAR_SAINT = "saint";

	private static final String PLACE_CHAR_D = "d'";
	private static final String PLACE_CHAR_DE = "de ";
	private static final String PLACE_CHAR_DES = "des ";
	private static final String PLACE_CHAR_DU = "du ";
	private static final String PLACE_CHAR_LA = "la ";
	private static final String PLACE_CHAR_LE = "le ";
	private static final String PLACE_CHAR_LES = "les ";
	private static final String PLACE_CHAR_L = "l'";

	private static final String[] REMOVE_CHARS = new String[] { PLACE_CHAR_D, PLACE_CHAR_DE, PLACE_CHAR_DES, PLACE_CHAR_DU, PLACE_CHAR_LA, PLACE_CHAR_LE,
			PLACE_CHAR_LES, PLACE_CHAR_L };

	private static final String[] REPLACE_CHARS = new String[] { " " + PLACE_CHAR_D, " " + PLACE_CHAR_DE, " " + PLACE_CHAR_DES, " " + PLACE_CHAR_DU,
			" " + PLACE_CHAR_LA, " " + PLACE_CHAR_LE, " " + PLACE_CHAR_LES, " " + PLACE_CHAR_L };

	@Override
	public String cleanStopName(String gStopName) {
		String result = gStopName.toLowerCase(Locale.ENGLISH);
		if (result.contains(PLACE_CHAR_ET)) {
			result = result.replace(PLACE_CHAR_ET, " / ");
		}
		result = MSpec.CLEAN_SLASHES.matcher(result).replaceAll(MSpec.CLEAN_SLASHES_REPLACEMENT);
		if (result.contains(PLACE_CHAR_AV)) {
			result = result.replace(PLACE_CHAR_AV, " ");
		}
		if (result.contains(PLACE_CHAR_CH)) {
			result = result.replace(PLACE_CHAR_CH, " ");
		}
		if (result.contains(PLACE_CHAR_BOUL)) {
			result = result.replace(PLACE_CHAR_BOUL, " ");
		}
		if (result.contains(PLACE_CHAR_RTE)) {
			result = result.replace(PLACE_CHAR_RTE, " ");
		}
		if (result.contains(PLACE_CHAR_TSSE)) {
			result = result.replace(PLACE_CHAR_TSSE, " ");
		}
		if (result.contains(PLACE_CHAR_SAINT)) {
			result = result.replace(PLACE_CHAR_SAINT, "St");
		}
		result = MSpec.removeStartWith(result, REMOVE_CHARS, 1); // 1 = keep space
		result = MSpec.replaceAll(result, REPLACE_CHARS, " ");
		return MSpec.cleanLabel(result);
	}
}
