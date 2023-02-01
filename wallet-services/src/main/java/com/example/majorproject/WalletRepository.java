package com.example.majorproject;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet,Integer> {

//    @Modifying
//    @Query(value="UPDATE Wallet w set w.amount = w.amount + :amount where w.userName = :userName",nativeQuery = true)
//    int updateWallet(String userName,int amount);

    Wallet findByUserName(String userName);
}
