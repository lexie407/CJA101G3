package com.toiukha.itnSpot.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItnSpotService {

    @Autowired
    private ItnSpotRepository itnSpotRepository;

    public List<ItnSpotVO> findAll() {
        return itnSpotRepository.findAll();
    }

    public ItnSpotVO findById(ItnSpotId itnSpotId) {
        Optional<ItnSpotVO> optional = itnSpotRepository.findById(itnSpotId);
        return optional.orElse(null);
    }

    public void updateSpo(ItnSpotVO itnSpotVO, Integer newSpotId) {
        itnSpotRepository.updateSpo(newSpotId, itnSpotVO.getId().getItnId(), itnSpotVO.getId().getSpotId());
    }

    public void deleteById(ItnSpotId itnSpotId) {
        itnSpotRepository.deleteById(itnSpotId);
    }
    
    /**
     * 獲取某個行程的所有景點，並依順序排列
     */
    public List<ItnSpotVO> getSpotsByItineraryId(Integer itnId) {
        return itnSpotRepository.getSpotsByitnId(itnId);
    }
    
    /**
     * 1. 新增 ItnSpot 紀錄
     * 自動判斷當前 itnId 的最大 seq，並將新紀錄的 seq 設為 (最大 seq + 1)
     * @param itnId 行程編號
     * @param spotId 景點編號
     * @return 新增後的 ItnSpot 物件
     * @throws IllegalArgumentException 如果該 itnId 和 spotId 的組合已存在
     */
    @Transactional
    public ItnSpotVO addItnSpot(Integer itnId, Integer spotId) {
    	ItnSpotId pk = new ItnSpotId(itnId, spotId);

        // 檢查該組合是否已存在
        if (itnSpotRepository.existsById(pk)) {
            throw new IllegalArgumentException("行程 " + itnId + " 已包含景點 " + spotId);
        }

        // 查詢當前 itnId 下的最大 seq 值
        Optional<Integer> maxSeqOptional = itnSpotRepository.getMaxSeqByItnId(itnId);
        Integer nextSeq = maxSeqOptional.map(maxSeq -> maxSeq + 1).orElse(1); // 如果沒有紀錄，則從 1 開始

        ItnSpotVO newItnSpot = new ItnSpotVO(pk, nextSeq);
        return itnSpotRepository.save(newItnSpot);
    }
        
        /**
         * 2. 修改 ItnSpot 紀錄的順序 (seq)
         * 此方法會重新編排特定 itnId 下所有景點的順序。
         * @param itnId 要修改的行程編號
         * @param updatedSpotIdsInOrder 包含新的景點順序的列表 (例如: [spotId1, spotId2, spotId3])
         * @return 更新後的 ItnSpot 列表
         * @throws IllegalArgumentException 如果提供的 spotId 列表中有重複的，或有不屬於該行程的 spotId
         */
        @Transactional
        public List<ItnSpotVO> updateItnSpotSequence(Integer itnId, List<Integer> updatedSpotIdsInOrder) {
            if (updatedSpotIdsInOrder == null || updatedSpotIdsInOrder.isEmpty()) {
                throw new IllegalArgumentException("更新順序的景點列表不能為空。");
            }

            // 獲取當前 itnId 下所有已存在的 ItnSpot 紀錄
            List<ItnSpotVO> existingItnSpots = itnSpotRepository.getSpotsByitnId(itnId);

            // 驗證 updatedSpotIdsInOrder 中的 spotId 是否都屬於這個 itnId
            // 以及是否有重複的 spotId
            List<Integer> existingSpotIds = existingItnSpots.stream()
                    .map(itnSpotVO -> itnSpotVO.getId().getSpotId())
                    .collect(Collectors.toList());

            // 檢查是否有重複的 spotId
            long distinctCount = updatedSpotIdsInOrder.stream().distinct().count();
            if (distinctCount != updatedSpotIdsInOrder.size()) {
                throw new IllegalArgumentException("更新順序的景點列表中包含重複的景點 ID。");
            }

            // 檢查更新列表中的 spotId 是否都存在於該行程
            for (Integer spotId : updatedSpotIdsInOrder) {
                if (!existingSpotIds.contains(spotId)) {
                    throw new IllegalArgumentException("景點 ID " + spotId + " 不屬於行程 " + itnId + "，或該景點不存在。");
                }
            }
            
            // 檢查更新列表的數量是否與現有數量匹配（如果順序是針對完整集合的）
            // 否則，如果只是部分更新，則這個檢查可能不適用，需根據需求調整
            if (updatedSpotIdsInOrder.size() != existingItnSpots.size()) {
                // 這表示有新增或刪除景點，這應該由另一個專門的方法處理，
                // 或者這個方法需要更複雜的邏輯來判斷新增/刪除並調整seq
                // 為了簡化，此方法假設是完整替換或重新排序
                throw new IllegalArgumentException("更新景點數量與現有景點數量不符。請使用新增/刪除操作。");
            }

            // 建立一個 Map，用來快速查找現有的 ItnSpot 物件
            // Key: spotId, Value: ItnSpot
            java.util.Map<Integer, ItnSpotVO> spotIdToItnSpotMap = existingItnSpots.stream()
                    .collect(Collectors.toMap(itnSpotVO -> itnSpotVO.getId().getSpotId(), itnSpot -> itnSpot));

            // 遍歷新的順序列表，更新 seq 值
            for (int i = 0; i < updatedSpotIdsInOrder.size(); i++) {
                Integer spotId = updatedSpotIdsInOrder.get(i);
                ItnSpotVO itnSpotToUpdate = spotIdToItnSpotMap.get(spotId);
                if (itnSpotToUpdate != null) {
                    // 如果新的順序與舊的順序不同，才進行更新
                    if (!itnSpotToUpdate.getSeq().equals(i + 1)) {
                        itnSpotToUpdate.setSeq(i + 1); // 順序從 1 開始
                        itnSpotRepository.save(itnSpotToUpdate); // 保存更新
                    }
                }
            }

            // 返回更新後的 ItnSpot 列表 (重新從資料庫查詢確保最新狀態)
            return itnSpotRepository.getSpotsByitnId(itnId);
        }
    
        /**
         * 3. 刪除 ItnSpot 紀錄，並重新調整剩餘景點的 seq
         * @param itnId 行程編號
         * @param spotId 要刪除的景點編號
         * @return boolean 表示是否成功刪除
         */
        @Transactional
        public boolean deleteItnSpot(Integer itnId, Integer spotId) {
        	ItnSpotId pk = new ItnSpotId(itnId, spotId);
            
            if (!itnSpotRepository.existsById(pk)) {
                return false; // 紀錄不存在，無需刪除
            }
            
            itnSpotRepository.deleteById(pk); // 刪除指定的 ItnSpot
            
            //強制刷新
            itnSpotRepository.flush();
            
            // 重新獲取該 itnId 下的所有景點，並重新編排 seq
            List<ItnSpotVO> remainingSpots = itnSpotRepository.getSpotsByitnId(itnId);
            
            System.out.println("剩下景點數量: " + remainingSpots.size());
            
            for (int i = 0; i < remainingSpots.size(); i++) {
                ItnSpotVO itnSpot = remainingSpots.get(i);
                if (!itnSpot.getSeq().equals(i + 1)) { // 如果順序不正確，則更新
                	ItnSpotVO OldItnSpot = itnSpot;
                	itnSpot.setSeq(i + 1);
                    itnSpotRepository.updateSeq(OldItnSpot.getId(), OldItnSpot.getSeq(), itnSpot.getSeq());
                }
            }
            return true;
        }
    }

