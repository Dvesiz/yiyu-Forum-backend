package org.jsut.mapper;
import org.apache.ibatis.annotations.*;
import org.jsut.pojo.Article;
import java.util.List;

@Mapper
public interface FavoriteMapper {
    // 添加收藏
    @Insert("insert into favorite(user_id, article_id, create_time) values(#{userId}, #{articleId}, now())")
    void add(Integer userId, Integer articleId);

    // 取消收藏
    @Delete("delete from favorite where user_id = #{userId} and article_id = #{articleId}")
    void delete(Integer userId, Integer articleId);

    // 检查是否收藏
    @Select("select count(*) from favorite where user_id = #{userId} and article_id = #{articleId}")
    int count(Integer userId, Integer articleId);

    // 查询我的收藏列表 (关联查询文章详情)
    @Select("select a.*, u.nickname from favorite f " +
            "join article a on f.article_id = a.id " +
            "left join user u on a.create_user = u.id " +
            "where f.user_id = #{userId} order by f.create_time desc")
    List<Article> list(Integer userId);
}