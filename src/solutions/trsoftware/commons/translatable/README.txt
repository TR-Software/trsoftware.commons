This source tree is for replacing certain Java classes with GWT-safe variants
using the super-source element in the module .gwt.xml config. This is exactly
how GWT implements its JRE emulation library.  This code lives in this separate
source tree so that it can be excluded from a standad java compile.