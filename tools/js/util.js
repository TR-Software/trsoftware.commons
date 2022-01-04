/**
 * Shortcut for <code>document.getElementById(id)</code>
 * @param {String} id
 * @return {HTMLElement}
 */
function gebi(id) {
  return document.getElementById(id);
}

/**
 * Adds an event listener to the given DOM element.
 *
 * @param {Element} element The listener will be added to this DOM element.
 * @param {String} eventName The name of the event, (e.g. "click", or "visibilitychange")
 * @param {Boolean} useCapture If true, useCapture indicates that the user wishes to initiate capture. After initiating capture,
 * all events of the specified type will be dispatched to the registered listener before being dispatched to any
 * EventTarget beneath it in the DOM tree. Events which are bubbling upward through the tree will not trigger a
 * listener designated to use capture. In other words, if false, the event will be handled in the "bubble" phase (not
 * the "preview" phase)
 * @param {function(Event)} listener the function to receive the event
 * @return A JavaScript 0-arg function that can be called to remove the listener added by this method.
 *
 * @see https://developer.mozilla.org/en-US/docs/Web/API/EventTarget/addEventListener
 */
function addEventListener(element, eventName, listener, useCapture) {
  // we attach our listener function in a cross-browser way (idea borrowed from http://javascriptrules.com/2009/07/22/cross-browser-event-listener-with-design-patterns/ )
  if (element.addEventListener) {
    // All standards-compliant browsers
    element.addEventListener(eventName, listener, useCapture);
    return function () {
      element.removeEventListener(eventName, listener, useCapture);
    };
  }
  else {
    // Non-standards-compliant browsers
    eventName = 'on' + eventName;
    if (element.attachEvent) {
      // Internet Explorer before IE9
      // NOTE: these functions don't support the useCapture arg; we could probably hack it by calling setCapture on the element (see https://msdn.microsoft.com/en-us/library/ms536742(v=vs.85).aspx), but it doesn't seem worth it
      element.attachEvent(eventName, listener);
      return function () {
        element.detachEvent(eventName, listener);
      };
    }
    else {
      // last resort (very old browsers, pre-DOM2)
      element[eventName] = listener;
      return function () {
        if (element[eventName] === listener)
          element[eventName] = null;
      };
    }
  }
}

/**
 * Provides a limited form of printf-like string substitution.
 *
 * Takes a template string and any number of additional arguments, and returns the given `template` string
 * with each occurrence of `"%s"` replaced with the corresponding value from the remaining arguments
 * (`arguments[1] ... arguments[arguments.length-1]`).  If there are more arguments than placeholders,
 * the unused arguments will be appended in square brackets at the end.
 *
 * This is similar to the `Strings.lenientFormat` method from the <a href="https://guava.dev/">Guava</a> library,
 * except that `%%s` is treated as a literal `"%s"` and will not be substituted.
 *
 * *Examples:*
 * ```
 *   format("foo%s bar%s", 1)      // returns "foo1 bar%s"
 *   format("foo%s bar%s", 1, 2)   // returns "foo1 bar2"
 *   format("foo%%s bar%s", 1, 2)  // returns "foo%s bar1 [2]"
 * ```
 *
 * @param {String} template a string containing zero or more "`%s`" placeholder sequences
 * @return {String}
 * @see https://guava.dev/releases/snapshot-jre/api/docs/com/google/common/base/Strings.html#lenientFormat(java.lang.String,java.lang.Object...)
 */
function format(template) {
  /*
   NOTE: this function is written for compatibility with older browsers (like IE)
  */
  var args = Array.prototype.slice.call(arguments, 1);  // the values to substitute
  var i = 0;
  /*
   Replace all occurrences of '%s' with the next value from the args (unless the '%s' is preceded by '%')
   NOTE: Since IE doesn't support either String.replaceAll or negative lookbehind in regular expressions,
   we use String.replace with a global regex and we implement negative lookbehind using the workaround suggested
   by https://stackoverflow.com/a/53286417
  */
  var result = template.replace(/(%%s)|(%s)/g,
      function(match, group1, group2) {
        if (group1)
            // matched "%%s": return the literal "%s"
          return "%s";
        // matched "%s": replace with next element from args
        if (i < args.length)
          return args[i++];
        // no more args left, return "%s" as-is
        return group2;
      });
  // if we run out of placeholders, append the extra args in square braces
  if (i < args.length) {
    result += ' [' + args.slice(i).join(', ') + ']';
  }
  return result;
}

