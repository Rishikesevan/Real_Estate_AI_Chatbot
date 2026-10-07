package com.project.RealEstate.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class UserSubscription
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String plan; // ONE_WEEK / ONE_MONTH

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private String razorpayPaymentId;

    private boolean active;

    @Column(nullable = false, unique = true)
    private String razorpayOrderId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
