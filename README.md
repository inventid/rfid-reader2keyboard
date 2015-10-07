[![inventid logo](https://cdn.inventid.nl/assets/logo-horizontally-ba8ae38ab1f53863fa4e99b977eaa1c7.png)](http://opensource.inventid.nl)

# rfid-reader2keyboard

This is a very simple maven project which reads the code of your rfid chip, and types it in any field.

To run, use `mvn clean install package` which will create two jar files which are executable.
One of these is lean, the other includes all dependencies.
The latter one should be used by clients.
Alternatively, you can run `mvn clean install exec:java` to execute things directly from the CLI.

## Releases

Any successful merge to `master` is automatically available on the [Github release page](https://github.com/inventid/rfid-reader2keyboard/releases).
Always send the most recent version to our customers.

