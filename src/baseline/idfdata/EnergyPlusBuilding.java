package baseline.idfdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import baseline.generator.EplusObject;
import baseline.generator.IdfReader;
import baseline.generator.OutdoorDesignSpecification;
import baseline.util.ClimateZone;

public class EnergyPlusBuilding {

    private Double totalFloorArea;
    private Double conditionedFloorArea;
    private Set<String> floorSet;

    private Double heatingSetPointNotMet;
    private Double coolingSetPointNotMet;

    private Double totalCoolingLoad;
    private Double totalHeatingLoad;

    private ClimateZone cZone;

    private List<ThermalZone> thermalZoneList;
    private HashMap<String, ArrayList<ThermalZone>> floorMap;

    private IdfReader baselineModel;

    public EnergyPlusBuilding(ClimateZone zone, IdfReader baselineModel) {
	thermalZoneList = new ArrayList<ThermalZone>();
	floorMap = new HashMap<String, ArrayList<ThermalZone>>();
	totalCoolingLoad = 0.0;
	totalHeatingLoad = 0.0;
	cZone = zone;
	this.baselineModel = baselineModel;
    }

    /**
     * Reload the data from energyplus output.
     */
    public void initializeBuildingData() {
	thermalZoneList.clear();
	floorMap.clear();
	floorSet = new HashSet<String>();
	totalCoolingLoad = 0.0;
	totalHeatingLoad = 0.0;
    }

    /*
     * All Setter Methods
     */
    public void setTotalFloorArea(Double area) {
	totalFloorArea = area;
    }

    public void setConditionedFloorArea(Double area) {
	conditionedFloorArea = area;
    }

    public void setHeatTimeSetPointNotMet(Double hr) {
	heatingSetPointNotMet = hr;
    }

    public void setCoolTimeSetPointNotMet(Double hr) {
	coolingSetPointNotMet = hr;
    }

    /**
     * add thermal zones to the data structure
     * 
     * @param zone
     */
    public void addThermalZone(ThermalZone zone) {
	thermalZoneList.add(zone);
    }

    /**
     * This method must be called prior to get floorMap, get totalcoolingload
     * and get total heating load
     * 
     * @return
     */
    public void getThermalZoneInfo() {
	// building the thermal zones
	for (ThermalZone zone : thermalZoneList) {
	    String block = zone.getBlock();
	    String floor = zone.getFloor();
	    floorSet.add(floor);
	    String level = block + ":" + floor;
	    if (!floorMap.containsKey(level)) {
		floorMap.put(level, new ArrayList<ThermalZone>());
	    }
	    zone.setOAVentilation(getDesignOutdoorAir(zone.getFullName()));
	    floorMap.get(level).add(zone);
	    totalCoolingLoad += zone.getCoolingLoad();
	    totalHeatingLoad += zone.getHeatingLoad();
	}
    }

    /**
     * get the floor map
     * 
     * @return
     */
    public HashMap<String, ArrayList<ThermalZone>> getFloorMap() {
	return floorMap;
    }

    public IdfReader getBaselineModel() {
	return baselineModel;
    }

    public ClimateZone getClimateZone() {
	return cZone;
    }

    /**
     * get the total cooling load
     * 
     * @return
     */
    public Double getTotalCoolingLoad() {
	return Math.round(totalCoolingLoad * 100.0) / 100.0;
    }

    /**
     * get the total heating load
     * 
     * @return
     */
    public Double getTotalHeatingLoad() {
	return Math.round(totalHeatingLoad * 100.0) / 100.;
    }

    /*
     * All getter methods
     */
    public Double getTotalFloorArea() {
	return totalFloorArea;
    }

    public Double getConditionedFloorArea() {
	return conditionedFloorArea;
    }

    public Integer getNumberOfFloor() {
	return floorSet.size();
    }

    public Double getHeatingSetPointNotMet() {
	return heatingSetPointNotMet;
    }

    public Double getCoolingSetPointNotMet() {
	return coolingSetPointNotMet;
    }

    private EplusObject getDesignOutdoorAir(String zoneName) {
	String oaObject = baselineModel.getValue("Sizing:Zone", zoneName,
		"Design Specification Outdoor Air Object Name");
	if (oaObject == null) {
	    String zoneListName = baselineModel.getZoneListName(zoneName);
	    oaObject = baselineModel.getValue("Sizing:Zone", zoneListName,
		    "Design Specification Outdoor Air Object Name");
	}
	OutdoorDesignSpecification oa = new OutdoorDesignSpecification(zoneName);

	setUpOAObject(oa, oaObject);
	return oa;
    }

    private void setUpOAObject(OutdoorDesignSpecification oa, String oaObject) {
	// this means we found the outdoor air object for this zone
	// for asset score tool, this is far than enough
	String method = baselineModel.getValue(
		"DesignSpecification:OutdoorAir", oaObject,
		"Outdoor Air Method");
	if (method != null) {
	    oa.changeOutdoorAirMethod(method);
	}
	String oaPerson = baselineModel.getValue(
		"DesignSpecification:OutdoorAir", oaObject,
		"Outdoor Air Flow per Person");
	if (oaPerson != null) {
	    oa.changeAirFlowperPerson(oaPerson);
	}
	String oaFloorArea = baselineModel.getValue(
		"DesignSpecification:OutdoorAir", oaObject,
		"Outdoor Air Flow per Zone Floor Area");
	if (oaFloorArea != null) {
	    oa.changeAirFlowperFloorArea(oaFloorArea);
	}
	String oaZone = baselineModel.getValue(
		"DesignSpecification:OutdoorAir", oaObject,
		"Outdoor Air Flow per Zone");
	if (oaZone != null) {
	    oa.changeAirFlowperZone(oaZone);
	}
	String oaACH = baselineModel.getValue("DesignSpecification:OutdoorAir",
		oaObject, "Outdoor Air Flow Air Changes per Hours");
	if (oaACH != null) {
	    oa.changeAirFlowperACH(oaACH);
	}
	String oaSchedule = baselineModel.getValue(
		"DesignSpecification:OutdoorAir", oaObject,
		"Outdoor Air Flow Rate Fraction Schedule Name");
	if (oaSchedule != null) {
	    oa.changeAirFlowSchedule(oaSchedule);
	}
    }
}
