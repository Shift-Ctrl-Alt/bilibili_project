package com.oymn.bilibili.service;

import com.oymn.bilibili.dao.repository.UserInfoRepository;
import com.oymn.bilibili.dao.repository.VideoRepository;
import com.oymn.bilibili.domain.UserInfo;
import com.oymn.bilibili.domain.Video;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ElasticSearchService {
    
    @Autowired
    private VideoRepository videoRepository;
    
    @Autowired
    private UserInfoRepository userInfoRepository;
    
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 添加用户信息
     * @param userInfo
     */
    public void addUserInfo(UserInfo userInfo){
        userInfoRepository.save(userInfo);
    }

    /**
     * 全文搜索
     * @param keywords  搜索关键字
     * @param pageNo
     * @param pageSize
     * @return
     */
    public List<Map<String, Object>> getContents(String keywords,
                                                 Integer pageNo,
                                                 Integer pageSize) throws IOException {
        //索引
        String[] indexs = {"videos", "user-infos"};
        
        SearchRequest searchRequest = new SearchRequest(indexs);
        
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //配置sourceBuilder
        sourceBuilder.from(pageNo - 1);
        sourceBuilder.size(pageSize);
        //对视频的标题和简介，用户的昵称进行搜索
        MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery(keywords, "title", "description", "nick");
        sourceBuilder.query(matchQueryBuilder);
        //设置超时时间（60秒）
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //配置searchRequest
        searchRequest.source(sourceBuilder);
        
        //高亮显示功能
        String[] array = {"title", "description", "nick"};
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        for (String key : array) {
            highlightBuilder.fields().add(new HighlightBuilder.Field(key));
        }
        highlightBuilder.requireFieldMatch(false);   //如果要多个字段进行高亮，需要设置为false
        highlightBuilder.preTags("<span style=\"color:red\">");
        highlightBuilder.postTags("</span>");
        
        sourceBuilder.highlighter(highlightBuilder);

        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<Map<String, Object>> arrayList = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits()) {
            //处理结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            Map<String, Object> sourceMap = hit.getSourceAsMap();
            for (String key : array) {
                HighlightField field = highlightFields.get(key);
                if (field != null) {
                    Text[] fragments = field.fragments();
                    String str = Arrays.toString(fragments);
                    str = str.substring(1, str.length() - 1);
                    sourceMap.put(key, str);
                }
            }
            
            arrayList.add(sourceMap);
        }
        
        return arrayList;
    }
    /**
     * 添加视频到es
     * @param video
     */
    public void addVideo(Video video){
        videoRepository.save(video);
    }

    /**
     * 根据标题模糊查询视频
     * @param keyword
     */
    public Video getVideos(String keyword){
        return videoRepository.findByTitleLike(keyword);
    }

    /**
     * 删除es中所有视频
     */
    public void deleteAllVideos(){
        videoRepository.deleteAll();
    }
}
