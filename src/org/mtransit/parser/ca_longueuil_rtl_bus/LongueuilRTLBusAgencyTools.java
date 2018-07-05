package org.mtransit.parser.ca_longueuil_rtl_bus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
		return cleanRouteLongName(gRoute.getRouteLongName());
	}

	private String cleanRouteLongName(String routeLongName) {
		routeLongName = CLEAN_TAXI.matcher(routeLongName).replaceAll(CLEAN_TAXI_REPLACEMENT);
		return CleanUtils.cleanLabelFR(routeLongName);
	}

	@Override
	public String getStopCode(GStop gStop) {
		return null; // no stop code
	}

	private static final String INDUSTRIEL_SHORT = "Ind.";
	private static final String INDUSTRIELS_SHORT = "Ind.";
	private static final String PARCS_INDUSTRIELS = "Parcs " + INDUSTRIELS_SHORT;
	private static final String BROSSARD = "Brossard";
	private static final String PV_BROSSARD = "P-V " + BROSSARD;
	private static final String SIMARD = "Simard";
	private static final String GAREAU = "Gareau";
	private static final String MARIE_VICTORIN = "Marie-Victorin";
	private static final String B_BROSSARD = "B " + BROSSARD;
	private static final String M_N_O_BROSSARD = "M-N-O " + BROSSARD;
	private static final String PACIFIC = "Pacific";
	private static final String CENTRE_VILLE = "Centre-Ville";
	private static final String PARC_INDUSTRIEL_G_LECLERC = "Parc " + INDUSTRIEL_SHORT + " G-Leclerc";
	private static final String ARMAND_FRAPPIER = "Armand-Frappier";
	private static final String PANAMA = "Panama";
	private static final String LONGUEUIL = "Longueuil";
	private static final String COUSINEAU = "Cousineau";
	private static final String GRANDE_ALLEE = "Grande Allée";
	private static final String GAETAN_BOUCHER = "Gaétan-Boucher";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), gTrip.getDirectionId());
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 5L) {
			if (Arrays.asList( //
					PANAMA, //
					CENTRE_VILLE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CENTRE_VILLE, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Gaétan-Boucher", //
					"M St-Hubert" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("M St-Hubert", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 15L) {
			if (Arrays.asList( //
					PANAMA, //
					CENTRE_VILLE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CENTRE_VILLE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 25L) {
			if (Arrays.asList( //
					LONGUEUIL, //
					PARCS_INDUSTRIELS //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(PARCS_INDUSTRIELS, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 30L) {
			if (Arrays.asList( //
					CENTRE_VILLE, //
					PV_BROSSARD //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(PV_BROSSARD, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 32L) {
			if (Arrays.asList( //
					PANAMA, //
					CENTRE_VILLE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CENTRE_VILLE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 33L) {
			if (Arrays.asList( //
					CENTRE_VILLE, //
					M_N_O_BROSSARD //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(M_N_O_BROSSARD, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 35L) {
			if (Arrays.asList( //
					PANAMA, //
					CENTRE_VILLE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CENTRE_VILLE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 37L) {
			if (Arrays.asList( //
					CENTRE_VILLE, //
					SIMARD //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SIMARD, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 42L) {
			if (Arrays.asList( //
					PANAMA, //
					CENTRE_VILLE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CENTRE_VILLE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 44L) {
			if (Arrays.asList( //
					PANAMA, //
					CENTRE_VILLE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CENTRE_VILLE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 47L) {
			if (Arrays.asList( //
					PANAMA, //
					CENTRE_VILLE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CENTRE_VILLE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 49L) {
			if (Arrays.asList( //
					PANAMA, //
					CENTRE_VILLE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CENTRE_VILLE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 50L) {
			if (Arrays.asList( //
					PANAMA, //
					CENTRE_VILLE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CENTRE_VILLE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 59L) {
			if (Arrays.asList( //
					CENTRE_VILLE, //
					GAREAU //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(GAREAU, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 60L) {
			if (Arrays.asList( //
					PANAMA, //
					CENTRE_VILLE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CENTRE_VILLE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 82L) {
			if (Arrays.asList( //
					LONGUEUIL, //
					MARIE_VICTORIN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(MARIE_VICTORIN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 106L) {
			if (Arrays.asList( //
					LONGUEUIL, //
					B_BROSSARD //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(B_BROSSARD, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 132L) {
			if (Arrays.asList( //
					PANAMA, //
					CENTRE_VILLE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CENTRE_VILLE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 142L) {
			if (Arrays.asList( //
					COUSINEAU, //
					PACIFIC //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(PACIFIC, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					COUSINEAU, //
					CENTRE_VILLE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CENTRE_VILLE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 821L) {
			if (Arrays.asList( //
					GRANDE_ALLEE, //
					ARMAND_FRAPPIER //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ARMAND_FRAPPIER, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 822L) {
			if (Arrays.asList( //
					GAETAN_BOUCHER, //
					PARC_INDUSTRIEL_G_LECLERC //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(PARC_INDUSTRIEL_G_LECLERC, mTrip.getHeadsignId());
				return true;
			}
		}
		System.out.printf("\nUnexpected trips to merge %s & %s\n", mTrip, mTripToMerge);
		System.exit(-1);
		return false;
	}

	private static final Pattern TERMINUS = Pattern.compile("((^|\\W){1}(terminus)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final Pattern SECTEUR = Pattern.compile("((^|\\W){1}(secteur)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final Pattern SECTEURS = Pattern.compile("((^|\\W){1}(secteurs)(\\W|$){1})", Pattern.CASE_INSENSITIVE);

	private static final Pattern INDUSTRIEL = Pattern.compile("((^|\\W){1}(industriel)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String INDUSTRIEL_REPLACEMENT = "$2" + INDUSTRIEL_SHORT + "$4";

	private static final Pattern INDUSTRIELS = Pattern.compile("((^|\\W){1}(industriels)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String INDUSTRIELS_REPLACEMENT = "$2" + INDUSTRIELS_SHORT + "$4";

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = CleanUtils.cleanSlashes(tripHeadsign);
		tripHeadsign = Utils.replaceAll(tripHeadsign, CleanUtils.SPACE_CHARS, CleanUtils.SPACE);
		tripHeadsign = TERMINUS.matcher(tripHeadsign).replaceAll(CleanUtils.SPACE);
		tripHeadsign = SECTEUR.matcher(tripHeadsign).replaceAll(CleanUtils.SPACE);
		tripHeadsign = SECTEURS.matcher(tripHeadsign).replaceAll(CleanUtils.SPACE);
		tripHeadsign = INDUSTRIEL.matcher(tripHeadsign).replaceAll(INDUSTRIEL_REPLACEMENT);
		tripHeadsign = INDUSTRIELS.matcher(tripHeadsign).replaceAll(INDUSTRIELS_REPLACEMENT);
		tripHeadsign = Utils.replaceAll(tripHeadsign, CleanUtils.SPACE_ST, CleanUtils.SPACE);
		tripHeadsign = CleanUtils.cleanStreetTypesFRCA(tripHeadsign);
		return CleanUtils.cleanLabelFR(tripHeadsign);
	}

	public static final Pattern RTL_LONG = Pattern.compile("(du reseau de transport de longueuil)", Pattern.CASE_INSENSITIVE);
	public static final String RTL_SHORT = "R.T.L.";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = gStopName.toLowerCase(Locale.ENGLISH); // SOURCE FILE ALL CAPS !!!
		gStopName = CleanUtils.CLEAN_ET.matcher(gStopName).replaceAll(CleanUtils.CLEAN_ET_REPLACEMENT);
		gStopName = RTL_LONG.matcher(gStopName).replaceAll(RTL_SHORT);
		gStopName = CleanUtils.cleanStreetTypesFRCA(gStopName);
		return CleanUtils.cleanLabelFR(gStopName);
	}
}
