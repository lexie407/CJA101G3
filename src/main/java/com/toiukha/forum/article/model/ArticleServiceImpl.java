package com.toiukha.forum.article.model;

import com.toiukha.forum.article.dto.ArticleDTO;
import com.toiukha.forum.article.dto.ArticleSearchCriteria;
import com.toiukha.forum.article.entity.Article;
import com.toiukha.forum.util.ArticleSpecifications;
import com.toiukha.forum.util.Debug;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.*;

@Service("articleService")
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;

    /**
     * 用於排序的Key，包含排序欄位和方向。
     * 這個類別可以用於在查詢中指定排序條件。
     */
    public record SortKey(ArticleSortField sortField, SortDirection sortDirection){}

    /**
     * 定義文章排序的欄位。
     * 以後會包含文章ID、建立時間、按讚數分類。
     */
    public enum ArticleSortField {
        ARTICLE_ID,
        ARTICLE_CREATINE,
        ARTICLE_LIKES,
        ARTICLE_STATUS,
    }

    // 定義排序方向，升冪或降冪
    public enum SortDirection {
        ASC, DESC
    }

    // 內部類別產生排序邏輯，根據文章按讚數排序
    private class LikesCompASC implements Comparator<ArticleDTO> {
        @Override
        public int compare(ArticleDTO o1, ArticleDTO o2) {
            return o1.getArtLike().compareTo(o2.getArtLike());
        }
    }

    // 用一個HashMap去存每個Key代表的排序邏輯(Comparator)。
    // Comparator是SAM (又稱為Functional Interface(函式介面))
    private final Map<SortKey, Comparator<ArticleDTO>> sortMap = new HashMap<SortKey, Comparator<ArticleDTO>>();
    {
        // 內部類別
        sortMap.put(new SortKey(ArticleSortField.ARTICLE_LIKES, SortDirection.ASC), new LikesCompASC());

        // 用匿名類別 new出Comparator，override compare方法，使他根據getArtId的值排列
        sortMap.put(new SortKey(ArticleSortField.ARTICLE_ID, SortDirection.ASC),
                /* 因為重點是實作的介面，所以我們new一個匿名類別
                 * 告訴java這個匿名類別實作 Comparator<ArticleDTO>介面  */
                new Comparator<ArticleDTO>() {
                    @Override
                    public int compare(ArticleDTO o1, ArticleDTO o2) {
                        return o1.getArtId().compareTo(o2.getArtId());
                    }
                });

        // 也可以用 lambda 表達式 new Comparator<IArticleDTO>出來，再放進sortMap
        Comparator<ArticleDTO> idDescComp = (dto1, dto2) -> dto1.getArtId().compareTo(dto2.getArtId());
        sortMap.put(
                new SortKey(ArticleSortField.ARTICLE_ID, SortDirection.DESC),
                idDescComp.reversed() // 直接使用reversed()方法反轉比較器(Comparator)的順序
        );

        // 使用方法參考(語法糖)建立 Comparator，根據 getArtCreTime() 進行升冪排序，等同於 Lambda 表達式：(dto1, dto2) -> dto1.getArtCreTime().compareTo(dto2.getArtCreTime())
        sortMap.put(new SortKey(ArticleSortField.ARTICLE_CREATINE, SortDirection.ASC), Comparator.comparing(ArticleDTO::getArtCreTime));
        sortMap.put(new SortKey(ArticleSortField.ARTICLE_CREATINE, SortDirection.DESC), Comparator.comparing(ArticleDTO::getArtCreTime).reversed());
    }

    @Autowired
    public ArticleServiceImpl(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    //===================  新增  ===================
    /**
     * 新增文章
     * @param artCat 文章分類
     * @param artSta 文章上下架狀態
     * @param artHol 所屬會員編號
     * @param artTitle 文章標題
     * @param artCon 文章內容
     * @return 新增的文章實體
     */
    //新增文章
    @Override
    public Article add(Byte artCat, Byte artSta, Integer artHol, String artTitle, String artCon) {
        Article artVO = new Article();
        artVO.setArtCat(artCat);
        artVO.setArtSta(artSta);
        artVO.setArtHol(artHol);
        artVO.setArtTitle(artTitle);
        artVO.setArtCon(artCon);
        return articleRepository.save(artVO);
    }

    @Override
    public Article add(Article artVO) {
        return articleRepository.save(artVO);
    }


    //===================  刪除  ===================
    //刪除功能用以資料測試，實務上以更改文章上下架狀態為主
    @Override
    public void delete(Integer id) {
        articleRepository.deleteById(id);
    }


    //===================  更新  ===================

    /* 文章更新_除id外的全欄位
     * 更新一篇文章的所有欄位（包含文章分類、上下架狀態、標題、內文、建立時間、作者、按讚數）。
     * 測試系統，或後台有需要修改文章的完整資料欄位時使用
     */
    @Override
    public Article update(Integer artId, Byte artCat, Byte artSta, Integer artHol, Integer artLike,
                          String artTitle, String artCon, Timestamp artCreTime) {
        Article artVO = new Article();
        artVO.setArtId(artId);
        artVO.setArtCat(artCat);
        artVO.setArtSta(artSta);
        artVO.setArtHol(artHol);
        artVO.setArtLike(artLike);
        artVO.setArtTitle(artTitle);
        artVO.setArtCon(artCon);
        artVO.setArtCreTime(artCreTime);
        return articleRepository.save(artVO);
    }

    /** 文章更新_基本欄位
     * 更新文章的基本欄位（文章分類、上下架狀態、標題、內文）。
     * 讓使用者編輯文章內容，不會影響文章建立時間、作者、按讚數等欄位。
     */
    @Override
    public Article updateBasic(Integer artId, Byte artCat, Byte artSta, String artTitle, String artCon) {
        Article artVO = articleRepository.findById(artId).orElse(null);
        if (artVO == null) {
            artVO = new Article(); // FIXME: 這裡應該拋出一個例外，表示找不到文章
        }
        artVO.setArtId(artId);
        artVO.setArtCat(artCat);
        artVO.setArtSta(artSta);
        artVO.setArtTitle(artTitle);
        artVO.setArtCon(artCon);
        return articleRepository.save(artVO);
    }

    @Override
    public void update(Article artVO) {
        articleRepository.save(artVO);
    }

    //===================  查詢  ===================

    @Override
    public Article getArticleById (Integer id) {
        return articleRepository.findById(id).orElse(null);
    }

    //根據artCat文章類別找資料
    @Override
    public List<Article> getByCategory(Byte artCat) {
        return articleRepository.findByArtCat(artCat);
    }

    //根據artTitle找文章
    @Override
    public List<Article> getByTitle(String artTitle) {
        return articleRepository.findByArtTitleContaining(artTitle);
    }

    //根據文章上下架找文章
    @Override
    public List<Article> getByStatus(Byte artSta) {
        return articleRepository.findByArtSta(artSta);
    }

    //根據會員編號找文章
    public List<Article> getByAuthorId(Integer artHol) {
        return articleRepository.findByArtHol(artHol);
    }

    //進階查詢
    // memo: 這是要在articleRepository繼承JpaSpecificationExecutor 才能使用的進階查詢
    @Override
    public List<Article> searchArticlesByCriteria(ArticleSearchCriteria criteria) {

        return articleRepository.findAll(ArticleSpecifications.withCriteria(criteria));
    }


    @Override
    public List<Article> getAll() {
        return articleRepository.findAll();
    }

    //===================  DTO查詢  ===================

    // 取得所有文章的DTO列表(支援分頁功能)
    @Override
    public Page<ArticleDTO> getAllPagedDTO(int page, int size, String sortBy, SortDirection sortDirection) {
        Sort.Direction direction = Sort.Direction.valueOf(sortDirection.name());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return articleRepository.getAllPagedDTO(pageable);
    }

    // 取得所有文章的DTO列表
    public List<ArticleDTO> getAllDTO(String sortBy, String sortDirection) {
        // 取得所有文章的DTO列表
        List<ArticleDTO> articles = articleRepository.getAllDTO();
        return sortArticles(articles, sortBy, sortDirection);
    }

    public List<ArticleDTO> getAllDTO(){
        return articleRepository.getAllDTO();
    }

    public ArticleDTO getDTOById(Integer id) {
        return articleRepository.getDTOById(id);
    }

    private List<ArticleDTO> sortArticles(List<ArticleDTO> articles, String sortBy, String sortDirection){
        // 這裡根據 sortBy 和 sortDirection 進行排序邏輯的實作

        // 根據哪個欄位排序
        ArticleSortField sortField;
        try {
            sortField = ArticleSortField.valueOf(sortBy);
        } catch (IllegalArgumentException e) {
            Debug.errorLog("排序欄位錯誤" + e.getMessage());
            sortField = ArticleSortField.ARTICLE_ID;
        }
        // 升冪或降冪排序
        SortDirection direction;
        try {
            direction = SortDirection.valueOf(sortDirection.toUpperCase());
        } catch (IllegalArgumentException e) {
            Debug.errorLog("排序方向錯誤" + e.getMessage());
            direction = SortDirection.ASC;
        }



        // 創建一個 SortKey 物件，包含排序欄位和方向
        SortKey sortKey = new SortKey(sortField, direction);
        Debug.log(sortKey.toString());
        // 根據排序的key從 map 中取得對應的比較器(Comparator)
        Comparator<ArticleDTO> comparator = sortMap.get(sortKey);
        if (comparator != null) {
            List<ArticleDTO> sortedList = new ArrayList<ArticleDTO>(articles);
            sortedList.sort(comparator);
            return sortedList;
        }else {
            // 如果沒有對應的Comparator，則使用原本getAllDTO()的結果
            Debug.errorLog("無效的sortKey，使用初始的文章列表");
            return articles;
        }
    }
}