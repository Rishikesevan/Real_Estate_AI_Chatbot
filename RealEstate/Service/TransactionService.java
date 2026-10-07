package com.project.RealEstate.Service;


import com.project.RealEstate.Repository.TransactionRepository;
import com.project.RealEstate.Entity.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction savetransaction(Transaction transaction){
        return transactionRepository.save(transaction);
    }

    public Transaction viewTransaction(Long id){
        List<Transaction> transactions = transactionRepository.findAll();
        Transaction transaction1 = new Transaction();
       for(Transaction transaction:transactions){
          if( transaction.getProperty().getId().equals(id)){
              transaction1 = transaction;
          }
       }
        return transaction1;
    }

    public List<Transaction> viewAllTransaction(){
        return transactionRepository.findAll();
    }


}
