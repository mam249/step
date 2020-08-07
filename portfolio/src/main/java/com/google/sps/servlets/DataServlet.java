// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;
import com.google.sps.servlets.DataServlet;

import com.google.gson.Gson;
import com.google.sps.data.Task;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/** Servlet that handles comments data */
@WebServlet("/data")
public final class DataServlet extends HttpServlet {
  private ArrayList<Task> tasks;
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String comment = request.getParameter("comment-box");
    String displayName = request.getParameter("display-name");
    boolean anonymous = Boolean.parseBoolean(getParameter(request, "anonymous", "false"));

    long timestamp = System.currentTimeMillis();    
    Entity taskEntity = new Entity("Task");
    taskEntity.setProperty("comment", comment);
    taskEntity.setProperty("displayName", displayName);
    taskEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);
    response.sendRedirect("/login");
  }
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Task").addSort("timestamp", SortDirection.DESCENDING);

    FetchOptions options = FetchOptions.Builder.withLimit(100000);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<Entity> results = datastore.prepare(query).asList(options);
    ArrayList<Task> tasks = new ArrayList<>();
    String comment = request.getParameter("comment-box");
    String displayName = request.getParameter("display-name");
    boolean anonymous = Boolean.parseBoolean(getParameter(request, "anonymous", "false"));
    long timestamp = System.currentTimeMillis();
    for (Entity entity : results) {
      long id = entity.getKey().getId();
      comment = (String) entity.getProperty("comment");
      displayName = (String) entity.getProperty("displayName");
      timestamp = (long) entity.getProperty("timestamp");

      Task task = new Task(id, comment, displayName, timestamp);
      tasks.add(task);
    }
    
    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(tasks));

  }

  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
  
}
