package com.project.RealEstate.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.project.RealEstate.Entity.Property;
import com.project.RealEstate.Repository.PropertyRepository;

@Service
public class PropertyService {
    @Autowired
    private PropertyRepository propertyRepository;

    public List<Property> viewAllProperties() {
        List<Property> properties = propertyRepository.findAll();
        return properties;
    }

    public List<Property> viewPropertiesBySearch(String location, String propertyType, String bhk) {
        List<Property> properties = propertyRepository.findAll();

        Integer bhkValue = null;

        if (bhk != null && !bhk.trim().isEmpty()) {
            bhkValue = Integer.parseInt(bhk);
        }

        Integer finalBhkValue = bhkValue;
        return properties.stream()
                .filter(p -> location == null || location.trim().isEmpty()
                        || p.getLocation().toLowerCase().contains(location.toLowerCase()))

                .filter(p -> propertyType == null || propertyType.trim().isEmpty()
                        || p.getPropertyType().equalsIgnoreCase(propertyType))

                .filter(p -> finalBhkValue == null || p.getBhk() == finalBhkValue)

                .collect(Collectors.toList());
    }

    public List<Property> viewAllProperty() {
        List<Property> properties = propertyRepository.findAll();
        return properties;
    }

    public Property getPropertyById(Long id) {
        return propertyRepository.findById(id).orElseThrow(() -> new RuntimeException("Property not found"));
    }

    public Property viewPropertiesDetails(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        if (property.getAgent() == null) {
            property.setAgent(null);
            return property;
        } else {
            property.setAgent(property.getAgent());
            return property;
        }
    }

    public List<Property> getHomeProperties() {
        List<Property> result = new ArrayList<>();

        List<Property> apartments = propertyRepository.findTop8ByPropertyTypeOrderByIdDesc("Apartment");

        List<Property> villas = propertyRepository.findTop8ByPropertyTypeOrderByIdDesc("Villa");

        result.addAll(apartments);
        result.addAll(villas);

        return result;
    }

    public List<Property> findByPropertiesOfAgent(Long agentId) {
        return propertyRepository.findByAgentId(agentId);
    }

    public List<Property> getLimitedPropertiesByAgent(Long agentId, int limit) {
        return propertyRepository.findTopByAgentId(agentId, PageRequest.of(0, limit));
    }

    public void savePropertyDetails(Property property) {
        propertyRepository.save(property);
    }

    public List<Property> searchByIntent(String location, Double budget, String propertyType, Integer bhk,
            String purpose) {
        return propertyRepository.searchProperties(location, budget, propertyType, bhk, purpose);
    }
}
