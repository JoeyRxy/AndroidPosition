package com.sjtu.demoapp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Hidden Markov Model for location (path) detection.
 */
public class HMM {

    /**
     * L个位置：{0,1,...,L-1}
     */
    private final int L;

    /**
     * <code>omitProb[l].get(pci)</code>是l号位置的基站为pci的rsrp概率分布(正态分布<code>N(mu,sigma)</code>)，其中<code>mu</code>是l位置pci基站的rsrp数据的均值，<code>sigma</code>是l位置pci基站的rsrp数据的标准差
     */
    private final Map<Integer, NormalDistribution>[] omitProb;

    /**
     *
     * @param L     位置集合{0,1,...,L-1}
     * @param stats 各个位置上的各个基站的统计信息：<code>stats[l].get(pci)</code>是l位置pci基站的统计信息（均值和标准差）
     */
    @SuppressWarnings("unchecked")
    public HMM(int L, Map<Integer, Stat>[] stats) {
        assert L > 0;
        this.L = L;
        assert stats.length == L;
        //
        omitProb = new Map[L];
        for (int i = 0; i < L; ++i) {
            omitProb[i] = new HashMap<>();
            for (Map.Entry<Integer, Stat> entry : stats[i].entrySet()) {
                omitProb[i].put(entry.getKey(),
                        new NormalDistribution(entry.getValue().getMean(),
                                entry.getValue().getStd()));
            }
        }
    }

    public double[] getInitProb0() {
        double[] initProb = new double[L];
        Arrays.fill(initProb, Util.logarithm(1. / L));
        return initProb;
    }

    public double[] getInitProb1(Map<Integer, Double> observation) {
        double []initProb = new double[L];
        for (int l = 0; l < L; ++l) {
            initProb[l] = getOmitProb(l, observation);
        }
        return initProb;
    }

    /**
     *
     * @param i   omitProb[i]
     * @param obs one observatoin: <pci, rsrp>
     * @return <strong>logarithm</strong> of the probability of the observation
     */
    private double getOmitProb(int i, Map<Integer, Double> obs) {
        double prob = 0;
        NormalDistribution pdf;
        for (Map.Entry<Integer, Double> entry : obs.entrySet())
            if (prob != Double.NEGATIVE_INFINITY && (pdf = omitProb[i].get(entry.getKey())) != null)
                prob += Util.logarithm(pdf.density(entry.getValue()));
        return prob;
    }

    /**
     * Attention: all probabilities shoule be converted to
     * <strong>logarithm</strong>, u should use {@link com.sjtu.demoapp.Util#logarithm(double[])} and {@link com.sjtu.demoapp.Util#logarithm(double[][][])}
     *
     * @param markov       T个时刻的转移概率矩阵，其中0时刻的转移矩阵无效。<code>markov[t][i][j]</code>表示从t-1时刻的i位置到t时刻的j位置的转移概率。
     * @param initProb     初始时刻的概率
     * @param observations T个时刻的观测序列
     * @return 根据观测序列得到可能的状态序列（位置序列）
     */
    public Integer[] viterbi(double[][][] markov, double[] initProb, Map<Integer, Double>[] observations) {
        int T = observations.length;
        assert T > 1;
        assert markov.length == T;
        assert markov[0].length == L;
        assert markov[0][0].length == L;
        assert initProb.length == L;
        //
        //
        double[][] delta = new double[L][2];
        int[][] psi = new int[L][T];
        //
        for (int i = 0; i < L; ++i) {
            delta[i][0] = initProb[i] + getOmitProb(i, observations[0]);
            psi[i][0] = 0;
        }
        //
        for (int t = 1; t < T; ++t) {
            for (int j = 0; j < L; ++j) { // t时刻的状态为j
                double max = Double.NEGATIVE_INFINITY;
                int argmax = -1;
                for (int i = 0; i < L; ++i) {// 枚举t-1时刻的状态（i）
                    double tmp = delta[i][t - 1 & 1] + markov[t][i][j];
                    if (tmp > max) {
                        max = tmp;
                        argmax = i;
                    }
                }
                delta[j][t & 1] = max + getOmitProb(j, observations[t]);
                psi[j][t] = argmax;
            }
        }
        //
        double max = Double.NEGATIVE_INFINITY;
        int argmax = -1;
        for (int i = 0; i < L; ++i) {
            if (delta[i][T - 1 & 1] > max) {
                max = delta[i][T - 1 & 1];
                argmax = i;
            }
        }
        //
        Integer[] path = new Integer[T];
        path[T - 1] = argmax;
        for (int t = T - 2; t >= 0; --t)
            path[t] = psi[path[t + 1]][t + 1];

        return path;
    }

    /**
     * Attention: all probabilities shoule be converted to
     * <strong>logarithm</strong>, u should use {@link com.sjtu.demoapp.Util#logarithm(double[])} and {@link com.sjtu.demoapp.Util#logarithm(double[][])}
     *
     * @param markov       转移概率矩阵
     * @param initProb     初始时刻的概率
     * @param observations T个时刻的观测序列
     * @return 根据观测序列得到可能的状态序列（位置序列）
     */
    public Integer[] viterbi(double[][] markov, double[] initProb, Map<Integer, Double>[] observations) {
        int T = observations.length;
        assert T > 1;
        assert markov.length == L;
        assert markov[0].length == L;
        assert initProb.length == L;
        //
        //
        double[][] delta = new double[L][2];
        int[][] psi = new int[L][T];
        //
        for (int i = 0; i < L; ++i) {
            delta[i][0] = initProb[i] + getOmitProb(i, observations[0]);
            psi[i][0] = 0;
        }
        //
        for (int t = 1; t < T; ++t) {
            for (int j = 0; j < L; ++j) { // t时刻的状态为j
                double max = Double.NEGATIVE_INFINITY;
                int argmax = -1;
                for (int i = 0; i < L; ++i) {// 枚举t-1时刻的状态（i）
                    double tmp = delta[i][t - 1 & 1] + markov[i][j];
                    if (tmp > max) {
                        max = tmp;
                        argmax = i;
                    }
                }
                delta[j][t & 1] = max + getOmitProb(j, observations[t]);
                psi[j][t] = argmax;
            }
        }
        //
        double max = Double.NEGATIVE_INFINITY;
        int argmax = -1;
        for (int i = 0; i < L; ++i) {
            if (delta[i][T - 1 & 1] > max) {
                max = delta[i][T - 1 & 1];
                argmax = i;
            }
        }
        //
        Integer[] path = new Integer[T];
        path[T - 1] = argmax;
        for (int t = T - 2; t >= 0; --t)
            path[t] = psi[path[t + 1]][t + 1];

        return path;
    }

}

