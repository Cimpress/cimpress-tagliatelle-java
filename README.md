# Tagliatelle Java client

This is the SDK for using Tagliatelle service in your Java code.

## Deploying


First prepare the build

    $ mvn release:prepare
    
The next step is optional because we have travis looking at the release tags. 

Travis should build the tagged commit that is made as part of the first step.

Although if you are in a hurry and have access you can perform release manually

    $ mvn release:perform

However make sure not to commit the changes `mvn release:prepare` makes, as this will break the snapshot version.    

