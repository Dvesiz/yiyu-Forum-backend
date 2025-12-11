package org.jsut.controller;

import org.jsut.mapper.FavoriteMapper;
import org.jsut.pojo.Article;
import org.jsut.pojo.Result;
import org.jsut.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorite") // 关键：定义了 /favorite 路径
public class FavoriteController {

    @Autowired
    private FavoriteMapper favoriteMapper;

    // 1. 添加收藏接口
    @PostMapping("/add")
    public Result add(Integer articleId) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        favoriteMapper.add(userId, articleId);
        return Result.success();
    }

    // 2. 取消收藏接口
    @DeleteMapping("/delete")
    public Result delete(Integer articleId) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        favoriteMapper.delete(userId, articleId);
        return Result.success();
    }

    // 3. 检查是否收藏 (详情页用)
    @GetMapping("/check")
    public Result<Boolean> check(Integer articleId) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        int count = favoriteMapper.count(userId, articleId);
        return Result.success(count > 0);
    }

    // 4. 我的收藏列表
    @GetMapping("/list")
    public Result<List<Article>> list() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        List<Article> list = favoriteMapper.list(userId);
        return Result.success(list);
    }
}