package com.dotorg.api.objects;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

/**
 * dotOrg-api
 * Date Created: 5/21/2016
 * |
 * Original Package: com.dotorg.api.objects
 * |
 * COPYRIGHT 2016
 */
@Entity
public class Chatter {
    @Id
    private Long chatterId;

    private Long memberId;

    private boolean isMuted;

    private boolean ignore;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    @Parent
    private Key<Chat> chatKey;

    private Chatter() {}

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Chatter> getKey() {
        return Key.create(Chatter.class, chatterId);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Long getChatterId() {
        return chatterId;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public void setChatterId(Long chatterId) {
        this.chatterId = chatterId;
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


    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }


    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }


    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Chat> getChatKey() {
        return chatKey;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public void setChatKey(Key<Chat> chatKey) {
        this.chatKey = chatKey;
    }
}
