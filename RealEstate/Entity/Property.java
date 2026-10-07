package com.project.RealEstate.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String location;

    private Double price;

    private int bhk;

    private String propertyType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;

    @Column(length = 2000)
    private String description;

    @ManyToOne
    private Owner owner;

    @ManyToOne(fetch = FetchType.EAGER)
    private Agent agent;  // Assigned agent

    private String status; // ACTIVE, SOLD, REMOVED

    private String buyRent;

    private String sellOrRent;

    private double areasqft;
}
