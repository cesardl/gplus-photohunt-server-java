/*
 * Copyright 2013 Google Inc. All Rights Reserved.
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

package com.google.plus.samples.photohunt;

import static com.google.plus.samples.photohunt.model.OfyService.ofy;

import com.google.plus.samples.photohunt.model.DirectedUserToUserEdge;
import com.google.plus.samples.photohunt.model.User;

import com.googlecode.objectify.Key;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides an API for working with Users. This servlet provides
 * the /api/friends end-point, and exposes the following operations:
 *
 *   GET /api/friends
 *
 * @author vicfryzel@google.com (Vic Fryzel)
 */
public class FriendsServlet extends JsonRestServlet {
  /**
   * Exposed as `GET /api/friends`.
   *
   * Takes no request payload, and identifies the incoming user by the user
   * data stored in their session.
   *
   * Returns the following JSON response representing the User that was
   * connected:
   * [
   *   {
   *     "id":0,
   *     "googleUserId":"",
   *     "googleDisplayName":"",
   *     "googlePublicProfileUrl":"",
   *     "googlePublicProfilePhotoUrl":"",
   *     "googleExpiresAt":0
   *   },
   *   ...
   * ]
   *
   * Issues the following errors along with corresponding HTTP response codes:
   * 401: "Unauthorized request"
   *
   * @see javax.servlet.http.HttpServlet#doGet(
   *     javax.servlet.http.HttpServletRequest,
   *     javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    try {
      checkAuthorization(req);
      String userId = req.getSession()
            .getAttribute(CURRENT_USER_SESSION_KEY).toString();
      User user = ofy().load().key(User.key(
          Long.parseLong(userId))).safeGet();
      List<DirectedUserToUserEdge> edges = ofy().load()
          .type(DirectedUserToUserEdge.class)
          .filter("ownerUserId =", user.getId()).list();
      List<Key<User>> friendKeys = new ArrayList<Key<User>>();
      for (DirectedUserToUserEdge edge : edges) {
        friendKeys.add(User.key(edge.getFriendUserId()));
      }
      Collection<User> friends = ofy().load().keys(friendKeys).values();
      sendResponse(req, resp, friends, "photohunt#friends");
    } catch (UserNotAuthorizedException e) {
      sendError(resp, 401, "Unauthorized request");
    }
  }
}