/**
 * Tests the <code>format</code> function using console assertions.
 */
// TODO: move unit tests to a different module?
function test_format() {
  function Expectation(expected, code) {
    this.code = code;
    this.expected = expected;
  }
  // TODO: maybe extract a general-purpose unit test library based on this code?
  var expectations = [
    new Expectation("foo", 'format("foo")'),
    new Expectation("foo%s", 'format("foo%s")'),
    new Expectation("foo1", 'format("foo%s", 1)'),
    new Expectation("foo1 bar%s", 'format("foo%s bar%s", 1)'),
    new Expectation("foo1 bar2", 'format("foo%s bar%s", 1, 2)'),
    new Expectation("foo%s bar1 [2]", 'format("foo%%s bar%s", 1, 2)'),
  ];

   // TODO: maybe instead of strings passed to eval, could specify code as anonymous functions and use Function.toString() to print the assertion?
   //    (example: (function(x) { return x === 1; }).toString().replace(/.*return \s*(.*);.*/, '$1') returns 'x === 1')

  for (var i = 0; i < expectations.length; i++) {
    var code = expectations[i].code;
    var expected = expectations[i].expected;
    console.log("Expecting", code, '-->', JSON.stringify(expected))
    var result = eval(code);
    console.assert(result === expected, code + " returned " + JSON.stringify(result) + " instead of " + JSON.stringify(expected))
  }
}

/**
 * Returns the numeric value corresponding to the current time -
 * the number of milliseconds elapsed since 1 January 1970 00:00:00 UTC.
 */
function timestamp() {
  // IE8 does not have Date.now
  if (Date.now) {
    return Date.now();
  }
  return (new Date()).getTime();
}


/*
================================================================================
Polyfills
================================================================================
 */
if (!Object.create) {
  Object.create = function (prototype, properties) {
    /*
     This minimal implementation is a hybrid of https://stackoverflow.com/a/18020329 and https://github.com/es-shims/es5-shim/
     WARNING: it will not work for some cases
       for a complete solution see: https://github.com/es-shims/es5-shim/blob/30ebdda82ed0d8f42b05785debf1e7ee066c3507/es5-sham.js#L199-L333)
    */
    var Type = function Type() {}; // An empty constructor.
    Type.prototype = prototype;
    var object = new Type();
    // must manually set `__proto__` for IE support (see https://github.com/es-shims/es5-shim/blob/v4.5.9/es5-sham.js#L318-L323)
    object.__proto__ = prototype;

    if (properties !== void 0) {
      if (Object.defineProperties)
        Object.defineProperties(object, properties);
      else
        throw "The multi-arg version of Object.create is not provided by this browser and cannot be shimmed.";
    }
    return object;
  };
}
if (!Object.getPrototypeOf) {
  // https://github.com/es-shims/es5-shim/blob/30ebdda82ed0d8f42b05785debf1e7ee066c3507/es5-sham.js#L59-L85
  Object.getPrototypeOf = function getPrototypeOf(object) {
    var proto = object.__proto__;
    if (proto || proto === null) {
      return proto;
    }
    else if (Object.prototype.toString.call(object.constructor) === '[object Function]') {
      return object.constructor.prototype;
    }
    else if (object instanceof Object) {
      return Object.prototype;
    }
    else {
      // Correctly return null for Objects created with `Object.create(null)` or `{ __proto__: null}`.
      // Also returns null for cross-realm objects on browsers that lack `__proto__` support
      // (like IE <11), but that's the best we can do.
      return null;
    }
  };
}
/*
  Partial polyfill for Function.bind borrowed from MDN
    (see http://web.archive.org/web/20210505032957/https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Function/bind#polyfill)
  *NOTE*: subject to the many limitations described on that page (most notably that it doesn't work with `new (funcA.bind(thisArg, args))`)
*/
if (!Function.prototype.bind) (function(){
  var slice = Array.prototype.slice;
  Function.prototype.bind = function() {
    var thatFunc = this, thatArg = arguments[0];
    var args = slice.call(arguments, 1);
    if (typeof thatFunc !== 'function') {
      // closest thing possible to the ECMAScript 5
      // internal IsCallable function
      throw new TypeError('Function.prototype.bind - ' +
             'what is trying to be bound is not callable');
    }
    return function(){
      var funcArgs = args.concat(slice.call(arguments))
      return thatFunc.apply(thatArg, funcArgs);
    };
  };
})();

