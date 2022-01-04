var eventsByCategory = {
  // Keyboard Events: (see https://w3c.github.io/uievents/#events-keyboardevents and https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent)
  keyboard: [
    "keydown",
    "keyup",
    "keypress",
  ],
  // Composition Events (for IMEs); see https://w3c.github.io/uievents/#events-compositionevents and https://developer.mozilla.org/en-US/docs/Web/API/Element/compositionstart_event
  composition: [
    "compositionstart",
    "compositionupdate",
    "compositionend",
  ],
  // Input Events (supported by modern browsers, not IE); see https://w3c.github.io/uievents/#events-inputevents and https://developer.mozilla.org/en-US/docs/Web/API/InputEvent
  input: [
    "input",
    "beforeinput",
  ],
  // Clipboard Events (see https://developer.mozilla.org/en-US/docs/Web/API/ClipboardEvent)
  clipboard: [
    "cut",
    "copy",
    "paste",
  ],
  // Mouse Events (see https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent)
  mouse: [
    "click",
    "dblclick",
    "mouseup",
    "mousedown",
    "mousemove"
  ],
  // Touch Events (see https://developer.mozilla.org/en-US/docs/Web/API/TouchEvent)
  touch: [
    "touchstart",
    "touchend",
    "touchmove",
    "touchcancel",
  ],
  // Selection Events (see https://developer.mozilla.org/en-US/docs/Web/API/Selection)
  selection: [
    "selectstart",
    "selectionchange",
  ]
}

var eventCounts = {};
var eventToggleWidgets = {};

function print(event, msg) {
  console.log(msg, event)
  var txtArea = gebi("txtOutput");
  txtArea.value += msg + "\n"
  txtArea.scrollTop = 9999999;  // hack to scroll the text area to the end: http://stackoverflow.com/questions/642353/dynamically-scrolling-a-textarea
}

function formatEventProps(event) {
  var props = [
    // keyboard events:
    'code', 'key', 'keyCode',
    // input/composition events:
    'data', 'inputType', 'isComposing',
    // mouse events:
    'clientX', 'clientY',

  ];
  var items = [];
  for (var i = 0; i < props.length; i++) {
    var prop = props[i];
    var value = event[prop];
    if (value !== undefined) {
      var item = prop + "=";
      if (typeof value == 'string')
        value = "'" + value + "'"
      item += value;
      items.push(item);
    }
  }
  return items.join(', ');
}

function handleEvent(jqEvent) {
  var event = jqEvent.originalEvent;
  var eventType = event.type;
  var eventTarget = event.target;
  var eventId = format("%s[%s]", eventType, eventCounts[eventType]++);
  var eventInfo = format("%s{%s}", eventId, formatEventProps(event));
  if (eventToggleWidgets[eventType]) {
    eventToggleWidgets[eventType].update();
  }

  print(event, format('Event   %s: %s', eventInfo, formatState(eventTarget)));
  setTimeout(function () {
    print(event, format('Timeout %s: %s', eventInfo, formatState(eventTarget)));
  }, 1);
}

var lastTimestamp;

/**
 * Prints the current state of the given input element
 * @param {HTMLElement} target
 * @return {String}
 */
function formatState(target) {
  var now = timestamp();
  var timeSinceLast = now - (lastTimestamp || now);
  lastTimestamp = now;
  var msg = "";
  if (target === txtInput) {
    msg = format('text="%s" cursor=%s, sel=%s; ',
        txtInput.value, getCursorPos(txtInput), getSelectionLength(txtInput));
  }
  return format("%s(+%s)", msg, timeSinceLast)
}

/**
 * Returns the caret (cursor) position of the specified text field.
 *
 * @param {HTMLInputElement} field a text input element
 * @return an integer in the range <code>[0, field.value.length)</code>, or <code>0</code> if unable to
 * determine
 * @see http://stackoverflow.com/questions/2897155/get-caret-position-within-an-text-input-field
 */
function getCursorPos(field) {
  // Legacy IE Support (before IE11)
  if (document.selection) {
    // based on GWT's TextBoxImplIE8.getCursorPos:
    try {
      var tr = elem.document.selection.createRange();
      if (tr.parentElement() !== elem)
        return -1;
      return -tr.move("character", -65535);
    }
    catch (e) {
      return 0;
    }
  }
  // Modern browsers: (based on GWT's TextBoxImpl.getCursorPos)
  else {
     // Guard needed for FireFox.
     try {
       return field.selectionStart;
     } catch (e) {
       return 0;
     }
  }
}

