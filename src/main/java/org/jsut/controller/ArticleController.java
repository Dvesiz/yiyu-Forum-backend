package org.jsut.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.jsut.pojo.Article;
import org.jsut.pojo.PageBean;
import org.jsut.pojo.Result;
import org.jsut.service.ArticleService;
import org.jsut.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;
    @PostMapping("/add")
    public Result add(@RequestBody @Validated Article article){
        articleService.add(article);
        return Result.success();
    }

    @GetMapping("/listsearch")
    public Result<PageBean<Article>> list(
            Integer pageNum,
            Integer pageSize,
            @RequestParam (required = false) Integer categoryId,
            @RequestParam (required = false) String state
    ){
        PageBean<Article> pageBean = articleService.list(pageNum,pageSize,categoryId,state);
        return Result.success(pageBean);
    }

    @GetMapping("/detail")
    public Result<Article> detail(Integer id){
        Article article = articleService.detail(id);
        return Result.success(article);
    }

    @PutMapping("/update")
    public Result update(@RequestBody @Validated Article article){
        articleService.update(article);
        return Result.success();
    }

    @DeleteMapping("/delete")
    public Result delete(Integer id){
        articleService.delete(id);
        return Result.success();
    }

    // 公开文章列表（不需要登录）
    @GetMapping("/public/list")
    public Result<PageBean<Article>> publicList(
            Integer pageNum,
            Integer pageSize,
            Integer categoryId,
            String keyword,
            HttpServletRequest request // <--- 1. 注入 Request 对象
    ) {
        // 2. 尝试获取当前登录用户的 ID
        Integer userId = null;
        String token = request.getHeader("Authorization");
        if (token != null && !token.isEmpty()) {
            try {
                Map<String, Object> claims = JwtUtil.parseToken(token);
                userId = (Integer) claims.get("id");
            } catch (Exception e) {
                // Token 无效或过期，视为未登录，userId 保持 null 即可，不报错
            }
        }

        // 3. 传给 Service
        PageBean<Article> pageBean = articleService.listPublic(pageNum, pageSize, categoryId, keyword, userId);
        return Result.success(pageBean);
    }


    // 公开文章详情（不需要登录）
    @GetMapping("/public/detail")
    public Result<Article> publicDetail(Integer id) {
        Article article = articleService.detail(id);
        return Result.success(article);
    }

    // 修改返回值类型为 Result<Map<String, String>>
    @GetMapping("/ai/generate")
    public Result<Map<String, String>> generateContent(String prompt) {
        // 硅基流动 API 配置
        String apiKey = "sk-ateqrhzfwwweaahzifkhztrpscnnabjlfhsxmjwbsvckbbma";
        String apiUrl = "https://api.siliconflow.cn/v1/chat/completions";

        try {
            // 1. 构建请求体 (JSON)
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "Qwen/Qwen2.5-VL-72B-Instruct");
            requestBody.put("temperature", 1.1);

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            // 提示词微调：确保AI不仅生成内容，还要知道我们需要它按特定格式输出（虽然你的提示词已经很好了）
            systemMsg.put("content", "你是一个专业的文章助手。请根据用户提示生成文章。规则：第一行必须是文章标题（不要加'标题：'前缀），第二行开始是正文内容。HTML格式排版正文。字数500字左右。");
            messages.add(systemMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);

            requestBody.put("messages", messages);

            String jsonBody = mapper.writeValueAsString(requestBody);

            // 2. 发送 HTTP 请求
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 3. 解析返回值
            if (response.statusCode() == 200) {
                JsonNode rootNode = mapper.readTree(response.body());
                String rawContent = rootNode
                        .path("choices").get(0)
                        .path("message")
                        .path("content")
                        .asText();

                // === 核心修改逻辑开始 ===
                String title = "";
                String content = "";

                if (rawContent != null && !rawContent.isEmpty()) {
                    // 使用 split 限制分割成2部分：第一部分是第一行，第二部分是剩余所有内容
                    // (?:\r\n|\r|\n) 是匹配各种换行符的正则
                    String[] parts = rawContent.split("(?:\r\n|\r|\n)", 2);

                    if (parts.length >= 1) {
                        // 去除可能存在的 markdown 符号 (如 **标题**) 和首尾空格
                        title = parts[0].replace("**", "").replace("##", "").trim();
                    }
                    if (parts.length >= 2) {
                        content = parts[1].trim(); // 剩余部分作为正文
                    } else {
                        // 如果AI只返回了一行，那就把这一行既当标题也当内容，或者只当内容，视情况而定
                        // 这里假设如果只有一行，它是正文
                        content = rawContent;
                    }
                }

                Map<String, String> resultMap = new HashMap<>();
                resultMap.put("title", title);
                resultMap.put("content", content);

                return Result.success(resultMap);
                // === 核心修改逻辑结束 ===

            } else {
                return Result.error("AI 响应异常: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("生成失败: " + e.getMessage());
        }
    }

}

