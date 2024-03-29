package com.dotorg.api.objects;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Parent;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * dotOrg-api
 * Date Created: 5/21/2016
 * |
 * Original Package: com.dotorg.api.objects
 * |
 * COPYRIGHT 2016
 */
@Entity
public class News {

    @Id
    private Long newsId;

    private String title;
    private String description;

    private Date createdAt;

    private Long memberId;
    private String name;
    private String imageUrl;

    @Ignore
    private Collection<Object> attachments;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    @Parent
    private Key<Group> groupKey;

    private News() {
    }

    public void createNewNews(Member member, User user, Key<Group> groupKey) {
        this.createdAt = new Date();
        this.memberId = member.getMemberId();
        this.name = member.getNickname();
        this.imageUrl = user.getImageUrl();
        this.groupKey = groupKey;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<News> getKey() {
        return Key.create(News.class, newsId);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.FALSE)
    public Long getNewsId() {
        return newsId;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public void setNewsId(Long newsId) {
        this.newsId = newsId;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.FALSE)
    public Long getMemberId() {
        return memberId;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Member> getMemberKey() {
        return Key.create(Member.class, memberId);
    }


    @ApiResourceProperty(ignored = AnnotationBoolean.FALSE)
    public String getName() {
        return name;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public void setName(String name) {
        this.name = name;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.FALSE)
    public String getImageUrl() {
        return imageUrl;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Collection<Object> getAttachments() {
        return attachments;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public void setAttachments(List<Object> attachments) {
        this.attachments = attachments;
    }


    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Group> getGroupKey() {
        return groupKey;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public void setGroupKey(Key<Group> groupKey) {
        this.groupKey = groupKey;
    }
}
