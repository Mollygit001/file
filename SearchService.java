package com.mockcompany.webapp.services;

import com.mockcompany.webapp.model.ProductItem;
import com.mockcompany.webapp.api.SearchReportResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class SearchService {

    private final EntityManager entityManager;

    @Autowired
    public SearchService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // Logic to search products based on various conditions
    public List<ProductItem> searchProducts(String searchTerm) {
        return entityManager.createQuery("SELECT item FROM ProductItem item WHERE item.name LIKE :searchTerm OR item.description LIKE :searchTerm", ProductItem.class)
                .setParameter("searchTerm", "%" + searchTerm + "%")
                .getResultList();
    }

    // Logic for generating the product report
    public SearchReportResponse generateReport() {
        Map<String, Integer> hits = new HashMap<>();
        SearchReportResponse response = new SearchReportResponse();
        response.setSearchTermHits(hits);

        int count = this.entityManager.createQuery("SELECT item FROM ProductItem item").getResultList().size();
        List<Number> matchingIds = new ArrayList<>();
        
        // Collect matching products for "cool" term
        matchingIds.addAll(
                this.entityManager.createQuery("SELECT item.id FROM ProductItem item WHERE item.name LIKE '%cool%'").getResultList()
        );
        matchingIds.addAll(
                this.entityManager.createQuery("SELECT item.id FROM ProductItem item WHERE item.description LIKE '%cool%'").getResultList()
        );

        // Deduplicate matching IDs
        List<Number> counted = new ArrayList<>();
        for (Number id : matchingIds) {
            if (!counted.contains(id)) {
                counted.add(id);
            }
        }
        response.getSearchTermHits().put("Cool", counted.size());
        response.setProductCount(count);

        // Additional search term hits for "Kids", "Perfect", "Amazing"
        int kidCount = 0;
        int perfectCount = 0;
        Pattern kidPattern = Pattern.compile("(.*)[kK][iI][dD][sS](.*)");
        
        for (ProductItem item : entityManager.createQuery("SELECT item FROM ProductItem item").getResultList()) {
            if (kidPattern.matcher(item.getName()).matches() || kidPattern.matcher(item.getDescription()).matches()) {
                kidCount++;
            }
            if (item.getName().toLowerCase().contains("perfect") || item.getDescription().toLowerCase().contains("perfect")) {
                perfectCount++;
            }
        }

        response.getSearchTermHits().put("Kids", kidCount);
        response.getSearchTermHits().put("Perfect", perfectCount);
        response.getSearchTermHits().put("Amazing", entityManager.createQuery("SELECT item FROM ProductItem item WHERE lower(concat(item.name, ' - ', item.description)) LIKE '%amazing%'").getResultList().size());

        return response;
    }
}