/*
================================================================================
OOP Utils
================================================================================
*/


/**
 * Implements the traditional approach to JavaScript inheritance by setting the `SubClass` prototype to match
 * the `SuperClass` while preserving the `constructor` function of the `SubClass`.
 *
 * **Example:**
 * ```
 *   function Vector(x, y) {
 *     this.x = +x;
 *     this.y = +y;
 *   }
 *   Vector.prototype.length = function () {
 *     return Math.sqrt(this.dotProduct(this));
 *   }
 *   Vector.prototype.dotProduct = function (vector) {
 *     return this.x * vector.x + this.y * vector.y;
 *   }
 *   // define a subclass that overrides dotProduct by modifying the result returned by superclass
 *   function Vector3d(x, y, z) {
 *     Vector.call(this, x, y);
 *     this.z = +z;
 *   }
 *   inheritPrototype(Vector3d, Vector);
 *   Vector3d.prototype.dotProduct = function (vector) {
 *     return Vector.prototype.dotProduct.call(this, vector) + this.z * (vector.z || 0);
 *   }
 * ```
 *
 * @param SubClass child constructor function
 * @param SuperClass parent constructor function
 *
 * @see getSuper
 * @see https://stackoverflow.com/a/23078075
 * @see https://developer.mozilla.org/en-US/docs/Learn/JavaScript/Objects/Inheritance
 * @see https://johnresig.com/blog/objectgetprototypeof/
 */
function inheritPrototype(SubClass, SuperClass) {
  SubClass.prototype = Object.create(SuperClass.prototype);
  // TODO: should we use Object.defineProperty to make the constructor prop non-enumerable?
  SubClass.prototype.constructor = SubClass;
}

/**
 * Returns the prototype inherited by the given object or class.
 *
 * TODO: test, document, (and potentially rename) this function
 *
 * @param obj a constructor function or an instance
 * @return {Object} the prototype of the argument's immediate superclass
 */
function getSuper(obj) {
  return Object.getPrototypeOf(obj.prototype || Object.getPrototypeOf(obj))
}



// TODO: move unit tests to a different module?
function test_inheritPrototype() {
  function Vector(x, y) {
    this.x = +x;
    this.y = +y;
  }
  Vector.prototype.length = function () {
    return Math.sqrt(this.dotProduct(this));
  }
  Vector.prototype.dotProduct = function (vector) {
    return this.x * vector.x + this.y * vector.y;
  }
  // define a subclass that overrides dotProduct by modifying the result returned by superclass
  function Vector3d(x, y, z) {
    Vector.call(this, x, y);
    this.z = +z;
  }
  inheritPrototype(Vector3d, Vector);
  Vector3d.prototype.dotProduct = function (vector) {
    return Vector.prototype.dotProduct.call(this, vector) + this.z * (vector.z || 0);
  }

  var vec = new Vector(1, 2);
  var vec3d = new Vector3d(2, 3, 4);
  // verify instanceof behavior
  console.assert(vec instanceof Object)
  console.assert(vec instanceof Vector)
  console.assert(!(vec instanceof Vector3d))
  console.assert(vec3d instanceof Object)
  console.assert(vec3d instanceof Vector)
  console.assert(vec3d instanceof Vector3d)
  // verify methods
  console.assert(vec.length() === Math.sqrt(5))
  console.assert(vec.dotProduct(vec3d) === 8)
  console.assert(vec3d.length() === Math.sqrt(4+9+16))
  console.assert(vec3d.dotProduct(vec) === 8)
  console.assert(vec3d.dotProduct(new Vector3d(1, 2, 3)) === 2+6+12)
}
