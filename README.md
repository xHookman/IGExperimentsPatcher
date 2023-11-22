# IGExperimentsPatcher

Enable developer options & experiments access in Instagram apk!

Or you can also just get the method name and class name for my [Xposed module](https://github.com/xHookman/IGExperiments) instead of patching an existing apk

## Usage

Go to release in the project and download latest jar

Open a terminal in the same folder and:

```bash
  java -jar IGExperimentsPatcher.jar -p <path to apk>
  Use -x option to only show class, method and argument to use in my Xposed module
```

Next you will need to sign your patched apk, you can use [Uber Apk Signer](https://github.com/patrickfav/uber-apk-signer/releases) if you don't want to annoy you with keys etc

```bash
  java -jar uber-apk-signer.jar -a <path to apk>
```

In app, long press home button to show the dev options entry.