/**
 * Returns the selection length within the specified text field.
 *
 * @param {HTMLInputElement} field a text input element
 * @return an integer in the range <code>[0, field.value.length)</code>
 * @see http://stackoverflow.com/questions/2897155/get-caret-position-within-an-text-input-field
 */
function getSelectionLength(field) {
  // Legacy IE Support (before IE11)
  if (document.selection) {
    // based on GWT's TextBoxImplIE8.getSelectionLength:
    try {
      var tr = field.document.selection.createRange();
      if (tr.parentElement() !== field)
        return 0;
      return tr.text.length;
    }
    catch (e) {
      return 0;
    }
  }
  // Modern browsers: (based on GWT's TextBoxImpl.getSelectionLength)
  else {
    // Guard needed for FireFox.
    try{
      return field.selectionEnd - field.selectionStart;
    } catch (e) {
      return 0;
    }
  }
}


function init() {
  // assign some global shortcuts:
  window.txtInput = gebi("txtInput");
  window.htmlInput = gebi("htmlInput");
  // populate the event selector table:
  var $tblEventToggles = $('#tblEventToggles')
  for (var catName in eventsByCategory) {
    if (eventsByCategory.hasOwnProperty(catName)) {
      var eventNames = eventsByCategory[catName];
      var $tr = $('<tr>').append(
          $('<td>').append(new CategoryToggleWidget(catName).$elt)
      );
      // add tblEventToggles row
      var $td = $('<td>').appendTo($tr);
      $tblEventToggles.append($tr);
      for (var i = 0; i < eventNames.length; i++) {
        var evName = eventNames[i];
        eventCounts[evName] = 0;
        var widget = new EventToggleWidget(evName);
        $td.append(widget.$elt);
      }
    }
  }
}

/*
================================================================================
Object-oriented widgets used to implement the event toggles in #tblEventToggles
================================================================================
*/

/**
 * Base class for the toggle widgets. Creates a checkbox element with the given name inside a label element.
 *
 * @param name {String} the name to use for the input element's <code>name</code> attribute and the inner text
 * of the <code>label</code>.
 * @constructor
 */
function ToggleWidget(name) {
  var self = this;
  this.name = name;
  this.$label = $('<label>')
      .append(
          this.$checkbox = $('<input>', {"name": name, "id": 'chk_' + name, "type": "checkbox"}).change(function (e) {
            self.setEnabled(this.checked)
          })
      )
      .append(name); // inner text of the label

  this.$elt = $('<div>', {"id": 'etWidget_' + name, "class": 'evToggle'})
      .append(this.$label);
}
ToggleWidget.prototype.setEnabled = function (enable) {
  enable = Boolean(enable);
  this.$checkbox.prop('checked', enable);  // make sure the checkbox state is consistent (if calling this method outside the change event handler)
  this.$elt.toggleClass("enabled", enable);
};

/**
 * Toggles an individual event.
 *
 * @param evName {String} the event type name
 * @constructor
 * @extends ToggleWidget
 */
function EventToggleWidget(evName) {
  ToggleWidget.call(this, evName);
  eventToggleWidgets[evName] = this;
  var $counter = $('<span>', {"id": 'etCount_' + evName, "class": 'evCount'});
  this.$elt.append($counter);

  this.setEnabled = function (enable) {
    ToggleWidget.prototype.setEnabled.call(this, enable);
    var $inputElts = $('#txtInput, #htmlInput');
    if (enable) {
      $inputElts.on(evName, handleEvent);
    } else {
      $inputElts.off(evName, handleEvent);
    }
    this.update();
  };

  this.update = function () {
    $counter.text(eventCounts[evName]);
  }
}
inheritPrototype(EventToggleWidget, ToggleWidget)

function CategoryToggleWidget(catName) {
  ToggleWidget.call(this, catName);

  this.setEnabled = function (enable) {
    ToggleWidget.prototype.setEnabled.call(this, enable);
    var eventNames = eventsByCategory[catName];
    for (var i = 0; i < eventNames.length; i++) {
      var evName = eventNames[i];
      eventToggleWidgets[evName].setEnabled(enable);
    }
  };
}
inheritPrototype(CategoryToggleWidget, ToggleWidget)
