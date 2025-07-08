package com.toiukha.spot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "spotimg")
public class SpotImgVO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IMGID")
    private Integer imgId;

    @Column(name = "SPOTID")
    private Integer spotId;

    @Column(name = "IMGPATH")
    private String imgPath;

    @Column(name = "IMGDESC")
    private String imgDesc;

    @Column(name = "IMGTIME")
    private LocalDateTime imgTime;

    // Getter/Setter
    public Integer getImgId() { return imgId; }
    public void setImgId(Integer imgId) { this.imgId = imgId; }

    public Integer getSpotId() { return spotId; }
    public void setSpotId(Integer spotId) { this.spotId = spotId; }

    public String getImgPath() { return imgPath; }
    public void setImgPath(String imgPath) { this.imgPath = imgPath; }

    public String getImgDesc() { return imgDesc; }
    public void setImgDesc(String imgDesc) { this.imgDesc = imgDesc; }

    public LocalDateTime getImgTime() { return imgTime; }
    public void setImgTime(LocalDateTime imgTime) { this.imgTime = imgTime; }
} 