package com.shepherdmoney.interviewproject.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.bytebuddy.dynamic.DynamicType.Builder.FieldDefinition.Optional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jakarta.persistence.CascadeType;
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
    
    @OneToMany(mappedBy = "creditCard", cascade = CascadeType.ALL)
    @OrderBy("date DESC")
    private List<BalanceHistory> balanceHistory = new ArrayList<>();

    public void updateBalance(Instant transactionTime, double transactionAmount) {
        java.util.Optional<BalanceHistory> balanceHistory = this.balanceHistory.stream().filter(bh -> bh.getDate().equals(transactionTime)).findFirst();
        Double newBalance;
        if (balanceHistory.isPresent()) {
            BalanceHistory currentBalanceHistory = balanceHistory.get();
            newBalance = currentBalanceHistory.getBalance() + transactionAmount;
            currentBalanceHistory.setBalance(newBalance);
        }
        else {
            newBalance = transactionAmount;
            BalanceHistory newBalanceHistory = new BalanceHistory();
            newBalanceHistory.setDate(transactionTime);
            newBalanceHistory.setBalance(transactionAmount);
            this.balanceHistory.add(newBalanceHistory);
         }
        Instant today = this.balanceHistory.get(0).getDate();
        LocalDate startLocalDate = transactionTime.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = today.atZone(ZoneId.systemDefault()).toLocalDate();
        for (LocalDate date = startLocalDate; !date.isAfter(endLocalDate); date = date.plusDays(1)) {
            LocalDateTime localDateTime = date.atStartOfDay();
            ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
            Instant curr_date = zonedDateTime.toInstant();
            java.util.Optional<BalanceHistory> curr_bh = this.balanceHistory.stream().filter(bh -> bh.getDate().equals(curr_date)).findFirst();
            if (curr_bh.isPresent()){
                BalanceHistory curr_BH = curr_bh.get();
                curr_BH.setBalance(curr_BH.getBalance() + transactionAmount);
            }
            else{
                BalanceHistory newBalanceHistory = new BalanceHistory();
                newBalanceHistory.setDate(curr_date);
                newBalanceHistory.setBalance(newBalance);
                this.balanceHistory.add(newBalanceHistory);
            }
        }
        Collections.sort(this.balanceHistory, Comparator.comparing(BalanceHistory::getDate).reversed());

         
        
    }
}
