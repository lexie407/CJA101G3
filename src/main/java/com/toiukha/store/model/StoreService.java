package com.toiukha.store.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toiukha.email.EmailService;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepo;
    
    @Autowired
	private EmailService emailService;

    // 檢查店家帳號是否已存在
    public boolean existsByStoreAcc(String storeAcc) {
        return storeRepo.existsByStoreAcc(storeAcc);
    }

    // 檢查店家統編是否已存在
    public boolean existsByStoreGui(String storeGui) {
        return storeRepo.existsByStoreGui(storeGui);
    }

    // 檢查店家信箱是否已存在
    public boolean existsByStoreEmail(String storeEmail) {
        return storeRepo.existsByStoreEmail(storeEmail);
    }

    // 根據帳號查店家資料（登入用）
    public Optional<StoreVO> findByStoreAcc(String storeAcc) {
        return storeRepo.findByStoreAcc(storeAcc);
    }

    // 根據信箱查店家資料（忘記密碼、驗證用）
    public Optional<StoreVO> findByStoreEmail(String storeEmail) {
        return storeRepo.findByStoreEmail(storeEmail);
    }

    // 根據主鍵查一筆店家資料
    public StoreVO getById(Integer storeId) {
        return storeRepo.findById(storeId)
                        .orElse(null);
    }
    
     // 商家註冊：設定預設值、存入資料庫
    @Transactional
    public StoreVO registerStore(StoreVO storeVO) {
       

        // 1. 設定預設值（防止前端或惡意請求竄改）
        storeVO.setStoreStatus((byte) 0);  // STORESTATUS NOT NULL 預設為 0
        storeVO.setStoreRegDate(new Timestamp(System.currentTimeMillis()));  // STOREREGDATE NOT NULL


        // 2. 儲存資料
        StoreVO saved = storeRepo.save(storeVO);

      

        return saved;
    }
    
//  處理忘記密碼流程：找商家、寄出重設連結（不告知是否存在）
    public void processForgotPassword(String email) {
        Optional<StoreVO> opt = storeRepo.findByStoreEmail(email);
        if (opt.isPresent()) {
            StoreVO store = opt.get();
            new Thread(() -> {
                emailService.sendStoreResetPasswordEmail(
                    store.getStoreEmail(),
                    store.getStoreId()
                );
            }).start();
        }
    }
    
    
     // 更新並儲存商家資料（含重設密碼時使用）
    @Transactional
    public StoreVO save(StoreVO storeVO) {
        return storeRepo.save(storeVO);
    }
    
    
 // 取得所有商家（用於搜尋頁面下拉式選單）
    public List<StoreVO> findAllStores() {
        return storeRepo.findAll();
    }
    
    // 取得所有待審核商家（storeStatus = 0）
    public List<StoreVO> findPendingStores() {
        return storeRepo.findByStoreStatus((byte)0);
    }
    
    // 更新商家狀態（通過/駁回）並設定更新時間 
    @Transactional
    public void updateStatus(Integer storeId, byte newStatus) {
        StoreVO store = storeRepo.findById(storeId)
            .orElseThrow(() -> new RuntimeException("找不到商家 id=" + storeId));
        store.setStoreStatus(newStatus);
        store.setStoreUpdatedAt(new Timestamp(System.currentTimeMillis()));
        storeRepo.save(store);
    }
    

}