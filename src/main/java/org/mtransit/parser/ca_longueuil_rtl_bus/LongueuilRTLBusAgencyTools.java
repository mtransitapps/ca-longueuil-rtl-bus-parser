package org.mtransit.parser.ca_longueuil_rtl_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.mtransit.parser.StringUtils.EMPTY;

// http://www.rtl-longueuil.qc.ca/en-CA/open-data/
// http://www.rtl-longueuil.qc.ca/en-CA/open-data/gtfs-files/
// http://www.rtl-longueuil.qc.ca/transit/latestfeed/RTL.zip
public class LongueuilRTLBusAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-longueuil-rtl-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new LongueuilRTLBusAgencyTools().start(args);
	}

	@Nullable
	private HashSet<Integer> serviceIdInts;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating RTL bus data...");
		long start = System.currentTimeMillis();
		this.serviceIdInts = extractUsefulServiceIdInts(args, this, true);
		super.start(args);
		MTLog.log("Generating RTL bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIdInts != null && this.serviceIdInts.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIdInts);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIdInts);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (this.serviceIdInts != null) {
			return excludeUselessTripInt(gTrip, this.serviceIdInts);
		}
		return super.excludeTrip(gTrip);
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
	public String getRouteLongName(@NotNull GRoute gRoute) {
		return cleanRouteLongName(gRoute.getRouteLongName());
	}

	private String cleanRouteLongName(String routeLongName) {
		routeLongName = CLEAN_TAXI.matcher(routeLongName).replaceAll(CLEAN_TAXI_REPLACEMENT);
		return CleanUtils.cleanLabelFR(routeLongName);
	}

	private static final String INDUSTRIEL_SHORT = "Ind";
	private static final String SECTEUR_SHORT = EMPTY; // "Sect";
	private static final String TERMINUS_SHORT = EMPTY; // "Term";
	private static final String PARCS_INDUSTRIELS = "Parcs " + INDUSTRIEL_SHORT;
	private static final String BROSSARD = "Brossard";
	private static final String SECTEUR_PV_BROSSARD = SECTEUR_SHORT + "P-V " + BROSSARD;
	private static final String SIMARD = "Simard";
	private static final String GAREAU = "Gareau";
	private static final String MARIE_VICTORIN = "Marie-Victorin";
	private static final String SECTEUR_B_BROSSARD = SECTEUR_SHORT + "B " + BROSSARD;
	private static final String SECTEUR_M_ST_HUBERT = SECTEUR_SHORT + "M " + "St-Hubert";
	private static final String SECTEUR_M_N_O_BROSSARD = SECTEUR_SHORT + "M-N-O " + BROSSARD;
	private static final String PACIFIC = "Pacific";
	private static final String CENTRE_VILLE_SHORT = "Ctr-Ville";
	private static final String TERMINUS_CENTRE_VILLE = TERMINUS_SHORT + CENTRE_VILLE_SHORT;
	private static final String PARC_INDUSTRIEL_G_LECLERC = "Parc " + INDUSTRIEL_SHORT + " G-Leclerc";
	private static final String ARMAND_FRAPPIER = "Armand-Frappier";
	private static final String TERMINUS_PANAMA = TERMINUS_SHORT + "Panama";
	private static final String TERMINUS_LONGUEUIL = TERMINUS_SHORT + "Longueuil";
	private static final String COUSINEAU = "Cousineau";
	private static final String GRANDE_ALLEE = "Grande Allée";
	private static final String GAETAN_BOUCHER = "Gaétan-Boucher";

	private static final HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;

	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<>();
		//noinspection deprecation
		map2.put(77L, new RouteTripSpec(77L, // << WRONG trip_headsign in trips.txt
				0, MTrip.HEADSIGN_TYPE_STRING, "Parc Ind Brossard",
				1, MTrip.HEADSIGN_TYPE_STRING, "CÉGEP Édouard-Montpetit")
				.addTripSort(0,
						Arrays.asList(
								"3199", // Thurber et De Gentilly est #CEGEP
								"1165", // ++
								"3213" // Isabelle et av. Illinois #PARC_IND
						)) //
				.addTripSort(1,
						Arrays.asList(
								"3213", // Isabelle et av. Illinois #PARC_IND
								"3502", // boul. Taschereau et av. Auteuil
								"3199" // Thurber et De Gentilly est #CEGEP
						))
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(170L, new RouteTripSpec(170L, // << WRONG trip_headsign in trips.txt
				0, MTrip.HEADSIGN_TYPE_STRING, "Jacques-Cartier", // Boulevard
				1, MTrip.HEADSIGN_TYPE_STRING, "Métro Papineau")
				.addTripSort(0,
						Arrays.asList(
								"3289", // Cartier et METRO PAPINEAU
								"1865", // ++
								"3894" // Maréchal et Montarville
						)) //
				.addTripSort(1,
						Arrays.asList(
								"3894", // Maréchal et Montarville
								"1904", // boul. Jacques-Cartier ouest et de Lyon
								"3949", // ==
								"1920", // !=
								"1921", // !=
								"5087", // !=
								"3285", // != av. De Lorimier et Sainte-Catherine est
								"3287", // ==
								"3289" // Cartier et METRO PAPINEAU
						))
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, @NotNull List<MTripStop> list1, @NotNull List<MTripStop> list2, @NotNull MTripStop ts1, @NotNull MTripStop ts2, @NotNull GStop ts1GStop, @NotNull GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@NotNull
	@Override
	public ArrayList<MTrip> splitTrip(@NotNull MRoute mRoute, @Nullable GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@NotNull
	@Override
	public Pair<Long[], Integer[]> splitTripStop(@NotNull MRoute mRoute, @NotNull GTrip gTrip, @NotNull GTripStop gTripStop, @NotNull ArrayList<MTrip> splitTrips, @NotNull GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		mTrip.setHeadsignString(
				cleanTripHeadsign(gTrip.getTripHeadsignOrDefault()),
				gTrip.getDirectionIdOrDefault()
		);
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (Arrays.asList( //
				TERMINUS_PANAMA, //
				TERMINUS_CENTRE_VILLE //
		).containsAll(headsignsValues)) {
			mTrip.setHeadsignString(TERMINUS_CENTRE_VILLE, mTrip.getHeadsignId());
			return true;
		}
		if (mTrip.getRouteId() == 5L) {
			if (Arrays.asList( //
					"Gaétan-Boucher", //
					SECTEUR_M_ST_HUBERT //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SECTEUR_M_ST_HUBERT, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 25L) {
			if (Arrays.asList( //
					TERMINUS_LONGUEUIL, //
					PARCS_INDUSTRIELS //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(PARCS_INDUSTRIELS, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 30L) {
			if (Arrays.asList( //
					TERMINUS_CENTRE_VILLE, //
					SECTEUR_PV_BROSSARD //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SECTEUR_PV_BROSSARD, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 33L) {
			if (Arrays.asList( //
					TERMINUS_CENTRE_VILLE, //
					SECTEUR_M_N_O_BROSSARD //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SECTEUR_M_N_O_BROSSARD, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 37L) {
			if (Arrays.asList( //
					TERMINUS_CENTRE_VILLE, //
					SIMARD //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SIMARD, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 59L) {
			if (Arrays.asList( //
					TERMINUS_CENTRE_VILLE, //
					GAREAU //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(GAREAU, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 82L) {
			if (Arrays.asList( //
					TERMINUS_LONGUEUIL, //
					MARIE_VICTORIN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(MARIE_VICTORIN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 106L) {
			if (Arrays.asList( //
					TERMINUS_LONGUEUIL, //
					SECTEUR_B_BROSSARD //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SECTEUR_B_BROSSARD, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 142L) {
			if (Arrays.asList( //
					COUSINEAU, // <>
					PACIFIC //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(PACIFIC, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					COUSINEAU, // <>
					TERMINUS_CENTRE_VILLE //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(TERMINUS_CENTRE_VILLE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 821L) { // T21
			if (Arrays.asList( //
					GRANDE_ALLEE, //
					ARMAND_FRAPPIER //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ARMAND_FRAPPIER, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 822L) { // T22
			if (Arrays.asList( //
					GAETAN_BOUCHER, //
					PARC_INDUSTRIEL_G_LECLERC //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(PARC_INDUSTRIEL_G_LECLERC, mTrip.getHeadsignId());
				return true;
			}
		}
		throw new MTLog.Fatal("Unexpected trips to merge %s & %s", mTrip, mTripToMerge);
	}

	private static final Pattern CIVIQUE_ = Pattern.compile("((^|\\W)(" + "civique ([\\d]+)" + ")(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String CIVIQUE_REPLACEMENT = "$2" + "#$4" + "$5";

	private static final Pattern CENTRE_VILLE_ = CleanUtils.cleanWords("centre-ville");
	private static final String CENTRE_VILLE_REPLACEMENT = CleanUtils.cleanWordsReplacement(CENTRE_VILLE_SHORT);

	private static final Pattern TERMINUS_ = CleanUtils.cleanWords("terminus");
	private static final String TERMINUS_REPLACEMENT = CleanUtils.cleanWordsReplacement(TERMINUS_SHORT.trim());

	private static final Pattern SECTEUR_ = CleanUtils.cleanWords("secteur", "secteurs");
	private static final String SECTEUR_REPLACEMENT = CleanUtils.cleanWordsReplacement(SECTEUR_SHORT.trim());

	private static final Pattern INDUSTRIEL_ = CleanUtils.cleanWords("industriel", "industriels");
	private static final String INDUSTRIEL_REPLACEMENT = CleanUtils.cleanWordsReplacement(INDUSTRIEL_SHORT);

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.cleanSlashes(tripHeadsign);
		tripHeadsign = Utils.replaceAll(tripHeadsign, CleanUtils.SPACE_CHARS, CleanUtils.SPACE);
		tripHeadsign = CENTRE_VILLE_.matcher(tripHeadsign).replaceAll(CENTRE_VILLE_REPLACEMENT);
		tripHeadsign = CIVIQUE_.matcher(tripHeadsign).replaceAll(CIVIQUE_REPLACEMENT);
		tripHeadsign = TERMINUS_.matcher(tripHeadsign).replaceAll(TERMINUS_REPLACEMENT);
		tripHeadsign = SECTEUR_.matcher(tripHeadsign).replaceAll(SECTEUR_REPLACEMENT);
		tripHeadsign = INDUSTRIEL_.matcher(tripHeadsign).replaceAll(INDUSTRIEL_REPLACEMENT);
		tripHeadsign = Utils.replaceAll(tripHeadsign, CleanUtils.SPACE_ST, CleanUtils.SPACE);
		tripHeadsign = CleanUtils.cleanStreetTypesFRCA(tripHeadsign);
		return CleanUtils.cleanLabelFR(tripHeadsign);
	}

	private static final Pattern RTL_LONG = Pattern.compile("(du reseau de transport de longueuil)", Pattern.CASE_INSENSITIVE);
	private static final String RTL_SHORT = "RTL";

	private String[] getIgnoredWords() {
		return new String[]{
				"CIBC", "ENA", "IGA", "RTL",
		};
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.FRENCH, gStopName, getIgnoredWords());
		gStopName = CleanUtils.CLEAN_ET.matcher(gStopName).replaceAll(CleanUtils.CLEAN_ET_REPLACEMENT);
		gStopName = RTL_LONG.matcher(gStopName).replaceAll(RTL_SHORT);
		gStopName = CIVIQUE_.matcher(gStopName).replaceAll(CIVIQUE_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypesFRCA(gStopName);
		return CleanUtils.cleanLabelFR(gStopName);
	}
}
