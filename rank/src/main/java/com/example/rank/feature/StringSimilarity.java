package com.example.rank.feature;

import com.alibaba.fastjson.JSONObject;
import com.github.vickumar1981.stringdistance.util.StringDistance;
//import com.oracle.tools.packager.mac.MacAppBundler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
/**
 * description: StringSimilarity
 * date: 2021/3/15 下午8:16
 * author: gallup
 * version: 1.0
 */
public class StringSimilarity {

    @Value("")
    String charEmbedPath;
    public List<Double> getSimilarity(String query, String item, List<String> features) {
        List<Double> results = new ArrayList<>();
        if(StringUtils.isBlank(query) || StringUtils.isBlank(item)){
            for(int i =0;i<features.size();i++){
                results.add(0.0);
            }
        }else{
            for (String feature : features) {
                switch (feature) {
                    case "cosine":
                        Double cosSimilarity = StringDistance.cosine(query, item);
                        results.add(cosSimilarity);
                        break;
                    case "damerau":
                        Double damerau = StringDistance.damerau(query, item);
                        results.add(damerau);
                        break;
                    case "diceCoefficient":
                        Double diceCoefficient = StringDistance.diceCoefficient(query, item);
                        results.add(diceCoefficient);
                        break;
                    case "hamming":
                        Double hamming = StringDistance.hamming(query, item);
                        results.add(hamming);
                        break;
                    case "jaccard":
                        Double jaccard = StringDistance.jaccard(query, item);
                        results.add(jaccard);
                        break;
                    case "jaro_similarity":
                        Double jaro = StringDistance.jaro(query, item);
                        results.add(jaro);
                        break;
                    case "jaroWinkler":
                        Double jaroWinkler = StringDistance.jaroWinkler(query, item);
                        results.add(jaroWinkler);
                        break;
                    case "levenshtein":
                        Double levenshtein = StringDistance.levenshtein(query, item);
                        results.add(levenshtein);
                        break;
                    case "needlemanWunsch":
                        Double needlemanWunsch = StringDistance.needlemanWunsch(query, item);
                        results.add(needlemanWunsch);
                        break;
                    case "nGram":
                        Double ngramSimilarity = StringDistance.nGram(query, item);
                        results.add(ngramSimilarity);
                        break;
                    case "bigram":
                        Double bigramSimilarity = StringDistance.nGram(query, item, 2);
                        results.add(bigramSimilarity);
                        break;
                    case "overlap":
                        Double overlap = StringDistance.overlap(query, item);
                        results.add(overlap);
                        break;
                    case "overlapBiGram":
                        Double overlapBiGram = StringDistance.overlap(query, item, 2);
                        results.add(overlapBiGram);
                        break;
                    case "smithWaterman":
                        Double smithWaterman = StringDistance.smithWaterman(query, item);
                        results.add(smithWaterman);
                        break;
                    case "smithWatermanGotoh":
                        Double smithWatermanGotoh = StringDistance.smithWatermanGotoh(query, item);
                        results.add(smithWatermanGotoh);
                        break;
                    case "tversky":
                        Double tversky = StringDistance.tversky(query, item, 0.5);
                        results.add(tversky);
                        break;
                    default:
                        break;

                }
            }
        }


        return results;
    }

    public List<Integer> getDistance(String query, String item, List<String> features) {
        // Distances between two strings
        List<Integer> results = new ArrayList<>();
        if(StringUtils.isBlank(query) || StringUtils.isBlank(item)){
            for(int i =0;i<features.size();i++){
                results.add(0);
            }

        }else{
            for (String feature : features) {
                switch (feature) {
                    case "damerauDist":
                        Integer damerauDist = StringDistance.damerauDist(query, item);
                        results.add(damerauDist);
                        break;
                    case "hamming_distance":
                        Integer hammingDist = StringDistance.hammingDist(query, item);
                        results.add(hammingDist);
                        break;
                    case "levenshtein_distance":
                        Integer levenshteinDist = StringDistance.levenshteinDist(query, item);
                        results.add(levenshteinDist);
                        break;
                    case "lcs":
                        Integer longestCommonSeq = StringDistance.longestCommonSeq(query, item);
                        results.add(longestCommonSeq);
                        break;
                    case "ngramDist":
                        Integer ngramDist = StringDistance.nGramDist(query, item);
                        results.add(ngramDist);
                        break;
                    case "bigramDist":
                        Integer bigramDist = StringDistance.nGramDist(query, item, 2);
                        results.add(bigramDist);
                        break;
                    default:
                        break;
                }

            }
        }

        return results;
    }

