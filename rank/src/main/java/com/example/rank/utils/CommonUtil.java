package com.example.rank.utils;

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;

import java.util.List;
import java.util.TreeMap;

public class CommonUtil {

    private static final String OS_NAME_KEY = "os.name";
    private static final String WIN_KEY = "win";
    /**
     * 检查系统是否是win系统
     * @return true win系统
     *         false 非win系统，可默认为linux系统
     */
    public static boolean checkSystemIsWin(){
        return System.getProperty(OS_NAME_KEY).toLowerCase().contains(WIN_KEY);
    }

    /**
     * 字节数组转float
     * @param b
     * @param index
     * @return
     */
    public static float byte2float(byte[] b, int index) {
        int ret = byte2int(b, index);
        return Float.intBitsToFloat(ret);
    }

    /**
     * 字节数组转int整数
     * @param b
     * @param index
     * @return
     */
    public static int byte2int(byte[] b, int index) {
        if (b == null || b.length <= index + 3){
            throw new IllegalStateException("byte2int exception");
        }
        int l;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        return l;
    }

    /**
     * 基于ac自动机进行敏感词检测
     * @param keyArray 词典中模式串
     * @param text 待检测文本
     * @return
     */
    public static List<AhoCorasickDoubleArrayTrie.Hit<String>> querySensitiveWord(String[] keyArray,String text){
        // Collect test data set
        TreeMap<String, String> map = new TreeMap<String, String>();

        for (String key : keyArray)
        {
            map.put(key, key);
        }
        // Build an AhoCorasickDoubleArrayTrie
        AhoCorasickDoubleArrayTrie<String> acdat = new AhoCorasickDoubleArrayTrie<String>();
        acdat.build(map);
        List<AhoCorasickDoubleArrayTrie.Hit<String>> wordList = acdat.parseText(text);

        return wordList;
    }
    public static void main(String[] args){
//        String[] keyArray = new String[]
//                {
//                        "hers",
//                        "his",
//                        "she",
//                        "he"
//                };
//        String text ="uhers";
        String[] keyArray = new String[]
                {
                        "999",
                        "大药房",
                        "旗舰店",
                        "感冒灵"
                };
        String text ="999感冒灵";
        CommonUtil.querySensitiveWord(keyArray,text);
    }
}
