package com.shepherdmoney.interviewproject.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String issuanceBank;

    private String number;

    // TODO: Credit card's owner. For detailed hint, please see User class
    private int userID;

    // TODO: Credit card's balance history. It is a requirement that the dates in the balanceHistory 
    //       list must be in chronological order, with the most recent date appearing first in the list. 
    //       Additionally, the first object in the list must have a date value that matches today's date, 
    //       since it represents the current balance of the credit card. For example:
    //       [
    //         {date: '2023-04-13', balance: 1500},
    //         {date: '2023-04-12', balance: 1200},
    //         {date: '2023-04-11', balance: 1000},
    //         {date: '2023-04-10', balance: 800}
    //       ]
    
    @OneToMany()
    @OrderBy("date DESC")
    private List<BalanceHistory> balanceHistory = new ArrayList<>();


    //update balance method for UpdateBalancePayload
    public void updateBalance(Instant transactionTime, double transactionAmount) {
        //check if there's already a balance history with corresponding transactionTime
        java.util.Optional<BalanceHistory> balanceHistory = this.balanceHistory.stream().filter(bh -> bh.getDate().equals(transactionTime)).findFirst();
        Double newBalance;
        //if there is already a balance history update its amount
        if (balanceHistory.isPresent()) {
            BalanceHistory currentBalanceHistory = balanceHistory.get();
            newBalance = currentBalanceHistory.getBalance() + transactionAmount;
            currentBalanceHistory.setBalance(newBalance);
        }
        //if there is not a balance history with given transactinoTime insert a new BalanceHistory
        else {
            newBalance = transactionAmount;
            BalanceHistory newBalanceHistory = new BalanceHistory();
            newBalanceHistory.setDate(transactionTime);
            newBalanceHistory.setBalance(transactionAmount);
            this.balanceHistory.add(newBalanceHistory);
         }
        //loop through each day from given transactionTime to Today and update the BalanceHistory for each day
        Instant today = this.balanceHistory.get(0).getDate();
        LocalDate startLocalDate = transactionTime.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = today.atZone(ZoneId.systemDefault()).toLocalDate();
        for (LocalDate date = startLocalDate; !date.isAfter(endLocalDate); date = date.plusDays(1)) {
            LocalDateTime localDateTime = date.atStartOfDay();
            ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
            Instant curr_date = zonedDateTime.toInstant();
            java.util.Optional<BalanceHistory> curr_bh = this.balanceHistory.stream().filter(bh -> bh.getDate().equals(curr_date)).findFirst();
            //if there is already a BalanceHistory update its amount
            if (curr_bh.isPresent()){
                BalanceHistory curr_BH = curr_bh.get();
                curr_BH.setBalance(curr_BH.getBalance() + transactionAmount);
            }
            //if not insert a new BalanceHistory with the same newBalance as the input transaction
            else{
                BalanceHistory newBalanceHistory = new BalanceHistory();
                newBalanceHistory.setDate(curr_date);
                newBalanceHistory.setBalance(newBalance);
                this.balanceHistory.add(newBalanceHistory);
            }
        }
        //sort BalanceHistory list by date, with Today at the first of the list
        Collections.sort(this.balanceHistory, Comparator.comparing(BalanceHistory::getDate).reversed());

         
        
    }
}
