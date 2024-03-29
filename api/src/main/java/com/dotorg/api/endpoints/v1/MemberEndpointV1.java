package com.dotorg.api.endpoints.v1;

import com.dotorg.api.exceptions.InvalidParameterException;
import com.dotorg.api.objects.Group;
import com.dotorg.api.objects.Member;
import com.dotorg.api.objects.Membership;
import com.dotorg.api.objects.User;
import com.dotorg.api.utils.ValidationHelper;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiReference;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;

@ApiReference(BaseEndpointV1.class)
public class MemberEndpointV1 {

    private static final Logger logger = Logger.getLogger(MemberEndpointV1.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 200;

    /**
     * Inserts a new {@code Member}.
     */
    @ApiMethod(
            name = "groups.members.add",
            path = "groups/{groupId}/members",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Member add(User addUser, @Named("groupId") Long groupId, @Named("token") String token) throws NotFoundException, UnauthorizedException, InvalidParameterException, BadRequestException {
        User user = ValidationHelper.validateToken(token);

        Group group = ofy().load().type(Group.class).id(groupId).now();
        if (group == null) {
            throw new NotFoundException("Could not find Group with ID: " + groupId);
        }

        ValidationHelper.validateUserInGroup(user, groupId);

        // FUTURE: add memberRole validation

        User newMember;
        if (addUser.getUserId() != null) {
            newMember = ofy().load().type(User.class).id(addUser.getUserId()).now();
        } else if (addUser.getEmail() != null) {
            newMember = ofy().load().type(User.class).filter("email", addUser.getEmail()).first().now();
        } else if (addUser.getPhoneNumber() != null) {
            newMember = ofy().load().type(User.class).filter("phoneNumber", addUser.getPhoneNumber()).first().now();
        } else {
            throw new InvalidParameterException("User provided to add is incorrect.");
        }

        ValidationHelper.validateUserNotInGroup(newMember, groupId);

        // Add member to group
        return newMember.addGroup(group.getGroupId());

        /*Queue queue = QueueFactory.getQueue("add-members");
        for (final User wrapperUser : usersWrapper.getUsers()) {
            queue.add(TaskOptions.Builder.withPayload(new DeferredTask() {
                @Override
                public void run() {
                    User addUser;
                    if (wrapperUser.getUserKey() != null) {
                        addUser = ofy().load().type(User.class).id(wrapperUser.getUserKey()).now();
                    } else if (wrapperUser.getEmail() != null) {
                        addUser = ofy().load().type(User.class).filter("email", wrapperUser.getEmail()).first().now();
                    } else if (wrapperUser.getPhoneNumber() != null) {
                        addUser = ofy().load().type(User.class).filter("phoneNumber", wrapperUser.getPhoneNumber()).first().now();
                    } else {
                        return;
                    }
                    // Add member to group
                    Member member = new Member(group.getKey(), addUser.getUserKey(), 2, addUser.getName());
                    // Use objectify to create entity(Member)
                    ofy().save().entity(member).now();
            }
            }));
        }*/
    }

    /**
     * Updates an existing {@code Member}.
     *
     * @param memberId the ID of the entity to be updated
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code memberId} does not correspond to an existing
     *                           {@code Member}
     */
    @ApiMethod(
            name = "groups.members.update",
            path = "groups/{groupId}/members/{memberId}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Member update(Member updateMember, @Named("groupId") Long groupId, @Named("memberId") Long memberId, @Named("token") String token) throws NotFoundException, UnauthorizedException {
        User user = ValidationHelper.validateToken(token);

        ValidationHelper.validateUserInGroup(user, groupId);

        Member member = ofy().load().type(Member.class).parent(Key.create(Group.class, groupId)).id(memberId).now();
        if (member == null) {
            throw new NotFoundException("Could not find Member with ID: " + memberId);
        }

        // FUTURE: Make so owners and other permissible people can change roles

        ValidationHelper.validateUserIsMember(user, member);

        return updateMember(member, updateMember);
    }


    /**
     * Deletes the specified {@code Member}.
     *
     * @param memberId the ID of the entity to delete
     * @throws NotFoundException if the {@code memberId} does not correspond to an existing
     *                           {@code Member}
     */
    @ApiMethod(
            name = "groups.members.remove",
            path = "groups/{groupId}/members/{memberId}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("groupId") Long groupId, @Named("memberId") Long memberId, @Named("token") String token) throws NotFoundException, UnauthorizedException {
        User user = ValidationHelper.validateToken(token);

        Group group = ofy().load().type(Group.class).id(groupId).now();
        if (group == null) {
            throw new NotFoundException("Could not find Group with ID: " + groupId);
        }

        ValidationHelper.validateUserInGroup(user, groupId);
        // FUTURE: Future add memberRole validation

        // May need to remove parent(group)
        Member member = ofy().load().type(Member.class).parent(group).id(memberId).now();
        if (member == null) {
            throw new NotFoundException("Could not find Member with ID: " + memberId);
        }

        ValidationHelper.validateMemberOfGroup(member, groupId);
        ValidationHelper.validateUserIsNotGroupOwner(member.getUserId(), group);

        Membership membership = ofy().load().type(Membership.class).id(member.getMemberId()).now();
        if (membership == null) {
            throw new NotFoundException("Could not find Member with ID: " + member.getUserId());
        }

        // If user == member then just leave not remove from group
        if (Objects.equals(user.getUserId(), member.getUserId())) {
            membership.setPrevious(true);
            ofy().save().entity(membership);
            logger.info("Member with ID: " + memberId + " left " + groupId);
        } else {
            membership.setKicked(true);
            logger.info("Kicked Member with ID: " + memberId);
        }
        ofy().save().entity(membership);
    }

    /**
     * List members for a specific {@code groupId}
     *
     * @param groupId of the group to list members in
     * @param cursor  start location of the query
     * @param limit   limit for the query, default 200
     * @param token token for current session
     * @return a response that encapsulates the result list and the next page token/cursor
     * @throws NotFoundException
     * @throws UnauthorizedException
     */
    @ApiMethod(
            name = "groups.members.list",
            path = "groups/{groupId}/members",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Member> list(@Named("groupId") Long groupId, @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit, @Named("token") String token) throws NotFoundException, UnauthorizedException {
        User user = ValidationHelper.validateToken(token);

        ValidationHelper.validateUserInGroup(user, groupId);

        Group group = ofy().load().type(Group.class).id(groupId).now();
        if (group == null) {
            throw new NotFoundException("Could not find Group with ID: " + groupId);
        }

        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        // Load members by group
        Query<Member> query = ofy().load().type(Member.class).ancestor(group).limit(DEFAULT_LIST_LIMIT);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        QueryResultIterator<Member> queryIterator = query.iterator();
        List<Member> memberList = new ArrayList<>(limit);
        while (queryIterator.hasNext()) {
            Member member = queryIterator.next();
            Membership membership = ofy().load().key(member.getMembershipKey()).now();
            if (membership != null) {
                member.setKicked(membership.isKicked());
                member.setLeftGroup(membership.isPrevious());
                memberList.add(member);
            }
        }

        return CollectionResponse.<Member>builder().setItems(memberList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private Member updateMember(Member member, Member updateMember) {
        if (updateMember.getNickname() != null) {
            member.setNickname(updateMember.getNickname());
        }

        // TODO: More here // FUTURE: Permissions

        ofy().save().entity(member).now();
        logger.info("Updated Member: " + member);
        return ofy().load().entity(member).now();
    }

}