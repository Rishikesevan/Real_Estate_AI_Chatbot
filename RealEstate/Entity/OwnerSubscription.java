package com.project.RealEstate.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
@Getter
@Setter
@Entity
@Table(name = "owner_subscription")
public class OwnerSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate subscriptionStart;
    private LocalDate subscriptionEnd;

    private int totalPostGiven;
    private int remainingPost;

    private String planName;

    private String status;

    private String paymentReferenceId;

    @ManyToOne
    private Owner owner;
}
