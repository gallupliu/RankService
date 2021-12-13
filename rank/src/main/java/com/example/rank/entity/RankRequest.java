package com.example.rank.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Data
@Setter
@Getter
public class RankRequest implements Serializable {
    private static final long serialVersionUID = 1821675745287098594L;
    private String code;
    private String userId;
    private String query;
    private List<String> idList;
}
