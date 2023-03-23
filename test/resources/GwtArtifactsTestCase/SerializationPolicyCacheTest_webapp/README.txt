This directory contains a hypothetical WAR consisting of 2 GWT modules:

1) solutions.trsoftware.commons.TestCommons.JUnit.Hosted: copied from /gwtTestsStaging/gwtHostedModeTests/war
2) solutions.trsoftware.commons.TestCommons.JUnit.Web:    copied from /gwtTestsStaging/gwtWebModeTests/war,

It also contains some arbitrary files and directories for testing SerializationPolicyCache.

@see SerializationPolicyCacheTest, GwtArtifactsTestCase

Notable differences between the compiler output for gwtWebModeTests and gwtHostedModeTests:
- rpcPolicyManifest/manifest.txt
  a) gwtWebModeTests:
     manifest.txt contains all the [serviceName]->[.gwt.rpc policy file] mappings
     (which are also specified by the individual mapping files in the rpcPolicyManifest/manifests subdir)
  b) gwtHostedModeTests:
     manifest.txt contains only comments; the actual policy mappings have to be derived from the
     individual mapping files in the rpcPolicyManifest/manifests subdir
