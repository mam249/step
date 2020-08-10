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
    private int i;
    private int j;
    private boolean containsAttendees;
    private Collection<TimeRange> available;
    private Collection<TimeRange> output;
  
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    this.events = events;
    this.request = request;
    duration = request.getDuration();
    if (Long.compare(duration, TimeRange.WHOLE_DAY.duration()) > 0) {
      return Arrays.asList();
    }
    if (request.getAttendees().isEmpty() || events.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    earliestStart = TimeRange.END_OF_DAY;
    latestEnd = TimeRange.START_OF_DAY;
    available = Arrays.asList(TimeRange.WHOLE_DAY);
    for (Event event: events) {
      if (event.getWhen().start() < earliestStart) {
        earliestStart = event.getWhen().start();
      }
      if (event.getWhen().end() > latestEnd) {
        latestEnd = event.getWhen().end();
      }
    }
    for (Event event: events) {
      containsAttendees = eventContainsAttendees(event, request);
      if (containsAttendees) {
          //throw new UnsupportedOperationException("just enough room?");
        available = splitAvailableTime(available, event);
      } else {
        return Arrays.asList(TimeRange.WHOLE_DAY);
      }
    }

    return removeNoDurationEvents(available);
  }



//works
  private boolean eventContainsAttendees(Event event, MeetingRequest request) {
    for (String requestAttendee: request.getAttendees()) {
      if (event.getAttendees().contains(requestAttendee)) {
        return true;  
      }
    }
    return false;
  }
  //not
  private boolean eventFitsInAvailable(Collection<TimeRange> available, Event event) {
    for (TimeRange slot: available) {
      if (slot.contains(TimeRange.fromStartEnd(event.getWhen().start(), event.getWhen().end(), false))) {
        return true;
      }
    }
    return false;
  }

// working
  private Collection<TimeRange> splitAvailableTime(Collection<TimeRange> available, Event event) {
    output = new ArrayList<>();
    for (TimeRange slot: available) {
      if ((slot.start() <= event.getWhen().start()) && (slot.end() >= event.getWhen().end())) {
        output.add(TimeRange.fromStartEnd(slot.start(), event.getWhen().start(), false));
        if (earliestStart>slot.start()) {
          output.add(TimeRange.fromStartEnd(slot.start(), earliestStart, false));
        }
        output.add(TimeRange.fromStartEnd(event.getWhen().end(), slot.end(), false));
        if (slot.end() > latestEnd) {
          output.add(TimeRange.fromStartEnd(latestEnd, slot.end(), false));
        }
      } else {
        output.add(slot);
      }
    }
    return cleanUpDuplicates(removeNoDurationEvents(output));
  }


  private boolean notEnoughRoom(Collection<TimeRange> available, Event event) {
    for (TimeRange slot: available) {
        if (slot.overlaps(event.getWhen())) {
          return false;
        }
    }
    return true;
  }

  private Collection<TimeRange> removeNoDurationEvents(Collection<TimeRange> available) {
      //removing (0,0), (1440,1440) etc from available:
    output = new ArrayList<>();
    for (TimeRange slot: available){
      if (slot.start() != slot.end()){
        output.add(slot);
      }
    }
    return output;
  }

  private Collection<TimeRange> cleanUpDuplicates(Collection<TimeRange> available) {
    output = new ArrayList<>();
    i=0;
    j=0;
    for (TimeRange slot: available) {
      for (TimeRange slot2: available) {
        if (i!=j){
          if (slot.contains(slot2) && !output.contains(slot2)){
            output.add(slot2);
          } else if (!output.contains(slot)) {
            output.add(slot);
          }
        }
        j+=1;
      }
      i+=1;
    }
    return output;
  }

/*
Day:
  |_________________________________|
  Events:
  |____X______XXXXXX________________|

  A- get a list of times that dont work
   -> from this get the times that work
  b- start w full day and subtract times that dont work

B-
0-24
->4-5
0-4, 5-24
-> 14-17
0-4, 5-14, 17-24

query() {
  List<TimeRange> available = 0-24
  for event : events
    if someone I care is in event
    available remove event time / split
}

*/

}
