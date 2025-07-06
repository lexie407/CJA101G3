package com.toiukha.groupactivity.model;

import java.util.List;

public class ItinerarySimpleDTO {
    private Integer itnId;
    private String itnName;
    private String itnDesc;
    private Integer isPublic;
    private List<SpotSimpleDTO> itnSpots;

    public Integer getItnId() { return itnId; }
    public void setItnId(Integer itnId) { this.itnId = itnId; }

    public String getItnName() { return itnName; }
    public void setItnName(String itnName) { this.itnName = itnName; }

    public String getItnDesc() { return itnDesc; }
    public void setItnDesc(String itnDesc) { this.itnDesc = itnDesc; }

    public Integer getIsPublic() { return isPublic; }
    public void setIsPublic(Integer isPublic) { this.isPublic = isPublic; }

    public List<SpotSimpleDTO> getItnSpots() { return itnSpots; }
    public void setItnSpots(List<SpotSimpleDTO> itnSpots) { this.itnSpots = itnSpots; }
} 