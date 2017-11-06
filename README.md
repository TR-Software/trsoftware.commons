# TR Software Commons

A collection of libraries for building Java web applications with [GWT](http://www.gwtproject.org/learnmore-sdk.html).

### Getting started

We don't provide any compiled jars for this library; it should be used in source code form (because the GWT compiler requires the source code)
to compile this library directly into your GWT module).  So download the source or clone this repo with git. 

### Dependencies

1. [GWT 2.5.0](https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/google-web-toolkit/gwt-2.5.0.zip)
   We recommend using this older version of GWT to enable recording clientside stack traces using [gwt-stack-trace-kit](https://github.com/aepshteyn/gwt-stack-trace-kit),
   but a newer version of GWT might also work if you don't need accurate clientside exception reporting.
1. [gwt-stack-trace-kit](https://github.com/aepshteyn/gwt-stack-trace-kit): instruments GWT 2.5.0 to provide accurate Java stack traces in clientside code compiled with GWT.

### Projects using this library

- [TypeRacer](http://play.typeracer.com/), the first multiplayer online typing game

### Contributors

- [Alex Epshteyn](https://github.com/aepshteyn), Founder of [TypeRacer](http://play.typeracer.com/) and President of TR Software Inc

_Copyright &copy; 2017 TR Software Inc._