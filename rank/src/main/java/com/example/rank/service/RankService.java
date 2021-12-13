package com.example.rank.service;
import com.example.rank.entity.ItemSortModel;
import com.example.rank.entity.RankRequest;

import java.util.List;

public interface RankService {
    public List<ItemSortModel> getResList(RankRequest request);
}
