// Copyright 2019 Google LLC
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

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;



/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public final class DataServlet extends HttpServlet {
  private ArrayList<Task> tasks;
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String comment = request.getParameter("comment-box");
    String displayName = request.getParameter("display-name");
    boolean anonymous = Boolean.parseBoolean(getParameter(request, "anonymous", "false"));
    long timestamp = System.currentTimeMillis();    
    
    if (anonymous) {
        displayName = "Anonymous";
    }
    if (displayName == ""){             //not working correctly right now
        displayName = "Anonymous";
    }
    
    Entity taskEntity = new Entity("Task");
    taskEntity.setProperty("comment", comment);
    taskEntity.setProperty("displayName", displayName);
    taskEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);
    response.sendRedirect("/index.html");
    
    Query query = new Query("Task").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);
    ArrayList<Task> tasks = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      comment = (String) entity.getProperty("comment");
      displayName = (String) entity.getProperty("displayName");
      timestamp = (long) entity.getProperty("timestamp");

      Task task = new Task(id, comment, displayName, timestamp);
      tasks.add(task);
    
    }
  }

  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
  
}
