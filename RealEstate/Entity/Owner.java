package com.project.RealEstate.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "owners")
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String name;

    @Column(unique = true)
    @NotNull(message = "Email is required")
    @Email(message = "Enter valid email")
    private String email;

    @NotNull(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must be strong.\nMust contain at least one lowercase letter,\none uppercase letter,\none digit,\none special character,\nand be at least 8 characters long.")
    private String password;

    @NotNull(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number is invalid")
    @Size(min = 10, max = 10, message = "Phone number must be 10 digits long")
    private String phone;

    @NotNull(message = "Address is required")
    @Size(min = 3, max = 255, message = "Address must be between 3 and 255 characters")
    @Pattern(regexp = "^[a-zA-Z]*$", message = "Address can only contain letters and spaces")
    @Column(nullable = false, length = 255)
    private String address;

    @Column(unique = true)
    private String token;

    private boolean enabled = false;

    private LocalDateTime tokenExpiryTime;

}
