This directory is a copy of /gwtTestsStaging/gwtHostedModeTests/war,
which contains the GWT compiler output for running unit tests in hosted (a.k.a "dev") mode.

It can be used for testing the utils in the solutions.trsoftware.commons.server.gwt package,
which scan the webapp's resources for GWT compiler artifacts.

@see GwtArtifactsTestCase, SerializationPolicyMapTest, BaseGwtTestCase

Notable differences between the compiler output for gwtWebModeTests and gwtHostedModeTests:
- rpcPolicyManifest/manifest.txt
  a) gwtWebModeTests:
     manifest.txt contains all the [serviceName]->[.gwt.rpc policy file] mappings
     (which are also specified by the individual mapping files in the rpcPolicyManifest/manifests subdir)
  b) gwtHostedModeTests:
     manifest.txt contains only comments; the actual policy mappings have to be derived from the
     individual mapping files in the rpcPolicyManifest/manifests subdir
