/*
 * Copyright 2016 OpenMarket Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.matrix.androidsdk.data;

import org.matrix.androidsdk.MXSession;

import org.matrix.androidsdk.rest.callback.ApiCallback;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.MatrixError;
import org.matrix.androidsdk.rest.model.RoomResponse;
import org.matrix.androidsdk.rest.model.TokensChunkResponse;

import java.util.Map;

/**
 * The `RoomEmailInvitation` gathers information for displaying the preview of a room that is unknown for the user.
 * Such room can come from an email invitation link or a link to a room.
 */
public class RoomPreviewData {

    private static final String LOG_TAG = "RoomPreviewData";

    // The id of the room to preview.
    private String mRoomId;

    // the id of the event to preview
    private String mEventId;

    // In case of email invitation, the information extracted from the email invitation link.
    private RoomEmailInvitation mRoomEmailInvitation;

    // preview information
    // comes from the email invitation or retrieve from an initialSync
    private String mRoomName;
    private String mRoomAvatarUrl;

    // the room state
    private RoomState mRoomState;

    // The last recent messages of the room.
    private TokensChunkResponse<Event> mMessages;

    // the session
    private MXSession mSession;

    /**
     * Create an RoomPreviewData instance
     * @param session the session.
     * @param roomId the room Id to preview
     * @param eventId the event Id to preview (optional)
     * @param emailInvitationParams the email invitation parameters (optional)
     */
    public RoomPreviewData(MXSession session, String roomId, String eventId, Map<String, String> emailInvitationParams) {
        mSession = session;
        mRoomId = roomId;
        mEventId = eventId;

        if (null != emailInvitationParams) {
            mRoomEmailInvitation = new RoomEmailInvitation(emailInvitationParams);
            mRoomName = mRoomEmailInvitation.roomName;
            mRoomAvatarUrl = mRoomEmailInvitation.roomAvatarUrl;
        }
    }

    /**
     * @return the room state
     */
    public RoomState getRoomState() {
        return mRoomState;
    }

    /**
     * @return the room name
     */
    public String getRoomName() {
        return mRoomName;
    }

    /**
     * @return the room avatar URL
     */
    public String getRoomAvatarUrl() {
        return mRoomAvatarUrl;
    }

    /**
     * @return the room id
     */
    public String getRoomId() {
        return mRoomId;
    }

    /**
     * @return the event id.
     */
    public String getEventId() {
        return mEventId;
    }

    /**
     * @return the session
     */
    public MXSession getSession() {
        return mSession;
    }

    /**
     * @return the room invitation
     */
    public RoomEmailInvitation getRoomEmailInvitation() {
        return mRoomEmailInvitation;
    }

    /**
     * Attempt to get more information from the homeserver about the room.
     * @param apiCallback the callback when the operation is done.
     */
    public void fetchPreviewData(final ApiCallback<Void> apiCallback) {
        mSession.getRoomsApiClient().initialSync(mRoomId, new ApiCallback<RoomResponse>() {
            @Override
            public void onSuccess(RoomResponse info) {

                mRoomState = new RoomState();
                mRoomState.roomId = mRoomId;

                for(Event event : info.state) {
                    mRoomState.applyState(event, EventTimeline.Direction.FORWARDS);
                }

                mRoomName = mRoomState.getDisplayName(mSession.getMyUserId());
                mRoomAvatarUrl = mRoomState.getAvatarUrl();

                apiCallback.onSuccess(null);
            }

            @Override
            public void onNetworkError(Exception e) {
                apiCallback.onNetworkError(e);
            }

            @Override
            public void onMatrixError(MatrixError e) {
                apiCallback.onMatrixError(e);
            }

            @Override
            public void onUnexpectedError(Exception e) {
                apiCallback.onUnexpectedError(e);
            }
        });
    }
}
