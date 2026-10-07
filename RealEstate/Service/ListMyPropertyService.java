package com.project.RealEstate.Service;

import com.project.RealEstate.Entity.Property;
import com.project.RealEstate.Entity.Transaction;
import com.project.RealEstate.Repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListMyPropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private TransactionService transactionService;



    public List<Property> myProperty(Long ownerId){
        List<Property> properties = propertyRepository.findAll();

        List<Property> propertyList = new ArrayList<>();

        for(Property property : properties){
            if(property.getOwner().getId()!=null && property.getOwner().getId().equals(ownerId)){
                propertyList.add(property);
            }
        }
        return propertyList;
    }

    public Property view(Long PropertyId){
        Property property = propertyRepository.findById(PropertyId).orElse(null);
        return property;
    }

    public Property update(Property property, Double number, LocalDate data)  {
            Property property1 = propertyRepository.findById(property.getId()).orElse(null);

            property1.setTitle(property.getTitle());
            property1.setPrice(property.getPrice());
            property1.setStatus(property.getStatus());
            property1.setBhk(property.getBhk());
            property1.setAreasqft(property.getAreasqft());
            property1.setPropertyType(property.getPropertyType());
            property1.setLocation(property.getLocation());
            property1.setStatus(property.getStatus());
            property1.setAgent(property.getAgent());

            if(property1.getStatus().toLowerCase().equals("sold")){
                Transaction transaction = new Transaction();

                transaction.setProperty(property1);
                transaction.setSoldDate(data);
                transaction.setSoldPrice(number);
                transactionService.savetransaction(transaction);
            }

            return propertyRepository.save(property1);
    }

    public void deleteById(Long id){
        Property property = propertyRepository.findById(id).orElse(null);
        propertyRepository.delete(property);

    }

    public Property viewSoldProperty(Long Pid){
           Property property = propertyRepository.findById(Pid).orElse(null);
           return property;

    }
}