    private int longestCommonSubsequence(String text1, String text2) {
        int len1=text1.length();
        int len2=text2.length();
        int [][]dp=new int[len1+1][len2+1];
        for (int i = 1; i <=len1; i++) {
            for (int j = 1; j <=len2; j++) {
                if (text1.charAt(i-1)==text2.charAt(j-1)){
                    dp[i][j]=dp[i-1][j-1]+1;
                }else {
                    dp[i][j]= Math.max(dp[i-1][j],dp[i][j-1]);
                }
            }
        }
        return dp[len1][len2];
    }

    public Map parseJson(String filepath) throws IOException {
        BufferedReader reader = null;
        String laststr = "";
        FileInputStream fileInputStream = new FileInputStream(filepath);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "GB2312");
        reader = new BufferedReader(inputStreamReader);
        String tempString = null;
        while ((tempString = reader.readLine()) != null) {
            laststr += tempString;
        }
        reader.close();
        Map maps =  JSONObject.parseObject(laststr);
        return maps;
    }




    public static void main(String[] args) throws InterruptedException, ExecutionException {
        StringSimilarity ss = new StringSimilarity();

        try{
            Map map = ss.parseJson("/Users/gallup/study/SearchPlatform/src/main/resources/char.json");
            System.out.println("error");
        }catch (IOException e){
            System.out.println("error");
        }

        List<String> featureSimi = new ArrayList<>();
        List<String> featureDistance = new ArrayList<>();
//        featureDistance.add("lcs");
//        featureDistance.add("damerauDist");
//        featureDistance.add("levenshtein_distance");
        featureDistance.add("hamming_distance");
//        featureSimi.add("cosine");
//        featureSimi.add("hamming");
//        featureSimi.add("jaro_similarity");
//        featureSimi.add("jaroWinkler");
//        featureSimi.add("damerau");
        List<Double> result = ss.getSimilarity("jellyfish", "smellyfish", featureSimi);
        List<Integer> res = ss.getDistance("li", "lee", featureDistance);
        List<Integer> res_3 = ss.getDistance("jellyfish", "smellyfish", featureDistance);
        List<Integer> res_4 = ss.getDistance("luisa", "bruna", featureDistance);
        List<Double> result_1 = ss.getSimilarity("luisa", "bruna", featureSimi);
        List<Integer> res_1 = ss.getDistance("martha", "", featureDistance);
        List<Integer> res_2 = ss.getDistance("避孕药", "避孕套", featureDistance);
        List<Integer> res_5 = ss.getDistance("高血压", "沙龙贪利", featureDistance);
        long start,end;
        start = System.currentTimeMillis();

        List<String> querys = new ArrayList<>();
        List<String> titiles = new ArrayList<>();
        for(int i =0;i<300;i++) {
            querys.add("六味地黄丸360粒");
            titiles.add("张仲景六味地黄丸360粒");
        }

        System.out.println("开始时间：" + start);
//        for(int i = 0;i <querys.size();i++){
////            List<Double> result = ss.getSimilarity(querys.get(i), titiles.get(i), featureSimi);
//            List<Integer> res = ss.getDistance(querys.get(i), titiles.get(i), featureDistance);
////            System.out.println(result);
//            System.out.println(res);
//        }


        end = System.currentTimeMillis();
        System.out.println("结束时间：" + end);
        System.out.println("运行时间：" + (end - start) + "(ms)");



    }
}
