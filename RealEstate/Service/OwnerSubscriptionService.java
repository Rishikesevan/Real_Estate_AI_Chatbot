package com.project.RealEstate.Service;

import com.project.RealEstate.Entity.Owner;
import com.project.RealEstate.Entity.OwnerSubscription;
import com.project.RealEstate.Repository.OwnerSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class OwnerSubscriptionService {
    @Autowired
    private OwnerSubscriptionRepository ownerSubscriptionRepository;

    @Autowired
    private OwnerService ownerService;

    public String toCheckSubscription(Long ownerId) {
        List<OwnerSubscription> subscriptions = ownerSubscriptionRepository.findAll();
        LocalDate today = LocalDate.now();
        for (OwnerSubscription subscription : subscriptions) {
            if (subscription.getOwner().getId().equals(ownerId)) {
                if (subscription.getStatus().equals("ACTIVE") && subscription.getSubscriptionEnd().isAfter(today)
                        || subscription.getSubscriptionEnd().isEqual(today)) {
                    return "ACTIVE";
                }
            }
        }
        return "FAILED";
    }

    public OwnerSubscription saveSubscription(String subId, Long ownerId, Integer amount) {
        OwnerSubscription subscriptions = new OwnerSubscription();
        Owner owner = ownerService.ownerId(ownerId);
        subscriptions.setOwner(owner);
        subscriptions.setStatus("ACTIVE");
        subscriptions.setPaymentReferenceId(subId);
        if (amount == 499) {
            LocalDate localDate = LocalDate.now();
            subscriptions.setSubscriptionStart(localDate);
            LocalDate localDate1 = LocalDate.now().plusMonths(1);
            subscriptions.setSubscriptionEnd(localDate1);
            subscriptions.setPlanName("BASIC");
            subscriptions.setTotalPostGiven(5);
            subscriptions.setRemainingPost(5);
        } else if (amount == 999) {
            LocalDate localDate = LocalDate.now();
            subscriptions.setSubscriptionStart(localDate);
            LocalDate localDate1 = LocalDate.now().plusMonths(3);
            subscriptions.setSubscriptionEnd(localDate1);
            subscriptions.setPlanName("SILVER");
            subscriptions.setTotalPostGiven(15);
            subscriptions.setRemainingPost(15);
        } else if (amount == 1999) {
            LocalDate localDate = LocalDate.now();
            subscriptions.setSubscriptionStart(localDate);
            LocalDate localDate1 = LocalDate.now().plusMonths(3);
            subscriptions.setSubscriptionEnd(localDate1);
            subscriptions.setPlanName("PREMIUM");
            subscriptions.setTotalPostGiven(50);
            subscriptions.setRemainingPost(50);
        }
        return ownerSubscriptionRepository.save(subscriptions);
    }

    public void editSubscription(Long ownerId) {
        List<OwnerSubscription> subscriptions = ownerSubscriptionRepository.findAll();

        for (OwnerSubscription subscription : subscriptions) {
            if (subscription.getOwner().getId().equals(ownerId) && subscription.getStatus().equals("ACTIVE")) {
                int rem = subscription.getRemainingPost() - 1;
                subscription.setRemainingPost(rem);

                if (rem == 0) {
                    subscription.setStatus("EXPIRED");
                }
                ownerSubscriptionRepository.save(subscription);
            }
        }
    }

    public void editPlain(Long ownerId) {
        List<OwnerSubscription> subscriptions = ownerSubscriptionRepository.findAll();
        LocalDate date = LocalDate.now();
        for (OwnerSubscription subscription : subscriptions) {
            if (subscription.getOwner().getId().equals(ownerId)) {
                if (subscription.getSubscriptionEnd().isBefore(date)) {
                    subscription.setStatus("EXPIRED");
                    ownerSubscriptionRepository.save(subscription);
                }
            }
        }
    }
}
