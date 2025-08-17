package com.royalcrown.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "flats")
public class Flat {
    @Id
    @NotBlank
    @Column(nullable = false, unique = true)
    private String flatNo;

    @Column(nullable = false)
    private boolean isPresident;

    public Flat() {}

    public Flat(String flatNo, boolean isPresident) {
        this.flatNo = flatNo;
        this.isPresident = isPresident;
    }

    // Getters and setters
    public String getFlatNo() { return flatNo; }
    public void setFlatNo(String flatNo) { this.flatNo = flatNo; }

    public boolean isPresident() { return isPresident; }
    public void setPresident(boolean president) { isPresident = president; }
}
