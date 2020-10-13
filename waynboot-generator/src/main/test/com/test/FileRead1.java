package com.test;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FileRead1 {
    public static void main(String[] args) {
        File file1 = new File("E:\\扫黑办聚合码\\打印一张");
        File file2 = new File("E:\\扫黑办聚合码\\打印两张");
        File file3 = new File("E:\\扫黑办聚合码\\打印三张");
        List<String> all = new ArrayList<>();
        List<String> list1 = Arrays.asList(file1.list());
        System.out.println(list1.size());
        all.addAll(list1);
        List<String> list2 = Arrays.asList(file2.list());
        all.addAll(list2);
        List<String> list3 = Arrays.asList(file3.list());
        all.addAll(list3);
        System.out.println(all.size());

        List<Integer> names = new ArrayList<>();
        for (String s : all) {
            if (s.contains("下载")) {
                continue;
            }
            System.out.println(s);
            String[] split = s.split("-", -1);
            if (split.length == 2) {
                if (Integer.parseInt(split[0].trim()) < 16) {
                    if (split[1].trim().contains("-")) {
                        names.add(Integer.parseInt(split[1].trim().split("-.png", -1)[0]));
                    } else {
                        names.add(Integer.parseInt(split[1].trim().split(".png", -1)[0]));
                    }
                } else {
                    names.add(Integer.parseInt(split[0].trim()));
                }
            }
            if (split.length == 3) {
                names.add(Integer.parseInt(split[1].trim()));
            }
        }
        names = names.stream().distinct().collect(Collectors.toList());
        names.sort(Comparator.comparingInt(o -> o));
        System.out.println(StringUtils.join(names, ","));
        System.out.println(names.size());
    }

}
