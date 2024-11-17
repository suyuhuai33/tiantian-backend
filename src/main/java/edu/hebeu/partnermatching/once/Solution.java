package edu.hebeu.partnermatching.once;

import java.util.ArrayList;
import java.util.List;

public class Solution {
    //存储结果
    List<List<Integer>> result = new ArrayList<>();
    //一次的遍历路径
    List<Integer> path =  new ArrayList<>();

    public List<List<Integer>> combinationSum3(int k, int n) {
        backTracking(k, n, 0, 1);
        return result;
    }

    public void backTracking(int k, int targetNum, int sum, int startIndex){
        //剪枝
        if(sum > targetNum){
            return;
        }
        if(path.size() == k){
            if(sum == targetNum){
                result.add(path);
            }
        }
        for (int i = startIndex; i < 9 - (k - path.size()) + 1; i++){
            sum += i;
            path.add(i);
            backTracking(k, targetNum, sum, i + 1);
            sum -= i;
            path.remove(path.size() - 1);
        }
    }
}
