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

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class FindMeetingQuery {
  private Collection<Event> events;
  private MeetingRequest request;
  private long duration;
  private int earliestStart;
  private int latestEnd;
  private boolean potentialClash;
  private boolean potentialClashFlag;
  private boolean gapFlag;
  
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    this.events = events;
    this.request = request;
    duration = request.getDuration();
    if (request.getAttendees().isEmpty()){
      return Arrays.asList(TimeRange.WHOLE_DAY);
    } else if (Long.compare(duration, TimeRange.WHOLE_DAY.duration()) > 0) {
      return Arrays.asList();
    } else if (!events.isEmpty()){
      earliestStart = TimeRange.END_OF_DAY;
      latestEnd = TimeRange.START_OF_DAY;
      potentialClashFlag = false;
      gapFlag = false;
      for (Event event: events){
        potentialClash = false;
        for (String requestAttendee: request.getAttendees()){
            if (event.getAttendees().contains(requestAttendee)){
              potentialClash = true;
              potentialClashFlag = true;
            }
        }
        if (potentialClash = true && (event.getWhen().start() < earliestStart)){
          earliestStart = event.getWhen().start();
        }
        else if (potentialClash = true && (event.getWhen().start() > latestEnd)){
          gapFlag = true;
          if ((earliestStart == TimeRange.START_OF_DAY) && (event.getWhen().end() == 1440)){
            if ((event.getWhen().start()-latestEnd) < duration){
              return Arrays.asList();
            }
            return Arrays.asList(TimeRange.fromStartEnd(latestEnd, event.getWhen().start(), false));
          } else if (earliestStart == TimeRange.START_OF_DAY) {
            return Arrays.asList(TimeRange.fromStartEnd(latestEnd, event.getWhen().start(), false), TimeRange.fromStartEnd(event.getWhen().end(), TimeRange.END_OF_DAY, true));
          } else if (event.getWhen().end() == 1440) {
            return Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, earliestStart, false), TimeRange.fromStartEnd(latestEnd, event.getWhen().start(), false));
          } else {
            return Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, earliestStart, false), TimeRange.fromStartEnd(latestEnd, event.getWhen().start(), false), TimeRange.fromStartEnd(event.getWhen().end(), TimeRange.END_OF_DAY, true));
          }
        }
        if (potentialClash = true && (event.getWhen().end() > latestEnd)){
          latestEnd = event.getWhen().end();
        }
      }
      if (!potentialClashFlag){
        return Arrays.asList(TimeRange.WHOLE_DAY);
      } else {
        return Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, earliestStart, false), TimeRange.fromStartEnd(latestEnd, TimeRange.END_OF_DAY, true));
      }
    } else {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

  }
}
