package com.wayn.common.util;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ID生成帮助类
 */
public class IdUtil {


    /**
     * 生成UUID
     *
     * @return string
     */
    public static String getUid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 生成分布式ID，依赖雪花算法
     *
     * @return long
     */
    public static long getSnowFlakeId() {
        return new SnowFlake(SnowFlake.getDataCenterId(), SnowFlake.getMachineId()).nextId();
    }

    /**
     * 雪花算法实现
     */
    private static class SnowFlake {

        /**
         * 起始的时间戳，2021-05-01
         */
        private final static long START_STAMP = 1619798400000L;

        /**
         * 每一部分占用的位数
         */
        private final static long SEQUENCE_BIT = 12; // 序列号占用的位数
        private final static long MACHINE_BIT = 5;   // 机器标识占用的位数
        private final static long DATACENTER_BIT = 5;// 数据中心占用的位数

        /**
         * 每一部分的最大值
         */
        private final static long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);
        private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
        private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

        /**
         * 每一部分向左的位移
         */
        private final static long MACHINE_LEFT = SEQUENCE_BIT;
        private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
        private final static long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;
        private final long datacenterId;  // 数据中心
        private final long machineId;     // 机器标识

        private final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current(); // 线程安全的Random
        private final int clockBackOffset = 10; // 可容忍的时钟回拨偏移量
        private long sequence = 0L; // 序列号
        private long lastStamp = -1L;// 上一次时间戳


        public SnowFlake(long datacenterId, long machineId) {
            if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
                throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
            }
            if (machineId > MAX_MACHINE_NUM || machineId < 0) {
                throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
            }
            this.datacenterId = datacenterId;
            this.machineId = machineId;
        }

        public static void main(String[] args) {
            SnowFlake snowFlake = new SnowFlake(getDataCenterId(), getMachineId());
            long start = System.currentTimeMillis();
            for (int i = 0; i < 5000000; i++) {
                System.out.println(snowFlake.nextId());
            }

            System.out.println(System.currentTimeMillis() - start);
        }

        private static long getMachineId() {
            try {
                String hostAddress = Inet4Address.getLocalHost().getHostAddress();
                int[] ints = StringUtils.toCodePoints(hostAddress);
                int sums = 0;
                for (int b : ints) {
                    sums += b;
                }
                return sums % 32;
            } catch (UnknownHostException e) {
                // 如果获取失败，则使用随机数备用
                return RandomUtils.nextLong(0, 31);
            }
        }

        private static long getDataCenterId() {
            int[] ints = StringUtils.toCodePoints(SystemUtils.getHostName());
            int sums = 0;
            for (int i : ints) {
                sums += i;
            }
            return sums % 32;
        }

        /**
         * 产生下一个ID
         *
         * @return long
         */
        public synchronized long nextId() {
            long currStamp = getNewStamp();
            // 检查时钟是否回拨
            if (currStamp < lastStamp) {
                // 校验时间偏移回拨量
                long offset = lastStamp - currStamp;
                if (offset > clockBackOffset) {
                    throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
                }

                try {
                    // 等待2倍的时间偏移量，容错处理
                    this.wait(offset << 1);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            // 重新获取当前时间
            currStamp = getNewStamp();
            if (currStamp < lastStamp) {
                throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
            }

            if (currStamp == lastStamp) {
                // 相同毫秒内，序列号自增，取值范围0-MAX_SEQUENCE之间
                sequence = (sequence + 1) & MAX_SEQUENCE;
                // 同一毫秒的序列数已经达到最大
                if (sequence == 0L) {
                    currStamp = getNextMill();
                }
            } else {
                // 不同毫秒内，序列号初始设置为0-1范围之间，保证奇偶数据出现概率持平
                sequence = threadLocalRandom.nextInt(2);
            }

            lastStamp = currStamp;

            return (currStamp - START_STAMP) << TIMESTAMP_LEFT // 时间戳部分
                    | datacenterId << DATACENTER_LEFT          // 数据中心部分
                    | machineId << MACHINE_LEFT                // 机器标识部分
                    | sequence;                                // 序列号部分
        }

        private long getNextMill() {
            long mill = getNewStamp();
            while (mill <= lastStamp) {
                mill = getNewStamp();
            }
            return mill;
        }

        private long getNewStamp() {
            return System.currentTimeMillis();
        }

    }

}
