package com.steelrain.lilac.batch.youtube;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class UtilFunctionTest {
    @Test
    public void testFormattedVideoIdString(){
        List<String> videoIds = new ArrayList<>(50);
        videoIds.add("jHFTVnVRXxM");
        /*videoIds.add("epUoyKZdJbU");
        videoIds.add("IuC05OTzMZs");
        videoIds.add("4amn4IxhDMw");
        videoIds.add("ui7cD7RNT_k");
        videoIds.add("Q0LoqvUj5TY");
        videoIds.add("cpzeO67w4oc");
        videoIds.add("Vb_4_oVnTtw");
        videoIds.add("_SK1E5F2hm4");
        videoIds.add("gER3iJZyd0A");
        videoIds.add("DgLyvEBeasc");
        videoIds.add("p-3dpi60W0c");
        videoIds.add("TUGJeCV31hU");
        videoIds.add("r3EFCezvflc");
        videoIds.add("NoUeKAbJIrc");
        videoIds.add("wZd-z7PDkE8");
        videoIds.add("WXOrarrdm5Q");
        videoIds.add("mXqkpX3cONA");
        videoIds.add("KIQuxDzuG6A");
        videoIds.add("-ag-GmvifSg");
        videoIds.add("AQRk8BlV2xI");
        videoIds.add("VItdcbsuj4I");
        videoIds.add("nAexDd8FBk0");
        videoIds.add("6pW-vAkLv-w");
        videoIds.add("uym8GmB49e4");
        videoIds.add("HXp3MD5WeiM");
        videoIds.add("6SJ6TPMKFC8");
        videoIds.add("0J6pkS84Yfs");
        videoIds.add("hzDE4wMoWOg");
        videoIds.add("DGwvOGuB0bk");
        videoIds.add("Al05JtY0nUE");
        videoIds.add("IXcgoqCYZKw");
        videoIds.add("QbrXkjVSZZs");
        videoIds.add("XwV97Aw4E-4");
        videoIds.add("ycMoJCtGQ9c");
        videoIds.add("fdHSCt4qknA");
        videoIds.add("eEI3ffvS-rU");
        videoIds.add("UdRdAATu8nI");
        videoIds.add("OSb6YgwE-i8");
        videoIds.add("C_EUxLnwOcM");
        videoIds.add("9MHoWe8V_qE");*/
        System.out.println("videoIds.count : "+videoIds.size());
        StringBuilder videoIdBuilder = new StringBuilder(1024);
        /*for(int i=0, size=videoIds.size()-1 ; i <= size ; i++){
            videoIdBuilder.append(videoIds.get(i));
            if(i < size){
                videoIdBuilder.append(",");
            }
        } // 영상의 상세정보를 얻기 위한 파라미터 만들기 끝*/
        for(String item : videoIds){
            videoIdBuilder.append(item);
            videoIdBuilder.append(",");
        }
        String tmp = videoIdBuilder.toString();
        String res = tmp.substring(0, tmp.length()-1);
        System.out.println("문자열 결과 : " + res);
    }
}
