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

/**
 * Adds a random greeting to the page.
 */
function addRandomImage() {
  const image_numbers = ['1692', '1710', '1740', '1741', '2938', '2940', '5317', '7953', '7954', '7983', '8105', '9292', '9303', '9377', '9378', '9380'];


  // Pick a random greeting.
  const image_number = image_numbers[Math.floor(Math.random() * image_numbers.length)];
  const image_url = "/images/IMG_" + image_number + ".jpg";
  // Add it to the page.

  const image_element = document.createElement('img');
  image_element.src = image_url;

  const imageContainer = document.getElementById('image-container');
  imageContainer.innerHTML = '';
  imageContainer.appendChild(image_element);
}

function loadTasks() {
  fetch('/data').then(response => response.json()).then((tasks) => {
    const taskListElement = document.getElementById('task-list');
    tasks.forEach((task) => {
      taskListElement.appendChild(createTaskElement(task));
    })
  });
}

/** Creates an element that represents a task,*/
function createTaskElement(task) {
  const taskElement = document.createElement('li');
  taskElement.className = 'task';

  const titleElement = document.createElement('span');
  titleElement.innerText = task.comment + ' - Commented by ' + task.displayName;

  taskElement.appendChild(titleElement);
  return taskElement;
}
