package com.project.RealEstate.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "new_projects")
public class NewProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectName;
    private String builderName;
    private String location;
    private Double startingPrice;

    private String status; // UPCOMING / NEW_LAUNCH / ONGOING

    private LocalDate handOverDate;

    @ManyToOne
    private Owner owner;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;

}
