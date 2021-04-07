package org.mtransit.parser.ca_longueuil_rtl_bus;

import org.jetbrains.annotations.NotNull;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.RegexUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.mt.data.MAgency;

import java.util.Locale;
import java.util.regex.Pattern;

// https://www.rtl-longueuil.qc.ca/en-CA/open-data/
// https://www.rtl-longueuil.qc.ca/en-CA/open-data/gtfs-files/
// https://www.rtl-longueuil.qc.ca/transit/latestfeed/RTL.zip
public class LongueuilRTLBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new LongueuilRTLBusAgencyTools().start(args);
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "RTL";
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final String AGENCY_COLOR = "A32638";

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final Pattern CLEAN_TAXI = Pattern.compile("(taxi)[\\s]*-[\\s]*", Pattern.CASE_INSENSITIVE);
	private static final String CLEAN_TAXI_REPLACEMENT = "Taxi ";

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = CLEAN_TAXI.matcher(routeLongName).replaceAll(CLEAN_TAXI_REPLACEMENT);
		return CleanUtils.cleanLabelFR(routeLongName);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern CIVIQUE_ = Pattern.compile("((^|\\W)(" + "civique ([\\d]+)" + ")(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String CIVIQUE_REPLACEMENT = "$2" + "#$4" + "$5";

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.cleanSlashes(tripHeadsign);
		tripHeadsign = RegexUtils.replaceAllNN(tripHeadsign, CleanUtils.SPACE_CHARS, CleanUtils.SPACE);
		tripHeadsign = CIVIQUE_.matcher(tripHeadsign).replaceAll(CIVIQUE_REPLACEMENT);
		tripHeadsign = RTL_LONG.matcher(tripHeadsign).replaceAll(RTL_SHORT);
		tripHeadsign = RegexUtils.replaceAllNN(tripHeadsign, CleanUtils.SPACE_ST, CleanUtils.SPACE);
		tripHeadsign = CleanUtils.cleanBounds(Locale.FRENCH, tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypesFRCA(tripHeadsign);
		return CleanUtils.cleanLabelFR(tripHeadsign);
	}

	private static final Pattern RTL_LONG = Pattern.compile("((du )?r[eé]seau (de )?transport (de )?longueuil)",
			Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.CANON_EQ);
	private static final String RTL_SHORT = "RTL";

	private String[] getIgnoredWords() {
		return new String[]{
				"CIBC", "ENA", "IGA", "RTL",
				"DIX30", "DIX",
		};
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.FRENCH, gStopName, getIgnoredWords());
		gStopName = CleanUtils.CLEAN_ET.matcher(gStopName).replaceAll(CleanUtils.CLEAN_ET_REPLACEMENT);
		gStopName = RTL_LONG.matcher(gStopName).replaceAll(RTL_SHORT);
		gStopName = CIVIQUE_.matcher(gStopName).replaceAll(CIVIQUE_REPLACEMENT);
		gStopName = CleanUtils.cleanBounds(Locale.FRENCH, gStopName);
		gStopName = CleanUtils.cleanStreetTypesFRCA(gStopName);
		return CleanUtils.cleanLabelFR(gStopName);
	}
}
