[![inventid logo](https://cdn.inventid.nl/assets/logo-horizontally-ba8ae38ab1f53863fa4e99b977eaa1c7.png)](http://opensource.inventid.nl)

# rfid-reader2keyboard

This is a very simple maven project which reads the code (_uid_) of your RFID chip, and types it as if it were typed on a keyboard.

Although one might think this should be simple, due to the availability of the `javax.smardcardio`, nothing could be further from the truth.
There are a number of issues with this part of the `javax` library:

1. On some platform, methods as `waitForCardAbsent` and `waitForCardPresent` may block indefinitely, albeit setting a timeout value.
1. On some platform, methods as `waitForCardAbsent` and `waitForCardPresent` may continue even if the condition is simply not met.
1. Scanners can become disconnected from your software, causing random failures after a period of time.
1. The exceptions thrown by the JVM may differ per platform. On OSX a CardException may be raised, whereas Windows can trigger a CardNotPresentException under the same circumstances. 

In general, this makes development quite hard and testing close to impossible.
Timeouts may be triggered after several hours of correct functioning, or simply after a minute of idle time.

This small program attempts to mediate these issues, such that scanning of cards is viable crossplatform within the JVM.

## How to run

To run, use `mvn clean install package` which will create two jar files which are executable.
One of these is lean, the other includes all dependencies.
The latter one should be used by clients.
Alternatively, you can run `mvn clean install exec:java` to execute things directly from the CLI.

## Releases

Any successful merge to `master` is automatically available on the [Github release page](https://github.com/inventid/rfid-reader2keyboard/releases).
Always send the most recent version to our customers.

