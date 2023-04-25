package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class CreditCardController {

    // TODO: wire in CreditCard repository here (~1 line)
    private final CreditCardRepository creditCardRepository;
    private final UserRepository userRepository;

    public CreditCardController(CreditCardRepository creditCardRepository, UserRepository userRepository) {
        this.creditCardRepository = creditCardRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        // TODO: Create a credit card entity, and then associate that credit card with user with given userId
        //       Return 200 OK with the credit card id if the user exists and credit card is successfully associated with the user
        //       Return other appropriate response code for other exception cases
        //       Do not worry about validating the card number, assume card number could be any arbitrary format and length
        if(userRepository.existsById(payload.getUserId())) {
            //if user exists, add the credit card to user and link the credit card to the user through UserID
            CreditCard creditCard = new CreditCard();
            creditCard.setIssuanceBank(payload.getCardIssuanceBank());
            creditCard.setNumber(payload.getCardNumber());
            creditCard.setUserID(payload.getUserId());
            CreditCard savedCreditCard = creditCardRepository.save(creditCard);
            User user = userRepository.findById(payload.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
            user.getCreditCards().add(savedCreditCard);
            userRepository.save(user);
            return ResponseEntity.ok(savedCreditCard.getId());
        }
        else{
            return ResponseEntity.badRequest().build();
        }
        
    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        // TODO: return a list of all credit card associated with the given userId, using CreditCardView class
        //       if the user has no credit card, return empty list, never return null
        List<CreditCard> creditCards = creditCardRepository.findByUserID(userId);
        List<CreditCardView> creditCardViews = creditCards.stream().map(card -> CreditCardView.builder().issuanceBank(card.getIssuanceBank()).number(card.getNumber()).build()).collect(Collectors.toList());
        return ResponseEntity.ok(creditCardViews);
    }

    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        // TODO: Given a credit card number, efficiently find whether there is a user associated with the credit card
        //       If so, return the user id in a 200 OK response. If no such user exists, return 400 Bad Request
        CreditCard creditCard = creditCardRepository.findByNumber(creditCardNumber);
        if (creditCard == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(creditCard.getUserID());
    }

    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<Integer> updateBalanceHistoryForCreditCard(@RequestBody UpdateBalancePayload[] payload) {
        //TODO: Given a list of transactions, update credit cards' balance history.
        //      For example: if today is 4/12, a credit card's balanceHistory is [{date: 4/12, balance: 110}, {date: 4/10, balance: 100}],
        //      Given a transaction of {date: 4/10, amount: 10}, the new balanceHistory is
        //      [{date: 4/12, balance: 120}, {date: 4/11, balance: 110}, {date: 4/10, balance: 110}]
        //      Return 200 OK if update is done and successful, 400 Bad Request if the given card number
        //        is not associated with a card.
        
        for (UpdateBalancePayload transaction : payload) {
            CreditCard creditCard = creditCardRepository.findByNumber(transaction.getCreditCardNumber());
            if (creditCard == null) {
                return ResponseEntity.badRequest().build();
            }
            creditCard.updateBalance(transaction.getTransactionTime(), transaction.getTransactionAmount());
            creditCardRepository.save(creditCard);
        }
        return ResponseEntity.ok().build();
    }
    
}
